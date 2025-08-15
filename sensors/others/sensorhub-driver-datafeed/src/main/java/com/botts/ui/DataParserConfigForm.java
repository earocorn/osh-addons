package com.botts.ui;

import com.botts.api.parser.DataParserConfig;
import com.vaadin.v7.data.Property;
import com.vaadin.v7.ui.Field;
import org.sensorhub.ui.GenericConfigForm;
import org.sensorhub.ui.api.UIConstants;

import java.util.*;

public class DataParserConfigForm extends GenericConfigForm {

    @Override
    protected Field<?> buildAndBindField(String label, String propId, Property<?> prop) {
        Field<?> field = super.buildAndBindField(label, propId, prop);

        if (propId.endsWith(UIConstants.PROP_ID))
            field.setVisible(false);
        else if (propId.endsWith(UIConstants.PROP_NAME))
            field.setVisible(false);
        else if (propId.endsWith(UIConstants.PROP_AUTOSTART))
            field.setVisible(false);
        else if (propId.endsWith(UIConstants.PROP_MODULECLASS))
            field.setCaption("Provider Class");

        return field;
    }

}
