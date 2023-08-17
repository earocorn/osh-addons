package org.sensorhub.impl.sensor.onvif;

import org.sensorhub.api.sensor.SensorException;
import org.sensorhub.impl.sensor.rtpcam.RTCPSender;
import org.sensorhub.impl.sensor.rtpcam.RTPH264Receiver;
import org.sensorhub.impl.sensor.rtpcam.RTPVideoOutput;
import org.sensorhub.impl.sensor.rtpcam.RTSPClient;

import java.io.IOException;
import java.util.concurrent.Executors;

public class outputH264  extends RTPVideoOutput<OnvifCameraDriver> {


    public outputH264(String name, OnvifCameraDriver driver) {
        super(name, driver);
    }

    public void start(OnvifBasicVideoConfig onvifBasicVideoConfig, OnvifRTSPConfig onvifRTSPConfig, int timeout) throws SensorException {

//        this.videoConfig=onvifBasicVideoConfig;
//        this.rtspConfig=onvifRTSPConfig;

        // start payload process executor
        executor = Executors.newSingleThreadExecutor();
        firstFrameReceived = false;

        try {
            // setup stream with RTSP server
            rtspClient = new RTSPClient(
                    onvifRTSPConfig.remoteHost,
                    onvifRTSPConfig.remotePort,
                    onvifRTSPConfig.videoPath,
                    onvifRTSPConfig.user,
                    onvifRTSPConfig.password,
                    onvifRTSPConfig.localUdpPort,
                    timeout);

            // some cameras don't have a real RTSP server (i.e. 3DR Solo UAV)
            // in this case we just need to maintain a TCP connection so keep the RTSP client alive
            if (!onvifRTSPConfig.onlyConnectRtsp) {
                rtspClient.sendOptions();
                rtspClient.sendDescribe();
                rtspClient.sendSetup();
                log.info("Connected to RTSP server");
            }

            // start RTP/H264 receiving thread
            rtpThread = new RTPH264Receiver(parentSensor.getHostUrl(),rtspClient.getRemoteRtpPort(), onvifRTSPConfig.localUdpPort, this);
            RTSPClient.StreamInfo h264Stream = null;
            int streamIndex = 0;
            int i = 0;
            if (rtspClient.isConnected()) {
                // look for H264 stream
                for (RTSPClient.StreamInfo stream : rtspClient.getMediaStreams()) {
                    if (stream.codecString != null && stream.codecString.contains("H264")) {
                        h264Stream = stream;
                        streamIndex = i;
                    }

                    i++;
                }

                if (h264Stream == null)
                    throw new IOException("No stream with H264 codec found");

                // set initial parameter sets if we received them via RTSP
                if (h264Stream.paramSets != null)
                    rtpThread.setParameterSets(h264Stream.paramSets);
            }
            rtpThread.start();

            // play stream with RTSP if server responded to SET UP
            if (rtspClient.isConnected()) {
                // send PLAY request
                rtspClient.sendPlay(streamIndex);

                // start RTCP sending thread
                // some cameras need that to maintain the stream
                rtcpThread = new RTCPSender(onvifRTSPConfig.remoteHost, onvifRTSPConfig.localUdpPort + 1, rtspClient.getRemoteRtcpPort(), 1000, rtspClient);
                rtcpThread.start();
            }
        } catch (IOException e) {
            throw new SensorException("Cannot connect to RTP stream", e);
        }
    }
}
