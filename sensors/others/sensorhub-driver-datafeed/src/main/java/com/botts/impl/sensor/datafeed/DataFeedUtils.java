package com.botts.impl.sensor.datafeed;

import com.botts.impl.sensor.datafeed.data.BaseDataType;
import com.botts.impl.sensor.datafeed.data.DataComponentConfig;
import net.opengis.swe.v20.DataBlock;
import net.opengis.swe.v20.DataComponent;
import net.opengis.swe.v20.DataType;
import net.opengis.swe.v20.ScalarComponent;
import org.vast.swe.SWEBuilders;
import org.vast.swe.SWEHelper;

public class DataFeedUtils {
    static SWEHelper fac = new SWEHelper();

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

    public static SWEBuilders.DataComponentBuilder<? extends SWEBuilders.SimpleComponentBuilder<?,?>, ? extends ScalarComponent> createDataComponent(DataComponentConfig config) {
        if (config == null)
            return null;
        switch (config.dataType) {
            case INTEGER -> {
                return fac.createCount();
            }
            case STRING -> {
                return fac.createText();
            }
            case BOOLEAN -> {
                return fac.createBoolean();
            }
            case LONG -> {
                return fac.createQuantity().dataType(DataType.LONG);
            }
            case DOUBLE -> {
                return fac.createQuantity().dataType(DataType.DOUBLE);
            }
            case FLOAT -> {
                return fac.createQuantity().dataType(DataType.FLOAT);
            }
            case BYTE -> {
                return fac.createQuantity().dataType(DataType.BYTE);
            }
        }
        return null;
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
