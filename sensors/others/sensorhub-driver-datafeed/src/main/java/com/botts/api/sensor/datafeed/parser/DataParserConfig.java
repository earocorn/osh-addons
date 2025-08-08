package com.botts.api.sensor.datafeed.parser;

import com.botts.impl.sensor.datafeed.data.DataRecordConfig;
import com.botts.impl.sensor.datafeed.data.FieldMapping;
import com.botts.impl.sensor.datafeed.data.DataField;
import org.sensorhub.api.config.DisplayInfo;

import java.util.Collection;

public abstract class DataParserConfig {

    @DisplayInfo.Required
    public Collection<DataField> inputFields;

    @DisplayInfo.Required
    public DataRecordConfig outputStructure = new DataRecordConfig();

    @DisplayInfo.Required
    public Collection<FieldMapping> fieldMapping;

    public abstract Class<? extends IDataParser> getDataParserClass();

}
