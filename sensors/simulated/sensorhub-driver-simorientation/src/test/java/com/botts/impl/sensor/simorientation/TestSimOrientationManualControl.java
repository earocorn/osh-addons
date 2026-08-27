/***************************** BEGIN LICENSE BLOCK ***************************

 The contents of this file are subject to the Mozilla Public License, v. 2.0.
 If a copy of the MPL was not distributed with this file, You can obtain one
 at http://mozilla.org/MPL/2.0/.

 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 for the specific language governing rights and limitations under the License.

 Copyright (C) 2026 Botts Innovative Research, Inc. All Rights Reserved.

 ******************************* END LICENSE BLOCK ***************************/

package com.botts.impl.sensor.simorientation;

import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sensorhub.api.command.CommandData;
import org.sensorhub.api.command.ICommandStatus.CommandStatusCode;

import static org.junit.Assert.*;

public class TestSimOrientationManualControl {

    private SimOrientationSensor sensor;

    @Before
    public void init() throws Exception {
        var config = new SimOrientationConfig();
        config.id = UUID.randomUUID().toString();
        config.manualControl = true;

        sensor = new SimOrientationSensor();
        sensor.init(config);
    }

    @After
    public void cleanup() throws Exception {
        if (sensor != null)
            sensor.cleanup();
    }

    @Test
    public void testManualControlPublishesFixedOrientation() throws Exception {
        var control = sensor.getCommandInputs().get(SimOrientationControl.NAME);
        assertNotNull(control);

        var commandData = control.getCommandDescription().createDataBlock();
        commandData.setDoubleValue(123.45);
        var status = control.submitCommand(new CommandData(1, commandData)).get();
        assertEquals(CommandStatusCode.COMPLETED, status.getStatusCode());

        sensor.orientationOutput.sendMeasurement();
        assertEquals(123.45, sensor.orientationOutput.getLatestRecord().getDoubleValue(1), 1e-9);
        sensor.orientationOutput.sendMeasurement();
        assertEquals(123.45, sensor.orientationOutput.getLatestRecord().getDoubleValue(1), 1e-9);

        commandData = control.getCommandDescription().createDataBlock();
        commandData.setDoubleValue(360.0);
        status = control.submitCommand(new CommandData(2, commandData)).get();
        assertEquals(CommandStatusCode.COMPLETED, status.getStatusCode());
        sensor.orientationOutput.sendMeasurement();
        assertEquals(360.0, sensor.orientationOutput.getLatestRecord().getDoubleValue(1), 1e-9);
    }

    @Test
    public void testOutOfRangeOrientationIsRejected() throws Exception {
        var control = sensor.getCommandInputs().get(SimOrientationControl.NAME);
        var commandData = control.getCommandDescription().createDataBlock();
        commandData.setDoubleValue(361.0);

        var status = control.submitCommand(new CommandData(1, commandData)).get();

        assertEquals(CommandStatusCode.REJECTED, status.getStatusCode());
        assertEquals(0.0, sensor.orientationOutput.getAzimuth(), 1e-9);
    }

    @Test
    public void testControlStreamIsAbsentWhenManualControlIsDisabled() throws Exception {
        var autoConfig = new SimOrientationConfig();
        autoConfig.id = UUID.randomUUID().toString();
        autoConfig.manualControl = false;
        var autoSensor = new SimOrientationSensor();
        try {
            autoSensor.init(autoConfig);
            assertFalse(autoSensor.getCommandInputs().containsKey(SimOrientationControl.NAME));
        } finally {
            autoSensor.cleanup();
        }
    }
}
