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
package org.talend.repository.ui.dialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.Folder;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProjectRepositoryNode;
import org.talend.core.ui.advanced.composite.FilteredCheckboxTree;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.viewer.ui.provider.RepoCommonViewerProvider;

/**
 * DOC yhch class global comment. Detailled comment <br/>
 *
 */
public class UseDynamicJobSelectionDialog extends Dialog {

    private FilteredCheckboxTree filteredCheckboxTree;

    private String[] needCheckedjobs;

    private List<RepositoryNode> repositoryNodes = new ArrayList<RepositoryNode>();

    private String curProcessId;

    public UseDynamicJobSelectionDialog(IShellProvider parentShell) {
        super(parentShell);
    }

    public UseDynamicJobSelectionDialog(Shell parentShell, ERepositoryObjectType type, String curProcessId,
            boolean isSelectUseDynamic) {
        super(parentShell);
        setShellStyle(SWT.SHELL_TRIM | SWT.APPLICATION_MODAL | getDefaultOrientation());
        this.curProcessId = curProcessId;
    }

    public String[] getNeedCheckedjobs() {
        return needCheckedjobs;
    }

    public void setNeedCheckedjobs(String[] needCheckedjobs) {
        this.needCheckedjobs = needCheckedjobs;
    }

    /**
     * Configures the shell
     *
     * @param shell the shell
     */
    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(Messages.getString("UseDynamicJobSelectionDialog.selectJobShellText"));
    }

    private CheckboxTreeViewer getItemsTreeViewer() {
        return filteredCheckboxTree.getViewer();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        GridLayoutFactory.swtDefaults().numColumns(2).applyTo(container);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).hint(500, 400).applyTo(container);

        Label label = new Label(container, SWT.NONE);
        label.setText(Messages.getString("UseDynamicJobSelectionDialog.selectJob")); //$NON-NLS-1$
        GridDataFactory.swtDefaults().span(2, 1).applyTo(label);

        createTreeViewer(container);
        createSelectionButton(container);

        setCheckingNodes();
        return container;
    }

    private void setCheckingNodes() {
        final IContentProvider contentProvider = getItemsTreeViewer().getContentProvider();
        final Object input = getItemsTreeViewer().getInput();
        if (contentProvider instanceof ITreeContentProvider && input instanceof ProjectRepositoryNode
                && getNeedCheckedjobs() != null && getNeedCheckedjobs().length > 0) {
            ITreeContentProvider cnfContentProvider = (ITreeContentProvider) contentProvider;

            List<IRepositoryNode> checkingNodes = new ArrayList<IRepositoryNode>();
            List<String> needCheckedJobIds = new ArrayList(Arrays.asList(getNeedCheckedjobs()));

            final ProjectRepositoryNode root = (ProjectRepositoryNode) input;
            final RepositoryNode rootRepositoryNode = root.getRootRepositoryNode(getSupportType());
            if (rootRepositoryNode == null) {
                return;
            }
            withReferenceProjects(cnfContentProvider, rootRepositoryNode, needCheckedJobIds, checkingNodes);

            // set check for node
            getItemsTreeViewer().setCheckedElements(checkingNodes.toArray());
            // select and will expand the selected node auto
            getItemsTreeViewer().setSelection(new StructuredSelection(checkingNodes), true);

            // let scroll bar on top
            getItemsTreeViewer().setSelection(new StructuredSelection(rootRepositoryNode));
            getItemsTreeViewer().setSelection(StructuredSelection.EMPTY);
        }
    }

    private void withReferenceProjects(final ITreeContentProvider cnfContentProvider, RepositoryNode rootRepositoryNode,
            final List<String> needCheckedJobIds, List<IRepositoryNode> checkingNodes) {
        if (rootRepositoryNode == null) {
            return;
        }
        setCheckingNodes(cnfContentProvider, rootRepositoryNode, needCheckedJobIds, checkingNodes, true);

        if (needCheckedJobIds.isEmpty()) {
            return;
        }
        // ref
        final IRepositoryNode refRoot = rootRepositoryNode.getRoot().getRootRepositoryNode(
                ERepositoryObjectType.REFERENCED_PROJECTS);
        if (refRoot != null) {
            final Object[] children = cnfContentProvider.getChildren(refRoot);
            for (Object c : children) {
                if (c instanceof ProjectRepositoryNode) {
                    withReferenceProjects(cnfContentProvider,
                            ((ProjectRepositoryNode) c).getRootRepositoryNode(getSupportType()), needCheckedJobIds, checkingNodes);
                }
            }
        }
    }

    private void setCheckingNodes(ITreeContentProvider contentProvider, IRepositoryNode current, List<String> checkingList,
            List<IRepositoryNode> checkingNodes, boolean isSelectionId) {
        if (checkingList.isEmpty()) {
            return; // if no checking items yet.
        }

        boolean valid = false;
        if (current.getType() == ENodeType.REPOSITORY_ELEMENT) {
            if (isSelectionId) {
                IRepositoryViewObject object = current.getObject();
                if (object != null && checkingList.contains(object.getId())) {
                    valid = true;
                    checkingList.remove(object.getId()); // have done, so remove
                }
            } else {
                final Object label = current.getProperties(EProperties.LABEL);
                if (checkingList.contains(label)) {
                    valid = true;
                    checkingList.remove(label); // have done, so remove

                }
            }
        }
        if (valid) {
            checkingNodes.add(current);
        } else if (current.hasChildren()) {
            for (IRepositoryNode c : current.getChildren()) {
                setCheckingNodes(contentProvider, c, checkingList, checkingNodes, isSelectionId);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        collectCheckedNodes();
        filteredCheckboxTree.dispose();
        super.okPressed();
    }

    public List<RepositoryNode> getRepositoryNodes() {
        return this.repositoryNodes;
    }

    private void collectCheckedNodes() {
        repositoryNodes.clear();
        CheckboxTreeViewer exportItemsTreeViewer = getItemsTreeViewer();
        for (int i = 0; i < exportItemsTreeViewer.getCheckedElements().length; i++) {
            RepositoryNode node = (RepositoryNode) exportItemsTreeViewer.getCheckedElements()[i];
            if (node.getType() == ENodeType.REPOSITORY_ELEMENT) {
                repositoryNodes.add(node);
            }
        }
    }

    protected ERepositoryObjectType getSupportType() {
        return ERepositoryObjectType.PROCESS; // currently, only support standard job for dynamic
    }

    private void createTreeViewer(Composite itemComposite) {
        filteredCheckboxTree = new FilteredCheckboxTree(itemComposite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI) {

            @Override
            protected CheckboxTreeViewer doCreateTreeViewer(Composite parent, int style) {
                return (CheckboxTreeViewer) RepoCommonViewerProvider.CHECKBOX.createViewer(parent);
            }

            @Override
            protected void refreshCompleted() {
                restoreCheckedElements();
            }
        };
        CheckboxTreeViewer viewer = filteredCheckboxTree.getViewer();
        viewer.setComparer(new IElementComparer() {

            private String getElementUniqueString(Object element) {
                if (element instanceof RepositoryNode && getSupportType() != null
                        && getSupportType().equals(((RepositoryNode) element).getContentType())) {
                    final RepositoryNode node = (RepositoryNode) element;
                    StringBuffer sb = new StringBuffer();

                    boolean validElem = false;
                    ENodeType type = node.getType();
                    if (ENodeType.REPOSITORY_ELEMENT.equals(type)) {
                        sb.append(node.getId());
                        validElem = true;
                    } else if (ENodeType.SIMPLE_FOLDER.equals(type)) {
                        final IRepositoryViewObject object = node.getObject();
                        // path
                        if (object instanceof Folder) {
                            sb.append(((Folder) object).getPath());
                            sb.append('/');
                        }
                        sb.append(node.getLabel());
                        validElem = true;
                    }
                    if (validElem) {
                        sb.append('|');
                        // add project
                        if (node.getRoot() != null && node.getRoot().getProject() != null) {
                            sb.append(node.getRoot().getProject().getTechnicalLabel());
                        }

                        sb.append('|');
                        sb.append(node.getContentType().getType());

                        sb.append('|');
                        sb.append(type);

                        return sb.toString();
                    }
                }
                return null;
            }

            @Override
            public int hashCode(Object element) {
                final String elementUniqueString = getElementUniqueString(element);
                if (elementUniqueString != null) {
                    return elementUniqueString.hashCode();
                }
                return element.hashCode();
            }

            @Override
            public boolean equals(Object a, Object b) {
                String aStr = getElementUniqueString(a);
                String bStr = getElementUniqueString(b);

                return aStr != null && bStr != null ? aStr.equals(bStr) : a.equals(b);
            }
        });
        viewer.addFilter(new ViewerFilter() {

            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                RepositoryNode node = (RepositoryNode) element;
                return filterRepositoryNode(node);
            }
        });
    }

    public void addCheckStateListener(ICheckStateListener listener) {
        CheckboxTreeViewer exportItemsTreeViewer = getItemsTreeViewer();
        exportItemsTreeViewer.addCheckStateListener(listener);
    }

    public void removeCheckStateListener(ICheckStateListener listener) {
        CheckboxTreeViewer exportItemsTreeViewer = getItemsTreeViewer();
        exportItemsTreeViewer.removeCheckStateListener(listener);
    }

    private boolean filterRepositoryNode(RepositoryNode node) {
        if (node == null) {
            return false;
        }
        if (node.isBin()) {
            return false;
        }

        ERepositoryObjectType contentType = node.getContentType();
        if (contentType != null) {
            if (getSupportType().equals(contentType)) {
                String id = node.getId();
                // hide current process
                if (curProcessId != null && !curProcessId.isEmpty() && curProcessId.equals(id)) {
                    return false;
                }
                return true;
            } else if (contentType == ERepositoryObjectType.SVN_ROOT) {
                return true;
            } else if (contentType == ERepositoryObjectType.REFERENCED_PROJECTS) {
                return true;
            } else {
                return false;
            }
        } else {
            if (node.getType() == ENodeType.REPOSITORY_ELEMENT) {
                return true;
            }
        }

        return false;
    }

    /**
     * DOC hcw Comment method "createSelectionButton".
     *
     * @param itemComposite
     */
    private void createSelectionButton(Composite itemComposite) {
        Composite buttonComposite = new Composite(itemComposite, SWT.NONE);
        GridLayoutFactory.swtDefaults().margins(0, 25).applyTo(buttonComposite);
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
        expandBtn.setText(Messages.getString("UseDynamicJobSelectionDialog.expandBtnText")); //$NON-NLS-1$
        expandBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                CheckboxTreeViewer exportItemsTreeViewer = getItemsTreeViewer();
                exportItemsTreeViewer.expandAll();
            }
        });
        setButtonLayoutData(expandBtn);

        Button collapseBtn = new Button(buttonComposite, SWT.PUSH);
        collapseBtn.setText(Messages.getString("UseDynamicJobSelectionDialog.collapseBtnText")); //$NON-NLS-1$
        collapseBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                CheckboxTreeViewer exportItemsTreeViewer = getItemsTreeViewer();
                exportItemsTreeViewer.collapseAll();
            }
        });
        setButtonLayoutData(collapseBtn);
    }

    /**
     * DOC nrousseau Comment method "dispose".
     */
    public void dispose() {
        CheckboxTreeViewer exportItemsTreeViewer = getItemsTreeViewer();
        exportItemsTreeViewer.setCheckedElements(ArrayUtils.EMPTY_OBJECT_ARRAY);
        exportItemsTreeViewer = null;
        repositoryNodes.clear();
        repositoryNodes = null;
        needCheckedjobs = null;
        curProcessId = null;
        filteredCheckboxTree = null;
    }
}
