package org.sensorhub.impl.sensor.onvif;

import org.sensorhub.impl.sensor.videocam.BasicVideoConfig;
import org.sensorhub.impl.sensor.videocam.VideoResolution;


public class OnvifBasicVideoConfig extends BasicVideoConfig {
    public OnvifBasicVideoConfig(int fr, boolean grayScale, String backUpFile) {
        this.frameRate=fr;
        this.grayscale=grayScale;
        this.backupFile=backUpFile;
    }

    @Override
    public VideoResolution getResolution() {
        return null;
    }
}

