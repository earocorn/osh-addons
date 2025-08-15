package com.botts.impl.utils;


public class XMLDataParserConfig extends DataParserConfig {

    @Override
    public Class<? extends IDataParser> getDataParserClass() {
        return XMLDataParser.class;
    }
}
