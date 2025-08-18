/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.datastore.h2;

import com.alexalmanza.impl.datastore.mongodb.MongoObsSystemDatabase;
import org.sensorhub.api.config.DisplayInfo;
import org.sensorhub.api.database.DatabaseConfig;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


/**
 * <p>
 * Config class for {@link MVObsSystemDatabase} module
 * </p>
 *
 * @author Alex Robin
 * @date Sep 23, 2019
 */
public class MongoObsSystemDatabaseConfig extends DatabaseConfig
{
    
    @DisplayInfo(desc="Set to enable spatial indexing of individual observations sampling locations (when provided)")
    public boolean indexObsLocation = false;

    public enum IdProviderType
    {
        SEQUENTIAL,
        UID_HASH
    }

    @DisplayInfo(label = "Connection URI", desc = "Connection string to connect to MongoDB instance")
    public String uri;

    @NotNull
    @DisplayInfo(label = "ID Generator", desc = "Method used to generate new resource IDs")
    public IdProviderType idProviderType = IdProviderType.SEQUENTIAL;


    @DisplayInfo(desc = "Set to compress underlying file storage")
    public boolean useCompression = false;


    @DisplayInfo(desc = "Set to open the database as read-only")
    public boolean readOnly = false;


    public MongoObsSystemDatabaseConfig()
    {
        this.moduleClass = MongoObsSystemDatabase.class.getCanonicalName();
    }
}
