package com.botts.api.parser.data;

public enum BaseDataType {

    INTEGER,
    STRING,
    BOOLEAN,
    DOUBLE,
    FLOAT,
    LONG,
    BYTE;

    public static BaseDataType getType(Class<?> clazz) {
        if (clazz.equals(Double.class)) {
            return DOUBLE;
        } else if(clazz.equals(Float.class)) {
            return FLOAT;
        } else if(clazz.equals(Integer.class)) {
            return INTEGER;
        } else if(clazz.equals(Long.class)) {
            return LONG;
        } else if(clazz.equals(Byte.class)) {
            return BYTE;
        } else if(clazz.equals(String.class)) {
            return STRING;
        } else if(clazz.equals(Boolean.class)) {
            return BOOLEAN;
        }
        return null;
    }

}
