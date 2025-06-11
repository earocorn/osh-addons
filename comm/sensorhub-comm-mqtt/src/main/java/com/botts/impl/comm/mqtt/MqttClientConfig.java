package com.botts.impl.comm.mqtt;

import org.sensorhub.api.config.DisplayInfo;
import org.sensorhub.impl.comm.IPConfig;

public class MqttClientConfig extends IPConfig {

    @DisplayInfo(desc="Port number to connect to on remote host")
    @DisplayInfo.ValueRange(min=0, max=65535)
    @DisplayInfo.Required
    public int remotePort = 1883;

    public String clientId = "osh";

    public Protocol protocol = Protocol.TCP;

    public String subscribeTopicId;
    public String publishTopicId;
    public int qos = 1;
    public boolean retain = true;

    public enum Protocol {
        WS("ws"),
        WSS("wss"),
        TCP("tcp"),
        MQTT("mqtt");
        final String protocol;
        Protocol(String protocol) { this.protocol = protocol; }
        public String getName() { return protocol; }
    }

}
