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

import de.fraunhofer.iosb.ilt.sta.MqttException;
import de.fraunhofer.iosb.ilt.sta.service.MqttConfig;
import de.fraunhofer.iosb.ilt.sta.service.SensorThingsService;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.sensorhub.api.common.SensorHubException;
import org.sensorhub.impl.comm.HTTPConfig;
import org.sensorhub.impl.sensor.AbstractSensorModule;
import org.vast.util.Asserts;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class STAVirtualSensor extends AbstractSensorModule<STAVirtualSensorConfig> {

    SensorThingsService sensorThingsService;
    String staEndpointUrl;

    @Override
    public void setConfiguration(STAVirtualSensorConfig config)
    {
        super.setConfiguration(config);

        if (config.sensorThingsEndpoint != null)
            staEndpointUrl = buildEndpointUrl();
    }


    private String buildEndpointUrl()
    {
        var endpoint = config.sensorThingsEndpoint;
        String scheme = "http";
        if (endpoint.enableTLS)
            scheme = "https";

        String endpointUrl = scheme + "://" + endpoint.remoteHost + ":" + endpoint.remotePort;
        if (endpoint.resourcePath != null)
        {
            if (endpoint.resourcePath.charAt(0) != '/')
                endpointUrl += '/';
            endpointUrl += endpoint.resourcePath;
        }

        return endpointUrl;
    }

    @Override
    public void doInit() throws SensorHubException {
        super.doInit();

        Asserts.checkNotNull(config.serialNumber, "config.serialNumber");
        Asserts.checkNotNull(staEndpointUrl, "SensorThings endpoint URL");

        // Generate identifiers
        generateUniqueID("urn:osh:driver:sensorthings:", config.serialNumber);
        generateXmlID("SENSOR_THINGS", config.serialNumber);

        CompletableFuture.runAsync(() -> {
            if (config.mqttConfig != null) {
                MqttConfig mqttConfig = new MqttConfig(config.mqttConfig.broker);

                if (config.mqttConfig.password != null) {
                    MqttConnectOptions options = new MqttConnectOptions();
                    options.setUserName(config.mqttConfig.username);
                    options.setPassword(config.mqttConfig.password.toCharArray());
                    mqttConfig.setOptions(options);
                    mqttConfig.setClientId(getUniqueIdentifier());
                }

                try {
                    sensorThingsService = new SensorThingsService(new URL(staEndpointUrl), mqttConfig);
                } catch (MalformedURLException | MqttException e) {
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    sensorThingsService = new SensorThingsService(new URL(staEndpointUrl));
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }

            // TODO: Collect datastreams and create outputs
        });
    }

    @Override
    public void doStart() throws SensorHubException {
        for (var output : getOutputs().values())
            ((STAVirtualSensorOutput) output).start();
    }

    @Override
    public void doStop() throws SensorHubException {
        for (var output : getOutputs().values())
            try {
                ((STAVirtualSensorOutput) output).stop();
            } catch (MqttException e) {
                getLogger().error("Error stopping STAVirtualSensorOutput", e);
            }
    }

    // Use MQTT client connection status as connection status of this driver
    @Override
    public boolean isConnected() {
        return false;
    }
}
