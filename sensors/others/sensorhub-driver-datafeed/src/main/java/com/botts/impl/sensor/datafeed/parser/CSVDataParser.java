package com.botts.impl.sensor.datafeed.parser;

import com.botts.api.sensor.datafeed.parser.AbstractDataParser;
import com.botts.impl.sensor.datafeed.data.DataField;
import com.botts.impl.sensor.datafeed.parser.config.CSVDataParserConfig;
import net.opengis.swe.v20.DataComponent;

import java.util.HashMap;
import java.util.Map;

import static com.botts.impl.sensor.datafeed.DataFeedUtils.parseValue;

public class CSVDataParser extends AbstractDataParser {

    // TODO Add to config
    private boolean hasSkippedHeader = false;
    private CSVDataParserConfig config;

    public CSVDataParser(CSVDataParserConfig config, DataComponent outputStructure) {
        super(config, outputStructure);
        this.config = config;
    }

    @Override
    public Map<String, Object> parse(byte[] data) {
        String line = new String(data);
        if (!hasSkippedHeader) {
            hasSkippedHeader = true;
            return null;
        }

        String[] values = line.split(config.delimiter);
        if (values.length != getInputFields().size())
            throw new IllegalArgumentException("Number of values (" + values.length +  ") does not match number of fields (" + getInputFields().size() + ")");

        Map<String, Object> dataMap = new HashMap<>();
        int valueIndex = 0;
        for (DataField field : getInputFields()) {
            String rawValue = values[valueIndex++].trim();
            Object realValue = parseValue(rawValue, field.dataType);
            dataMap.put(field.name, realValue);
        }

        return dataMap;
    }
}
