package com.botts.impl.utils;

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
