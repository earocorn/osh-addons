package com.botts.impl.parser.protobuf;


import com.botts.api.parser.AbstractDataParser;
import com.botts.api.parser.IStreamProcessor;
import com.botts.api.parser.data.BaseDataType;
import com.botts.api.parser.data.DataFeedUtils;
import com.botts.api.parser.data.DataField;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.protobuf.*;
import com.google.protobuf.util.JsonFormat;
import net.opengis.swe.v20.DataBlock;
import net.opengis.swe.v20.DataComponent;
import org.sensorhub.api.common.SensorHubException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vast.util.Asserts;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class ProtobufDataParser extends AbstractDataParser implements IStreamProcessor {

    private final Logger logger = LoggerFactory.getLogger(ProtobufDataParser.class);
    private final ProtobufDataParserConfig config;
    private final Map<String, Descriptors.Descriptor> descriptorMap = new HashMap<>();
    private final Descriptors.Descriptor defaultDescriptor;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Object taskLock = new Object();
    private volatile Future<?> task;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    public ProtobufDataParser(ProtobufDataParserConfig config, DataComponent outputStructure) {
        super(config, outputStructure);
        this.config = Asserts.checkNotNull(config, "config");
        Asserts.checkNotNull(config.descFilePath);

        try {
            loadDescriptors(config.descFilePath);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        this.defaultDescriptor = descriptorMap.get(config.defaultMessageType);
        if (this.defaultDescriptor == null)
            throw new IllegalArgumentException("No default message type found. Config value: " + config.defaultMessageType);
    }

    public void loadDescriptors(String filepath) throws IOException, Descriptors.DescriptorValidationException {
        DescriptorProtos.FileDescriptorSet set = DescriptorProtos.FileDescriptorSet.parseFrom(new FileInputStream(filepath));
        Map<String, Descriptors.FileDescriptor> fileDescriptorMap = new HashMap<>();

        for (DescriptorProtos.FileDescriptorProto proto : set.getFileList()) {
            Descriptors.FileDescriptor[] deps = new Descriptors.FileDescriptor[proto.getDependencyCount()];
            for (int i = 0; i < proto.getDependencyCount(); i++)
                deps[i] = fileDescriptorMap.get(proto.getDependency(i));

            Descriptors.FileDescriptor fileDescriptor = Descriptors.FileDescriptor.buildFrom(proto, deps);
            fileDescriptorMap.put(fileDescriptor.getName(), fileDescriptor);

            for (Descriptors.Descriptor messageType : fileDescriptor.getMessageTypes())
                descriptorMap.put(messageType.getFullName(), messageType);
        }
    }

    public DynamicMessage generateTestMessage() {
        Descriptors.Descriptor msgDesc = descriptorMap.get("etf.ETFMessage");
        Descriptors.Descriptor loginMsgDesc = descriptorMap.get("etf.EtfLoginMsg");

        DynamicMessage loginMsg = DynamicMessage.newBuilder(loginMsgDesc)
                .setField(loginMsgDesc.findFieldByName("hostname"), "hartmann")
                .setField(loginMsgDesc.findFieldByName("ipaddr"), "65.105.136.92")
                .setField(loginMsgDesc.findFieldByName("xref"), "https://www.normand-lesch.io/voluptatum/cumque?voluptatibus=consectetur&odio=beatae")
                .setField(loginMsgDesc.findFieldByName("sendrecv"), 2)
                .build();

        var cmdField = msgDesc.findFieldByName("cmd");
        var cmdEnum = cmdField.getEnumType();
        var loginEnumValue = cmdEnum.findValueByName("ETF_LOGIN_MSG");

        return DynamicMessage.newBuilder(msgDesc)
                .setField(cmdField, loginEnumValue)
                .setField(msgDesc.findFieldByName("loginmsg"), loginMsg)
                .build();
    }

    public static Object findInJsonObject(JsonObject root, String fieldPath, BaseDataType dataType) {
        String[] parts = fieldPath.split("\\.");

        if (parts.length <= 2)
            return null;

        JsonElement current = root;

        for (int i = 2; i < parts.length; i++) {
            String part = parts[i];
            if (current != null && current.isJsonObject()) {
                JsonObject obj = current.getAsJsonObject();
                if (!obj.has(part))
                    return null;
                current = obj.get(part);
            } else
                return null;
        }

        if (current != null && current.isJsonPrimitive())
            return DataFeedUtils.parseValue(current.getAsString(), dataType);

        return null;
    }

    @Override
    public DataBlock parse(byte[] data) {
        try {
            DataBlock dataBlock = getRecordStructure().createDataBlock();
            DynamicMessage message = DynamicMessage.parseFrom(defaultDescriptor, data);
            String jsonString = JsonFormat.printer().includingDefaultValueFields().print(message);
            JsonObject object = JsonParser.parseString(jsonString).getAsJsonObject();

            if (object == null)
                return dataBlock;


            for (DataField field : getInputFields()) {
                Object realValue = findInJsonObject(object, field.name, field.dataType);
                if (realValue == null) {
                    logger.warn("Field {} has no data", field.name);
                    continue;
                }

                DataFeedUtils.setFieldData(getRecordStructure().getComponentIndex(field.name), realValue, dataBlock);
            }

            return dataBlock;
        } catch (InvalidProtocolBufferException e) {
            throw new IllegalStateException("Unable to parse message", e);
        }
    }

    @Override
    public void processStream(InputStream inputStream, Consumer<DataBlock> consumer) {
        Asserts.checkNotNull(inputStream, "inputStream");
        Asserts.checkNotNull(consumer, "consumer");

        if (isRunning.compareAndSet(false, true)) {
            logger.warn("Stream is already running");
            return;
        }

        logger.debug("Starting stream processing");
        synchronized (taskLock) {
            task = executor.submit(() -> {
                try {
                    CodedInputStream protobufStream = CodedInputStream.newInstance(inputStream);

                    while (isRunning.get() && (!protobufStream.isAtEnd())) {
                        int size = protobufStream.readRawVarint32();
                        int oldLimit = protobufStream.pushLimit(size);
                        byte[] messageBytes = protobufStream.readRawBytes(size);
                        protobufStream.popLimit(oldLimit);
                        DataBlock dataBlock = parse(messageBytes);
                        consumer.accept(dataBlock);
                    }
                } catch (IOException e) {
                    logger.error("Stream processing error", e);
                }
            });
        }
    }

    @Override
    public void start() throws SensorHubException {

    }

    @Override
    public void stop() {
        logger.debug("Stopping stream processor");

        isRunning.set(false);

        synchronized (taskLock) {
            if (task != null) {
                task.cancel(true);
                task = null;
            }
        }

        logger.debug("Stream processor stopped");
    }

}
