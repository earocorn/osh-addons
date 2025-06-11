package com.botts.impl.sensor.datafeed.data;

public class DataStreamField {

    public int cardinality;
    public String name;
    public String description;
    public String definition;
    public BaseDataType dataType;

    public DataStreamField(int cardinality, String name, String description, String definition, BaseDataType dataType) {
        this.cardinality = cardinality;
        this.name = name;
        this.description = description;
        this.definition = definition;
        this.dataType = dataType;
    }

    public DataStreamField() {}

}
