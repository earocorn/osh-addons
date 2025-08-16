package com.botts.api.parser;


import com.botts.api.parser.data.DataField;
import com.botts.api.parser.data.DataRecordConfig;
import com.botts.api.parser.data.FieldMapping;
import org.checkerframework.checker.units.qual.A;
import org.sensorhub.api.config.DisplayInfo;
import org.sensorhub.api.module.SubModuleConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class DataParserConfig extends SubModuleConfig {

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
