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
package org.talend.repository.ui.actions.importexport;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.IExtensionStateModel;
import org.eclipse.ui.navigator.INavigatorContentService;
import org.talend.commons.CommonsPlugin;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.commons.utils.time.TimeMeasure;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.hadoop.IHadoopClusterService;
import org.talend.core.hadoop.repository.HadoopRepositoryUtil;
import org.talend.core.model.process.ProcessUtils;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Project;
import org.talend.core.model.properties.Property;
import org.talend.core.model.relationship.Relation;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.Folder;
import org.talend.core.model.repository.IExtendedRepositoryNodeHandler;
import org.talend.core.model.repository.IRepositoryReviewFilter;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.RepositoryContentManager;
import org.talend.core.model.repository.RepositoryObject;
import org.talend.core.repository.constants.FileConstants;
import org.talend.core.repository.model.ProjectRepositoryNode;
import org.talend.core.repository.model.repositoryObject.MetadataColumnRepositoryObject;
import org.talend.core.ui.ITestContainerProviderService;
import org.talend.core.ui.advanced.composite.FilteredCheckboxTree;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.repository.ProjectManager;
import org.talend.repository.i18n.Messages;
import org.talend.repository.local.ExportItemUtil;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;
import org.talend.repository.model.nodes.IProjectRepositoryNode;
import org.talend.repository.viewer.ui.provider.INavigatorContentServiceProvider;
import org.talend.repository.viewer.ui.provider.RepoCommonViewerProvider;

/**
 * Initialy copied from org.eclipse.ui.internal.wizards.datatransfer.WizardProjectsImportPage.
 */
public class ExportItemWizardPage extends WizardPage {

    private static final String CODE = "CODE";

    private static final String C_BEAN_REGISTER = "cBeanRegister";

    private static final String BEANS_END_STRING = "();";

    private static final String BEANS_BEGIN_STRING = "new beans.";

    private Button itemFromDirectoryRadio;

    private Text directoryPathField;

    private Button browseDirectoriesButton;

    private Button itemFromArchiveRadio;

    private Text archivePathField;

    private Button browseArchivesButton;

    private String previouslyBrowsedDirectory = ""; //$NON-NLS-1$

    private String previouslyBrowsedArchive = ""; //$NON-NLS-1$

    private static final String[] FILE_EXPORT_MASK = { "*.zip;*.tar;*.tar.gz", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$

    private static final String DESTINATION_FILE = "destinationFile"; //$NON-NLS-1$

    private static final String DIRECTORY_PATH = "directoryPath"; //$NON-NLS-1$

    private static final String ARCHIVE_PATH = "archivePath"; //$NON-NLS-1$

    private String lastPath;

    private IStructuredSelection selection;

    private FilteredCheckboxTree filteredCheckboxTree;

    private Button exportDependencies;

    Set initcheckedNodes = new HashSet();

    Set uncheckedNodes = new HashSet();

    Collection<IRepositoryViewObject> implicitDependences = new ArrayList<IRepositoryViewObject>();

    Set checkedDependency = new HashSet();

    private String baseViewId;

    protected ExportItemWizardPage(String pageName, IStructuredSelection selection, String baseViewId) {
        super(pageName);
        this.selection = selection;
        this.baseViewId = baseViewId;
        setDescription(Messages.getString("ExportItemWizardPage.description")); //$NON-NLS-1$
        // setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_WIZBAN_EXPORT_WIZ));
        setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor("IMG_WIZBAN_EXPORT_WIZ")); //$NON-NLS-1$

        TimeMeasure.display = CommonsPlugin.isDebugMode();
        TimeMeasure.displaySteps = CommonsPlugin.isDebugMode();
        TimeMeasure.measureActive = CommonsPlugin.isDebugMode();
    }

    protected CheckboxTreeViewer getItemsTreeViewer() {
        return filteredCheckboxTree.getViewer();
    }

    @Override
    public void createControl(Composite parent) {

        TimeMeasure.begin(this.getClass().getSimpleName());

        Composite workArea = new Composite(parent, SWT.NONE);
        setControl(workArea);

        workArea.setLayout(new GridLayout());
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));

        createItemRoot(workArea);
        TimeMeasure.step(this.getClass().getSimpleName(), "finished to createItemRoot"); //$NON-NLS-1$

        createItemList(workArea);
        TimeMeasure.step(this.getClass().getSimpleName(), "finished to createItemList"); //$NON-NLS-1$

        TimeMeasure.end(this.getClass().getSimpleName());
        TimeMeasure.display = false;
        TimeMeasure.displaySteps = false;
        TimeMeasure.measureActive = false;
    }

    private String reloadExportPath(String pathType) {
        if (getDialogSettings() != null) {
            IDialogSettings section = getDialogSettings().getSection(DESTINATION_FILE);
            if (section == null) {
                section = getDialogSettings().addNewSection(DESTINATION_FILE);
            }
            return section.get(pathType);
        }
        return null;
    }

    /**
     * DOC hcw Comment method "createItemList".
     *
     * @param workArea
     */
    private void createItemList(Composite workArea) {
        Composite itemComposite = new Composite(workArea, SWT.NONE);
        GridLayout gdlItemComposite = new GridLayout(2, false);
        gdlItemComposite.marginWidth = 0;
        itemComposite.setLayout(gdlItemComposite);

        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).hint(500, 300).applyTo(itemComposite);

        Label label = new Label(itemComposite, SWT.NONE);
        label.setText(Messages.getString("ExportItemWizardPage.labelText")); //$NON-NLS-1$
        GridDataFactory.swtDefaults().span(2, 1).applyTo(label);

        createTreeViewer(itemComposite);
        TimeMeasure.step(this.getClass().getSimpleName(), "finished to createTreeViewer"); //$NON-NLS-1$

        createSelectionButton(itemComposite);
        CheckboxTreeViewer exportItemsTreeViewer = getItemsTreeViewer();

        addTreeCheckedSelection();
        // if user has select some items in repository view, mark them as checked
        checkSelectedElements(exportItemsTreeViewer);
    }

    protected void checkSelectedElements(CheckboxTreeViewer exportItemsTreeViewer) {
        if (selection != null && !selection.isEmpty()) {
            initcheckedNodes.addAll(selection.toList());

            Set<IRepositoryNode> beanDependencies = null;

            List list = selection.toList();
            Object firstFromList = null;

            if (list.size() > 0) {
                firstFromList = list.get(0);

                ERepositoryObjectType firstObjectobjectType = getObjectType(firstFromList);
                expandRoot(firstObjectobjectType);
                expandParent(exportItemsTreeViewer, firstFromList, firstObjectobjectType);

                Object[] elements = selection.toArray();
                Object[] routeElements = getRoutes(elements);
                Map<String, Item> items = new HashMap<String, Item>();
                collectNodes(items, routeElements);

                registerRelatedBeans(items.values());

                beanDependencies = getBeanDependencies(items.values());
            }

            Set nodes = new HashSet();

            for (Object obj : list) {
                ERepositoryObjectType objectType = getObjectType(obj);
                expandRoot(objectType);
                expandParent(exportItemsTreeViewer, obj, objectType);
                checkElement(obj, nodes);
            }

            if (beanDependencies != null) {
                for (Object obj : beanDependencies) {
                    ERepositoryObjectType objectType = getObjectType(obj);
                    expandRoot(objectType);
                    expandParent(exportItemsTreeViewer, obj, objectType);
                    checkElement(obj, nodes);
                }
            }
            TimeMeasure.step(this.getClass().getSimpleName(), "finished to collect nodes"); //$NON-NLS-1$
            exportItemsTreeViewer.setCheckedElements(nodes.toArray());
            TimeMeasure.step(this.getClass().getSimpleName(), "finished to check nodes"); //$NON-NLS-1$
        }
    }

    private Set<IRepositoryNode> getBeanDependencies(Collection<Item> items) {
        Set<Relation> relations = RelationshipItemBuilder.getInstance().getBeanRelations(items);
        Set<String> relationsIds = new HashSet<String>();

        if (relations != null) {
            for (Relation rel : relations) {
                relationsIds.add(rel.getId());
            }
        }

        Set<IRepositoryNode> repositoryNode = getBeansWithIds(relationsIds);

        return repositoryNode;
    }

    private Set<IRepositoryNode> getBeansWithIds(Set<String> ids) {
        RepositoryNode codeRepositoryNode = ProjectRepositoryNode.getInstance().getRootRepositoryNode(
                ERepositoryObjectType.valueOf("BEANS"));
        List<IRepositoryNode> checkedNodesCode = codeRepositoryNode.getChildren();

        Set<IRepositoryNode> repositoryNodes = new HashSet<IRepositoryNode>();
        getBeansWithIdsHelper(checkedNodesCode, ids, repositoryNodes);

        return repositoryNodes;
    }

    private void getBeansWithIdsHelper(List<IRepositoryNode> children, Set<String> beanIds, Set<IRepositoryNode> repositoryNodes) {
        if (children != null) {
            for (Object element : children) {
                if (element instanceof RepositoryNode) {
                    RepositoryNode checkedNode = (RepositoryNode) element;

                    if (checkedNode.getChildren() != null && checkedNode.getChildren().size() > 0) {
                        getBeansWithIdsHelper(checkedNode.getChildren(), beanIds, repositoryNodes);
                    } else {
                        if (checkedNode.getObject() != null &&
                                checkedNode.getObject().getLabel() != null &&
                                ENodeType.REPOSITORY_ELEMENT.equals(checkedNode.getType()) &&
                                beanIds.contains(checkedNode.getId())) {
                            repositoryNodes.add(checkedNode);
                        }
                    }
                }
            }
        }
    }


    private ERepositoryObjectType getObjectType(Object nodeObject) {
        ERepositoryObjectType objectType = null;
        if (nodeObject instanceof RepositoryNode) {
            RepositoryNode node = (RepositoryNode) nodeObject;
            objectType = node.getObjectType();
            // for user folders in metadata , routines , documentation
            if (ERepositoryObjectType.FOLDER.equals(objectType)) {
                RepositoryNode rootNode = getParentNodeNotFolder(node);
                objectType = rootNode.getContentType();
            }
            if (objectType == null) {
                objectType = node.getContentType();
            }
        } else {
            for (IExtendedRepositoryNodeHandler nodeHandler : RepositoryContentManager.getExtendedNodeHandler()) {
                objectType = nodeHandler.getObjectType(nodeObject);
                if (objectType != null) {
                    return objectType;
                }
            }
        }
        return objectType;
    }

    protected boolean selectRepositoryNode(Viewer viewer, RepositoryNode node) {
        if (node == null) {
            return false;
        }
        IRepositoryViewObject object = node.getObject();
        if (object != null) {
            // column
            if (object instanceof MetadataColumnRepositoryObject) {
                return false;
            }
            if (node.getObjectType() == ERepositoryObjectType.METADATA_CON_TABLE) {
                return false;
            }
        }
        // hide the conn folder
        if (object == null && node.getParent() != null && node.getParent().getObject() != null
                && node.getParent().getObjectType() == ERepositoryObjectType.METADATA_CONNECTIONS) {
            return false;
        }
        // hide the generated documentation node, avoid to export .
        final ERepositoryObjectType contentType = node.getContentType();
        if (object == null && contentType != null && contentType.equals(ERepositoryObjectType.GENERATED)) {
            return false;
        }

        return true;
    }

    private void checkElement(Object obj, Set<Object> nodes) {
        if (obj == null) {
            return;
        }
        if (obj instanceof RepositoryNode) {
            Object repositoryNode = null;
            RepositoryNode node = (RepositoryNode) obj;
            ERepositoryObjectType objectType = node.getObjectType();
            Property property = null;
            if (objectType != null) {
                if (objectType == ERepositoryObjectType.METADATA_CON_TABLE
                        || objectType == ERepositoryObjectType.METADATA_CON_VIEW
                        || objectType == ERepositoryObjectType.METADATA_CON_SYNONYM
                        || objectType == ERepositoryObjectType.METADATA_CON_CALCULATION_VIEW
                        || objectType == ERepositoryObjectType.METADATA_CON_QUERY) {
                    if (node.getObject() != null) {
                        property = node.getObject().getProperty();
                    }
                } else if (objectType == ERepositoryObjectType.METADATA_CON_COLUMN) {
                    if (node.getObject() != null && (node.getObject() instanceof MetadataColumnRepositoryObject)) {
                        IRepositoryViewObject viewObj = ((MetadataColumnRepositoryObject) node.getObject()).getViewObject();
                        if (viewObj != null) {
                            property = viewObj.getProperty();
                        }
                    }
                }

            }
            if (property != null) {
                repositoryNode = RepositoryNodeUtilities.getRepositoryNode(property.getId(), false);
                if (repositoryNode != null) {
                    nodes.add(repositoryNode);
                }
            } else {
                nodes.add(node);
            }
        } else {
            nodes.add(obj);
        }
    }

    private void expandParent(TreeViewer viewer, Object nodeObject, ERepositoryObjectType type) {
        if (type == ERepositoryObjectType.SVN_ROOT) {
            viewer.setExpandedState(nodeObject, true);
        }
        Object parent = getParentNode(nodeObject);

        if (parent != null) {
            expandParent(viewer, parent, type);
        }
        if (nodeObject instanceof ProjectRepositoryNode
                || ((nodeObject instanceof RepositoryNode) && ((RepositoryNode) nodeObject).getContentType() == ERepositoryObjectType.REFERENCED_PROJECTS)) {
            viewer.expandToLevel(nodeObject, 3);
        } else {
            viewer.expandToLevel(nodeObject, 1);
        }
    }

    private Object getParentNode(Object nodeObject) {
        for (IExtendedRepositoryNodeHandler nodeHandler : RepositoryContentManager.getExtendedNodeHandler()) {
            Object parent = nodeHandler.getParent(nodeObject);
            if (parent != null) {
                return parent;
            }
        }
        if (nodeObject instanceof RepositoryNode) {
            return ((RepositoryNode) nodeObject).getParent();
        }
        return null;
    }

    // expand root node for node in metadata , routines , documentation
    private void expandRoot(ERepositoryObjectType type) {
        ERepositoryObjectType objectType = type;
        if (objectType != null) {
            if (objectType == ERepositoryObjectType.METADATA_CON_TABLE || objectType == ERepositoryObjectType.METADATA_CON_VIEW
                    || objectType == ERepositoryObjectType.METADATA_CON_SYNONYM
                    || objectType == ERepositoryObjectType.METADATA_CON_CALCULATION_VIEW
                    || objectType == ERepositoryObjectType.METADATA_CON_QUERY
                    || objectType == ERepositoryObjectType.METADATA_CONNECTIONS
                    || objectType == ERepositoryObjectType.METADATA_FILE_DELIMITED
                    || objectType == ERepositoryObjectType.METADATA_FILE_POSITIONAL
                    || objectType == ERepositoryObjectType.METADATA_FILE_REGEXP
                    || objectType == ERepositoryObjectType.METADATA_FILE_XML
                    || objectType == ERepositoryObjectType.METADATA_FILE_LDIF
                    || objectType == ERepositoryObjectType.METADATA_FILE_EXCEL
                    || objectType == ERepositoryObjectType.METADATA_GENERIC_SCHEMA
                    || objectType == ERepositoryObjectType.METADATA_LDAP_SCHEMA
                    || objectType == ERepositoryObjectType.METADATA_SALESFORCE_SCHEMA
                    || objectType == ERepositoryObjectType.METADATA_WSDL_SCHEMA
                    || objectType == ERepositoryObjectType.METADATA_FILE_EBCDIC
                    || objectType == ERepositoryObjectType.METADATA_FILE_HL7
                    || objectType == ERepositoryObjectType.METADATA_MDMCONNECTION
                    || objectType == ERepositoryObjectType.METADATA_FILE_RULES
                    || objectType == ERepositoryObjectType.METADATA_SAPCONNECTIONS
                    || objectType == ERepositoryObjectType.METADATA_SAP_FUNCTION
                    || objectType == ERepositoryObjectType.METADATA_SAP_IDOC
                    || objectType == ERepositoryObjectType.METADATA_HEADER_FOOTER) {
                objectType = ERepositoryObjectType.METADATA;
            } else if (objectType == ERepositoryObjectType.ROUTINES || objectType == ERepositoryObjectType.SNIPPETS) {
                objectType = ERepositoryObjectType.ROUTINES;
            } else if (objectType == ERepositoryObjectType.DOCUMENTATION || objectType == ERepositoryObjectType.JOB_DOC
                    || objectType == ERepositoryObjectType.JOBLET_DOC) {
                objectType = ERepositoryObjectType.DOCUMENTATION;
            }
        }
        if (objectType != null) {
            CheckboxTreeViewer exportItemsTreeViewer = getItemsTreeViewer();
            if (objectType == ERepositoryObjectType.METADATA || objectType == ERepositoryObjectType.ROUTINES
                    || objectType == ERepositoryObjectType.DOCUMENTATION) {
                RepositoryNode rootRepositoryNode = ProjectRepositoryNode.getInstance().getRootRepositoryNode(objectType);
                if (rootRepositoryNode != null) {
                    exportItemsTreeViewer.expandToLevel(rootRepositoryNode, 2);
                }
            }

        }

    }

    private RepositoryNode getParentNodeNotFolder(RepositoryNode node) {
        if (ERepositoryObjectType.FOLDER.equals(node.getObjectType())) {
            return getParentNodeNotFolder(node.getParent());
        } else {
            return node;
        }
    }

    private void addTreeCheckedSelection() {
        final CheckboxTreeViewer exportItemsTreeViewer = getItemsTreeViewer();
        exportItemsTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                // refreshExportDependNodes();
            }

        });
        exportItemsTreeViewer.addCheckStateListener(new ICheckStateListener() {

            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {

                ArrayList elementList = new ArrayList();
                elementList.add(event.getElement());
                Object[] elements = elementList.toArray();
                Object[] routeElements = getRoutes(elements);
                Map<String, Item> items = new HashMap<String, Item>();
                collectNodes(items, routeElements);

                Set<IRepositoryNode> beanDependencies = getBeanDependencies(items.values());

                Set toselect = new HashSet();
                if (event.getChecked()) {
                    initcheckedNodes.add(event.getElement());

                    checkedDependency.addAll(beanDependencies);

                    if (beanDependencies != null) {
                        for (Object obj : beanDependencies) {
                            ERepositoryObjectType objectType = getObjectType(obj);
                            expandRoot(objectType);
                            expandParent(exportItemsTreeViewer, obj, objectType);
                            checkElement(obj, toselect);
                        }
                    }
                    uncheckedNodes.removeAll(beanDependencies);
                    for (Object repNode : toselect) {
                        exportItemsTreeViewer.setChecked(repNode, true);
                    }

                    // remove children and parent from uncheckednodes
                    TreeItem treeItem = getTreeItem(exportItemsTreeViewer.getTree().getItems(), event.getElement());
                    Set subItems = collectSubData(treeItem);
                    Set parent = collectParentData(treeItem);
                    uncheckedNodes.removeAll(subItems);
                    uncheckedNodes.removeAll(parent);
                } else {
                    uncheckedNodes.add(event.getElement());
                    // remove children from initcheckedNodes
                    TreeItem treeItem = getTreeItem(exportItemsTreeViewer.getTree().getItems(), event.getElement());
                    Set subItems = collectSubData(treeItem);
                    initcheckedNodes.removeAll(subItems);

                    if (beanDependencies != null) {
                        for (Object obj : beanDependencies) {
                            ERepositoryObjectType objectType = getObjectType(obj);
                            checkElement(obj, toselect);
                        }
                    }
                    for (Object repNode : toselect) {
                        exportItemsTreeViewer.setChecked(repNode, false);
                    }
                    uncheckedNodes.addAll(beanDependencies);
                }
            }
        });
    }

    private Set collectParentData(TreeItem treeItem) {
        Set parentItems = new HashSet();
        if (treeItem != null) {
            parentItems.add(treeItem.getData());
            parentItems.addAll(collectParentData(treeItem.getParentItem()));
        }
        return parentItems;
    }

    private Set collectSubData(TreeItem treeItem) {
        Set subItems = new HashSet();
        if (treeItem != null) {
            subItems.add(treeItem.getData());
            for (TreeItem subItem : treeItem.getItems()) {
                subItems.addAll(collectSubData(subItem));
            }
        }

        return subItems;
    }

    private TreeItem getTreeItem(TreeItem[] treeItems, Object objectToFind) {
        for (TreeItem currentChild : treeItems) {
            if (objectToFind.equals(currentChild.getData())) {
                return currentChild;
            }
            TreeItem toReturn = getTreeItem(currentChild.getItems(), objectToFind);
            if (toReturn != null) {
                return toReturn;
            }
        }
        return null;
    }

    private void createTreeViewer(Composite itemComposite) {
        filteredCheckboxTree = new FilteredCheckboxTree(itemComposite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI) {

            @Override
            protected CheckboxTreeViewer doCreateTreeViewer(Composite parent, int style) {
                RepoCommonViewerProvider provider = RepoCommonViewerProvider.CHECKBOX;
                if (baseViewId != null) {
                    provider.setViewId(baseViewId);
                }
                CheckboxTreeViewer viwer = (CheckboxTreeViewer) provider.createViewer(parent);

                // FIXME this is temp fix for 5.4.2
                INavigatorContentService navigatorContentService = ((INavigatorContentServiceProvider) viwer)
                        .getNavigatorContentService();
                if (navigatorContentService != null) {
                    IExtensionStateModel findStateModel = navigatorContentService
                            .findStateModel("com.oaklandsw.transform.navigatorContent");//$NON-NLS-1$
                    if (findStateModel != null) {
                        findStateModel.setBooleanProperty("org.talend.repository.ui.actions.importexport", true);//$NON-NLS-1$
                    }
                }

                return viwer;

            }

            @Override
            protected void refreshCompleted() {
                getViewer().expandToLevel(3);
                restoreCheckedElements();
            }

            @Override
            protected boolean isNodeCollectable(TreeItem item) {
                Object obj = item.getData();
                if (obj instanceof RepositoryNode) {
                    RepositoryNode node = (RepositoryNode) obj;
                    if (node.getObjectType() == ERepositoryObjectType.METADATA_CONNECTIONS) {
                        return true;
                    }
                }
                return false;
            }
        };
        filteredCheckboxTree.getViewer().addFilter(new ViewerFilter() {

            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                boolean select = true;
                if (element instanceof RepositoryNode) {
                    select = selectRepositoryNode(viewer, (RepositoryNode) element);
                }
                if (select) {
                    for (IRepositoryReviewFilter nodeHandler : RepositoryContentManager.getRepositoryReviewFilters()) {
                        boolean exportFilter = nodeHandler.filter(viewer, parentElement, element, "repository_review");//$NON-NLS-1$
                        if (!exportFilter) {
                            return exportFilter;
                        }

                    }
                }
                return select;
            }
        });
    }

    /**
     * DOC hcw Comment method "createSelectionButton".
     *
     * @param itemComposite
     */
    private void createSelectionButton(Composite itemComposite) {
        Composite buttonComposite = new Composite(itemComposite, SWT.NONE);
        GridLayout gdlButtonComposite = new GridLayout();
        gdlButtonComposite.marginHeight = 0;
        gdlButtonComposite.marginWidth = 0;
        gdlButtonComposite.marginTop = 26;
        buttonComposite.setLayout(gdlButtonComposite);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(buttonComposite);

        Button selectAll = new Button(buttonComposite, SWT.PUSH);
        // selectAll.setText(DataTransferMessages.DataTransfer_selectAll);
        selectAll.setText(Messages.getString("DataTransferMessages.DataTransfer_selectAll")); //$NON-NLS-1$
        selectAll.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                CheckboxTreeViewer exportItemsTreeViewer = getItemsTreeViewer();
                exportItemsTreeViewer.setAllChecked(true);
            }
        });

        setButtonLayoutData(selectAll);

        Button deselectAll = new Button(buttonComposite, SWT.PUSH);
        // deselectAll.setText(DataTransferMessages.DataTransfer_deselectAll);
        deselectAll.setText(Messages.getString("DataTransferMessages.DataTransfer_deselectAll")); //$NON-NLS-1$
        deselectAll.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                CheckboxTreeViewer exportItemsTreeViewer = getItemsTreeViewer();
                exportItemsTreeViewer.setAllChecked(false);
            }
        });

        setButtonLayoutData(deselectAll);

        Button expandBtn = new Button(buttonComposite, SWT.PUSH);
        expandBtn.setText(Messages.getString("ExportItemWizardPage.expandBtnText")); //$NON-NLS-1$
        expandBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                CheckboxTreeViewer exportItemsTreeViewer = getItemsTreeViewer();
                exportItemsTreeViewer.expandAll();
            }
        });
        setButtonLayoutData(expandBtn);

        Button collapseBtn = new Button(buttonComposite, SWT.PUSH);
        collapseBtn.setText(Messages.getString("ExportItemWizardPage.collapseBtnText")); //$NON-NLS-1$
        collapseBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                CheckboxTreeViewer exportItemsTreeViewer = getItemsTreeViewer();
                exportItemsTreeViewer.collapseAll();
            }
        });
        setButtonLayoutData(collapseBtn);
    }

    @SuppressWarnings("restriction")
    private void createItemRoot(Composite workArea) {
        Composite projectGroup = new Composite(workArea, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        layout.makeColumnsEqualWidth = false;
        layout.marginWidth = 0;
        projectGroup.setLayout(layout);
        projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        itemFromDirectoryRadio = new Button(projectGroup, SWT.RADIO);
        // itemFromDirectoryRadio.setText(DataTransferMessages.WizardProjectsImportPage_RootSelectTitle);
        itemFromDirectoryRadio.setText(Messages.getString("DataTransferMessages.WizardProjectsImportPage_RootSelectTitle")); //$NON-NLS-1$

        this.directoryPathField = new Text(projectGroup, SWT.BORDER);

        String arcFileName = null;
        if (selection != null && selection.getFirstElement() instanceof RepositoryNode) {
            RepositoryNode node = (RepositoryNode) selection.getFirstElement();
            if (node.getObject() == null) {
                arcFileName = node.getLabel();
            } else {
                arcFileName = node.getObject().getLabel();
            }
        }
        this.directoryPathField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
        String exportDirPath = reloadExportPath(DIRECTORY_PATH);
        if (exportDirPath != null) {
            if (arcFileName != null) {
                if (new Path(exportDirPath).lastSegment() != null && !new Path(exportDirPath).lastSegment().equals(arcFileName)) {
                    exportDirPath = exportDirPath + File.separator + arcFileName;
                }
            }
            this.directoryPathField.setText(exportDirPath);
            this.lastPath = exportDirPath;
        }

        browseDirectoriesButton = new Button(projectGroup, SWT.PUSH);
        // browseDirectoriesButton.setText(DataTransferMessages.DataTransfer_browse);
        browseDirectoriesButton.setText(Messages.getString("DataTransferMessages.DataTransfer_browse")); //$NON-NLS-1$
        setButtonLayoutData(browseDirectoriesButton);

        // new project from archive radio button
        itemFromArchiveRadio = new Button(projectGroup, SWT.RADIO);
        // itemFromArchiveRadio.setText(DataTransferMessages.WizardProjectsImportPage_ArchiveSelectTitle);
        itemFromArchiveRadio.setText(Messages.getString("DataTransferMessages.WizardProjectsImportPage_ArchiveSelectTitle")); //$NON-NLS-1$

        // project location entry field
        archivePathField = new Text(projectGroup, SWT.BORDER);

        archivePathField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));

        if (arcFileName != null) {
            String exportArchivePath = reloadExportPath(ARCHIVE_PATH);
            // when first open the exportItem dialog ,the exportPath maybe empty,need judge
            if (exportArchivePath != null && exportArchivePath.trim().length() > 0) {
                File beforeExportArchiveFolder = new File(exportArchivePath).getParentFile();
                /*
                 * TDI-22791, when the exportArchivePath is only name without path. No need check the existed or not.
                 * Because will be create the parent folders, when export item.
                 */
                if (beforeExportArchiveFolder != null/* && beforeExportArchiveFolder.exists() */) {
                    String newPath = new File(beforeExportArchiveFolder, arcFileName + FileConstants.ZIP_FILE_SUFFIX)
                            .getAbsolutePath();
                    this.archivePathField.setText(newPath);
                }
            } else if (exportDirPath != null && exportDirPath.trim().length() > 0) {
                String newPath = exportDirPath + File.separator + arcFileName + FileConstants.ZIP_FILE_SUFFIX;
                this.archivePathField.setText(newPath);
            }
        }

        // browse button
        browseArchivesButton = new Button(projectGroup, SWT.PUSH);
        // browseArchivesButton.setText(DataTransferMessages.DataTransfer_browse);
        browseArchivesButton.setText(Messages.getString("DataTransferMessages.DataTransfer_browse")); //$NON-NLS-1$
        setButtonLayoutData(browseArchivesButton);

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

        directoryPathField.addTraverseListener(new TraverseListener() {

            @Override
            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_RETURN) {
                    e.doit = false;
                    lastPath = directoryPathField.getText().trim();
                }
            }

        });

        directoryPathField.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(org.eclipse.swt.events.FocusEvent e) {
                lastPath = directoryPathField.getText().trim();
            }

        });

        directoryPathField.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                String exportPath = resetExportPath(directoryPathField.getText());
                saveExportPath(DIRECTORY_PATH, exportPath);
            }

        });

        archivePathField.addTraverseListener(new TraverseListener() {

            @Override
            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_RETURN) {
                    e.doit = false;
                    lastPath = archivePathField.getText().trim();
                }
            }
        });

        archivePathField.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(org.eclipse.swt.events.FocusEvent e) {
                lastPath = archivePathField.getText().trim();
            }
        });

        archivePathField.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                saveExportPath(ARCHIVE_PATH, archivePathField.getText());
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

        exportDependencies = new Button(workArea, SWT.CHECK);
        exportDependencies.setText(Messages.getString("ExportItemWizardPage.exportDependenciesText")); //$NON-NLS-1$
        exportDependencies.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                checkedDependency.clear();
                implicitDependences.clear();
                CheckboxTreeViewer exportItemsTreeViewer = getItemsTreeViewer();
                Set allNode = new HashSet();
                Set<IRepositoryNode> beanDependencies = getBeanDependencies(getSelectedRouteItems());
                if (exportDependencies.getSelection()) {
                    refreshExportDependNodes();
                    exportDependenciesSelected();
                    checkedDependency.addAll(beanDependencies);
                    allNode.addAll(checkedDependency);
                    allNode.addAll(implicitDependences);
                } else {
                    allNode.addAll(initcheckedNodes);
                }
                Set toselect = new HashSet();
                for (Object obj : allNode) {
                    ERepositoryObjectType objectType = getObjectType(obj);
                    if (exportDependencies.getSelection()) {
                        expandRoot(objectType);
                        expandParent(exportItemsTreeViewer, obj, objectType);
                    }
                    checkElement(obj, toselect);
                }
                exportItemsTreeViewer.setCheckedElements(toselect.toArray());
                if (!exportDependencies.getSelection()) {
                    for (Object unchecked : uncheckedNodes) {
                        exportItemsTreeViewer.setChecked(unchecked, false);
                    }
                }
            }
        });
    }

    private String resetExportPath(String exportPath) {
        if (getSelectedItems() == null || getSelectedItems().isEmpty()) {
            return exportPath;
        }
        for (Item item : getSelectedItems()) {
            if (item.getProperty() == null) {
                continue;
            }
            String label = item.getProperty().getLabel();
            IPath path = new Path(exportPath);
            if (label.equals(path.lastSegment())) {
                return path.removeLastSegments(1).toOSString();
            }
        }
        return exportPath;
    }

    private void refreshExportDependNodes() {

        CheckboxTreeViewer exportItemsTreeViewer = getItemsTreeViewer();
        Object[] checkedObj = exportItemsTreeViewer.getCheckedElements();
        for (Object element : checkedObj) {
            if (element instanceof RepositoryNode) {
                RepositoryNode checkedNode = (RepositoryNode) element;
                if (checkedNode != null && !RepositoryNode.NO_ID.equals(checkedNode.getId())) {
                    if (ENodeType.REPOSITORY_ELEMENT.equals(checkedNode.getType())) {
                        checkedDependency.add(checkedNode);
                    }
                }
            }
        }
    }

    private Set<String> getCBeanRegisterComponentDependency(Item selectedItems) {
        Set<String> result = new HashSet<String>();

        ProcessItem prItem = (ProcessItem)selectedItems;
        result.addAll(getBeanDependenciesFromCodeSection(prItem.getProcess()));

        return result;
    }

    private Set<String> getBeanDependenciesFromCodeSection(ProcessType process) {
        Set<String> classNames = new HashSet<>();
        for (Object node : process.getNode()) {
            NodeType nodeType = (NodeType)node;
            if (nodeType.getComponentName().equals(C_BEAN_REGISTER)) {
                for (Object elemParam : nodeType.getElementParameter()) {
                    ElementParameterType ept = (ElementParameterType)elemParam;
                    if (ept.getName().equals(CODE)) {
                        String codeValue = ept.getValue();

                        BufferedReader bufReader = new BufferedReader(new StringReader(codeValue));

                        String line=null;
                        try {
                            while( (line=bufReader.readLine()) != null )
                            {
                                String className = StringUtils.substringBetween(line, BEANS_BEGIN_STRING, BEANS_END_STRING);
                                if (StringUtils.isNotBlank(className)) {
                                    classNames.add(className);
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return classNames;
    }

    private void registerRelatedBeans(Collection<Item> items) {
        RepositoryNode codeRepositoryNode = ProjectRepositoryNode.getInstance().getRootRepositoryNode(
                ERepositoryObjectType.valueOf("BEANS"));
        List<IRepositoryNode> checkedNodesBeans = codeRepositoryNode.getChildren();

        for (Item item : items) {
            Set<String> beanNames = getCBeanRegisterComponentDependency(item);

            if (beanNames != null && beanNames.size() > 0) {
                Set<IRepositoryNode> beanDependencies = new HashSet<IRepositoryNode>();
                findBeanObjectByNamesFromRoot(checkedNodesBeans, beanNames, beanDependencies);

                for (IRepositoryNode node : beanDependencies) {
                    RelationshipItemBuilder instance = RelationshipItemBuilder.getInstance();

                    instance.addRelationShip(item, node.getId(), node.getObject().getVersion(), node.getContentType().toString());
                }
            }
        }
    }

    private void findBeanObjectByNamesFromRoot(List<IRepositoryNode> children, Set<String> beanDependenciesNames, Set<IRepositoryNode> beanDependencies) {
        if (children != null) {
            for (Object element : children) {
                if (element instanceof RepositoryNode) {
                    RepositoryNode checkedNode = (RepositoryNode) element;

                    if (checkedNode.getChildren() != null && checkedNode.getChildren().size() > 0) {
                        findBeanObjectByNamesFromRoot(checkedNode.getChildren(), beanDependenciesNames, beanDependencies);
                    } else {
                        if (checkedNode.getObject() != null &&
                                checkedNode.getObject().getLabel() != null &&
                                beanDependenciesNames.contains(checkedNode.getObject().getLabel()) &&
                                ENodeType.REPOSITORY_ELEMENT.equals(checkedNode.getType())) {
                            beanDependencies.add(checkedNode);
                        }
                    }
                }
            }
        }
    }

    /**
     * DOC qwei Comment method "exportDependenciesSelected".
     */
    private void exportDependenciesSelected() {
        final Collection<Item> selectedItems = getSelectedItems();

        IRunnableWithProgress runnable = new IRunnableWithProgress() {

            @Override
            public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                monitor.beginTask("Dependencies", 100);//$NON-NLS-1$

                //
                final List<IRepositoryViewObject> repositoryObjects = new ArrayList<IRepositoryViewObject>();

                ProcessUtils.clearFakeProcesses();
                RepositoryNodeUtilities.checkItemDependencies(selectedItems, repositoryObjects, false, true);
                monitor.worked(60);
                for (IRepositoryViewObject repositoryObject : repositoryObjects) {
                    RepositoryNode repositoryNode = RepositoryNodeUtilities.getRepositoryNode(repositoryObject, monitor);
                    if (repositoryNode != null) {
                        checkedDependency.add(repositoryNode);
                    } else {
                        implicitDependences.add(repositoryObject);
                    }
                    // check relateion ship for job -->map -->structure
                    for (IExtendedRepositoryNodeHandler nodeHandler : RepositoryContentManager.getExtendedNodeHandler()) {
                        List nodesAndDependencies = nodeHandler.getRepositoryNodeAndDependencies(repositoryObject);
                        if (!nodesAndDependencies.isEmpty()) {
                            checkedDependency.addAll(nodesAndDependencies);
                        }
                    }
                }

                // check relateion ship for map -->structure
                for (Item item : selectedItems) {
                    for (IExtendedRepositoryNodeHandler nodeHandler : RepositoryContentManager.getExtendedNodeHandler()) {
                        List nodesAndDependencies = nodeHandler.getRepositoryNodeAndDependencies(new RepositoryObject(item
                                .getProperty()));
                        if (!nodesAndDependencies.isEmpty()) {
                            checkedDependency.addAll(nodesAndDependencies);
                        }
                    }
                }

                monitor.worked(90);
                ProcessUtils.clearFakeProcesses();
                monitor.done();
            }

        };
        // final ProgressMonitorJobsDialog dialog = new ProgressMonitorJobsDialog(getShell());
        final ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
        try {
            dialog.run(true, true, runnable);
        } catch (InvocationTargetException e) {
            //
        } catch (InterruptedException e) {
            //
        }

    }

    private void archiveRadioSelected() {
        if (itemFromArchiveRadio.getSelection()) {
            directoryPathField.setEnabled(false);
            browseDirectoriesButton.setEnabled(false);
            archivePathField.setEnabled(true);
            browseArchivesButton.setEnabled(true);
            lastPath = archivePathField.getText().trim();
            archivePathField.setFocus();
        }
    }

    protected void handleLocationDirectoryButtonPressed() {

        DirectoryDialog dialog = new DirectoryDialog(directoryPathField.getShell());
        // dialog.setMessage(DataTransferMessages.FileExport_selectDestinationTitle);
        dialog.setMessage(Messages.getString("DataTransferMessages.FileExport_selectDestinationTitle")); //$NON-NLS-1$

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
            lastPath = directoryPathField.getText().trim();
            String exportPath = resetExportPath(lastPath);
            saveExportPath(DIRECTORY_PATH, exportPath);

        }

    }

    /**
     * The browse button has been selected. Select the location.
     */
    protected void handleLocationArchiveButtonPressed() {

        FileDialog dialog = new FileDialog(archivePathField.getShell(), SWT.SAVE);
        dialog.setFilterExtensions(FILE_EXPORT_MASK);
        // dialog.setText(DataTransferMessages.ArchiveExport_selectDestinationTitle);
        dialog.setText(Messages.getString("DataTransferMessages.ArchiveExport_selectDestinationTitle")); //$NON-NLS-1$

        String fileName = archivePathField.getText().trim();
        if (fileName.length() == 0) {
            fileName = previouslyBrowsedArchive;
        }

        if (fileName.length() == 0) {
            // dialog.setFilterPath(IDEWorkbenchPlugin.getPluginWorkspace().getRoot().getLocation().toOSString());
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
            lastPath = archivePathField.getText().trim();
            saveExportPath(ARCHIVE_PATH, lastPath);

        }

    }

    private void saveExportPath(String pathType, String path) {
        if (getDialogSettings() != null) {
            IDialogSettings section = getDialogSettings().getSection(DESTINATION_FILE);
            if (section == null) {
                section = getDialogSettings().addNewSection(DESTINATION_FILE);
            }
            if (section != null) {
                section.put(pathType, path);
            }
        }
    }

    private void directoryRadioSelected() {
        if (itemFromDirectoryRadio.getSelection()) {
            directoryPathField.setEnabled(true);
            browseDirectoriesButton.setEnabled(true);
            archivePathField.setEnabled(false);
            browseArchivesButton.setEnabled(false);
            lastPath = directoryPathField.getText().trim();
            directoryPathField.setFocus();
        }
    }

    private void deleteFile(File file) {
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            } else if (file.isDirectory()) {
                File files[] = file.listFiles();
                for (File file2 : files) {
                    this.deleteFile(file2);
                }
            }
            file.delete();
        }
    }

    public boolean performFinish() {
        if (!checkExportFile()) {
            return false;
        }
        ProjectManager pManager = ProjectManager.getInstance();
        Project project = pManager.getCurrentProject().getEmfProject();
        String projectPath = lastPath + "\\" + project.getTechnicalLabel(); //$NON-NLS-1$
        if ((itemFromDirectoryRadio.getSelection() && new File(projectPath).exists())
                || (itemFromArchiveRadio.getSelection() && new File(archivePathField.getText()).exists())) {
            File oldFile = new File(projectPath).exists() ? new File(projectPath) : new File(archivePathField.getText());
            if (MessageDialogWithToggle
                    .openConfirm(
                            null,
                            Messages.getString("ExportItemWizardPage.waring"), Messages.getString("ExportItemWizardPage.fileAlreadyExist"))) { //$NON-NLS-1$ //$NON-NLS-2$
                deleteFile(oldFile);
            } else {
                return false;
            }
        }
        Collection<Item> selectedItems = getSelectedItems();
        if (exportDependencies.getSelection() && !implicitDependences.isEmpty()) {
            for (IRepositoryViewObject object : implicitDependences) {
                selectedItems.add(object.getProperty().getItem());
            }
        }
        List<String> folders = new ArrayList<String>();
        for (Object obj : filteredCheckboxTree.getViewer().getCheckedElements()) {
            if (obj instanceof RepositoryNode) {
                RepositoryNode repositoryNode = (RepositoryNode) obj;
                if (repositoryNode.getType() == ENodeType.SIMPLE_FOLDER && repositoryNode.getObject() instanceof Folder) {
                    Folder folder = (Folder) repositoryNode.getObject();
                    ERepositoryObjectType contentType = repositoryNode.getContentType();
                    IProjectRepositoryNode root = repositoryNode.getRoot();
                    if (root != null && root.getProject() != null) {
                        IPath path = new Path(root.getProject().getTechnicalLabel());
                        path = path.append(contentType.getFolder());
                        String folderPath = folder.getPath();
                        if (folderPath != null && !"".equals(folderPath)) {
                            path = path.append(folderPath);
                        }
                        path = path.append(folder.getLabel());
                        folders.add(path.toPortableString() + "/");
                    }
                }
            }
        }

        try {
            ExportItemUtil exportItemUtil = new ExportItemUtil();
            if (itemFromArchiveRadio.getSelection()) {
                if (lastPath != null && !lastPath.endsWith(FileConstants.TAR_FILE_SUFFIX)
                        && !lastPath.endsWith(FileConstants.TAR_GZ_FILE_SUFFIX)
                        && !lastPath.endsWith(FileConstants.ZIP_FILE_SUFFIX)) {
                    // set zip as default
                    lastPath = lastPath + FileConstants.ZIP_FILE_SUFFIX;
                }
            }

            // MOD sgandon 31/03/2010 bug 12229: moved getAllVersion into ExportItemUtil.exportitems() method.
            exportItemUtil.exportItems(new File(lastPath), selectedItems, folders, true, new NullProgressMonitor());

        } catch (Exception e) {
            MessageBoxExceptionHandler.process(e);
        }
        return true;
    }

    private boolean checkExportFile() {
        if (lastPath == null || "".equals(lastPath.trim())) {//$NON-NLS-1$
            MessageDialog.openError(getShell(), "Error", "Must input the export path or archive file.");//$NON-NLS-1$//$NON-NLS-2$
            return false;
        }
        return true;
    }

    private static boolean isRepositoryFolder(RepositoryNode node) {
        final ENodeType type = node.getType();
        if (type == ENodeType.SIMPLE_FOLDER || type == ENodeType.STABLE_SYSTEM_FOLDER || type == ENodeType.SYSTEM_FOLDER) {
            return true;
        }
        return false;
    }

    /**
     * Get all selected items to export.
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    private Collection<Item> getSelectedItems() {
        Set checkedElements = getCheckedItems();

        Object[] elements = checkedElements.toArray();

        Map<String, Item> items = new HashMap<String, Item>();
        collectNodes(items, elements);
        return new ArrayList<Item>(items.values());
    }

    @SuppressWarnings("unchecked")
    private Collection<Item> getSelectedRouteItems() {
        Set checkedElements = getCheckedItems();

        Object[] elements = checkedElements.toArray();
        Object[] routeElements = getRoutes(elements);

        Map<String, Item> items = new HashMap<String, Item>();
        collectNodes(items, routeElements);
        return new ArrayList<Item>(items.values());
    }

    private Set getCheckedItems() {
        // add this if user use filter
        Set checkedElements = new HashSet();
        for (Object obj : filteredCheckboxTree.getCheckedLeafNodes()) {
            // if (obj instanceof RepositoryNode) {
            checkedElements.add(obj);
            // }
        }

        // add this if user does not use filter
        for (Object obj : filteredCheckboxTree.getViewer().getCheckedElements()) {
            if (obj instanceof RepositoryNode) {
                RepositoryNode repositoryNode = (RepositoryNode) obj;
                if (!isRepositoryFolder(repositoryNode) && !(repositoryNode instanceof ProjectRepositoryNode)) {
                    checkedElements.add(obj);
                }
            } else {
                // for transform items
                checkedElements.add(obj);
            }
        }
        return checkedElements;
    }

    private Object[] getRoutes(Object[] elements) {

        ArrayList<Object> result = new ArrayList(elements != null ? elements.length : 0);
        for (Object object : elements) {
            if (object instanceof RepositoryNode) {
                RepositoryNode repositoryNode = (RepositoryNode) object;
                if (repositoryNode.getObjectType() != null &&
                        repositoryNode.getObjectType().equals(ERepositoryObjectType.PROCESS_ROUTE)) {
                    result.add(object);
                }
            }
        }

        return result.toArray();
    }

    private void collectNodes(Map<String, Item> items, Object[] objects) {
        for (Object object : objects) {
            if (object instanceof RepositoryNode) {
                RepositoryNode repositoryNode = (RepositoryNode) object;
                IRepositoryViewObject repositoryObject = repositoryNode.getObject();
                if (repositoryObject != null) {
                    if (repositoryObject.getRepositoryObjectType().isResourceItem()) {
                        Item item = repositoryObject.getProperty().getItem();
                        items.put(item.getProperty().getId(), item);
                    }
                } else {
                    if (repositoryNode.getParent() != null && repositoryNode.getParent().getObject() != null) {
                        Item item = repositoryNode.getParent().getObject().getProperty().getItem();
                        items.put(item.getProperty().getId(), item);
                    }
                }
                if (filteredCheckboxTree != null && !isHadoopClusterNode(repositoryNode)) {
                    IContentProvider contentProvider = filteredCheckboxTree.getViewer().getContentProvider();
                    if (contentProvider instanceof ITreeContentProvider) {
                        // only check childrens of allowed items in this viewer
                        if (selectRepositoryNode(getItemsTreeViewer(), repositoryNode)) {
                            Object[] children = ((ITreeContentProvider) contentProvider).getChildren(repositoryNode);
                            List<Object> childrenNodes = getUnTestCaseChildren(children);
                            collectNodes(items, childrenNodes.toArray());
                        }
                    }
                }
            } else {
                for (IExtendedRepositoryNodeHandler nodeHandler : RepositoryContentManager.getExtendedNodeHandler()) {
                    Property property = nodeHandler.getProperty(object);
                    if (property != null) {
                        items.put(property.getId(), property.getItem());
                    }
                }
            }
        }
    }

    private List<Object> getUnTestCaseChildren(Object[] children) {
        ITestContainerProviderService testContainerService = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(ITestContainerProviderService.class)) {
            testContainerService = (ITestContainerProviderService) GlobalServiceRegister.getDefault().getService(
                    ITestContainerProviderService.class);
        }
        List<Object> childrenNodes = new ArrayList<Object>();
        for (Object obj : children) {
            if ((obj instanceof RepositoryNode) && testContainerService != null) {
                boolean isTestCase = testContainerService.isTestContainerType(((RepositoryNode) obj).getObjectType());
                if (!isTestCase) {
                    childrenNodes.add(obj);
                }
            } else {
                childrenNodes.add(obj);
            }
        }
        return childrenNodes;
    }

    // private void collectNodes(Map<String, Item> items, RepositoryNode repositoryNode) {
    // IRepositoryViewObject repositoryObject = repositoryNode.getObject();
    // if (repositoryObject != null) {
    // if (repositoryObject.getRepositoryObjectType().isResourceItem()) {
    // Item item = repositoryObject.getProperty().getItem();
    // items.put(item.getProperty().getId(), item);
    // }
    // } else {
    // if (repositoryNode.getParent() != null && repositoryNode.getParent().getObject() != null) {
    // Item item = repositoryNode.getParent().getObject().getProperty().getItem();
    // items.put(item.getProperty().getId(), item);
    // }
    // }
    // if (filteredCheckboxTree != null && !isHadoopClusterNode(repositoryNode)) {
    // IContentProvider contentProvider = filteredCheckboxTree.getViewer().getContentProvider();
    // if (contentProvider instanceof ITreeContentProvider) {
    // Object[] children = ((ITreeContentProvider) contentProvider).getChildren(repositoryNode);
    // collectNodes(items, children);
    // }
    // }
    //
    // }

    public boolean performCancel() {
        return true;
    }

    private boolean isHadoopClusterNode(IRepositoryNode repositoryNode) {
        IHadoopClusterService hadoopClusterService = HadoopRepositoryUtil.getHadoopClusterService();
        if (hadoopClusterService != null) {
            return hadoopClusterService.isHadoopClusterNode(repositoryNode);
        }

        return false;
    }

}
