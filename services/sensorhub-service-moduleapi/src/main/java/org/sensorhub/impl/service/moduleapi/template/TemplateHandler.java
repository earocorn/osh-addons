package org.sensorhub.impl.service.moduleapi.template;

import org.sensorhub.api.common.SensorHubException;
import org.sensorhub.api.module.IModuleProvider;
import org.sensorhub.api.module.ModuleConfig;
import org.sensorhub.impl.module.ModuleRegistry;
import org.sensorhub.impl.service.consys.ServiceErrors;
import org.sensorhub.impl.service.moduleapi.ModuleBaseResourceHandler;
import org.sensorhub.impl.service.consys.resource.RequestContext;
import org.sensorhub.impl.service.moduleapi.module.ModuleBindingJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TemplateHandler extends ModuleBaseResourceHandler {

    public static final String[] NAMES = { "templates" };
    private static final Logger log = LoggerFactory.getLogger(TemplateHandler.class);
    ModuleRegistry registry;
    Map<String, ModuleConfig> modulesMap;

    public TemplateHandler(ModuleRegistry registry) {
        this.registry = registry;
        this.modulesMap = new HashMap<>();
        var installedModules = registry.getInstalledModuleTypes().iterator();
        while(installedModules.hasNext()) {
            var i = installedModules.next();
            try {
                modulesMap.put(i.getModuleClass().getCanonicalName(), registry.createModuleConfig(i));
            } catch (SensorHubException e) {
                log.error("Failed to create module config " + i.getModuleClass().getCanonicalName(), e);
            }
        }
    }

    protected ModuleBindingJson getBinding(RequestContext ctx, boolean forReading) throws IOException {
        return new ModuleBindingJson(ctx, null, forReading, this.registry);
    }

    /**
     * List all available module configuration templates
     * @param ctx
     * @throws IOException
     */
    @Override
    protected void list(RequestContext ctx) throws IOException {
        var binding = getBinding(ctx, false);
        binding.startCollection();

        for(Map.Entry<String, ModuleConfig> entry : modulesMap.entrySet())
            binding.serializeConfig(entry.getValue());

        binding.endCollection();
    }

    /**
     * Retrieve module configuration template by its object class
     * @param ctx
     * @param objectClass
     * @throws IOException
     */
    @Override
    protected void getById(RequestContext ctx, String objectClass) throws IOException {
        var config = modulesMap.get(objectClass);
        if(config == null)
            throw ServiceErrors.notFound(objectClass);

        var binding = getBinding(ctx, false);
        binding.serializeConfig(config);
    }

    @Override
    protected void create(RequestContext ctx) throws IOException {
        throw new UnsupportedOperationException("Operation not supported");
    }

    @Override
    protected void update(RequestContext ctx, String id) throws IOException {
        throw new UnsupportedOperationException("Operation not supported");
    }

    @Override
    protected void delete(RequestContext ctx, String id) throws IOException {
        throw new UnsupportedOperationException("Operation not supported");
    }

    @Override
    public String[] getNames() {
        return NAMES;
    }
}
