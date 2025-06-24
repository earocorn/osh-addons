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

package com.botts.impl.driver.sensorthings;

import org.sensorhub.api.config.DisplayInfo;
import org.sensorhub.api.sensor.SensorConfig;
import org.sensorhub.impl.comm.HTTPConfig;

import java.util.ArrayList;
import java.util.List;

public class STAVirtualSensorConfig extends SensorConfig {

    @DisplayInfo.Required
    @DisplayInfo(desc = "Serial number or unique identifier")
    public String serialNumber = "001";

    @DisplayInfo(label = "HTTP Poll Rate (ms)", desc = "Rate (in milliseconds) to poll observations from service")
    public int httpPollRate = 1000;

    @DisplayInfo.Required
    @DisplayInfo(label = "SensorThings Endpoint" , desc = "SensorThings API endpoint to connect")
    public HTTPConfig sensorThingsEndpoint;

    @DisplayInfo(label = "Datastream Limit", desc = "Limit of how many Datastreams to subscribe")
    public int datastreamLimit = 20;

    @DisplayInfo(label="Observed Properties", desc="List of ObservedProperty values to make available as outputs")
    public List<String> observedProperties = new ArrayList<>();

    @DisplayInfo(label = "MQTT Config (optional)", desc = "MQTT config for streaming data. If MQTT is not used, the driver will default to HTTP polling.")
    public StreamConfig mqttConfig;
}
