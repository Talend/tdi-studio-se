// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.generic.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Composite;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.runtime.model.components.IComponentConstants;
import org.talend.commons.ui.swt.actions.ITreeContextualAction;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.components.api.wizard.WizardImageType;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.metadata.Dbms;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataTalendType;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.INode;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.runtime.services.IGenericDBService;
import org.talend.core.runtime.services.IGenericWizardService;
import org.talend.core.utils.ReflectionUtils;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.designer.core.IUnifiedComponentService;
import org.talend.designer.core.generic.model.GenericComponent;
import org.talend.designer.core.generic.model.GenericElementParameter;
import org.talend.designer.core.generic.model.GenericTableUtils;
import org.talend.designer.core.generic.utils.ComponentsUtils;
import org.talend.designer.core.generic.utils.SchemaUtils;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.model.components.UnifiedJDBCBean;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.utils.UnifiedComponentUtil;
import org.talend.repository.generic.action.GenericAction;
import org.talend.repository.generic.internal.IGenericWizardInternalService;
import org.talend.repository.generic.internal.service.GenericWizardInternalService;
import org.talend.repository.generic.model.genericMetadata.SubContainer;
import org.talend.repository.generic.ui.DBDynamicComposite;
import org.talend.repository.generic.ui.DynamicComposite;
import org.talend.repository.generic.util.GenericConnectionUtil;
import org.talend.repository.generic.util.RepTypeMappingManager;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.RepositoryNode;

import orgomg.cwm.objectmodel.core.TaggedValue;

/**
 * created by ycbai on 2015年9月9日 Detailled comment
 *
 */
public class GenericWizardService implements IGenericWizardService {

    private IGenericWizardInternalService internalService = null;

    List<String> typeNames = new ArrayList<>();

    public GenericWizardService() {
        internalService = new GenericWizardInternalService();
    }

    @Override
    public List<RepositoryNode> createNodesFromComponentService(RepositoryNode curParentNode) {
        List<RepositoryNode> repNodes = new ArrayList<>();
        Set<ComponentWizardDefinition> wizardDefinitions = internalService.getComponentService().getTopLevelComponentWizards();
        for (ComponentWizardDefinition wizardDefinition : wizardDefinitions) {
            String name = wizardDefinition.getName();
            String displayName = wizardDefinition.getDisplayName();
            String folder = "metadata/" + name; //$NON-NLS-1$
            int ordinal = 100;
            ERepositoryObjectType repositoryType = internalService.createRepositoryType(name, displayName, name, folder, ordinal);
            if (curParentNode == null && "JDBC".equals(name)) { //$NON-NLS-1$
                Class<ComponentProperties> jdbcClass = ReflectionUtils.getClass(
                        "org.talend.components.jdbc.wizard.JDBCConnectionWizardProperties",
                        wizardDefinition.getClass().getClassLoader());
                if (jdbcClass != null && wizardDefinition.supportsProperties(jdbcClass)) {
                    IGenericDBService dbService = null;
                    if (GlobalServiceRegister.getDefault().isServiceRegistered(IGenericDBService.class)) {
                        dbService = (IGenericDBService) GlobalServiceRegister.getDefault().getService(IGenericDBService.class);
                    }
                    if (dbService != null) {
                        dbService.getExtraTypes().add(repositoryType);
                    }
                }
            }
            if (curParentNode != null && !needHide(repositoryType)) {
                repNodes.add(internalService.createRepositoryNode(curParentNode, wizardDefinition.getDisplayName(),
                        repositoryType, ENodeType.SYSTEM_FOLDER));
            }
        }
        return repNodes;
    }

    private boolean needHide(ERepositoryObjectType type) {
        if (type == null) {
            return false;
        }
        List<ERepositoryObjectType> extraTypes = new ArrayList<ERepositoryObjectType>();
        IGenericDBService dbService = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IGenericDBService.class)) {
            dbService = (IGenericDBService) GlobalServiceRegister.getDefault().getService(IGenericDBService.class);
        }
        if (dbService != null) {
            extraTypes.addAll(dbService.getExtraTypes());
        }
        return extraTypes.contains(type);
    }

    @Override
    public List<String> getGenericTypeNames() {
        if (typeNames.isEmpty()) {
            Set<ComponentWizardDefinition> wizardDefinitions = internalService.getComponentService()
                    .getTopLevelComponentWizards();
            for (ComponentWizardDefinition wizardDefinition : wizardDefinitions) {
                typeNames.add(wizardDefinition.getName());
            }
        }
        return typeNames;
    }

    @Override
    public boolean isGenericType(ERepositoryObjectType repObjType) {
        if (repObjType == null) {
            return false;
        }
        try {
	        List<String> genericTypeNames = getGenericTypeNames();
	        if (genericTypeNames != null && genericTypeNames.contains(repObjType.getType())) {
	            return true;
	        }
        } catch (Exception e) {
        	// only log the error, might happens during junit execution
        	ExceptionHandler.process(e);
        }
        return false;
    }

    @Override
    public boolean isGenericItem(Item item) {
        if (item != null && item instanceof ConnectionItem) {
            return ((ConnectionItem) item).getConnection().getCompProperties() != null;
        }
        return false;
    }

    @Override
    public boolean isGenericConnection(Connection connection) {
        return connection != null && connection.getCompProperties() != null;
    }

    @Override
    public Image getNodeImage(String typeName) {
        InputStream imageStream = internalService.getComponentService().getWizardPngImage(typeName,
                WizardImageType.TREE_ICON_16X16);
        if (imageStream == null) {
            return null;
        }
        // node image ImageProvider.getImageDesc(ECoreImage.METADATA_TABLE_ICON)
        ImageData id = new ImageData(imageStream);
        Image image = new Image(null, id);
        return image;
    }

    @Override
    public Image getWiardImage(String typeName) {
        InputStream imageStream = internalService.getComponentService().getWizardPngImage(typeName,
                WizardImageType.WIZARD_BANNER_75X66);
        ImageData id = new ImageData(imageStream);
        Image image = new Image(null, id);
        return image;
    }

    @Override
    public List<MetadataTable> getMetadataTables(Connection connection) {
        List<MetadataTable> metadataTables = new ArrayList<>();
        if (connection != null) {
            return SchemaUtils.getMetadataTables(connection, SubContainer.class);
        }
        return metadataTables;
    }

    @Override
    public Composite creatDynamicComposite(Composite composite, Element element, EComponentCategory sectionCategory,
            boolean isCompactView) {
        DynamicComposite dynamicComposite = null;
        if (element != null && element instanceof INode) {
            INode node = (INode) element;
            ComponentProperties props = null;
            if (node.getComponentProperties() == null) {
                props = ComponentsUtils.getComponentProperties(node.getComponent().getName());
            } else {
                props = node.getComponentProperties();
            }
            if (props != null) {
                Form form = props.getForm(EComponentCategory.ADVANCED.equals(sectionCategory) ? Form.ADVANCED : Form.MAIN);
                dynamicComposite = new DynamicComposite(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_FOCUS, sectionCategory,
                        element, isCompactView, composite.getBackground(), form);
                List<ElementParameter> elementParameters = (List<ElementParameter>) node.getElementParameters();
                for (ElementParameter parameter : elementParameters) {
                    if (parameter instanceof GenericElementParameter) {
                        GenericElementParameter genericElementParameter = (GenericElementParameter) parameter;
                        genericElementParameter.callBeforePresent();
                        genericElementParameter.removePropertyChangeListener(dynamicComposite);
                        genericElementParameter.addPropertyChangeListener(dynamicComposite);
                    }
                }
            }
        }
        return dynamicComposite;
    }

    @Override
    public void refreshDynamicComposite(Composite composite) {
        if (composite instanceof DynamicComposite) {
            ((DynamicComposite) composite).resetParameters(false);
        }
    }

    @Override
    public void updateComponentSchema(INode node, IMetadataTable metadataTable) {
        SchemaUtils.updateComponentSchema(node, metadataTable, Boolean.FALSE);
    }

    @Override
    public List<ComponentProperties> getAllComponentProperties(Connection connection, String tableLabel, boolean withEvaluator) {
        return getAllComponentProperties(connection, tableLabel, withEvaluator, false, new HashMap<Object, Object>());
    }

    @Override
    public List<ComponentProperties> getAllComponentProperties(Connection connection, String tableLabel, boolean withEvaluator,
            boolean forComponentValue, Map<Object, Object> contextMap) {
        List<ComponentProperties> componentProperties = new ArrayList<>();
        Set<ComponentProperties> componentPropertiesSet = new HashSet<>();
        if(contextMap == null){
            contextMap = new HashMap<Object, Object>();
        }
        if (isGenericConnection(connection)) {
            String compProperties = connection.getCompProperties();
            ComponentProperties cp = null;
            if(contextMap.get(connection.getId()) != null){
                cp = (ComponentProperties) contextMap.get(connection.getId());
            }else{
                cp = ComponentsUtils.getComponentPropertiesFromSerialized(compProperties, connection,
                        withEvaluator);
                if(cp != null){
                    contextMap.put(connection.getId(), cp);
                }
            }
            if (cp != null) {
                componentProperties.add(cp);
            }
            List<MetadataTable> metadataTables;
            //"forComponentValue" is avoid to load all the metadataTables,
            //if just get the component value, totally no need to get hundreds of tables,
            if (tableLabel == null && !forComponentValue) {
                metadataTables = SchemaUtils.getMetadataTables(connection, SubContainer.class);
            } else {
                metadataTables = Arrays.asList(SchemaUtils.getMetadataTable(connection, tableLabel, SubContainer.class));
            }
            if(metadataTables == null){
                return componentProperties;
            }
            for (MetadataTable metadataTable : metadataTables) {
                if (metadataTable == null) {
                    continue;
                }
                for (TaggedValue taggedValue : metadataTable.getTaggedValue()) {
                    if (IComponentConstants.COMPONENT_PROPERTIES_TAG.equals(taggedValue.getTag())) {
                        ComponentProperties compPros = null;
                        if(contextMap.get(metadataTable.getId()) != null){
                            compPros = (ComponentProperties) contextMap.get(metadataTable.getId());
                        }else{
                            compPros = ComponentsUtils.getComponentPropertiesFromSerialized(taggedValue.getValue(), connection, withEvaluator);
                            if(compPros != null){
                                contextMap.put(metadataTable.getId(), compPros);
                            }
                        }
                        if (compPros != null && !componentPropertiesSet.contains(compPros)) {
                            compPros.updateNestedProperties(cp);
                            componentProperties.add(compPros);
                            componentPropertiesSet.add(compPros);
                        }
                    }
                }
            }
        }
        return componentProperties;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.core.runtime.services.IGenericWizardService#getAllComponentProperties(org.talend.core.model.metadata.
     * builder.connection.Connection, java.lang.String, boolean)
     */
    @Override
    public List<ComponentProperties> getAllComponentProperties(Connection connection, String tableLabel) {
        return getAllComponentProperties(connection, tableLabel, false);
    }

    @Override
    public ERepositoryObjectType getNewRepType(String oldRepTypeName) {
        return RepTypeMappingManager.getInstance().getNewRepType(oldRepTypeName);
    }

    @Override
    public String getConnectionProperties(Connection connection) {
        if (isGenericConnection(connection)) {
            String compProperties = connection.getCompProperties();
            return compProperties;
        }
        return null;
    }

    @Override
    public ITreeContextualAction getDefaultAction(RepositoryNode node) {
        if (node == null) {
            return null;
        }
        ITreeContextualAction defaultAction = null;
        ComponentWizard editWizard = GenericConnectionUtil.getEditWizard(node);
        if (editWizard != null) {
            defaultAction = new GenericAction(editWizard);
        }
        return defaultAction;
    }

    @Override
    public void initAdditionalJDBCRepositoryObjType() {
        List<ERepositoryObjectType> types = new ArrayList<ERepositoryObjectType>();
        Map<String, UnifiedJDBCBean> additionalJDBC = UnifiedComponentUtil.getAdditionalJDBC();
        if (additionalJDBC.keySet().isEmpty()) {
            return;
        }
        Collection<UnifiedJDBCBean> beans = additionalJDBC.values();
        for (UnifiedJDBCBean bean : beans) {
            ERepositoryObjectType type = internalService.createRepositoryType(bean.getDisplayName(), bean.getDisplayName(),
                    bean.getComponentKey(), "metadata/connections", 100);
            types.add(type);
        }
        IGenericDBService dbService = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IGenericDBService.class)) {
            dbService = (IGenericDBService) GlobalServiceRegister.getDefault().getService(IGenericDBService.class);
        }
        if (dbService != null && !types.isEmpty()) {
            dbService.getExtraTypes().addAll(types);
        }
    }

    @Override
    public boolean getIfAdditionalJDBCDBType(String dbType) {
        Map<String, UnifiedJDBCBean> additionalJDBC = UnifiedComponentUtil.getAdditionalJDBC();
        return additionalJDBC.get(dbType) != null;
    }

    @Override
    public void initAdditonalJDBCConnectionValue(DatabaseConnection connection, Composite dynamicForm, String dbType, String propertyId) {
        if (!(dynamicForm instanceof DBDynamicComposite)) {
            return;
        }
        DBDynamicComposite dynamicFormComposite = (DBDynamicComposite) dynamicForm;
        Map<String, UnifiedJDBCBean> additionalJDBC = UnifiedComponentUtil.getAdditionalJDBC();
        // set new form first, otherwise would keep before value
        ComponentWizard componentWizard = internalService.getComponentWizard("JDBC", propertyId);
        if (componentWizard != null) {
            dynamicFormComposite.setForm(componentWizard.getForms().get(0));
        }

        Dbms mysqlDbms = MetadataTalendType.getDefaultDbmsFromProduct(EDatabaseTypeName.MYSQL.getProduct().toUpperCase());
        if (additionalJDBC.get(dbType) != null) {
            // additional jdbc
            Properties componentProperties = dynamicFormComposite.getForm().getProperties();
            UnifiedJDBCBean bean = additionalJDBC.get(dbType);
            connection.setProductId(dbType);
            connection.setUsername(null);
            connection.setPassword(null);
            connection.setURL(bean.getUrl());
            connection.setDriverClass(bean.getDriverClass());
            String driverJarPaths = GenericTableUtils.getDriverJarPaths(bean.getPaths());
            if (StringUtils.isNotBlank(driverJarPaths)) {
                connection.setDriverJarPath(driverJarPaths);
            }
            UnifiedJDBCBean unifiedJDBCBean = additionalJDBC.get(dbType);
            Dbms dbms = MetadataTalendType.getDefaultDbmsFromProduct(unifiedJDBCBean.getDatabaseId());
            String dbmsId = null;
            if (dbms.getProduct().equals(unifiedJDBCBean.getDatabaseId())) {
                dbmsId = dbms.getId();
            } else {
                // avoid not found return default one
                dbmsId = mysqlDbms.getId();
            }
            if (StringUtils.isNotBlank(dbmsId)) {
                connection.setDbmsId(dbmsId);
                NamedThing thing = componentProperties.getProperty("mappingFile");
                if (thing != null) {
                    Property property = (Property) thing;
                    property.setValue(dbmsId);
                }
            }
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("jdbcUrl", TalendQuoteUtils.addQuotes(bean.getUrl()));
            map.put("driverClass", TalendQuoteUtils.addQuotes(bean.getDriverClass()));
            map.put("drivers", bean.getPaths());
            UnifiedComponentUtil.setCompPropertiesForJDBC(componentProperties, map);
        } else {
            // set to empty original jdbc for switch dbtype
            connection.setUsername(null);
            connection.setPassword(null);
            connection.setURL(null);
            connection.setDriverClass(null);
            connection.setDriverJarPath(null);
            if (mysqlDbms != null && StringUtils.isNotBlank(mysqlDbms.getId())) {
                connection.setDbmsId(mysqlDbms.getId());
                NamedThing thing = dynamicFormComposite.getForm().getProperties().getProperty("mappingFile");
                if (thing != null) {
                    Property property = (Property) thing;
                    property.setValue(mysqlDbms.getId());
                }
            }
        }
        dynamicFormComposite.resetParameters(true);
        dynamicFormComposite.refresh();
    }

    @Override
    public String getDatabseNameByNode(IElement node) {
        if (node != null && node instanceof Node) {
            Node editorNode = (Node) node;
            if (editorNode.getDelegateComponent() != null) {
                // get database
                IComponent delegateComponent = editorNode.getDelegateComponent();
                String emfComponent = editorNode.getComponent().getDisplayName();
                if (UnifiedComponentUtil.isDelegateComponent(delegateComponent)) {
                    IUnifiedComponentService service = GlobalServiceRegister.getDefault()
                            .getService(IUnifiedComponentService.class);
                    return service.getUnifiedCompDisplayName(delegateComponent, emfComponent);
                }
            }
        }
        return null;
    }

    @Override
    public String getDefinitionName4AdditionalJDBC(IElement element) {
        if (!(element instanceof Node)) {
            return null;
        }
        Node node = (Node) element;
        String databaseName = this.getDatabseNameByNode(node);
        if (StringUtils.isNotBlank(databaseName) && UnifiedComponentUtil.isAdditionalJDBC(databaseName)) {
            IComponent component = node.getComponent();
            if (component instanceof GenericComponent) {
                GenericComponent genericComp = (GenericComponent) component;
                return genericComp.getComponentDefinition().getName();
            }
        }
        return null;
    }

    @Override
    public Dbms getDbms4AdditionalJDBC(String typeName) {
        if (!UnifiedComponentUtil.isAdditionalJDBC(typeName)) {
            return null;
        }
        UnifiedJDBCBean unifiedJDBCBean = UnifiedComponentUtil.getAdditionalJDBC().get(typeName);
        if (unifiedJDBCBean != null) {
            String databaseId = unifiedJDBCBean.getDatabaseId();
            Dbms dbms = MetadataTalendType.getDefaultDbmsFromProduct(databaseId);
            if (databaseId.equals(dbms.getProduct())) {
                // avoid not found return default one
                return dbms;
            }
        }
        return null;
    }

}
