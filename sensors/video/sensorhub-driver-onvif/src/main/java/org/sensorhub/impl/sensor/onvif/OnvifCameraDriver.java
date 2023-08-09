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
import de.onvif.soap.devices.MediaDevice;
import de.onvif.soap.exception.SOAPFaultException;
import net.opengis.sensorml.v20.IdentifierList;
import org.onvif.ver10.device.wsdl.GetDeviceInformationResponse;
import org.onvif.ver10.schema.*;
import org.sensorhub.api.common.SensorHubException;
import org.sensorhub.api.sensor.SensorException;
import org.sensorhub.impl.sensor.AbstractSensorModule;
import org.vast.sensorML.SMLFactory;

import javax.xml.soap.SOAPException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URL;
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
    OnvifVideoOutputH264 h264VideoOutput;
    OnvifVideoOutput mpeg4VideoOutput;
    OnvifVideoOutput mjpegVideoOutput;

//    OnvifVideoControl videoControlInterface;
    OnvifPtzOutput ptzPosOutput;
    OnvifPtzControl ptzControlInterface;
    String hostIp;
    Integer port;
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
    String profileToken;
    OnvifRTSPConfig onvifRTSPConfig;
    VideoResolution videoResolution;
    OnvifBasicVideoConfig onvifBasicVideoConfig;

    URI streamUri;
    URL streamUrl;
    Profile mjpegProfile;


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

        if (profiles == null || profiles.isEmpty()) {
            throw new SensorHubException("Camera does not have any profiles to use");
        }

        //Media profile for video stream
        for (Profile mediaProfile :profiles) {
            logger.info("Media Profile Found: " + mediaProfile.getName());
            VideoEncoderConfiguration videoEncoderConfiguration = mediaProfile.getVideoEncoderConfiguration();
            VideoEncoding videoEncoding = videoEncoderConfiguration.getEncoding();
            MediaDevice media = camera.getMedia();
            profileToken = mediaProfile.getToken();
            logger.info("profile token: " + mediaProfile.getToken());
            VideoEncoderConfigurationOptions videoEncoderConfigurationOptions;
            try {
                videoEncoderConfigurationOptions = media.getVideoEncoderConfigurationOptions(profileToken);
            } catch (SOAPException | ConnectException | SOAPFaultException e) {
                throw new RuntimeException(e);
            }

            if (videoEncoding == VideoEncoding.H_264) {
                h264Profile = mediaProfile;
                H264Options h264Options = videoEncoderConfigurationOptions.getH264();
                //video res are being put into a list
                List<VideoResolution> resolutionsAvailable = h264Options.getResolutionsAvailable();
                h264Profile.getVideoEncoderConfiguration();
                videoResolution= resolutionsAvailable.get(0);
                videoEncoderConfiguration.setResolution(videoResolution);
                logger.info("h264 Resolution: " + videoResolution.getWidth() + "x" + videoResolution.getHeight());
                videoEncoderConfiguration.setQuality(videoEncoderConfigurationOptions.getQualityRange().getMax());
                VideoRateControl videoRateControl = videoEncoderConfiguration.getRateControl();
                videoRateControl.setFrameRateLimit(h264Options.getFrameRateRange().getMax());
                videoRateControl.setEncodingInterval(h264Options.getEncodingIntervalRange().getMin());
                videoRateControl.setBitrateLimit(videoRateControl.getBitrateLimit());
                videoEncoderConfiguration.setRateControl(videoRateControl);
                h264Profile.setVideoEncoderConfiguration(videoEncoderConfiguration);
                try {
                    media.setVideoEncoderConfiguration(videoEncoderConfiguration);
                } catch (SOAPException | ConnectException | SOAPFaultException e) {
                    throw new RuntimeException(e);
                }
            }

            //MPEG4 video Encoder
            if (videoEncoding == VideoEncoding.MPEG_4) {
                logger.info("mpeg4 profile token" + mediaProfile.getToken());
                mpeg4Profile = mediaProfile;

                Mpeg4Options mpeg4Options = videoEncoderConfigurationOptions.getMPEG4();
                List<VideoResolution> resolutionsAvailable = mpeg4Options.getResolutionsAvailable();
                mpeg4Profile.getVideoEncoderConfiguration();
                videoResolution= resolutionsAvailable.get(0);
                videoEncoderConfiguration.setResolution(videoResolution);
                logger.info("mpeg4 resolution: " + videoResolution.getWidth() + "x" + videoResolution.getHeight());
                videoEncoderConfiguration.setQuality(videoEncoderConfigurationOptions.getQualityRange().getMax());
                VideoRateControl videoRateControl = videoEncoderConfiguration.getRateControl();
                videoRateControl.setFrameRateLimit(mpeg4Options.getFrameRateRange().getMax());
                videoRateControl.setEncodingInterval(mpeg4Options.getEncodingIntervalRange().getMin());
                videoRateControl.setBitrateLimit(videoRateControl.getBitrateLimit());
                videoEncoderConfiguration.setRateControl(videoRateControl);
                mpeg4Profile.setVideoEncoderConfiguration(videoEncoderConfiguration);
                try {
                    media.setVideoEncoderConfiguration(videoEncoderConfiguration);
                } catch (SOAPException | ConnectException | SOAPFaultException e) {
                    throw new RuntimeException(e);
                }
            }

            //Mjpeg video Encoder
            if (videoEncoding == VideoEncoding.JPEG) {
                logger.info("mjpeg profile token" + mediaProfile.getToken());
                mjpegProfile = mediaProfile;
                JpegOptions jpegOptions = videoEncoderConfigurationOptions.getJPEG();
                List<VideoResolution> resolutionsAvailable = jpegOptions.getResolutionsAvailable();
                mjpegProfile.getVideoEncoderConfiguration();
                videoResolution= resolutionsAvailable.get(0);
                videoEncoderConfiguration.setResolution(videoResolution);
                logger.info("resolution: " + videoResolution.getWidth() + "x" + videoResolution.getHeight());
                videoEncoderConfiguration.setQuality(videoEncoderConfigurationOptions.getQualityRange().getMax());
                VideoRateControl videoRateControl = videoEncoderConfiguration.getRateControl();
                videoRateControl.setFrameRateLimit(jpegOptions.getFrameRateRange().getMax());
                videoRateControl.setEncodingInterval(jpegOptions.getEncodingIntervalRange().getMin());
                videoRateControl.setBitrateLimit(videoRateControl.getBitrateLimit());
                videoEncoderConfiguration.setRateControl(videoRateControl);
                mjpegProfile.setVideoEncoderConfiguration(videoEncoderConfiguration);
                try {
                    media.setVideoEncoderConfiguration(videoEncoderConfiguration);
                } catch (SOAPException | ConnectException | SOAPFaultException e) {
                    throw new RuntimeException(e);
                }
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

        //get media profile

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
            h264VideoOutput = new OnvifVideoOutputH264(outputName, this); //call to RTPvideoOutput
            addOutput(h264VideoOutput, false);
            //call to init function to get resolution of video
            //h264VideoOutput.init(h264Profile.getVideoEncoderConfiguration().getResolution().getWidth(), h264Profile.getVideoEncoderConfiguration().getResolution().getHeight());

            h264VideoOutput.init(videoResolution.getWidth(), videoResolution.getHeight());

            try {
                try {
                    streamUrl= URI.create(camera.getMedia().getHTTPStreamUri(h264Profile.getToken())).toURL();
                    //streamUri = URI.create(camera.getMedia().getHTTPStreamUri(h264Profile.getToken()));
                } catch (SOAPException e) {
                    throw new RuntimeException();
                } catch (SOAPFaultException e) {
                    throw new RuntimeException(e);
                }
                logger.info("HTTP: " + streamUrl);
            } catch (Exception e) {
                logger.info("h264 stream cannot connect to http", e);
            }
            try{
                try {
                    streamUrl= URI.create(camera.getMedia().getRTSPStreamUri(h264Profile.getToken())).toURL();
                } catch (SOAPException e) {
                    throw new RuntimeException();
                } catch (SOAPFaultException e) {
                    throw new RuntimeException(e);
                }
                logger.info("RTSP: " + streamUrl);
            } catch (Exception e) {
                logger.info("h264 stream cannot connect to rtsp", e);
            }


           /* try {
                logger.info("http uri:" + camera.getMedia().getHTTPStreamUri(h264Profile.getToken()));  //this is what it needs to work
            } catch (ConnectException e) {
                throw new RuntimeException(e);
            } catch (SOAPException e) {
                throw new RuntimeException(e);
            } catch (SOAPFaultException e) {
                throw new RuntimeException(e);
            }*/

        }
         if (config.enableMJPEG) {
             String outputName = videoOutName + videoOutNum;
             mjpegVideoOutput = new OnvifVideoOutput(this, outputName);
             addOutput(mjpegVideoOutput, false);
             mjpegVideoOutput.init();

             try {
                 try {
                     streamUrl= URI.create(camera.getMedia().getHTTPStreamUri(mjpegProfile.getToken())).toURL();
                 } catch (SOAPException e) {
                     throw new RuntimeException();
                 } catch (SOAPFaultException e) {
                     throw new RuntimeException(e);
                 }
                 logger.info("HTTP: " + streamUrl);
             } catch (Exception e) {
                 logger.info("mjpeg stream cannot connect to http", e);
             }
             try{
                 try {
                     streamUrl= URI.create(camera.getMedia().getRTSPStreamUri(mjpegProfile.getToken())).toURL();
                 } catch (SOAPException e) {
                     throw new RuntimeException();
                 } catch (SOAPFaultException e) {
                     throw new RuntimeException(e);
                 }
                 logger.info("RTSP: " + streamUrl);
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
                     streamUrl= URI.create(camera.getMedia().getHTTPStreamUri(mpeg4Profile.getToken())).toURL();
                 } catch (SOAPException e) {
                     throw new RuntimeException();
                 } catch (SOAPFaultException e) {
                     throw new RuntimeException(e);
                 }
                 logger.info("HTTP: " + streamUrl);
             } catch (Exception e) {
                 logger.info("mpeg4 stream cannot connect to http", e);
             }
             try{
                 try {
                     streamUrl= URI.create(camera.getMedia().getRTSPStreamUri(mpeg4Profile.getToken())).toURL();
                 } catch (SOAPException e) {
                     throw new RuntimeException();
                 } catch (SOAPFaultException e) {
                     throw new RuntimeException(e);
                 }
                 logger.info("RTSP: " + streamUrl);
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
		if (camera == null)
			throw new SensorHubException("Exception occurred when connecting to camera");

		// start video output for mpeg4
		if (mpeg4VideoOutput != null) {
            mpeg4VideoOutput.start();
        }
        //start video output from H264 rtp
        if (h264VideoOutput != null) {
            h264VideoOutput.start(streamUrl,timeout);
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
        logger.info("Camera is connected");
        return camera != null;
    }

    @Override
    protected void doStop() {}

    @Override
    public void cleanup() {}

    protected String getHostUrl() {
        return hostIp;
    }

    protected String getUser() {
        return user;
    }
    protected String getPassword() {
        return password;
    }

}
