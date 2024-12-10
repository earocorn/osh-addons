/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.

Copyright (C) 2024 Botts Innovative Research, Inc. All Rights Reserved.

******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.sta.ingest;

import com.google.common.base.Strings;
import org.sensorhub.api.common.SensorHubException;
import org.sensorhub.api.database.IObsSystemDatabase;
import org.sensorhub.api.module.ModuleEvent.ModuleState;
import org.sensorhub.impl.module.AbstractModule;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;


/**
 * <p>
 *     Service to ingest SensorThings API resources as OSH-readable resources.
 * </p>
 *
 * @author Alex Almanza
 * @since Dec 9, 2024
 */
public class STAIngestService extends AbstractModule<STAIngestConfig>
{

    @Override
    public void setConfiguration(STAIngestConfig config)
    {
        super.setConfiguration(config);
    }


    @Override
    protected void doStart() throws SensorHubException
    {
        IObsSystemDatabase writeDb;

        // Get obs database to store ingested data
        if (!Strings.isNullOrEmpty(config.databaseID))
        {
            writeDb = (IObsSystemDatabase) getParentHub().getModuleRegistry().getModuleById(config.databaseID);
            if (writeDb != null && !writeDb.isOpen())
                writeDb = null;
        }
        else
            writeDb = getParentHub().getSystemDriverRegistry().getSystemStateDatabase();


        // TODO: Ingest Datastreams, Things, Sensors, Observations, ObservedProperties. Store in writeDb

        Instant now = Instant.now();

        for (String urlString : config.staBaseResourcePathList)
        {
            try
            {
                URL url = new URL(urlString);
                STAIngestor staIngestor = new STAIngestor(url, writeDb);
            } catch (MalformedURLException e) {
                throw new SensorHubException("URL " + urlString + " is not a valid URL", e);
            }
        }

        long elapsed = Instant.now().minusMillis(now.toEpochMilli()).toEpochMilli();
        reportStatus("Ingestion completed in " + elapsed + "ms");

        setState(ModuleState.STARTED);
    }


    @Override
    protected void doStop()
    {
        setState(ModuleState.STOPPED);
    }

}
