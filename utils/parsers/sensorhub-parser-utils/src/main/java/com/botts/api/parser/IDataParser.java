package com.botts.api.parser;

import net.opengis.swe.v20.DataBlock;
import net.opengis.swe.v20.DataComponent;

public interface IDataParser {

    DataComponent getRecordStructure();

    DataBlock parse(byte[] data);
}
