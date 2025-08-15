package com.botts.impl.utils;

import org.sensorhub.api.module.*;
import org.sensorhub.impl.module.JarModuleProvider;

public class LineBasedStreamDescriptor extends JarModuleProvider implements IModuleProvider
{
    @Override
    public String getModuleName()
    {
        return "Line-Based Streamer";
    }


    @Override
    public String getModuleDescription()
    {
        return "";
    }

}
