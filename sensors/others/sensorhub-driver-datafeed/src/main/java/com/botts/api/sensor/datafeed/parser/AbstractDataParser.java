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
import java.util.stream.Collectors;

import static com.botts.impl.sensor.datafeed.DataFeedUtils.setComponentData;

public abstract class AbstractDataParser implements IDataParser {

    private static final Logger log = LoggerFactory.getLogger(AbstractDataParser.class);
    private final DataComponent outputStructure;
    private final List<DataField> inputFields;
    private final Map<String, String> fieldMap;

    public List<DataField> getInputFields() {
        return inputFields;
    }

    public AbstractDataParser(DataParserConfig config, DataComponent outputStructure) {
        Asserts.checkNotNull(config, "config");
        Asserts.checkNotNull(config.outputStructure, "config.outputStructure");
        this.outputStructure = Asserts.checkNotNull(outputStructure, "outputStructure");

        // Ensure we are at least sorting by ordinality
        this.inputFields = Asserts.checkNotNull(config.inputFields, "inputFields").stream()
                .sorted(Comparator.comparingInt(d -> d.ordinality))
                .collect(Collectors.toList());

        this.fieldMap = config.fieldMapping.stream()
                .collect(Collectors.toMap(
                        entry -> entry.inputFieldName,
                        entry -> entry.outputFieldName
                ));
    }

    @Override
    public DataComponent getRecordStructure() {
        return outputStructure;
    }

    @Override
    public DataBlock createDataBlock(Map<String, Object> parsedData) {
        this.outputStructure.renewDataBlock();
        // Set timestamp
        this.outputStructure.getComponent(0).getData().setDoubleValue(System.currentTimeMillis() / 1000d);

        for (Map.Entry<String, Object> entry : parsedData.entrySet()) {
            String inputFieldName = entry.getKey();
            Object value = entry.getValue();
            String outputFieldName = fieldMap.get(inputFieldName);
            if (outputFieldName == null)
                throw new IllegalArgumentException("Fields are not mapped properly. Please check configuration");

            setComponentData(this.outputStructure.getComponent(outputFieldName), value);
        }
        return this.outputStructure.getData();
    }

}
