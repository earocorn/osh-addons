package org.sensorhub.impl.sensor.onvif;

import org.sensorhub.impl.sensor.rtpcam.RTSPConfig;

public class OnvifRTSPConfig extends RTSPConfig {
    public OnvifRTSPConfig(String videopath, int localUDPPort,boolean onlyConnectRTSP,int remoteport, String User, String Password,boolean enableTls){
        //string after port
        this.videoPath=videopath;
        // empty string
        this.localUdpPort=localUDPPort;
        //false
        this.onlyConnectRtsp=onlyConnectRTSP;
        this.remotePort= remoteport;
        //need this from config
        this.user=User;
        //need this from config
        this.password=Password;
        // false
        this.enableTLS=enableTls;
    }

}
