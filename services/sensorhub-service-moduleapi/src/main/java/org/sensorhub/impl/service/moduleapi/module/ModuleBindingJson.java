package org.sensorhub.impl.service.moduleapi.module;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.sensorhub.api.ISensorHub;
import org.sensorhub.api.common.IdEncoders;
import org.sensorhub.api.common.SensorHubException;
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
        try{
            writer.beginObject();
            writer.name("config").value(gson.toJson(res));

            var module = this.registry.getModuleById(res.id);
            writer.name("state").value(module.getCurrentState().name());
            writer.name("statusMessage").value(module.getStatusMessage());
            writer.name("latestError").value(module.getCurrentError().getMessage());
        } catch (SensorHubException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void startCollection() throws IOException {
        startJsonCollection(writer);
    }

    @Override
    public void endCollection(Collection<ResourceLink> links) throws IOException {
        endJsonCollection(writer, null);
    }
}
