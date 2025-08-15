package com.botts.impl.json;

import org.sensorhub.api.module.*;
import org.sensorhub.impl.module.JarModuleProvider;

public class JsonParserDescriptor extends JarModuleProvider implements IModuleProvider
{
    @Override
    public String getModuleName()
    {
        return "JSON Data Parser";
    }


    @Override
    public String getModuleDescription()
    {
        return "Parses data formatted in json";
    }

}
