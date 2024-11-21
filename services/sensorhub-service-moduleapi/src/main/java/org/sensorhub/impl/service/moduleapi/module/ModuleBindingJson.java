package org.sensorhub.impl.service.moduleapi.module;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.sensorhub.api.ISensorHub;
import org.sensorhub.api.common.IdEncoders;
import org.sensorhub.api.common.SensorHubException;
import org.sensorhub.api.module.IModule;
import org.sensorhub.api.module.IModuleManager;
import org.sensorhub.api.module.ModuleConfig;
import org.sensorhub.api.module.ModuleEvent;
import org.sensorhub.impl.SensorHub;
import org.sensorhub.impl.module.ModuleRegistry;
import org.sensorhub.impl.service.moduleapi.util.ModuleConfigUtil;
import org.sensorhub.impl.service.sweapi.resource.RequestContext;
import org.sensorhub.impl.service.sweapi.resource.ResourceBindingJson;
import org.sensorhub.impl.service.sweapi.resource.ResourceLink;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public class ModuleBindingJson extends ResourceBindingJson<String, ModuleConfig> {

    protected final JsonReader reader;
    protected final JsonWriter writer;
    protected final Gson gson;
    protected final ModuleRegistry registry;

    protected ModuleBindingJson(RequestContext ctx, IdEncoders idEncoders, boolean forReading, ModuleRegistry registry) throws IOException {
        super(ctx, idEncoders, forReading);

        this.gson = new ModuleConfigUtil().gson;
        this.registry = registry;

        if(forReading) {
            InputStream is = new BufferedInputStream(ctx.getInputStream());
            this.reader = getJsonReader(is);
            this.writer = null;
        } else {
            this.reader = null;
            this.writer = getJsonWriter(ctx.getOutputStream(), ctx.getPropertyFilter());
        }
    }

    @Override
    public ModuleConfig deserialize(JsonReader reader) {
        return gson.fromJson(reader, ModuleConfig.class);
    }

    public ModuleConfig deserialize() {
        return deserialize(reader);
    }

    @Override
    public void serialize(String key, ModuleConfig res, boolean showLinks, JsonWriter writer) throws IOException {
        /*
        {
            config: {
                objClass: "",
                name: "",
                id: "",
                ...
            },
            state: "STARTED"
        }
         */
        try {
            var module = this.registry.getModuleById(res.id);
            writer.beginObject();
            writer.name("id").value(module.getConfiguration().id);
            writer.name("name").value(module.getConfiguration().name);
            writer.name("description").value(module.getConfiguration().description);
            writer.name("state").value(module.getCurrentState().name());
            writer.name("statusMessage").value(module.getStatusMessage() != null ? module.getStatusMessage() : "NONE");
            writer.name("latestError").value(module.getCurrentError() != null ? module.getCurrentError().getMessage() : "NONE");
            writer.endObject();
            writer.flush();
        } catch (SensorHubException e) {
            throw new RuntimeException(e);
        }
    }

    public void serialize(ModuleConfig module) throws IOException {
        serialize(null, module, false, writer);
    }

    public void serializeConfig(ModuleConfig config) throws IOException {
        gson.toJson(config, ModuleConfig.class, writer);
        writer.flush();
    }

    @Override
    public void startCollection() throws IOException {
        startJsonCollection(writer);
    }

    @Override
    public void endCollection(Collection<ResourceLink> links) throws IOException {
        endJsonCollection(writer, null);
    }

    public void endCollection() throws IOException {
        endCollection(null);
    }
}
