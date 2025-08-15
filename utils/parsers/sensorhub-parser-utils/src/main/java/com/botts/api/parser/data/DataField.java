package com.botts.api.parser.data;


public class DataField {

    public int index;
    public String name;
    public BaseDataType dataType;

    public DataField(int index, String name, String description, String definition, BaseDataType dataType) {
        this.index = index;
        this.name = name;
        this.dataType = dataType;
    }

    public DataField() {}

    public DataField(int index, String name, BaseDataType dataType) {
        this.index = index;
        this.name = name;
        this.dataType = dataType;
    }

}
