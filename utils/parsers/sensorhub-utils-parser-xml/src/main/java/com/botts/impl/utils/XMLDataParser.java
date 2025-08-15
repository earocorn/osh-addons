package com.botts.impl.utils;

import net.opengis.swe.v20.DataBlock;
import net.opengis.swe.v20.DataComponent;
import org.sensorhub.api.common.SensorHubException;

import java.util.Map;

public class XMLDataParser extends AbstractDataParser {

    DataComponent outputStructure;

    public XMLDataParser(DataParserConfig config, DataComponent outputStructure) {
        super(config, outputStructure);
    }

    @Override
    public DataBlock parse(byte[] data) {
        return null;
    }

    @Override
    public void start() throws SensorHubException {

    }

    @Override
    public void stop() throws SensorHubException {

    }
}