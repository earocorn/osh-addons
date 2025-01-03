package org.sensorhub.impl.service.moduleapi.module;

import com.google.gson.stream.JsonReader;
import org.sensorhub.api.common.SensorHubException;
import org.sensorhub.api.module.IModule;
import org.sensorhub.impl.module.ModuleRegistry;
import org.sensorhub.impl.service.consys.BaseHandler;
import org.sensorhub.impl.service.consys.InvalidRequestException;
import org.sensorhub.impl.service.consys.ServiceErrors;
import org.sensorhub.impl.service.consys.resource.RequestContext;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;

public class ControlHandler extends BaseHandler {

    public static final String[] NAMES = { "control" };

    public static final HashSet<String> MODULE_ACTIONS = new HashSet<>(){{
        add("START");
        add("STOP");
        add("RESTART");
        add("INIT");
    }};

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
        // TODO: Check module id exists
        // TODO: Check body only contains "action"
        // TODO: Check value contains one of set params [ "START", "STOP", "RESTART", etc.]
        // TODO: Try action on module
        IModule<?> module = ModuleHandler.validatePathModule(ctx, this.registry);
        String moduleId = module.getLocalID();

        InputStream is = new BufferedInputStream(ctx.getInputStream());
        var reader = new JsonReader(new InputStreamReader(is, StandardCharsets.UTF_8));

        reader.beginObject();

        String actionField = reader.nextName();
        String actionValue = reader.nextString();

        try {
            reader.endObject();
        } catch (Exception e) {
            throw ServiceErrors.invalidPayload("Must ONLY specify an action");
        }

        if(!actionField.equals("action"))
            throw ServiceErrors.invalidPayload("Must specify an action");

        switch(actionValue) {
            case "INIT":
                try {
                    registry.initModuleAsync(module);
                } catch (SensorHubException ex) {
                    throw ServiceErrors.internalError("Failed to reinitialize module " + moduleId);
                }
                break;
            case "START":
                try {
                    registry.startModuleAsync(module);
                } catch (SensorHubException ex) {
                    throw ServiceErrors.internalError("Failed to start module " + moduleId);
                }
                break;
            case "STOP":
                try {
                    registry.stopModuleAsync(module);
                } catch (SensorHubException ex) {
                    throw ServiceErrors.internalError("Failed to stop module " + moduleId);
                }
                break;
            case "RESTART":
                try {
                    registry.restartModuleAsync(module);
                } catch (SensorHubException ex) {
                    throw ServiceErrors.internalError("Failed to restart module " + moduleId);
                }
                break;
            default:
                throw ServiceErrors.invalidPayload("Must specify a valid action (START, STOP, RESTART, or INIT)");
        }
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
