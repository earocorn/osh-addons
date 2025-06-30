package com.botts.ui;

import org.sensorhub.ui.ObjectTypeSelectionPopup;

import java.util.Map;

public class DataTypeSelectionPopup extends ObjectTypeSelectionPopup {

    public interface ObjectTypeSelectionCallbackWrapper extends ObjectTypeSelectionCallback {
        void onSelected(Class<?> objectType);
    }

    public DataTypeSelectionPopup(String title, Map<String, Class<?>> typeList, ObjectTypeSelectionCallbackWrapper callback) {
        super(title, typeList, callback);
    }

}
