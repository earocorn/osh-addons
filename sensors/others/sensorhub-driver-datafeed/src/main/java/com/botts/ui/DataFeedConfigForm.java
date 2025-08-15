package com.botts.ui;

import com.vaadin.ui.*;
import org.sensorhub.ui.GenericConfigForm;
import org.sensorhub.ui.data.*;

import java.util.*;

public class DataFeedConfigForm extends GenericConfigForm {

    private static final String PROP_PARSER_CONFIG = "dataParserConfig";
    private static final String PROP_FIELD_MAPPING = "fieldMapping";
    private static final String PROP_INPUT_FIELDS = "inputFields";
    private static final String PROP_OUTPUT_STRUCT = "outputStructure";
    private static final String PROP_FIELDS = "fields";
    private static final String PROP_COMM_TYPE = "commType";
    private static final String PROP_PARSER_TYPE = "parserType";

    @Override
    public Map<String, Class<?>> getPossibleTypes(String propId, BaseProperty<?> prop) {

        if(propId.equals(PROP_PARSER_TYPE)){
            Map<String, Class<?>> classList = new LinkedHashMap<>();
            try
            {
                classList.put("CSV", Class.forName("com.botts.impl.sensor.datafeed.parsers.CsvConfig"));
                classList.put("JSON", Class.forName("com.botts.impl.sensor.datafeed.parsers.JsonConfig"));
                classList.put("Line Based", Class.forName("com.botts.impl.sensor.datafeed.parsers.ParserConfig"));
                classList.put("Protobuf", Class.forName("com.botts.impl.sensor.datafeed.parsers.ProtobufConfig"));
                classList.put("XML", Class.forName("com.botts.impl.sensor.datafeed.parsers.XMLConfig"));
            }
            catch (ClassNotFoundException e)
            {
                getOshLogger().error("Cannot find comm class", e);
            }
            return classList;
        }

         else if(propId.equals(PROP_COMM_TYPE)){
            Map<String, Class<?>> classList = new LinkedHashMap<>();
            try
            {
                classList.put("Stream", Class.forName("com.botts.impl.sensor.datafeed.comm.StreamConfig"));
                classList.put("Message Queue", Class.forName("com.botts.impl.sensor.datafeed.comm.MsgQueueCommConfig"));
            }
            catch (ClassNotFoundException e)
            {
                getOshLogger().error("Cannot find comm class", e);
            }
            return classList;
        }

        return super.getPossibleTypes(propId, prop);
    }

    private Map<String, Class<?>> getDataTypeList() {
        Map<String, Class<?>> typeList = new HashMap<>();
        typeList.put("Double", Double.class);
        typeList.put("Float", Float.class);
        typeList.put("Integer", Integer.class);
        typeList.put("Long", Long.class);
        typeList.put("Byte", Byte.class);
        typeList.put("String", String.class);
        typeList.put("Boolean", Boolean.class);
        return typeList;
    }
}
