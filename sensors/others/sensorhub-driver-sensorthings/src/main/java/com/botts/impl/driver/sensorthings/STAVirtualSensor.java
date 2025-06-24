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

import com.botts.impl.driver.sensorthings.client.STASubscriber;
import com.botts.impl.driver.sensorthings.client.STAUtils;
import de.fraunhofer.iosb.ilt.sta.MqttException;
import de.fraunhofer.iosb.ilt.sta.ServiceFailureException;
import de.fraunhofer.iosb.ilt.sta.model.EntityType;
import de.fraunhofer.iosb.ilt.sta.query.ExpandedEntity;
import de.fraunhofer.iosb.ilt.sta.service.MqttConfig;
import de.fraunhofer.iosb.ilt.sta.service.SensorThingsService;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.sensorhub.api.common.SensorHubException;
import org.sensorhub.api.module.ModuleEvent;
import org.sensorhub.impl.comm.HTTPConfig;
import org.sensorhub.impl.sensor.AbstractSensorModule;
import org.vast.data.TextEncodingImpl;
import org.vast.util.Asserts;

import javax.net.ssl.SSLContext;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class STAVirtualSensor extends AbstractSensorModule<STAVirtualSensorConfig> {

    SensorThingsService sensorThingsService;
    String staEndpointUrl;
    protected boolean isMqttEnabled = false;

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

        String endpointUrl = scheme + "://" + endpoint.remoteHost;
        if (endpoint.remotePort != 80 && endpoint.remotePort != 443)
            endpointUrl += ":" + endpoint.remotePort;
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
        Asserts.checkNotNull(config.serialNumber, "config.serialNumber");
        Asserts.checkNotNull(staEndpointUrl, "SensorThings endpoint URL");

        // Only initialize after Datastreams are queried and outputs are registered
        initAsync = true;

        // Generate identifiers
        generateUniqueID("urn:osh:driver:sensorthings:", config.serialNumber);
        generateXmlID("SENSOR_THINGS", config.serialNumber);

        System.setProperty("javax.net.ssl.trustStore", "/Users/alexalmanza/Desktop/truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "changeit");

        CompletableFuture.runAsync(() -> {
            try {
                if (config.mqttConfig != null) {
                    MqttConfig mqttConfig = new MqttConfig(config.mqttConfig.broker);
                    if (config.mqttConfig.password != null) {
                        MqttConnectOptions options = new MqttConnectOptions();
                        options.setUserName(config.mqttConfig.username);
                        options.setPassword(config.mqttConfig.password.toCharArray());
                        mqttConfig.setOptions(options);
                        mqttConfig.setClientId(getUniqueIdentifier());
                    }

                    sensorThingsService = new SensorThingsService(new URL(staEndpointUrl), mqttConfig);
                    isMqttEnabled = true;
                } else {
                    sensorThingsService = new SensorThingsService(new URL(staEndpointUrl));
                    isMqttEnabled = false;
                }

                for (var configProp : config.observedProperties) {
                    var obsProps = sensorThingsService
                            .observedProperties()
                            .query()
                            .filter("definition eq '" + configProp + "'")
                            .expand(EntityType.DATASTREAMS.getName())
                            .list();

                    var obsPropIterator = obsProps.fullIterator();
                    int currentNumDatastreams = 0;
                    while (obsPropIterator.hasNext()) {
                        var obsProp = obsPropIterator.next();
                        if (!config.observedProperties.contains(obsProp.getDefinition()))
                            continue;

                        var relatedDatastreams = obsProp.getDatastreams();
                        var relatedDatastreamsIterator = relatedDatastreams.fullIterator();
                        while (relatedDatastreamsIterator.hasNext()) {
                            if (currentNumDatastreams++ > config.datastreamLimit)
                                break;

                            var datastream = relatedDatastreamsIterator.next();
                            var recordStructure = STAUtils.toSweCommon(datastream);
                            // Use ID to uniquely identify Datastreams as OSH outputs
                            recordStructure.setName(recordStructure.getName() + datastream.getId());
                            final STAVirtualSensorOutput output = new STAVirtualSensorOutput(this,
                                    recordStructure,
                                    new STASubscriber(datastream,
                                            config.httpPollRate, isMqttEnabled));
                            addOutput(output, false);
                        }
                    }
                }

                if (getOutputs().isEmpty())
                    throw new CompletionException("Requested data is not available from SensorThings API " + staEndpointUrl + ". Please check observed properties are valid.", null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        })
        .thenRun(() -> setState(ModuleEvent.ModuleState.INITIALIZED))
        .exceptionally(e -> {
            reportError(e.getMessage(), e.getCause());
            return null;
        });
    }

    @Override
    protected void setState(ModuleEvent.ModuleState newState) {
        super.setState(newState);
        if (config.autoStart && ModuleEvent.ModuleState.INITIALIZED.equals(getCurrentState())) {
            try {
                start();
            } catch (SensorHubException e) {
                throw new RuntimeException(e);
            }
        }
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
