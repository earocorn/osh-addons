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

import com.botts.api.sensor.datafeed.parser.DataParserConfig;
import com.botts.api.sensor.datafeed.parser.IDataParser;
import net.opengis.swe.v20.DataComponent;
import org.sensorhub.api.comm.ICommProvider;
import org.sensorhub.api.common.SensorHubException;
import org.sensorhub.impl.sensor.AbstractSensorModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * DataFeedDriver implementation for the sensor.
 * <p>
 * This class is responsible for providing sensor information, managing output registration,
 * and performing initialization and shutdown for the driver and its outputs.
 */
public class DataFeedDriver extends AbstractSensorModule<DataFeedConfig> {
    static final String UID_PREFIX = "urn:osh:driver:datafeed:";
    static final String XML_PREFIX = "DATAFEED_DRIVER_";

    private static final Logger logger = LoggerFactory.getLogger(DataFeedDriver.class);

    DataFeedOutput output;
    Thread processingThread;
    volatile boolean doProcessing = true;
    ICommProvider<?> commProvider;
    IDataParser dataParser;

    @Override
    public void doInit() throws SensorHubException {
        super.doInit();

        // Generate identifiers
        generateUniqueID(UID_PREFIX, config.serialNumber);
        generateXmlID(XML_PREFIX, config.serialNumber);

        // Create and initialize output
        output = new DataFeedOutput(this);
        addOutput(output, false);
        output.init();
    }

    @Override
    public void doStart() throws SensorHubException {
        super.doStart();

        // init comm provider
        if (commProvider == null)
        {
            try {
                if (config.commSettings == null)
                    throw new SensorHubException("No communication settings specified");

                var moduleReg = getParentHub().getModuleRegistry();
                commProvider = (ICommProvider<?>)moduleReg.loadSubModule(config.commSettings, true);
                commProvider.start();
                Class<?> clazz = config.dataParserConfig.getDataParserClass();
                Constructor<?> constructor = clazz.getConstructor(config.dataParserConfig.getClass(), DataComponent.class);
                dataParser = (IDataParser) constructor.newInstance(config.dataParserConfig, output.getRecordDescription());
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                commProvider = null;
                dataParser = null;
                throw new RuntimeException(e);
            }
        }

        startProcessing();
    }

    @Override
    public void doStop() throws SensorHubException {
        super.doStop();
        stopProcessing();
    }

    @Override
    public boolean isConnected() {
        return processingThread != null && processingThread.isAlive();
    }

    /**
     * Starts the data processing thread.
     * <p>
     * This method simulates sensor data collection and processing by generating data samples at regular intervals.
     */
    public void startProcessing() {
        doProcessing = true;

        processingThread = new Thread(() -> {
            if (commProvider != null && commProvider.isStarted()) {
                try {
                    dataParser.subscribe(commProvider.getInputStream(), (dataBlock) -> {
                        output.setData(dataBlock);
                    });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        processingThread.start();
    }

    /**
     * Signals the processing thread to stop.
     */
    public void stopProcessing() {
        doProcessing = false;
    }
}
