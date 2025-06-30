package com.botts.api.sensor.datafeed.parser;

import net.opengis.swe.v20.DataBlock;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.function.Consumer;

public interface IDataParser {

    void subscribe(InputStream inputStream, Consumer<DataBlock> consumer) throws IOException;

    void unsubscribe();

    Map<String, Object> parse(byte[] data);
}
