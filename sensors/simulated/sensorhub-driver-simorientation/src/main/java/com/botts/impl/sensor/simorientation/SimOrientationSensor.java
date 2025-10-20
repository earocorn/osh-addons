/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
The Initial Developer is Botts Innovative Research Inc. Portions created by the Initial
Developer are Copyright (C) 2014 the Initial Developer. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package com.botts.impl.sensor.simorientation;

import org.sensorhub.api.common.SensorHubException;
import org.sensorhub.impl.sensor.AbstractSensorModule;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * <p>
 * Driver implementation outputting simulated weather data by randomly
 * increasing or decreasing temperature, pressure, wind speed, and
 * wind direction.  Serves as a simple sensor to deploy as well as
 * a simple example of a sensor driver.
 * </p>
 *
 * @author Mike Botts
 * @since Dec 24, 2014
 */
public class SimOrientationSensor extends AbstractSensorModule<SimOrientationConfig>
{
    SimOrientationOutput dataInterface;
    ScheduledExecutorService service;
    
    @Override
    protected void doInit() throws SensorHubException
    {
        super.doInit();
        
        // generate identifiers
        generateUniqueID("urn:osh:sensor:simorientation:", config.serialNumber);
        generateXmlID("SIMULATED_ORIENTATION_SENSOR_", config.serialNumber);
        
        // init main data interface
        dataInterface = new SimOrientationOutput(this);
        addOutput(dataInterface, false);
        dataInterface.init();

        service = Executors.newSingleThreadScheduledExecutor();
    }


    @Override
    protected void updateSensorDescription()
    {
        synchronized (sensorDescLock)
        {
            super.updateSensorDescription();
            
            if (!sensorDescription.isSetDescription())
                sensorDescription.setDescription("Simulated orientation sensor generating random measurements");
        }
    }


    @Override
    protected void doStart() throws SensorHubException
    {
        if (dataInterface != null)
            dataInterface.start();

        service.scheduleAtFixedRate(() ->
                        locationOutput.updateLocation(
                                System.currentTimeMillis()/1000d,
                                config.location.lat,
                                config.location.lon,
                                config.location.alt,
                                true)
        , 1, 1, TimeUnit.SECONDS);
    }
    

    @Override
    protected void doStop() throws SensorHubException
    {
        if (dataInterface != null)
            dataInterface.stop();
    }
    

    @Override
    public void cleanup() throws SensorHubException
    {
       
    }
    
    
    @Override
    public boolean isConnected()
    {
        return true;
    }
}

