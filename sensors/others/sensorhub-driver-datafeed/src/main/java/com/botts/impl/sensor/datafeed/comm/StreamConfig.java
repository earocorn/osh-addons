package com.botts.impl.sensor.datafeed.comm;

import org.sensorhub.api.comm.CommProviderConfig;
import org.sensorhub.api.comm.MessageQueueConfig;
import org.sensorhub.api.config.DisplayInfo;

public class StreamConfig extends CommConfig {

    @DisplayInfo(desc = "Communication settings for using a stream (TCP, UDP, serial, etc.)")
    public CommProviderConfig<?> streamCommSettings;
}
