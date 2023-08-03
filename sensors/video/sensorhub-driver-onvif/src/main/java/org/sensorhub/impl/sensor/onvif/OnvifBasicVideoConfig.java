package org.sensorhub.impl.sensor.onvif;

import org.sensorhub.impl.sensor.videocam.BasicVideoConfig;


public abstract class OnvifBasicVideoConfig extends BasicVideoConfig {
    public OnvifBasicVideoConfig(int frameRate, boolean grayScale, String backUpFile) {
        this.frameRate=frameRate;
        this.grayscale=grayScale;
        this.backupFile=backUpFile;
    }
//TODO: implement resolutions

}

