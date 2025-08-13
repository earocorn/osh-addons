package com.botts.impl.sensor.datafeed.comm;

import org.sensorhub.api.comm.MessageQueueConfig;
import org.sensorhub.api.config.DisplayInfo;

public class MsgQueueConfig extends CommConfig{
    @DisplayInfo(desc = "Communication settings for using a Message Queue provider")
    public MessageQueueConfig messageQueueCommSettings;
}
