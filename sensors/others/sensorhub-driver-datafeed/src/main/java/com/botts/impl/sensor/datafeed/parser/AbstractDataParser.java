package com.botts.impl.sensor.datafeed.parser;

import com.botts.api.sensor.datafeed.parser.DataParserConfig;
import com.botts.api.sensor.datafeed.parser.IDataParser;
import com.botts.impl.sensor.datafeed.data.DataField;
import net.opengis.swe.v20.DataBlock;
import net.opengis.swe.v20.DataComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vast.util.Asserts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Consumer;

import static com.botts.impl.sensor.datafeed.DataFeedUtils.setComponentData;

public class AbstractDataParser implements IDataParser {

    private static final Logger log = LoggerFactory.getLogger(AbstractDataParser.class);
    DataParserConfig config;
    boolean isRunning = false;
    Thread parserThread;

    DataComponent outputStructure;
    List<DataField> inputFields;

    public AbstractDataParser(DataParserConfig config) {
        this.config = config;
        Asserts.checkNotNull(config, "config");
        this.outputStructure = config.outputStructure;
        Asserts.checkNotNull(config.outputStructure, "outputStructure");

        // Ensure we are at least sorting by cardinality
        this.inputFields = config.inputFields.stream()
                .sorted(Comparator.comparingInt(d -> d.cardinality))
                .toList();
        Asserts.checkNotNull(inputFields, "inputFields");
    }

    @Override
    public DataParserConfig getConfiguration() {
        return config;
    }

    @Override
    public void subscribe(InputStream inputStream, Consumer<DataBlock> handler) {
        if (parserThread != null)
            unsubscribe();
        isRunning = true;

        parserThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while (isRunning && (line = reader.readLine()) != null) {
                    var data = parse(line);
                    if (data != null)
                        handler.accept(mappedData(data));
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
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String fieldName = entry.getKey();
            Object value = entry.getValue();
            String mappedFieldName = config.fieldMapping.stream().toList().get(0).value;
            setComponentData(this.outputStructure.getComponent(mappedFieldName), value);
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
    public Map<String, Object> parse(String data) {
        throw new UnsupportedOperationException();
    }

//    @Override
//    public Map<String, Object> parse(byte[] data) {
//        throw new UnsupportedOperationException();
//    }

    @Override
    public void setConfiguration(DataParserConfig config) {
        this.config = config;
    }
}
