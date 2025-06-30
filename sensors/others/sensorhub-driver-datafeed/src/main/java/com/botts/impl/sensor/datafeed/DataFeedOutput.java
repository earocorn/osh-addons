/***************************** BEGIN LICENSE BLOCK ***************************
 The contents of this file are subject to the Mozilla Public License, v. 2.0.
 If a copy of the MPL was not distributed with this file, You can obtain one
 at http://mozilla.org/MPL/2.0/.

 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 for the specific language governing rights and limitations under the License.

 Copyright (C) 2020-2025 Botts Innovative Research, Inc. All Rights Reserved.
 ******************************* END LICENSE BLOCK ***************************/
package com.botts.impl.sensor.datafeed;

import com.botts.impl.sensor.datafeed.data.BaseDataType;
import com.botts.impl.sensor.datafeed.data.DataComponentConfig;
import com.botts.impl.sensor.datafeed.data.DataRecordConfig;
import net.opengis.swe.v20.DataBlock;
import net.opengis.swe.v20.DataComponent;
import net.opengis.swe.v20.DataEncoding;
import net.opengis.swe.v20.DataRecord;
import org.sensorhub.api.data.DataEvent;
import org.sensorhub.impl.sensor.AbstractSensorOutput;
import org.sensorhub.impl.sensor.VarRateSensorOutput;
import org.vast.swe.SWEBuilders;
import org.vast.swe.SWEConstants;
import org.vast.swe.SWEHelper;
import org.vast.swe.helper.GeoPosHelper;
import org.vast.swe.helper.VectorHelper;

import java.util.ArrayList;

import static com.botts.impl.sensor.datafeed.DataFeedUtils.setFieldData;

/**
 * DataFeedOutput specification and provider for {@link DataFeedDriver}.
 */
public class DataFeedOutput extends VarRateSensorOutput<DataFeedDriver> {

    private final DataRecordConfig dataRecordConfig;
    private DataRecord dataRecord;
    private DataEncoding dataEncoding;

    /**
     * Creates a new output for the sensor driver.
     *
     * @param parentDriver Sensor driver providing this output.
     */
    DataFeedOutput(DataFeedDriver parentDriver) {
        super(parentDriver.getConfiguration().dataParserConfig.outputStructure.name, parentDriver, 5.0);
        this.dataRecordConfig = parentDriver.getConfiguration().dataParserConfig.outputStructure;
    }

    /**
     * Initializes the data structure for the output, defining the fields, their ordering, and data types.
     */
    void init() {
        // Get an instance of SWE Factory suitable to build components
        GeoPosHelper sweFactory = new GeoPosHelper();

        SWEBuilders.DataRecordBuilder dataRecordBuilder = sweFactory.createRecord()
                .name(dataRecordConfig.name)
                .label(dataRecordConfig.label)
                .description(dataRecordConfig.description)
                .addField("sampleTime", sweFactory.createTime()
                        .asSamplingTimeIsoUTC()
                        .label("Sample Time")
                        .description("Time of data collection"));

        for (DataComponentConfig field : dataRecordConfig.fields) {
            var component = DataFeedUtils.createDataComponent(field)
                    .label(field.label)
                    .description(field.description)
                    .definition(field.definition);

            if (!field.uom.isBlank() && field.dataType == BaseDataType.FLOAT || field.dataType == BaseDataType.DOUBLE || field.dataType == BaseDataType.BYTE || field.dataType == BaseDataType.LONG)
                ((SWEBuilders.QuantityBuilder) component).uom(field.uom);

            dataRecordBuilder.addField(field.name, component);
        }

        dataRecord = dataRecordBuilder.build();
        dataEncoding = sweFactory.newTextEncoding(",", "\n");
    }

    @Override
    public DataComponent getRecordDescription() {
        return dataRecord;
    }

    @Override
    public DataEncoding getRecommendedEncoding() {
        return dataEncoding;
    }

    public void setData(DataBlock data) {
        DataBlock dataBlock = latestRecord == null ? dataRecord.createDataBlock() : latestRecord.renew();

        if (dataBlock.getAtomCount() != data.getAtomCount())
            throw new IllegalArgumentException("Driver output structure does not match parser output structure");

        // Publish the data block
        latestRecord = data;
        latestRecordTime = data.getLongValue(0);
        eventHandler.publish(new DataEvent(latestRecordTime, DataFeedOutput.this, data));
    }
}
