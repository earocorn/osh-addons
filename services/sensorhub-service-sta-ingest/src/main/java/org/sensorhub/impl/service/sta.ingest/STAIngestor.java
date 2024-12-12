package org.sensorhub.impl.service.sta.ingest;

import de.fraunhofer.iosb.ilt.sta.MqttException;
import de.fraunhofer.iosb.ilt.sta.ServiceFailureException;
import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.Observation;
import de.fraunhofer.iosb.ilt.sta.model.Sensor;
import de.fraunhofer.iosb.ilt.sta.model.Thing;
import de.fraunhofer.iosb.ilt.sta.service.MqttConfig;
import de.fraunhofer.iosb.ilt.sta.service.SensorThingsService;
import net.opengis.OgcProperty;
import net.opengis.swe.v20.AbstractSWEIdentifiable;
import org.sensorhub.api.common.SensorHubException;
import org.sensorhub.api.database.IObsSystemDatabase;
import org.sensorhub.api.datastore.DataStoreException;
import org.sensorhub.api.system.ISystemWithDesc;
import org.sensorhub.impl.system.SystemDatabaseTransactionHandler;
import org.sensorhub.impl.system.SystemUtils;
import org.sensorhub.impl.system.wrapper.SystemWrapper;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class STAIngestor {

    private final SensorThingsService sts;
    private List<ISystemWithDesc> systems;
    private final STAUtils staUtils;
    SystemDatabaseTransactionHandler transactionHandler;
    boolean usingStateDb;

    public STAIngestor(URL apiUrl, MqttConfig mqttConfig, boolean usingStateDb, SystemDatabaseTransactionHandler transactionHandler) throws MalformedURLException, MqttException {
        this.sts = new SensorThingsService(apiUrl, mqttConfig);
        this.staUtils = new STAUtils();
        this.transactionHandler = transactionHandler;
        this.usingStateDb = usingStateDb;
    }

    public void ingest()
    {
        try
        {
            // Get all things
            var things = sts.things().query().list();
            for (Thing thing : things)
                registerThing(thing);

        } catch (ServiceFailureException e) {
            throw new RuntimeException(e);
        }
    }

    private void registerThing(Thing thing)
    {
        Thread thread = new Thread(() -> {
            System.out.println("Starting new discovery thread for " + thing.getName());
            try {
                var smlThing = staUtils.toSmlProcess(thing);
                var parentSystem = new SystemWrapper(smlThing);

                var handler = transactionHandler.addOrUpdateSystem(parentSystem);

                var thingDs = thing.datastreams().query().list();

                Map<String, Sensor> thingSensors = new HashMap<>();
                Map<String, Map<String, Datastream>> sensorDatastreams = new HashMap<>();

                for (Datastream datastream : thingDs) {
                    thingSensors.put(datastream.getSensor().getName(), datastream.getSensor());

                    if (sensorDatastreams.containsKey(datastream.getSensor().getName()))
                        sensorDatastreams.get(datastream.getSensor().getName()).put(datastream.getName(), datastream);
                    else
                        sensorDatastreams.put(datastream.getSensor().getName(), new HashMap<>());
                }

                for (Sensor sensor : thingSensors.values()) {
                    var smlSensor = staUtils.toSmlProcess(sensor);
                    var sensorDs = sensorDatastreams.get(sensor.getName());
                    for (Datastream datastream : sensorDs.values())
                        smlSensor.addOutput(datastream.getName(), staUtils.toSweCommon(datastream));


                    var system = new SystemWrapper(smlSensor);
                    var memberHandler = handler.addOrUpdateMember(system);

                    if (smlSensor.getNumOutputs() > 0)
                        SystemUtils.addDatastreamsFromOutputs(memberHandler, system.getFullDescription().getOutputList());

                    for (OgcProperty<AbstractSWEIdentifiable> output : system.getFullDescription().getOutputList().getProperties()) {
                        var dsHandler = transactionHandler.getDataStreamHandler(memberHandler.getSystemUID(), output.getName());
                        var datastream = sensorDs.get(output.getName());

                        // Add historical observations
                        var observations = datastream.observations().query().list();
                        for (Observation observation : observations)
                            dsHandler.addObs(staUtils.toObsData(observation, dsHandler.getDataStreamKey().getInternalID(), null));

                        DatastreamSubscriber subscriber = new DatastreamSubscriber(datastream, dsHandler, this.sts);
                        subscriber.doStart();
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
