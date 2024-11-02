package org.sensorhub.impl.service.moduleapi.module;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.sensorhub.api.common.IdEncoders;
import org.sensorhub.api.module.ModuleConfig;
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

    protected ModuleBindingJson(RequestContext ctx, IdEncoders idEncoders, boolean forReading) throws IOException {
        super(ctx, idEncoders, forReading);

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
    public ModuleConfig deserialize(JsonReader reader) throws IOException {
        return null;
    }

    @Override
    public void serialize(String key, ModuleConfig res, boolean showLinks, JsonWriter writer) throws IOException {
        writer.beginObject();
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
