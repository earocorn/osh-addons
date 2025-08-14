package com.botts.impl.sensor.datafeed.parser;

import com.botts.api.sensor.datafeed.parser.AbstractDataParser;
import com.botts.impl.sensor.datafeed.DataFeedUtils;
import com.botts.impl.sensor.datafeed.data.DataField;
import com.botts.impl.sensor.datafeed.parser.config.CSVDataParserConfig;
import java.util.Collections;

import net.opengis.swe.v20.DataBlock;
import net.opengis.swe.v20.DataComponent;
import org.vast.sensorML.SMLHelper;
import org.vast.swe.SWEHelper;

import java.util.HashMap;
import java.util.Map;

public class CSVDataParser extends AbstractDataParser {

    // TODO Add to config
    private boolean hasSkippedHeader = false;
    private final CSVDataParserConfig config;

    public CSVDataParser(CSVDataParserConfig config, DataComponent outputStructure) {
        super(config, outputStructure);
        this.config = config;
    }


    @Override
    public DataBlock parse(byte[] data) {
        DataBlock dataBlock = getRecordStructure().createDataBlock();

        String line = new String(data);
        if (!hasSkippedHeader) {
            hasSkippedHeader = true;
            return dataBlock;
        }
        String[] values = line.split(config.delimiter);


        for (DataField field : getInputFields()) {
            String rawValue = values[field.index].trim();
            Object realValue = DataFeedUtils.parseValue(rawValue, field.dataType);
            DataFeedUtils.setFieldData(field.index, realValue, dataBlock);
        }

        return dataBlock;
    }
}
