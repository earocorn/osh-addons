package org.sensorhub.impl.service.moduleapi.template;

import org.sensorhub.api.common.SensorHubException;
import org.sensorhub.api.module.IModule;
import org.sensorhub.api.module.IModuleProvider;
import org.sensorhub.api.processing.IProcessModule;
import org.sensorhub.api.sensor.ISensorModule;
import org.sensorhub.impl.module.ModuleRegistry;
import org.sensorhub.impl.service.sweapi.BaseHandler;
import org.sensorhub.impl.service.sweapi.InvalidRequestException;
import org.sensorhub.impl.service.sweapi.resource.RequestContext;

import java.io.IOException;
import java.util.Map;

public class TemplateHandler extends BaseHandler {

    public static final String[] NAMES = { "templates" };
    ModuleRegistry registry;

    public TemplateHandler(ModuleRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void doGet(RequestContext ctx) throws SecurityException {

    }

    /**
     * List all available module configuration templates
     * @param ctx
     * @throws IOException
     */
    private void list(RequestContext ctx) throws IOException {

    }

    /**
     * Retrieve module configuration template by its object class
     * @param ctx
     * @param objectClass
     * @throws SensorHubException
     * @throws IOException
     */
    private void getById(RequestContext ctx, String objectClass) throws SensorHubException, IOException {

    }

    @Override
    public void doPost(RequestContext ctx) throws InvalidRequestException, IOException, SecurityException {
        throw new UnsupportedOperationException("Operation not supported");
    }

    @Override
    public void doPut(RequestContext ctx) throws SecurityException {
        throw new UnsupportedOperationException("Operation not supported");
    }

    @Override
    public void doDelete(RequestContext ctx) throws SecurityException {
        throw new UnsupportedOperationException("Operation not supported");
    }

    @Override
    public String[] getNames() {
        return NAMES;
    }

}
