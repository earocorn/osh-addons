package com.botts.ui;

import com.botts.api.sensor.datafeed.parser.DataParserConfig;
import org.sensorhub.ui.GenericConfigForm;
import org.sensorhub.ui.data.BaseProperty;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

public class DataParserConfigForm extends GenericConfigForm {

    private static final String DRIVER_CONFIG_PACKAGE = "com.botts.impl.sensor.datafeed.";
    private static final String PROP_PARSER_CONFIG = "dataParserConfig";

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
}
