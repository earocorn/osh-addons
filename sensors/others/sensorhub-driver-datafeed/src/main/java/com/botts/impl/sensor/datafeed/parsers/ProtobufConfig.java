package com.botts.impl.sensor.datafeed.parsers;

import com.botts.impl.utils.ProtobufDataParserConfig;
import org.sensorhub.api.config.DisplayInfo;

public class ProtobufConfig extends ParserConfig{
    @DisplayInfo(desc = "protobuf")
    public ProtobufDataParserConfig parserConfig;
}
