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

import de.fraunhofer.iosb.ilt.sta.ServiceFailureException;
import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.EntityType;
import de.fraunhofer.iosb.ilt.sta.model.IdLong;
import de.fraunhofer.iosb.ilt.sta.model.Observation;
import de.fraunhofer.iosb.ilt.sta.query.ExpandedEntity;
import de.fraunhofer.iosb.ilt.sta.query.Expansion;
import de.fraunhofer.iosb.ilt.sta.query.InvalidRelationException;
import de.fraunhofer.iosb.ilt.sta.service.SensorThingsService;
import net.opengis.gml.v32.AbstractFeature;
import org.sensorhub.api.common.SensorHubException;
import org.sensorhub.api.module.ModuleEvent;
import org.sensorhub.impl.sensor.AbstractSensorModule;
import org.vast.util.Asserts;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class CIoTDriver extends AbstractSensorModule<CIoTDriverConfig> {

    SensorThingsService sensorThingsService;
    String staEndpointUrl;
    List<CIoTPoller> pollers;
    ScheduledExecutorService executor;

    @Override
    public void setConfiguration(CIoTDriverConfig config)
    {
        super.setConfiguration(config);

        if (config.sensorThingsEndpoint != null)
            staEndpointUrl = buildEndpointUrl();
    }


    private String buildEndpointUrl() {
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
        super.doInit();
        pollers = new ArrayList<>();

        Asserts.checkNotNull(config.serialNumber, "config.serialNumber");
        Asserts.checkNotNull(staEndpointUrl, "SensorThings endpoint URL");

        // Generate identifiers
        generateUniqueID("urn:osh:driver:civiliot:video:", config.serialNumber);
        generateXmlID("CIVIL_IOT_VIDEO", config.serialNumber);

        // TODO: Remove the following. This is just because IntelliJ certs r broken
        System.setProperty("javax.net.ssl.trustStore", "/home/earocorn/Desktop/truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "changeit");

        try {
            sensorThingsService = new SensorThingsService(new URL(staEndpointUrl));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        // If datastream IDs are specified, then use those from config, else just use a range of IDs from the config
        boolean useDatastreamRange = true;
        int start = config.idStart;
        int end = config.idEnd;
        if (!(config.datastreamIds == null || config.datastreamIds.isEmpty())) {
            useDatastreamRange = false;
            start = 0;
            end = config.datastreamIds.size();
        }

        for (int i = start; i <= end; i++) {
            int id = i;
            long pollInterval = config.pollInterval;
            if (!useDatastreamRange) {
                var datastreamPollConfig = config.datastreamIds.get(i);
                id = datastreamPollConfig.datastreamId;
                pollInterval = datastreamPollConfig.pollInterval;
            }

            try {
                Datastream datastream = sensorThingsService.datastreams().find(new IdLong((long)id), Expansion.of(EntityType.DATASTREAMS)
                        .with(ExpandedEntity.from(EntityType.OBSERVATIONS))
                        .with(ExpandedEntity.from(EntityType.THING, EntityType.LOCATIONS)));
                Observation latestObs = datastream.getObservations().fullIterator().next();

                AbstractFeature feature = null;
                var obsArea = datastream.getObservedArea();
                if (obsArea != null) {
                    feature = STAUtils.toSamplingFeature(obsArea);
                    // Specify other information, because the above method only converts GeoJson geometry to feature geometry
                    feature.setName("Feature " + id);
                    feature.setUniqueIdentifier(STAUtils.FEATURE_UID_PREFIX + id);
                    feature.setDescription("Observed area for datastream: " + datastream.getDescription());
                } else {
                    var location = datastream.getThing().getLocations().fullIterator().next();
                    if (location != null)
                        feature = STAUtils.toGmlFeature(location, ""+id);
                }
                addFoi(feature);

                // Get dimensions if possible
                URL url = new URL(latestObs.getResult().toString().trim());
                int[] dimensions = ImageURLUtils.getDimensions(url);
                // Create, initialize, and connect output for video stream
                VideoOutput<CIoTDriver> output = new VideoOutput<>(
                        this,
                        dimensions,
                        "video" + id,
                        "Video " + datastream.getName() + " " + id,
                        datastream.getDescription(),
                        pollInterval*60);
                output.init();
                addOutput(output, false);

                // Create poller to handle output publishing
                CIoTPoller poller = new CIoTPoller(url, pollInterval, output);
                // If we have a feature, then attach it to the poller
                if (feature != null)
                    poller.setFoi(feature);
                pollers.add(poller);
            } catch (ServiceFailureException | InvalidRelationException | IOException e) {
                getLogger().error("Unable to retrieve data from Datastream {}: {}", id, e.getMessage());
            }
        }

        if (getOutputs().isEmpty())
            throw new CompletionException("Requested data is not available from SensorThings API " + staEndpointUrl + ". Please check that Datastream IDs are valid", null);

        if (!pollers.isEmpty() && executor == null) {
            executor = Executors.newScheduledThreadPool(pollers.size());
            getLogger().debug("Created scheduled thread pool executor of size {}", pollers.size());
            for (var poller : pollers)
                poller.setExecutor(executor);
        }
    }

    @Override
    public void doStart() throws SensorHubException {
        super.doStart();
        for (var poller : pollers)
            poller.start();
    }

    @Override
    public void cleanup() throws SensorHubException {
        super.cleanup();
        for (var poller : pollers) {
            poller.stop();
            poller.cleanup();
        }
    }

    @Override
    public void doStop() throws SensorHubException {
        super.doStop();
        for (var poller : pollers)
            poller.stop();
    }

    @Override
    public boolean isConnected() {
        return executor != null && !pollers.isEmpty();
    }
}
