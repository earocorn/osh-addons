package com.botts.api.parser.data;


import org.sensorhub.api.config.DisplayInfo;
import org.vast.swe.SWEHelper;

import java.util.Collection;

public class DataRecordConfig {

    public String label = "Data Feed Output";
    public String name = "dataFeedOutput";
    public String description = "Outputs from data feed driver";
    public String definition = SWEHelper.getPropertyUri("DataFeed");
    @DisplayInfo(label = "Output Fields")
    public Collection<DataComponentConfig> fields;

}
