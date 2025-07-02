/***************************** BEGIN LICENSE BLOCK ***************************

 The contents of this file are subject to the Mozilla Public License, v. 2.0.
 If a copy of the MPL was not distributed with this file, You can obtain one
 at http://mozilla.org/MPL/2.0/.

 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 for the specific language governing rights and limitations under the License.

 The Initial Developer is Botts Innovative Research Inc. Portions created by the Initial
 Developer are Copyright (C) 2025 the Initial Developer. All Rights Reserved.

 ******************************* END LICENSE BLOCK ***************************/

package com.botts.impl.driver.civiliot;

import net.opengis.swe.v20.*;
import org.sensorhub.api.data.DataEvent;
import org.sensorhub.api.sensor.ISensorModule;
import org.sensorhub.impl.sensor.VarRateSensorOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vast.cdm.common.CDMException;
import org.vast.data.AbstractDataBlock;
import org.vast.data.DataBlockMixed;
import org.vast.swe.SWEHelper;
import org.vast.swe.helper.RasterHelper;

public class VideoOutput<T extends ISensorModule<?>> extends VarRateSensorOutput<T> {
    private static final String CODEC_MJPEG = "JPEG";
    private static final Logger logger = LoggerFactory.getLogger(VideoOutput.class.getSimpleName());

    private final String outputLabel;
    private final String outputDescription;
    private final int videoFrameWidth;
    private final int videoFrameHeight;
    private final String codecName;

    private DataComponent dataStruct;
    private DataEncoding dataEncoding;

    /**
     * Creates a new video output.
     *
     * @param parentSensor         Sensor driver providing this output.
     * @param videoFrameDimensions The width and height of the video frame.
     */
    public VideoOutput(T parentSensor, int[] videoFrameDimensions) {
        this(parentSensor, videoFrameDimensions, "video", "Video", "Video stream using ffmpeg library", 0.04167);
    }

    /**
     * Creates a new video output.
     *
     * @param parentSensor         Sensor driver providing this output.
     * @param videoFrameDimensions The width and height of the video frame.
     * @param name                 The name of the output.
     * @param outputLabel          The label of the output.
     * @param outputDescription    The description of the output.
     */
    public VideoOutput(T parentSensor, int[] videoFrameDimensions, String name, String outputLabel, String outputDescription, double initialSamplingPeriod) {
        super(name, parentSensor, initialSamplingPeriod);

        this.videoFrameWidth = videoFrameDimensions[0];
        this.videoFrameHeight = videoFrameDimensions[1];
        this.outputLabel = outputLabel;
        this.outputDescription = outputDescription;
        this.codecName = CODEC_MJPEG;

        logger.debug("Video output created.");
    }

    /**
     * Initializes the data structure for the output, defining the fields, their ordering, and data types.
     */
    public void init() {
        logger.debug("Initializing video output.");

        RasterHelper sweHelper = new RasterHelper();
        dataStruct = sweHelper.createRecord()
                .name(getName())
                .label(outputLabel)
                .description(outputDescription)
                .definition(SWEHelper.getPropertyUri("VideoFrame"))
                .addField("sampleTime", sweHelper.createTime()
                        .asSamplingTimeIsoUTC()
                        .label("Sample Time")
                        .description("Time of data collection"))
                .addField("phenomenonTime", sweHelper.createTime()
                        .asPhenomenonTimeIsoUTC()
                        .label("Phenomenon Time")
                        .description("Time reported by sensor"))
                .addField("img", sweHelper.newRgbImage(videoFrameWidth, videoFrameHeight, DataType.BYTE))
                .build();

        BinaryEncoding dataEnc = sweHelper.newBinaryEncoding(ByteOrder.BIG_ENDIAN, ByteEncoding.RAW);


        BinaryComponent sampleTimeEnc = sweHelper.newBinaryComponent();
        sampleTimeEnc.setRef("/" + dataStruct.getComponent(0).getName());
        sampleTimeEnc.setCdmDataType(DataType.DOUBLE);
        dataEnc.addMemberAsComponent(sampleTimeEnc);

        BinaryComponent phenomenonTimeEnc = sweHelper.newBinaryComponent();
        phenomenonTimeEnc.setRef("/" + dataStruct.getComponent(1).getName());
        phenomenonTimeEnc.setCdmDataType(DataType.DOUBLE);
        dataEnc.addMemberAsComponent(phenomenonTimeEnc);

        BinaryBlock compressedBlock = sweHelper.newBinaryBlock();
        compressedBlock.setRef("/" + dataStruct.getComponent(2).getName());
        compressedBlock.setCompression(codecName);
        dataEnc.addMemberAsBlock(compressedBlock);

        try {
            SWEHelper.assignBinaryEncoding(dataStruct, dataEnc);
        } catch (CDMException e) {
            throw new RuntimeException("Invalid binary encoding configuration", e);
        }

        this.dataEncoding = dataEnc;
    }

    @Override
    public DataComponent getRecordDescription() {
        return dataStruct;
    }

    @Override
    public DataEncoding getRecommendedEncoding() {
        return dataEncoding;
    }

    /**
     * Sets the video frame data in the output.
     *
     * @param dataBuffer The data buffer record containing the video frame data.
     * @param phenomenonTime The time reported by the sensor
     */
    public void processBuffer(byte[] dataBuffer, long phenomenonTime, String foiUID) {
        long timestamp = System.currentTimeMillis();

        DataBlock dataBlock = latestRecord == null ? dataStruct.createDataBlock() : latestRecord.renew();

        dataBlock.setDoubleValue(0, timestamp / 1000d);
        dataBlock.setDoubleValue(1, phenomenonTime / 1000d);

        // Set underlying video frame data
        AbstractDataBlock frameData = ((DataBlockMixed) dataBlock).getUnderlyingObject()[2];
        frameData.setUnderlyingObject(dataBuffer);

        latestRecord = dataBlock;
        latestRecordTime = timestamp;

        eventHandler.publish(new DataEvent(latestRecordTime, this, foiUID, dataBlock));
    }

    public void processBuffer(byte[] dataBuffer, long phenomenonTime) {
        processBuffer(dataBuffer, phenomenonTime, null);
    }


}
