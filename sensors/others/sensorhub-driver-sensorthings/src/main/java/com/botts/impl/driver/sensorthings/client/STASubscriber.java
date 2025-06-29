package com.botts.impl.driver.sensorthings.client;

import de.fraunhofer.iosb.ilt.sta.MqttException;
import de.fraunhofer.iosb.ilt.sta.ServiceFailureException;
import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.EntityType;
import de.fraunhofer.iosb.ilt.sta.model.Observation;
import de.fraunhofer.iosb.ilt.sta.service.MqttSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class STASubscriber {

    Logger logger = LoggerFactory.getLogger(STASubscriber.class);
    ScheduledExecutorService scheduler;
    ScheduledFuture<?> pollingTask;
    private volatile Object latestObsId = null;
    Datastream datastream;
    MqttSubscription mqttSubscription;
    long pollingRate;
    private boolean useMqtt;

    public STASubscriber(Datastream datastream, long pollingRate, boolean useMqtt) {
        this.datastream = datastream;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.pollingRate = pollingRate;
        this.useMqtt = useMqtt;
    }

    public void startStream(StreamListener streamListener) {
        if (useMqtt) {
            try {
                logger.info("Attempting to start MQTT subscription on Datastream {}...", datastream.getName());
                subscribeMQTT(streamListener);
            } catch (MqttException e) {
                logger.error("Unable to connect via MQTT", e);
            }
        } else {
            logger.info("Attempting to start polling service on Datastream {}...", datastream.getName());
            subscribePoller(streamListener);
        }
    }

    public void stopStream() throws MqttException {
        unsubscribeMQTT();
        unsubscribePoller();
    }

    private void subscribeMQTT(StreamListener streamListener) throws MqttException {
        unsubscribeMQTT();
        mqttSubscription = datastream.
                <Observation>subscribeRelative(observation ->
                streamListener.onDataReceived(STAUtils.createDataBlock(observation)),
                EntityType.OBSERVATIONS);
    }

    private void unsubscribeMQTT() throws MqttException {
        if (mqttSubscription != null)
            datastream.getService().unsubscribe(mqttSubscription);
    }

    private void subscribePoller(StreamListener streamListener) {
        // Schedule polling mechanism at configured polling rate
        pollingTask = scheduler.scheduleAtFixedRate(() -> {
            try {
                // Get latest observation based on phenomenonTime
                Observation obs = datastream.getService().observations().query().orderBy("phenomenonTime").first();
                if (obs != null) {
                    Object currentObsId = obs.getId().getValue();
                    // Trigger callback if it's the first observation recorded, or a new observation
                    if (latestObsId == null || !latestObsId.equals(currentObsId)) {
                        streamListener.onDataReceived(STAUtils.createDataBlock(obs));
                        latestObsId = currentObsId;
                    }
                }
            } catch (ServiceFailureException e) {
                logger.warn("Error retrieving latest Observation from Datastream {}", datastream.getName(), e);
            }
        }, 0, pollingRate, TimeUnit.MILLISECONDS);
    }

    private void unsubscribePoller() {
        // Cancel current task if needed
        if (pollingTask != null && !pollingTask.isCancelled())
            pollingTask.cancel(true);

        // Shutdown scheduler
        if (scheduler != null && !scheduler.isShutdown())
            scheduler.shutdownNow();
    }

}
