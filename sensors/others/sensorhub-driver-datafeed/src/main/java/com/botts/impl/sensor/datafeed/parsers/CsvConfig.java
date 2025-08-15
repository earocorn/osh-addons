package com.botts.impl.sensor.datafeed.parsers;

import com.botts.impl.utils.CSVDataParserConfig;
import org.sensorhub.api.config.DisplayInfo;

public class CsvConfig extends ParserConfig{
    @DisplayInfo(desc = "csv")
    public CSVDataParserConfig parserConfig;
}
