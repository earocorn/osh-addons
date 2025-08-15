package com.botts.impl.json;


import com.botts.impl.utils.DataParserConfig;
import com.botts.impl.utils.IDataParser;

public class JSONDataParserConfig extends DataParserConfig {

    @Override
    public Class<? extends IDataParser> getDataParserClass() {
        return JSONDataParser.class;
    }
}
