package com.botts.impl.json;


import com.botts.impl.utils.AbstractDataParser;
import com.botts.impl.utils.IStreamProcessor;
import com.botts.impl.utils.data.BaseDataType;
import com.botts.impl.utils.data.DataFeedUtils;
import com.botts.impl.utils.data.DataField;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import net.opengis.swe.v20.DataBlock;
import net.opengis.swe.v20.DataComponent;
import org.sensorhub.api.common.SensorHubException;

import java.io.InputStream;
import java.util.function.Consumer;

public class JSONDataParser extends AbstractDataParser implements IStreamProcessor {

    public JSONDataParser(JSONDataParserConfig config, DataComponent outputStructure) {
        super(config, outputStructure);
    }

    public static Object findInJsonObject(JsonObject root, String key, BaseDataType dataType) {
        if (root.has(key))
            return DataFeedUtils.parseValue(root.get(key).getAsString(), dataType);

        for (String objKey : root.keySet()) {
            if (root.get(objKey).isJsonObject()) {
                JsonObject object = (JsonObject) root.get(objKey);
                return  findInJsonObject(object, key, dataType);
            }
        }
        return null;
    }

    @Override
    public DataBlock parse(byte[] data) {
        DataBlock dataBlock = getRecordStructure().createDataBlock();
        String jsonString = new String(data);
        JsonObject jsonObject;
        try {
            jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException("Illegal JSON data: " + jsonString, e);
        }

        if (jsonObject == null)
            return dataBlock;

        for (DataField field : getInputFields()) {
            if (!jsonObject.has(field.name))
                throw new IllegalArgumentException("Field " + field.name + " has no data");

            String rawValue = jsonObject.get(field.name).getAsString();
            Object realValue = DataFeedUtils.parseValue(rawValue, field.dataType);

            DataFeedUtils.setFieldData(getRecordStructure().getComponentIndex(field.name), realValue, dataBlock);
        }

        return dataBlock;
    }

    @Override
    public void processStream(InputStream inputStream, Consumer<DataBlock> consumer) {
        // Process pretty-printed and single line
    }

    @Override
    public void start() throws SensorHubException {

    }

    @Override
    public void stop() {
        // Stop processing
    }
}
