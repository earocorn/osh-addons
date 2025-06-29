package com.botts.ui;

import com.botts.api.sensor.datafeed.parser.DataParserConfig;
import com.vaadin.event.Action;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.*;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.fieldgroup.FieldGroup;
import com.vaadin.v7.data.util.converter.Converter;
import com.vaadin.v7.ui.Table;
import com.vaadin.v7.ui.TreeTable;
import org.sensorhub.api.security.IPermission;
import org.sensorhub.api.security.IPermissionPath;
import org.sensorhub.impl.security.BasicSecurityRealmConfig;
import org.sensorhub.impl.security.PermissionSetting;
import org.sensorhub.ui.BasicSecurityConfigForm;
import org.sensorhub.ui.GenericConfigForm;
import org.sensorhub.ui.ValueEntryPopup;
import org.sensorhub.ui.data.BaseProperty;
import org.sensorhub.ui.data.ComplexProperty;
import org.sensorhub.ui.data.ContainerProperty;
import org.sensorhub.ui.data.MyBeanItem;

import java.util.*;

public class DataFeedConfigForm extends GenericConfigForm {

    private static final String DRIVER_CONFIG_PACKAGE = "com.botts.impl.sensor.datafeed.";
    private static final String PROP_PARSER_CONFIG = "dataParserConfig";
    private static final String PROP_FIELD_MAPPING = "fieldMapping";

    @Override
    public Map<String, Class<?>> getPossibleTypes(String propId, BaseProperty<?> prop) {
        Map<String, Class<?>> classList = new LinkedHashMap<>();
        if(propId.equals(PROP_PARSER_CONFIG)) {
            ServiceLoader<DataParserConfig> sl = ServiceLoader.load(DataParserConfig.class);
            var it = sl.iterator();

            while (it.hasNext())
            {
                try
                {
                    DataParserConfig parserConfig = it.next();
                    classList.put(parserConfig.getDataParserClass().getSimpleName(), parserConfig.getClass());
                }
                catch (ServiceConfigurationError e)
                {
                    getOshLogger().error("{}: {}", ServiceConfigurationError.class.getName(), e.getMessage());
                }
            }
        }

        if (!classList.isEmpty())
            return classList;

        return super.getPossibleTypes(propId, prop);
    }

    @Override
    public void build(String propId, ComplexProperty prop, boolean includeSubForms) {
        buildTable(this);
        super.build(propId, prop, includeSubForms);
    }

    @Override
    protected Component buildTable(String propId, ContainerProperty prop, Class<?> eltType) {
        if (propId.equals(PROP_PARSER_CONFIG + GenericConfigForm.PROP_SEP + PROP_FIELD_MAPPING)) {
            System.out.println("HELLO");
        }
        return super.buildTable(propId, prop, eltType);
    }

    private void buildTable(VerticalLayout layout) {
        // permission table
        final TreeTable table = new TreeTable();
        table.setSizeFull();
        table.setHeight(500f, Unit.PIXELS);
        table.setSelectable(true);
        table.setNullSelectionAllowed(false);
        table.setImmediate(true);
        table.setColumnReorderingAllowed(false);
        table.addContainerProperty("key", String.class, null);
        table.addContainerProperty("value", String.class, null);
        table.setColumnHeaderMode(Table.ColumnHeaderMode.EXPLICIT_DEFAULTS_ID);
        table.setColumnHeader("key", "KEY");
        table.setColumnHeader("value", "ValUE22312");

//      }
    }

}
