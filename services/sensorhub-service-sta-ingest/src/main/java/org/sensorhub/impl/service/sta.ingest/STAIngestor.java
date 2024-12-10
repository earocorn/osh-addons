package org.sensorhub.impl.service.sta.ingest;

import de.fraunhofer.iosb.ilt.sta.ServiceFailureException;
import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.Observation;
import de.fraunhofer.iosb.ilt.sta.model.Sensor;
import de.fraunhofer.iosb.ilt.sta.model.Thing;
import de.fraunhofer.iosb.ilt.sta.model.ext.EntityList;
import de.fraunhofer.iosb.ilt.sta.service.SensorThingsService;
import net.opengis.swe.v20.DataRecord;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.database.IObsSystemDatabase;
import org.sensorhub.api.datastore.DataStoreException;
import org.sensorhub.api.system.ISystemWithDesc;
import org.sensorhub.api.system.SystemId;
import org.sensorhub.impl.system.wrapper.SystemWrapper;
import org.vast.swe.SWEHelper;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class STAIngestor {

    private final SensorThingsService sta;
    private List<ISystemWithDesc> systems;
    private STAUtils staUtils;

    public STAIngestor(URL apiUrl, IObsSystemDatabase writeDb) throws MalformedURLException
    {
        sta = new SensorThingsService(apiUrl);
        staUtils = new STAUtils();

        try
        {
//            EntityList<Thing> things = sta.things().query().list();
//            for (Thing thing : things) {
////                System.out.println("Thing " + thing.getName());
//
//                var datastreams = thing.datastreams().query().list();
//                for (Datastream datastream : datastreams) {
//                    System.out.println("Datastream " + datastream.getName() + " has observed property " + datastream.getObservedProperty().getName());
//                }
//            }

            EntityList<Sensor> sensors = sta.sensors().query().list();
            for (Sensor sensor : sensors) {
//                System.out.println("\nSensor " + sensor.getName());
//                System.out.println("id " + sensor.getId());
//                System.out.println("desc " + sensor.getDescription());

                var system = staUtils.toSystem(sensor);
                var sysId = writeDb.getSystemDescStore().add(system);

                var datastreams = sensor.datastreams().query().list();
                for (Datastream datastream : datastreams) {
                    var dsId = writeDb.getDataStreamStore().add(staUtils.toSweDataStream(new SystemId(sysId.getInternalID(), system.getUniqueIdentifier()), datastream));
                    var observations = datastream.observations().query().list();
                    for (Observation observation : observations) {
                        var foiId = writeDb.getFoiStore().add(staUtils.toGmlFeature(observation.getFeatureOfInterest(), system.getId()));
                        writeDb.getObservationStore().add(staUtils.toObsData(observation, dsId.getInternalID(), foiId.getInternalID()));
                    }
//                    System.out.println("Datastream " + datastream.getName());
                }
            }

        } catch (ServiceFailureException | DataStoreException e) {
            throw new RuntimeException(e);
        }

        // TODO: Create systems from Things and Sensors
        // TODO: Create and register the datastreams to the Things and Sensors
        // TODO: Put observations in db
    }

    public void ingest()
    {

    }


}
