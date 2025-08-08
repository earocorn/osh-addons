package com.botts.api.sensor.datafeed.parser;

import net.opengis.swe.v20.DataBlock;
import net.opengis.swe.v20.DataComponent;

import java.util.Map;

public interface IDataParser {

    DataComponent getRecordStructure();

    DataBlock createDataBlock(Map<String, Object> parsedData);

    Map<String, Object> parse(byte[] data);
}
