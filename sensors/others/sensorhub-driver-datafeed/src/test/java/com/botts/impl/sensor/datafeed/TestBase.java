/***************************** BEGIN LICENSE BLOCK ***************************
 The contents of this file are subject to the Mozilla Public License, v. 2.0.
 If a copy of the MPL was not distributed with this file, You can obtain one
 at http://mozilla.org/MPL/2.0/.

 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 for the specific language governing rights and limitations under the License.

 Copyright (C) 2025 Botts Innovative Research, Inc. All Rights Reserved.
 ******************************* END LICENSE BLOCK ***************************/
package com.botts.impl.sensor.datafeed;

import net.opengis.sensorml.v20.AbstractProcess;
import net.opengis.sensorml.v20.AggregateProcess;
import net.opengis.sensorml.v20.PhysicalSystem;
import net.opengis.sensorml.v20.SimpleProcess;
import net.opengis.swe.v20.DataRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sensorhub.api.ISensorHub;
import org.sensorhub.api.datastore.obs.DataStreamFilter;
import org.sensorhub.api.datastore.obs.ObsFilter;
import org.sensorhub.api.datastore.system.SystemFilter;
import org.sensorhub.impl.SensorHub;
import org.sensorhub.impl.processing.CommandStreamSink;
import org.sensorhub.impl.processing.StreamDataSource;
import org.sensorhub.impl.system.wrapper.SystemWrapper;
import org.vast.sensorML.LinkImpl;
import org.vast.sensorML.PhysicalSystemImpl;
import org.vast.sensorML.SMLHelper;

import java.time.Instant;

/**
 * Base class for unit tests which initializes the sensor before each test and cleans up after.
 */
public class TestBase {
    Driver driver;
    Output output;

    @Before
    public void init() throws Exception {
        Config config = new Config();
        config.serialNumber = "123456789";
        config.name = "Sensor Template";
        config.description = "Description of the sensor";
        driver = new Driver();
        driver.init(config);
        driver.start();
        output = driver.output;
    }

    @After
    public void cleanup() throws Exception {
        if (null != driver) {
            driver.stop();
        }
    }

    @Test
    public void test() {
        PhysicalSystem sensorDescription = new PhysicalSystemImpl();
        SMLHelper helper = new SMLHelper();
        helper.edit(sensorDescription)
                .description("Description of the sensor")
                .name("Test Sensor")
                .addIdentifier(helper.identifiers.serialNumber("123456789"))
                .addCharacteristicList("operating_specs", helper.characteristics.operatingCharacteristics()
                        .add("voltage", helper.characteristics.operatingVoltageRange(100.0, 250.0, "V"))
                        .add("temperature", helper.conditions.temperatureRange(-20.0, 90.0, "Cel")))
                .build();

        PhysicalSystem newSystem = helper.createPhysicalSystem()
                .addIdentifier(helper.identifiers.author("John Doe"))
                .build();
        SystemWrapper systemWrapper = new SystemWrapper(newSystem);

        DataRecord record = helper.createRecord()
                .addField("time", helper.createTime()
                        .asSamplingTimeIsoUTC()
                        .build())
                .addField("textField", helper.createText()
                        .label("Text Field")
                        .definition("https://ontologyrepo.org/TextField1")
                        .description("Description for this text field")
                        .build())
                .addField("speed", helper.createQuantity()
                        .definition("https://ontologyrepo.org/Speed")
                        .uom("m/s")
                        .addAllowedInterval(0.0, 30.0)
                        .build())
                .build();


        SimpleProcess process = helper.createSimpleProcess()
                .uniqueID("urn:osh:process:simple1")
                .name("My Simple Process")
                .description("A description of my simple process")
                .addInput("inputData", record)
                .addOutput("outputData", record)
                .addParameter("paramData", record)
                .build();
        // Adding a data source based on OSH system UID
        StreamDataSource dataSource = new StreamDataSource();
        dataSource.getParameterList()
                .getComponent(StreamDataSource.PRODUCER_URI_PARAM)
                .getData()
                .setStringValue("urn:osh:sensor:simulated:001");

        // Adding a command sink based on OSH system UID
        CommandStreamSink commandSink = new CommandStreamSink();
        commandSink.getParameterList()
                .getComponent(CommandStreamSink.SYSTEM_UID_PARAM)
                .getData()
                .setStringValue("urn:osh:sensor:robot:001");
        commandSink.getParameterList()
                .getComponent(CommandStreamSink.OUTPUT_NAME_PARAM)
                .getData()
                .setStringValue("moveForward");

        // Building process chainn (AggregateProcess)
        AggregateProcess processChain = helper.createAggregateProcess()
                .uniqueID("urn:osh:process:chain1")
                .name("My Process Chain")
                .description("A description of my process chain")
                .addOutput("outputData", record)
                .addComponent("simpleProcess", process)
                .addComponent("dataSource", (AbstractProcess) dataSource)
                .addComponent("commandSink", (AbstractProcess) commandSink)
                .build();
        // Add connections
        processChain.addConnection(new LinkImpl("dataSource/outputs/1", "simpleProcess/inputs/inputData"));
        processChain.addConnection(new LinkImpl("simpleProcess/outputs/outputData", "commandSink/inputs/1"));
        processChain.addConnection(new LinkImpl("simpleProcess/outputs/outputData", "outputs/outputData"));

        ISensorHub hub = new SensorHub();
        hub.getDatabaseRegistry().getObsDatabaseByModuleID("module12345");
        hub.getDatabaseRegistry().getObsDatabaseByNum(4);
        hub.getDatabaseRegistry().getFederatedDatabase();
        hub.getSystemDriverRegistry().getDatabase("urn:osh:sensor:simulated:001");

//        IObsSystemDatabase db = hub.getDatabaseRegistry().getObsDatabaseByNum(4);
//        FeatureKey systemKey = new FeatureKey(new BigIdLong(5, 1234));
//        db.getSystemDescStore().put(systemKey, new SystemWrapper(newSystem));
//        var item = db.getSystemDescStore().get(systemKey);
//        db.getSystemDescStore().remove(systemKey);

        var db = hub.getDatabaseRegistry().getFederatedDatabase();
        SystemFilter sysFilter = new SystemFilter.Builder()
                .includeMembers(true)
                .withUniqueIDs("urn:osh:sensor:simulated:001")
                .build();
        DataStreamFilter dsFilter = new DataStreamFilter.Builder()
                .withOutputNames("weather")
                .withSystems(sysFilter)
                .build();
        ObsFilter obsFilter = new ObsFilter.Builder()
                .withPhenomenonTimeDuring(
                        Instant.now()
                                .minusSeconds(100),
                        Instant.now())
                .withLimit(2000)
                .build();
        var entries = db.getSystemDescStore().selectEntries(sysFilter).toList();
        for (var entry : entries) {
            var systemKey = entry.getKey();
            var systemDescription = entry.getValue();
        }
        db.getDataStreamStore().removeEntries(dsFilter);
        long numObs = db.getObservationStore().select(obsFilter).count();
    }

}
