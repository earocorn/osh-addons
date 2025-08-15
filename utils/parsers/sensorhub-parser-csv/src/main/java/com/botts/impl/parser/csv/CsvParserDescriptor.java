package com.botts.impl.parser.csv;

import org.sensorhub.api.module.*;
import org.sensorhub.impl.module.JarModuleProvider;

public class CsvParserDescriptor extends JarModuleProvider implements IModuleProvider
{
    @Override
    public String getModuleName()
    {
        return "CSV Data Parser";
    }


    @Override
    public String getModuleDescription()
    {
        return "Parses data formatted in csv";
    }

    /**
     * Retrieves the class implementing the OpenSensorHub interface necessary to perform SOS/SPS/SOS-T operations.
     *
     * @return The class used to interact with the sensor/sensor platform.
     */
    @Override
    public Class<? extends IModuleBase<?>> getModuleClass() {
        return CSVDataParser.class;
    }

    /**
     * Identifies the class used to configure this driver.
     *
     * @return The java class used to exposing configuration settings for the driver.
     */
    @Override
    public Class<? extends ModuleConfigBase> getModuleConfigClass() {
        return CSVDataParserConfig.class;
    }
}
