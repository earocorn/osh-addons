package org.sensorhub.impl.sensor.onvif;

import org.onvif.ver10.schema.VideoResolution;
import org.sensorhub.api.sensor.SensorException;
import org.sensorhub.impl.sensor.rtpcam.RTPVideoOutput;

//package org.sensorhub.impl.sensor.onvif;
//
//import de.onvif.soap.devices.MediaDevice;
//import de.onvif.soap.exception.SOAPFaultException;
//import net.opengis.swe.v20.DataBlock;
//import net.opengis.swe.v20.DataComponent;
//import net.opengis.swe.v20.DataEncoding;
//import org.onvif.ver10.schema.*;
//import org.sensorhub.api.sensor.SensorException;
//import org.sensorhub.impl.sensor.rtpcam.RTPVideoOutput;
//
//import javax.media.Buffer;
//import javax.xml.soap.SOAPException;
//import java.io.BufferedInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.ConnectException;
//import java.net.URL;
//import java.util.List;
//
public class OnvifVideoOutputH264 extends RTPVideoOutput<OnvifCameraDriver> {

    OnvifBasicVideoConfig onvifBasicVideoConfig;
    OnvifRTSPConfig onvifRtspConfig;

    public OnvifVideoOutputH264(String name, OnvifCameraDriver driver) {
        super(name, driver);
    }


    //init will have the resolutions available and have options for the h264 stream based on camera specs
    void init(){
        //implement the videoresolution call from th eonvif profiles
        VideoResolution videoResolution;
    }

    public void start() throws SensorException{

      OnvifCameraConfig cameraConfig = parentSensor.getConfiguration();
      super.start(onvifBasicVideoConfig, onvifRtspConfig, cameraConfig.timeout);


      //start disconnections

    }
    public void stop(){
      super.stop();
    }


}


