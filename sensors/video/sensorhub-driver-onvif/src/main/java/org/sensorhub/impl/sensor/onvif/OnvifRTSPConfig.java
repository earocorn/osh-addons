package org.sensorhub.impl.sensor.onvif;

import org.sensorhub.impl.sensor.rtpcam.RTSPConfig;

import java.net.URI;

public class OnvifRTSPConfig extends RTSPConfig {
    public OnvifRTSPConfig(String user, String password, String remHost, int remPort, String vidPath, int localUDPPort){
        this.remotePort= remPort;
        this.videoPath= vidPath;
        this.user= user;
        this.password=password;
        this.remoteHost=remHost;
        this.localUdpPort=localUDPPort;
    }

}
