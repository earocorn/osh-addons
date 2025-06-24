package com.botts.impl.driver.sensorthings;

import org.sensorhub.api.config.DisplayInfo;

public class StreamConfig {


    @DisplayInfo.Required
    @DisplayInfo(label = "MQTT Broker")
    @DisplayInfo.FieldType(DisplayInfo.FieldType.Type.REMOTE_ADDRESS)
    public String broker = "tcp://mqtt.broker:1883";

    @DisplayInfo.Required
    @DisplayInfo(desc = "MQTT Topic ID")
    public String topicId;

    @DisplayInfo(desc = "Username")
    public String username;

    @DisplayInfo(desc = "Password")
    @DisplayInfo.FieldType(DisplayInfo.FieldType.Type.PASSWORD)
    public String password;

}
