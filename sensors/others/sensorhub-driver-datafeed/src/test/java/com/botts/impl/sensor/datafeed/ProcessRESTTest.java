/***************************** BEGIN LICENSE BLOCK ***************************
 The contents of this file are subject to the Mozilla Public License, v. 2.0.
 If a copy of the MPL was not distributed with this file, You can obtain one
 at http://mozilla.org/MPL/2.0/.

 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 for the specific language governing rights and limitations under the License.

 Copyright (C) 2025 Botts Innovative Research, Inc. All Rights Reserved.
 ******************************* END LICENSE BLOCK ***************************/
package com.botts.impl.sensor.datafeed;

import com.google.gson.FormattingStyle;
import com.google.gson.stream.JsonWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sensorhub.api.ISensorHub;
import org.sensorhub.api.processing.IProcessProvider;
import org.sensorhub.api.processing.IProcessingManager;
import org.sensorhub.impl.SensorHub;
import org.vast.process.IProcessExec;
import org.vast.process.ProcessException;
import org.vast.sensorML.AbstractProcessImpl;
import org.vast.sensorML.SMLJsonBindings;
import org.vast.sensorML.SMLUtils;
import org.vast.sensorML.SimpleProcessImpl;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

/**
 * Base class for unit tests which initializes the sensor before each test and cleans up after.
 */
public class ProcessRESTTest {
    ISensorHub hub;
    IProcessingManager processingManager;
    Collection<IProcessProvider> processProviders;
    JsonWriter jsonWriter;
    SMLUtils smlUtils;
    SMLJsonBindings jsonBindings;

    @Before
    public void init() throws Exception {
        hub = new SensorHub();
        hub.start();
        processingManager = hub.getProcessingManager();
        processProviders = processingManager.getAllProcessingPackages();
        jsonWriter = new JsonWriter(new OutputStreamWriter(System.out));
        jsonWriter.setFormattingStyle(FormattingStyle.PRETTY);
        smlUtils = new SMLUtils(SMLUtils.V2_0);
        jsonBindings = new SMLJsonBindings();
    }

    @After
    public void cleanup() throws Exception {
        jsonWriter.close();

        if (null != hub) {
            hub.stop();
        }
    }

}
