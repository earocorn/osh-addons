package com.botts.impl.comm.kafka;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.sensorhub.api.comm.IMessageQueuePush;
import org.sensorhub.api.common.SensorHubException;
import org.sensorhub.impl.module.AbstractSubModule;
import org.vast.util.Asserts;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

public class KafkaMessageQueue extends AbstractSubModule<KafkaMessageQueueConfig> implements IMessageQueuePush<KafkaMessageQueueConfig>, Runnable {

    private final Set<MessageListener> listeners = new CopyOnWriteArraySet<>();
    private KafkaConsumer<byte[], byte[]> consumer;
    private KafkaProducer<byte[], byte[]> producer;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private Thread consumerThread;
    private static final String SECURITY_PROTOCOL_CONFIG = "security.protocol";
    private static final String SASL_SSL = "SASL_SSL";

    @Override
    public void init(KafkaMessageQueueConfig config) throws SensorHubException {
        super.init(config);

        Properties props = new Properties();
        props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, config.groupId);
        props.setProperty(ConsumerConfig.CLIENT_ID_CONFIG, config.clientId);

        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());

        if (config.enableTLS) {
            props.setProperty(SslConfigs.SSL_PROTOCOL_CONFIG, "TLSv1.2");
            props.setProperty(SECURITY_PROTOCOL_CONFIG, "SSL");
            props.setProperty(SslConfigs.SSL_ENABLED_PROTOCOLS_CONFIG, "TLSv1.2");
        }

        if (config.sslConfig != null) {
            props.setProperty(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, config.sslConfig.trustStorePath);
            props.setProperty(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, config.sslConfig.trustStorePassword);
            props.setProperty(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, config.sslConfig.trustStoreFormat.toString());
            props.setProperty(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, config.sslConfig.keyStorePath);
            props.setProperty(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, config.sslConfig.keyStorePassword);
            props.setProperty(SslConfigs.SSL_KEY_PASSWORD_CONFIG, config.sslConfig.keyStorePassword);
            props.setProperty(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, config.sslConfig.keyStoreFormat.toString());
        }

        switch (config.authType) {
            case SASL_SCRAM_SHA_256 -> configureSaslScramProps(props, true);
            case SASL_SCRAM_SHA_512 -> configureSaslScramProps(props, false);
            case SASL_KERBEROS -> configureKerberosProps(props);
            case SASL_PLAINTEXT -> configureSaslPlaintextProps(props);
        }

        props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        for (String prop : config.additionalProperties) {
            String[] split = prop.split("=");
            if (split.length == 2)
                props.setProperty(split[0].trim(), split[1].trim());
        }

        if (config.enableSubscribe) {
            consumer = new KafkaConsumer<>(props);
            consumerThread = new Thread(this);
        }

        if (config.enablePublish)
            producer = new KafkaProducer<>(props);
    }

    private void configureSaslScramProps(Properties props, boolean useSHA256) {
        String jaasConfigString = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\""
                + escape(config.username) + "\" password=\"" + escape(config.password) + "\";";
        props.setProperty(SECURITY_PROTOCOL_CONFIG, SASL_SSL);
        props.setProperty(SaslConfigs.SASL_JAAS_CONFIG, jaasConfigString);
        if (useSHA256)
            props.setProperty(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-256");
        else
            props.setProperty(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-512");
    }

    private void configureKerberosProps(Properties props) {
        if (config.kerberosConfig != null) {
            props.setProperty(SECURITY_PROTOCOL_CONFIG, SASL_SSL);
            props.setProperty(SaslConfigs.SASL_KERBEROS_SERVICE_NAME, config.kerberosConfig.serviceName);
            props.setProperty(SaslConfigs.SASL_MECHANISM, "GSSAPI");
            String escapedConfig = escape(config.kerberosConfig.keyTabLocation);
            String escapedPrincipal = escape(config.kerberosConfig.principal);
            String jaasConfig = "com.sun.security.auth.module.Krb5LoginModule required\n useKeyTab="
                    + config.kerberosConfig.useKeyTab + "\n storeKey=" + config.kerberosConfig.storeKey + "\n keyTab=\""
                    + escapedConfig + "\"\n principal=\"" + escapedPrincipal + "\";";
            props.setProperty(SaslConfigs.SASL_JAAS_CONFIG, jaasConfig);
        }
    }

    private void configureSaslPlaintextProps(Properties props) {
        String jaasConfig = "org.apache.kafka.common.security.plain.PlainLoginModule required username=\""
                + escape(config.username) + "\" password=\"" + escape(config.password) + "\";";
        props.setProperty(SaslConfigs.SASL_MECHANISM, "PLAIN");
        props.setProperty(SECURITY_PROTOCOL_CONFIG, SASL_SSL);
        props.setProperty(SaslConfigs.SASL_JAAS_CONFIG, jaasConfig);
    }

    private String escape(final String s) {
        if (s == null) return null;
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("'", "\\'")
                .replace("\t", "\\t")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }

    @Override
    public void start() {
        Asserts.checkNotNull(consumer, "KafkaConsumer");

        if (config.enableSubscribe) {
            consumer.subscribe(Collections.singletonList(config.topicName));
            consumerThread.start();
        }
        isRunning.set(true);
    }

    @Override
    public void run() {
        try {
            while (isRunning.get()) {
                ConsumerRecords<byte[], byte[]> records = consumer.poll(Duration.ofMillis(config.pollTimeout));
                for (ConsumerRecord<byte[], byte[]> consumerRecord : records)
                    for (MessageListener listener : listeners) {
                        HashMap<String, String> attributes = new HashMap<>();
                        attributes.put("key", Arrays.toString(consumerRecord.key()));
                        consumerRecord.headers().forEach(header -> attributes.put(header.key(), Arrays.toString(header.value())));
                        listener.receive(attributes, consumerRecord.value());
                    }
            }
        } catch (Exception e) {
            if (isRunning.get())
                getLogger().error("Error occurred while consuming data", e);
        }
    }

    @Override
    public void stop() {
        isRunning.set(false);
        if (consumer != null) {
            consumer.wakeup();
            consumer.close();
        }
        if (producer != null)
            producer.close();
    }

    @Override
    public void publish(byte[] payload) {
        publish(null, payload);
    }

    @Override
    public void publish(Map<String, String> attrs, byte[] payload) {
        if (!config.enablePublish)
            return;

        final ProducerRecord<byte[], byte[]> producerRecord = attrs.get("key") == null ?
                        new ProducerRecord<>(config.topicName, payload) :
                        new ProducerRecord<>(config.topicName, attrs.get("key").getBytes(), payload);
        producer.send(producerRecord, (metadata, exception) -> {
            if (exception != null)
                getLogger().error("Error occurred while sending producerRecord", exception);
        });
    }

    @Override
    public void registerListener(MessageListener listener) {
        listeners.add(listener);
    }

    @Override
    public void unregisterListener(MessageListener listener) {
        listeners.remove(listener);
    }

}
