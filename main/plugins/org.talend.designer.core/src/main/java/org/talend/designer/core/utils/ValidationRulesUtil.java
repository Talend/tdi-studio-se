// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.swt.graphics.RGB;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataColumn;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.metadata.MetadataToolHelper;
import org.talend.core.model.metadata.builder.connection.HL7Connection;
import org.talend.core.model.metadata.builder.connection.ValidationRulesConnection;
import org.talend.core.model.metadata.builder.connection.WSDLSchemaConnection;
import org.talend.core.model.metadata.builder.connection.XmlFileConnection;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.model.properties.HL7ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.ValidationRulesConnectionItem;
import org.talend.core.model.properties.WSDLSchemaConnectionItem;
import org.talend.core.model.properties.XmlFileConnectionItem;
import org.talend.core.model.properties.impl.XmlFileConnectionItemImpl;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.model.components.NodeConnector;
import org.talend.designer.core.ui.editor.cmd.ChangeValuesFromRepository;
import org.talend.designer.core.ui.editor.cmd.ConnectionDeleteCommand;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.process.EDatabaseComponentName;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * DOC ycbai class global comment. Detailled comment
 */
public class ValidationRulesUtil {

    private static final String MAP = "MAP";//$NON-NLS-1$

    private static final String OUTPUT = "Output"; //$NON-NLS-1$

    private static final String INPUT = "Input"; //$NON-NLS-1$

    /**
     * DOC ycbai Comment method "getRelatedValidationRuleObjs". get the validation rules object related to the element.
     *
     * @param node
     * @return
     */
    public static List<IRepositoryViewObject> getRelatedValidationRuleObjs(IElement element) {
        String repId = (String) element.getPropertyValue(EParameterName.REPOSITORY_SCHEMA_TYPE.getName());
        return getRelatedValidationRuleObjs(repId);
    }

    public static List<IRepositoryViewObject> getRelatedValidationRuleObjs(String schemaId) {
        List<IRepositoryViewObject> rulesObjs = new ArrayList<IRepositoryViewObject>();

        if (schemaId != null) {
            try {
                IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                List<IRepositoryViewObject> members = factory.getAll(ERepositoryObjectType.METADATA_VALIDATION_RULES);
                if (members != null && members.size() > 0) {
                    for (IRepositoryViewObject member : members) {
                        if (member != null && member.getProperty() != null) {
                            Item item = member.getProperty().getItem();
                            if (item != null && item instanceof ValidationRulesConnectionItem) {
                                ValidationRulesConnectionItem validItem = (ValidationRulesConnectionItem) item;
                                ValidationRulesConnection connection = (ValidationRulesConnection) validItem.getConnection();
                                if (connection != null && !rulesObjs.contains(member)) {
                                    rulesObjs.add(member);
                                }
                            }
                        }
                    }
                }
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }
        }

        return rulesObjs;
    }

    public static IRepositoryViewObject getCurrentValidationRuleObjs(IElement element) {
        IElementParameter param = element.getElementParameter(EParameterName.VALIDATION_RULES.getName());
        if (param == null || param.getValue() == null || (Boolean) param.getValue() == false) {
            return null;
        }

        param = element.getElementParameter(EParameterFieldType.VALIDATION_RULE_TYPE.getName());
        if (param == null) {
            return null;
        }

        param = param.getChildParameters().get(EParameterName.REPOSITORY_VALIDATION_RULE_TYPE.getName());
        if (param != null && param.getValue() != null) {
            IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
            String linkedRepository = (String) param.getValue();
            try {
                IRepositoryViewObject object = factory.getLastVersion(linkedRepository);
                if (object == null) {
                    return null;
                }
                return object;
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }
        }

        return null;
    }

    public static boolean isCurrentValRuleObjInList(List<IRepositoryViewObject> list, IRepositoryViewObject currentViewObject) {
        for (IRepositoryViewObject obj : list) {
            if (obj != null && obj.getId() != null && obj.getId().equals(currentViewObject.getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * DOC ycbai Comment method "getComponentsFromItemId". modify from method getAppropriateComponent() of
     * TalendEditorDropTargetListener.
     *
     * @param itemId
     * @param type
     * @param quickCreateInput
     * @param quickCreateOutput
     * @return
     */
    public static IComponent getComponentsFromItemId(String itemId, ERepositoryObjectType type, boolean quickCreateInput,
            boolean quickCreateOutput) {
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        IRepositoryViewObject obj = null;
        try {
            obj = factory.getLastVersion(itemId);
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
        if (obj == null) {
            return null;
        }
        Item item = obj.getProperty().getItem();
        if (item == null) {
            return null;
        }

        EDatabaseComponentName name = EDatabaseComponentName.getCorrespondingComponentName(item, type);
        String componentName = null;
        if (item instanceof JobletProcessItem) { // joblet
            componentName = item.getProperty().getLabel();
        } else if (name == null) {
            return null;
        } else { // tRunjob
            componentName = name.getDefaultComponentName();
        }

        // tRunJob is special from our rules
        if (name == EDatabaseComponentName.RunJob || item instanceof JobletProcessItem) {
            return ComponentsFactoryProvider.getInstance().get(componentName, ComponentCategory.CATEGORY_4_DI.getName());
        } else {
            // for database, file, webservices, saleforce ...
            List<IComponent> neededComponents = filterNeededComponents(item, type);

            IComponent component = chooseOneComponent(neededComponents, name, quickCreateInput, quickCreateOutput);
            return component;
        }

    }

    private static IComponent chooseOneComponent(List<IComponent> neededComponents, EDatabaseComponentName name,
            boolean quickCreateInput, boolean quickCreateOutput) {
        if (neededComponents.isEmpty()) {
            return null;
        }
        if (neededComponents.size() == 1) {
            return neededComponents.get(0);
        }

        IComponent inputComponent = getComponentByName(name.getInputComponentName(), quickCreateInput, neededComponents);
        if (inputComponent != null) {
            return inputComponent;
        }
        IComponent outputComponent = getComponentByName(name.getOutPutComponentName(), quickCreateOutput, neededComponents);
        if (outputComponent != null) {
            return outputComponent;
        }

        IComponent defaultComponent = getComponentByName(name.getDefaultComponentName(), true, neededComponents);
        if (defaultComponent != null) {
            return defaultComponent;
        } else {
            return neededComponents.get(0);
        }
    }

    private static IComponent getComponentByName(String name, boolean quickCreate, List<IComponent> neededComponents) {
        if (quickCreate) {
            for (IComponent component : neededComponents) {
                if (component.getName().equals(name)) {
                    return component;
                }
            }
        }
        return null;
    }

    public static List<IComponent> filterNeededComponents(Item item, ERepositoryObjectType type) {
        EDatabaseComponentName name = EDatabaseComponentName.getCorrespondingComponentName(item, type);
        String productNameWanted = filterProductNameWanted(name, item);
        boolean hl7Related = false;
        boolean hl7Output = false;
        if (item instanceof HL7ConnectionItem) {
            hl7Related = true;
            EList list = ((HL7Connection) ((HL7ConnectionItem) item).getConnection()).getRoot();
            if (list != null && list.size() > 0) {
                hl7Output = true;
            }
        }

        Collection<IComponent> components = ComponentsFactoryProvider.getInstance().readComponents();
        List<IComponent> neededComponents = new ArrayList<IComponent>();

        EmfComponent emfComponent = null;
        for (IComponent component : components) {
            if (component instanceof EmfComponent) {
                emfComponent = (EmfComponent) component;
                String componentProductname = emfComponent.getRepositoryType();

                boolean value = true;
                if (type == ERepositoryObjectType.METADATA_CON_TABLE) {
                    if (emfComponent.getName().toUpperCase().endsWith(MAP)) {
                        value = false;
                    }
                }
                if (hl7Output && !component.getName().equals("tHL7Output")) { //$NON-NLS-1$
                    value = false;
                } else if (hl7Related && !hl7Output && !component.getName().equals("tHL7Input")) {// bug15632
                    value = false;
                }

                boolean flag = filterComponent(component, name, type);

                if (((componentProductname != null && productNameWanted.endsWith(componentProductname)) && value) || flag) {
                    neededComponents.add(emfComponent);
                }
            }
        }
        return sortFilteredComponnents(item, type, neededComponents);
    }

    private static String filterProductNameWanted(EDatabaseComponentName name, Item item) {
        String productNameWanted = name.getProductName();

        if (item instanceof XmlFileConnectionItem) {
            XmlFileConnection connection = (XmlFileConnection) ((XmlFileConnectionItemImpl) item).getConnection();
            if (!connection.isInputModel()) {
                productNameWanted = "XMLOUTPUT"; //$NON-NLS-1$
            }
        }
        if (item instanceof WSDLSchemaConnectionItem) {
            WSDLSchemaConnection connection = (WSDLSchemaConnection) ((WSDLSchemaConnectionItem) item).getConnection();
            if (!connection.isIsInputModel()) {
                productNameWanted = "WEBSERVICE"; //$NON-NLS-1$
            }
        }
        return productNameWanted;
    }

    private static boolean filterComponent(IComponent component, EDatabaseComponentName name, ERepositoryObjectType type) {
        if (component != null && name != null && type != null) {
            String originalFamilyName = component.getOriginalFamilyName();
            if (originalFamilyName.startsWith("ELT")) { //$NON-NLS-1$
                String dbType = name.getDBType();
                switch (name) {
                case DBORACLEFORSID:
                case DBORACLESN:
                    dbType = EDatabaseTypeName.ORACLEFORSID.getProduct();
                }
                if (dbType != null && originalFamilyName.toUpperCase().endsWith(dbType.toUpperCase())) {
                    if (type == ERepositoryObjectType.METADATA_CONNECTIONS) {
                        if (component.getName().toUpperCase().endsWith(MAP)) {
                            return true;
                        }
                    }
                    if (type == ERepositoryObjectType.METADATA_CON_TABLE) {
                        if (!component.getName().toUpperCase().endsWith(MAP)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private static List<IComponent> sortFilteredComponnents(Item item, ERepositoryObjectType type,
            List<IComponent> neededComponents) {

        List<IComponent> normalTopComponents = new ArrayList<IComponent>();
        List<IComponent> specialTopComponents = new ArrayList<IComponent>();

        for (IComponent component : neededComponents) {
            String name = component.getName();
            if (name.contains(OUTPUT) || name.contains(INPUT)) {
                normalTopComponents.add(component);
            } else if (isSpecialTop(item, type, component)) {
                specialTopComponents.add(component);
            }
        }

        List<IComponent> sortedComponents = new ArrayList<IComponent>();
        sortedComponents.addAll(specialTopComponents);
        sortedComponents.addAll(normalTopComponents);

        // add the left components
        neededComponents.removeAll(specialTopComponents);
        neededComponents.removeAll(normalTopComponents);
        sortedComponents.addAll(neededComponents);

        return sortedComponents;
    }

    private static boolean isSpecialTop(Item item, ERepositoryObjectType type, IComponent component) {
        // for MPx component
        if (component.getName().startsWith("tFS")) { //$NON-NLS-1$
            return true;
        }
        return false;
    }

    public static void removeRejectConnector(INode node) {
        for (INodeConnector connector : node.getListConnector()) {
            if ("VALIDATION_REJECT".equals(connector.getName())) { //$NON-NLS-1$
                node.getListConnector().remove(connector);
                return;
            }
        }
    }

    public static void removeRejectConnection(INode node) {
        List<Connection> connectionList = new ArrayList<Connection>();
        for (IConnection connection : node.getOutgoingConnections()) {
            if ("VALIDATION_REJECT".equals(connection.getConnectorName()) && connection instanceof Connection) { //$NON-NLS-1$
                connectionList.add((Connection) connection);
                break;
            }
        }
        if (connectionList.size() > 0) {
            new ConnectionDeleteCommand(connectionList).execute();
        }
    }

    public static boolean isHasValidationRule(INode node) {
        IElementParameter validationParam = node.getElementParameter(EParameterName.VALIDATION_RULES.getName());
        if (validationParam != null && validationParam.getValue() != null && ((Boolean) validationParam.getValue())) {
            return true;
        }
        return false;
    }

    public static INodeConnector createRejectConnector(INode node) {
        INodeConnector rejectConnector = null;
        if (isHasValidationRule(node)) {
            boolean isHas = false;
            for (INodeConnector connector : node.getListConnector()) {
                if ("VALIDATION_REJECT".equals(connector.getName())) {//$NON-NLS-1$
                    isHas = true;
                }
            }
            if (!isHas) {
                rejectConnector = new NodeConnector(node);
                rejectConnector.setDefaultConnectionType(EConnectionType.FLOW_MAIN);
                rejectConnector.setLinkName("Validation Reject");//$NON-NLS-1$
                rejectConnector.setMenuName("Validation Reject");//$NON-NLS-1$
                rejectConnector.setMaxLinkInput(-1);
                rejectConnector.setMinLinkInput(0);
                rejectConnector.setMaxLinkOutput(1);
                rejectConnector.setMinLinkOutput(0);
                rejectConnector.setBuiltIn(false);
                rejectConnector.setMultiSchema(false);
                rejectConnector.setMergeAllowDifferentSchema(false);
                rejectConnector.setName("VALIDATION_REJECT");//$NON-NLS-1$
                RGB rgb = new RGB(243, 99, 0);
                rejectConnector.addConnectionProperty(EConnectionType.FLOW_MAIN, rgb, 2);
                rejectConnector.getConnectionProperty(EConnectionType.FLOW_MAIN).setRGB(rgb);
                rejectConnector.setBaseSchema("FLOW");//$NON-NLS-1$
                rejectConnector.addConnectionProperty(EConnectionType.FLOW_REF, rgb, 2);
                rejectConnector.addConnectionProperty(EConnectionType.FLOW_MERGE, rgb, 2);

                if (!node.getListConnector().contains(rejectConnector)) {
                    ((List<INodeConnector>) node.getListConnector()).add(rejectConnector);
                }
            }
        }

        return rejectConnector;
    }

    public static void updateRejectMetatable(INode node, INode refNode) {
        if (!isHasValidationRule(node)) {
            return;
        }
        List<IMetadataTable> metadatas = node.getMetadataList();
        if (metadatas != null && metadatas.size() > 0) {
            IMetadataTable table = null;
            boolean isHasRejectTable = false;
            for (IMetadataTable metadataTable : metadatas) {
                if ("VALIDATION_REJECT".equals(metadataTable.getTableName())) {//$NON-NLS-1$
                    isHasRejectTable = true;
                    table = metadataTable;
                    break;
                }
            }
            if (!isHasRejectTable) {
                table = new MetadataTable();
            }
            table.setAttachedConnector("VALIDATION_REJECT");//$NON-NLS-1$
            table.setTableName("VALIDATION_REJECT");//$NON-NLS-1$
            table.setReadOnly(true);

            List<? extends IConnection> mainConnection = null;
            if (refNode != null) {
                mainConnection = refNode.getIncomingConnections(EConnectionType.FLOW_MAIN);
            } else {
                mainConnection = node.getIncomingConnections(EConnectionType.FLOW_MAIN);
            }
            IConnection connection = null;
            if (mainConnection != null && mainConnection.size() > 0) {
                table.getListColumns().clear();
                connection = mainConnection.get(0);
            } else {
                List<? extends IConnection> outgoingConnections;
                if (refNode != null) {
                    outgoingConnections = refNode.getOutgoingConnections("FLOW");//$NON-NLS-1$
                } else {
                    outgoingConnections = node.getOutgoingConnections("FLOW");//$NON-NLS-1$
                }
                if (outgoingConnections != null && outgoingConnections.size() > 0) {
                    connection = outgoingConnections.get(0);
                }
            }
            if (connection != null) {
                IMetadataTable inputTable = connection.getMetadataTable();
                if (inputTable != null) {
                    MetadataToolHelper.copyTable(null, inputTable, table);
                }
            }
            List<IMetadataColumn> listColumns = table.getListColumns();
            boolean isHasErrorMsgCol = false;
            for (IMetadataColumn metadataColumn : listColumns) {
                if ("errorMessage".equals(metadataColumn.getLabel())) {//$NON-NLS-1$
                    isHasErrorMsgCol = true;
                    break;
                }
            }
            if (!isHasErrorMsgCol) {
                MetadataColumn column = new MetadataColumn();
                column.setId("30");//$NON-NLS-1$
                column.setKey(false);
                column.setLabel("errorMessage");//$NON-NLS-1$
                column.setLength(255);
                column.setOriginalDbColumnName("errorMessage");//$NON-NLS-1$
                column.setPrecision(0);
                column.setCustom(true);
                column.setReadOnly(true);
                column.setNullable(true);
                column.setTalendType("id_String"); //$NON-NLS-1$
                table.getListColumns().add(column);
                if (!isHasRejectTable) {
                    node.getMetadataList().add(table);
                }
            }
        }
    }

    public static void appendRemoveValidationRuleCommands(CompoundCommand cc, IElement elem) {
        List<Command> commands = getRemoveValidationRuleCommands(elem);
        for (Command command : commands) {
            cc.add(command);
        }
    }

    public static List<Command> getRemoveValidationRuleCommands(IElement elem) {
        List<Command> commands = new ArrayList<Command>();
        commands.add(new PropertyChangeCommand(elem, EParameterName.VALIDATION_RULES.getName(), false));
        commands.add(new ChangeValuesFromRepository(elem, null, "VALIDATION_RULE_TYPE:VALIDATION_RULE_TYPE", //$NON-NLS-1$
                EmfComponent.BUILTIN));
        commands.add(new PropertyChangeCommand(elem, EParameterName.REPOSITORY_VALIDATION_RULE_TYPE.getName(), "")); //$NON-NLS-1$

        return commands;
    }
}
