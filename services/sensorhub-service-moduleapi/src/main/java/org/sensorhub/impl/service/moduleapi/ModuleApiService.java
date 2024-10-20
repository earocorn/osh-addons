/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.

Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.

******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.moduleapi;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.sensorhub.api.common.SensorHubException;
import org.sensorhub.api.database.IObsSystemDatabase;
import org.sensorhub.api.event.IEventListener;
import org.sensorhub.api.module.ModuleEvent.ModuleState;
import org.sensorhub.api.service.IServiceModule;
import org.sensorhub.impl.service.AbstractHttpServiceModule;
import org.sensorhub.impl.service.moduleapi.home.HomePageHandler;
import org.sensorhub.impl.service.moduleapi.module.ModuleHandler;
import org.sensorhub.impl.service.sweapi.ObsSystemDbWrapper;
import org.sensorhub.impl.service.sweapi.RestApiService;
import org.sensorhub.utils.NamedThreadFactory;


/**
 * <p>
 * Implementation of OSH Module API service.<br/>
 * The service can be configured to interface with OSH modules.
 * </p>
 *
 * @author Alex Almanza
 * @since Oct 20, 2024
 */
public class ModuleApiService extends AbstractHttpServiceModule<ModuleApiServiceConfig> implements RestApiService, IServiceModule<ModuleApiServiceConfig>, IEventListener
{
    protected ModuleApiServlet servlet;
    ScheduledExecutorService threadPool;


    @Override
    public void setConfiguration(ModuleApiServiceConfig config)
    {
        super.setConfiguration(config);
        this.securityHandler = new ModuleApiSecurity(this, config.security.enableAccessControl);
    }


    @Override
    protected void doStart() throws SensorHubException
    {
        // create filtered DB or expose entire federated DB
        IObsSystemDatabase readDb;
        if (config.exposedResources != null)
            readDb = config.exposedResources.getFilteredView(getParentHub());
        else
            readDb = getParentHub().getDatabaseRegistry().getFederatedDatabase();

        // init thread pool
        threadPool = Executors.newScheduledThreadPool(
            Runtime.getRuntime().availableProcessors(),
            new NamedThreadFactory("SWAPool"));

        // init timeout monitor
        //timeOutMonitor = new TimeOutMonitor();
        
//        var db = new ObsSystemDbWrapper(readDb, null, getParentHub().getIdEncoders());
//        var eventBus = getParentHub().getEventBus();
//        var security = (ModuleApiSecurity)this.securityHandler;
        
        // create resource handlers hierarchy
        var homePage = new HomePageHandler(config);
        var rootHandler = new RootHandler(homePage, false);
        
        var moduleHandler = new ModuleHandler(getParentHub().getModuleRegistry());
        rootHandler.addSubResource(moduleHandler);
        
        // deploy servlet
        servlet = new ModuleApiServlet(this, (ModuleApiSecurity) securityHandler, rootHandler, getLogger());
        deploy();

        setState(ModuleState.STARTED);
    }


    protected void deploy() throws SensorHubException
    {
        var wildcardEndpoint = config.endPoint + "/*";
        
        // deploy ourself to HTTP server
        httpServer.deployServlet(servlet, wildcardEndpoint);
        httpServer.addServletSecurity(wildcardEndpoint, config.security.requireAuth);
    }


    @Override
    protected void doStop()
    {
        // undeploy servlet
        undeploy();
        
        // stop thread pool
        if (threadPool != null)
            threadPool.shutdown();

        setState(ModuleState.STOPPED);
    }


    protected void undeploy()
    {
        // return silently if HTTP server missing on stop
        if (httpServer == null || !httpServer.isStarted())
            return;

        if (servlet != null)
        {
            httpServer.undeployServlet(servlet);
            servlet.destroy();
            servlet = null;
        }
    }


    @Override
    public void cleanup() throws SensorHubException
    {
        // unregister security handler
        if (securityHandler != null)
            securityHandler.unregister();
    }


    public ScheduledExecutorService getThreadPool()
    {
        return threadPool;
    }
    
    
    public ModuleApiServlet getServlet()
    {
        return servlet;
    }
    
    
    public String getPublicEndpointUrl()
    {
        return getHttpServer().getPublicEndpointUrl(config.endPoint);
    }


    /*public TimeOutMonitor getTimeOutMonitor()
    {
        return timeOutMonitor;
    }*/
}
