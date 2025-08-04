package com.botts.impl.comm.kafka;

import org.apache.kafka.clients.admin.ConfigEntry;
import org.sensorhub.api.config.DisplayInfo;
import org.sensorhub.impl.comm.IPConfig;

import java.util.ArrayList;
import java.util.List;

public class KafkaClientConfig extends IPConfig {

    @DisplayInfo(desc="Port number to connect to on remote host")
    @DisplayInfo.ValueRange(min=0, max=65535)
    @DisplayInfo.Required
    public int remotePort = 9092;

    @DisplayInfo(label = "Group ID", desc = "Kafka consumer group ID")
    public String groupId = "osh-client";

    @DisplayInfo(label = "Topic", desc = "Kafka topic to subscribe or publish to")
    @DisplayInfo.Required
    public String topic;

    @DisplayInfo(label = "Poll Timeout (ms)", desc = "Consumer poll timeout")
    public int pollTimeout = 100;

    @DisplayInfo(label = "Use SSL", desc = "Enable SSL/TLS")
    public boolean useSSL = false;

    @DisplayInfo(label = "Additional Properties", desc = "Properties specified by 'key=value'")
    public List<String> additionalProperties = new ArrayList<>();

    public String getBootstrapServers() {
        return remoteHost + ":" + remotePort;
    }

}
