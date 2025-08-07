package com.botts.impl.comm.mqtt;

import org.eclipse.paho.client.mqttv3.*;
import org.sensorhub.api.client.ClientException;
import org.sensorhub.api.comm.ICommProvider;
import org.sensorhub.api.common.SensorHubException;
import org.sensorhub.impl.comm.LocalMessageQueue;
import org.sensorhub.impl.module.AbstractModule;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MqttCommProvider extends AbstractModule<MqttCommProviderConfig> implements ICommProvider<MqttCommProviderConfig> {

    MqttClient mqttClient;
    InputStream inputStream;
    OutputStream outputStream;
    private final BlockingQueue<byte[]> messageQueue = new LinkedBlockingQueue<>();

    @Override
    protected void doStart() throws SensorHubException {
        super.doStart();

        String protocol = config.protocol.protocol.getName();
        String host = config.protocol.remoteHost;
        int port = config.protocol.remotePort;
        String clientId = config.protocol.clientId;
        String pubTopic = config.protocol.publishTopicId;
        String subTopic = config.protocol.subscribeTopicId;
        int qos = config.protocol.qos;
        boolean retain = config.protocol.retain;

        try {
            mqttClient = new MqttClient(protocol + "://" + host + ":" + port, clientId);
            mqttClient.connect();
            logger.info("Connected to MQTT broker");
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }

        mqttClient.setCallback(new MqttCallback() {

            @Override
            public void connectionLost(Throwable throwable) {
                reportError("Connection to broker lost", throwable);
            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                var accepted = messageQueue.offer(mqttMessage.getPayload());
                if (accepted) {
                    logger.info("Message arrived");
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });

        if (subTopic != null && !subTopic.isBlank()) {
            try {
                mqttClient.subscribe(subTopic);
            } catch (MqttException e) {
                throw new RuntimeException(e);
            }

            inputStream = new InputStream() {
                private byte[] buffer = null;
                private int position = 0;

                @Override
                public int read() throws IOException {
                    if (buffer == null || position >= buffer.length) {
                        try {
                            buffer = messageQueue.take();
                            position = 0;
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    return buffer[position++] & 0xFF;
                }
            };
        }

        if (pubTopic != null && !pubTopic.isBlank()) {
            outputStream = new OutputStream() {
                private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                @Override
                public void write(int b) throws IOException {
                    buffer.write(b);
                }

                @Override
                public void flush() throws IOException {
                    try {
                        mqttClient.publish(pubTopic, buffer.toByteArray(), qos, retain);
                        buffer.reset();
                    } catch (MqttException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void close() throws IOException {
                    flush();
                }
            };
        }
        if (inputStream == null && outputStream == null)
            throw new ClientException("No input stream or output stream created for MQTT client");
    }

    @Override
    protected void doStop() throws SensorHubException {
        super.doStop();

        if (mqttClient == null)
            return;

        try {
            mqttClient.disconnect();
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return inputStream;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return outputStream;
    }
}
