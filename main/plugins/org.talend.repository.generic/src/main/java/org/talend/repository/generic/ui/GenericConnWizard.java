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
package org.talend.repository.generic.ui;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.swt.dialogs.ErrorDialogWidthDetailArea;
import org.talend.commons.utils.VersionUtils;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.RepositoryObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.core.runtime.services.IGenericDBService;
import org.talend.core.runtime.services.IGenericWizardService;
import org.talend.daikon.properties.presentation.Form;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.designer.core.generic.constants.IGenericConstants;
import org.talend.metadata.managment.ui.utils.ConnectionContextHelper;
import org.talend.metadata.managment.ui.wizard.CheckLastVersionRepositoryWizard;
import org.talend.repository.generic.i18n.Messages;
import org.talend.repository.generic.internal.IGenericWizardInternalService;
import org.talend.repository.generic.internal.service.GenericWizardInternalService;
import org.talend.repository.generic.model.genericMetadata.GenericMetadataFactory;
import org.talend.repository.generic.persistence.GenericRepository;
import org.talend.repository.generic.ui.common.GenericWizardDialog;
import org.talend.repository.generic.ui.common.GenericWizardPage;
import org.talend.repository.generic.update.GenericUpdateManager;
import org.talend.repository.generic.util.GenericWizardServiceFactory;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;

/**
 *
 * created by ycbai on 2015年9月16日 Detailled comment
 *
 */
public class GenericConnWizard extends CheckLastVersionRepositoryWizard {

    private ComponentWizard wizard;

    private GenericWizardPage wizPage;

    private Connection connection;

    private Property connectionProperty;

    private String originalLabel;

    private String originalVersion;

    private String originalPurpose;

    private String originalDescription;

    private String originalStatus;

    private RepositoryNode repNode;

    private ERepositoryObjectType repObjType;

    private List<IMetadataTable> oldMetadataTable;

    private IGenericWizardService wizardService;

    private ComponentService compService;

    private static String location = "repositoryLocation";//$NON-NLS-1$

    public GenericConnWizard(IWorkbench workbench, boolean creation, RepositoryNode node, String[] existingNames) {
        this(workbench, creation, null, node, existingNames);
    }

    public GenericConnWizard(IWorkbench workbench, boolean creation, ComponentWizard componentWizard, RepositoryNode node,
            String[] existingNames) {
        super(workbench, creation);
        wizard = componentWizard;
        this.existingNames = existingNames;
        repNode = node;
        repObjType = repNode.getObjectType();
        if(repObjType == null || repNode.getType() != ENodeType.REPOSITORY_ELEMENT){
            repObjType = (ERepositoryObjectType) repNode.getProperties(EProperties.CONTENT_TYPE);
        }
        wizardService = GenericWizardServiceFactory.getGenericWizardService();
        ENodeType nodeType = node.getType();
        switch (nodeType) {
        case SIMPLE_FOLDER:
        case REPOSITORY_ELEMENT:
            pathToSave = RepositoryNodeUtilities.getPath(node);
            break;
        case SYSTEM_FOLDER:
            pathToSave = new Path(""); //$NON-NLS-1$
            break;
        }
        switch (nodeType) {
        case SIMPLE_FOLDER:
        case SYSTEM_FOLDER:
            connection = GenericMetadataFactory.eINSTANCE.createGenericConnection();
            connectionProperty = PropertiesFactory.eINSTANCE.createProperty();
            // ses the id to be used for persistence lookup
            connectionProperty.setId(ProxyRepositoryFactory.getInstance().getNextId());
            connectionProperty.setAuthor(((RepositoryContext) CoreRuntimePlugin.getInstance().getContext()
                    .getProperty(Context.REPOSITORY_CONTEXT_KEY)).getUser());
            connectionProperty.setVersion(VersionUtils.DEFAULT_VERSION);
            connectionProperty.setStatusCode(""); //$NON-NLS-1$

            connectionItem = GenericMetadataFactory.eINSTANCE.createGenericConnectionItem();

            connectionItem.setProperty(connectionProperty);
            connectionItem.setConnection(connection);
            break;
        case REPOSITORY_ELEMENT:
            RepositoryObject object = new RepositoryObject(node.getObject().getProperty());
            setRepositoryObject(object);
            connection = ((ConnectionItem) object.getProperty().getItem()).getConnection();
            // Set context name to null so as to open context select dialog once if there are more than one context
            // group when opening a connection.
            connection.setContextName(null);
            connectionProperty = object.getProperty();
            connectionItem = (ConnectionItem) object.getProperty().getItem();
            // set the repositoryObject, lock and set isRepositoryObjectEditable
            setRepositoryObject(node.getObject());
            initLockStrategy();
            break;
        }
        if (wizard == null) {
            wizard = getDefaultWizard(repObjType.getType());
        }
        if (!creation) {
            this.originalLabel = this.connectionItem.getProperty().getDisplayName();
            this.originalVersion = this.connectionItem.getProperty().getVersion();
            this.originalDescription = this.connectionItem.getProperty().getDescription();
            this.originalPurpose = this.connectionItem.getProperty().getPurpose();
            this.originalStatus = this.connectionItem.getProperty().getStatusCode();
        }
        oldMetadataTable = GenericUpdateManager.getConversionMetadataTables(connectionItem.getConnection());
        compService = new GenericWizardInternalService().getComponentService();
        compService.setRepository(new GenericRepository());
        IWizardContainer container = this.getContainer();
        if (container instanceof GenericWizardDialog) {
            GenericWizardDialog genericWizardDialog = (GenericWizardDialog) container;
            genericWizardDialog.setCompService(compService);
        }
        // initialize the context mode
        ConnectionContextHelper.checkContextMode(connectionItem);
        setHelpAvailable(false);
        setRepositoryLocation(wizard, location, connectionItem.getProperty().getId());
    }

    @Override
    public void addPages() {
        if (wizard == null) {
            return;
        }
        ComponentWizardDefinition wizardDefinition = wizard.getDefinition();
        if (wizardDefinition == null) {
            return;
        }
        setWindowTitle(wizardDefinition.getDisplayName());
        Image wiardImage = wizardService.getWiardImage(repObjType.getType());
        setDefaultPageImageDescriptor(ImageDescriptor.createFromImage(wiardImage));
        connectionItem.setTypeName(repObjType.getType());

        List<Form> forms = wizard.getForms();
        for (int i = 0; i < forms.size(); i++) {
            Form form = forms.get(i);
            boolean addContextSupport = false;
            if (i == 0) {// Add context support in the first form.
                addContextSupport = true;
            }
            if(isRetrieve()){
                addContextSupport = false;
            }
            wizPage = new GenericConnWizardPage(connectionItem, isRepositoryObjectEditable(), existingNames, creation, form,
                    compService, addContextSupport);
            if (wizPage != null) {
                wizPage.setTitle(form.getTitle());
                wizPage.setDescription(form.getSubtitle());
                if (creation) {
                    wizPage.setPageComplete(false);
                } else {
                    wizPage.setPageComplete(isRepositoryObjectEditable());
                }
            }
            addPage(wizPage);
        }
    }

    private ComponentWizard getDefaultWizard(String typeName) {
        IGenericWizardInternalService internalService = new GenericWizardInternalService();
        return internalService.getComponentWizard(typeName, connectionProperty.getId());
    }

    private void setRepositoryLocation(Object objectClass,String key, Object value){
        Field declaredField = null;
        try {
            declaredField = objectClass.getClass().getDeclaredField(key);
        } catch (NoSuchFieldException | SecurityException e) {
            try {
                declaredField = objectClass.getClass().getSuperclass().getDeclaredField(key);
            } catch (NoSuchFieldException | SecurityException e1) {
            }
        }
        declaredField.setAccessible(true);
        try {
            declaredField.set(objectClass, value);
        } catch (IllegalArgumentException | IllegalAccessException e) {
        }
    }

    @Override
    public boolean performFinish() {
        if (wizPage.isPageComplete()) {
            try {
                createOrUpdateConnectionItem();
            } catch (Throwable e) {
                new ErrorDialogWidthDetailArea(getShell(), IGenericConstants.REPOSITORY_PLUGIN_ID,
                        Messages.getString("GenericConnWizard.persistenceException"), //$NON-NLS-1$
                        ExceptionUtils.getFullStackTrace(e));
                ExceptionHandler.process(e);
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    private void updateConnectionItem(IProxyRepositoryFactory repFactory) throws CoreException, PersistenceException {
        repFactory.save(connectionItem);
        IWizardPage[] pages = getPages();
        if (pages.length > 0 && pages[0] instanceof GenericWizardPage) {
            GenericWizardPage namePage = (GenericWizardPage) pages[0];
            boolean nameModifiedByUser = namePage.nameModifiedByUser();
            if (nameModifiedByUser) {
                if (GlobalServiceRegister.getDefault().isServiceRegistered(IDesignerCoreService.class)) {
                    IDesignerCoreService service = (IDesignerCoreService) GlobalServiceRegister.getDefault()
                            .getService(IDesignerCoreService.class);
                    if (service != null) {
                        service.refreshComponentView(connectionItem);
                    }
                }
            }
        }

        closeLockStrategy();
    }

    private void createOrUpdateConnectionItem() throws CoreException {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        final IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        IWorkspaceRunnable operation = new IWorkspaceRunnable() {

            @Override
            public void run(IProgressMonitor monitor) throws CoreException {
                try {
                    Form form = wizPage.getForm();
                    setRepositoryLocation(form.getProperties(), location, connectionItem.getProperty().getId());
                    if (form.isCallAfterFormFinish()) {
                        if (creation) {
                            factory.create(connectionItem, pathToSave);
                        }
                        compService.afterFormFinish(form.getName(), form.getProperties());
                    }
                    updateConnectionItem(factory);
                } catch (Throwable e) {
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
        	GenericUpdateManager.updateGenericConnection(connectionItem, oldMetadataTable);
        }
    }

    @Override
    public boolean performCancel() {
        if (!creation) {
            connectionItem.getProperty().setVersion(this.originalVersion);
            connectionItem.getProperty().setDisplayName(this.originalLabel);
            connectionItem.getProperty().setDescription(this.originalDescription);
            connectionItem.getProperty().setPurpose(this.originalPurpose);
            connectionItem.getProperty().setStatusCode(this.originalStatus);
        }
        return super.performCancel();
    }

    /**
     * We will accept the selection in the workbench to see if we can initialize from it.
     *
     * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
     */
    public void init(final IWorkbench workbench, final IStructuredSelection sel) {
        super.setWorkbench(workbench);
        this.selection = sel;
    }

    @Override
    public ConnectionItem getConnectionItem() {
        return this.connectionItem;
    }

    private boolean isRetrieve(){
        ComponentWizardDefinition wizardDefinition = wizard.getDefinition();
        if (wizardDefinition == null) {
            return false;
        }
        List<ERepositoryObjectType> extraTypes = new ArrayList<ERepositoryObjectType>();
        IGenericDBService dbService = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IGenericDBService.class)) {
            dbService = (IGenericDBService) GlobalServiceRegister.getDefault().getService(
                    IGenericDBService.class);
        }
        if(dbService != null){
            extraTypes.addAll(dbService.getExtraTypes());
        }
        for(ERepositoryObjectType type : extraTypes){
            if(wizardDefinition.getName().equals(type.getLabel()+".retrieveschema")){
                return true;
            }
        }
        return false;
    }

}
