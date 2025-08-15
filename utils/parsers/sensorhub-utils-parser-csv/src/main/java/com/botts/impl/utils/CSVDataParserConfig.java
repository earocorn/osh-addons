package com.botts.impl.utils;


public class CSVDataParserConfig extends DataParserConfig {

    public String delimiter = ",";

    @Override
    public Class<? extends IDataParser> getDataParserClass() {
        return CSVDataParser.class;
    }
}
