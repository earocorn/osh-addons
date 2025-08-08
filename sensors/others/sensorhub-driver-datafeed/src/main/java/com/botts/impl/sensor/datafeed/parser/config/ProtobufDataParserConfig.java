package com.botts.impl.sensor.datafeed.parser.config;

import com.botts.api.sensor.datafeed.parser.DataParserConfig;
import com.botts.api.sensor.datafeed.parser.IDataParser;
import com.botts.impl.sensor.datafeed.parser.ProtobufDataParser;
import org.sensorhub.api.config.DisplayInfo;

public class ProtobufDataParserConfig extends DataParserConfig {

    @DisplayInfo(label="Protobuf Descriptor File Path", desc = "Filepath of Protobuf schema descriptor file")
    public String descFilePath;

    @DisplayInfo(desc = "Default message type specified by proto file")
    public String defaultMessageType;

    @Override
    public Class<? extends IDataParser> getDataParserClass() {
        return ProtobufDataParser.class;
    }
}
