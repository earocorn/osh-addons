package com.botts.impl.sensor.datafeed;

import com.botts.impl.sensor.datafeed.data.BaseDataType;
import com.botts.impl.sensor.datafeed.data.DataField;
import com.botts.impl.sensor.datafeed.data.FieldMapping;
import com.botts.impl.sensor.datafeed.parser.ProtobufDataParser;
import com.botts.impl.sensor.datafeed.parser.config.ProtobufDataParserConfig;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import org.junit.Before;
import org.junit.Test;
import org.vast.data.DataRecordImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
        field.index = 0;
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
        DataField hostname = new DataField();
        hostname.name = "hostname";
        hostname.dataType = BaseDataType.STRING;

        DataField ipaddr = new DataField();
        ipaddr.name = "ipaddr";
        ipaddr.dataType = BaseDataType.STRING;

        DataField xref = new DataField();
        xref.name = "xref";
        xref.dataType = BaseDataType.STRING;

        config.inputFields.addAll(Set.of(hostname, ipaddr, xref));
        config.fieldMapping = sampleMapping;
        config.defaultMessageType = "etf.ETFMessage";
        config.descFilePath = "../../../../../raft_out.desc";
        ProtobufDataParser parser = new ProtobufDataParser(config, new DataRecordImpl());
        DynamicMessage testMsg = parser.generateTestMessage();
        var parsed = parser.parse(testMsg.toByteArray());
        // Create ui for field input selection
        // hostname, ipaddr, xref
    }

    @Test
    public void testXML() {

    }

}
