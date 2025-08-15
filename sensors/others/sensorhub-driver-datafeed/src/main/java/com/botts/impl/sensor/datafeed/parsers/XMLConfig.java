package com.botts.impl.sensor.datafeed.parsers;

import com.botts.api.sensor.datafeed.parser.DataParserConfig;
import com.botts.impl.utils.XMLDataParserConfig;
import org.sensorhub.api.config.DisplayInfo;

public class XMLConfig extends ParserConfig {
    @DisplayInfo(desc = "json")
    public XMLDataParserConfig parserConfig;
}
