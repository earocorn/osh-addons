package com.botts.impl.comm.kafka;

import org.sensorhub.api.module.*;
import org.sensorhub.impl.module.JarModuleProvider;

public class KafkaMessageQueueDescriptor extends JarModuleProvider implements IModuleProvider
{
    @Override
    public String getModuleName()
    {
        return "Kafka Comm Provider";
    }
    

    @Override
    public String getModuleDescription()
    {
        return "Kafka communication provider using Apache Kafka Client";
    }
    

    @Override
    public Class<? extends IModuleBase<?>> getModuleClass()
    {
        return KafkaMessageQueue.class;
    }
    

    @Override
    public Class<? extends ModuleConfigBase> getModuleConfigClass()
    {
        return KafkaMessageQueueConfig.class;
    }
}
