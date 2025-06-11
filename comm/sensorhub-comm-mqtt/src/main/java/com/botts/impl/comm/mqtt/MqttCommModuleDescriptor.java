package com.botts.impl.comm.mqtt;

import org.sensorhub.api.module.IModule;
import org.sensorhub.api.module.IModuleProvider;
import org.sensorhub.api.module.ModuleConfig;
import org.sensorhub.impl.module.JarModuleProvider;

public class MqttCommModuleDescriptor extends JarModuleProvider implements IModuleProvider
{
    @Override
    public String getModuleName()
    {
        return "MQTT Comm Driver";
    }
    

    @Override
    public String getModuleDescription()
    {
        return "Simple MQTT communication provider using Eclipse Paho MQTT Client";
    }
    

    @Override
    public Class<? extends IModule<?>> getModuleClass()
    {
        return MqttCommProvider.class;
    }
    

    @Override
    public Class<? extends ModuleConfig> getModuleConfigClass()
    {
        return MqttCommProviderConfig.class;
    }
}
