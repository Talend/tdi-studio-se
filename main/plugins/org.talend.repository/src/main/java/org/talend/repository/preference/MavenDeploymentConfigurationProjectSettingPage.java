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
package org.talend.repository.preference;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.internal.navigator.NavigatorContentServiceContentProvider;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.process.ProcessUtils;
import org.talend.core.model.relationship.Relation;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.Folder;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.IRepositoryFactory;
import org.talend.core.repository.model.ProjectRepositoryNode;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.ui.view.RepositoryLabelProvider;
import org.talend.core.ui.advanced.composite.FilteredCheckboxTree;
import org.talend.designer.maven.model.TalendJavaProjectConstants;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryWorkUnit;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.DeploymentConfsModel;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.nodes.IProjectRepositoryNode;
import org.talend.repository.utils.DeploymentConfsUtils;
import org.talend.repository.viewer.ui.provider.RepoCommonViewerProvider;
import org.talend.repository.viewer.ui.viewer.CheckboxRepoCommonViewer;

@Deprecated
public class MavenDeploymentConfigurationProjectSettingPage extends ProjectSettingPage {

    private static final String DEFAULT_CONF_NAME = "conf1"; //$NON-NLS-1$

    private static final String TITLE_ADD = "Add"; // $NON-NLS-N$

    private static final String TITLE_RENAME = "Rename"; // $NON-NLS-N$

    // Should be the same with CamelRepositoryNodeType.repositoryRouteDesinsType
    private final String NODENAME_ROUTE_DESIGNS = "ROUTE_DESIGNS"; //$NON-NLS-1$

    // Should be the same with ESBRepositoryNodeType.SERVICES
    private final String NODENAME_SERVICES = "SERVICES"; //$NON-NLS-1$

    private FilteredCheckboxTree filteredCheckboxTree;

    private CheckboxTreeViewer treeViewer;

    private Combo confsCombo;

    private Button addButton;

    private Button renameButton;

    private Button removeButton;

    private ProjectRepositoryNode root;

    private Map<String, DeploymentConfsModel> confsModels;

    private Set<String> confsToRemove = new HashSet<>();

    private Map<String, RepositoryNode> allModulesElementsMap = new LinkedHashMap<>();

    private DeploymentConfsUtils deploymentConfsUtils;

    private RelationshipItemBuilder builder = RelationshipItemBuilder.getInstance();

    private ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

    protected ICheckStateListener checkStateListener = new ICheckStateListener() {

        @Override
        public void checkStateChanged(CheckStateChangedEvent event) {
            filteredCheckboxTree.calculateCheckedLeafNodes();
        }
    };

    public MavenDeploymentConfigurationProjectSettingPage() {
        super();
        noDefaultAndApplyButton();

        root = new ProjectRepositoryNode(null, null, ENodeType.STABLE_SYSTEM_FOLDER);
        root.setOptions(IRepositoryFactory.OPTION_ALL_VERSION | IRepositoryFactory.OPTION_SKIP_DELETED);
        root.initialize(null);
        deploymentConfsUtils = new DeploymentConfsUtils(ProjectManager.getInstance().getCurrentProject());
    }

    private DeploymentConfsModel getSelectedConf() {
        String selectedConfName = confsCombo.getItem(confsCombo.getSelectionIndex());
        return confsModels.get(selectedConfName);
    }

    private Set<Object> getCheckedElements() {
        return getSelectedConf().getElements();
    }

    @SuppressWarnings("restriction")
    private void processAllElements(RepositoryNode node) {
        if (node == null) {
            return;
        }
        if (node.getType() == ENodeType.REPOSITORY_ELEMENT) {
            if (node.getObject() != null) {
                String moduleId = deploymentConfsUtils.getModuleId(node.getObject());
                allModulesElementsMap.put(moduleId, node);
            }
        } else {
            IContentProvider contentProvider = treeViewer.getContentProvider();
            if (contentProvider instanceof NavigatorContentServiceContentProvider) {
                NavigatorContentServiceContentProvider navigatorProvider = (NavigatorContentServiceContentProvider) contentProvider;
                Object[] children = navigatorProvider.getChildren(node);
                for (Object child : children) {
                    if (child instanceof RepositoryNode) {
                        processAllElements((RepositoryNode) child);
                    }
                }
            } else {
                for (IRepositoryNode child : node.getChildren()) {
                    processAllElements((RepositoryNode) child);
                }
            }
        }
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.horizontalSpacing = 0;
        composite.setLayout(gridLayout);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.widthHint = 570;
        composite.setLayoutData(gridData);

        Composite topCompsite = new Composite(composite, SWT.NONE);
        GridLayout topCompsiteLayout = new GridLayout(4, false);
        topCompsiteLayout.marginHeight = 2;
        topCompsite.setLayout(topCompsiteLayout);
        GridData topCompsiteData = new GridData(GridData.FILL_HORIZONTAL);
        topCompsite.setLayoutData(topCompsiteData);

        confsCombo = new Combo(topCompsite, SWT.READ_ONLY);
        GridData confData = new GridData();

        GC gc = new GC(confsCombo);
        Point labelSize = gc.stringExtent(DEFAULT_CONF_NAME);
        gc.dispose();
        int hint = labelSize.x + (ITabbedPropertyConstants.HSPACE * 40);
        confData.widthHint = hint;
        confsCombo.setLayoutData(confData);

        addButton = new Button(topCompsite, SWT.NONE);
        addButton.setText("Add"); //$NON-NLS-1$

        renameButton = new Button(topCompsite, SWT.NONE);
        renameButton.setText("Rename"); //$NON-NLS-1$

        removeButton = new Button(topCompsite, SWT.NONE);
        removeButton.setText("Remove"); //$NON-NLS-1$

        Composite buttomCompsite = new Composite(composite, SWT.BORDER);
        GridLayoutFactory.swtDefaults().numColumns(2).applyTo(buttomCompsite);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(buttomCompsite);

        addRepositoryTreeViewer(buttomCompsite);

        createSelectionButton(buttomCompsite);

        initFields();

        addListeners();

        return composite;
    }

    private void addRepositoryTreeViewer(Composite composite) {
        filteredCheckboxTree = new FilteredCheckboxTree(composite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI) {

            @Override
            protected CheckboxTreeViewer doCreateTreeViewer(Composite parent, int style) {
                RepoCommonViewerProvider provider = new RepoCommonViewerProvider() {

                    @Override
                    protected TreeViewer createTreeViewer(Composite parent, int style) {
                        return new CheckboxRepoCommonViewer(getViewId(), parent, style) {

                            @Override
                            protected Object[] getSortedChildren(Object parentElementOrTreePath) {
                                Object[] children = super.getSortedChildren(parentElementOrTreePath);
                                sortChildren(this, parentElementOrTreePath, children);
                                return children;
                            }
                        };
                    }

                    @Override
                    public IProjectRepositoryNode getProjectRepositoryNode() {
                        return root;
                    }

                };
                RepositoryLabelProvider.setRefresh(false);

                CheckboxTreeViewer viewer = (CheckboxTreeViewer) provider.createViewer(parent);
                return viewer;
            }

            @Override
            protected Composite createFilterControls(Composite parent) {
                createFilterText(parent);
                return parent;
            }

            @Override
            protected void refreshCompleted() {
                getViewer().expandToLevel(3);
                restoreCheckedElements();
            }

            @Override
            public void calculateCheckedLeafNodes() {
                TreeItem[] roots = getViewer().getTree().getItems();
                Set<Object> checked = new HashSet<>();
                Set<Object> unchecked = new HashSet<>();
                collectLeafNodes(roots, checked, unchecked);
                Set<Object> currentConfCheckedElements = getCheckedElements();
                currentConfCheckedElements.removeAll(unchecked);
                currentConfCheckedElements.addAll(checked);
            }

            @Override
            public Object[] getCheckedLeafNodes() {
                return getCheckedElements().toArray();
            }

            @Override
            public void resetCheckedElements() {
                getCheckedElements().clear();
            }

        };

        treeViewer = filteredCheckboxTree.getViewer();
        treeViewer.addFilter(new ViewerFilter() {

            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                RepositoryNode node = (RepositoryNode) element;
                return filterRepositoryNode(node);
            }
        });
        treeViewer.addCheckStateListener(checkStateListener);
        treeViewer.expandToLevel(3);
        GridData filterData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
        filterData.horizontalSpan = 2;
        filteredCheckboxTree.getFilterControl().setLayoutData(filterData);
    }

    private void createSelectionButton(Composite composite) {
        Composite buttonComposite = new Composite(composite, SWT.NONE);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(buttonComposite);
        buttonComposite.setLayout(new GridLayout(1, false));

        Button hide = new Button(buttonComposite, SWT.PUSH);
        hide.setVisible(false);
        Button selectAll = new Button(buttonComposite, SWT.PUSH);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(selectAll);
        selectAll.setText(Messages.getString("DataTransferMessages.DataTransfer_selectAll")); //$NON-NLS-1$
        selectAll.addSelectionListener(new SelectionAdapter() {

            @SuppressWarnings("deprecation")
            @Override
            public void widgetSelected(SelectionEvent e) {
                treeViewer.removeCheckStateListener(checkStateListener);
                treeViewer.setAllChecked(true);
                treeViewer.addCheckStateListener(checkStateListener);
                getCheckedElements().addAll(allModulesElementsMap.values());
            }
        });

        Button deselectAll = new Button(buttonComposite, SWT.PUSH);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(deselectAll);
        deselectAll.setText(Messages.getString("DataTransferMessages.DataTransfer_deselectAll")); //$NON-NLS-1$
        deselectAll.addSelectionListener(new SelectionAdapter() {

            @SuppressWarnings("deprecation")
            @Override
            public void widgetSelected(SelectionEvent e) {
                treeViewer.removeCheckStateListener(checkStateListener);
                treeViewer.setAllChecked(false);
                treeViewer.addCheckStateListener(checkStateListener);
                getCheckedElements().clear();
            }
        });

        Button expandBtn = new Button(buttonComposite, SWT.PUSH);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(expandBtn);
        expandBtn.setText("Expand All"); //$NON-NLS-1$
        expandBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                treeViewer.expandAll();
            }
        });

        Button collapseBtn = new Button(buttonComposite, SWT.PUSH);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(collapseBtn);
        collapseBtn.setText("Collapse All"); //$NON-NLS-1$
        collapseBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                treeViewer.collapseAll();
            }
        });
    }

    private boolean filterRepositoryNode(RepositoryNode node) {
        if (node == null) {
            return false;
        }
        if (node.isBin()) {
            return false;
        }
        if (node.getObject() != null) {
            // ERepositoryStatus status = FACTORY.getStatus(node.getObject());
            // maybe don't need?
            // if (status == ERepositoryStatus.LOCK_BY_OTHER) {
            // return false;
            // }
            if (node.getObject().isDeleted()) {
                return false;
            }
        }
        ERepositoryObjectType type = node.getObjectType();
        if (type == null) {
            if (node.getProperties(EProperties.LABEL) instanceof ERepositoryObjectType) {
                type = (ERepositoryObjectType) node.getProperties(EProperties.LABEL);
            }
            if (type == null) {
                return false;
            }
        }

        if (node.getObject() != null && !ProjectManager.getInstance().isInCurrentMainProject(node.getObject().getProperty())) {
            return false;
        }

        if (type == ERepositoryObjectType.FOLDER) {
            if (node.getObject() instanceof Folder) {
                type = ((Folder) node.getObject()).getContentType();
            }
        }
        if (type == ERepositoryObjectType.PROCESS || type == ERepositoryObjectType.PROCESS_MR
        // TODO release this until ESB worked on CI
        // || type == ERepositoryObjectType.valueOf(NODENAME_ROUTE_DESIGNS)
        // || type == ERepositoryObjectType.valueOf(NODENAME_SERVICES) || type == ERepositoryObjectType.PROCESS_ROUTE
                || type == ERepositoryObjectType.PROCESS_SPARK || type == ERepositoryObjectType.PROCESS_SPARKSTREAMING
                || type == ERepositoryObjectType.PROCESS_STORM) {
            return true;
        }
        return false;
    }

    private void initFields() {
        confsModels = deploymentConfsUtils.loadConfs();
        processAllElements(root);
        if (!confsModels.isEmpty()) {
            initCheckedElements();
        } else {
            DeploymentConfsModel confModel = new DeploymentConfsModel(DEFAULT_CONF_NAME);
            confsModels.put(DEFAULT_CONF_NAME, confModel);
        }
        confsCombo.setItems(confsModels.keySet().toArray(new String[] {}));
        confsCombo.select(0);
        filteredCheckboxTree.restoreCheckedElements();
    }

    private void addListeners() {
        confsCombo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                filteredCheckboxTree.restoreCheckedElements();
            }
        });

        addButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                DeploymentConfsDialog addDialog = new DeploymentConfsDialog(getShell(), TITLE_ADD);
                if (addDialog.open() == Window.OK) {
                    String confName = addDialog.getInputText();
                    DeploymentConfsModel model = new DeploymentConfsModel(confName);
                    confsModels.put(confName, model);
                    confsCombo.setItems(confsModels.keySet().toArray(new String[] {}));
                    confsCombo.select(confsModels.size() - 1);
                    filteredCheckboxTree.restoreCheckedElements();
                }
            }
        });

        renameButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                DeploymentConfsDialog addDialog = new DeploymentConfsDialog(getShell(), TITLE_RENAME);
                if (addDialog.open() == Window.OK) {
                    DeploymentConfsModel confModel = getSelectedConf();
                    String oldName = confModel.getConfName();
                    String newName = addDialog.getInputText();
                    confModel.setConfName(newName);
                    confsModels.remove(oldName);
                    confsToRemove.add(oldName);
                    confsModels.put(newName, confModel);
                    confsCombo.setItems(confsModels.keySet().toArray(new String[] {}));
                    confsCombo.select(confsModels.size() - 1);
                    filteredCheckboxTree.restoreCheckedElements();
                }
            }
        });

        removeButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (confsModels.size() < 2) {
                    MessageDialog.openInformation(getShell(), "Information",
                            Messages.getString("MavenDeploymentConfigurationProjectSettingPage.removeMsg")); //$NON-NLS-1$
                    return;
                }
                if (MessageDialog.openConfirm(getShell(), "Confirm",
                        Messages.getString("MavenDeploymentConfigurationProjectSettingPage.removeWarning"))) { //$NON-NLS-1$
                    String confName = getSelectedConf().getConfName();
                    confsModels.remove(confName);
                    confsToRemove.add(confName);
                    confsCombo.setItems(confsModels.keySet().toArray(new String[] {}));
                    confsCombo.select(0);
                    filteredCheckboxTree.restoreCheckedElements();
                }
            }
        });

    }

    private void initCheckedElements() {
        for (DeploymentConfsModel confModel : confsModels.values()) {
            Map<String, String> modules = confModel.getModules();
            for (Map.Entry<String, String> entry : modules.entrySet()) {
                String moduleId = entry.getKey();
                if (allModulesElementsMap.containsKey(moduleId)) {
                    confModel.getElements().add(allModulesElementsMap.get(moduleId));
                }
            }
        }
    }

    @Override
    public void refresh() {
    }

    @Override
    public boolean performCancel() {
        RepositoryLabelProvider.setRefresh(true);
        return super.performCancel();
    }

    @Override
    public boolean performOk() {
        if (confsModels == null) {
            return true;
        }
        RepositoryWorkUnit<Object> workUnit = new RepositoryWorkUnit<Object>("update deployment confs") { //$NON-NLS-1$

            @Override
            protected void run() {
                try {
                    deploymentConfsUtils.removeDeletedConfsFolder(confsToRemove);

                    for (Map.Entry<String, DeploymentConfsModel> entry : confsModels.entrySet()) {
                        DeploymentConfsModel confModel = entry.getValue();
                        Set<Object> selectedElement = confModel.getElements();
                        Map<String, String> modules = confModel.getModules();
                        modules.clear();
                        addCodesModules(modules);
                        for (Object obj : selectedElement) {
                            if (obj instanceof RepositoryNode) {
                                RepositoryNode node = (RepositoryNode) obj;
                                // FIXME should filter all non-process type from node.
                                // remove ref project nodes.
                                if (node.getObjectType() != null && filterRepositoryNode(node)) {
                                    addToModules(node.getObject(), modules);
                                }
                            }
                        }
                        deploymentConfsUtils.writeConfModelToJsonFile(confModel);
                    }
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
            }
        };
        workUnit.setAvoidUnloadResources(true);
        ProxyRepositoryFactory.getInstance().executeRepositoryWorkUnit(workUnit);

        RepositoryLabelProvider.setRefresh(true);
        return super.performOk();
    }

    private void addToModules(IRepositoryViewObject object, Map<String, String> modules) throws PersistenceException {
        String moduleId = deploymentConfsUtils.getModuleId(object);
        String modulePath = deploymentConfsUtils.calculateModulePath(object);
        if (!modules.containsKey(moduleId)) {
            modules.put(moduleId, modulePath);
            List<Relation> relations = builder.getItemsJobRelatedTo(object.getId(), object.getVersion(),
                    RelationshipItemBuilder.JOB_RELATION);
            for (Relation relation : relations) {
                String id = relation.getId();
                String version = relation.getVersion();
                IRepositoryViewObject subJobObject;
                if (RelationshipItemBuilder.LATEST_VERSION.equals(version)) {
                    subJobObject = factory.getLastVersion(id);
                } else {
                    subJobObject = factory.getSpecificVersion(id, version, true);
                }
                if (subJobObject != null && ProjectManager.getInstance().isInCurrentMainProject(subJobObject.getProperty())) {
                    addToModules(subJobObject, modules);
                }
            }
        }
    }

    protected void addCodesModules(Map<String, String> modules) {
        String KEY = "modules"; //$NON-NLS-1$
        String CODES = TalendJavaProjectConstants.DIR_CODES;
        String ROUTINES = TalendJavaProjectConstants.DIR_ROUTINES;
        String BEANS = TalendJavaProjectConstants.DIR_BEANS;
        String SEPARATOR = "|"; //$NON-NLS-1$
        String SLASH = "/"; //$NON-NLS-1$
        modules.put(KEY + SEPARATOR + "ci", "../" + TalendJavaProjectConstants.FILE_POM_CI_BUILDER); //$NON-NLS-1$ //$NON-NLS-2$
        modules.put(KEY + SEPARATOR + ROUTINES, "../../" + CODES + SLASH + ROUTINES); //$NON-NLS-1$

        if (ProcessUtils.isRequiredBeans(null)) {
            modules.put(KEY + SEPARATOR + BEANS, "../../" + CODES + SLASH + BEANS); //$NON-NLS-1$
        }
    }

    class DeploymentConfsDialog extends Dialog {

        private Text text;

        private String confName;

        private String title;

        protected DeploymentConfsDialog(Shell parent, String title) {
            super(parent);
            this.title = title;
        }

        @Override
        protected Control createDialogArea(Composite parent) {
            final Composite composite = (Composite) super.createDialogArea(parent);
            composite.setLayout(new GridLayout(1, false));
            getShell().setText(title);
            Label label = new Label(composite, SWT.NONE);
            text = new Text(composite, SWT.BORDER);
            GridDataFactory.fillDefaults().grab(true, false).applyTo(text);
            text.addModifyListener(new ModifyListener() {

                @Override
                public void modifyText(ModifyEvent e) {
                    String confName = text.getText();
                    if (confsModels.keySet().contains(confName)) {
                        getButton(IDialogConstants.OK_ID).setEnabled(false);
                        label.setText(Messages.getString("MavenDeploymentConfigurationProjectSettingPage.nameExistedWarning", //$NON-NLS-1$
                                confName));

                        label.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
                    } else {
                        getButton(IDialogConstants.OK_ID).setEnabled(true);
                        label.setText(""); //$NON-NLS-1$
                    }
                    label.getParent().layout();

                }
            });
            return composite;
        }

        public String getInputText() {
            return confName;
        }

        @Override
        protected void okPressed() {
            if (text != null && !text.isDisposed()) {
                confName = text.getText();
            }
            super.okPressed();
        }

    }

}
