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

import org.sensorhub.api.config.DisplayInfo;
import org.sensorhub.api.sensor.SensorConfig;
import org.sensorhub.impl.comm.HTTPConfig;

import java.util.ArrayList;
import java.util.List;

public class CIoTDriverConfig extends SensorConfig {

    @DisplayInfo.Required
    @DisplayInfo(desc = "Serial number or unique identifier")
    public String serialNumber = "001";

    @DisplayInfo.Required
    @DisplayInfo(label = "SensorThings Endpoint" , desc = "SensorThings API endpoint to connect")
    public HTTPConfig sensorThingsEndpoint;

    @DisplayInfo(label = "Datastream ID Start")
    public int idStart = 59;
    @DisplayInfo(label = "Datastream ID End")
    public int idEnd = 130;
    @DisplayInfo(label = "General Poll Interval (min)", desc = "Poll interval to used if Datastream IDs & poll rates are not specified")
    public int pollInterval = 2;

    @DisplayInfo(label = "Datastream IDs", desc = "SensorThings Datastream IDs to stream video")
    public List<DatastreamPollConfig> datastreamIds = new ArrayList<>();

    public static class DatastreamPollConfig {
        @DisplayInfo(label = "Poll Interval (min)", desc = "Interval (in minutes) at which frames are polled from the image repository")
        public int pollInterval = 10;

        @DisplayInfo.Required
        @DisplayInfo(label = "Datastream ID", desc = "SensorThings Datastream ID from which this driver receives video")
        public int datastreamId;
    }

    public CIoTDriverConfig() {
        this.sensorThingsEndpoint = new HTTPConfig();
        this.sensorThingsEndpoint.remoteHost = "sta.ci.taiwan.gov.tw";
        this.sensorThingsEndpoint.remotePort = 443;
        this.sensorThingsEndpoint.enableTLS = true;
        this.sensorThingsEndpoint.resourcePath = "/STA_CCTV/v1.0/";
    }

}
