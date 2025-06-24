package com.botts.impl.driver.sensorthings;

import com.botts.impl.driver.sensorthings.client.STASubscriber;
import de.fraunhofer.iosb.ilt.sta.MqttException;
import net.opengis.swe.v20.DataBlock;
import net.opengis.swe.v20.DataComponent;
import net.opengis.swe.v20.DataEncoding;
import org.sensorhub.api.data.DataEvent;
import org.sensorhub.impl.sensor.VarRateSensorOutput;
import org.vast.util.Asserts;

public class STAVirtualSensorOutput extends VarRateSensorOutput<STAVirtualSensor> {

    DataComponent recordStructure;
    DataEncoding recordEncoding;
    STAVirtualSensorConfig config;
    STASubscriber subscriber;

    public STAVirtualSensorOutput(STAVirtualSensor sensor, DataComponent recordStructure, DataEncoding recordEncoding, STASubscriber subscriber) {
        super(recordStructure.getName(), sensor, sensor.getConfiguration().httpPollRate);
        this.recordStructure = Asserts.checkNotNull(recordStructure, DataComponent.class);
        this.recordEncoding = Asserts.checkNotNull(recordEncoding, DataEncoding.class);
        this.subscriber = subscriber;
        this.config = sensor.getConfiguration();
    }

    public void publishNewRecord(DataBlock dataBlock) {
        long now = System.currentTimeMillis();
        updateSamplingPeriod(now);

        latestRecord = dataBlock;
        latestRecordTime = now;
        eventHandler.publish(new DataEvent(latestRecordTime, this, dataBlock));
    }

    public void start() {
        subscriber.startStream(this::publishNewRecord);
    }

    public void stop() throws MqttException {
        subscriber.stopStream();
    }

    @Override
    public DataComponent getRecordDescription() { return recordStructure; }

    @Override
    public DataEncoding getRecommendedEncoding() { return recordEncoding; }

}
