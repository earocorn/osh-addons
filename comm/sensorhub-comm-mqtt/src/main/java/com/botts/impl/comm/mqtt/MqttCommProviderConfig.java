package com.botts.impl.comm.mqtt;

import org.sensorhub.api.comm.CommProviderConfig;

public class MqttCommProviderConfig extends CommProviderConfig<MqttClientConfig> {


    public MqttCommProviderConfig() {
        this.moduleClass = MqttCommProvider.class.getCanonicalName();
        this.protocol = new MqttClientConfig();
    }

}
