package org.sensorhub.impl.sensor.onvif;

import net.opengis.swe.v20.DataBlock;
import net.opengis.swe.v20.DataComponent;
import net.opengis.swe.v20.DataEncoding;
import net.sf.jipcam.axis.media.protocol.http.MjpegStream;
import org.sensorhub.api.sensor.SensorException;
import org.sensorhub.impl.sensor.AbstractSensorOutput;
import org.vast.data.DataBlockMixed;

import javax.media.Buffer;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;


/**
 * <p>
 * Implementation of sensor interface for generic cameras using ONVIF
 * protocol. This particular class provides time-tagged video output from the video
 * camera capabilities.
 * </p>
 *
 * @author Joshua Wolfe <developer.wolfe@gmail.com>
 * @since June 13, 2017
 */
public class OnvifVideoOutput extends AbstractSensorOutput <OnvifCameraDriver> {
    DataComponent videoDataStruct;
    DataEncoding videoEncoding;
    boolean streaming;
    URL url;

    public OnvifVideoOutput(OnvifCameraDriver driver, String name) {
        super(name, driver);
    }

    protected void init() throws SensorException {
        try {
            // get image size from camera HTTP interface
           // int[] imgSize = getImageSize();
            //VideoCamHelper fac = new VideoCamHelper();

            // build output structure
            //DataStream videoStream = fac.newVideoOutputMJPEG(getName(), imgSize[0], imgSize[1]);
            //videoDataStruct = videoStream.getElementType();
            // videoEncoding = videoStream.getEncoding();

        } catch (Exception e) {
            throw new SensorException("Error while initializing video output ", e);
        }
    }


//    protected int[] getImageSize() throws IOException {
//
//        BufferedReader reader = new BufferedReader(new InputStreamReader(getImgSizeUrl.openStream()));
//
//        int imgSize[] = new int[2];
//        String line;
//        while ((line = reader.readLine()) != null) {
//            // split line and parse each possible property
//            String[] tokens = line.split("=");
//            if (tokens[0].trim().equalsIgnoreCase("image width"))
//                imgSize[0] = Integer.parseInt(tokens[1].trim());
//            else if (tokens[0].trim().equalsIgnoreCase("image height"))
//                imgSize[1] = Integer.parseInt(tokens[1].trim());
//        }
//
//        // index 0 is width, index 1 is height
//        return imgSize;
//    }



    //RTSP port is usually 554, the HTTP/ONVIF port is 80 or 8080
    //http://[IP address]:port/onvif/device_service.

    protected void start() {
        try {

            final URL videoUrl = new URL(parentSensor.getHostUrl().replace("axis-cgi", "mjpg/video.mjpg"));

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    // send http query
                    try {
                        InputStream is = new BufferedInputStream(videoUrl.openStream());
                        MjpegStream stream = new MjpegStream(is, null);
                        streaming = true;

                        while (streaming) {
                            // extract next frame from MJPEG stream
                            Buffer buf = new Buffer();
                            buf.setData(new byte[] {});
                            stream.read(buf);
                            byte[] frameData = (byte[]) buf.getData();

                            // create new data block
                            DataBlock dataBlock;
                            if (latestRecord == null)
                                dataBlock = videoDataStruct.createDataBlock();
                            else
                                dataBlock = latestRecord.renew();

                            //double timestamp = AXISJpegHeaderReader.getTimestamp(frameData) / 1000.;
                            double timestamp = System.currentTimeMillis() / 1000.;
                            dataBlock.setDoubleValue(0, timestamp);
                            //System.out.println(new DateTimeFormat().formatIso(timestamp, 0));

                            // uncompress to RGB bufferd image
                            /*InputStream imageStream = new ByteArrayInputStream(frameData);
                            ImageInputStream input = ImageIO.createImageInputStream(imageStream);
                            Iterator<ImageReader> readers = ImageIO.getImageReadersByMIMEType("image/jpeg");
                            ImageReader reader = readers.next();
                            reader.setInput(input);
                            //int width = reader.getWidth(0);
                            //int height = reader.getHeight(0);

                            // The ImageTypeSpecifier object gives you access to more info such as
                            // bands, color model, etc.
                           // ImageTypeSpecifier imageType = reader.getRawImageType(0);

                            BufferedImage rgbImage = reader.read(0);
                            byte[] byteData = ((DataBufferByte)rgbImage.getRaster().getDataBuffer()).getData();
                            ((DataBlockMixed)dataBlock).getUnderlyingObject()[1].setUnderlyingObject(byteData);*/

                            // assign compressed data
                            ((DataBlockMixed) dataBlock).getUnderlyingObject()[1].setUnderlyingObject(frameData);

                            latestRecord = dataBlock;
                            latestRecordTime = System.currentTimeMillis();
                            //TODO: SensorDataEvent
                            //eventHandler.publish(new SensorDataEvent(latestRecordTime, OnvifVideoOutput.this, latestRecord));
                        }

                        // wait 1s before trying to reconnect
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            t.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public double getAverageSamplingPeriod() {return 1 / 30.;}

    @Override
    public DataComponent getRecordDescription() {return videoDataStruct;}

    @Override
    public DataEncoding getRecommendedEncoding() {return videoEncoding;}

    public void stop() {}

}