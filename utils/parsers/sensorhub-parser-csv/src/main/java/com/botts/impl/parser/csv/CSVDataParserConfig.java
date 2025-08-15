package com.botts.impl.parser.csv;


import com.botts.api.parser.DataParserConfig;
import com.botts.api.parser.IDataParser;

public class CSVDataParserConfig extends DataParserConfig {

    public String delimiter = ",";

    @Override
    public Class<? extends IDataParser> getDataParserClass() {
        return CSVDataParser.class;
    }
}
