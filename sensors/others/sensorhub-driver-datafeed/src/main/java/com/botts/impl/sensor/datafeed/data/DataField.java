package com.botts.impl.sensor.datafeed.data;

public class DataField {

    public int ordinality;
    public String name;
    public BaseDataType dataType;

    public DataField(int ordinality, String name, String description, String definition, BaseDataType dataType) {
        this.ordinality = ordinality;
        this.name = name;
        this.dataType = dataType;
    }

    public DataField() {}

}
