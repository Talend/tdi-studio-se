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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.talend.commons.runtime.model.components.IComponentConstants;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ITDQRepositoryService;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.metadata.IMetadataConnection;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataToolAvroHelper;
import org.talend.core.model.metadata.MetadataToolHelper;
import org.talend.core.model.metadata.builder.ConvertionHelper;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.maven.MavenUrlHelper;
import org.talend.core.runtime.services.IGenericDBService;
import org.talend.core.runtime.util.GenericTypeUtils;
import org.talend.core.ui.check.IChecker;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.presentation.Form;
import org.talend.designer.core.generic.constants.IGenericConstants;
import org.talend.designer.core.generic.model.GenericElementParameter;
import org.talend.designer.core.generic.model.GenericTableUtils;
import org.talend.designer.core.generic.utils.ComponentsUtils;
import org.talend.designer.core.model.FakeElement;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.metadata.managment.utils.MetadataConnectionUtils;
import org.talend.repository.generic.internal.IGenericWizardInternalService;
import org.talend.repository.generic.internal.service.GenericWizardInternalService;
import org.talend.repository.generic.persistence.GenericRepository;
import org.talend.repository.generic.ui.DBDynamicComposite;
import org.talend.repository.generic.ui.DynamicComposite;
import org.talend.repository.generic.ui.context.ContextComposite;
import org.talend.repository.generic.ui.context.handler.GenericContextHandler;
import org.talend.repository.generic.update.GenericUpdateManager;
import org.talend.repository.generic.util.GenericContextUtil;
import org.talend.repository.generic.util.GenericWizardServiceFactory;
import org.talend.repository.model.IProxyRepositoryFactory;

import orgomg.cwm.objectmodel.core.CoreFactory;
import orgomg.cwm.objectmodel.core.TaggedValue;

/**
 * DOC hwang  class global comment. Detailled comment
 */
public class GenericDBService implements IGenericDBService{

    private static List<ERepositoryObjectType> extraTypes = new ArrayList<ERepositoryObjectType>();

    @Override
    public Map<String, Composite> creatDBDynamicComposite(Composite composite, EComponentCategory sectionCategory, boolean isReadOnly, boolean isCreation,
            Property property, String typeName) {
        Map<String, Composite> map = new HashMap<String, Composite>();
        IGenericWizardInternalService internalService = new GenericWizardInternalService();
        Item item = property.getItem();
        if(!(item instanceof ConnectionItem)){
            return map;
        }
        ConnectionItem gitem = (ConnectionItem) item;
        Connection connection = gitem.getConnection();
        ComponentWizard componentWizard = internalService.getComponentWizard(typeName, property.getId());
        if(!isCreation && ((ConnectionItem)item).getConnection().getCompProperties() != null){
            ComponentProperties componentProperties = ComponentsUtils
                    .getComponentPropertiesFromSerialized(connection.getCompProperties(), connection);
            List<ComponentWizard> wizards = GenericWizardServiceFactory.getGenericWizardInternalService()
                    .getComponentWizardsForProperties(componentProperties, gitem.getProperty().getId());
            for (ComponentWizard wizard : wizards) {
                ComponentWizardDefinition wizardDefinition = wizard.getDefinition();
                if (wizardDefinition.isTopLevel()) {
                    continue;
                }
                String wizardName = wizardDefinition.getName();
                if (wizardName.toLowerCase().contains("edit")) { //$NON-NLS-1$
                    componentWizard = wizard;
                    break;
                }
            }
        }

        List<Form> forms = componentWizard.getForms();
        Element baseElement = new FakeElement("");//$NON-NLS-1$
        Form form = forms.get(0);

        // Set a default mappingFile for JDBC when creating
        if (isCreation && "JDBC".equalsIgnoreCase(typeName)) { //$NON-NLS-1$
            form.setValue("mappingFile", "mysql_id"); //$NON-NLS-1$//$NON-NLS-2$
        }

        DBDynamicComposite dynamicComposite = new DBDynamicComposite(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_FOCUS, EComponentCategory.BASIC,
                baseElement, (ConnectionItem) property.getItem(), true, composite.getBackground(), form, false);
        dynamicComposite.setLayoutData(createMainFormData(true));
        map.put("DynamicComposite", dynamicComposite);

        if(isCreation && ((ConnectionItem)item).getConnection().getCompProperties() != null){
            ComponentProperties componentProperties = ComponentsUtils
                    .getComponentPropertiesFromSerialized(connection.getCompProperties(), connection);
            for(IElementParameter param : baseElement.getElementParameters()){
                NamedThing thing = componentProperties.getProperty(param.getName());
                if(thing == null){
                    continue;
                }
                if(thing instanceof org.talend.daikon.properties.property.Property){
                    param.setValue(ComponentsUtils.getParameterValue(baseElement,
                            (org.talend.daikon.properties.property.Property)thing, param.getFieldType(), param.getName()));
                }else if(thing instanceof Properties){
                    param.setValue(GenericTableUtils.getTableValues(((Properties)thing), param));
                }
            }
            dynamicComposite.resetParameters(false);
        }

        Composite contextParentComp = new Composite(composite, SWT.NONE);
        contextParentComp.setLayoutData(createFooterFormData(dynamicComposite));
        contextParentComp.setLayout(new GridLayout());

        GenericContextHandler contextHandler = new GenericContextHandler();
        contextHandler.setParameters(getContextParameters(baseElement));
        ContextComposite contextComp = new ContextComposite(contextParentComp, (ConnectionItem)property.getItem(), isReadOnly,
                contextHandler);

        contextComp.addPropertyChangeListener(dynamicComposite);
        contextComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        map.put("ContextComposite", contextComp);
        return map;
    }

    private List<IElementParameter> getContextParameters(Element element) {
        List<IElementParameter> contextParameters = new ArrayList<>();
        for (IElementParameter parameter : element.getElementParameters()) {
            if (parameter instanceof GenericElementParameter) {
                GenericElementParameter genericElementParameter = (GenericElementParameter) parameter;
                if (genericElementParameter.isSupportContext()) {
                    contextParameters.add(parameter);
                }
                List<ElementParameter> relatedParameters = ComponentsUtils.getRelatedParameters(genericElementParameter);
                for (ElementParameter relatedParameter : relatedParameters) {
                    if (relatedParameter instanceof GenericElementParameter
                            && ((GenericElementParameter) relatedParameter).isSupportContext()) {
                        contextParameters.add(relatedParameter);
                    }
                }
            }
        }
        return contextParameters;
    }

    private FormData createMainFormData(boolean addContextSupport) {
        FormData data = new FormData();
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(100, 0);
        data.top = new FormAttachment(0, 0);
        if (addContextSupport) {
            data.bottom = new FormAttachment(85, 0);
        } else {
            data.bottom = new FormAttachment(100, 0);
        }
        return data;
    }

    private FormData createFooterFormData(Composite topComposite) {
        FormData data = new FormData();
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(100, 0);
        data.top = new FormAttachment(topComposite, 0);
        data.bottom = new FormAttachment(100, 0);
        return data;
    }

    @Override
    public void dbWizardPerformFinish(ConnectionItem item, Form form, boolean creation, IPath pathToSave, List<IMetadataTable> oldMetadataTable, final String contextName) throws CoreException {
        ComponentService compService = new GenericWizardInternalService().getComponentService();
        compService.setRepository(new GenericRepository());
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        final IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        IWorkspaceRunnable operation = new IWorkspaceRunnable() {

            @Override
            public void run(IProgressMonitor monitor) throws CoreException {
                ConnectionItem connItem = item;
                try {
                    if (form != null && form.isCallAfterFormFinish()) {
                        if (creation) {
                            factory.create(connItem, pathToSave);
                        }
                        try {
                            compService.afterFormFinish(form.getName(), form.getProperties());
                        } catch (Throwable e) {
                        }
                    }
                    IRepositoryViewObject repViewObj = factory.getLastVersion(connItem.getProperty().getId());
//                    String compProperties = connItem.getConnection().getCompProperties();
                    if(repViewObj != null){
                        Property property = repViewObj.getProperty();
                        if (property != null) {
                            connItem = (ConnectionItem) property.getItem();
//                            connItem.getConnection().setCompProperties(compProperties);
                        }
                    }
                    Connection connection = connItem.getConnection();
                    convertPropertiesToDBElements(form.getProperties(), connection);
                    // set the name and label to Connection model.
                    String label = connItem.getProperty().getLabel();
                    connection.setName(label);
                    connection.setLabel(label);
                    IMetadataConnection metadataConnection = null;
                    if (contextName == null) {
                        metadataConnection = ConvertionHelper.convert(connection, true);
                    } else {
                        metadataConnection = ConvertionHelper.convert(connection, false, contextName);
                    }
                    MetadataConnectionUtils.fillConnectionInformation(connItem, metadataConnection);
                    factory.save(connItem);
                    updateConnectionOnDQSide(creation, connItem);
                } catch (Throwable e) {
                    e.printStackTrace();
                    throw new CoreException(new Status(IStatus.ERROR, IGenericConstants.REPOSITORY_PLUGIN_ID,
                            "Error when saving the connection", e));
                }
            }
        };
        ISchedulingRule schedulingRule = workspace.getRoot();
        // the update the project files need to be done in the workspace runnable to avoid all
        // notification of changes before the end of the modifications.
        workspace.run(operation, schedulingRule, IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
        // Move it from WorkspaceRunnable to avoid the conflicting rules with other jobs.
        if (!creation) {
            GenericUpdateManager.updateGenericConnection(item, oldMetadataTable);
        }

    }

    /**
     * Update some attributes on DQ side(same as other type database connections)
     *
     * @param creation
     * @param connectionItem
     */
    private void updateConnectionOnDQSide(boolean creation, ConnectionItem connectionItem) {
        ITDQRepositoryService tdqRepService = null;

        if (GlobalServiceRegister.getDefault().isServiceRegistered(ITDQRepositoryService.class)) {
            tdqRepService =
                    (ITDQRepositoryService) GlobalServiceRegister.getDefault().getService(ITDQRepositoryService.class);
        }
        if (tdqRepService != null) {
            // MOD qiongli 2012-11-19 TDQ-6287
            if (creation) {
                tdqRepService.notifySQLExplorer(connectionItem);
            } else {
                tdqRepService.updateAliasInSQLExplorer(connectionItem, connectionItem.getProperty().getDisplayName());
            }
        }
    }

    @Override
    public Form getDynamicForm(Composite composite){
        if(composite != null && composite instanceof DynamicComposite){
            return ((DynamicComposite)composite).getForm();
        }
        return null;
    }

    @Override
    public IChecker getDynamicChecker(Composite dynamicComposite) {
        if(dynamicComposite != null && dynamicComposite instanceof DynamicComposite){
            return ((DynamicComposite)dynamicComposite).getChecker();
        }
        return null;
    }

    @Override
    public void resetConnectionItem(Composite composite, ConnectionItem connectionItem) {
        if(composite instanceof ContextComposite){
            ((ContextComposite)composite).setConnectionItem(connectionItem);
        }else if(composite instanceof DynamicComposite){
            ((DynamicComposite)composite).setConnectionItem(connectionItem);
        }
    }

    @Override
    public List<ERepositoryObjectType> getExtraTypes() {
        return extraTypes;
    }

    @Override
    public void convertPropertiesToDBElements(Properties props,Connection connection){
        if(!(connection instanceof DatabaseConnection)){
            return;
        }
        if(props == null){
            return;
        }
        DatabaseConnection dbConnection = (DatabaseConnection) connection;
        for (NamedThing otherProp : props.getProperties()) {
            NamedThing thisProp = props.getProperty(otherProp.getName());
            if(thisProp == null){
                continue;
            }
            if (otherProp instanceof PropertiesImpl) {
                convertPropertiesToDBElements((Properties) otherProp, connection);
            } else if (otherProp instanceof org.talend.daikon.properties.property.Property) {
                // copy the value
                String proName = ((org.talend.daikon.properties.property.Property) otherProp).getName();
                Object value = ((org.talend.daikon.properties.property.Property) otherProp).getStoredValue();
                if (proName.equals("jdbcUrl")) {//$NON-NLS-1$
                    dbConnection.setURL((String) value);
                } else if (proName.equals("driverClass")) {//$NON-NLS-1$
                    dbConnection.setDriverClass((String) value);
                } else if (proName.equals("userId")) {//$NON-NLS-1$
                    dbConnection.setUsername((String) value);
                } else if (proName.equals("password")) {//$NON-NLS-1$
                    dbConnection.setRawPassword((String) value);
                } else if (proName.equals("mappingFile")) {//$NON-NLS-1$
                    dbConnection.setDbmsId((String) value);
                } else if (proName.equals("drivers") && GenericTypeUtils.isListStringType((org.talend.daikon.properties.property.Property) otherProp)) {//$NON-NLS-1$
                    List<String> listString = (List<String>) value;
                    String jars = GenericTableUtils.getDriverJarPaths(listString);
                    if (jars != null) {
                        dbConnection.setDriverJarPath(jars);
                    }
                    String driverJarUris = GenericTableUtils.getDriverJarUris(listString);
                    if (driverJarUris != null) {
                        dbConnection.setDriverJarUri(driverJarUris);
                    }
                }
            }
        }
    }

    @Override
    public String getMVNPath(String value) {
        boolean containContextParam = ContextParameterUtils.isContainContextParam(value);
        if (!containContextParam) {
            String valueNoQuote = TalendQuoteUtils.removeQuotes(value);
            boolean isMvnUri = MavenUrlHelper.isMvnUrl(valueNoQuote);
            ModuleNeeded module = null;
            if (isMvnUri) {
                module = new ModuleNeeded("", "", true, valueNoQuote);//$NON-NLS-1$ //$NON-NLS-2$
                module.setMavenUri(valueNoQuote);
            } else {
                module = new ModuleNeeded("", valueNoQuote, "", true);//$NON-NLS-1$ //$NON-NLS-2$
            }
            // get maven uri again incase it is customized
            String mvnPath = module.getMavenUri();
            if (mvnPath != null) {
                return TalendQuoteUtils.addQuotesIfNotExist(mvnPath);
            }
        }
        return value;
    }

    @Override
    public IMetadataTable converTable(INode node, IMetadataTable iTable) {
        org.talend.core.model.metadata.builder.connection.MetadataTable table = ConvertionHelper.convert(iTable);
        ComponentProperties properties = node.getComponentProperties();
        Properties mainProperties = properties.getProperties("main");
        Properties flowProperties = properties.getProperties("schemaFlow");
        Properties rejectProperties = properties.getProperties("schemaReject");
        if(mainProperties != null){
            mainProperties.setValue("schema", MetadataToolAvroHelper.convertToAvro(table));
        }
        if(flowProperties != null){
            flowProperties.setValue("schema", MetadataToolAvroHelper.convertToAvro(table));
        }
        if(rejectProperties != null){
            rejectProperties.setValue("schema", MetadataToolAvroHelper.convertToAvro(table));
        }

        TaggedValue serializedPropsTV = CoreFactory.eINSTANCE.createTaggedValue();
        serializedPropsTV.setTag(IComponentConstants.COMPONENT_PROPERTIES_TAG);
        serializedPropsTV.setValue(properties.toSerialized());
        table.getTaggedValue().add(serializedPropsTV);
        TaggedValue schemaPropertyTV = CoreFactory.eINSTANCE.createTaggedValue();
        schemaPropertyTV.setTag(IComponentConstants.COMPONENT_SCHEMA_TAG);
        schemaPropertyTV.setValue("schema");
        table.getTaggedValue().add(schemaPropertyTV);

        iTable = MetadataToolHelper.convert(table);
        return iTable;
    }

    @Override
    public void setPropertyTaggedValue(ComponentProperties properties) {
        List<org.talend.daikon.properties.property.Property> propertyValues = ComponentsUtils.getAllValuedProperties(properties);
        for (org.talend.daikon.properties.property.Property property : propertyValues) {
            property.setTaggedValue(IGenericConstants.REPOSITORY_VALUE, property.getName());
        }
    }

    @Override
    public void initReferencedComponent(IElementParameter refPara, String newValue){
        ComponentsUtils.initReferencedComponent(refPara, newValue);
    }

    @Override
    public Properties getComponentProperties(String typeName, String id) {
        IGenericWizardInternalService internalService = new GenericWizardInternalService();
        ComponentWizard componentWizard = internalService.getComponentWizard(typeName, id);
        List<Form> forms = componentWizard.getForms();
        return forms.get(0).getProperties();
    }

    @Override
    public ERepositoryObjectType getExtraDBType(ERepositoryObjectType type) {
        if(getExtraTypes().contains(type)){
            return ERepositoryObjectType.METADATA_CONNECTIONS;
        }
        return type;
    }

    @Override
    public void updateCompPropertiesForContextMode(Connection connection, Map<String, String> contextVarMap) {
        GenericContextUtil.updateCompPropertiesForContextMode(connection, contextVarMap);
    }

    @Override
    public List<ERepositoryObjectType> getAllGenericMetadataDBRepositoryType() {
        List<ERepositoryObjectType> repoTypes = new ArrayList<ERepositoryObjectType>();
        IGenericWizardInternalService internalService = new GenericWizardInternalService();
        // already init RepositoryObjectType in GenericWizardService#createNodesFromComponentService
        Set<ComponentWizardDefinition> wizardDefinitions = internalService.getComponentService().getTopLevelComponentWizards();
        for (ComponentWizardDefinition componentWizardDefinition : wizardDefinitions) {
            ERepositoryObjectType repoType = ERepositoryObjectType.valueOf(componentWizardDefinition.getName());
            if (repoType != null) {
                repoTypes.add(repoType);
            }
        }
        return repoTypes;
    }

}
