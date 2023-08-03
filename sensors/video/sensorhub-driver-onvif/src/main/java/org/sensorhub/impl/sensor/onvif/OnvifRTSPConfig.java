package org.sensorhub.impl.sensor.onvif;

import org.sensorhub.impl.sensor.rtpcam.RTSPConfig;

public class OnvifRTSPConfig extends RTSPConfig {
    public OnvifRTSPConfig(String videopath, int localUDPPort,boolean onlyConnectRTSP,int remoteport, String User, String Password,boolean enableTls){
        this.videoPath=videopath;
        this.localUdpPort=localUDPPort;
        this.onlyConnectRtsp=onlyConnectRTSP;
        this.remotePort= remoteport;
        this.user=User;
        this.password=Password;
        this.enableTLS=enableTls;
    }

}
