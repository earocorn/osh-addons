package com.botts.impl.utils;

import com.botts.impl.utils.data.DataField;
import com.botts.impl.utils.data.DataRecordConfig;
import com.botts.impl.utils.data.FieldMapping;

import org.sensorhub.api.config.DisplayInfo;
import org.sensorhub.api.module.SubModuleConfig;

import java.util.List;

public abstract class DataParserConfig extends SubModuleConfig {

    @DisplayInfo.Required
    public List<DataField> inputFields;

    @DisplayInfo.Required
    public DataRecordConfig outputStructure = new DataRecordConfig();

    @DisplayInfo.Required
    public List<FieldMapping> fieldMapping;

    @DisplayInfo(desc = "If selected, output will be constructed based on input fields.")
    public boolean useDefaultMapping = false;

    public abstract Class<? extends IDataParser> getDataParserClass();

}
