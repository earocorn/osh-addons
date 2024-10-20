package org.sensorhub.impl.service.moduleapi.module;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import org.sensorhub.api.common.SensorHubException;
import org.sensorhub.api.module.IModule;
import org.sensorhub.api.module.IModuleProvider;
import org.sensorhub.api.module.ModuleConfig;
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

    ModuleRegistry registry;
    Collection<IModule<?>> loadedModules;
    HashMap<String, IModuleProvider> availableModuleTypes;
    Gson gson;

    public ModuleHandler(ModuleRegistry registry) {
        this.registry = registry;
        this.loadedModules = registry.getLoadedModules();
        var installedModules = registry.getInstalledModuleTypes();
        this.availableModuleTypes = new HashMap<>();
        for(var i : installedModules) {
            availableModuleTypes.put(i.getModuleName(), i);
        }

        ModuleConfigUtil moduleConfigUtil = new ModuleConfigUtil();
        gson = moduleConfigUtil.gson;
    }

    @Override
    public String[] getNames() {
        return NAMES;
    }

    @Override
    public void doGet(RequestContext ctx) throws InvalidRequestException, IOException, SecurityException {
        // return all modules
        System.out.println("Loaded modules: ");
        for(var i : loadedModules) {
            System.out.println(i.getName());
        }

        System.out.println("\nInstalled Modules: ");
        for(var i : availableModuleTypes.values()) {
            System.out.println(i.getModuleName() + "\n");
            try {
                var clazz = i.getModuleConfigClass().getDeclaredConstructor().newInstance();
                for(var field : clazz.getClass().getDeclaredFields()) {
                    System.out.println(field.getName());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void doPost(RequestContext ctx) throws InvalidRequestException, IOException, SecurityException {
        // add new module
        InputStream is = new BufferedInputStream(ctx.getInputStream());
        var reader = new JsonReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        reader.beginObject();
        String driverField = reader.nextName();
        String driverValue = reader.nextString();

        if(!driverField.equals("driverName"))
            throw ServiceErrors.invalidPayload("Must specify a driver name");

        if(availableModuleTypes.get(driverValue) == null)
            throw ServiceErrors.notFound(driverValue);

        System.out.println("Found module with name: " + driverValue);

        String configField = reader.nextName();

        if(!configField.equals("config"))
            throw ServiceErrors.invalidPayload("Must specify a configuration for " + driverValue);

        ModuleConfig moduleConfig = gson.fromJson(reader, ModuleConfig.class);

        if(moduleConfig == null)
            throw ServiceErrors.notFound("config");

        var mod = registry.getLoadedModuleById(moduleConfig.id);

        if(registry.isModuleLoaded(moduleConfig.id))
            throw ServiceErrors.requestRejected("Module already loaded");

        try {
            registry.loadModule(moduleConfig);
        } catch (Exception e) {
            throw ServiceErrors.badRequest(e.getMessage());
        }

//        try {
////            Thread.sleep(500);
//            registry.destroyModule(moduleConfig.id);
//            System.out.println("Destroyed problematic module");
//        } catch (Exception ex) {
//            throw new RuntimeException(ex);
//        }

        if(mod != null)
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

}
