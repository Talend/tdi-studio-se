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
package org.talend.repository.imports;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.wizards.datatransfer.ArchiveFileManipulations;
import org.eclipse.ui.internal.wizards.datatransfer.TarException;
import org.eclipse.ui.internal.wizards.datatransfer.TarFile;
import org.eclipse.ui.internal.wizards.datatransfer.TarLeveledStructureProvider;
import org.osgi.framework.FrameworkUtil;
import org.talend.commons.exception.LoginException;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobDocumentationItem;
import org.talend.core.model.properties.JobletDocumentationItem;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.Project;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.RepositoryViewObject;
import org.talend.core.model.utils.TalendPropertiesUtil;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.service.IExchangeService;
import org.talend.core.ui.advanced.composite.FilteredCheckboxTree;
import org.talend.repository.ProjectManager;
import org.talend.repository.i18n.Messages;
import org.talend.repository.imports.TreeBuilder.IContainerNode;
import org.talend.repository.imports.TreeBuilder.ProjectNode;
import org.talend.repository.imports.TreeBuilder.TypeNode;
import org.talend.repository.items.importexport.ui.managers.TalendZipLeveledStructureProvider;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;

/**
 * Initialy copied from org.eclipse.ui.internal.wizards.datatransfer.WizardProjectsImportPage.
 *
 * @deprecated have moved to /org.talend.repository.items.importexport, but still need for MDM, so keep it temp.
 */
@Deprecated
class ImportItemWizardPage extends WizardPage {

    private final ImportItemUtil repositoryUtil = new ImportItemUtil();

    private Button itemFromDirectoryRadio;

    private Text directoryPathField;

    protected Shell shell;

    private Button browseDirectoriesButton;

    private Button itemFromArchiveRadio;

    private Text archivePathField;

    private Button browseArchivesButton;

    private Button selectExchangeButton;

    private String previouslyBrowsedDirectory = ""; //$NON-NLS-1$

    private Object lastPath;

    private List<ItemRecord> selectedItems = new ArrayList<ItemRecord>();

    private CheckboxTreeViewer checkTreeViewer;

    private String previouslyBrowsedArchive = ""; //$NON-NLS-1$

    protected boolean allVersions = false;

    private static final String[] FILE_IMPORT_MASK = { "*.jar;*.zip;*.tar;*.tar.gz;*.tgz", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$

    // private Collection<ItemRecord> items = new ArrayList<ItemRecord>();

    private Label itemListInfo;

    private Label nothing;

    private TableViewer errorsList;

    private final List<String> errors = new ArrayList<String>();

    private ResourcesManager manager;

    private Button overwriteButton;

    boolean overwrite = false;

    private FilteredCheckboxTree filteredCheckboxTree;

    private boolean needToRefreshPalette;

    protected String selectedArchive;

    protected RepositoryNode rNode;

    private ZipFile sourceFile;

    private TarFile sourceTarFile;

    private List<ItemRecord> totalItemRecords = new ArrayList<ItemRecord>();

    protected ImportItemWizardPage(RepositoryNode rNode, String pageName) {

        super(pageName);
        this.rNode = rNode;
        setDescription(Messages.getString("ImportItemWizardPage.ImportDescription")); //$NON-NLS-1$
        // setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_WIZBAN_IMPORT_WIZ));
        setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor("IMG_WIZBAN_IMPORT_WIZ")); //$NON-NLS-1$
    }

    @Override
    public void createControl(Composite parent) {
        Composite workArea = new Composite(parent, SWT.NONE);
        setControl(workArea);

        workArea.setLayout(new GridLayout());
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));

        createItemRoot(workArea);

        createItemList(workArea);
        createErrorsList(workArea);

        // see feature 3949
        overwriteButton = new Button(workArea, SWT.CHECK);
        overwriteButton.setText(Messages.getString("ImportItemWizardPage.overwriteButtonText")); //$NON-NLS-1$
        overwriteButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                overwrite = overwriteButton.getSelection();
                if (StringUtils.isNotEmpty(directoryPathField.getText()) || StringUtils.isNotEmpty(archivePathField.getText())) {
                    populateItems();
                }
            }

        });
    }

    private void createErrorsList(Composite workArea) {
        Composite composite = new Composite(workArea, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.makeColumnsEqualWidth = false;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        GridData gridData = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH);
        gridData.heightHint = 100;
        composite.setLayoutData(gridData);

        Label title = new Label(composite, SWT.NONE);
        title.setText(Messages.getString("ImportItemWizardPage.ErrorsAndWarnings")); //$NON-NLS-1$
        title.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        errorsList = new TableViewer(composite, SWT.BORDER);
        errorsList.getControl().setLayoutData(gridData);

        errorsList.setContentProvider(new IStructuredContentProvider() {

            @Override
            public void dispose() {
            }

            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            }

            @Override
            public Object[] getElements(Object inputElement) {
                return errors.toArray();
            }
        });

        errorsList.setLabelProvider(new LabelProvider() {

            @Override
            public String getText(Object element) {
                return element.toString();
            }
        });

        errorsList.setInput(this);
        errorsList.setSorter(new ViewerSorter());
    }

    private void createItemList(Composite workArea) {
        Composite labelComposite = new Composite(workArea, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginWidth = 0;
        layout.makeColumnsEqualWidth = false;
        labelComposite.setLayout(layout);

        Label title = new Label(labelComposite, SWT.NONE);
        title.setText(Messages.getString("ImportItemWizardPage.ItemsList")); //$NON-NLS-1$

        itemListInfo = new Label(labelComposite, SWT.NONE);
        itemListInfo.setForeground(new Color(null, 255, 0, 0)); // red
        itemListInfo.setText(Messages.getString("ImportItemWizardPage.NoValidItems")); //$NON-NLS-1$
        itemListInfo.setVisible(false);

        Composite listComposite = new Composite(workArea, SWT.NONE);
        GridLayout layout2 = new GridLayout();
        layout2.numColumns = 2;
        layout2.marginWidth = 0;
        layout2.makeColumnsEqualWidth = false;
        listComposite.setLayout(layout2);

        GridData gridData = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH);
        gridData.heightHint = 250;
        gridData.widthHint = 600;
        listComposite.setLayoutData(gridData);

        checkTreeViewer = (CheckboxTreeViewer) createTreeViewer(listComposite);

        createSelectionButtons(listComposite);

    }

    private TreeViewer createTreeViewer(Composite listComposite) {
        filteredCheckboxTree = new FilteredCheckboxTree(listComposite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI);

        CheckboxTreeViewer viewer = filteredCheckboxTree.getViewer();

        viewer.setContentProvider(new ITreeContentProvider() {

            @Override
            public Object[] getChildren(Object parentElement) {
                if (parentElement instanceof IContainerNode) {
                    return ((IContainerNode) parentElement).getChildren().toArray();
                }
                return null;
            }

            @Override
            public Object[] getElements(Object inputElement) {
                // return getValidItems();
                return repositoryUtil.getTreeViewInput().toArray();
            }

            @Override
            public boolean hasChildren(Object element) {
                if (element instanceof IContainerNode) {
                    return ((IContainerNode) element).hasChildren();
                }
                return false;
            }

            @Override
            public Object getParent(Object element) {
                return null;
            }

            @Override
            public void dispose() {

            }

            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            }

        });

        viewer.setLabelProvider(new LabelProvider() {

            @Override
            public String getText(Object element) {
                if (element instanceof IContainerNode) {
                    if (element instanceof ProjectNode) {
                        Project project = ((ProjectNode) element).getProject();
                        return ProjectManager.getProjectDisplayLabel(project);
                    }
                    return ((IContainerNode) element).getLabel();
                } else if (element instanceof ItemRecord) {
                    return ((ItemRecord) element).getLabel();
                }
                return super.getText(element);

            }

            @Override
            public Image getImage(Object element) {
                if (element instanceof IContainerNode) {
                    return ((IContainerNode) element).getImage();
                } else if (element instanceof ItemRecord) {
                    return ((ItemRecord) element).getImage();
                }

                return super.getImage(element);
            }

        });
        viewer.setSorter(TreeBuilder.createSorter());
        viewer.setInput(this);
        viewer.addCheckStateListener(new ICheckStateListener() {

            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                filteredCheckboxTree.calculateCheckedLeafNodes();
                updateFinishStatus();
            }
        });

        filteredCheckboxTree.getViewer().addFilter(new ViewerFilter() {

            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                return selectRepositoryNode(viewer, element);
            }
        });

        return viewer;
    }

    protected boolean selectRepositoryNode(Viewer viewer, Object element) {
        if (element == null) {
            return false;
        }
        // hide the generated documentation node , avoid to import .
        if (element != null && element instanceof TypeNode) {
            ERepositoryObjectType type = ((TypeNode) element).type;
            if (type.equals(ERepositoryObjectType.JOB_DOC) || type.equals(ERepositoryObjectType.JOBLET_DOC)) {
                return false;
            }
        }
        if (element instanceof IContainerNode) {
            List importElement = ((IContainerNode) element).getChildren();
            for (Object node : importElement) {
                if (node != null && node instanceof ItemRecord) {
                    ERepositoryObjectType type = ((ItemRecord) node).getType();
                    if (type.equals(ERepositoryObjectType.JOB_DOC) || type.equals(ERepositoryObjectType.JOBLET_DOC)) {
                        return false;
                    }
                }
            }
        } else if (element instanceof ItemRecord) {
            ERepositoryObjectType type = ((ItemRecord) element).getType();
            if (type.equals(ERepositoryObjectType.JOB_DOC) || type.equals(ERepositoryObjectType.JOBLET_DOC)) {
                return false;
            }
        }
        return true;
    }

    private void createSelectionButtons(Composite listComposite) {
        Composite buttonsComposite = new Composite(listComposite, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 25;
        buttonsComposite.setLayout(layout);

        buttonsComposite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));

        Button selectAll = new Button(buttonsComposite, SWT.PUSH);
        // selectAll.setText(DataTransferMessages.DataTransfer_selectAll);
        selectAll.setText(Messages.getString("DataTransferMessages.DataTransfer_selectAll")); //$NON-NLS-1$
        selectAll.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (checkTreeViewer.getTree().getItemCount() > 0) {
                    for (int i = 0; i < checkTreeViewer.getTree().getItemCount(); i++) {
                        TreeItem topItem = checkTreeViewer.getTree().getItem(i)/* .getTopItem() */;
                        if (topItem != null) {
                            checkTreeViewer.setSubtreeChecked(topItem.getData(), true);
                            filteredCheckboxTree.calculateCheckedLeafNodes();
                            updateFinishStatus();
                        }
                    }
                }
            }
        });
        Dialog.applyDialogFont(selectAll);
        setButtonLayoutData(selectAll);

        Button deselectAll = new Button(buttonsComposite, SWT.PUSH);
        // deselectAll.setText(DataTransferMessages.DataTransfer_deselectAll);
        deselectAll.setText(Messages.getString("DataTransferMessages.DataTransfer_deselectAll")); //$NON-NLS-1$
        deselectAll.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                checkTreeViewer.setCheckedElements(new Object[0]);
                filteredCheckboxTree.calculateCheckedLeafNodes();
                updateFinishStatus();
            }
        });
        Dialog.applyDialogFont(deselectAll);
        setButtonLayoutData(deselectAll);

        Button refresh = new Button(buttonsComposite, SWT.PUSH);
        // refresh.setText(DataTransferMessages.DataTransfer_refresh);
        refresh.setText(Messages.getString("DataTransferMessages.DataTransfer_refresh")); //$NON-NLS-1$
        refresh.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (itemFromDirectoryRadio.getSelection()) {
                    updateItemsList(directoryPathField.getText().trim(), true);
                } else {
                    updateItemsList(archivePathField.getText().trim(), true);
                }
            }
        });
        Dialog.applyDialogFont(refresh);
        setButtonLayoutData(refresh);
    }

    private void createItemRoot(Composite workArea) {
        Composite projectGroup = new Composite(workArea, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 4;
        layout.makeColumnsEqualWidth = false;
        layout.marginWidth = 0;
        projectGroup.setLayout(layout);
        projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        itemFromDirectoryRadio = new Button(projectGroup, SWT.RADIO);
        // itemFromDirectoryRadio.setText(DataTransferMessages.WizardProjectsImportPage_RootSelectTitle);
        itemFromDirectoryRadio.setText(Messages.getString("DataTransferMessages.WizardProjectsImportPage_RootSelectTitle")); //$NON-NLS-1$

        this.directoryPathField = new Text(projectGroup, SWT.BORDER);

        this.directoryPathField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));

        browseDirectoriesButton = new Button(projectGroup, SWT.PUSH);
        // browseDirectoriesButton.setText(DataTransferMessages.DataTransfer_browse);
        browseDirectoriesButton.setText(Messages.getString("DataTransferMessages.DataTransfer_browse")); //$NON-NLS-1$
        setButtonLayoutData(browseDirectoriesButton);

        nothing = new Label(projectGroup, SWT.NONE);
        nothing.setText(" "); //$NON-NLS-1$

        // new project from archive radio button
        itemFromArchiveRadio = new Button(projectGroup, SWT.RADIO);
        // itemFromArchiveRadio.setText(DataTransferMessages.WizardProjectsImportPage_ArchiveSelectTitle);
        itemFromArchiveRadio.setText(Messages.getString("DataTransferMessages.WizardProjectsImportPage_ArchiveSelectTitle")); //$NON-NLS-1$

        // project location entry field
        archivePathField = new Text(projectGroup, SWT.BORDER);

        archivePathField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
        // browse button
        // Composite buttonCom = new Composite(projectGroup, SWT.NONE);
        // GridLayout buttonlayout = new GridLayout();
        // buttonlayout.numColumns = 2;
        // buttonlayout.makeColumnsEqualWidth = false;
        // buttonlayout.marginWidth = 0;
        // buttonCom.setLayout(buttonlayout);
        // buttonCom.setLayoutData(new GridData());
        browseArchivesButton = new Button(projectGroup, SWT.PUSH);
        // browseArchivesButton.setText(DataTransferMessages.DataTransfer_browse);
        browseArchivesButton.setText(Messages.getString("DataTransferMessages.DataTransfer_browse")); //$NON-NLS-1$
        setButtonLayoutData(browseArchivesButton);
        if (PluginChecker.isExchangeSystemLoaded() && !TalendPropertiesUtil.isHideExchange()) {
            selectExchangeButton = new Button(projectGroup, SWT.PUSH);
            selectExchangeButton.setText(Messages.getString("ImportItemWizardPage.browseTalend")); //$NON-NLS-1$
            setButtonLayoutData(selectExchangeButton);
            selectExchangeButton.setEnabled(false);
        }
        itemFromDirectoryRadio.setSelection(true);
        archivePathField.setEnabled(false);
        browseArchivesButton.setEnabled(false);

        browseDirectoriesButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                handleLocationDirectoryButtonPressed();
            }
        });

        browseArchivesButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                handleLocationArchiveButtonPressed();
            }
        });
        if (selectExchangeButton != null) {
            selectExchangeButton.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {

                    archivePathField.setEditable(false);

                    IExchangeService service = GlobalServiceRegister.getDefault().getService(
                            IExchangeService.class);

                    selectedArchive = service.openExchangeDialog();
                    if (selectedArchive != null) {
                        previouslyBrowsedArchive = selectedArchive;
                        archivePathField.setText(previouslyBrowsedArchive);
                        updateItemsList(selectedArchive, false);
                    }

                }
            });
        }
        directoryPathField.addTraverseListener(new TraverseListener() {

            @Override
            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_RETURN) {
                    e.doit = false;
                    updateItemsList(directoryPathField.getText().trim(), false);
                }
            }

        });

        directoryPathField.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(org.eclipse.swt.events.FocusEvent e) {
                updateItemsList(directoryPathField.getText().trim(), false);
            }

        });

        archivePathField.addTraverseListener(new TraverseListener() {

            @Override
            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_RETURN) {
                    e.doit = false;
                    updateItemsList(archivePathField.getText().trim(), false);
                }
            }
        });

        archivePathField.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(org.eclipse.swt.events.FocusEvent e) {
                updateItemsList(archivePathField.getText().trim(), false);
            }
        });

        itemFromDirectoryRadio.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                directoryRadioSelected();
            }
        });

        itemFromArchiveRadio.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                archiveRadioSelected();
            }
        });
    }

    private void archiveRadioSelected() {
        if (itemFromArchiveRadio.getSelection()) {
            directoryPathField.setEnabled(false);
            browseDirectoriesButton.setEnabled(false);
            archivePathField.setEnabled(true);
            browseArchivesButton.setEnabled(true);
            if (selectExchangeButton != null) {
                selectExchangeButton.setEnabled(true);
            }
            updateItemsList(archivePathField.getText(), false);
            archivePathField.setFocus();
        }
    }

    protected void handleLocationDirectoryButtonPressed() {

        DirectoryDialog dialog = new DirectoryDialog(directoryPathField.getShell());
        // dialog.setMessage(DataTransferMessages.WizardProjectsImportPage_SelectDialogTitle);
        dialog.setMessage(Messages.getString("DataTransferMessages.WizardProjectsImportPage_SelectDialogTitle")); //$NON-NLS-1$

        String dirName = directoryPathField.getText().trim();
        if (dirName.length() == 0) {
            dirName = previouslyBrowsedDirectory;
        }

        if (dirName.length() == 0) {
            // dialog.setFilterPath(IDEWorkbenchPlugin.getPluginWorkspace().getRoot().getLocation().toOSString());
            dialog.setFilterPath(ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString());
        } else {
            File path = new File(dirName);
            if (path.exists()) {
                dialog.setFilterPath(new Path(dirName).toOSString());
            }
        }

        String selectedDirectory = dialog.open();
        if (selectedDirectory != null) {
            previouslyBrowsedDirectory = selectedDirectory;
            directoryPathField.setText(previouslyBrowsedDirectory);
            updateItemsList(selectedDirectory, false);
        }

    }

    /**
     * The browse button has been selected. Select the location.
     */
    protected void handleLocationArchiveButtonPressed() {

        FileDialog dialog = new FileDialog(archivePathField.getShell());
        dialog.setFilterExtensions(FILE_IMPORT_MASK);
        // dialog.setText(DataTransferMessages.WizardProjectsImportPage_SelectArchiveDialogTitle);
        dialog.setText(Messages.getString(".WizardProjectsImportPage_SelectArchiveDialogTitle")); //$NON-NLS-1$

        String fileName = archivePathField.getText().trim();
        if (fileName.length() == 0) {
            fileName = previouslyBrowsedArchive;
        }

        if (fileName.length() == 0) {
            dialog.setFilterPath(ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString());
        } else {
            File path = new File(fileName);
            if (path.exists()) {
                dialog.setFilterPath(new Path(fileName).toOSString());
            }
        }

        String selectedArchive = dialog.open();
        if (selectedArchive != null) {
            previouslyBrowsedArchive = selectedArchive;
            archivePathField.setText(previouslyBrowsedArchive);
            updateItemsList(selectedArchive, false);
        }

    }

    private void directoryRadioSelected() {
        if (itemFromDirectoryRadio.getSelection()) {
            directoryPathField.setEnabled(true);
            browseDirectoriesButton.setEnabled(true);
            archivePathField.setEnabled(false);
            browseArchivesButton.setEnabled(false);
            if (selectExchangeButton != null) {
                selectExchangeButton.setEnabled(false);
            }
            updateItemsList(directoryPathField.getText(), false);
            directoryPathField.setFocus();
        }
    }

    public void updateItemsList(final String path, boolean isneedUpdate) {
        if (!isneedUpdate) {
            if (path.equals(lastPath)) {
                return;
            }
        }
        lastPath = path;

        if (path == null || path.length() == 0) {
            selectedItems = new ArrayList<ItemRecord>();
            checkTreeViewer.refresh(true);
            // get the top item to check if tree is empty, if not then uncheck everything
            TreeItem topItem = checkTreeViewer.getTree().getTopItem();
            if (topItem != null) {
                checkTreeViewer.setSubtreeChecked(topItem.getData(), false);
            } // else not root element, tree is already empty
            checkValidItems();
            return;
        }

        final boolean dirSelected = this.itemFromDirectoryRadio.getSelection();
        try {
            getContainer().run(true, true, new IRunnableWithProgress() {

                @Override
                public void run(IProgressMonitor monitor) {

                    // monitor.beginTask(DataTransferMessages.WizardProjectsImportPage_SearchingMessage, 100);
                    monitor.beginTask(Messages.getString("DataTransferMessages.WizardProjectsImportPage_SearchingMessage"), 100); //$NON-NLS-1$
                    File directory = new File(path);
                    monitor.worked(10);
                    if (!dirSelected && ArchiveFileManipulations.isTarFile(path)) {
                        sourceTarFile = getSpecifiedTarSourceFile(path);
                        if (sourceTarFile == null) {
                            return;
                        }

                        TarLeveledStructureProvider provider = new TarLeveledStructureProvider(sourceTarFile);
                        manager = ResourcesManagerFactory.getInstance().createResourcesManager(provider);

                        if (!manager.collectPath2Object(provider.getRoot())) {
                            return;
                        }
                    } else if (!dirSelected && ArchiveFileManipulations.isZipFile(path)) {
                        sourceFile = getSpecifiedZipSourceFile(path);
                        if (sourceFile == null) {
                            return;
                        }
                        TalendZipLeveledStructureProvider provider = new TalendZipLeveledStructureProvider(sourceFile);
                        manager = ResourcesManagerFactory.getInstance().createResourcesManager(provider);

                        if (!manager.collectPath2Object(provider.getRoot())) {
                            return;
                        }
                    } else if (dirSelected && directory.isDirectory()) {
                        manager = ResourcesManagerFactory.getInstance().createResourcesManager();

                        if (!manager.collectPath2Object(directory)) {
                            return;
                        }
                    } else {
                        monitor.worked(60);
                    }
                    monitor.done();
                }

            });
        } catch (InvocationTargetException e) {
            IDEWorkbenchPlugin.log(e.getMessage(), e);
        } catch (InterruptedException e) {
            // Nothing to do if the user interrupts.
        }

        populateItems();
    }

    /**
     * DOC hcw Comment method "populateItems".
     */
    private void populateItems() {
        selectedItems.clear();
        final Collection<ItemRecord> items = new ArrayList<ItemRecord>();
        IRunnableWithProgress op = new IRunnableWithProgress() {

            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                repositoryUtil.clearAllData();
                items.addAll(totalItemRecords = repositoryUtil.populateItems(manager, overwrite, monitor));
            }

        };
        try {
            new ProgressMonitorDialog(getShell()).run(true, true, op);
        } catch (Exception e) {
            // ignore me
        }

        errors.clear();
        for (ItemRecord itemRecord : items) {
            // bug 21738
            if (itemRecord.getExistingItemWithSameId() != null
                    && itemRecord.getExistingItemWithSameId() instanceof RepositoryViewObject) {
                RepositoryViewObject reObject = (RepositoryViewObject) itemRecord.getExistingItemWithSameId();
                if (itemRecord.getProperty() != null && reObject != null) {
                    if (itemRecord.getProperty().getId().equals(reObject.getId())
                            && itemRecord.getProperty().getLabel().equals(reObject.getLabel())
                            && itemRecord.getProperty().getVersion().equals(reObject.getVersion())) {
                        for (String error : itemRecord.getErrors()) {
                            errors.add("'" + itemRecord.getItemName() + "' " + error); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                    } else if (itemRecord.getProperty().getId().equals(reObject.getId())
                            && itemRecord.getProperty().getLabel().equals(reObject.getLabel())
                            && !itemRecord.getProperty().getVersion().equals(reObject.getVersion())) {
                        for (String error : itemRecord.getErrors()) {
                            errors.add("'" + itemRecord.getItemName() + "' " + Messages.getString("ImportItemWizardPage.ErrorsMessage", reObject.getVersion())); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                    } else {
                        // TDI-21399,TDI-21401
                        // if item is locked, cannot overwrite
                        ERepositoryStatus status = reObject.getRepositoryStatus();
                        if (status == ERepositoryStatus.LOCK_BY_OTHER || status == ERepositoryStatus.LOCK_BY_USER) {
                            for (String error : itemRecord.getErrors()) {
                                errors.add("'" + itemRecord.getItemName() + "' " + error); //$NON-NLS-1$ //$NON-NLS-2$
                            }
                        }
                    }
                }
            } else {
                if (itemRecord.getProperty() != null) {
                    Item item = itemRecord.getProperty().getItem();
                    if (item != null && (item instanceof JobDocumentationItem || item instanceof JobletDocumentationItem)) {
                        continue;
                    }
                    for (String error : itemRecord.getErrors()) {
                        errors.add("'" + itemRecord.getItemName() + "' " + error); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
            }
        }
        if (errorsList != null) {
            errorsList.refresh();
        }

        selectedItems.addAll(items);

        checkTreeViewer.refresh(true);
        checkTreeViewer.expandAll();
        filteredCheckboxTree.resetCheckedElements();
        checkValidItems();
        updateFinishStatus();
        // see feature 0004170: Unselect all items to import
        // itemsList.setCheckedElements(checkValidItems());
    }

    private ItemRecord[] checkValidItems() {
        ItemRecord[] validItems = getValidItems();
        boolean hasValidItems = validItems.length > 0;

        itemListInfo.setVisible(!hasValidItems);
        return validItems;
    }

    protected ZipFile getSpecifiedZipSourceFile() {
        return getSpecifiedZipSourceFile(archivePathField.getText());
    }

    private ZipFile getSpecifiedZipSourceFile(String fileName) {
        if (fileName.length() == 0) {
            return null;
        }

        try {
            return new ZipFile(fileName);
        } catch (ZipException e) {
            // displayErrorDialog(DataTransferMessages.ZipImport_badFormat);
            displayErrorDialog(Messages.getString("DataTransferMessages.ZipImport_badFormat")); //$NON-NLS-1$
        } catch (IOException e) {
            // displayErrorDialog(DataTransferMessages.ZipImport_couldNotRead);
            displayErrorDialog(Messages.getString("DataTransferMessages.ZipImport_couldNotRead")); //$NON-NLS-1$
        }

        archivePathField.setFocus();
        return null;
    }

    protected void displayErrorDialog(String message) {
        MessageDialog.openError(getContainer().getShell(), getErrorDialogTitle(), message);
    }

    protected String getErrorDialogTitle() {
        // return IDEWorkbenchMessages.WizardExportPage_internalErrorTitle;
        return Messages.getString("IDEWorkbenchMessages.WizardExportPage_internalErrorTitle"); //$NON-NLS-1$
    }

    protected TarFile getSpecifiedTarSourceFile() {
        return getSpecifiedTarSourceFile(archivePathField.getText());
    }

    private TarFile getSpecifiedTarSourceFile(String fileName) {
        if (fileName.length() == 0) {
            return null;
        }

        try {
            return new TarFile(fileName);
        } catch (TarException e) {
            // displayErrorDialog(DataTransferMessages.TarImport_badFormat);
            displayErrorDialog(Messages.getString("DataTransferMessages.TarImport_badFormat")); //$NON-NLS-1$
        } catch (IOException e) {
            // displayErrorDialog(DataTransferMessages.ZipImport_couldNotRead);
            displayErrorDialog(Messages.getString("DataTransferMessages.ZipImport_couldNotRead")); //$NON-NLS-1$
        }

        archivePathField.setFocus();
        return null;
    }

    public ItemRecord[] getValidItems() {

        List validItems = new ArrayList();
        for (ItemRecord itemRecord : selectedItems) {
            if (itemRecord.isValid()) {
                validItems.add(itemRecord);

            }
        }
        return (ItemRecord[]) validItems.toArray(new ItemRecord[validItems.size()]);
    }

    private List<ItemRecord> getCheckedElements() {
        // add this if user use filter
        Set checkedElements = new HashSet();
        for (Object obj : filteredCheckboxTree.getCheckedLeafNodes()) {
            checkedElements.add(obj);
        }
        // add this if user does not use filter
        for (Object obj : checkTreeViewer.getCheckedElements()) {
            if (obj instanceof ItemRecord) {
                checkedElements.add(obj);
            }
        }
        // sort the item
        List<ItemRecord> list = new ArrayList<ItemRecord>(checkedElements);
        Collections.sort(list, new Comparator<ItemRecord>() {

            @Override
            public int compare(ItemRecord o1, ItemRecord o2) {
                return TreeBuilder.compare(o1, o2);
            }
        });
        return list;
    }

    /**
     * DOC ycbai Comment method "getHadoopSubnodes".
     *
     * Get hadoop sub item records.
     *
     * @param itemRecords
     * @return
     */
    private Set<ItemRecord> getHadoopSubrecords(List<ItemRecord> itemRecords) {
        Set<ItemRecord> hadoopSubrecords = new HashSet<ItemRecord>();
        for (ItemRecord itemRecord : itemRecords) {
            hadoopSubrecords.addAll(ImportItemUtil.collectHadoopSubrecords(manager, totalItemRecords, itemRecord));
        }

        return hadoopSubrecords;
    }

    public boolean performFinish() {
        final List<ItemRecord> itemRecords = new ArrayList<ItemRecord>();
        final List<ItemRecord> checkedItemRecords = getCheckedElements();
        itemRecords.addAll(checkedItemRecords);
        itemRecords.addAll(getHadoopSubrecords(itemRecords));

        for (ItemRecord itemRecord : itemRecords) {
            Item item = itemRecord.getProperty().getItem();
            if (item instanceof JobletProcessItem) {
                needToRefreshPalette = true;
            }

            IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
            if (item.getState().isLocked()) {
                try {
                    factory.unlock(item);
                } catch (PersistenceException e) {
                    ExceptionHandler.process(e);
                } catch (LoginException e) {
                    ExceptionHandler.process(e);
                }
            }
            ERepositoryStatus status = factory.getStatus(item);
            if (status != null && status == ERepositoryStatus.LOCK_BY_USER) {
                try {
                    factory.unlock(item);
                } catch (PersistenceException e) {
                    ExceptionHandler.process(e);
                } catch (LoginException e) {
                    ExceptionHandler.process(e);
                }
            }
        }
        try {
            IRunnableWithProgress iRunnableWithProgress = new IRunnableWithProgress() {

                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    IPath destinationPath = null;
                    String contentType = "";
                    if (rNode != null && rNode.getType().equals(ENodeType.SIMPLE_FOLDER)) {
                        destinationPath = RepositoryNodeUtilities.getPath(rNode);
                        contentType = rNode.getContentType().name();
                    }

                    repositoryUtil.setErrors(false);
                    repositoryUtil.clear();

                    repositoryUtil.importItemRecords(manager, itemRecords, monitor, overwrite, destinationPath, contentType);
                    if (repositoryUtil.hasErrors()) {
                        throw new InvocationTargetException(new CoreException(new Status(IStatus.ERROR, FrameworkUtil.getBundle(
                                this.getClass()).getSymbolicName(), "Import errors"))); //$NON-NLS-1$
                    }

                }
            };

            new ProgressMonitorDialog(getShell()).run(true, true, iRunnableWithProgress);

        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            if (repositoryUtil.getRoutineExtModulesMap().isEmpty()) {
                if (targetException instanceof CoreException) {
                    MessageDialog.openWarning(getShell(), Messages.getString("ImportItemWizardPage.ImportSelectedItems"), //$NON-NLS-1$
                            Messages.getString("ImportItemWizardPage.ErrorsOccured")); //$NON-NLS-1$
                }
            }

        } catch (InterruptedException e) {
            //
        }
        ResourcesManager curManager = this.manager;
        if (curManager instanceof ProviderManager) {
            curManager.closeResource();
        }

        selectedItems = null;
        itemRecords.clear();
        return true;
    }

    public boolean performCancel() {
        selectedItems = null;
        repositoryUtil.clearAllData();
        if (sourceFile != null) {
            try {
                sourceFile.close();
            } catch (IOException e) {
                //
            }
        }
        if (sourceTarFile != null) {
            try {
                sourceTarFile.close();
            } catch (IOException e) {
                //
            }
        }
        return true;
    }

    /**
     * Getter for needToRefreshPalette.
     *
     * @return the needToRefreshPalette
     */
    public boolean isNeedToRefreshPalette() {
        return this.needToRefreshPalette;
    }

    private void updateFinishStatus() {
        List<ItemRecord> checkedElements = getCheckedElements();
        updateErrorMessage(checkedElements);
        if (checkedElements.isEmpty() || getErrorMessage() != null) {
            this.setPageComplete(false);
        } else {
            this.setPageComplete(true);
        }
    }

    /**
     * Checks for consistency in selected elements and report an error message. in case of error or null the message
     * error.
     *
     * @param checkedElements element to be checked
     */
    private void updateErrorMessage(List<ItemRecord> checkedElements) {
        String errorMessage = checkErrorFor2ItemsWithSameIdAndVersion(checkedElements);
        setErrorMessage(errorMessage);
    }

    /**
     * This check that 2 items in the list do not have the same Id and the same version. if that is so the return an
     * error message else return null.
     *
     * @param checkedElementsn the element to be checked
     * @return an error message or null if no error.
     */
    private String checkErrorFor2ItemsWithSameIdAndVersion(List<ItemRecord> checkedElements) {
        String errorMessage = null;
        HashMap<String, ItemRecord> duplicateCheckMap = new HashMap<String, ItemRecord>();
        for (ItemRecord itRecord : checkedElements) {
            ItemRecord otherRecord = duplicateCheckMap.put(itRecord.getProperty().getId() + itRecord.getProperty().getVersion(),
                    itRecord);
            if (otherRecord != null) {
                errorMessage = Messages.getString("ImportItemWizardPage.0", itRecord.getPath(), otherRecord.getPath()); //$NON-NLS-1$
            }// else keep going
        }
        return errorMessage;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.wizard.WizardPage#isPageComplete()
     */
    @Override
    public boolean isPageComplete() {
        if (selectedItems.isEmpty() || getErrorMessage() != null) {
            return false;
        }
        return super.isPageComplete();
    }

}
