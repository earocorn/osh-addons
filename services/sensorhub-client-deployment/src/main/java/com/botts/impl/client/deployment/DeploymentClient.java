/***************************** BEGIN LICENSE BLOCK ***************************

The contents of this file are subject to the Mozilla Public License, v. 2.0.
If a copy of the MPL was not distributed with this file, You can obtain one
at http://mozilla.org/MPL/2.0/.

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the License.
 
Copyright (C) 2012-2016 Sensia Software LLC. All Rights Reserved.
 
******************************* END LICENSE BLOCK ***************************/

package com.botts.impl.client.deployment;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

import com.google.common.base.Strings;
import net.opengis.gml.v32.Reference;
import net.opengis.gml.v32.impl.ReferenceImpl;
import net.opengis.sensorml.v20.DeployedSystem;
import net.opengis.sensorml.v20.Deployment;
import net.opengis.sensorml.v20.impl.DeployedSystemImpl;
import org.sensorhub.api.comm.ICommProvider;
import org.sensorhub.api.common.SensorHubException;
import org.sensorhub.api.database.IObsSystemDatabase;
import org.sensorhub.api.datastore.system.SystemFilter;
import org.sensorhub.api.system.ISystemWithDesc;
import org.sensorhub.impl.module.AbstractModule;
import org.sensorhub.impl.service.consys.client.ConSysApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vast.sensorML.SMLHelper;
import org.vast.util.Asserts;


/**
 * <p>
 * Comm Relay Service implementation simply forwarding data from incoming input
 * stream to outgoing outputstream, and from outgoing inputstream to incoming
 * outputstream (for commands).
 * </p>
 *
 * @author Alexandre Robin <alex.robin@sensiasoftware.com>
 * @since Feb 16, 2016
 */
public class DeploymentClient extends AbstractModule<DeploymentClientConfig>
{
    private static final Logger log = LoggerFactory.getLogger(DeploymentClient.class);
    IObsSystemDatabase dataBaseView;
    String apiEndpointUrl;
    ConSysApiClient client;
    SMLHelper fac;

    public DeploymentClient()
    {
        fac = new SMLHelper();
    }

    @Override
    public void setConfiguration(DeploymentClientConfig config)
    {
        super.setConfiguration(config);

        String scheme = "http";
        if (config.conSys.enableTLS)
            scheme += "s";
        apiEndpointUrl = scheme + "://" + config.conSys.remoteHost + ":" + config.conSys.remotePort;
        if (config.conSys.resourcePath != null)
        {
            if (config.conSys.resourcePath.charAt(0) != '/')
                apiEndpointUrl += '/';
            apiEndpointUrl += config.conSys.resourcePath;
        }
    }

    @Override
    protected void doInit() {
        this.dataBaseView = config.dataSourceSelector.getFilteredView(getParentHub());

        this.client = ConSysApiClient.
                newBuilder(apiEndpointUrl)
                .simpleAuth(config.conSys.user, !config.conSys.password.isEmpty() ? config.conSys.password.toCharArray() : null)
                .build();
    }

    @Override
    protected void doStart() throws SensorHubException {
        // Check if endpoint is available
        try{
            HttpURLConnection urlConnection = (HttpURLConnection) new URL(apiEndpointUrl).openConnection();
            if (!Strings.isNullOrEmpty(config.conSys.user)) {
                urlConnection.setAuthenticator(new Authenticator() {
                    @Override
                    public PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(config.conSys.user, config.conSys.password != null ? config.conSys.password.toCharArray() : new char[0]);
                    }
                });
            }
            urlConnection.connect();
            Asserts.checkArgument(urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK);
        } catch (Exception e) {
            throw new SensorHubException("Unable to establish connection to Connected Systems endpoint");
        }

        reportStatus("Connection to " + apiEndpointUrl + " was made successfully");
    }

    @Override
    protected void doStop() throws SensorHubException {
        super.doStop();
    }

    private Deployment createDeployment() {
        String description = "";
        if (config.deployment.description != null && !config.deployment.description.isEmpty())
            description = config.deployment.description;

        Deployment deployment = fac.createDeployment()
                .name(config.deployment.name)
                .id(config.deployment.deploymentId)
                .uniqueID("urn:osh:deployment:" + config.deployment.deploymentId)
                .description(description)
                .build();

        dataBaseView.getSystemDescStore().selectEntries(
                dataBaseView.getSystemDescStore().selectAllFilter()
        ).forEach(entry -> {
            ISystemWithDesc system = entry.getValue();
            DeployedSystem deployedSystem = new DeployedSystemImpl();
            deployedSystem.setId(system.getId());
            deployedSystem.setName(system.getName());
            deployedSystem.setDescription(system.getDescription());
            deployedSystem.setUniqueIdentifier(system.getUniqueIdentifier());
            Reference systemRef = new ReferenceImpl();
            // systemRef.setName(system.getName());
            // TODO: Need to push system to remote server to get href link
            // systemRef.setHref(target CSAPI system link);
            deployedSystem.setSystemRef(systemRef);
        });

        return deployment;
    }

}
