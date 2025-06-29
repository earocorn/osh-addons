package com.botts.api.sensor.datafeed.parser;

import com.botts.impl.sensor.datafeed.data.FieldMapping;
import com.botts.impl.sensor.datafeed.data.DataField;
import net.opengis.swe.v20.DataComponent;
import org.sensorhub.api.config.DisplayInfo;

import java.util.Collection;

public abstract class DataParserConfig {

    @DisplayInfo.Required
    public Collection<DataField> inputFields;

    @DisplayInfo.Required
    public DataComponent outputStructure;

    @DisplayInfo.Required
    @DisplayInfo.FieldType(DisplayInfo.FieldType.Type.TABLE)
    public Collection<FieldMapping> fieldMapping;

    public abstract Class<? extends IDataParser> getDataParserClass();

}
