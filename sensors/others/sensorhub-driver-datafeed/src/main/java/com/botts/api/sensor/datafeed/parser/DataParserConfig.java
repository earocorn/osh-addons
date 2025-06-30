package com.botts.api.sensor.datafeed.parser;

import com.botts.impl.sensor.datafeed.data.BaseDataType;
import com.botts.impl.sensor.datafeed.data.DataRecordConfig;
import com.botts.impl.sensor.datafeed.data.FieldMapping;
import com.botts.impl.sensor.datafeed.data.DataField;
import net.opengis.swe.v20.DataComponent;
import org.sensorhub.api.config.DisplayInfo;
import org.vast.data.DataRecordImpl;
import org.vast.swe.SWEBuilders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class DataParserConfig {

    @DisplayInfo.Required
    public Collection<DataField> inputFields;

    @DisplayInfo.Required
    public DataRecordConfig outputStructure = new DataRecordConfig();

    @DisplayInfo.Required
    public Collection<FieldMapping> fieldMapping;

    public abstract Class<? extends IDataParser> getDataParserClass();

}
