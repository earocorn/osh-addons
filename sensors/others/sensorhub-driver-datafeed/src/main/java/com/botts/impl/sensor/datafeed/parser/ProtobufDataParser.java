package com.botts.impl.sensor.datafeed.parser;

import com.botts.api.sensor.datafeed.parser.AbstractDataParser;
import com.botts.api.sensor.datafeed.parser.IStreamProcessor;
import com.botts.impl.sensor.datafeed.DataFeedUtils;
import com.botts.impl.sensor.datafeed.data.DataField;
import com.botts.impl.sensor.datafeed.parser.config.ProtobufDataParserConfig;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.protobuf.*;
import com.google.protobuf.util.JsonFormat;
import net.opengis.swe.v20.DataBlock;
import net.opengis.swe.v20.DataComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vast.util.Asserts;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ProtobufDataParser extends AbstractDataParser implements IStreamProcessor {

    private final Logger logger = LoggerFactory.getLogger(ProtobufDataParser.class);
    private final ProtobufDataParserConfig config;
    private final Map<String, Descriptors.Descriptor> descriptorMap = new HashMap<>();
    private final Descriptors.Descriptor defaultDescriptor;

    public ProtobufDataParser(ProtobufDataParserConfig config, DataComponent outputStructure) {
        super(config, outputStructure);
        this.config = Asserts.checkNotNull(config, "config");

        try {
            loadDescriptors(config.descFilePath);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        this.defaultDescriptor = descriptorMap.get(config.defaultMessageType);
        if (this.defaultDescriptor == null)
            throw new IllegalArgumentException("No default message type found. Config value: " + config.defaultMessageType);
    }

    private void loadDescriptors(String filepath) throws IOException, Descriptors.DescriptorValidationException {
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

    @Override
    public Map<String, Object> parse(byte[] data) {
        try {
            DynamicMessage message = DynamicMessage.parseFrom(defaultDescriptor, data);
            String jsonString = JsonFormat.printer().includingDefaultValueFields().print(message);
            JsonObject object = JsonParser.parseString(jsonString).getAsJsonObject();

            if (object == null)
                return Collections.emptyMap();

            Map<String, Object> dataMap = new HashMap<>();

            for (DataField field : getInputFields()) {
                if (!object.has(field.name)) {
                    logger.warn("Field {} has no data", field.name);
                    continue;
                }

                String rawValue = object.get(field.name).getAsString();
                Object realValue = DataFeedUtils.parseValue(rawValue, field.dataType);
                dataMap.put(field.name, realValue);
            }

            return dataMap;
        } catch (InvalidProtocolBufferException e) {
            throw new IllegalStateException("Unable to parse message", e);
        }
    }

    @Override
    public void processStream(InputStream inputStream, Consumer<DataBlock> consumer) {
        // TODO: Allow processing of protobuf stream
    }

    @Override
    public void stop() {
        // TODO: Stop protobuf stream processing
    }

}
