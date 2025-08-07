package com.botts.impl.comm.kafka;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.config.Config;
import org.apache.kafka.common.config.SecurityConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.sensorhub.api.comm.IMessageQueuePush;
import org.sensorhub.api.comm.MessageQueueConfig;
import org.sensorhub.api.common.SensorHubException;
import org.sensorhub.impl.module.AbstractSubModule;
import org.vast.util.Asserts;

import java.io.*;
import java.nio.charset.StandardCharsets;
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
            props.setProperty("security.protocol", "SSL");
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

        props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        for (String prop : config.additionalProperties) {
            String[] split = prop.split("=");
            if (split.length == 2)
                props.put(split[0].trim(), split[1].trim());
        }

        if (config.enableSubscribe) {
            consumer = new KafkaConsumer<>(props);
            consumerThread = new Thread(this);
        }

        if (config.enablePublish)
            producer = new KafkaProducer<>(props);
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
                for (ConsumerRecord<byte[], byte[]> record : records)
                    for (MessageListener listener : listeners) {
                        HashMap<String, String> attributes = new HashMap<>();
                        attributes.put("key", Arrays.toString(record.key()));
                        record.headers().forEach(header -> {
                            attributes.put(header.key(), Arrays.toString(header.value()));
                        });
                        listener.receive(attributes, record.value());
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

        final ProducerRecord<byte[], byte[]> record = attrs.get("key") == null ?
                        new ProducerRecord<>(config.topicName, payload) :
                        new ProducerRecord<>(config.topicName, attrs.get("key").getBytes(), payload);
        producer.send(record, (metadata, exception) -> {
            if (exception != null)
                getLogger().error("Error occurred while sending record", exception);
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
