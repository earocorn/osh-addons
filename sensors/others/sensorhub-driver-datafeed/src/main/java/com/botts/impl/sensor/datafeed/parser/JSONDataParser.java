package com.botts.impl.sensor.datafeed.parser;

import com.botts.api.sensor.datafeed.parser.AbstractDataParser;
import com.botts.api.sensor.datafeed.parser.DataParserConfig;
import com.botts.impl.sensor.datafeed.DataFeedUtils;
import com.botts.impl.sensor.datafeed.data.DataField;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gwt.json.client.JSONObject;
import net.opengis.swe.v20.DataComponent;

import java.util.HashMap;
import java.util.Map;

public class JSONDataParser extends AbstractDataParser {

    public JSONDataParser(DataParserConfig config, DataComponent outputStructure) {
        super(config, outputStructure);
    }

    @Override
    public Map<String, Object> parse(byte[] data) {
        String jsonString = new String(data);
        JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();

        Map<String, Object> dataMap = new HashMap<>();

        for (DataField field : getInputFields()) {
            if (!jsonObject.has(field.name))
                throw new IllegalArgumentException("Field " + field.name + " has no data");

            String rawValue = String.valueOf(jsonObject.get(field.name));
            Object realValue = DataFeedUtils.parseValue(rawValue, field.dataType);
            dataMap.put(field.name, realValue);
        }

        return dataMap;
    }
}
