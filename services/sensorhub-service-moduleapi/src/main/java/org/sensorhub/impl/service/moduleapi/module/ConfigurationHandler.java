package org.sensorhub.impl.service.moduleapi.module;

import com.google.gson.stream.JsonReader;
import org.sensorhub.api.module.IModule;
import org.sensorhub.api.module.ModuleConfig;
import org.sensorhub.impl.module.AbstractModule;
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

public class ConfigurationHandler extends BaseHandler {

    public static String[] NAMES = new String[] { "configuration" };

    ModuleRegistry registry;

    public ConfigurationHandler(final ModuleRegistry registry) {
        this.registry = registry;
    }

    protected ModuleBindingJson getBinding(RequestContext ctx, boolean forReading) throws IOException {
        return new ModuleBindingJson(ctx, null, forReading, this.registry);
    }

    @Override
    public String[] getNames() {
        return NAMES;
    }

    @Override
    public void doGet(RequestContext ctx) throws InvalidRequestException, IOException, SecurityException {
        // TODO: Get current module configuration
        IModule<?> module = ModuleHandler.validatePathModule(ctx, this.registry);

        var binding = getBinding(ctx, false);

        binding.serializeConfig(module.getConfiguration());
    }

    @Override
    public void doPost(RequestContext ctx) throws InvalidRequestException, IOException, SecurityException {
        throw ServiceErrors.unsupportedOperation("This endpoint only supports GET and PUT");
    }

    @Override
    public void doPut(RequestContext ctx) throws InvalidRequestException, IOException, SecurityException {
        // TODO: Check if path is valid module
        // TODO: Get current module configuration
        // TODO: Get module configuration in request body
        // TODO: Update module configuration
        var module = ModuleHandler.validatePathModule(ctx, this.registry);

        var binding = getBinding(ctx, true);
        ModuleConfig body = binding.deserialize();

        // Check module class and module class from request are same
        // TODO: Better check
        if(!module.getConfiguration().moduleClass.equals(body.moduleClass))
            throw ServiceErrors.invalidPayload("Cannot edit a module's moduleClass");

        try {
            registry.updateModuleConfigAsync(body);
        } catch (Exception e) {
            throw ServiceErrors.internalError("Error updating module configuration");
        }
    }

    @Override
    public void doDelete(RequestContext ctx) throws InvalidRequestException, IOException, SecurityException {
        throw ServiceErrors.unsupportedOperation("This endpoint only supports GET and PUT");
    }
}
