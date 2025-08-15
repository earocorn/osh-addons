package com.botts.impl.sensor.datafeed.parsers;

import com.botts.impl.json.JSONDataParserConfig;
import org.sensorhub.api.config.DisplayInfo;

public class JsonConfig extends ParserConfig{
    @DisplayInfo(desc = "json")
    public JSONDataParserConfig parserConfig;
}
