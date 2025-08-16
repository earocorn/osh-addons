package com.botts.impl.parser.json;


import com.botts.api.parser.AbstractDataParser;
import com.botts.api.parser.IStreamProcessor;
import com.botts.api.parser.data.BaseDataType;
import com.botts.api.parser.data.DataFeedUtils;
import com.botts.api.parser.data.DataField;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import net.opengis.swe.v20.DataBlock;
import net.opengis.swe.v20.DataComponent;
import org.sensorhub.api.common.SensorHubException;
import org.vast.util.Asserts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class JSONDataParser extends AbstractDataParser implements IStreamProcessor {

    private final ExecutorService executorService;
    JSONDataParserConfig config;

    public JSONDataParser(JSONDataParserConfig config, DataComponent outputStructure) {
        super(config, outputStructure);

        this.config = config;
        this.executorService = Executors.newSingleThreadExecutor();

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
        Asserts.checkNotNull(inputStream, "inputStream");
        Asserts.checkNotNull(consumer, "consumer");

        executorService.submit(() -> {
            if (config.isPretty) {
                JsonReader jsonReader = new JsonReader(new InputStreamReader(inputStream));
                Gson gson = new Gson();

                try{
                    while(jsonReader.hasNext()){
                        JsonObject jsonObject = gson.fromJson(jsonReader, JsonObject.class);
                        if(jsonObject != null){
                            String line = gson.toJson(jsonObject);
                            if(line.isEmpty()) continue;
                            DataBlock dataBlock = parse(line.getBytes());
                            if(dataBlock != null)
                                consumer.accept(dataBlock);
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            } else {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        try {
                            if(line.isEmpty()) continue;
                            DataBlock dataBlock = parse(line.getBytes());
                            if (dataBlock != null) {
                                consumer.accept(dataBlock);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void start() throws SensorHubException {

    }

    @Override
    public void stop() {
        // Stop processing
    }
}
