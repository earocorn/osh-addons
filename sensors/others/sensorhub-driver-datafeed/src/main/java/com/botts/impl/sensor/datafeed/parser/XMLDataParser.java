package com.botts.impl.sensor.datafeed.parser;

import com.botts.api.sensor.datafeed.parser.AbstractDataParser;
import com.botts.api.sensor.datafeed.parser.DataParserConfig;
import net.opengis.swe.v20.DataComponent;

import java.util.Map;

public class XMLDataParser extends AbstractDataParser {

    DataComponent outputStructure;

    public XMLDataParser(DataParserConfig config, DataComponent outputStructure) {
        super(config, outputStructure);
    }

    @Override
    public Map<String, Object> parse(byte[] data) {
        return null;
    }
}
