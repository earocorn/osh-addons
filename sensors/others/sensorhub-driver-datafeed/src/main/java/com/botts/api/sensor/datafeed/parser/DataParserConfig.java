package com.botts.api.sensor.datafeed.parser;

import com.botts.impl.sensor.datafeed.data.DataRecordConfig;
import com.botts.impl.sensor.datafeed.data.FieldMapping;
import com.botts.impl.sensor.datafeed.data.DataField;
import org.sensorhub.api.config.DisplayInfo;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class DataParserConfig {

    @DisplayInfo.Required
    public List<DataField> inputFields = new ArrayList<>();

    @DisplayInfo.Required
    public DataRecordConfig outputStructure = new DataRecordConfig();

    @DisplayInfo.Required
    public List<FieldMapping> fieldMapping = new ArrayList<>();

    @DisplayInfo(desc = "If selected, output will be constructed based on input fields.")
    public boolean useDefaultMapping = false;

    public abstract Class<? extends IDataParser> getDataParserClass();

}
