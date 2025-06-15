package com.botts.impl.sensor.datafeed.parser.config;

import com.botts.api.sensor.datafeed.parser.DataParserConfig;
import com.botts.api.sensor.datafeed.parser.IDataParser;
import com.botts.impl.sensor.datafeed.parser.CSVDataParser;

public class CSVDataParserConfig extends DataParserConfig {

    public String delimiter = ",";

    @Override
    public Class<? extends IDataParser> getDataParserClass() {
        return CSVDataParser.class;
    }
}
