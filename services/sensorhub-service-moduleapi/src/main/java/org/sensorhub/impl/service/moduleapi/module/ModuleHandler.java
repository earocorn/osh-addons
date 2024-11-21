package org.sensorhub.impl.service.moduleapi.module;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.sensorhub.api.common.SensorHubException;
import org.sensorhub.api.module.IModule;
import org.sensorhub.api.module.IModuleManager;
import org.sensorhub.api.module.IModuleProvider;
import org.sensorhub.api.module.ModuleConfig;
import org.sensorhub.api.processing.IProcessModule;
import org.sensorhub.api.sensor.ISensorModule;
import org.sensorhub.impl.datastore.DataStoreFiltersTypeAdapterFactory;
import org.sensorhub.impl.module.ModuleConfigJsonFile;
import org.sensorhub.impl.module.ModuleRegistry;
import org.sensorhub.impl.service.moduleapi.util.ModuleConfigUtil;
import org.sensorhub.impl.service.sweapi.BaseHandler;
import org.sensorhub.impl.service.sweapi.InvalidRequestException;
import org.sensorhub.impl.service.sweapi.ServiceErrors;
import org.sensorhub.impl.service.sweapi.resource.RequestContext;
import org.sensorhub.impl.service.sweapi.stream.StreamHandler;

import java.io.*;
import java.nio.Buffer;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;

public class ModuleHandler extends BaseHandler {

    public static final String[] NAMES = { "modules" };

    Collection<IModule<?>> loadedModules;
    HashMap<String, IModuleProvider> availableModuleTypes;
    ModuleRegistry registry;

    public ModuleHandler(ModuleRegistry registry) {
        this.registry = registry;
        this.loadedModules = registry.getLoadedModules();
        var installedModules = registry.getInstalledModuleTypes();
        this.availableModuleTypes = new HashMap<>();
        for(var i : installedModules) {
            var moduleName = i.getModuleName();
            var moduleClass = i.getModuleClass();
            var moduleVersion = i.getModuleVersion();
            var config = i.getModuleConfigClass();
            var className = moduleClass.getName();
            var module = moduleClass.getModule();
            var configFields = config.getFields();
            availableModuleTypes.put(i.getModuleName(), i);
        }
    }

    private ModuleBindingJson getBinding(RequestContext ctx, boolean forReading) throws IOException {
        return new ModuleBindingJson(ctx, null, forReading, this.registry);
    }

    @Override
    public void doGet(RequestContext ctx) throws IOException, SecurityException {
        if (ctx.isEndOfPath())
        {
            list(ctx);
            return;
        }

        // otherwise there should be a specific collection ID
        String id = ctx.popNextPathElt();
        if (ctx.isEndOfPath())
        {
            try {
                getById(ctx, id);
            } catch (SensorHubException e) {
                throw new InvalidRequestException(InvalidRequestException.ErrorCode.NOT_FOUND, "The requested module could not be found");
            }
        }
    }

    private void list(RequestContext ctx) throws IOException {
        var binding = getBinding(ctx, false);
        binding.startCollection();
        for(IModule<?> module : this.registry.getLoadedModules()) {
            // TODO: Take these from service configuration
            if(module instanceof ISensorModule<?> || module instanceof IProcessModule)
                binding.serialize(module.getConfiguration());
        }
        binding.endCollection();
    }

    private void getById(RequestContext ctx, String id) throws SensorHubException, IOException {
        // get module config by id
        var binding = getBinding(ctx, false);
        IModule<?> module = this.registry.getModuleById(id);
        if(module == null)
            throw new SensorHubException("Module not found");
        binding.serialize(module.getConfiguration());
    }

    @Override
    public void doPost(RequestContext ctx) throws InvalidRequestException, IOException, SecurityException {
        // add new module
        var binding = getBinding(ctx, true);

        ModuleConfig moduleConfig = binding.deserialize();

        if(moduleConfig == null)
            throw new InvalidRequestException(InvalidRequestException.ErrorCode.BAD_PAYLOAD, "A valid configuration must be specified");

        boolean isNew = !registry.isModuleLoaded(moduleConfig.id);

        if(!isNew)
            throw ServiceErrors.requestRejected("Module already loaded");

        IModule<?> newModule = null;

        try {
            // TODO: If module fails to load, send error response and delete it from node/config
            newModule = registry.loadModule(moduleConfig);
        } catch (Exception e) {
            throw ServiceErrors.badRequest(e.getMessage());
        }

        if(newModule != null)
            System.out.println("Loaded module: " + moduleConfig.id);
    }

    @Override
    public void doPut(RequestContext ctx) throws InvalidRequestException, IOException, SecurityException {
        // update module config
    }

    @Override
    public void doDelete(RequestContext ctx) throws InvalidRequestException, IOException, SecurityException {
        // remove module
    }

    @Override
    public String[] getNames() {
        return NAMES;
    }

}
