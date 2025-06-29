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
import org.sensorhub.ui.*;
import org.sensorhub.ui.data.*;

import java.util.*;

public class DataFeedConfigForm extends GenericConfigForm {

    private static final String DRIVER_CONFIG_PACKAGE = "com.botts.impl.sensor.datafeed.";
    private static final String PROP_PARSER_CONFIG = "dataParserConfig";
    private static final String PROP_FIELD_MAPPING = "fieldMapping";
    private static final String PROP_INPUT_FIELDS = "inputFields";

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
        super.build(propId, prop, includeSubForms);
    }

    @Override
    protected Component buildTable(String propId, ContainerProperty prop, Class<?> eltType) {
        if (propId.equals(PROP_PARSER_CONFIG + GenericConfigForm.PROP_SEP + PROP_FIELD_MAPPING)) {
            System.out.println("HELLO");
            return buildTable("Field Mapping", "", buildFieldMapperTable(propId, prop, eltType));
        } else if (propId.equals(PROP_PARSER_CONFIG + GenericConfigForm.PROP_SEP + PROP_INPUT_FIELDS)) {
            System.out.println("input fields");
            return buildTable("Input Data", "",buildInputFieldsTable(propId, prop, eltType));
        }
        return super.buildTable(propId, prop, eltType);
    }

    private Component buildTable(String title, String description, Component table) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidth(100.0f, Unit.PERCENTAGE);
        layout.setSpacing(true);
        layout.setCaption(title);
        layout.setDescription(description);
        layout.addComponent(table);
        return layout;
    }

    private TreeTable buildTreeTable() {
        final TreeTable table = new TreeTable();
        table.setSizeFull();
        table.setHeight(200f, Unit.PIXELS);
        table.setSelectable(true);
        table.setNullSelectionAllowed(false);
        table.setImmediate(true);
        table.setColumnReorderingAllowed(false);
        return table;
    }

    private TreeTable buildFieldMapperTable(String propId, ContainerProperty prop, Class<?> eltType) {
        TreeTable table = buildTreeTable();
        table.setColumnHeader("key", "KEY");
        table.setColumnHeader("value", "ValUE22312");

        return table;
    }

    private Component buildInputFieldsTable(String propId, ContainerProperty prop, Class<?> eltType) {
        TreeTable table = buildTreeTable();
        table.addContainerProperty("key", String.class, null);
        table.addContainerProperty("value", String.class, null);
        table.setColumnHeaderMode(Table.ColumnHeaderMode.EXPLICIT_DEFAULTS_ID);
        table.setColumnHeader("key", "InputFields");
        table.setColumnHeader("value", "test12312313223");
        final MyBeanItemContainer<Object> container = prop.getValue();
        var items = container.getItemIds();
        var item = container.getItem(items.get(0));

        FieldWrapper<Object> wrapper = new FieldWrapper<Object>(table) {
            @Override
            protected Component initContent() {
                HorizontalLayout layout = new HorizontalLayout();
                layout.setSpacing(true);

                layout.addComponent(table);
                layout.setComponentAlignment(table, Alignment.MIDDLE_LEFT);

                VerticalLayout buttons = new VerticalLayout();
                layout.addComponent(buttons);

                Button addButton = new Button(ADD_ICON);
                addButton.addStyleName(STYLE_QUIET);
                addButton.addStyleName(STYLE_SMALL);
                buttons.addComponent(addButton);
                addButton.addClickListener((clickEvent) -> {
//                   Map<String, Class<?>> typeList = getPossibleTypes(propId, prop);
//                   container.addBean()
                    System.out.println("clickEvent");
                });
                return layout;
            }
        };

        return wrapper;
    }

    private TreeTable buildDataRecordTable(String propId, ContainerProperty prop, Class<?> eltType) {
        TreeTable table = buildTreeTable();
        table.addContainerProperty("key", String.class, null);
        table.addContainerProperty("value", String.class, null);
        table.setColumnHeaderMode(Table.ColumnHeaderMode.EXPLICIT_DEFAULTS_ID);
        table.setColumnHeader("key", "KEY");
        table.setColumnHeader("value", "ValUE22312");

        return table;
    }

}
