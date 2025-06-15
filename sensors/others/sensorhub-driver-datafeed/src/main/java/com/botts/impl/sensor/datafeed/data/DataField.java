package com.botts.impl.sensor.datafeed.data;

public class DataField {

    public int cardinality;
    public String name;
    public BaseDataType dataType;

    public DataField(int cardinality, String name, String description, String definition, BaseDataType dataType) {
        this.cardinality = cardinality;
        this.name = name;
        this.dataType = dataType;
    }

    public DataField() {}

}
