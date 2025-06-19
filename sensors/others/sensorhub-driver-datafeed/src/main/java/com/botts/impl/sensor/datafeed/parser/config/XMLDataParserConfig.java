package com.botts.impl.sensor.datafeed.parser.config;

import com.botts.api.sensor.datafeed.parser.DataParserConfig;
import com.botts.api.sensor.datafeed.parser.IDataParser;
import com.botts.impl.sensor.datafeed.parser.XMLDataParser;

public class XMLDataParserConfig extends DataParserConfig {

    @Override
    public Class<? extends IDataParser> getDataParserClass() {
        return XMLDataParser.class;
    }
}
