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
package org.talend.designer.core.ui.projectsetting;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.context.ContextUtils;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.RepositoryManager;
import org.talend.core.repository.model.ProjectRepositoryNode;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.ui.view.RepositoryLabelProvider;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.services.IDesignerCoreUIService;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.model.process.jobsettings.JobSettingsConstants;
import org.talend.designer.core.model.utils.emf.talendfile.ParametersType;
import org.talend.designer.core.ui.editor.cmd.ChangeValuesFromRepository;
import org.talend.designer.core.ui.editor.cmd.LoadProjectSettingsCommand;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.editor.update.UpdateManagerUtils;
import org.talend.designer.core.ui.views.properties.WidgetFactory;
import org.talend.designer.core.utils.DetectContextVarsUtils;
import org.talend.metadata.managment.ui.wizard.metadata.ShowAddedContextdialog;
import org.talend.repository.ProjectManager;
import org.talend.repository.UpdateRepositoryUtils;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.preference.ProjectSettingPage;
import org.talend.repository.ui.views.IRepositoryView;
import org.talend.repository.viewer.ui.provider.RepositoryContentProvider;
import org.talend.repository.viewer.ui.viewer.CheckboxRepositoryTreeViewer;

/**
 * DOC achen class global comment. Detailled comment
 */
public class StatLogsAndImplicitcontextTreeViewPage extends ProjectSettingPage {

    private IRepositoryView repositoryView;

    private final IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

    final List<IProcess2> opendProcess = UpdateManagerUtils.getOpenedProcess();

    // implicit context tree
    private CheckboxRepositoryTreeViewer viewer;

    private AllJobContentProvider contentProvider;

    private List<RepositoryNode> checkedObjects = new ArrayList<RepositoryNode>();

    private List<RepositoryNode> addedObjects = new ArrayList<RepositoryNode>();

    private List<RepositoryNode> removedObjects = new ArrayList<RepositoryNode>();

    // stats and log tree
    private CheckboxRepositoryTreeViewer statViewer;

    private AllJobContentProvider statContentProvider;

    private List<RepositoryNode> statCheckedObjects = new ArrayList<RepositoryNode>();

    private List<RepositoryNode> statAddedObjects = new ArrayList<RepositoryNode>();

    private List<RepositoryNode> statRemovedObjects = new ArrayList<RepositoryNode>();

    private WidgetFactory widgetFactory = new WidgetFactory();

    private boolean statsLogAddContextModel = false;

    private boolean implicitAddContextModel = false;

    private boolean addContext = false;

    private IEditorReference[] editors = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
            .getEditorReferences();

    // private IDesignerCoreService coreService = CorePlugin.getDefault().getDesignerCoreService();

    public StatLogsAndImplicitcontextTreeViewPage() {
        super();
        this.noDefaultAndApplyButton();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        Composite composite = widgetFactory.createComposite(parent, SWT.NONE);
        // composite.setLayout(new FillLayout());
        GridLayout layout = new GridLayout();
        layout.marginLeft = 5;
        layout.marginRight = 5;
        layout.numColumns = 2;
        layout.makeColumnsEqualWidth = true;
        composite.setLayout(layout);

        repositoryView = RepositoryManager.getRepositoryView();
        createImplicitcontextTree(composite);
        createStatTree(composite);

        IProxyRepositoryFactory facto = ProxyRepositoryFactory.getInstance();
        if (facto.isUserReadOnlyOnCurrentProject()) {
            composite.setEnabled(false);
        }
        return composite;
    }

    @Override
    public void dispose() {
        if (widgetFactory != null) {
            widgetFactory.dispose();
        }
        super.dispose();
    }

    private void createImplicitcontextTree(Composite composite) {
        Group g = widgetFactory.createGroup(composite, Messages.getString("ExtraComposite.ImplicitContextSettings")); //$NON-NLS-1$
        GridData gd = new GridData(GridData.FILL_BOTH);
        g.setLayoutData(gd);
        g.setLayout(new FillLayout());
        viewer = new CheckboxRepositoryTreeViewer(g, SWT.MULTI | SWT.V_SCROLL);
        contentProvider = new AllJobContentProvider(repositoryView);
        viewer.setContentProvider(contentProvider);
        viewer.setLabelProvider(new RepositoryLabelProvider(repositoryView));
        viewer.setSorter(repositoryView.getViewer().getSorter());
        IViewSite viewSite = repositoryView.getViewSite();
        viewer.setInput(viewSite);

        // This only tree listener aim is to change open/close icons on folders :
        viewer.addTreeListener(new ITreeViewerListener() {

            @Override
            public void treeCollapsed(TreeExpansionEvent event) {
                RepositoryNode node = (RepositoryNode) event.getElement();
                if (node.getType().equals(ENodeType.SIMPLE_FOLDER)) {
                    TreeItem item = getObject(viewer.getTree(), event.getElement());
                    if (item != null) {
                        item.setImage(ImageProvider.getImage(ECoreImage.FOLDER_CLOSE_ICON));
                    }
                }
            }

            @Override
            public void treeExpanded(TreeExpansionEvent event) {
                RepositoryNode node = (RepositoryNode) event.getElement();
                if (node.getType().equals(ENodeType.SIMPLE_FOLDER)) {
                    TreeItem item = getObject(viewer.getTree(), event.getElement());
                    if (item != null) {
                        item.setImage(ImageProvider.getImage(ECoreImage.FOLDER_OPEN_ICON));
                    }
                }
            }
        });

        viewer.addCheckStateListener(new ICheckStateListener() {

            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                RepositoryNode node = (RepositoryNode) event.getElement();
                List<RepositoryNode> objects = new ArrayList<RepositoryNode>();
                processItems(objects, node);
                if (event.getChecked()) {
                    addedObjects.addAll(objects);
                    removedObjects.removeAll(objects);
                    checkedObjects.addAll(addedObjects);
                } else {
                    addedObjects.removeAll(objects);
                    removedObjects.addAll(objects);
                    checkedObjects.removeAll(objects);
                }
                // set checked
                viewer.setCheckedElements(checkedObjects.toArray());
                // viewer.refresh();
            }
        });

        RepositoryNode[] nodes = contentProvider.getContents();
        List<RepositoryNode> objects = new ArrayList<RepositoryNode>();
        if (nodes != null) {
            for (RepositoryNode n : nodes) {
                processItems(objects, n);
                for (RepositoryNode node : objects) {
                    if (isUseProjectSetting(node)) {
                        if (!checkedObjects.contains(node)) {
                            checkedObjects.add(node);
                        }

                    }
                }
            }
        }
        if (nodes != null) {
            viewer.setExpandedElements(nodes);
        }
        viewer.setCheckedElements(checkedObjects.toArray());
    }

    private void createStatTree(Composite composite) {
        Group g = widgetFactory.createGroup(composite, Messages.getString("StatsAndLogsComposite.StatsLogsSettings")); //$NON-NLS-1$
        GridData gd = new GridData(GridData.FILL_BOTH);
        g.setLayoutData(gd);
        g.setLayout(new FillLayout());
        statViewer = new CheckboxRepositoryTreeViewer(g, SWT.MULTI | SWT.V_SCROLL);
        statContentProvider = new AllJobContentProvider(repositoryView);
        statViewer.setContentProvider(statContentProvider);
        statViewer.setLabelProvider(new RepositoryLabelProvider(repositoryView));
        statViewer.setSorter(repositoryView.getViewer().getSorter());
        IViewSite viewSite = repositoryView.getViewSite();
        statViewer.setInput(viewSite);

        // This only tree listener aim is to change open/close icons on folders :
        statViewer.addTreeListener(new ITreeViewerListener() {

            @Override
            public void treeCollapsed(TreeExpansionEvent event) {
                RepositoryNode node = (RepositoryNode) event.getElement();
                if (node.getType().equals(ENodeType.SIMPLE_FOLDER)) {
                    TreeItem item = getObject(statViewer.getTree(), event.getElement());
                    if (item != null) {
                        item.setImage(ImageProvider.getImage(ECoreImage.FOLDER_CLOSE_ICON));
                    }
                }
            }

            @Override
            public void treeExpanded(TreeExpansionEvent event) {
                RepositoryNode node = (RepositoryNode) event.getElement();
                if (node.getType().equals(ENodeType.SIMPLE_FOLDER)) {
                    TreeItem item = getObject(statViewer.getTree(), event.getElement());
                    if (item != null) {
                        item.setImage(ImageProvider.getImage(ECoreImage.FOLDER_OPEN_ICON));
                    }
                }
            }
        });

        statViewer.addCheckStateListener(new ICheckStateListener() {

            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                RepositoryNode node = (RepositoryNode) event.getElement();
                List<RepositoryNode> objects = new ArrayList<RepositoryNode>();
                processItems(objects, node);
                if (event.getChecked()) {
                    statAddedObjects.addAll(objects);
                    statRemovedObjects.removeAll(objects);
                    statCheckedObjects.addAll(objects);
                } else {
                    statAddedObjects.removeAll(objects);
                    statRemovedObjects.addAll(objects);
                    statCheckedObjects.removeAll(objects);
                }
                // set checked
                statViewer.setCheckedElements(statCheckedObjects.toArray());
                // viewer.refresh();
            }
        });

        RepositoryNode[] nodes = statContentProvider.getContents();
        List<RepositoryNode> objects = new ArrayList<RepositoryNode>();
        if (nodes != null) {
            for (RepositoryNode n : nodes) {
                processItems(objects, n);
                for (RepositoryNode node : objects) {
                    if (isStatUseProjectSetting(node)) {
                        if (!statCheckedObjects.contains(node)) {
                            statCheckedObjects.add(node);
                        }
                    }
                }
            }
        }
        if (nodes != null) {
            statViewer.setExpandedElements(nodes);
        }
        statViewer.setCheckedElements(statCheckedObjects.toArray());
    }

    private boolean isUseProjectSetting(RepositoryNode node) {
        Property property = node.getObject().getProperty();
        ProcessItem pItem = (ProcessItem) property.getItem();
        ParametersType pType = pItem.getProcess().getParameters();

        String implictB = ElementParameter2ParameterType.getParameterValue(pType,
                EParameterName.IMPLICITCONTEXT_USE_PROJECT_SETTINGS.getName());
        if (implictB != null && "true".equals(implictB)) { //$NON-NLS-1$
            return true;
        } else {
            return false;
        }
    }

    private boolean isStatUseProjectSetting(RepositoryNode node) {
        Property property = node.getObject().getProperty();
        ProcessItem pItem = (ProcessItem) property.getItem();
        ParametersType pType = pItem.getProcess().getParameters();

        String statB = ElementParameter2ParameterType.getParameterValue(pType,
                EParameterName.STATANDLOG_USE_PROJECT_SETTINGS.getName());
        if (statB != null && "true".equals(statB)) { //$NON-NLS-1$
            return true;
        } else {
            return false;
        }
    }

    private boolean isOpenProcess(RepositoryNode node) {
        Property property = node.getObject().getProperty();
        if (property.getItem() instanceof ProcessItem) {
            for (IProcess process : opendProcess) {
                if (process.getId().equals(property.getId()) && process.getName().equals(property.getLabel())
                        && process.getVersion().equals(property.getVersion())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void processItems(List<RepositoryNode> objects, RepositoryNode node) {
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

    private TreeItem getObject(Tree tree, Object objectToFind) {
        for (TreeItem item : tree.getItems()) {
            TreeItem toReturn = getObject(item, objectToFind);
            if (toReturn != null) {
                return toReturn;
            }
        }
        return null;
    }

    private TreeItem getObject(TreeItem parent, Object objectToFind) {
        for (TreeItem currentChild : parent.getItems()) {
            if (objectToFind.equals(currentChild.getData())) {
                return currentChild;
            }
            TreeItem toReturn = getObject(currentChild, objectToFind);
            if (toReturn != null) {
                return toReturn;
            }
        }
        return null;
    }

    class AllJobContentProvider extends RepositoryContentProvider {

        public AllJobContentProvider(IRepositoryView v) {
            super(v);
        }

        @Override
        public Object[] getElements(Object parent) {
            IRepositoryView view = getView();
            if (view == null || parent.equals(view.getViewSite())) {
                return getContents();
            }
            return getChildren(parent);
        }

        RepositoryNode[] getContents() {
            ProjectRepositoryNode root = getRoot();
            if (root != null) {
                RepositoryNode rootRepositoryNode = root.getRootRepositoryNode(ERepositoryObjectType.PROCESS);
                if (rootRepositoryNode != null) {
                    return new RepositoryNode[] { rootRepositoryNode };
                }
            }
            return null;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.preference.PreferencePage#performApply()
     */
    @Override
    protected void performApply() {
        save();
        super.performApply();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        save();
        return super.performOk();
    }

    private void saveProcess(RepositoryNode node, String paramName, Boolean isUseProjectSettings, boolean addContextModel,
            Map<String, Set<String>> contextVars, IProgressMonitor monitor) {
        Property property = node.getObject().getProperty();
        ProcessItem pItem = (ProcessItem) property.getItem();
        ParametersType pType = pItem.getProcess().getParameters();

        if (isOpenProcess(node)) {
            Process process = getProcess(opendProcess, node);
            LoadProjectSettingsCommand command = new LoadProjectSettingsCommand(process, paramName, isUseProjectSettings);
            exeCommand(process, command);
            Element elem = null;
            String propertyName = "";
            String propertyTypeName = "";
            if (EParameterName.IMPLICITCONTEXT_USE_PROJECT_SETTINGS.getName().equals(paramName)) {
                elem = (Element) pro.getInitialContextLoad();
                propertyName = JobSettingsConstants.getExtraParameterName(EParameterName.PROPERTY_TYPE.getName()) + ':'
                        + EParameterName.REPOSITORY_PROPERTY_TYPE.getName();
                propertyTypeName = JobSettingsConstants.getExtraParameterName(EParameterName.PROPERTY_TYPE.getName()) + ':'
                        + EParameterName.PROPERTY_TYPE.getName();
            } else if (EParameterName.STATANDLOG_USE_PROJECT_SETTINGS.getName().equals(paramName)) {
                elem = (Element) pro.getStatsAndLog();
                propertyName = EParameterName.PROPERTY_TYPE.getName() + ':' + EParameterName.REPOSITORY_PROPERTY_TYPE.getName();
                propertyTypeName = EParameterName.PROPERTY_TYPE.getName() + ':' + EParameterName.PROPERTY_TYPE.getName();
            }
            IElementParameter ptParam = elem.getElementParameterFromField(EParameterFieldType.PROPERTY_TYPE);
            if (ptParam != null) {
                IElementParameter propertyElem = ptParam.getChildParameters().get(EParameterName.PROPERTY_TYPE.getName());
                Object proValue = propertyElem.getValue();
                if (proValue instanceof String && ((String) proValue).equalsIgnoreCase(EmfComponent.REPOSITORY)) {
                    IElementParameter repositoryElem = ptParam.getChildParameters().get(
                            EParameterName.REPOSITORY_PROPERTY_TYPE.getName());
                    String value = (String) repositoryElem.getValue();
                    ConnectionItem connectionItem = UpdateRepositoryUtils.getConnectionItemByItemId(value);
                    if (connectionItem != null) {
                        Connection connection = connectionItem.getConnection();
                        ChangeValuesFromRepository cmd = new ChangeValuesFromRepository(process, connection,
                                addContextModel ? propertyName : propertyTypeName, value);
                        cmd.ignoreContextMode(true);
                        exeCommand(process, cmd);
                    }
                }
            }

            monitor.worked(100);
        } else {
            ElementParameter2ParameterType.setParameterValue(pType, paramName, isUseProjectSettings);
            if (isUseProjectSettings) {
                if (EParameterName.IMPLICITCONTEXT_USE_PROJECT_SETTINGS.getName().equals(paramName)) {
                    ProjectSettingManager.reloadImplicitValuesFromProjectSettings(pType, ProjectManager.getInstance()
                            .getCurrentProject());
                } else if (EParameterName.STATANDLOG_USE_PROJECT_SETTINGS.getName().equals(paramName)) {
                    ProjectSettingManager.reloadStatsAndLogFromProjectSettings(pType, ProjectManager.getInstance()
                            .getCurrentProject());
                }
            }

            if (addContextModel && contextVars != null && !contextVars.isEmpty() && ContextUtils.getAllContextItem() != null) {
                ContextUtils.addInContextModelForProcessItem(pItem, contextVars, ContextUtils.getAllContextItem());
            }

            try {
                factory.save(pItem);
                monitor.worked(100);
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }
        }

    }

    private void saveChangedNode(String paramName, boolean addContextModel, Map<String, Set<String>> contextVars,
            IProgressMonitor monitor) {
        List<RepositoryNode> checked = new ArrayList<RepositoryNode>();
        List<RepositoryNode> unChecked = new ArrayList<RepositoryNode>();
        if (EParameterName.IMPLICITCONTEXT_USE_PROJECT_SETTINGS.getName().equals(paramName)) {
            checked = addedObjects;
            unChecked = removedObjects;
        } else if (EParameterName.STATANDLOG_USE_PROJECT_SETTINGS.getName().equals(paramName)) {
            checked = statAddedObjects;
            unChecked = statRemovedObjects;
        }
        for (RepositoryNode node : checked) {
            saveProcess(node, paramName, Boolean.TRUE, addContextModel, contextVars, monitor);
        }
        for (RepositoryNode node : unChecked) {
            saveProcess(node, paramName, Boolean.FALSE, addContextModel, contextVars, monitor);
        }
    }

    private void save() {
        if (viewer == null) {
            return;
        }

        final IRunnableWithProgress runnable = new IRunnableWithProgress() {

            @Override
            public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                monitor.beginTask(
                        Messages.getString("StatLogsAndImplicitcontextTreeViewPage.SaveProjectSettings"), (addedObjects.size() + statAddedObjects.size()) * 100); //$NON-NLS-1$
                addContext = false;
                statsLogAddContextModel = false;
                implicitAddContextModel = false;
                Map<String, Set<String>> implicitContextVars = null;
                Map<String, Set<String>> statsLogContextVars = null;

                // if add some object to use project setting ,check context model
                if (!addedObjects.isEmpty()) {
                    if (pro != null) {
                        Element implicitContextLoad = (Element) pro.getInitialContextLoad();
                        implicitContextVars = DetectContextVarsUtils.detectByPropertyType(implicitContextLoad, true);

                    }
                }
                if (!statAddedObjects.isEmpty()) {
                    if (pro != null) {
                        Element statsAndLog = (Element) pro.getStatsAndLog();
                        statsLogContextVars = DetectContextVarsUtils.detectByPropertyType(statsAndLog, true);
                    }
                }
                // if statslog and implicit use the same connection only show add context dialog one time
                if (implicitContextVars != null && statsLogContextVars != null && !implicitContextVars.isEmpty()
                        && !statsLogContextVars.isEmpty()
                        && implicitContextVars.keySet().toArray()[0].equals(statsLogContextVars.keySet().toArray()[0])) {
                    showAddContextDialog(implicitContextVars);
                    if (addContext) {
                        statsLogAddContextModel = true;
                        implicitAddContextModel = true;
                    }
                } else {
                    if (implicitContextVars != null && !implicitContextVars.isEmpty()) {
                        showAddContextDialog(implicitContextVars);
                        if (addContext) {
                            implicitAddContextModel = true;
                        }
                    }
                    if (statsLogContextVars != null && !statsLogContextVars.isEmpty()) {
                        showAddContextDialog(statsLogContextVars);
                        if (addContext) {
                            statsLogAddContextModel = true;
                        }
                    }
                }

                saveChangedNode(EParameterName.IMPLICITCONTEXT_USE_PROJECT_SETTINGS.getName(), implicitAddContextModel,
                        implicitContextVars, monitor);
                saveChangedNode(EParameterName.STATANDLOG_USE_PROJECT_SETTINGS.getName(), statsLogAddContextModel,
                        statsLogContextVars, monitor);

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

    private void showAddContextDialog(Map<String, Set<String>> vars) {
        final Map<String, Set<String>> contextVars = vars;
        Display disp = Display.getCurrent();
        if (disp == null) {
            disp = Display.getDefault();
        }
        if (disp != null) {
            disp.syncExec(new Runnable() {

                @Override
                public void run() {
                    ShowAddedContextdialog showDialog = new ShowAddedContextdialog(contextVars, true);
                    if (showDialog.open() == Window.OK) {
                        addContext = true;
                    }
                }
            });
        }
    }

    private org.talend.designer.core.ui.editor.process.Process getProcess(List<IProcess2> list, RepositoryNode p) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(p.getId())) {
                return (org.talend.designer.core.ui.editor.process.Process) list.get(i);
            }
        }
        return null;
    }

    private void exeCommand(final Process process, final Command cmd) {
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

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.preference.ProjectSettingPage#refresh()
     */
    @Override
    public void refresh() {
        // TODO Auto-generated method stub

    }

}
