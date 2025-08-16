package com.botts.impl.parser.protobuf;

import com.botts.api.parser.DataParserConfig;
import com.botts.api.parser.IDataParser;
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
