package org.sensorhub.impl.sensor.nexrad;

import java.io.IOException;
import java.util.List;

import org.sensorhub.impl.sensor.nexrad.aws.Radial;

/**
 * <p>Title: RadialProvider.java</p>
 * <p>Description: </p>
 *
 * @author tcook
 * @date Sep 14, 2016
 * 
 * Probably won't go this route
 * 
 */

public interface RadialProvider {
	
	public Radial getNextRadial();
	// Deprecate this if we end up going to always passing site in
	public List<Radial>  getNextRadials() throws IOException;

	public List<Radial>  getNextRadials(String site) throws IOException;

}
