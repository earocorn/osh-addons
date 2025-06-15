package com.botts.impl.sensor.datafeed.parser;

import com.botts.impl.sensor.datafeed.data.DataField;
import com.botts.impl.sensor.datafeed.parser.config.CSVDataParserConfig;

import java.util.HashMap;
import java.util.Map;

import static com.botts.impl.sensor.datafeed.Utils.parseValue;

public class CSVDataParser extends AbstractDataParser {

    private boolean hasSkippedHeader = false;
    private final CSVDataParserConfig config;

    public CSVDataParser(CSVDataParserConfig config) {
        super(config);
        this.config = config;
    }

    @Override
    public Map<String, Object> parse(String data) {
        if (!hasSkippedHeader) {
            hasSkippedHeader = true;
            return null;
        }

        String[] values = data.split(config.delimiter);
        if (values.length != inputFields.size())
            throw new IllegalArgumentException("Number of values (" + values.length +  ") does not match number of fields (" + inputFields.size() + ")");

        Map<String, Object> dataMap = new HashMap<>();
        int valueIndex = 0;
        for (DataField field : inputFields) {
            String rawValue = values[valueIndex].trim();
            Object realValue = parseValue(rawValue, field.dataType);
            dataMap.put(field.name, realValue);
        }

        return dataMap;
    }
}
