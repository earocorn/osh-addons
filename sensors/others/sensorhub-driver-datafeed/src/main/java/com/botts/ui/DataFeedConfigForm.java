package com.botts.ui;

import com.botts.api.sensor.datafeed.parser.DataParserConfig;
import com.botts.impl.sensor.datafeed.data.*;
import com.vaadin.ui.*;
import com.vaadin.v7.data.Validator;
import com.vaadin.v7.ui.Table;
import org.sensorhub.ui.DisplayUtils;
import org.sensorhub.ui.FieldWrapper;
import org.sensorhub.ui.GenericConfigForm;
import org.sensorhub.ui.ObjectTypeSelectionPopup;
import org.sensorhub.ui.data.*;
import org.vast.swe.SWEHelper;

import java.util.*;

public class DataFeedConfigForm extends GenericConfigForm {

    private static final String PROP_PARSER_CONFIG = "dataParserConfig";
    private static final String PROP_FIELD_MAPPING = "fieldMapping";
    private static final String PROP_INPUT_FIELDS = "inputFields";
    private static final String PROP_OUTPUT_STRUCT = "outputStructure";
    private static final String PROP_FIELDS = "fields";

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

//    @Override
//    protected Component buildTable(String propId, ContainerProperty prop, Class<?> eltType) {
////        Component component = super.buildTable(propId, prop, eltType);
////        if (component instanceof Table table) {
////            table.setEditable(true);
////            table.setInvalidAllowed(true);
////        }
////        if (propId.equals(PROP_PARSER_CONFIG + PROP_SEP + PROP_FIELD_MAPPING)) {
////            return buildTable("Field Mapping", "", buildFieldMapperTable(propId, prop, eltType));
////        } else if (propId.equals(PROP_PARSER_CONFIG + PROP_SEP + PROP_INPUT_FIELDS)) {
////            return buildTable("Input Fields", "",buildInputFieldsTable(propId, prop, eltType));
////        } else if (propId.equals(PROP_PARSER_CONFIG + PROP_SEP + PROP_OUTPUT_STRUCT + PROP_SEP + PROP_FIELDS)) {
////            return buildTable("Output Fields", "", buildDataComponentTable(propId, prop, eltType));
////        }
////        return component;
//    }

    private Component buildTable(String title, String description, Component table) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidth(100.0f, Unit.PERCENTAGE);
        layout.setSpacing(true);
        layout.setCaption(title);
        layout.setDescription(description);
        layout.addComponent(table);
        return layout;
    }

    private Table buildDefaultTable() {
        final Table table = new Table();
        table.setSizeFull();
        table.setHeight(200f, Unit.PIXELS);
        table.setSelectable(true);
        table.setNullSelectionAllowed(true);
        table.setImmediate(true);
        table.setColumnReorderingAllowed(false);
        table.setInvalidAllowed(true);
        return table;
    }

    private Component buildFieldMapperTable(String propId, ContainerProperty prop, Class<?> eltType) {
        Table table = buildDefaultTable();
        String FIELD_MAPPING = new StringBuilder()
                .append(PROP_PARSER_CONFIG)
                .append(PROP_SEP)
                .append(PROP_FIELD_MAPPING)
                .append(PROP_SEP).toString();
        String PROP_INPUT_NAME = new StringBuilder(FIELD_MAPPING).append("inputFieldName").toString();
        String PROP_OUTPUT_NAME = new StringBuilder(FIELD_MAPPING).append("outputFieldName").toString();
        final MyBeanItemContainer<Object> container = prop.getValue();
        table.setContainerDataSource(container);
        table.setEditable(true);
        table.setColumnHeader(PROP_INPUT_NAME, "Input Field Name");
        table.setColumnHeader(PROP_OUTPUT_NAME, "Output Field Name");

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
                    try {
                        container.addBean(FieldMapping.class.newInstance(), propId + PROP_SEP);
                    } catch (InstantiationException | IllegalAccessException e) {
                        DisplayUtils.showErrorPopup("Error adding item " + propId, e);
                    }
                });

                Button delButton = new Button(DEL_ICON);
                delButton.addStyleName(STYLE_QUIET);
                delButton.addStyleName(STYLE_SMALL);
                buttons.addComponent(delButton);
                delButton.addClickListener((clickEvent) -> {
                    Object itemId = table.getValue();
                    container.removeItem(itemId);
                });

                return layout;
            }

            @Override
            public void commit() throws SourceException, Validator.InvalidValueException {
                prop.setValue(container);
            }
        };

        return wrapper;
    }

    private Component buildInputFieldsTable(String propId, ContainerProperty prop, Class<?> eltType) {
        Table table = buildDefaultTable();
        String INPUT_FIELDS = new StringBuilder()
                .append(PROP_PARSER_CONFIG)
                .append(PROP_SEP)
                .append(PROP_INPUT_FIELDS)
                .append(PROP_SEP).toString();
        String PROP_CARDINALITY = new StringBuilder(INPUT_FIELDS).append("cardinality").toString();
        String PROP_NAME = new StringBuilder(INPUT_FIELDS).append("name").toString();
        String PROP_DATA_TYPE = new StringBuilder(INPUT_FIELDS).append("dataType").toString();
        final MyBeanItemContainer<Object> container = prop.getValue();
        table.setContainerDataSource(container);
        table.setEditable(true);
        table.setColumnHeader(PROP_CARDINALITY, "Cardinality");
        table.setColumnHeader(PROP_NAME, "Field Name");
        table.setColumnHeader(PROP_DATA_TYPE, "Field Data Type");
        for (var itemId : container.getItemIds())
            container.getItem(itemId).getItemProperty(PROP_DATA_TYPE).setReadOnly(true);

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
                    Map<String, Class<?>> typeList = getDataTypeList();
                    DataTypeSelectionPopup.ObjectTypeSelectionCallbackWrapper callback = new DataTypeSelectionPopup.ObjectTypeSelectionCallbackWrapper() {
                        @Override
                        public void onSelected(Class<?> objectType) {
                            try {
                                var bean = DataField.class.newInstance();
                                bean.dataType = BaseDataType.getType(objectType);
                                bean.cardinality = container.getItemIds().size();
                                var beanItem = container.addBean(bean, propId + PROP_SEP);
                                beanItem.getItemProperty(PROP_DATA_TYPE).setReadOnly(true);
                            } catch (InstantiationException | IllegalAccessException e) {
                                DisplayUtils.showErrorPopup("Error adding item " + propId, e);
                            }
                        }
                    };

                    if (typeList == null || typeList.isEmpty())
                    {
                        // we use the declared type
                        callback.onSelected(container.getBeanType());
                    }
                    else if (typeList.size() == 1)
                    {
                        // we automatically use the only type in the list
                        Class<?> firstType = typeList.values().iterator().next();
                        callback.onSelected(firstType);
                    }
                    else
                    {
                        // we popup the list so the user can select what he wants
                        ObjectTypeSelectionPopup popup = new ObjectTypeSelectionPopup(OPTION_SELECT_MSG, typeList, callback);
                        popup.setModal(true);
                        getUI().addWindow(popup);
                    }
                });

                Button delButton = new Button(DEL_ICON);
                delButton.addStyleName(STYLE_QUIET);
                delButton.addStyleName(STYLE_SMALL);
                buttons.addComponent(delButton);
                delButton.addClickListener((clickEvent) -> {
                    Object itemId = table.getValue();
                    container.removeItem(itemId);
                });

                return layout;
            }

            @Override
            public void commit() throws SourceException, Validator.InvalidValueException {
                prop.setValue(container);
            }
        };

        return wrapper;
    }

    private Component buildDataComponentTable(String propId, ContainerProperty prop, Class<?> eltType) {
        Table table = buildDefaultTable();
        String DATA_COMPONENT = new StringBuilder()
                .append(PROP_PARSER_CONFIG)
                .append(PROP_SEP)
                .append(PROP_OUTPUT_STRUCT)
                .append(PROP_SEP)
                .append(PROP_FIELDS)
                .append(PROP_SEP).toString();
        String PROP_NAME = new StringBuilder(DATA_COMPONENT).append("name").toString();
        String PROP_LABEL = new StringBuilder(DATA_COMPONENT).append("label").toString();
        String PROP_DESCRIPTION = new StringBuilder(DATA_COMPONENT).append("description").toString();
        String PROP_DEFINITION = new StringBuilder(DATA_COMPONENT).append("definition").toString();
        String PROP_DATA_TYPE = new StringBuilder(DATA_COMPONENT).append("dataType").toString();
        final MyBeanItemContainer<Object> container = prop.getValue();
        table.setContainerDataSource(container);
        table.setEditable(true);
        table.setColumnHeader(PROP_NAME, "Name");
        table.setColumnHeader(PROP_LABEL, "Label");
        table.setColumnHeader(PROP_DESCRIPTION, "Description");
        table.setColumnHeader(PROP_DEFINITION, "Definition");
        table.setColumnHeader(PROP_DATA_TYPE, "Data Type");
        for (var itemId : container.getItemIds())
            container.getItem(itemId).getItemProperty(PROP_DATA_TYPE).setReadOnly(true);

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
                    Map<String, Class<?>> typeList = getDataTypeList();
                    DataTypeSelectionPopup.ObjectTypeSelectionCallbackWrapper callback = new DataTypeSelectionPopup.ObjectTypeSelectionCallbackWrapper() {
                        @Override
                        public void onSelected(Class<?> objectType) {
                            try {
                                var bean = DataComponentConfig.class.newInstance();
                                var propids = container.getContainerPropertyIds();
                                bean.dataType = BaseDataType.getType(objectType);
                                var beanItem = container.addBean(bean, propId + PROP_SEP);
                                beanItem.getItemProperty(PROP_DATA_TYPE).setReadOnly(true);
                            } catch (InstantiationException | IllegalAccessException e) {
                                DisplayUtils.showErrorPopup("Error adding item " + propId, e);
                            }
                        }
                    };

                    if (typeList == null || typeList.isEmpty())
                    {
                        // we use the declared type
                        callback.onSelected(container.getBeanType());
                    }
                    else if (typeList.size() == 1)
                    {
                        // we automatically use the only type in the list
                        Class<?> firstType = typeList.values().iterator().next();
                        callback.onSelected(firstType);
                    }
                    else
                    {
                        // we popup the list so the user can select what he wants
                        ObjectTypeSelectionPopup popup = new ObjectTypeSelectionPopup(OPTION_SELECT_MSG, typeList, callback);
                        popup.setModal(true);
                        getUI().addWindow(popup);
                    }
                });

                Button delButton = new Button(DEL_ICON);
                delButton.addStyleName(STYLE_QUIET);
                delButton.addStyleName(STYLE_SMALL);
                buttons.addComponent(delButton);
                delButton.addClickListener((clickEvent) -> {
                    Object itemId = table.getValue();
                    container.removeItem(itemId);
                });

                return layout;
            }

            @Override
            public void commit() throws SourceException, Validator.InvalidValueException {
                prop.setValue(container);
            }
        };

        return wrapper;
    }

    private Map<String, Class<?>> getDataTypeList() {
        Map<String, Class<?>> typeList = new HashMap<>();
        typeList.put("Double", Double.class);
        typeList.put("Float", Float.class);
        typeList.put("Integer", Integer.class);
        typeList.put("Long", Long.class);
        typeList.put("Byte", Byte.class);
        typeList.put("String", String.class);
        typeList.put("Boolean", Boolean.class);
        return typeList;
    }
}
