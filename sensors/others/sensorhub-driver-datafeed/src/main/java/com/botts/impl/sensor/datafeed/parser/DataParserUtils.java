//package com.botts.impl.sensor.datafeed.parser;
//
//import com.botts.impl.sensor.datafeed.data.DataStreamField;
//import net.opengis.swe.v20.DataComponent;
//import net.opengis.swe.v20.DataRecord;
//import net.opengis.swe.v20.DataType;
//import org.vast.swe.SWEBuilders;
//import org.vast.swe.SWEHelper;
//
//import java.util.Comparator;
//import java.util.List;
//import java.util.Set;
//
//public class DataParserUtils {
//
//    SWEHelper fac;
//
//    DataParserUtils() {
//        fac = new SWEHelper();
//    }
//
//    DataComponent toDataComponent(Set<DataStreamField> dataStreamFields) {
//        List<DataStreamField> sorted = dataStreamFields.stream()
//                .sorted(Comparator.comparingInt(d -> d.cardinality)).toList();
//        SWEBuilders.DataRecordBuilder record = fac.createRecord();
//        for (DataStreamField dataStreamField : sorted) {
//            record.addField(dataStreamField.name, toDataComponent(dataStreamField));
//        }
//        return record.build();
//    }
//
//    DataComponent toDataComponent(DataStreamField field) {
//        SWEBuilders.DataComponentBuilder<?, ?> builder;
//        switch(field.dataType) {
//            case BYTE -> builder = fac.createQuantity().dataType(DataType.BYTE);
//            case LONG -> builder = fac.createQuantity().dataType(DataType.LONG);
//            case FLOAT -> builder = fac.createQuantity().dataType(DataType.FLOAT);
//            case DOUBLE -> builder = fac.createQuantity().dataType(DataType.DOUBLE);
//            case STRING -> builder = fac.createText();
//            case INTEGER -> builder = fac.createCount();
//        }
//        if(builder == null)
//            return null;
//
//        return builder.definition(field.definition)
//                .description(field.description)
//                .build();
//    }
//
//}
