package org.sensorhub.impl.sensor.onvif;

import org.sensorhub.impl.sensor.rtpcam.RTPVideoOutput;

public class OnvifVideoOutputH264 extends RTPVideoOutput<OnvifCameraDriver> {

    OnvifBasicVideoConfig onvifBasicVideoConfig;
    OnvifRTSPConfig onvifRTSPConfig;

    public OnvifVideoOutputH264(String name, OnvifCameraDriver driver) {
        super(name, driver);
    }



    public void stop(){
      super.stop();
    }


}


