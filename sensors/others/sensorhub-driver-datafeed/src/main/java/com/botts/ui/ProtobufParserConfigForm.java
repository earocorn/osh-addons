package com.botts.ui;

import com.botts.api.parser.DataParserConfig;
import com.botts.api.parser.data.DataField;
import com.botts.impl.parser.protobuf.ProtobufDataParserConfig;
import com.botts.impl.parser.protobuf.ProtobufHelper;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.vaadin.event.Action;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.Property;
import com.vaadin.v7.data.fieldgroup.FieldGroup;
import com.vaadin.v7.data.util.converter.Converter;
import com.vaadin.v7.ui.*;
import org.sensorhub.ui.DisplayUtils;
import org.sensorhub.ui.FieldWrapper;
import org.sensorhub.ui.GenericConfigForm;
import org.sensorhub.ui.data.ContainerProperty;
import org.sensorhub.ui.data.MyBeanItem;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class ProtobufParserConfigForm extends GenericConfigForm {

    private static final String PROP_MSG = "dataParserConfig.defaultMessageType";
    private static final String PROP_PROTO_DESC_PATH = "dataParserConfig.descFilePath";
    private static final String PROP_INPUTS = "dataParserConfig.inputFields";
    private static final String PROP_INPUT_NAME = "dataParserConfig.inputFields.name";

    private static AtomicReference<String> PROTO_DESC_PATH = new AtomicReference<>(null);
    private static AtomicReference<String> PROTO_MSG_TYPE = new AtomicReference<>(null);

    private static final String PROP_STATE = "state";
    private static final String PROP_FIELD = "field";
    private static final Action ENABLE_ACTION = new Action("Enable", FontAwesome.CHECK);
    private static final Action DISABLE_ACTION = new Action("Disable", FontAwesome.BAN);
    private transient ProtobufDataParserConfig parserConfig;
    private transient TreeTable inputFieldsTable;
    private static AtomicBoolean usingOldFile = new AtomicBoolean(false);
    private static ConcurrentHashMap<String, Descriptors.Descriptor> descriptorMap = new ConcurrentHashMap<>();

    @Override
    public void build(String title, String popupText, MyBeanItem<Object> beanItem, boolean includeSubForms) {
        if (beanItem.getBean() instanceof DataParserConfig)
            this.parserConfig = (ProtobufDataParserConfig)beanItem.getBean();
        super.build(title, popupText, beanItem, includeSubForms);
    }

    private Collection<Descriptors.Descriptor> getProtoFields() throws IOException, Descriptors.DescriptorValidationException {
        String filepath = PROTO_DESC_PATH.get();
        if (PROTO_DESC_PATH.get() == null)
            return Collections.emptyList();
        DescriptorProtos.FileDescriptorSet set = DescriptorProtos.FileDescriptorSet.parseFrom(new FileInputStream(filepath));
        Map<String, Descriptors.FileDescriptor> fileDescriptorMap = new HashMap<>();
        Map<String, Descriptors.Descriptor> descriptorMap = new HashMap<>();

        for (DescriptorProtos.FileDescriptorProto proto : set.getFileList()) {
            Descriptors.FileDescriptor[] deps = new Descriptors.FileDescriptor[proto.getDependencyCount()];
            for (int i = 0; i < proto.getDependencyCount(); i++)
                deps[i] = fileDescriptorMap.get(proto.getDependency(i));

            Descriptors.FileDescriptor fileDescriptor = Descriptors.FileDescriptor.buildFrom(proto, deps);
            fileDescriptorMap.put(fileDescriptor.getName(), fileDescriptor);

            for (Descriptors.Descriptor messageType : fileDescriptor.getMessageTypes())
                descriptorMap.put(messageType.getFullName(), messageType);
        }

        ProtobufParserConfigForm.descriptorMap.clear();
        ProtobufParserConfigForm.descriptorMap.putAll(descriptorMap);

        return descriptorMap.values();
    }

    @Override
    protected Field<?> buildAndBindField(String label, String propId, Property<?> prop) {
        // For custom fields that we need to bind manually
        if (propId.equals(PROP_MSG)) {
            if (prop.getValue() != null) {
                PROTO_MSG_TYPE.set(prop.getValue().toString());
                populateTable();
            }
            com.vaadin.v7.ui.ComboBox select = new ComboBox();
            select.setCaption(label);
            select.setWidth(500, Unit.PIXELS);
            select.setNullSelectionAllowed(false);

            try {
                var possibleValues = getProtoFields();

                for (Descriptors.Descriptor descriptor : possibleValues) {
                    if (descriptor == null) {
                        select.setNewItemsAllowed(true);
                        select.setImmediate(true);
                    } else
                        select.addItem(descriptor.getFullName());
                }

                select.addValueChangeListener(event -> {
                    var val = event.getProperty().getValue();
                    if (val != null) {
                        parserConfig.defaultMessageType = val.toString();
                        PROTO_MSG_TYPE.set(val.toString());
                        populateTable();
                    }
                });

                fieldGroup.bind(select, propId);

                return new FieldWrapper<>(select) {
                    @Override
                    protected Component initContent() {
                        HorizontalLayout layout = new HorizontalLayout();
                        layout.setSpacing(true);

                        layout.addComponent(select);
                        layout.setComponentAlignment(select, Alignment.MIDDLE_LEFT);
//                        final Field<Object> wrapper = this;

                        Button refreshButton = new Button(FontAwesome.REFRESH);
                        refreshButton.setDescription("Refresh File");
                        refreshButton.addStyleName(STYLE_QUIET);
                        layout.addComponent(refreshButton);
                        layout.setComponentAlignment(refreshButton, Alignment.MIDDLE_LEFT);
                        refreshButton.addClickListener(event -> {
                            select.removeAllItems();
                            Collection<Descriptors.Descriptor> possibleValues = null;
                            try {
                                possibleValues = getProtoFields();
                            } catch (IOException | Descriptors.DescriptorValidationException e) {
                                getOshLogger().error("Unable to retrieve fields. Please make sure that the file/path is valid", e);
                            }

                            if (possibleValues == null)
                                return;

                            for (Descriptors.Descriptor descriptor : possibleValues) {
                                if (descriptor == null) {
                                    select.setNewItemsAllowed(true);
                                    select.setImmediate(true);
                                } else
                                    select.addItem(descriptor.getFullName());
                            }
                        });
                        return layout;
                    }
                };
            } catch (Descriptors.DescriptorValidationException | IOException e) {
                getOshLogger().warn("Unable to create combo box options", e);
            }
        }

        Field<?> field = super.buildAndBindField(label, propId, prop);
        if (propId.equals(PROP_PROTO_DESC_PATH)) {
            if (PROTO_DESC_PATH.get() == null && prop.getValue() != null)
                PROTO_DESC_PATH.set(prop.getValue().toString());

            field.addValueChangeListener(event -> {
                var val = event.getProperty().getValue();
                if (val != null)
                    PROTO_DESC_PATH.set(val.toString());
            });
        }

        return field;
    }

    @Override
    protected void buildListComponent(String propId, ContainerProperty prop, FieldGroup fieldGroup) {
        if (propId.equals(PROP_INPUTS)) {
            HorizontalLayout layout = new HorizontalLayout();
            layout.setWidth(100.0f, Unit.PERCENTAGE);
            layout.setSpacing(true);
            layout.setCaption("Proto Fields");
            layout.setDescription("Enabled and disabled input fields");

            // possible fields table
            buildTable(layout);

            subForms.add(layout);
        } else
            super.buildListComponent(propId, prop, fieldGroup);
    }

    private enum FieldState {
        ENABLED,
        DISABLED
    }

    private void buildTable(HorizontalLayout layout)
    {
        // permission table
        final TreeTable table = new TreeTable();
        table.setSizeFull();
        table.setHeight(500f, Unit.PIXELS);
        table.setSelectable(true);
        table.setNullSelectionAllowed(false);
        table.setImmediate(true);
        table.setColumnReorderingAllowed(false);
        table.addContainerProperty(PROP_FIELD, Descriptors.FieldDescriptor.class, null);
        table.addContainerProperty(PROP_STATE, FieldState.class, FieldState.DISABLED);
        table.setColumnHeaderMode(Table.ColumnHeaderMode.EXPLICIT_DEFAULTS_ID);
        table.setColumnHeader(PROP_FIELD, "Proto Field Name");
        table.setColumnHeader(PROP_STATE, "Enable/Disable");

        // cell converter for name
        table.setConverter(PROP_FIELD, new Converter<String, Descriptors.FieldDescriptor>() {
            @Override
            public Descriptors.FieldDescriptor convertToModel(String value, Class<? extends Descriptors.FieldDescriptor> targetType, Locale locale) throws ConversionException {
                return null;
            }

            @Override
            public String convertToPresentation(Descriptors.FieldDescriptor value, Class<? extends String> targetType, Locale locale) throws ConversionException {
                if (value == null)
                    return null;
                if (value.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE) {
                    return value.getName() + " (" + value.getMessageType().getName() + ")";
                } else
                    return value.getName();
            }

            @Override
            public Class<Descriptors.FieldDescriptor> getModelType() {
                return Descriptors.FieldDescriptor.class;
            }

            @Override
            public Class<String> getPresentationType()
            {
                return String.class;
            }
        });

        // cell converter for state
        table.setConverter(PROP_STATE, new Converter<String, FieldState>() {
            @Override
            public FieldState convertToModel(String value, Class<? extends FieldState> targetType, Locale locale) throws ConversionException {
                return FieldState.valueOf(value);
            }

            @Override
            public String convertToPresentation(FieldState value, Class<? extends String> targetType, Locale locale) throws ConversionException {
                return value == FieldState.ENABLED ? "Enabled" : "Disabled";
            }

            @Override
            public Class<FieldState> getModelType()
            {
                return FieldState.class;
            }

            @Override
            public Class<String> getPresentationType()
            {
                return String.class;
            }
        });

        // cell style depending on state
        table.setCellStyleGenerator((Table.CellStyleGenerator) (source, itemId, propertyId) -> {
            if (propertyId != null && propertyId.equals(PROP_STATE))
            {
                FieldState state = (FieldState)table.getItem(itemId).getItemProperty(PROP_STATE).getValue();
                return state == FieldState.ENABLED ? "perm-allow" : "perm-deny";
            }

            return null;
        });

        // context menu
        table.addActionHandler(new Action.Handler() {
            @Override
            public Action[] getActions(Object target, Object sender)
            {
                List<Action> actions = new ArrayList<>();

                if (target != null)
                {
                    actions.add(ENABLE_ACTION);
                    actions.add(DISABLE_ACTION);
                }

                return actions.toArray(new Action[0]);
            }

            @Override
            public void handleAction(Action action, Object sender, Object target)
            {
                final Object selectedId = table.getValue();

                if (selectedId != null)
                {
                    String fieldFullName = selectedId.toString();

                    if (action == ENABLE_ACTION)
                    {
//                        String[] split = fieldFullName.split("\\.");
//                        String msg = split[split.length-1];
//                        String type = fieldFullName.substring(0, fieldFullName.lastIndexOf("."));
                        addInputField(fieldFullName);
                    }
                    else if (action == DISABLE_ACTION)
                    {
                        removeInputField(fieldFullName);
                    }

                    refreshFields(table);
                }
            }
        });

        this.inputFieldsTable = table;
        layout.addComponent(table);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private void addInputField(String fullPath) {
        if (descriptorMap == null || PROTO_MSG_TYPE.get() == null) {
            return;
        }

        // Root descriptor (e.g., etf.ETFMessage)
        Descriptors.Descriptor rootDescriptor = descriptorMap.get(PROTO_MSG_TYPE.get());
        if (rootDescriptor == null) {
            return;
        }

        if (fullPath == null) {
            return;
        }

        // Avoid duplicates
        if (parserConfig.inputFields.stream().anyMatch(f -> f.name.equals(fullPath))) {
            return;
        }

        // Find final descriptor to get type info
        Descriptors.FieldDescriptor finalField = findFieldByPath(rootDescriptor, fullPath);
        if (finalField != null) {
            if (finalField.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE) {
                for (var subField : finalField.getMessageType().getFields())
                    // TODO BUILD FULL PATH HERE FOR CHILDREN
                    addInputField(buildFullPath(subField, fullPath));
            }
            parserConfig.inputFields.add(
                    new DataField(finalField.getIndex(), fullPath, ProtobufHelper.toBaseDataType(finalField.getJavaType()))
            );
        }
    }

    private String buildFullPath(Descriptors.FieldDescriptor descriptor, String parentPath) {
        return (parentPath == null || parentPath.isEmpty())
                ? descriptor.getContainingType().getFullName() + "." + descriptor.getName()
                : parentPath + "." + descriptor.getName();
    }

    private Descriptors.FieldDescriptor findFieldByPath(Descriptors.Descriptor descriptor, String fullPath) {
        String[] parts = fullPath.split("\\.");
        Descriptors.Descriptor current = descriptor;
        Descriptors.FieldDescriptor fieldDesc = null;

        // Skip the first part (package) and the root message name
        int startIndex = parts.length > 2 ? 2 : 0;
        for (int i = startIndex; i < parts.length; i++) {
            fieldDesc = current.findFieldByName(parts[i]);
            if (fieldDesc == null) {
                return null;
            }
            if (i < parts.length - 1 && fieldDesc.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE) {
                current = fieldDesc.getMessageType();
            }
        }
        return fieldDesc;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private Descriptors.FieldDescriptor findInDescriptor(Descriptors.Descriptor descriptor, String fullFieldName) {
        for (Descriptors.FieldDescriptor fieldDescriptor : descriptor.getFields()) {
            if (Objects.equals(fieldDescriptor.getFullName(), fullFieldName))
                return fieldDescriptor;
            if (fieldDescriptor.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE) {
                var subDesc = findInDescriptor(fieldDescriptor.getMessageType(), fullFieldName);
                if (subDesc != null)
                    return subDesc;
            }
        }
        return null;
    }

    private void removeInputField(String fieldName) {
        // TODO: Remove from UI
        if (descriptorMap == null || PROTO_MSG_TYPE.get() == null)
            return;

        // If removing a tree, remove recursively
        if (inputFieldsTable.hasChildren(fieldName))
            for (var childId : inputFieldsTable.getChildren(fieldName))
                removeInputField(childId.toString());

        parserConfig.inputFields.removeIf(f -> Objects.equals(f.name, fieldName));
    }

    private void refreshFields(TreeTable table) {
        table.getParent().setCaption(parserConfig.inputFields.size() + " fields enabled");
        for (Object itemId : table.getContainerDataSource().getItemIds()) {
            Item item = table.getItem(itemId);
            item.getItemProperty(PROP_STATE).setValue(getState(itemId.toString()));
        }
    }

    private FieldState getState(String fullPath) {
        return parserConfig.inputFields.stream().anyMatch(f -> Objects.equals(f.name, fullPath))
                ? FieldState.ENABLED : FieldState.DISABLED;
    }

    private void populateTable() {
        if (this.parserConfig != null
                && PROTO_MSG_TYPE.get() != null
                && descriptorMap != null) {
            var defaultDesc = descriptorMap.get(PROTO_MSG_TYPE.get());
            if (defaultDesc != null) {
                clearTree();
                for (var field : defaultDesc.getFields())
                    addFieldToTree(inputFieldsTable, field, null, "");
            }
        }
    }

    private synchronized void clearTree() {
        for (var item : inputFieldsTable.getItemIds())
            removeItemRecursively(item);
    }

    private synchronized void removeItemRecursively(Object item) {
        if (inputFieldsTable.hasChildren(item)) {
            var children = new ArrayList<>(inputFieldsTable.getChildren(item));
            for (var child : children) {
                removeItemRecursively(child);
                inputFieldsTable.removeItem(child);
            }
        }
        inputFieldsTable.removeItem(item);
    }

    private synchronized void addFieldToTree(TreeTable table, Descriptors.FieldDescriptor descriptor, Object parentId, String parentPath) {
        String fullRootPath = (parentPath == null || parentPath.isEmpty())
                ? descriptor.getContainingType().getFullName() + "." + descriptor.getName()
                : parentPath + "." + descriptor.getName();

        Object newItemId = fullRootPath;
        Item newItem = table.getItem(newItemId);
        if (newItem == null)
            newItem = table.addItem(newItemId);

        newItem.getItemProperty(PROP_FIELD).setValue(descriptor);
        newItem.getItemProperty(PROP_STATE).setValue(getState(fullRootPath));

        if (parentId != null)
            table.setParent(newItemId, parentId);

        if (descriptor.getJavaType() != Descriptors.FieldDescriptor.JavaType.MESSAGE)
            table.setChildrenAllowed(newItemId, false);
        else {
            for (Descriptors.FieldDescriptor childDesc : descriptor.getMessageType().getFields())
                addFieldToTree(table, childDesc, newItemId, fullRootPath);
        }
    }

}
