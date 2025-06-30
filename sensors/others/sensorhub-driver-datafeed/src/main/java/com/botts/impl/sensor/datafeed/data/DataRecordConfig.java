package com.botts.impl.sensor.datafeed.data;

import net.opengis.swe.v20.DataRecord;
import org.sensorhub.api.config.DisplayInfo;
import org.vast.swe.SWEHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DataRecordConfig {

    public String label = "Data Feed Output";
    public String name = "dataFeedOutput";
    public String description = "Outputs from data feed driver";
    public String definition = SWEHelper.getPropertyUri("DataFeed");
    @DisplayInfo(label = "Output Fields")
    public Collection<DataComponentConfig> fields;

}
