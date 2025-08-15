package com.botts.ui;

import com.botts.api.sensor.datafeed.parser.DataParserConfig;
import com.botts.impl.sensor.datafeed.parsers.ProtobufConfig;
import com.botts.impl.utils.ProtobufHelper;
import com.botts.impl.utils.data.DataField;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.vaadin.event.Action;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.Property;
import com.vaadin.v7.data.fieldgroup.FieldGroup;
import com.vaadin.v7.data.util.converter.Converter;
import com.vaadin.v7.ui.ComboBox;
import com.vaadin.v7.ui.Field;
import com.vaadin.v7.ui.Table;
import com.vaadin.v7.ui.TreeTable;
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
    private transient ProtobufConfig parserConfig;
    private transient TreeTable inputFieldsTable;
    private static AtomicBoolean usingOldFile = new AtomicBoolean(false);
    private static ConcurrentHashMap<String, Descriptors.Descriptor> descriptorMap = new ConcurrentHashMap<>();

    @Override
    public void build(String title, String popupText, MyBeanItem<Object> beanItem, boolean includeSubForms) {
        if (beanItem.getBean() instanceof DataParserConfig)
            this.parserConfig = (ProtobufConfig) beanItem.getBean();
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

        this.descriptorMap.clear();
        this.descriptorMap.putAll(descriptorMap);

        return descriptorMap.values();

//        List<String> leafMessages = new ArrayList<>();
//        Set<String> visited = new HashSet<>();
//
//        // Iterate through each top-level message
//        for (var entry : descriptorMap.entrySet()) {
//            Queue<Descriptors.Descriptor> queue = new ArrayDeque<>();
//            queue.add(entry.getValue());
//
//            while (!queue.isEmpty()) {
//                Descriptors.Descriptor current = queue.poll();
//                if (!visited.add(current.getFullName())) {
//                    continue; // skip already processed
//                }
//
//                List<Descriptors.FieldDescriptor> fields = current.getFields();
//                boolean hasMessageField = false;
//
//                for (Descriptors.FieldDescriptor field : fields) {
//                    if (field.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE) {
//                        hasMessageField = true;
//                        queue.add(field.getMessageType());
//                    } else {
//                        leafMessages.add(field.getFullName());
//                    }
//                }
//
//                if (!hasMessageField) {
//                    leafMessages.add(current.getFullName());
//                }
//            }
//        }

//        return leafMessages;
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
                        parserConfig.parserConfig.defaultMessageType = val.toString();
                        PROTO_MSG_TYPE.set(val.toString());
                        populateTable();
                    }
                });

                fieldGroup.bind(select, propId);
                return select;
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

            this.inputFieldsTable.addValueChangeListener(event -> {
                // TODO: Update this prop when config changes
//                prop.get
                prop.setValue(prop.getValue());
            });

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
                return value.getFullName();
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

//        // detect all modules for which permissions are set
//        // and add all root permissions to tree
//        HashSet<String> moduleIdStrings = new HashSet<>();
//        addTopLevelPermissions(moduleIdStrings, permConfig.allow);
//        addTopLevelPermissions(moduleIdStrings, permConfig.deny);
//        for (String moduleIdString: moduleIdStrings)
//        {
//            IPermission perm = getParentHub().getSecurityManager().getModulePermissions(moduleIdString);
//            if (perm != null)
//                addPermToTree(table, perm, null);
//        }

        this.inputFieldsTable = table;
        layout.addComponent(table);
    }

    private void addInputField(String fieldName) {
        // TODO: Add to UI and config
        if (descriptorMap != null && PROTO_MSG_TYPE.get() != null) {
            var defaultDescriptor = descriptorMap.get(PROTO_MSG_TYPE.get());
            if (defaultDescriptor == null)
                return;

            // TODO: If have fields, do it recursively
            Descriptors.FieldDescriptor fieldDesc = findInDescriptor(defaultDescriptor, fieldName);
            if (fieldDesc != null) {
                if (fieldDesc.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE && inputFieldsTable.hasChildren(fieldName))
                    for (var child : fieldDesc.getMessageType().getFields())
                        addInputField(child.getFullName());
                // TODO: Change this
                // Check if already in config
                for (int i = 0; i < parserConfig.inputFields.size(); i++)
                    if (Objects.equals(parserConfig.inputFields.get(i).name, fieldName))
                        return;
                // Add new field
                DataField field = new DataField(fieldDesc.getIndex(), fieldName, ProtobufHelper.toBaseDataType(fieldDesc.getJavaType()));
                parserConfig.inputFields.add(field);
            }
        }
    }

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
        if (descriptorMap != null && PROTO_MSG_TYPE.get() != null) {
            var defaultDescriptor = descriptorMap.get(PROTO_MSG_TYPE.get());
            if (defaultDescriptor == null)
                return;

            // TODO: If have fields, do it recursively
            Descriptors.FieldDescriptor fieldDesc = findInDescriptor(defaultDescriptor, fieldName);
            if (fieldDesc != null) {
                if (fieldDesc.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE)
                    for (var child : fieldDesc.getMessageType().getFields())
                        removeInputField(child.getFullName());
                for (int i = 0; i < parserConfig.inputFields.size(); i++)
                    if (Objects.equals(parserConfig.inputFields.get(i).name, fieldName))
                        parserConfig.inputFields.remove(i);
            }
        }
    }

    private void refreshFields(TreeTable table) {
        for (Object itemId : table.getContainerDataSource().getItemIds()) {
//            if (table.hasChildren(itemId))
//                continue;
            Item item = table.getItem(itemId);
            Descriptors.FieldDescriptor field = (Descriptors.FieldDescriptor) item.getItemProperty(PROP_FIELD).getValue();
            item.getItemProperty(PROP_STATE).setValue(getState(field));
            try {
                commit();
            } catch (FieldGroup.CommitException e) {
                getOshLogger().warn("Commit failed", e);
            }
        }
    }

    private FieldState getState(Descriptors.FieldDescriptor field) {
        FieldState state = FieldState.DISABLED;
        var fieldIterator = parserConfig.inputFields.iterator();
        while (fieldIterator.hasNext()) {
            if (Objects.equals(fieldIterator.next().name, field.getFullName())) {
                state = FieldState.ENABLED;
                break;
            }
        }
        return state;
    }

    private void populateTable() {
        if (this.parserConfig != null
                && PROTO_MSG_TYPE.get() != null
                && descriptorMap != null) {
            var defaultDesc = descriptorMap.get(PROTO_MSG_TYPE.get());
            if (defaultDesc != null) {
                clearTree();
                for (var field : defaultDesc.getFields())
                    addFieldToTree(inputFieldsTable, field, null);
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

    private synchronized void addFieldToTree(TreeTable table, Descriptors.FieldDescriptor descriptor, Object parentId) {
        Object newItemId = descriptor.getFullName();
        Item newItem = table.getItem(newItemId);
        if (newItem == null)
            newItem = table.addItem(newItemId);
        newItem.getItemProperty(PROP_FIELD).setValue(descriptor);
        newItem.getItemProperty(PROP_STATE).setValue(getState(descriptor));

        if (parentId != null)
            table.setParent(newItemId, parentId);

        if (descriptor.getJavaType() != Descriptors.FieldDescriptor.JavaType.MESSAGE)
            table.setChildrenAllowed(newItemId, false);
        else {
            for (Descriptors.FieldDescriptor childDesc : descriptor.getMessageType().getFields())
//            var childDesc = descriptor.getMessageType().getFields().get(0);
                addFieldToTree(table, childDesc, newItemId);
        }
    }

}
