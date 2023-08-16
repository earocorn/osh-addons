package org.sensorhub.impl.sensor.onvif;

import org.sensorhub.impl.sensor.rtpcam.RTSPConfig;

import java.net.URI;

public class OnvifRTSPConfig extends RTSPConfig {

//    public OnvifRTSPConfig(URI uri){

//        String username;
//        String password;
//        String remoteHost;
//        int remotePort;
//        String videoPath;
//        int localUdpPort= 20000;

        //populate the parsing of the uri
        //remoteHost=parentSensor.getHostUrl();
//        this.localUdpPort=20000;
//        this.remotePort=uri.getPort();
//        this.videoPath= uri.getPath()+uri.getQuery();
       // username= parentSensor.getUser();
       // password= parentSensor.getPassword();

//    }


//  public OnvifRTSPConfig(String vidPath, String remHost, int localUDPport, int remPort, String User, String PW){
//        this.user= User;
//        this.password=PW;
//        this.videoPath=vidPath;
//        this.remotePort=remPort;
//        this.localUdpPort=localUDPport;
//        this.remoteHost=remHost;
//    }


    public OnvifRTSPConfig(URI uri, String user, String password, String remoteHost){
        this.remotePort=uri.getPort();
        this.videoPath= uri.getPath()+uri.getQuery();
        this.user= user;
        this.password=password;
        this.remoteHost=remoteHost;
        this.localUdpPort=20000;
    }

//    public OnvifRTSPConfig(String vidPath, int remPort){
//        this.videoPath=vidPath;
//        this.remotePort=remPort;
//    }


//    onvifRTSPConfig.user= user;
//    onvifRTSPConfig.password=password;
//    onvifRTSPConfig.remoteHost= hostIp;
//    onvifRTSPConfig.localUdpPort=20000;
//    onvifRTSPConfig.videoPath= streamUri.getPath()+streamUri.getQuery();
//    onvifRTSPConfig.remotePort= streamUri.getPort();
}
