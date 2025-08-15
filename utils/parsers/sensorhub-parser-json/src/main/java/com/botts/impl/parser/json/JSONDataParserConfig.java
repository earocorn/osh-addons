package com.botts.impl.parser.json;


import com.botts.api.parser.DataParserConfig;
import com.botts.api.parser.IDataParser;
import org.sensorhub.api.config.DisplayInfo;

public class JSONDataParserConfig extends DataParserConfig {

    @DisplayInfo(label="Pretty Print", desc="")
    public boolean isPretty = false;

    @Override
    public Class<? extends IDataParser> getDataParserClass() {
        return JSONDataParser.class;
    }
}
