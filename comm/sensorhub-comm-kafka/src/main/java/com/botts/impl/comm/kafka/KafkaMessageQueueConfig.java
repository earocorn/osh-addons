package com.botts.impl.comm.kafka;

import org.sensorhub.api.comm.MessageQueueConfig;
import org.sensorhub.api.config.DisplayInfo;
import org.sensorhub.impl.comm.IPConfig;

import java.util.ArrayList;
import java.util.List;

public class KafkaMessageQueueConfig extends MessageQueueConfig {

    @DisplayInfo(desc = "Bootstrap server host")
    public String remoteHost = "localhost";

    @DisplayInfo(desc="Port number to connect to on remote host")
    @DisplayInfo.ValueRange(min=0, max=65535)
    public int remotePort = 9092;

    @DisplayInfo(desc = "Kafka consumer group ID")
    public String groupId = "osh-client-group";

    @DisplayInfo(desc = "Kafka consumer client ID")
    public String clientId = "osh-client";

    @DisplayInfo(label = "Poll Timeout (ms)", desc = "Consumer poll timeout")
    public int pollTimeout = 100;

    @DisplayInfo(desc = "Additional configuration for connecting with SSL")
    public SSLConfig sslConfig;

    public boolean enableTLS = false;

    @DisplayInfo(desc = "Type of authentication to use to connect to Kafka broker")
    public AuthType authType = AuthType.NONE;

    public String username;

    public String password;

    public KerberosConfig kerberosConfig;

    @DisplayInfo(label = "Additional Properties", desc = "Properties specified by 'key=value'")
    public List<String> additionalProperties = new ArrayList<>();

    public String getBootstrapServers() {
        return remoteHost + ":" + remotePort;
    }

    public enum AuthType {
        NONE,
        SASL_PLAINTEXT,
        SASL_KERBEROS,
        SASL_SCRAM_SHA_256,
        SASL_SCRAM_SHA_512,
    }

}
