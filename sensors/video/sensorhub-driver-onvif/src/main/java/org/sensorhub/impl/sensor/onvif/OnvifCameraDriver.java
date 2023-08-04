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
    //RTPVideoOutput<OnvifCameraDriver> h264VideoOutputRTP;

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
    OnvifRTSPConfig onvifRTSPConfig;
    OnvifBasicVideoConfig onvifBasicVideoConfig;
   // BasicVideoConfig basicVideoConfig;
    //RTSPConfig rtspConfig;
    URI streamUri;
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
        //h264VideoOutputRTP = null;
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

        /*       serialNumber = camera.getDevices(). getDeviceInformation().getSerialNumber().trim();
        modelNumber = camera.getDevices().getDeviceInformation().getModel().trim();
        shortName = camera.getDevices().getDeviceInformation().getManufacturer().trim();
        longName = shortName + "_" + modelNumber + "_" + serialNumber;*/

        // generate identifiers
        generateUniqueID("urn:onvif:cam:", serialNumber);
        generateXmlID("ONVIF_CAM_", serialNumber);


            //list any profiles the camera may have
            List<Profile> profiles = camera.getDevices().getProfiles();
            logger.info("List of profiles: " + profiles);
            if (profiles == null || profiles.isEmpty()) {
                throw new SensorHubException("Camera does not have any profiles to use");
            }

            //ks
            //Media profile for video stream
        for (Profile mediaProfile :profiles){
            logger.info("Profile Name: "+ mediaProfile.getName());
            VideoEncoderConfiguration videoEncoderConfiguration = mediaProfile.getVideoEncoderConfiguration();
            VideoEncoding videoEncoding = videoEncoderConfiguration.getEncoding();
            MediaDevice media = camera.getMedia();
            if (videoEncoding == VideoEncoding.H_264) {
                logger.info("h264 profile token" + mediaProfile.getToken());
                h264Profile = mediaProfile;
                String profileToken = h264Profile.getToken();
                VideoEncoderConfigurationOptions videoEncoderConfigurationOptions;
                try {
                    videoEncoderConfigurationOptions = media.getVideoEncoderConfigurationOptions(profileToken);
                } catch (SOAPException | ConnectException | SOAPFaultException e) {
                    throw new RuntimeException(e);
                }
                H264Options h264Options = videoEncoderConfigurationOptions.getH264();
                List<VideoResolution> resolutions = h264Options.getResolutionsAvailable();
                int maxRes=-1;
                int selectedResIndex=-1;
                for (int index=0; index<resolutions.size(); index++){
                    VideoResolution videoResolution = resolutions.get(index);
                    int res= videoResolution.getWidth()* videoResolution.getHeight();
                    if (res>maxRes){
                        maxRes=res;
                        selectedResIndex=index;
                    }
                }
                logger.info("resolutions avail: "+ resolutions);
                h264Profile.getVideoEncoderConfiguration();
                VideoResolution videoResolution= resolutions.get(selectedResIndex);
                videoEncoderConfiguration.setResolution(videoResolution);
                logger.info("resolution selected: "+ videoResolution.getWidth()+ "x"+ videoResolution.getHeight());
                videoEncoderConfiguration.setQuality(videoEncoderConfigurationOptions.getQualityRange().getMax());
                VideoRateControl videoRateControl= videoEncoderConfiguration.getRateControl();
                videoRateControl.setFrameRateLimit(h264Options.getFrameRateRange().getMax());
                videoRateControl.setEncodingInterval(h264Options.getEncodingIntervalRange().getMin());
                videoRateControl.setBitrateLimit(videoEncoderConfigurationOptions.getExtension().getH264().getBitrateRange().getMax());
                videoEncoderConfiguration.setRateControl(videoRateControl);
                h264Profile.setVideoEncoderConfiguration(videoEncoderConfiguration);
                try {
                    media.setVideoEncoderConfiguration(videoEncoderConfiguration);
                } catch (SOAPException e) {
                    throw new RuntimeException(e);
                } catch (ConnectException e) {
                    throw new RuntimeException(e);
                } catch (SOAPFaultException e) {
                    throw new RuntimeException(e);
                }

            }
            else if(videoEncoding==VideoEncoding.MPEG_4) {
                logger.info("mpeg4 profile token" + mediaProfile.getToken());
                mpeg4Profile = mediaProfile;
                String profileToken = mjpegProfile.getToken();
                VideoEncoderConfigurationOptions videoEncoderConfigurationOptions;
                try {
                    videoEncoderConfigurationOptions = media.getVideoEncoderConfigurationOptions(profileToken);
                } catch (SOAPException | ConnectException | SOAPFaultException e) {
                    throw new RuntimeException(e);
                }
                Mpeg4Options mpeg4Options = videoEncoderConfigurationOptions.getMPEG4();
                List<VideoResolution> resolutions = mpeg4Options.getResolutionsAvailable();
                logger.info("resolutions avail: "+ resolutions);
                int maxRes=-1;
                int selectedResIndex=-1;
                for (int index=0; index<resolutions.size(); index++){
                    VideoResolution videoResolution = resolutions.get(index);
                    int res= videoResolution.getWidth()* videoResolution.getHeight();
                    if (res>maxRes){
                        maxRes=res;
                        selectedResIndex=index;
                    }
                }
                mpeg4Profile.getVideoEncoderConfiguration();
                VideoResolution videoResolution= resolutions.get(selectedResIndex);
                videoEncoderConfiguration.setResolution(videoResolution);
                logger.info("resolution: "+ videoResolution.getWidth()+ "x"+ videoResolution.getHeight());
                videoEncoderConfiguration.setQuality(videoEncoderConfigurationOptions.getQualityRange().getMax());
                VideoRateControl videoRateControl= videoEncoderConfiguration.getRateControl();
                videoRateControl.setFrameRateLimit(mpeg4Options.getFrameRateRange().getMax());
                videoRateControl.setEncodingInterval(mpeg4Options.getEncodingIntervalRange().getMin());
                videoRateControl.setBitrateLimit(videoEncoderConfigurationOptions.getExtension().getMPEG4().getBitrateRange().getMax());
                videoEncoderConfiguration.setRateControl(videoRateControl);
                mpeg4Profile.setVideoEncoderConfiguration(videoEncoderConfiguration);
                try {
                    media.setVideoEncoderConfiguration(videoEncoderConfiguration);
                } catch (SOAPException e) {
                    throw new RuntimeException(e);
                } catch (ConnectException e) {
                    throw new RuntimeException(e);
                } catch (SOAPFaultException e) {
                    throw new RuntimeException(e);
                }
            }
            else{
                logger.info("mjpeg profile token"+ mediaProfile.getToken());
                mjpegProfile= mediaProfile;
                String profileToken = mjpegProfile.getToken();
                VideoEncoderConfigurationOptions videoEncoderConfigurationOptions;
                try {
                    videoEncoderConfigurationOptions = media.getVideoEncoderConfigurationOptions(profileToken);
                } catch (SOAPException | ConnectException | SOAPFaultException e) {
                    throw new RuntimeException(e);
                }
                JpegOptions jpegOptions = videoEncoderConfigurationOptions.getJPEG();
                List<VideoResolution> resolutions = jpegOptions.getResolutionsAvailable();
                logger.info("resolutions avail: "+ resolutions);
                int maxRes=-1;
                int selectedResIndex=-1;
                for (int index=0; index<resolutions.size(); index++){
                    VideoResolution videoResolution = resolutions.get(index);
                    int res= videoResolution.getWidth()* videoResolution.getHeight();
                    if (res>maxRes){
                        maxRes=res;
                        selectedResIndex=index;
                    }
                }
                mjpegProfile.getVideoEncoderConfiguration();
                VideoResolution videoResolution= resolutions.get(selectedResIndex);
                videoEncoderConfiguration.setResolution(videoResolution);
                logger.info("resolution: "+ videoResolution.getWidth()+ "x"+ videoResolution.getHeight());
                videoEncoderConfiguration.setQuality(videoEncoderConfigurationOptions.getQualityRange().getMax());
                VideoRateControl videoRateControl= videoEncoderConfiguration.getRateControl();
                videoRateControl.setFrameRateLimit(jpegOptions.getFrameRateRange().getMax());
                videoRateControl.setEncodingInterval(jpegOptions.getEncodingIntervalRange().getMin());
                videoRateControl.setBitrateLimit(videoEncoderConfigurationOptions.getExtension().getJPEG().getBitrateRange().getMax());
                videoEncoderConfiguration.setRateControl(videoRateControl);
                mjpegProfile.setVideoEncoderConfiguration(videoEncoderConfiguration);
                try {
                    media.setVideoEncoderConfiguration(videoEncoderConfiguration);
                } catch (SOAPException e) {
                    throw new RuntimeException(e);
                } catch (ConnectException e) {
                    throw new RuntimeException(e);
                } catch (SOAPFaultException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        //video resolutions available for the device

        /*
//            for (Profile mediaProfile : profiles) {
//                VideoEncoderConfiguration videoEncoderConfiguration = mediaProfile.getVideoEncoderConfiguration();
//                VideoEncoding videoEncoding = videoEncoderConfiguration.getEncoding();
//                if (videoEncoding == VideoEncoding.H_264)
//                {
//                    logger.info("profile token"+ mediaProfile.getToken());
//                    h264Profile = mediaProfile;
//                    if (h264Profile == null) {
//                        throw new SensorHubException("No H264 profiles available for camera at " + hostIp);
//                    }
//                }
//                else if(videoEncoding==VideoEncoding.MPEG_4){
//                    mpeg4Profile=mediaProfile;
//                    if (mpeg4Profile == null) {
//                        throw new SensorHubException("No MPEG4 profiles available for camera at " + hostIp);
//                    }
//                }
            }*/
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
        //media = camera.getMedia();
        if(h264Profile!=null) {
            H264Configuration h264Config = h264Profile.getVideoEncoderConfiguration().getH264();
            if (config.enableH264 && h264Config == null) {
                throw new SensorException("Cannot connect to H264 stream - H264 not supported");
            }
        }
        else {
            Mpeg4Configuration mpeg4Config = mpeg4Profile.getVideoEncoderConfiguration().getMPEG4();
            if (config.enableMPEG4 && mpeg4Config == null) {
                throw new SensorException("Cannot connect to MPEG4 stream - MPEG4 not supported");
            }
        }
    //    logger.info("Get bounds: "+ String.valueOf(profile.getVideoSourceConfiguration().getBounds().getWidth()+ " x "+ String.valueOf(profile.getVideoSourceConfiguration().getBounds().getHeight())));
//        logger.info("Get resolution: "+ String.valueOf(profile.getVideoEncoderConfiguration().getResolution().getWidth()+ " x "+String.valueOf(profile.getVideoEncoderConfiguration().getResolution().getHeight())));

        // create I/O objects
        String videoOutName = "video";
        int videoOutNum = 1;

        //config profiles
        //h264 configuration enabled
        if (config.enableH264) {
            String outputName = videoOutName + videoOutNum; //output name
            h264VideoOutput = new OnvifVideoOutputH264(outputName, this); //call to RTPvideoOutput
            addOutput(h264VideoOutput, false);
            //call to init function to get resolution of video
            h264VideoOutput.init(h264Profile.getVideoEncoderConfiguration().getResolution().getWidth(), h264Profile.getVideoEncoderConfiguration().getResolution().getHeight());

/*            h264VideoOutputRTP = new RTPVideoOutput<>(outputName, this); //call to RTPvideoOutput
//            addOutput(h264VideoOutputRTP, false);
//
//            //call to init function to get resolution of video
//            h264VideoOutputRTP.init(h264Profile.getVideoEncoderConfiguration().getResolution().getWidth(), h264Profile.getVideoEncoderConfiguration().getResolution().getHeight());
*/

            /*
                 h264VideoOutput= new OnvifVideoOutputH264(this, outputName);
                addOutput(h264VideoOutput, false);
                h264VideoOutput.init();
             */


            //logger.info("logger info source: "+ profile.getVideoSourceConfiguration().getSourceToken());
            //logger.info("logger info encoder: "+ profile.getVideoEncoderConfiguration().getToken());
            try {
                try {
                    streamUri = URI.create(camera.getMedia().getHTTPStreamUri(h264Profile.getToken()));
                } catch (SOAPException e) {
                    throw new RuntimeException();
                } catch (SOAPFaultException e) {
                    throw new RuntimeException(e);
                }
                logger.info("HTTP Stream: " + streamUri);
            } catch (Exception e) {
                logger.info("cannot connect to http stream", e);
            }
            try {
                try {
                    streamUri = URI.create(camera.getMedia().getRTSPStreamUri(h264Profile.getToken()));
                } catch (SOAPException e) {
                    throw new RuntimeException();
                } catch (SOAPFaultException e) {
                    throw new RuntimeException(e);
                }
                logger.info("RTSP Stream: " + streamUri);
            } catch (Exception e) {
                logger.info("cannot connect to rtsp stream", e);
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
                    streamUri = URI.create(camera.getMedia().getHTTPStreamUri(mjpegProfile.getToken()));
                } catch (SOAPException e) {
                    throw new RuntimeException();
                } catch (SOAPFaultException e) {
                    throw new RuntimeException(e);
                }
                logger.info("Mjpeg HTTP Stream: " + streamUri);
            } catch (Exception e) {
                logger.info("Mjpeg: cannot connect to http stream", e);
            }
            try {
                try {
                    streamUri = URI.create(camera.getMedia().getRTSPStreamUri(mjpegProfile.getToken()));
                } catch (SOAPException | SOAPFaultException e) {
                    throw new RuntimeException(e);
                }
                logger.info("Mjpeg RTSP Stream: " + streamUri);
            } catch (Exception e) {
                logger.info("Mjpeg :cannot connect to rtsp stream", e);
            }
        }

        // add MPEG4 video output
        if (config.enableMPEG4) {
            String outputName = videoOutName + videoOutNum;
            mpeg4VideoOutput = new OnvifVideoOutput(this, outputName);
            addOutput(mpeg4VideoOutput, false);
            mpeg4VideoOutput.init();

            try{
                try {
                    streamUri= URI.create(camera.getMedia().getHTTPStreamUri(mpeg4Profile.getToken()));
                } catch (SOAPException e) {
                    throw new RuntimeException();
                } catch (SOAPFaultException e) {
                    throw new RuntimeException(e);
                }
                logger.info("HTTP Stream: "+streamUri);
            }catch(Exception e){
                logger.info("cannot connect to http stream", e);
            }
            try{
                try {
                    streamUri= URI.create(camera.getMedia().getRTSPStreamUri(mpeg4Profile.getToken()));
                } catch (SOAPException | SOAPFaultException e) {
                    throw new RuntimeException(e);
                }
                logger.info("RTSP Stream: "+streamUri);
            }catch(Exception e){
                logger.info ("cannot connect to rtsp stream", e);
            }
//            logger.info("logger info: "+ profile.getVideoSourceConfiguration().getSourceToken());
//            logger.info("logger info: "+ profile.getVideoEncoderConfiguration().getToken());
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

    //start the video and ptz output
    @Override
    protected void doStart() throws SensorHubException {
		// Validate connection to camera
		if (camera == null)
			throw new SensorHubException("Exception occurred when connecting to camera");

		// start video output for Mjpeg
		if (mpeg4VideoOutput != null) {
            mpeg4VideoOutput.start();
            logger.info("Video output for mpeg4 stream is starting");
        }

        //start video output from H264 rtp
        if (h264VideoOutput != null) {
            h264VideoOutput.start(onvifBasicVideoConfig,onvifRTSPConfig, timeout);
            logger.info("Video output for h264 stream is starting");
        }
        if (mjpegVideoOutput!=null){
            mjpegVideoOutput.start();
            logger.info("Video output for mjpeg stream is starting");
        }
		// start PTZ output
		if (ptzPosOutput != null && ptzControlInterface != null) {
			ptzPosOutput.start();
			ptzControlInterface.start();
		}

        /*
        if (h264VideoOutput !=null){
        h264VideoOutput.start(onvifBasicVideoConfig, onvifRTSPConfig, timeout);
        }
         */
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
    public boolean isConnected() {return camera != null;}

    @Override
    protected void doStop() {}

    @Override
    public void cleanup() {}

    protected String getHostUrl() {return hostIp;}


}
