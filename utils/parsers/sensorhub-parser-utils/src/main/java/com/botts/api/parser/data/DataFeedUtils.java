package com.botts.api.parser.data;

import net.opengis.swe.v20.DataBlock;
import net.opengis.swe.v20.DataComponent;
import net.opengis.swe.v20.DataType;
import net.opengis.swe.v20.ScalarComponent;
import org.vast.data.*;
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

    public static void setDataBlockField(int index, DataBlock datum, DataBlock dataBlock){
        if (datum instanceof DataBlockInt) {
            setFieldData(index, ((DataBlockInt)datum).getUnderlyingObject()[index], dataBlock);
        } else if (datum instanceof DataBlockDouble) {
            setFieldData(index, ((DataBlockDouble)datum).getUnderlyingObject()[index], dataBlock);
        } else if (datum instanceof DataBlockString) {
            setFieldData(index, ((DataBlockString)datum).getUnderlyingObject()[index], dataBlock);
        } else if (datum instanceof DataBlockBoolean) {
            setFieldData(index, ((DataBlockBoolean)datum).getUnderlyingObject()[index], dataBlock);
        } else if (datum instanceof DataBlockByte) {
            setFieldData(index, ((DataBlockByte)datum).getUnderlyingObject()[index], dataBlock);
        } else if (datum instanceof DataBlockFloat) {
            setFieldData(index, ((DataBlockFloat)datum).getUnderlyingObject()[index], dataBlock);
        } else if (datum instanceof DataBlockLong) {
            setFieldData(index, ((DataBlockLong)datum).getUnderlyingObject()[index], dataBlock);
        } else if (datum instanceof DataBlockShort) {
            setFieldData(index, ((DataBlockShort)datum).getUnderlyingObject()[index], dataBlock);
        } else if (datum instanceof DataBlockParallel) {
            setFieldData(index, ((DataBlockParallel)datum).getUnderlyingObject()[index], dataBlock);
        } else if (datum instanceof DataBlockMixed) {
            setFieldData(index, ((DataBlockMixed)datum).getUnderlyingObject()[index], dataBlock);
        } else if (datum instanceof DataBlockCompressed) {
            setFieldData(index, ((DataBlockCompressed)datum).getUnderlyingObject()[index], dataBlock);
        } else if (datum instanceof DataBlockDateTime) {
            setFieldData(index, ((DataBlockDateTime)datum).getUnderlyingObject()[index], dataBlock);
        } else if (datum instanceof DataBlockTuple) {
            setFieldData(index, ((DataBlockTuple)datum).getUnderlyingObject()[index], dataBlock);
        } else if (datum instanceof DataBlockList) {
            setFieldData(index, ((DataBlockList)datum).get(index), dataBlock);
        } else if (datum instanceof DataBlockProxy) {
            setDataBlockField(index, ((DataBlockProxy)datum).getDataBlock(), dataBlock);
        } else if(datum instanceof DataBlockUShort) {
            setFieldData(index, ((DataBlockUShort)datum).getUnderlyingObject()[index], dataBlock);
        } else if(datum instanceof DataBlockUInt) {
            setFieldData(index, ((DataBlockUInt)datum).getUnderlyingObject()[index], dataBlock);
        } else if(datum instanceof DataBlockUByte) {
            setFieldData(index, ((DataBlockUByte)datum).getUnderlyingObject()[index], dataBlock);
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
            default -> throw new IllegalStateException("Unexpected value: " + config.dataType);
        }
    }

    public static void setComponentData(DataComponent component, Object datum) {
        setFieldData(0, datum, component.getData());
    }

    public static Object parseValue(String rawValue, BaseDataType dataType) {
        try {
            return switch (dataType) {
                case INTEGER -> Integer.parseInt(rawValue);
                case DOUBLE -> Double.parseDouble(rawValue);
                case FLOAT -> Float.parseFloat(rawValue);
                case BYTE -> Byte.parseByte(rawValue);
                case LONG -> Long.parseLong(rawValue);
                case BOOLEAN -> Boolean.parseBoolean(rawValue);
                default -> rawValue;
            };
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse value: " + rawValue + " as " + dataType.name(), e);
        }
    }
}
