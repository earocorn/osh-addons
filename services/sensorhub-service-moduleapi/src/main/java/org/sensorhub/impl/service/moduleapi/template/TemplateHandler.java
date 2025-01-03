package org.sensorhub.impl.service.moduleapi.template;

import org.sensorhub.api.common.BigId;
import org.sensorhub.api.common.SensorHubException;
import org.sensorhub.api.datastore.IQueryFilter;
import org.sensorhub.impl.module.ModuleRegistry;
import org.sensorhub.impl.service.consys.ResourceParseException;
import org.sensorhub.impl.service.consys.resource.ResourceBinding;
import org.sensorhub.impl.service.moduleapi.ModuleBaseResourceHandler;
import org.sensorhub.impl.service.consys.InvalidRequestException;
import org.sensorhub.impl.service.consys.resource.RequestContext;

import java.io.IOException;
import java.util.Map;

public class TemplateHandler extends ModuleBaseResourceHandler {

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
    @Override
    protected void list(RequestContext ctx) throws IOException {

    }

    @Override
    protected void create(RequestContext ctx) throws IOException {

    }

    @Override
    protected void update(RequestContext ctx, String id) throws IOException {

    }

    @Override
    protected void delete(RequestContext ctx, String id) throws IOException {

    }

    /**
     * Retrieve module configuration template by its object class
     * @param ctx
     * @param objectClass
     * @throws IOException
     */
    @Override
    protected void getById(RequestContext ctx, String objectClass) throws IOException {

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
