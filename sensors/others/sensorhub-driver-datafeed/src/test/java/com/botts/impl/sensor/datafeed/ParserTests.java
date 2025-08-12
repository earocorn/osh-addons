package com.botts.impl.sensor.datafeed;

import com.botts.impl.sensor.datafeed.data.BaseDataType;
import com.botts.impl.sensor.datafeed.data.DataField;
import com.botts.impl.sensor.datafeed.data.FieldMapping;
import com.botts.impl.sensor.datafeed.parser.ProtobufDataParser;
import com.botts.impl.sensor.datafeed.parser.config.ProtobufDataParserConfig;
import com.google.protobuf.Descriptors;
import org.junit.Before;
import org.junit.Test;
import org.sensorhub.utils.ModuleUtils;
import org.vast.data.DataRecordImpl;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ParserTests {

    List<DataField> sampleFields;
    List<FieldMapping> sampleMapping;

    @Before
    public void setup() {
        sampleFields = new ArrayList<>();
        sampleMapping = new ArrayList<>();
        DataField field = new DataField();
        field.dataType = BaseDataType.FLOAT;
        field.name = "test";
        field.ordinality = 0;
        sampleFields.add(field);

        FieldMapping mapping = new FieldMapping();
        mapping.inputFieldName = "test";
        mapping.outputFieldName = "test";
        sampleMapping.add(mapping);
    }

    @Test
    public void testCSV() {

    }

    @Test
    public void testJSON() {

    }

    @Test
    public void testProtobuf() throws Descriptors.DescriptorValidationException, IOException {
        ProtobufDataParserConfig config = new ProtobufDataParserConfig();
        config.inputFields = sampleFields;
        config.fieldMapping = sampleMapping;
        config.defaultMessageType = "EtfLoginMsg";
        config.descFilePath = "../../../../../raft.proto";
        System.out.println(System.getProperty("user.dir"));
        ProtobufDataParser parser = new ProtobufDataParser(config, new DataRecordImpl());
    }

    @Test
    public void testXML() {

    }

}
