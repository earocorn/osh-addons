package com.botts.impl.sensor.datafeed;

import com.botts.impl.sensor.datafeed.data.BaseDataType;
import net.opengis.swe.v20.DataBlock;
import net.opengis.swe.v20.DataComponent;

public class DataFeedUtils {
    public static void setFieldData(int index, Object datum, DataBlock dataBlock) {
        if (datum instanceof Integer) {
            dataBlock.setIntValue(index, (Integer) datum);
        } else if (datum instanceof Double) {
            dataBlock.setDoubleValue(index, (Double) datum);
        } else if (datum instanceof String) {
            dataBlock.setStringValue(index, (String) datum);
        } else if (datum instanceof Boolean) {
            dataBlock.setBooleanValue(index, (Boolean) datum);
        } else if (datum instanceof Byte) {
            dataBlock.setByteValue(index, (Byte) datum);
        } else if (datum instanceof Float) {
            dataBlock.setFloatValue(index, (Float) datum);
        } else if (datum instanceof Long) {
            dataBlock.setLongValue(index, (Long) datum);
        } else if (datum instanceof Short) {
            dataBlock.setShortValue(index, (Short) datum);
        }
    }

    public static void setComponentData(DataComponent component, Object datum) {
        setFieldData(0, datum, component.getData());
    }

    public static Object parseValue(String rawValue, BaseDataType dataType) {
        try {
            switch (dataType) {
                case INTEGER: return Integer.parseInt(rawValue);
                case DOUBLE: return Double.parseDouble(rawValue);
                case FLOAT: return Float.parseFloat(rawValue);
                case BYTE: return Byte.parseByte(rawValue);
                case LONG: return Long.parseLong(rawValue);
                case BOOLEAN: return Boolean.parseBoolean(rawValue);
                case STRING:
                default: return rawValue;
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse value: " + rawValue + " as " + dataType.name(), e);
        }
    }
}
