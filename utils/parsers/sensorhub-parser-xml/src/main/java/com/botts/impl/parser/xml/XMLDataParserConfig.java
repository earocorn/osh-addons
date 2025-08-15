package com.botts.impl.parser.xml;

import com.botts.api.parser.DataParserConfig;
import com.botts.api.parser.IDataParser;

public abstract class XMLDataParserConfig extends DataParserConfig {

    @Override
    public Class<? extends IDataParser> getDataParserClass() {
        return XMLDataParser.class;
    }
}
