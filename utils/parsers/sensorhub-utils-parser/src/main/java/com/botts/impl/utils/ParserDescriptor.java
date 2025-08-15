package com.botts.impl.utils;

import org.sensorhub.api.module.*;
import org.sensorhub.impl.module.JarModuleProvider;

public class ParserDescriptor extends JarModuleProvider implements IModuleProvider
{
    @Override
    public String getModuleName()
    {
        return "Data Parser";
    }


    @Override
    public String getModuleDescription()
    {
        return "Parses data";
    }


//    @Override
//    public Class<? extends IModuleBase<?>> getModuleClass()
//    {
//        return MqttMessageQueue.class;
//    }
//
//
//    @Override
//    public Class<? extends ModuleConfigBase> getModuleConfigClass()
//    {
//        return MqttMessageQueueConfig.class;
//    }
}
