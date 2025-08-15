package com.botts.api.parser;

import net.opengis.swe.v20.DataBlock;

import java.io.InputStream;
import java.util.function.Consumer;

public interface IStreamProcessor {

   void processStream(InputStream inputStream, Consumer<DataBlock> consumer);

   void stop();

}
