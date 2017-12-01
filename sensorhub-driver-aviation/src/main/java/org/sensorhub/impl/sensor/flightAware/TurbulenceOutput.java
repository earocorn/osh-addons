/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.

The Initial Developer is Sensia Software LLC. Portions created by the Initial
Developer are Copyright (C) 2014 the Initial Developer. All Rights Reserved.

 ******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.sensor.flightAware;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.sensorhub.api.common.SensorHubException;
import org.sensorhub.api.data.IMultiSourceDataInterface;
import org.sensorhub.api.sensor.SensorDataEvent;
import org.sensorhub.impl.sensor.AbstractSensorOutput;
import org.sensorhub.impl.sensor.mesh.DirectoryWatcher;
import org.sensorhub.impl.sensor.mesh.FileListener;
import org.vast.data.DataBlockList;
import org.vast.data.DataBlockMixed;
import org.vast.swe.SWEHelper;
import org.vast.swe.helper.GeoPosHelper;

import net.opengis.swe.v20.Count;
import net.opengis.swe.v20.DataArray;
import net.opengis.swe.v20.DataBlock;
import net.opengis.swe.v20.DataComponent;
import net.opengis.swe.v20.DataEncoding;
import net.opengis.swe.v20.DataRecord;
import net.opengis.swe.v20.DataType;
import net.opengis.swe.v20.Quantity;
import net.opengis.swe.v20.Text;
import ucar.ma2.InvalidRangeException;


/**
 * 
 * @author Tony Cook
 * 
 * 
 * TODO- add zulu time output somewhere
 *
 */
public class TurbulenceOutput extends AbstractSensorOutput<FlightAwareDriver> implements IMultiSourceDataInterface //, FileListener  
{
	private static final int AVERAGE_SAMPLING_PERIOD = (int)TimeUnit.MINUTES.toSeconds(15); 

	Thread watcherThread;
	private DirectoryWatcher watcher;

	DataRecord recordStruct;
	DataEncoding encoding;	
	Map<String, Long> latestUpdateTimes;
	Map<String, DataBlock> latestRecords = new LinkedHashMap<>();  // key is full turb uid
	Map<String, FlightPlan> availableFlightPlans = new LinkedHashMap<>();  // key is full turb uid

	private DataRecord profileStruct;

//	private TurbulenceReader reader;

	public TurbulenceOutput(FlightAwareDriver parentSensor) 
	{
		super(parentSensor);
		latestUpdateTimes = new HashMap<String, Long>();
	}


	@Override
	public String getName()
	{
		return "Turbulence profile data";
	}

	protected void init()
	{
		SWEHelper fac = new SWEHelper();
		GeoPosHelper geoHelper = new GeoPosHelper();

		//  NumProfiles, Profiles []
		recordStruct = fac.newDataRecord(2); 
		recordStruct.setName(getName());
		recordStruct.setDefinition("http://earthcastwx.com/ont/swe/property/turbulence"); // ??

		Count numProfiles = fac.newCount(DataType.INT);
		numProfiles.setId("NUM_PROFILES");
		recordStruct.addComponent("NumProfiles",numProfiles);

		profileStruct = fac.newDataRecord(6); 
		profileStruct.setName(getName());
		profileStruct.setDefinition("http://earthcastwx.com/ont/swe/property/turbulenceProfiles"); // ??

		//  
		DataArray profileArr = fac.newDataArray();
		profileArr.setElementType("Turbulence", profileStruct);
		profileArr.setElementCount(numProfiles);
		recordStruct.addComponent("Profiles", profileArr);

		//	 time, wayPtName, lat, lon, numPoints, float[] turbVal
		profileStruct.addComponent("time", fac.newTimeStampIsoGPS());
		Text name = fac.newText("http://sensorml.com/ont/swe/property/code", "ICAO Code", "Typically, ICAO airline code plus IATA/ticketing flight number");
		profileStruct.addComponent("WaypointName", name);
		Quantity latQuant = fac.newQuantity("http://sensorml.com/ont/swe/property/Latitude", "Latitude", null, "deg", DataType.FLOAT);
		Quantity lonQuant = fac.newQuantity("http://sensorml.com/ont/swe/property/Longitde", "Longitde", null, "deg", DataType.FLOAT);
		profileStruct.addComponent("Latitude", latQuant);
		profileStruct.addComponent("Longitude", lonQuant);

		//  Vertical Profile
		Count numPoints = fac.newCount(DataType.INT);
		numPoints.setDefinition("http://sensorml.com/ont/swe/property/NumberOfSamples"); 
		numPoints.setId("PROFILE_POINTS");
		profileStruct.addComponent("numTurbulencePoints",numPoints);

		//  Turb values
		Quantity valuesQuant = fac.newQuantity("http://earthcastwx.com/ont/swe/property/TurbulenceVales", "Turbulence Profile Values", null, "", DataType.FLOAT);
		DataArray valuesArr = fac.newDataArray();
		valuesArr.setElementType("Turbulence", valuesQuant);
		//  DataArrayImpl.579
		//             elementCount.setValue((Count)sizeComponent);  //  (Count)sizeComponent == 0 
		numPoints.setValue(52);
		valuesArr.setElementCount(numPoints);
		profileStruct.addComponent("Turbulence Profile", valuesArr);

		encoding = fac.newTextEncoding(",", "\n");
	}

//	public void start(String dataPath) throws SensorHubException {
//		try {
//			watcher = new DirectoryWatcher(Paths.get(dataPath), StandardWatchEventKinds.ENTRY_CREATE);
//			watcherThread = new Thread(watcher);
//			watcher.addListener(this);
//			watcherThread.start();
//		} catch (IOException e) {
//			e.printStackTrace();
//			throw new SensorHubException("TurbulenceSensor could not create DirectoryWatcher...", e);
//		}
//	}

	public DataBlock sendProfiles(List<TurbulenceRecord> recs, FlightPlan plan)
	{
		DataArray profileArr = (DataArray)recordStruct.getComponent(1);
		profileArr.updateSize(recs.size());
		DataBlock bigBlock = recordStruct.createDataBlock();
		bigBlock.setIntValue(0, recs.size());
		DataBlock profileBlock = ((DataBlockMixed)bigBlock).getUnderlyingObject()[1];

		// build data block from Mesh Record
		int idx = 0;
		for(TurbulenceRecord rec: recs) {
			DataArray turbArr = (DataArray)profileStruct.getComponent(5);
			turbArr.updateSize(rec.turbulence.length);
			DataBlock dataBlock = profileStruct.createDataBlock();

			dataBlock.setDoubleValue(0, rec.time);
			dataBlock.setStringValue(1, rec.waypointName);
			dataBlock.setDoubleValue(2, rec.lat);
			dataBlock.setDoubleValue(3, rec.lon);
			dataBlock.setIntValue(4 , rec.turbulence.length);
			((DataBlockMixed)dataBlock).getUnderlyingObject()[5].setUnderlyingObject(rec.turbulence);
			Object o = ((DataBlockList)profileBlock).getUnderlyingObject();
			LinkedList<DataBlock> ll = (LinkedList<DataBlock>) o;
			ll.set(idx++, dataBlock);
		}
		// update latest record and send event
		latestRecord = bigBlock;
		latestRecordTime = System.currentTimeMillis();
		String flightUid = FlightAwareDriver.TURBULENCE_UID_PREFIX + plan.oshFlightId;
		latestUpdateTimes.put(flightUid, plan.time);
		latestRecords.put(flightUid, latestRecord);   
		eventHandler.publishEvent(new SensorDataEvent(latestRecordTime, TurbulenceOutput.this, bigBlock));
		return bigBlock;
	}

	public void addFlightPlan(String uid, FlightPlan newPlan) {
		availableFlightPlans.put(uid, newPlan);
	}

	public double getAverageSamplingPeriod()
	{
		return AVERAGE_SAMPLING_PERIOD;
	}


	@Override 
	public DataComponent getRecordDescription()
	{
		return recordStruct;
	}


	@Override
	public DataEncoding getRecommendedEncoding()
	{
		return encoding;
	}


	@Override
	public Collection<String> getEntityIDs()
	{
		return parentSensor.getEntityIDs();
	}


	@Override
	public Map<String, DataBlock> getLatestRecords()
	{
		return Collections.unmodifiableMap(latestRecords);
	}


	@Override
	public DataBlock getLatestRecord(String entityID) {
		DataBlock b = latestRecords.get(entityID);
		//		if(b != null)
		//			return b;

		//  get FlightPlan- *should* be updating in FltAware 
		//  every time newFlightPlan() is called there
		//  Since FlightAwareSensor is now listening for Turb files
		FlightPlan plan = availableFlightPlans.get(entityID);
		if (plan == null) {
			log.info("TurbOutput.getLatest():  Should have data for this plan but we don't. Cannot compute turbulence");
			return null;
		}

		// Construct new latestRecord for this id
		try {
//			List<TurbulenceRecord> recs = reader.getTurbulence(plan);
			List<TurbulenceRecord> recs = parentSensor.getTurbulence(plan);
			if(recs == null) {
				log.info("TurbOutput.getLatest():  Reading turb data failed.");
				return null;
			}
			DataBlock bigBlock = sendProfiles(recs, plan);
			return bigBlock;
		} catch (IOException | InvalidRangeException e) {
			System.err.println(e.getMessage());
		}

		return null;
	}

}
