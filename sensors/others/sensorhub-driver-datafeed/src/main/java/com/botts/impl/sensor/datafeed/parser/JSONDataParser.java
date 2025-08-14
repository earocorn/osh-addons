package com.botts.impl.sensor.datafeed.parser;

import com.botts.api.sensor.datafeed.parser.AbstractDataParser;
import com.botts.api.sensor.datafeed.parser.DataParserConfig;
import com.botts.api.sensor.datafeed.parser.IStreamProcessor;
import com.botts.impl.sensor.datafeed.DataFeedUtils;
import com.botts.impl.sensor.datafeed.data.DataField;
import com.botts.impl.sensor.datafeed.parser.config.JSONDataParserConfig;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gwt.json.client.JSONObject;
import net.opengis.swe.v20.DataBlock;
import net.opengis.swe.v20.DataComponent;

import javax.xml.crypto.Data;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class JSONDataParser extends AbstractDataParser implements IStreamProcessor {

    public JSONDataParser(JSONDataParserConfig config, DataComponent outputStructure) {
        super(config, outputStructure);
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

//        Map<String, Object> dataMap = new HashMap<>();

        for (DataField field : getInputFields()) {
            if (!jsonObject.has(field.name))
                throw new IllegalArgumentException("Field " + field.name + " has no data");

            String rawValue = jsonObject.get(field.name).getAsString();
            Object realValue = DataFeedUtils.parseValue(rawValue, field.dataType);

            DataFeedUtils.setFieldData(getRecordStructure().getComponentIndex(field.name), realValue, dataBlock);

//            dataMap.put(field.name, realValue);
        }

        return dataBlock;
    }

    @Override
    public void processStream(InputStream inputStream, Consumer<DataBlock> consumer) {
        // Process pretty-printed and single line
    }

    @Override
    public void stop() {
        // Stop processing
    }
}
