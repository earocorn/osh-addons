package com.botts.api.sensor.datafeed.parser;

import com.botts.impl.sensor.datafeed.data.BaseDataType;
import com.botts.impl.sensor.datafeed.data.FieldMapping;
import com.botts.impl.sensor.datafeed.data.DataField;
import net.opengis.swe.v20.DataComponent;
import org.sensorhub.api.config.DisplayInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class DataParserConfig {

    @DisplayInfo.Required
    @DisplayInfo.FieldType(DisplayInfo.FieldType.Type.TABLE)
    public Collection<DataField> inputFields = getInputFields();

    private Collection<DataField> getInputFields() {
        var l = new ArrayList<DataField>();
        var df1 = new DataField();
        df1.cardinality = 0;
        df1.name = "temperature";
        df1.dataType = BaseDataType.DOUBLE;
        l.add(df1);
        return l;
    }

    @DisplayInfo.Required
    public DataComponent outputStructure;

    @DisplayInfo.Required
    @DisplayInfo.FieldType(DisplayInfo.FieldType.Type.TABLE)
    public Collection<FieldMapping> fieldMapping = getFieldMapping();

    private Collection<FieldMapping> getFieldMapping() {
        var l = new ArrayList<FieldMapping>();
        var fm1 = new FieldMapping();
        fm1.inputFieldName = "temperature";
        fm1.outputFieldName = "temperatureOutput";
        l.add(fm1);
        return l;
    }

    public abstract Class<? extends IDataParser> getDataParserClass();

}
