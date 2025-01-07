package org.sensorhub.impl.service.moduleapi.type;

import org.sensorhub.api.module.IModuleProvider;
import org.sensorhub.impl.module.ModuleRegistry;
import org.sensorhub.impl.service.consys.ServiceErrors;
import org.sensorhub.impl.service.consys.resource.RequestContext;
import org.sensorhub.impl.service.moduleapi.ModuleBaseResourceHandler;
import org.sensorhub.impl.service.moduleapi.module.ModuleBindingJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ModuleTypeHandler extends ModuleBaseResourceHandler {

    public static final String[] NAMES = { "types" };
    private static final Logger log = LoggerFactory.getLogger(ModuleTypeHandler.class);
    ModuleRegistry registry;
    Map<String, IModuleProvider> modulesMap;

    public ModuleTypeHandler(ModuleRegistry registry) {
        this.registry = registry;
        this.modulesMap = new HashMap<>();
        for (IModuleProvider i : registry.getInstalledModuleTypes()) {
            modulesMap.put(i.getModuleClass().getCanonicalName(), i);
        }

//        var osgiCtx = registry.getParentHub().getOsgiContext();
//
//        if(osgiCtx == null)
//            return;
//
//        try {
//            // TODO: Query osgi manifest
//            var ref = osgiCtx.getServiceReference(RepositoryAdmin.class);
//            var repo = osgiCtx.getService(ref);
//            var resources = repo.discoverResources("(symbolicname=*)");
//            // TODO: Show OSGi modules available for download
//        } catch (InvalidSyntaxException e) {
//            throw new RuntimeException(e);
//        }
    }

    protected ModuleBindingJson getBinding(RequestContext ctx, boolean forReading) throws IOException {
        return new ModuleBindingJson(ctx, null, forReading, this.registry);
    }

    /**
     * List all available module types
     * @param ctx
     * @throws IOException
     */
    @Override
    protected void list(RequestContext ctx) throws IOException {
        var binding = getBinding(ctx, false);

        /*
        differentiate between installed modules
        and modules available via OSGi
        {
            "installed" : [],
            "osgi": []
        }
         */

        binding.startCollection();

        var allModules = modulesMap.values();

        Collection<IModuleProvider> filteredModules;
        if(!ctx.getParameterMap().isEmpty()) {
            var nameParam = ctx.getParameter("name");
            var versionParam = ctx.getParameter("version");
            var vendorParam = ctx.getParameter("vendor");

            filteredModules = allModules.stream().filter(moduleProvider -> (
                    (nameParam == null || moduleProvider.getModuleName().contains(nameParam)) &&
                    (versionParam == null || moduleProvider.getModuleVersion().contains(versionParam)) &&
                    (vendorParam == null || moduleProvider.getProviderName().contains(vendorParam))
            )).collect(Collectors.toList());
        } else {
            filteredModules = new ArrayList<>(allModules);
        }

        for(IModuleProvider moduleProvider : filteredModules)
            binding.serializeProvider(moduleProvider);

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
        var provider = modulesMap.get(objectClass);
        if(provider == null)
            throw ServiceErrors.notFound(objectClass);

        var binding = getBinding(ctx, false);
        binding.serializeProvider(provider);
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
