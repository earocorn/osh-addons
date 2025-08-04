package com.botts.impl.comm.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.sensorhub.api.comm.ICommProvider;
import org.sensorhub.api.common.SensorHubException;
import org.sensorhub.impl.module.AbstractModule;

import java.io.*;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public class KafkaCommProvider extends AbstractModule<KafkaCommProviderConfig> implements ICommProvider<KafkaCommProviderConfig>, Runnable {

    private KafkaConsumer<String, String> consumer;
    private PipedInputStream inputStream;
    private PipedOutputStream connector;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private Thread workerThread;

    @Override
    protected void doStart() throws SensorHubException {
        super.doStart();

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.protocol.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, config.protocol.groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        for (String prop : config.protocol.additionalProperties) {
            String[] split = prop.split("=");
            if (split.length == 2)
                props.put(split[0].trim(), split[1].trim());
        }

        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(config.protocol.topic));

        connector = new PipedOutputStream();
        try {
            inputStream = new PipedInputStream(connector, 64*1024);
        } catch (IOException e) {
            throw new SensorHubException("Failed to create input stream", e);
        }

        isRunning.set(true);

        workerThread = new Thread(this, "Kafka-Consumer-Thread");
        workerThread.start();
    }

    @Override
    public void run() {
        try {
            while (isRunning.get()) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(config.protocol.pollTimeout));
                for (ConsumerRecord<String, String> record : records) {
                    byte[] data = (record.value() + "\n").getBytes();
                    connector.write(data);
                    connector.flush();
                }
            }
        } catch (Exception e) {
            if (isRunning.get())
                getLogger().error("Error occurred while consuming data", e);
        }
    }


    @Override
    protected void doStop() throws SensorHubException {
        try {
            isRunning.set(false);
            if (consumer != null) {
                consumer.wakeup();
                consumer.close();
            }
            if (workerThread != null)
                workerThread.join();
            if (connector != null)
                connector.close();
            if (inputStream != null)
                inputStream.close();
        } catch (InterruptedException | IOException e) {
            throw new SensorHubException("Failed to stop Kafka comm provider", e);
        } finally {
            super.doStop();
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return inputStream;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return null;
    }

}
