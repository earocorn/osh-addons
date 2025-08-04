package com.botts.impl.comm.kafka;

import org.sensorhub.api.module.IModule;
import org.sensorhub.api.module.IModuleProvider;
import org.sensorhub.api.module.ModuleConfig;
import org.sensorhub.impl.module.JarModuleProvider;

public class KafkaCommModuleDescriptor extends JarModuleProvider implements IModuleProvider
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
    public Class<? extends IModule<?>> getModuleClass()
    {
        return KafkaCommProvider.class;
    }
    

    @Override
    public Class<? extends ModuleConfig> getModuleConfigClass()
    {
        return KafkaCommProviderConfig.class;
    }
}
