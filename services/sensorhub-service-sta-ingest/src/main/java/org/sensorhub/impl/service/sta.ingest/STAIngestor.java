package org.sensorhub.impl.service.sta.ingest;

import de.fraunhofer.iosb.ilt.sta.MqttException;
import de.fraunhofer.iosb.ilt.sta.ServiceFailureException;
import de.fraunhofer.iosb.ilt.sta.model.*;
import de.fraunhofer.iosb.ilt.sta.model.ext.EntityList;
import de.fraunhofer.iosb.ilt.sta.service.MqttConfig;
import de.fraunhofer.iosb.ilt.sta.service.SensorThingsService;
import net.opengis.OgcProperty;
import net.opengis.sensorml.v20.AbstractProcess;
import net.opengis.swe.v20.AbstractSWEIdentifiable;
import org.sensorhub.api.common.SensorHubException;
import org.sensorhub.api.database.IObsSystemDatabase;
import org.sensorhub.api.datastore.DataStoreException;
import org.sensorhub.api.system.ISystemWithDesc;
import org.sensorhub.impl.system.SystemDatabaseTransactionHandler;
import org.sensorhub.impl.system.SystemTransactionHandler;
import org.sensorhub.impl.system.SystemUtils;
import org.sensorhub.impl.system.wrapper.SystemWrapper;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class STAIngestor {

    private final SensorThingsService sts;
    private List<ISystemWithDesc> systems;
    private final STAUtils staUtils;
    SystemDatabaseTransactionHandler transactionHandler;
    boolean usingStateDb;
    boolean isMqttEnabled;

    class SensorData {
        AbstractProcess smlDescription;
        Map<String, Datastream> datastreams;

        public SensorData(AbstractProcess smlDescription) {
            this.smlDescription = smlDescription;
            this.datastreams = new HashMap<>();
        }
    }

    public STAIngestor(URL apiUrl, MqttConfig mqttConfig, boolean usingStateDb, SystemDatabaseTransactionHandler transactionHandler) throws MalformedURLException, MqttException {
        if(mqttConfig != null)
        {
            this.sts = new SensorThingsService(apiUrl, mqttConfig);
            this.isMqttEnabled = true;
        }
        else
        {
            this.sts = new SensorThingsService(apiUrl);
            this.isMqttEnabled = false;
        }
        this.staUtils = new STAUtils();
        this.transactionHandler = transactionHandler;
        this.usingStateDb = usingStateDb;
    }

    public void ingest()
    {
        try
        {
            // Get all things
            var things = sts.things().query().list().toList();
            registerThing(things.get(0));
//            for (Thing thing : things) {
//                registerThing(thing);
//            }

        } catch (ServiceFailureException e) {
            throw new RuntimeException(e);
        }
    }

    private void registerThing(Thing thing)
    {
        Thread thread = new Thread(() -> {
            System.out.println("Starting new discovery thread for " + thing.getName());
            if(thing.getName().equals("4")) {
                System.out.println(thing.getDescription() + "\n" + thing.getProperties().keySet().stream().collect(Collectors.toList()).get(0));
            }
            try {
                // TODO: Get datastreams for thing, get sensors for datastreams. Should be connected Thing -> Datastream <- Sensor

                var smlThing = staUtils.toSmlProcess(thing);
                var parentSystem = new SystemWrapper(smlThing);
                var handler = transactionHandler.addOrUpdateSystem(parentSystem);
                var datastreams = thing.datastreams().query().list();
                System.out.println("Parent: " + smlThing.getUniqueIdentifier());

                Map<Id<?>, SensorData> smlSensorMap = new HashMap<>();

                for (Datastream datastream : datastreams) {
                    var sensor = datastream.getSensor();

                    var sensorData = smlSensorMap.get(sensor.getId());
                    if(sensorData == null) {
                        sensorData = new SensorData(staUtils.toSmlProcess(sensor));
                        smlSensorMap.put(sensor.getId(), sensorData);
                        System.out.println("Child: " + sensorData.smlDescription.getUniqueIdentifier());
                    }
                    if(sensorData.smlDescription.getUniqueIdentifier().equals("infrarotdetektor_in_termicam2:5647"))
                        System.out.println("WTF?");
                    // Add datastream output to its sensor description
                    sensorData.smlDescription.addOutput(datastream.getName(), staUtils.toSweCommon(datastream));

                    // Keep track of the association between current datastream and sensor
                    sensorData.datastreams.put(datastream.getName(), datastream);
                }

                for(SensorData sensorData : smlSensorMap.values()) {
                    var smlSensor = sensorData.smlDescription;

                    // Add or update system handler
                    var system = new SystemWrapper(smlSensor);
                    var memberHandler = handler.addOrUpdateMember(system);

                    // Create datastreams if we have outputs
                    if (smlSensor.getNumOutputs() > 0)
                        SystemUtils.addDatastreamsFromOutputs(memberHandler, system.getFullDescription().getOutputList());

                    // Add historical observations and create MQTT subscription
                    for (OgcProperty<AbstractSWEIdentifiable> output : system.getFullDescription().getOutputList().getProperties()) {
                        var dsHandler = transactionHandler.getDataStreamHandler(memberHandler.getSystemUID(), output.getName());
                        var datastream = sensorData.datastreams.get(output.getName());

                        // Add historical observations
                        if(!this.usingStateDb) {
                            var observations = datastream.observations().query().list();
                            for (Observation observation : observations) {
                                var obs = staUtils.toObsData(observation, dsHandler.getDataStreamKey().getInternalID(), null);
                                dsHandler.addObs(obs);
                            }
                        }

                        // Subscribe if MQTT available
                        if (isMqttEnabled)
                            new DatastreamSubscriber(datastream, dsHandler, this.sts).doStart();
                    }
                }

            } catch (Exception e) {
                try {
                    throw new SensorHubException("Error registering thing " + thing.getName(), e);
                } catch (SensorHubException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        thread.start();
    }



}
