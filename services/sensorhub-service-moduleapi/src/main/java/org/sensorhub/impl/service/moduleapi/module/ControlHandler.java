package org.sensorhub.impl.service.moduleapi.module;

import org.sensorhub.impl.module.ModuleRegistry;
import org.sensorhub.impl.service.sweapi.BaseHandler;
import org.sensorhub.impl.service.sweapi.InvalidRequestException;
import org.sensorhub.impl.service.sweapi.ServiceErrors;
import org.sensorhub.impl.service.sweapi.resource.RequestContext;

import java.io.IOException;

public class ControlHandler extends BaseHandler {

    public static final String[] NAMES = { "control" };

    ModuleRegistry registry;

    public ControlHandler(ModuleRegistry registry) {
        this.registry = registry;
    }

    @Override
    public String[] getNames() {
        return NAMES;
    }

    @Override
    public void doGet(RequestContext ctx) throws InvalidRequestException, IOException, SecurityException {
        throw ServiceErrors.unsupportedOperation("This endpoint only supports POST");
    }

    @Override
    public void doPost(RequestContext ctx) throws InvalidRequestException, IOException, SecurityException {
        // TODO: allow { "action" : "START"/stop/restart/init/
        // TODO: Check we are at correct endpoint. Should be /modules/{moduleId}/control
        // TODO: Check body only contains "action"
        // TODO: Check value contains one of set params [ "START", "STOP", "RESTART", etc.]
        // TODO: Try action on module
        if(ctx.isEndOfPath() && !(ctx.getParentRef().type instanceof ModuleHandler))
            throw ServiceErrors.unsupportedOperation("Commands can only be used on a module");

        String id = ctx.popNextPathElt();
        String path = ctx.getRequestPath();
        String endpoint = ctx.getApiRootURL();
        boolean isEnd = ctx.isEndOfPath();
    }

    @Override
    public void doPut(RequestContext ctx) throws InvalidRequestException, IOException, SecurityException {
        throw ServiceErrors.unsupportedOperation("This endpoint only supports POST");
    }

    @Override
    public void doDelete(RequestContext ctx) throws InvalidRequestException, IOException, SecurityException {
        throw ServiceErrors.unsupportedOperation("This endpoint only supports POST");
    }
}
