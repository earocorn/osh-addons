package com.botts.api.sensor.datafeed.parser;

import com.botts.impl.sensor.datafeed.data.DataField;
import net.opengis.swe.v20.DataComponent;
import org.sensorhub.api.config.DisplayInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class DataParserConfig {

    @DisplayInfo.Required
    public List<DataField> inputFields;

    @DisplayInfo.Required
    public DataComponent outputStructure;

    @DisplayInfo.Required
    public HashMap<String, String> fieldMapping = new HashMap<>();

    public abstract Class<? extends IDataParser> getDataParserClass();

}
