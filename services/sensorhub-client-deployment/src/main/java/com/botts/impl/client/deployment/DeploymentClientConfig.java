/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2012-2016 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package com.botts.impl.client.deployment;

import org.sensorhub.api.client.ClientConfig;
import org.sensorhub.api.comm.CommProviderConfig;
import org.sensorhub.api.config.DisplayInfo;
import org.sensorhub.api.service.ServiceConfig;
import org.sensorhub.impl.comm.HTTPConfig;
import org.sensorhub.impl.datastore.view.ObsSystemDatabaseViewConfig;

public class DeploymentClientConfig extends ClientConfig
{

    @DisplayInfo(desc="Filtered view to select systems/datastreams to register with Connected Systems")
    @DisplayInfo.Required
    public ObsSystemDatabaseViewConfig dataSourceSelector;


    @DisplayInfo(label="Connected Systems Endpoint", desc="Connected Systems endpoint where the requests are sent")
    public HTTPConfig conSys = new HTTPConfig();

    @DisplayInfo(label = "Deployment Settings", desc = "Common settings for this deployment")
    public DeploymentConfig deployment;

    public DeploymentClientConfig()
    {
        this.moduleClass = DeploymentClient.class.getCanonicalName();
        this.conSys.resourcePath = "/sensorhub/api";
    }
}
