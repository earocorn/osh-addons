package org.sensorhub.impl.service.moduleapi.module;

import org.sensorhub.api.module.IModule;
import org.sensorhub.api.module.IModuleProvider;
import org.sensorhub.api.module.ModuleConfig;
import org.sensorhub.api.processing.IProcessModule;
import org.sensorhub.api.sensor.ISensorModule;
import org.sensorhub.impl.module.ModuleRegistry;
import org.sensorhub.impl.service.consys.InvalidRequestException;
import org.sensorhub.impl.service.consys.ServiceErrors;
import org.sensorhub.impl.service.consys.resource.RequestContext;
import org.sensorhub.impl.service.moduleapi.ModuleBaseResourceHandler;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;

public class ModuleHandler extends ModuleBaseResourceHandler {

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
//            var moduleName = i.getModuleName();
//            var moduleClass = i.getModuleClass();
//            var moduleVersion = i.getModuleVersion();
//            var config = i.getModuleConfigClass();
//            var className = moduleClass.getName();
//            var module = moduleClass.getModule();
//            var configFields = config.getFields();
            availableModuleTypes.put(i.getModuleName(), i);
        }
    }

    protected ModuleBindingJson getBinding(RequestContext ctx, boolean forReading) throws IOException {
        return new ModuleBindingJson(ctx, null, forReading, this.registry);
    }

    public static IModule<?> validatePathModule(RequestContext ctx, ModuleRegistry registry) throws InvalidRequestException {
        if(ctx.isEndOfPath() && !(ctx.getRequestPath().contains(ModuleHandler.NAMES[0])))
            throw ServiceErrors.unsupportedOperation("Configuration can only be retrieved from a module");

        String[] path = ctx.getRequestPath().split("/");
        String moduleId = path[path.length - 2];
        IModule<?> module;

        try {
            module = registry.getModuleById(moduleId);
            if(module == null)
                throw ServiceErrors.notFound(moduleId);
        } catch (Exception e) {
            throw ServiceErrors.notFound(moduleId);
        }
        return module;
    }

    @Override
    public void list(RequestContext ctx) throws IOException {
        var binding = getBinding(ctx, false);
        binding.startCollection();
        for(IModule<?> module : this.registry.getLoadedModules()) {
            // TODO: Take these from service configuration
            if(module instanceof ISensorModule<?> || module instanceof IProcessModule)
                binding.serialize(module.getConfiguration());
        }
        binding.endCollection();
    }

    @Override
    public void getById(RequestContext ctx, String id) throws IOException {
        // get module config by id
        var binding = getBinding(ctx, false);
        IModule<?> module;
        try {
            module = this.registry.getModuleById(id);
            binding.serialize(module.getConfiguration());
        } catch (Exception e) {
            throw new FileNotFoundException("Module not found");
        }
    }

    @Override
    public void create(RequestContext ctx) throws InvalidRequestException, IOException, SecurityException {
        if (!ctx.isEndOfPath())
            return;

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
    protected void update(RequestContext ctx, String id) throws IOException {
        throw ServiceErrors.unsupportedOperation("");
    }

    @Override
    protected void delete(RequestContext ctx, String id) throws IOException {
        // TODO: Implement module deletion logic
        throw ServiceErrors.unsupportedOperation("");
    }

    @Override
    public String[] getNames() {
        return NAMES;
    }
}
