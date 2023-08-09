package org.sensorhub.impl.sensor.onvif;

import org.sensorhub.api.sensor.SensorException;
import org.sensorhub.impl.sensor.rtpcam.RTPVideoOutput;

public class OnvifVideoOutputH264 extends RTPVideoOutput<OnvifCameraDriver> {

    OnvifBasicVideoConfig onvifBasicVideoConfig;
    OnvifRTSPConfig onvifRTSPConfig;

    public OnvifVideoOutputH264(String name, OnvifCameraDriver driver) {
        super(name, driver);
    }


    //init will have the resolutions available and have options for the h264 stream based on camera specs
    public void init(int imageWidth, int imageHeight) {
        //implement the videoResolution call from the onvif profiles


    }

    public void start(OnvifBasicVideoConfig onvifBasicVideoConfig, OnvifRTSPConfig onvifRTSPConfig, int timeout) throws SensorException{
        this.onvifBasicVideoConfig= onvifBasicVideoConfig;
        this.onvifRTSPConfig= onvifRTSPConfig;


        //OnvifCameraConfig cameraConfig = parentSensor.getConfiguration();
      //super.start(onvifBasicVideoConfig, onvifRtspConfig, cameraConfig.timeout);


    }
    public void stop(){
      super.stop();
    }


}


