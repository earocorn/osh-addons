/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
The Initial Developer is Bott's Innovative Research Inc. Portions created by the Initial
Developer are Copyright (C) 2014 the Initial Developer. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.sensor.onvif;

import de.onvif.soap.OnvifDevice;
import de.onvif.soap.devices.InitialDevice;
//import de.onvif.soap.devices.MediaDevice;
import de.onvif.soap.exception.SOAPFaultException;
import net.opengis.sensorml.v20.IdentifierList;
import org.onvif.ver10.device.wsdl.GetDeviceInformationResponse;
import org.onvif.ver10.schema.*;
import org.sensorhub.api.common.SensorHubException;
import org.sensorhub.api.sensor.SensorException;
import org.sensorhub.impl.module.RobustConnection;
import org.sensorhub.impl.sensor.AbstractSensorModule;
import org.vast.sensorML.SMLFactory;

import javax.xml.soap.SOAPException;
import java.net.ConnectException;
import java.net.URI;
//import java.net.URL;
import java.util.List;
import java.util.Optional;
/**
 * <p>
 * Implementation of sensor interface for generic Cameras using IP
 * protocol
 * </p>
 * 
 * @author Joshua Wolfe <developer.wolfe@gmail.com>
 * @since June 13, 2017
 */

public class OnvifCameraDriver extends AbstractSensorModule <OnvifCameraConfig>
{
    //OnvifVideoOutputH264 h264VideoOutput;
    RobustConnection connection;
    //outputH264 h264VideoOutput;

    OnvifVideoH264 h264VideoOutput;
    OnvifVideoOutput mpeg4VideoOutput;
    OnvifVideoOutput mjpegVideoOutput;

//    OnvifVideoControl videoControlInterface;
    OnvifPtzOutput ptzPosOutput;
    OnvifPtzControl ptzControlInterface;
    String hostIp;
    Integer port;
    Integer udpPort;
    String user;
    String password;
    String path;
    Integer timeout;
    OnvifDevice camera;
    Profile profile;
    Profile mpeg4Profile;
    Profile h264Profile;
    String serialNumber;
    String modelNumber;
    String shortName;
    String longName;
    URI streamUri;
    Profile mjpegProfile;
    OnvifBasicVideoConfig onvifBasicVideoConfig;
    OnvifRTSPConfig onvifRTSPConfig;

    public OnvifCameraDriver() {
    }
    @Override
    public void setConfiguration(final OnvifCameraConfig config) {
        super.setConfiguration(config);
        hostIp = config.hostIp;
        port = config.port;
        user = config.user;
        password = config.password;
        path = config.path;
        timeout = config.timeout;
        udpPort=config.udpPort;
    }

    @Override
    protected void doInit() throws SensorHubException {
        // reset internal state in case init() was already called
        super.doInit();
        mpeg4VideoOutput = null;
        h264VideoOutput=null;
        mjpegVideoOutput =null;
        ptzPosOutput = null;
        ptzControlInterface = null;


        if (hostIp == null) {
            throw new SensorHubException("No host IP address provided in config");
        }
        try {
            if (user == null || password == null) {
                logger.info("User PW Null");
                camera = new OnvifDevice(hostIp);
                logger.info("created new onvif device");
            } else {
                logger.info("User PW entered");
                camera = new OnvifDevice(hostIp, user, password);
                logger.info("created new onvif device");
            }
            logger.info("check if connected by running getName");
            InitialDevice devices = camera.getDevices();
            logger.info("devices: " + devices);
            logger.info("Devices Found: "+ devices.getHostname());
            logger.info(camera.getDevices().getDeviceInformation().toString());
            logger.info("running getSoap");
            camera.getSoap().setLogging(false);  //disables ptz messages written to files when set to false
        } catch (ConnectException e) {
            throw new SensorHubException("Exception occurred when connecting to camera");
        } catch (SOAPException e) {
            throw new SensorHubException("Exception occurred when calling XML Web service over SOAP");
        } catch (Exception e) {
            throw new SensorHubException(e.toString());
        } catch (SOAPFaultException e) {
            throw new RuntimeException(e);
        }
        logger.info ("Connected to camera.");

        //get device information
        Optional<GetDeviceInformationResponse> deviceInformation = camera.getDevices().getDeviceInformation();
        deviceInformation.ifPresent(deviceInformationResponse -> {
            serialNumber = deviceInformationResponse.getSerialNumber().trim();
            modelNumber = deviceInformationResponse.getModel().trim();
            shortName = deviceInformationResponse.getManufacturer().trim();
            longName = shortName + "_" + modelNumber + "_" + serialNumber;
        });
        //check device information was received
        if (serialNumber == null || serialNumber.isEmpty() || modelNumber == null || modelNumber.isEmpty() || shortName == null || shortName.isEmpty()) {
            throw new SensorHubException("Failed to retrieve device information");
        }

        // generate identifiers
        generateUniqueID("urn:onvif:cam:", serialNumber);
        generateXmlID("ONVIF_CAM_", serialNumber);

        //list any profiles the camera may have
        List<Profile> profiles = camera.getDevices().getProfiles();
        logger.info("Number of profiles: " + profiles.size());

        if (profiles.isEmpty()) {
            throw new SensorHubException("Camera does not have any profiles to use");
        }

        //Media profile for video stream
        for (Profile mediaProfile :profiles) {
            VideoEncoderConfiguration videoEncoderConfiguration = mediaProfile.getVideoEncoderConfiguration();
            VideoEncoding videoEncoding = videoEncoderConfiguration.getEncoding();

            //h264 video encoder
            if (videoEncoding == VideoEncoding.H_264) {
                h264Profile = mediaProfile;
            }

            //MPEG4 video Encoder
            if (videoEncoding == VideoEncoding.MPEG_4) {
                mpeg4Profile = mediaProfile;
            }

            //Mjpeg video Encoder
            if (videoEncoding == VideoEncoding.JPEG) {
                mjpegProfile = mediaProfile;
            }
        }
            //ptz profiles
            for (Profile p : profiles) {
                String token = p.getToken();
                if (camera.getPtz().isAbsoluteMoveSupported(token) &&
                        camera.getPtz().isRelativeMoveSupported(token) &&
                        camera.getPtz().isPtzOperationsSupported(token)) {
                    profile = p;
                    break;
                }
            }
            if (profile == null) {
                throw new SensorHubException("Camera does not have any profiles capable of PTZ for "+ hostIp);
            }

        //Profile Configurations
        if(h264Profile!=null) {
            H264Configuration h264Config = h264Profile.getVideoEncoderConfiguration().getH264();
            if (config.enableH264 && h264Config == null) {
                throw new SensorException("Cannot connect to H264 stream - H264 not supported");
            }

        }
        if(mpeg4Profile!=null) {
            Mpeg4Configuration mpeg4Config = mpeg4Profile.getVideoEncoderConfiguration().getMPEG4();
            if (config.enableMPEG4 && mpeg4Config == null) {
                throw new SensorException("Cannot connect to MPEG4 stream - MPEG4 not supported");
            }
        }
        //TODO: mjpegProfile null


        // create I/O objects
        String videoOutName = "video";
        int videoOutNum = 1;

        //h264 configuration enabled
        if (config.enableH264) {
            String outputName = videoOutName + videoOutNum; //output name
            //h264VideoOutput = new OnvifVideoOutputH264(outputName, this);
            //h264VideoOutput= new outputH264(outputName,this);
            h264VideoOutput= new OnvifVideoH264(outputName, this);
            addOutput(h264VideoOutput, false);

            //call to init function to get resolution of video
            h264VideoOutput.init(h264Profile.getVideoEncoderConfiguration().getResolution().getWidth(), h264Profile.getVideoEncoderConfiguration().getResolution().getHeight());
            try{
                try {
                    streamUri = URI.create(camera.getMedia().getRTSPStreamUri(h264Profile.getToken()));
                } catch (SOAPException e) {
                    throw new RuntimeException();
                } catch (SOAPFaultException e) {
                    throw new RuntimeException(e);
                }
                logger.info("RTSP: " + streamUri);
            } catch (Exception e) {
                logger.info("h264 stream cannot connect to rtsp", e);
            }
            int frameRate;
            frameRate= h264Profile.getVideoEncoderConfiguration().getRateControl().getFrameRateLimit();

            onvifRTSPConfig = new OnvifRTSPConfig(user, password, hostIp, port, path, udpPort);
            onvifBasicVideoConfig= new OnvifBasicVideoConfig(frameRate, false, null);

        }

        //add mjpeg video output
         if (config.enableMJPEG) {
             String outputName = videoOutName + videoOutNum;
             mjpegVideoOutput = new OnvifVideoOutput(this, outputName);
             addOutput(mjpegVideoOutput, false);
             mjpegVideoOutput.init();

             try {
                 try {
                     streamUri = URI.create(camera.getMedia().getHTTPStreamUri(mjpegProfile.getToken()));
                     
                 } catch (SOAPException e) {
                     throw new RuntimeException();
                 } catch (SOAPFaultException e) {
                     throw new RuntimeException(e);
                 }
                 logger.info("HTTP: " + streamUri);
             } catch (Exception e) {
                 logger.info("mjpeg stream cannot connect to http", e);
             }
             try{
                 try {
                     streamUri = URI.create(camera.getMedia().getHTTPStreamUri(mjpegProfile.getToken()));
                 } catch (SOAPException e) {
                     throw new RuntimeException();
                 } catch (SOAPFaultException e) {
                     throw new RuntimeException(e);
                 }
                 logger.info("RTSP: " + streamUri);
             } catch (Exception e) {
                 logger.info("mjpeg stream cannot connect to rtsp", e);
             }

        }

        // add mpeg4 video output
         if (config.enableMPEG4) {
            String outputName = videoOutName + videoOutNum;
            mpeg4VideoOutput = new OnvifVideoOutput(this, outputName);
            addOutput(mpeg4VideoOutput, false);
            mpeg4VideoOutput.init();

             try {
                 try {
                     streamUri = URI.create(camera.getMedia().getHTTPStreamUri(mpeg4Profile.getToken()));
                 } catch (SOAPException e) {
                     throw new RuntimeException();
                 } catch (SOAPFaultException e) {
                     throw new RuntimeException(e);
                 }
                 logger.info("HTTP: " + streamUri);
             } catch (Exception e) {
                 logger.info("mpeg4 stream cannot connect to http", e);
             }
             try{
                 try {
                     streamUri = URI.create(camera.getMedia().getHTTPStreamUri(mpeg4Profile.getToken()));
                 } catch (SOAPException e) {
                     throw new RuntimeException();
                 } catch (SOAPFaultException e) {
                     throw new RuntimeException(e);
                 }
                 logger.info("RTSP: " + streamUri);
             } catch (Exception e) {
                 logger.info("mpeg4 stream cannot connect to rtsp", e);
             }

         }
        // add PTZ output
        ptzPosOutput = new OnvifPtzOutput(this);
        addOutput(ptzPosOutput, false);
        ptzPosOutput.init();

        // add PTZ controller
        ptzControlInterface = new OnvifPtzControl(this);
        addControlInput(ptzControlInterface);
        ptzControlInterface.init();
    }

    @Override
    protected void doStart() throws SensorHubException {
		// Validate connection to camera
		if (camera == null) {
            throw new SensorHubException("Exception occurred when connecting to camera");
        }

		// start video output for mpeg4
		if (mpeg4VideoOutput != null) {
            mpeg4VideoOutput.start();
        }

        //start video output from H264 rtp
        if (h264VideoOutput!=null){
            //h264VideoOutput.start();
            h264VideoOutput.start(onvifBasicVideoConfig, onvifRTSPConfig, timeout);
        }

        //start video output for mjpeg
        if (mjpegVideoOutput!=null){
            mjpegVideoOutput.start();
        }
		// start PTZ output
		if (ptzPosOutput != null && ptzControlInterface != null) {
			ptzPosOutput.start();
			ptzControlInterface.start();
		}
    }

    @Override
    protected void updateSensorDescription() {
        synchronized(sensorDescLock) {
            // parent class reads SensorML from config if provided
            // and then sets unique ID, outputs and control inputs
            super.updateSensorDescription();

            SMLFactory smlFac = new SMLFactory();

            if (!sensorDescription.isSetDescription())
                sensorDescription.setDescription("ONVIF Video Camera");

            IdentifierList identifierList = smlFac.newIdentifierList();
            sensorDescription.addIdentification(identifierList);
        }
    }

    @Override
    public boolean isConnected() {
//        logger.info("Camera is connected");
//        return camera != null;
        if (connection==null){
            return false;
        }
        return connection.isConnected();
    }

    @Override
    protected void doStop() {}
    @Override
    public void cleanup() {}
    protected String getHostUrl() {return hostIp;}
    protected String getUser() {
        return user;
    }
    protected String getPassword() {
        return password;
    }


}
