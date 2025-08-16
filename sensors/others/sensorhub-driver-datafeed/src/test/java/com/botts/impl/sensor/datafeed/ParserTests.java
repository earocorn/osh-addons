package com.botts.impl.sensor.datafeed;

import com.botts.api.parser.data.BaseDataType;
import com.botts.api.parser.data.DataField;
import com.botts.api.parser.data.FieldMapping;
import com.botts.impl.parser.protobuf.ProtobufDataParser;
import com.botts.impl.parser.protobuf.ProtobufDataParserConfig;
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
        config.inputFields = getTestFields();
        config.useDefaultMapping = true;
        config.fieldMapping = sampleMapping;
        config.defaultMessageType = "etf.ETFMessage";
        config.descFilePath = "../../../../../raft_out.desc";
        ProtobufDataParser parser = new ProtobufDataParser(config, new DataRecordImpl());
        DynamicMessage testMsg = parser.generateTestMessage();
        var parsed = parser.parse(testMsg.toByteArray());
//        for (var i : parsed.entrySet())
//            System.out.println(i.getKey() + " : " + i.getValue());
        // Create ui for field input selection
        // hostname, ipaddr, xref
    }

    private List<DataField> getTestFields() {
        List<DataField> testFields = new ArrayList<>();testFields.add(new DataField(0, "etf.ETFMessage.cmd", BaseDataType.STRING));
        testFields.add(new DataField(0, "etf.ETFMessage.loginmsg.hostname", BaseDataType.STRING));
        testFields.add(new DataField(1, "etf.ETFMessage.loginmsg.ipaddr", BaseDataType.STRING));
        testFields.add(new DataField(2, "etf.ETFMessage.loginmsg.xref", BaseDataType.STRING));
        testFields.add(new DataField(3, "etf.ETFMessage.loginmsg.sendrecv", BaseDataType.INTEGER));
        testFields.add(new DataField(3, "etf.ETFMessage.loginmsg", null));
        return testFields;
    }

    @Test
    public void testXML() {

    }

}