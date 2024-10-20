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
import org.sensorhub.impl.service.moduleapi.ModuleApiServiceConfig;
import org.sensorhub.impl.service.sweapi.BaseHandler;
import org.sensorhub.impl.service.sweapi.InvalidRequestException;
import org.sensorhub.impl.service.sweapi.ServiceErrors;
import org.sensorhub.impl.service.sweapi.resource.RequestContext;
import org.sensorhub.impl.service.sweapi.resource.ResourceFormat;


public class HomePageHandler extends BaseHandler
{
    
    ModuleApiServiceConfig serviceConfig;
    
    
    public HomePageHandler(ModuleApiServiceConfig serviceConfig)
    {
        this.serviceConfig = serviceConfig;
    }
    
    
    @Override
    public void doGet(RequestContext ctx) throws InvalidRequestException, IOException, SecurityException
    {
        var format = parseFormat(ctx.getParameterMap());
        
        // set content type
        ctx.setResponseContentType(format.getMimeType());

        if (format.isOneOf(ResourceFormat.AUTO, ResourceFormat.JSON))
            new HomePageJson(ctx).serialize(0L, serviceConfig, true);
        else
            throw ServiceErrors.unsupportedFormat(format);
    }
    
    
    @Override
    public String[] getNames()
    {
        return null;
    }
    
    
    @Override
    public void doPost(RequestContext ctx) throws InvalidRequestException, IOException, SecurityException
    {
        ServiceErrors.unsupportedOperation("");
    }
    

    @Override
    public void doPut(RequestContext ctx) throws InvalidRequestException, IOException, SecurityException
    {
        ServiceErrors.unsupportedOperation("");
    }
    

    @Override
    public void doDelete(RequestContext ctx) throws InvalidRequestException, IOException, SecurityException
    {
        ServiceErrors.unsupportedOperation("");
    }

}
