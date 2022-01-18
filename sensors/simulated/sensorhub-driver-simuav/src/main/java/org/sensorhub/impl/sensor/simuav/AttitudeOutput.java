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

package org.sensorhub.impl.sensor.simuav;

import org.sensorhub.impl.sensor.simuav.AttitudeOutput;
import java.util.concurrent.TimeUnit;
import org.sensorhub.api.data.DataEvent;
import net.opengis.swe.v20.DataBlock;
import net.opengis.swe.v20.DataComponent;
import net.opengis.swe.v20.DataEncoding;
import org.vast.swe.helper.GeoPosHelper;


public class AttitudeOutput extends UavOutput<SimUavDriver>
{
    DataComponent dataStruct;
    DataEncoding dataEncoding;

    
    public AttitudeOutput(SimUavDriver parentSensor)
    {
        super("platform_att", parentSensor);
        
        // create output data structure
        GeoPosHelper fac = new GeoPosHelper();
        dataStruct = fac.createRecord()
            .name(getName())
            .label("Platform Attitude")
            .addField("time", fac.createTime()
                .asSamplingTimeIsoUTC())
            .addField("hpr", fac.createEulerOrientationNED("deg"))
            .build();
     
        // also generate encoding definition
        dataEncoding = fac.newTextEncoding(",", "\n");
    }

    
    public void start()
    {
        stop(); // always stop previous runnable
        
        future = parentSensor.scheduler.scheduleAtFixedRate(() -> {
            
            var now = System.currentTimeMillis();
            var currentState = parentSensor.currentState;
            
            // build and publish datablock
            DataBlock dataBlock = dataStruct.createDataBlock();
            dataBlock.setDoubleValue(0, now/1000.);
            dataBlock.setDoubleValue(1, currentState.heading);
            dataBlock.setDoubleValue(2, currentState.pitch);
            dataBlock.setDoubleValue(3, currentState.roll);
            
            // update latest record and send event
            latestRecord = dataBlock;
            latestRecordTime = now;
            eventHandler.publish(new DataEvent(latestRecordTime, AttitudeOutput.this, dataBlock));
            
        }, 0, 100, TimeUnit.MILLISECONDS);
    }


    @Override
    public double getAverageSamplingPeriod()
    {
        return 0.1;
    }


    @Override
    public DataComponent getRecordDescription()
    {
        return dataStruct;
    }


    @Override
    public DataEncoding getRecommendedEncoding()
    {
        return dataEncoding;
    }
}
