/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2019 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.sta;

import java.time.Instant;
import java.util.stream.Stream;
import org.sensorhub.api.datastore.FeatureKey;
import org.sensorhub.api.datastore.IFeatureStore;
import net.opengis.gml.v32.AbstractFeature;


/**
 * <p>
 * Interface for SensorThings Location data stores.
 * </p>
 *
 * @author Alex Robin
 * @date Oct 16, 2019
 */
public interface ILocationStore extends IFeatureStore<FeatureKey, AbstractFeature>
{

    public void addAssociation(long thingID, long locationID, Instant time);
    
    
    public Stream<Instant> getThingHistoricalLocations(long thingID);
    
}
