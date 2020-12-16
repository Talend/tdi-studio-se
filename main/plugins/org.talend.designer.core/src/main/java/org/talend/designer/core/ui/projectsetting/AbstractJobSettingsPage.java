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
package org.talend.designer.core.ui.projectsetting;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.CorePlugin;
import org.talend.core.model.context.ContextUtils;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.designerproperties.RepositoryToComponentProperty;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.update.UpdatesConstants;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.services.IDesignerCoreUIService;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.model.utils.emf.talendfile.ParametersType;
import org.talend.designer.core.ui.editor.cmd.ChangeValuesFromRepository;
import org.talend.designer.core.ui.editor.cmd.LoadProjectSettingsCommand;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.views.properties.WidgetFactory;
import org.talend.designer.core.utils.DetectContextVarsUtils;
import org.talend.metadata.managment.ui.wizard.metadata.ShowAddedContextdialog;
import org.talend.repository.UpdateRepositoryUtils;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.preference.ProjectSettingPage;

/**
 * cli class global comment. Detailled comment
 */
public abstract class AbstractJobSettingsPage extends ProjectSettingPage {

    protected final IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

    private ProjectSettingMultipleThreadDynamicComposite mComposite;

    private Element elem;

    private WidgetFactory widgetFactory = new WidgetFactory();

    private List<IProcess2> openedProcessList = new ArrayList<IProcess2>();

    private List<IRepositoryViewObject> checkedNodeObject = new ArrayList<IRepositoryViewObject>();

    private boolean isConnectionChanged = false;

    private static boolean isUserIdentified;

    public AbstractJobSettingsPage() {
        super();
        if (isUserIdentified) {
            isUserIdentified = false;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        Composite composite = widgetFactory.createComposite(parent, SWT.NONE);
        composite.setLayout(new FormLayout());
        FormData data = createFormData();
        composite.setLayoutData(data);
        //
        checkSettingExisted();

        elem = checkAndCreateElement();
        if (getParametersType() != null) {
            ElementParameter2ParameterType.loadElementParameters(elem, getParametersType(), getPropertyTypeName());
        }
        // update connection from repository if needed
        updateProjectSetting();

        mComposite = new ProjectSettingMultipleThreadDynamicComposite(composite, SWT.V_SCROLL | SWT.BORDER, getCategory(), elem,
                true, getRepositoryPropertyName());
        mComposite.setLayoutData(createFormData());
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        if (factory.isUserReadOnlyOnCurrentProject()) {
            composite.setEnabled(false);
        }
        return composite;
    }

    public void updateProjectSetting() {

        String[] split = getRepositoryPropertyName().split(":");
        String parentParamName = split[0];

        Element elementParams = elem;
        IElementParameter elementParameter = elementParams.getElementParameter(parentParamName);
        if (elementParameter != null && elementParameter.isShow(elem.getElementParameters())
                && elementParameter.getChildParameters() != null) {
            if (elementParameter.getChildParameters().get("PROPERTY_TYPE") != null
                    && !EmfComponent.BUILTIN.equals(elementParameter.getChildParameters().get("PROPERTY_TYPE").getValue())) {

                DatabaseConnection connection = null;
                String id = (String) elementParameter.getChildParameters().get("REPOSITORY_PROPERTY_TYPE").getValue();
                IRepositoryViewObject lastVersion = UpdateRepositoryUtils.getRepositoryObjectById(id);
                if (lastVersion != null && lastVersion.getProperty() != null) {
                    Item item = lastVersion.getProperty().getItem();
                    if (item instanceof DatabaseConnectionItem) {
                        DatabaseConnectionItem dbItem = (DatabaseConnectionItem) item;
                        connection = (DatabaseConnection) dbItem.getConnection();
                    }
                }

                if (connection != null) {
                    boolean sameValues = true;
                    for (IElementParameter param : elementParams.getElementParameters()) {
                        String repositoryValue = param.getRepositoryValue();
                        if (param.isShow(elementParams.getElementParameters()) && repositoryValue != null
                                && !param.getName().equals("PROPERTY_TYPE")) {
                            Object repValue = RepositoryToComponentProperty.getValue(connection, repositoryValue, null);
                            if (repValue == null) {
                                continue;
                            }
                            if (repositoryValue.equals(UpdatesConstants.TYPE)) { // datebase type
                                boolean found = false;
                                String[] list = param.getListRepositoryItems();
                                for (int i = 0; (i < list.length) && (!found); i++) {
                                    if (repValue.equals(list[i])) {
                                        found = true;
                                    }

                                }
                                if (!found) {
                                    sameValues = false;
                                    break;
                                }

                            } else {
                                // check the value
                                if ((param.getName().equals("DRIVER_JAR")
                                        || param.getName().equals("DRIVER_JAR_IMPLICIT_CONTEXT")) && param.getValue() != null) {
                                    sameValues = isSameDriverList((List<Map<String, String>>) param.getValue(),
                                            (List<Map<String, String>>) repValue);
                                    if (!sameValues) {
                                        break;
                                    }
                                } else if (!param.getValue().equals(repValue)) {
                                    sameValues = false;
                                    break;
                                }
                            }

                        }
                    }

                    if (!sameValues) {
                        boolean ok = MessageDialog.openQuestion(getShell(), getDisplayName(),
                                "Connection has been changed , do you want to change value from repository ?");
                        if (ok) {
                            ChangeValuesFromRepository changeValuesFromRepository = new ChangeValuesFromRepository(elem,
                                    connection, getRepositoryPropertyName(), id);
                            changeValuesFromRepository.execute();
                            isConnectionChanged = true;
                        }
                    }

                } else {
                    MessageDialog.openInformation(getShell(), getDisplayName(),
                            "Connection has been deleted ,change to build in automaticlly");
                    elem.setPropertyValue("REPOSITORY_PROPERTY_TYPE", "");
                    ChangeValuesFromRepository changeValuesFromRepository1 = new ChangeValuesFromRepository(elem, null,
                            getPropertyTypeName(), EmfComponent.BUILTIN);
                    changeValuesFromRepository1.execute();
                    isConnectionChanged = true;
                }

            }

        }

    }

    protected boolean isSameDriverList(List<Map<String, String>> driverList1, List<Map<String, String>> driverList2) {
        if (driverList1 == null) {
            if (driverList2 == null) {
                return true;
            } else {
                return false;
            }
        } else {
            if (driverList2 == null) {
                return false;
            } else {
                // compare
                if (driverList1.size() != driverList2.size()) {
                    return false;
                }
                Set<String> driverSet1 = new HashSet<String>();
                Set<String> driverSet2 = new HashSet<String>();

                for (Map<String, String> driverMap : driverList1) {
                    String jar = driverMap.get("drivers");
                    if (jar != null) {
                        driverSet1.add(TalendQuoteUtils.removeQuotes(jar));
                    }
                }
                for (Map<String, String> driverMap : driverList2) {
                    String jar = driverMap.get("drivers");
                    if (jar != null) {
                        driverSet2.add(TalendQuoteUtils.removeQuotes(jar));
                    }
                }

                return driverSet1.equals(driverSet2);
            }
        }
    }

    protected abstract void checkSettingExisted();

    protected abstract Element checkAndCreateElement();

    protected abstract EComponentCategory getCategory();

    protected abstract String getDisplayName();

    @Override
    public void dispose() {
        if (widgetFactory != null) {
            widgetFactory.dispose();
        }
        super.dispose();
    }

    protected FormData createFormData() {
        FormData data = new FormData();
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(100, 0);
        data.top = new FormAttachment(0, 0);
        data.bottom = new FormAttachment(100, 0);
        return data;
    }

    protected abstract ParametersType getParametersType();

    protected abstract String getPropertyTypeName();

    protected abstract String getRepositoryPropertyName();

    protected abstract EParameterName getParameterName();

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.preference.ProjectSettingPage#refresh()
     */
    @Override
    public void refresh() {
        if (mComposite != null) {
            ElementParameter2ParameterType.loadElementParameters(elem, getParametersType(), getPropertyTypeName());
            mComposite.refresh();
        }

    }

    protected boolean isStatUseProjectSetting(IRepositoryViewObject object) {
        Property property = object.getProperty();
        ProcessItem pItem = (ProcessItem) property.getItem();
        ParametersType pType = pItem.getProcess().getParameters();
        String statB = ElementParameter2ParameterType.getParameterValue(pType, getParameterName().getName());

        return Boolean.parseBoolean(statB);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.preference.PreferencePage#performApply()
     */
    @Override
    protected void performApply() {
        performOk();
        super.performApply();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {

        if (mComposite != null && (mComposite.isCommandExcute() || isConnectionChanged)) {
            // save to the memory
            ParametersType parametersType = getParametersType();
            if (parametersType != null) {
                ElementParameter2ParameterType.saveElementParameters(elem, parametersType);
            }
            ProjectSettingManager.saveProject();
            save();
        }

        // if (parameters != null) {
        // ElementParameter2ParameterType.loadProjectsettingsParameters(parameters);
        // }
        return super.performOk();
    }

    protected void exeCommand(final Process process, final Command cmd) {
        boolean executed = false;
        if (process != null) {
            IDesignerCoreUIService designerCoreUIService = CoreUIPlugin.getDefault().getDesignerCoreUIService();
            if (designerCoreUIService != null) {
                executed = designerCoreUIService.executeCommand(process, cmd);
            }
        }
        if (!executed) {
            cmd.execute();
        }
    }

    protected IEditorReference[] getEditors() {
        final List<IEditorReference> list = new ArrayList<IEditorReference>();
        Display.getDefault().syncExec(new Runnable() {

            @Override
            public void run() {
                IEditorReference[] reference = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                        .getEditorReferences();
                list.addAll(Arrays.asList(reference));
            }
        });
        return list.toArray(new IEditorReference[0]);
    }

    protected void processItems(List<RepositoryNode> objects, RepositoryNode node) {
        if (node == null) {
            return;
        }
        if (node.getType() == ENodeType.REPOSITORY_ELEMENT) {
            if (node.getObject() != null) {
                objects.add(node);
            }
        } else {
            for (IRepositoryNode child : node.getChildren()) {
                processItems(objects, (RepositoryNode) child);
            }
        }
    }

    protected org.talend.designer.core.ui.editor.process.Process getProcess(List<IProcess2> list, IRepositoryViewObject object) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(object.getId())) {
                return (org.talend.designer.core.ui.editor.process.Process) list.get(i);
            }
        }
        return null;
    }

    protected boolean isOpenProcess(IRepositoryViewObject object) {
        Property property = object.getProperty();
        if (property.getItem() instanceof ProcessItem) {
            for (IProcess2 process : openedProcessList) {
                if (process.getId().equals(property.getId()) && process.getName().equals(property.getLabel())
                        && process.getVersion().equals(property.getVersion())) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<ContextItem> allContextItems;

    private boolean addContextModel = false;

    protected void save() {
        boolean confirm = false;
        if (!isUserIdentified) {
            confirm = MessageDialog.openQuestion(getShell(), "Question",
                    Messages.getString("AbstractJobSettingsPage.save"));
            isUserIdentified = true;
        }
        final boolean generatePom = confirm;
        List<String> checkedObjects = new ArrayList<String>();
        List<IRepositoryViewObject> allProcess = null;
        try {
            allProcess = ProxyRepositoryFactory.getInstance().getAll(ERepositoryObjectType.PROCESS);
        } catch (PersistenceException e1) {
            ExceptionHandler.process(e1);
        }

        for (IRepositoryViewObject object : allProcess) {
            if (isStatUseProjectSetting(object)) {
                if (!checkedObjects.contains(object.getProperty().getId())) {
                    checkedObjects.add(object.getProperty().getId());
                    if (!checkedNodeObject.contains(object)) {
                        checkedNodeObject.add(object);
                    }
                }
            }

        }

        List<IProcess2> allOpenedProcessList = CorePlugin.getDefault().getDesignerCoreService().getOpenedProcess(getEditors());
        if (allOpenedProcessList != null) {
            for (int i = 0; i < allOpenedProcessList.size(); i++) {
                if (checkedObjects.contains(allOpenedProcessList.get(i).getProperty().getId())) {
                    openedProcessList.add(allOpenedProcessList.get(i));
                }
            }
        }
        //

        final IRunnableWithProgress runnable = new IRunnableWithProgress() {

            @Override
            public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

                monitor.beginTask(getTaskMessages(), (checkedNodeObject.size()) * 100);
                final Map<String, Set<String>> contextVars = DetectContextVarsUtils.detectByPropertyType(elem, true);

                addContextModel = false; // must init this
                if (!contextVars.isEmpty()) {
                    // boolean showDialog = false;
                    Set<String> contextSet = new HashSet<String>();
                    for (String key : contextVars.keySet()) {
                        contextSet = contextVars.get(key);
                        break;
                    }
                    Connection connection = null;
                    IElementParameter ptParam = elem.getElementParameterFromField(EParameterFieldType.PROPERTY_TYPE);
                    if (ptParam != null) {
                        IElementParameter propertyElem = ptParam.getChildParameters().get(EParameterName.PROPERTY_TYPE.getName());
                        Object proValue = propertyElem.getValue();
                        if (proValue instanceof String && ((String) proValue).equalsIgnoreCase(EmfComponent.REPOSITORY)) {
                            IElementParameter repositoryElem = ptParam.getChildParameters()
                                    .get(EParameterName.REPOSITORY_PROPERTY_TYPE.getName());
                            String value = (String) repositoryElem.getValue();
                            ConnectionItem connectionItem = UpdateRepositoryUtils.getConnectionItemByItemId(value);
                            connection = connectionItem.getConnection();
                            if (connection != null && connection.isContextMode()) {
                                addContextModel = true;
                                // ContextItem contextItem =
                                // ContextUtils.getContextItemById(connection.getContextId());
                                // for (IProcess process : openedProcessList) {
                                // Set<String> addedContext =
                                // ConnectionContextHelper.checkAndAddContextVariables(contextItem,
                                // contextSet, process.getContextManager(), false);
                                // if (addedContext != null && !addedContext.isEmpty()) {
                                // showDialog = true;
                                // break;
                                // }
                                // }
                            }
                        }
                    }
                    if (addContextModel) {

                        // if the context is not existed in job, will add or not.
                        Display disp = Display.getCurrent();
                        if (disp == null) {
                            disp = Display.getDefault();
                        }
                        if (disp != null) {
                            disp.syncExec(new Runnable() {

                                @Override
                                public void run() {
                                    showContextAndCheck(contextVars);
                                }
                            });
                        } else {
                            showContextAndCheck(contextVars);
                        }
                    }
                }
                monitor.worked(10);
                IWorkspaceRunnable workspaceRunnable = new IWorkspaceRunnable() {

                    @Override
                    public void run(IProgressMonitor monitor) throws CoreException {
                        for (IRepositoryViewObject object : checkedNodeObject) {
                            saveProcess(object, addContextModel, generatePom, contextVars, monitor);
                        }
                    }
                };

                IWorkspace workspace = ResourcesPlugin.getWorkspace();
                try {
                    ISchedulingRule schedulingRule = workspace.getRoot();
                    workspace.run(workspaceRunnable, schedulingRule, IWorkspace.AVOID_UPDATE, monitor);
                } catch (CoreException e) {
                    throw new InvocationTargetException(e);
                }
                monitor.done();
            }
        };

        final ProgressMonitorDialog dialog = new ProgressMonitorDialog(null);
        try {
            dialog.run(true, true, runnable);
        } catch (InvocationTargetException e) {
            ExceptionHandler.process(e);
        } catch (InterruptedException e) {
            ExceptionHandler.process(e);
        }

    }

    private void showContextAndCheck(final Map<String, Set<String>> contextVars) {

        ShowAddedContextdialog showDialog = new ShowAddedContextdialog(contextVars, true);
        if (showDialog.open() == Window.OK) {
            allContextItems = ContextUtils.getAllContextItem();
            addContextModel = true;
        }
    }

    protected abstract String getTaskMessages();

    protected void saveProcess(IRepositoryViewObject object, boolean addContextModel, boolean generatePom,
            Map<String, Set<String>> contextVars, IProgressMonitor monitor) {
        Property property = object.getProperty();
        ProcessItem pItem = (ProcessItem) property.getItem();

        if (isOpenProcess(object)) {
            Process process = getProcess(openedProcessList, object);
            LoadProjectSettingsCommand command = new LoadProjectSettingsCommand(process, getParameterName().getName(),
                    Boolean.TRUE);
            exeCommand(process, command);
            //
            IElementParameter ptParam = elem.getElementParameterFromField(EParameterFieldType.PROPERTY_TYPE);
            if (ptParam != null) {
                IElementParameter propertyElem = ptParam.getChildParameters().get(EParameterName.PROPERTY_TYPE.getName());
                Object proValue = propertyElem.getValue();
                if (proValue instanceof String && ((String) proValue).equalsIgnoreCase(EmfComponent.REPOSITORY)) {
                    IElementParameter repositoryElem = ptParam.getChildParameters()
                            .get(EParameterName.REPOSITORY_PROPERTY_TYPE.getName());
                    String value = (String) repositoryElem.getValue();
                    ConnectionItem connectionItem = UpdateRepositoryUtils.getConnectionItemByItemId(value);
                    if (connectionItem != null) {
                        Connection connection = connectionItem.getConnection();
                        ChangeValuesFromRepository cmd = new ChangeValuesFromRepository(process, connection,
                                addContextModel ? getRepositoryPropertyName() : getPropertyTypeName(), value);
                        cmd.ignoreContextMode(true);
                        exeCommand(process, cmd);
                    }
                }
            }

            monitor.worked(100);
        } else {
            try {

                reloadFromProjectSetings(pItem, addContextModel, contextVars);
                factory.save(pItem, false, !generatePom);
                monitor.worked(100);
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }

        }
    }

    protected void reloadFromProjectSetings(ProcessItem pItem, boolean addContextModel, Map<String, Set<String>> contextVars) {
        if (pItem != null) {
            ParametersType pType = pItem.getProcess().getParameters();
            if (getParameterName() == EParameterName.IMPLICITCONTEXT_USE_PROJECT_SETTINGS) {
                ProjectSettingManager.reloadImplicitValuesFromProjectSettings(pType, pro);
            } else if (getParameterName() == EParameterName.STATANDLOG_USE_PROJECT_SETTINGS) {
                ProjectSettingManager.reloadStatsAndLogFromProjectSettings(pType, pro);
            }
            if (addContextModel && !contextVars.isEmpty() && allContextItems != null) {
                ContextUtils.addInContextModelForProcessItem(pItem, contextVars, allContextItems);
            }
        }
    }
}
