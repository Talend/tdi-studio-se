// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.runtime.services.IGenericDBService;
import org.talend.core.ui.check.IChecker;
import org.talend.daikon.properties.presentation.Form;
import org.talend.designer.core.generic.constants.IGenericConstants;
import org.talend.designer.core.generic.model.GenericElementParameter;
import org.talend.designer.core.generic.utils.ComponentsUtils;
import org.talend.designer.core.model.FakeElement;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.repository.generic.internal.IGenericWizardInternalService;
import org.talend.repository.generic.internal.service.GenericWizardInternalService;
import org.talend.repository.generic.model.genericMetadata.GenericConnection;
import org.talend.repository.generic.model.genericMetadata.GenericConnectionItem;
import org.talend.repository.generic.model.genericMetadata.GenericMetadataFactory;
import org.talend.repository.generic.persistence.GenericRepository;
import org.talend.repository.generic.ui.DBDynamicComposite;
import org.talend.repository.generic.ui.DynamicComposite;
import org.talend.repository.generic.ui.context.ContextComposite;
import org.talend.repository.generic.ui.context.handler.GenericContextHandler;
import org.talend.repository.generic.update.GenericUpdateManager;
import org.talend.repository.generic.util.GenericWizardServiceFactory;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * DOC hwang  class global comment. Detailled comment
 */
public class GenericDBService implements IGenericDBService{
    
    private List<ERepositoryObjectType> extraTypes = new ArrayList<ERepositoryObjectType>();
    
    @Override
    public Map<String, Composite> creatDBDynamicComposite(Composite composite, EComponentCategory sectionCategory, boolean isReadOnly, boolean isCreation,
            Property property, String typeName) {
        Map<String, Composite> map = new HashMap<String, Composite>();
        IGenericWizardInternalService internalService = new GenericWizardInternalService();
        Item item = property.getItem();
        
        ComponentWizard componentWizard = internalService.getComponentWizard(typeName, property.getId());;
        if(!isCreation && (item instanceof GenericConnectionItem)){
            GenericConnectionItem gitem = (GenericConnectionItem) item;
            GenericConnection connection = (GenericConnection) gitem.getConnection();
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
        Element baseElement = new FakeElement(forms.get(0).getName());
        DBDynamicComposite dynamicComposite = new DBDynamicComposite(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_FOCUS, EComponentCategory.BASIC,
                baseElement, true, composite.getBackground(), forms.get(0), false);
        dynamicComposite.setLayoutData(createMainFormData(true));
        dynamicComposite.setConnectionItem((ConnectionItem)property.getItem());
        map.put("DynamicComposite", dynamicComposite);

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
    public Connection createGenericConnection() {
        return GenericMetadataFactory.eINSTANCE.createGenericConnection();
    }

    @Override
    public ConnectionItem createGenericConnectionItem() {
        return GenericMetadataFactory.eINSTANCE.createGenericConnectionItem();
    }

    @Override
    public String getGenericConnectionType(Item item) {
        if(item instanceof GenericConnectionItem){
            return ((GenericConnectionItem)item).getTypeName();
        }
        return null;
    }

    @Override
    public void setGenericConnectionType(String type, Item item) {
        if(item instanceof GenericConnectionItem){
            ((GenericConnectionItem)item).setTypeName(type);
        }
    }

    @Override
    public void dbWizardPerformFinish(ConnectionItem item, Form form, boolean creation, IPath pathToSave, List<IMetadataTable> oldMetadataTable) throws CoreException {
        ComponentService compService = new GenericWizardInternalService().getComponentService();
        compService.setRepository(new GenericRepository());
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        final IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        IWorkspaceRunnable operation = new IWorkspaceRunnable() {

            @Override
            public void run(IProgressMonitor monitor) throws CoreException {
                try {
                    if (form != null && form.isCallAfterFormFinish()) {
                        if (creation) {
                            factory.create(item, pathToSave);
                        }
                        compService.afterFormFinish(form.getName(), form.getProperties());
                    }
                    factory.save(item);
                } catch (Throwable e) {
                    //e.printStackTrace();
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

}
