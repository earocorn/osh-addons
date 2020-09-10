package org.sensorhub.impl.ndbc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.Map;

import org.slf4j.Logger;
import org.vast.ogc.om.SamplingPoint;
import org.vast.swe.SWEHelper;
import org.vast.util.Bbox;

import net.opengis.gml.v32.AbstractFeature;
import net.opengis.gml.v32.Point;
import net.opengis.gml.v32.impl.GMLFactory;

/**
 * <p>
 * Loader for NDBC tabular data of observation stations<br/>
 * See <a href="http://sdf.ndbc.noaa.gov/"> web service documentation</a>
 * Station Info in html: https://sdf.ndbc.noaa.gov/stations.shtml
 * </p>
 *
 * @author Lee Butler <labutler10@gmail.com>
 * @since Jan 27, 2018
 */
public class ObsStationLoader {
//	static final String BASE_URL = NDBCArchive.BASE_NDBC_URL
//			+ "?request=GetObservation&service=SOS&version=1.0.0&";
	static final int AGE_THRESHOLD_DAYS = 7;  // configurable
	
	Logger logger;
	String obsUrl;
	
	public ObsStationLoader(String ndbcUrl, Logger logger) {
		this.logger = logger;
		this.obsUrl = ndbcUrl + "?request=GetObservation&service=SOS&version=1.0.0&";
	}

	//  Load stations by requesting latest measurement from all (or  filtered) stations in network
	//  TODO- modify to get from caps
	public void loadStations(Map<String, AbstractFeature> fois, NDBCConfig config) throws IOException {
		StringBuilder buf = new StringBuilder(obsUrl);

		//  We want all stations
		buf.append("&offering=urn:ioos:network:noaa.nws.ndbc:all");
		// site bbox
		if (config.siteBbox != null && !config.siteBbox.isNull()) {
			Bbox bbox = config.siteBbox;
			buf.append("&featureofinterest=BBOX:").append(bbox.getMinX())
					.append(",").append(bbox.getMinY()).append(",").append(bbox.getMaxX()).append(",")
					.append(bbox.getMaxY());
		} 

		//  Filter parameters MUST contain at least one property.  
		//  For getting station list, we need to specify a single property, so pick the first
		//  NOTE: using a param for the station list will only return stations that measure that param
		//  We can get all the buoy IDs, lat-lons, and time ranges from Caps doc:
		//  	https://sdf.ndbc.noaa.gov/sos/server.php?request=GetCapabilities&service=SOS
		//  It is 2 MB so for nwo just sticking with this method 
		if(config.parameters.size() == 0) {
			throw new IOException("DataFilter must contain at list one parameter in Set<ObsParam> parameters");
		}
		BuoyParam param = config.parameters.iterator().next();
		if(param == BuoyParam.ENVIRONMENTAL)
			param = BuoyParam.SEA_WATER_TEMPERATURE;
		buf.append("&observedProperty=" + param.toString().toLowerCase());

		buf.append("&responseformat=text/csv&eventtime=latest"); // output type
		logger.debug("Requesting stations from: " + buf.toString());
		URL url = new URL(buf.toString());
		BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

		String line;
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			
			// parse header
			if (line.startsWith("station_id,sensor_id"))
				line = reader.readLine(); // skip headers

			String[] token = line.split(",");
			String[] idArr = token[0].split(":");

			// Check the date- if latest is too old, skip it
			// NOTE: this check should not be done for archive data
			String dateStr = token[4];
			OffsetDateTime time = OffsetDateTime.parse(dateStr);
			OffsetDateTime now = OffsetDateTime.now();
			
			if(time.compareTo(now.minusDays(AGE_THRESHOLD_DAYS)) < 0)
				continue;
			
			GMLFactory gmlFac = new GMLFactory(true);
			SamplingPoint station = new SamplingPoint();
			
			// Get Buoy ID
			station.setId(idArr[idArr.length - 1]);
			station.setUniqueIdentifier(token[0]);
			station.setName("NDBC Buoy Station " + idArr[idArr.length - 1]);

			// Get Buoy Location
			Point stnLoc = gmlFac.newPoint();
			stnLoc.setSrsDimension(2);
			stnLoc.setSrsName(SWEHelper.getEpsgUri(4269)); // NAD83
			stnLoc.setPos(new double[] { Double.parseDouble(token[2]), Double.parseDouble(token[3]) });
			station.setShape(stnLoc);
			
			logger.debug("Adding FOI: " + station.getUniqueIdentifier() + " : " + station.getId() + " : " + stnLoc);
			
			fois.put(idArr[idArr.length - 1], station);
		}
	}
}
