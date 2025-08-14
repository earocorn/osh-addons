package com.botts.ui;

import com.botts.api.sensor.datafeed.parser.DataParserConfig;
import com.botts.impl.sensor.datafeed.data.*;
import com.vaadin.ui.*;
import com.vaadin.v7.data.Property;
import com.vaadin.v7.data.Validator;
import com.vaadin.v7.ui.ComboBox;
import com.vaadin.v7.ui.Field;
import com.vaadin.v7.ui.Table;
import org.sensorhub.ui.DisplayUtils;
import org.sensorhub.ui.FieldWrapper;
import org.sensorhub.ui.GenericConfigForm;
import org.sensorhub.ui.ObjectTypeSelectionPopup;
import org.sensorhub.ui.data.*;

import java.util.*;

public class DataFeedConfigForm extends GenericConfigForm {

    private static final String PROP_PARSER_CONFIG = "dataParserConfig";
    private static final String PROP_FIELD_MAPPING = "fieldMapping";
    private static final String PROP_INPUT_FIELDS = "inputFields";
    private static final String PROP_OUTPUT_STRUCT = "outputStructure";
    private static final String PROP_FIELDS = "fields";

    @Override
    public Map<String, Class<?>> getPossibleTypes(String propId, BaseProperty<?> prop) {
        Map<String, Class<?>> classList = new LinkedHashMap<>();
        if(propId.equals(PROP_PARSER_CONFIG)) {
            ServiceLoader<DataParserConfig> sl = ServiceLoader.load(DataParserConfig.class);
            var it = sl.iterator();

            while (it.hasNext())
            {
                try
                {
                    DataParserConfig parserConfig = it.next();
                    classList.put(parserConfig.getDataParserClass().getSimpleName(), parserConfig.getClass());
                }
                catch (ServiceConfigurationError e)
                {
                    getOshLogger().error("{}: {}", ServiceConfigurationError.class.getName(), e.getMessage());
                }
            }
        }

        if (!classList.isEmpty())
            return classList;

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
