package com.botts.impl.comm.kafka;

import org.sensorhub.api.comm.CommProviderConfig;

public class KafkaCommProviderConfig extends CommProviderConfig<KafkaClientConfig> {

    public KafkaCommProviderConfig() {
        this.moduleClass = KafkaCommProvider.class.getCanonicalName();
        this.protocol = new KafkaClientConfig();
    }

}
