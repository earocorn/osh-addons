package org.sensorhub.impl.sensor.onvif;

import org.sensorhub.api.sensor.SensorException;
import org.sensorhub.impl.sensor.rtpcam.*;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Executors;


public class OnvifVideoOutputH264 extends RTPVideoOutput<OnvifCameraDriver> {

    public OnvifVideoOutputH264(String name, OnvifCameraDriver driver) {
        super(name, driver);
    }

    public void start(URI uri, int timeout) throws SensorException
    {
        // start payload process executor
        executor = Executors.newSingleThreadExecutor();
        firstFrameReceived = false;

        String username;
        String password;
        String remoteHost;
        int remotePort;
        String videoPath;
        int localUdpPort= 20000;

        //populate the parsing of the uri
        remoteHost=parentSensor.getHostUrl();
        remotePort=uri.getPort();
        videoPath= uri.getPath()+uri.getQuery();
        username= parentSensor.getUser();
        password= parentSensor.getPassword();

        try
        {
            // setup stream with RTSP server
            rtspClient = new RTSPClient(
                    remoteHost,
                    remotePort,
                    videoPath,
                    username,
                    password,
                    localUdpPort,
                    timeout);

            // some cameras don't have a real RTSP server (i.e. 3DR Solo UAV)
            // in this case we just need to maintain a TCP connection so keep the RTSP client alive

            //if (){
            rtspClient.sendOptions();
            rtspClient.sendDescribe();
            rtspClient.sendSetup();
            log.info("Connected to RTSP server");
          //  }



            // start RTP/H264 receiving thread
            rtpThread = new RTPH264Receiver(remoteHost, rtspClient.getRemoteRtpPort(), localUdpPort, this);
            RTSPClient.StreamInfo h264Stream = null;
            int streamIndex = 0;
            int i = 0;
            if (rtspClient.isConnected())
            {
                // look for H264 stream
                for (RTSPClient.StreamInfo stream: rtspClient.getMediaStreams())
                {
                    if (stream.codecString != null && stream.codecString.contains("H264"))
                    {
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
            if (rtspClient.isConnected())
            {
                // send PLAY request
                rtspClient.sendPlay(streamIndex);

                // start RTCP sending thread
                // some cameras need that to maintain the stream
                rtcpThread = new RTCPSender(remoteHost, localUdpPort+1, rtspClient.getRemoteRtcpPort(), 1000, rtspClient);
                rtcpThread.start();
            }
        }
        catch (IOException e)
        {
            throw new SensorException("Cannot connect to RTP stream", e);
        }
    }


}


