/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2022 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.moduleapi.home;

import java.io.IOException;
import java.util.Collection;
import org.sensorhub.impl.service.moduleapi.ModuleApiServiceConfig;
import org.sensorhub.impl.service.sweapi.resource.RequestContext;
import org.sensorhub.impl.service.sweapi.resource.ResourceBindingJson;
import org.sensorhub.impl.service.sweapi.resource.ResourceLink;
import org.vast.util.ResponsibleParty;
import com.google.common.base.Strings;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;


public class HomePageJson extends ResourceBindingJson<Long, ModuleApiServiceConfig>
{
    
    public HomePageJson(RequestContext ctx) throws IOException
    {
        super(ctx, null, false);
    }
    
    
    @Override
    public void serialize(Long key, ModuleApiServiceConfig config, boolean showLinks, JsonWriter writer) throws IOException
    {
        writer.beginObject();
        
        // title and description
        var serviceInfo = config.ogcCapabilitiesInfo;
        var title = config.name;
        var description = config.description;
        if (serviceInfo != null)
        {
            if (!Strings.isNullOrEmpty(serviceInfo.title))
                title = serviceInfo.title;
            if (!Strings.isNullOrEmpty(serviceInfo.description))
                description = serviceInfo.description;
        }
        writer.name("title").value(title);
        if (description != null)
            writer.name("description").value(description);
            
        // links
        writer.name("links").beginArray();
        
        writer.beginObject();
        writer.name("rel").value("service-desc");
        writer.name("type").value("application/vnd.oai.openapi;version=3.0");
        writer.name("title").value("Definition of the API in OpenAPI 3.0");
        writer.endObject();
        
        writer.beginObject();
        writer.name("rel").value("conformance");
        writer.name("type").value("application/json");
        writer.name("title").value("OGC API conformance classes implemented by this server");
        writer.name("href").value(ctx.getApiRootURL() + "/conformance");
        writer.endObject();
        
        writer.beginObject();
        writer.name("rel").value("systems");
        writer.name("type").value("application/json");
        writer.name("title").value("Data collections available on this server");
        writer.name("href").value(ctx.getApiRootURL() + "/collections");
        writer.endObject();
        
        writer.endArray();
        writer.endObject();
        writer.flush();
    }


    @Override
    public ModuleApiServiceConfig deserialize(JsonReader reader) throws IOException
    {
        // this should never be called since home page is read-only
        throw new UnsupportedOperationException();
    }
    
    
    @Override
    public void startCollection() throws IOException
    {
        // this should never be called since home page is not a collection
        throw new UnsupportedOperationException();
    }


    @Override
    public void endCollection(Collection<ResourceLink> links) throws IOException
    {
        // this should never be called since home page is not a collection
        throw new UnsupportedOperationException();
    }
}
