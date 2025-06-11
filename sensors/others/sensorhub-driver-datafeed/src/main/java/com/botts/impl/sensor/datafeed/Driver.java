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

import org.sensorhub.api.comm.ICommProvider;
import org.sensorhub.api.common.SensorHubException;
import org.sensorhub.impl.sensor.AbstractSensorModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

/**
 * Driver implementation for the sensor.
 * <p>
 * This class is responsible for providing sensor information, managing output registration,
 * and performing initialization and shutdown for the driver and its outputs.
 */
public class Driver extends AbstractSensorModule<Config> {
    static final String UID_PREFIX = "urn:osh:sensor:simulated:";
    static final String XML_PREFIX = "SIMULATED_DRIVER_";

    private static final Logger logger = LoggerFactory.getLogger(Driver.class);

    Output output;
    Thread processingThread;
    volatile boolean doProcessing = true;
    ICommProvider<?> commProvider;

    @Override
    public void doInit() throws SensorHubException {
        super.doInit();

        // Generate identifiers
        generateUniqueID(UID_PREFIX, config.serialNumber);
        generateXmlID(XML_PREFIX, config.serialNumber);

        // Create and initialize output
        output = new Output(this);
        addOutput(output, false);
        output.init();
    }

    @Override
    public void doStart() throws SensorHubException {
        super.doStart();

        // init comm provider
        if (commProvider == null)
        {
            // we need to recreate comm provider here because it can be changed by UI
            // TODO do that in updateConfig
            try
            {
                if (config.commSettings == null)
                    throw new SensorHubException("No communication settings specified");

                var moduleReg = getParentHub().getModuleRegistry();
                commProvider = (ICommProvider<?>)moduleReg.loadSubModule(config.commSettings, true);
                commProvider.start();
            }
            catch (Exception e)
            {
                commProvider = null;
                throw e;
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
            while (doProcessing) {

                if (commProvider != null && commProvider.isStarted()) {
                    try(BufferedReader reader = new BufferedReader(new InputStreamReader(commProvider.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            Object[] data = readData(line);
                            output.setData(System.currentTimeMillis(), data);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

            }
        });
        processingThread.start();
    }

    private Object[] readData(String line) {
        switch(config.dataFormat) {
            case CSV -> System.out.println(line);
            case XML -> System.out.println(line);
            case JSON -> System.out.println(line);
        }
        // TODO: Read data in correct order of data stream mapping
        return new Object[]{line};
    }

    /**
     * Signals the processing thread to stop.
     */
    public void stopProcessing() {
        doProcessing = false;
    }
}
