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

import java.util.concurrent.CompletableFuture;
import net.opengis.swe.v20.DataComponent;
import org.sensorhub.api.command.CommandException;
import org.sensorhub.api.command.CommandStatus;
import org.sensorhub.api.command.ICommandData;
import org.sensorhub.api.command.ICommandStatus;
import org.sensorhub.impl.sensor.AbstractSensorControl;
import org.vast.swe.SWEConstants;
import org.vast.swe.helper.GeoPosHelper;

public class SimOrientationControl extends AbstractSensorControl<SimOrientationSensor> {

    public static final String NAME = "orientationControl";
    private static final double MIN_AZIMUTH = 0.0;
    private static final double MAX_AZIMUTH = 360.0;

    private final DataComponent commandData;

    public SimOrientationControl(SimOrientationSensor parentSensor) {
        super(NAME, parentSensor);

        var fac = new GeoPosHelper();
        commandData = fac.createQuantity()
                .name(NAME)
                .definition(GeoPosHelper.DEF_AZIMUTH_ANGLE)
                .refFrame(SWEConstants.REF_FRAME_NED)
                .axisId("z")
                .label("Fixed Azimuth")
                .description("Fixed true-north azimuth emitted by the simulated orientation output")
                .uomCode("deg")
                .addAllowedInterval(MIN_AZIMUTH, MAX_AZIMUTH)
                .build();
    }

    @Override
    public DataComponent getCommandDescription() {
        return commandData;
    }

    @Override
    public CompletableFuture<ICommandStatus> submitCommand(ICommandData command) {
        try {
            validateCommand(command);
            double azimuth = command.getParams().getDoubleValue();
            parentSensor.orientationOutput.setAzimuth(azimuth);
            getLogger().debug("Set manual orientation to {} degrees", azimuth);
            return CompletableFuture.completedFuture(CommandStatus.completed(command.getID()));
        } catch (CommandException e) {
            return CompletableFuture.completedFuture(CommandStatus.rejected(command.getID(), e.getMessage()));
        }
    }

    @Override
    public void validateCommand(ICommandData command) throws CommandException {
        if (!parentSensor.getConfiguration().manualControl)
            throw new CommandException("Manual orientation control is disabled");

        double azimuth = command.getParams().getDoubleValue();
        if (!Double.isFinite(azimuth) || azimuth < MIN_AZIMUTH || azimuth > MAX_AZIMUTH)
            throw new CommandException("Azimuth must be between 0 and 360 degrees");
    }
}
