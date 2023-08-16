package org.sensorhub.impl.sensor.onvif;

import org.sensorhub.api.sensor.SensorException;
import org.sensorhub.impl.sensor.rtpcam.RTPVideoOutput;

import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

public class OnvifVideoH264 extends RTPVideoOutput<OnvifCameraDriver> {

    public double maxFrameDelay=1.0;
    volatile long lastFrameTime;
    Timer watchdog;
    public OnvifVideoH264(String name, OnvifCameraDriver driver) {
        super(name, driver);
    }
    public void start() throws SensorException{
        super.start(parent.onvifBasicVideoConfig, parent.onvifRTSPConfig, parent.timeout);

        // start watchdog thread to detect disconnections
        final long maxFramePeriod = (long)(maxFrameDelay * 1000);
        lastFrameTime = Long.MAX_VALUE;
        TimerTask checkFrameTask = new TimerTask()
        {
            @Override
            public void run()
            {
                if (lastFrameTime < System.currentTimeMillis() - maxFramePeriod)
                {
                    parentSensor.getLogger().warn("No frame received in more than {}ms. Reconnecting...", maxFramePeriod);
                    parentSensor.connection.reconnect();
                    cancel();
                }
            }
        };
        watchdog = new Timer();
        watchdog.scheduleAtFixedRate(checkFrameTask, 0L, 10000L);
    }
    @Override
    public void onFrame(long timeStamp, int seqNum, ByteBuffer frameBuf, boolean packetLost)
    {
        super.onFrame(timeStamp, seqNum, frameBuf, packetLost);
        lastFrameTime = System.currentTimeMillis();
    }
    @Override
    public void stop()
    {
        super.stop();
        watchdog.cancel();
    }
}
