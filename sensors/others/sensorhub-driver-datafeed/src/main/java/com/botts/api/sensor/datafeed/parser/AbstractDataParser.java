package com.botts.api.sensor.datafeed.parser;

import com.botts.impl.sensor.datafeed.data.DataField;
import net.opengis.swe.v20.DataBlock;
import net.opengis.swe.v20.DataComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vast.util.Asserts;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;

import static com.botts.impl.sensor.datafeed.DataFeedUtils.setComponentData;

public abstract class AbstractDataParser implements IDataParser {

    private static final Logger log = LoggerFactory.getLogger(AbstractDataParser.class);
    private DataParserConfig config;
    boolean isRunning = false;
    private Thread parserThread;
    private DataComponent outputStructure;
    private List<DataField> inputFields;
    private Map<String, String> fieldMap;

    public List<DataField> getInputFields() {
        return inputFields;
    }
    public AbstractDataParser(DataParserConfig config, DataComponent outputStructure) {
        this.config = config;
        Asserts.checkNotNull(config, "config");
        // TODO Build structure from config
        this.outputStructure = outputStructure;
        Asserts.checkNotNull(config.outputStructure, "outputStructure");

        // Ensure we are at least sorting by cardinality
        this.inputFields = config.inputFields.stream()
                .sorted(Comparator.comparingInt(d -> d.cardinality))
                .toList();
        Asserts.checkNotNull(inputFields, "inputFields");

        this.fieldMap = new HashMap<>();
        for (var fieldMapEntry : config.fieldMapping)
            fieldMap.put(fieldMapEntry.inputFieldName, fieldMapEntry.outputFieldName);
    }

    @Override
    public void subscribe(InputStream inputStream, Consumer<DataBlock> handler) {
        if (parserThread != null)
            unsubscribe();
        isRunning = true;

        parserThread = new Thread(() -> {
            try {
                BufferedInputStream bis = new BufferedInputStream(inputStream);
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int b;
                while ((b = bis.read()) != -1 && isRunning) {
                    if (b == '\n') {
                        byte[] line = buffer.toByteArray();
                        buffer.reset();
                        var data = parse(line);
                        if (data != null)
                            handler.accept(mappedData(data));
                    } else {
                        buffer.write(b);
                    }
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        });

        parserThread.start();
    }

    @Override
    public void unsubscribe() {
        parserThread.interrupt();
        parserThread = null;
        isRunning = false;
    }

    private DataBlock mappedData(Map<String, Object> data) {
        this.outputStructure.renewDataBlock();
        this.outputStructure.getComponent(0).getData().setDoubleValue(System.currentTimeMillis() / 1000d);

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String inputFieldName = entry.getKey();
            Object value = entry.getValue();
            String outputFieldName = fieldMap.get(inputFieldName);
            if (outputFieldName == null)
                throw new IllegalArgumentException("Fields are not mapped properly. Please check configuration");

            setComponentData(this.outputStructure.getComponent(outputFieldName), value);
        }
        // TODO: Check that component datablocks update parent datablock
        return this.outputStructure.getData();
    }

    /**
     * Parses the expected input to a map of data values
     * and names from configured inputFields. The parsing method
     * should use names and/or cardinality from the configured inputFields.
     *
     * @param data
     * @return
     */
    @Override
    public abstract Map<String, Object> parse(byte[] data);
}
