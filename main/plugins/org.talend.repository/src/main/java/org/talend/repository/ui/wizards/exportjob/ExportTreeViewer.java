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
package org.talend.repository.ui.wizards.exportjob;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TreeItem;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.ui.ITestContainerProviderService;
import org.talend.core.ui.advanced.composite.FilteredCheckboxTree;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.views.IRepositoryView;
import org.talend.repository.viewer.ui.provider.RepositoryViewerProvider;

/**
 * DOC achen class global comment. Detailled comment
 */
public class ExportTreeViewer {

    private FilteredCheckboxTree filteredCheckboxTree;

    Collection<RepositoryNode> repositoryNodes = new ArrayList<RepositoryNode>();

    private IStructuredSelection selection;

    private SashForm sash;

    private Button moveButton;

    private JobScriptsExportWizardPage jobScriptExportWizardPage;

    private IRepositoryView realRepView;

    public ExportTreeViewer(IStructuredSelection selection, JobScriptsExportWizardPage jobScriptExportWizardPage) {
        this.selection = selection;
        if (this.selection != null && !this.selection.isEmpty()) {
            this.repositoryNodes.addAll(this.selection.toList());
        }
        this.jobScriptExportWizardPage = jobScriptExportWizardPage;
    }

    public ExportTreeViewer(RepositoryNode[] repNodes, JobScriptsExportWizardPage jobScriptExportWizardPage) {
        if (repNodes != null) {
            for (RepositoryNode repNode : repNodes) {
                this.repositoryNodes.add(repNode);
            }
        }
        this.jobScriptExportWizardPage = jobScriptExportWizardPage;
    }

    protected ERepositoryObjectType getCheckingType() {
        return ERepositoryObjectType.PROCESS; // default for job
    }

    public SashForm createContents(Composite parent) {
        // Splitter
        final GridData data = new GridData();
        data.heightHint = 600;
        data.widthHint = 600;
        sash = new SashForm(parent, SWT.HORIZONTAL | SWT.SMOOTH);
        sash.setLayoutData(data);

        GridLayout layout = new GridLayout();
        sash.setLayout(layout);

        // create tree
        createItemList(sash);

        // create button
        Composite buttonComposite = new Composite(sash, SWT.ERROR);
        buttonComposite.setLayout(new GridLayout());

        moveButton = new Button(buttonComposite, SWT.PUSH);
        moveButton.setText(">>"); //$NON-NLS-1$
        moveButton.setToolTipText("Show job tree"); //$NON-NLS-1$

        final GridData layoutData = new GridData();
        layoutData.verticalAlignment = GridData.CENTER;
        layoutData.horizontalAlignment = GridData.CENTER;
        layoutData.grabExcessHorizontalSpace = true;
        layoutData.grabExcessVerticalSpace = true;
        layoutData.widthHint = 30;
        moveButton.setLayoutData(layoutData);

        // add listner
        moveButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                if (moveButton.getText().equals("<<")) { //$NON-NLS-1$
                    sash.setWeights(new int[] { 0, 1, 23 });
                    moveButton.setText(">>"); //$NON-NLS-1$
                    moveButton.setToolTipText(Messages.getString("ExportTreeViewer.showJobTree")); //$NON-NLS-1$
                } else if (moveButton.getText().equals(">>")) { //$NON-NLS-1$
                    sash.setWeights(new int[] { 10, 1, 15 });
                    moveButton.setText("<<"); //$NON-NLS-1$
                    moveButton.setToolTipText(Messages.getString("ExportTreeViewer.hideJobTree")); //$NON-NLS-1$
                }
            }
        });

        return sash;
    }

    /**
     *
     * @param workArea
     */
    public Composite createItemList(Composite workArea) {

        Composite itemComposite = new Composite(workArea, SWT.BORDER);
        GridLayout gdlItemComposite = new GridLayout(2, false);
        itemComposite.setLayout(gdlItemComposite);

        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(itemComposite);

        Label label = new Label(itemComposite, SWT.NONE);
        label.setText(Messages.getString("ExportTreeViewer.selectItems")); //$NON-NLS-1$
        GridDataFactory.swtDefaults().grab(true, false).span(2, 1).applyTo(label);

        createTreeViewer(itemComposite);

        createSelectionButton(itemComposite);

        CheckboxTreeViewer exportItemsTreeViewer = getExportItemsTreeViewer();
        exportItemsTreeViewer.refresh();
        // force loading all nodes
        exportItemsTreeViewer.expandAll();
        exportItemsTreeViewer.collapseAll();
        // expand to level of metadata connection
        exportItemsTreeViewer.expandToLevel(4);

        // if user has select some items in repository view, mark them as checked
        if (!repositoryNodes.isEmpty()) {
            exportItemsTreeViewer.setCheckedElements(repositoryNodes.toArray());
            for (RepositoryNode node : repositoryNodes) {
                expandParent(exportItemsTreeViewer, node);
                exportItemsTreeViewer.refresh(node);
            }
            selectItems(exportItemsTreeViewer.getTree().getItems());
        }
        return itemComposite;
    }

    /***
     * bug fix : items are not selected in database repository mode if the repository node is refresh and a new instance
     * of the repository node is created we need to compare ids to select this nodes
     */
    private void selectItems(TreeItem[] treeItems) {
        CheckboxTreeViewer exportItemsTreeViewer = getExportItemsTreeViewer();
        for (TreeItem treeItem : treeItems) {
            if (treeItem.getData() != null && treeItem.getData() instanceof RepositoryNode) {
                RepositoryNode repositoryNode = (RepositoryNode) treeItem.getData();
                for (RepositoryNode repositoryNode2 : repositoryNodes) {
                    if (repositoryNode.getRoot().getProject().equals(repositoryNode2.getRoot().getProject())
                            && repositoryNode.getId().equals(repositoryNode2.getId())) {
                        exportItemsTreeViewer.setChecked(repositoryNode, true);
                    }
                }
            }
            selectItems(treeItem.getItems());
        }
    }

    private void expandParent(TreeViewer viewer, RepositoryNode node) {
        RepositoryNode parent = node.getParent();
        if (parent != null) {
            expandParent(viewer, parent);
            viewer.setExpandedState(parent, true);
        }
    }

    public FilteredCheckboxTree getFilteredCheckboxTree() {
        return this.filteredCheckboxTree;
    }

    protected CheckboxTreeViewer getExportItemsTreeViewer() {
        if (getFilteredCheckboxTree() != null) {
            return this.getFilteredCheckboxTree().getViewer();
        }
        return null;
    }

    public RepositoryNode[] getCheckNodes() {
        List<RepositoryNode> ret = new ArrayList<RepositoryNode>();
        CheckboxTreeViewer exportItemsTreeViewer = getExportItemsTreeViewer();
        for (int i = 0; i < exportItemsTreeViewer.getCheckedElements().length; i++) {
            RepositoryNode node = (RepositoryNode) exportItemsTreeViewer.getCheckedElements()[i];
            if (node.getType() == ENodeType.REPOSITORY_ELEMENT) {
                ret.add(node);
            }
        }
        return ret.toArray(new RepositoryNode[0]);
    }

    private void createTreeViewer(Composite itemComposite) {

        filteredCheckboxTree = new FilteredCheckboxTree(itemComposite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI) {

            @Override
            protected CheckboxTreeViewer doCreateTreeViewer(Composite parent, int style) {
                RepositoryViewerProvider provider = new RepositoryViewerProvider() {

                    @Override
                    protected ERepositoryObjectType getCheckingType() {
                        return ExportTreeViewer.this.getCheckingType();
                    }

                };
                return (CheckboxTreeViewer) provider.createViewer(parent);
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
        getExportItemsTreeViewer().addFilter(new ViewerFilter() {

            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                RepositoryNode node = (RepositoryNode) element;
                return filterRepositoryNode(node);
            }
        });
    }

    public void addCheckStateListener(ICheckStateListener listener) {
        getExportItemsTreeViewer().addCheckStateListener(listener);
    }

    public void removeCheckStateListener(ICheckStateListener listener) {
        getExportItemsTreeViewer().removeCheckStateListener(listener);
    }

    protected boolean filterRepositoryNode(RepositoryNode node) {
        if (node == null) {
            return false;
        }
        if (node.isBin()) {
            return false;
        }
        if (isTestContainer(node.getObjectType())) {
            return false;
        }

        ERepositoryObjectType contentType = node.getContentType();
        if (contentType != null) {
            if (contentType == ERepositoryObjectType.PROCESS) {
                return true;
            } else if (contentType == ERepositoryObjectType.SVN_ROOT) {
                return true;
            } else if (contentType == ERepositoryObjectType.REFERENCED_PROJECTS) {
                return true;
            } else if (contentType == ERepositoryObjectType.valueOf(ERepositoryObjectType.class, "PROCESS_MR")) {
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

    private boolean isTestContainer(ERepositoryObjectType type) {
        boolean isTestContainer = false;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(ITestContainerProviderService.class)) {
            ITestContainerProviderService testContainerService = (ITestContainerProviderService) GlobalServiceRegister
                    .getDefault().getService(ITestContainerProviderService.class);
            if (testContainerService != null) {
                isTestContainer = testContainerService.isTestContainerType(type);
            }
        }
        return isTestContainer;
    }

    protected void checkSelection() {
        if (jobScriptExportWizardPage == null) {
            return;
        }
        if (this.getCheckNodes().length == 0) {
            StringBuffer buff = new StringBuffer();
            buff.append(Messages.getString("JavaJobScriptsExportWSWizardPage.needOneJobSelected")); //$NON-NLS-1$
            jobScriptExportWizardPage.setErrorMessage(buff.toString());
            jobScriptExportWizardPage.setPageComplete(false);
        } else {
            jobScriptExportWizardPage.setErrorMessage(null);
            jobScriptExportWizardPage.setPageComplete(true);
        }
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
        GridDataFactory.fillDefaults().grab(true, false).applyTo(selectAll);

        // selectAll.setText(DataTransferMessages.DataTransfer_selectAll);
        selectAll.setText(Messages.getString("DataTransferMessages.DataTransfer_selectAll")); //$NON-NLS-1$
        selectAll.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                getExportItemsTreeViewer().setAllChecked(true);
                checkSelection();
            }
        });

        Button deselectAll = new Button(buttonComposite, SWT.PUSH);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(deselectAll);
        // deselectAll.setText(DataTransferMessages.DataTransfer_deselectAll);
        deselectAll.setText(Messages.getString("DataTransferMessages.DataTransfer_deselectAll")); //$NON-NLS-1$
        deselectAll.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                getExportItemsTreeViewer().setAllChecked(false);
                checkSelection();
            }
        });

        // setButtonLayoutData(deselectAll);

        Button expandBtn = new Button(buttonComposite, SWT.PUSH);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(expandBtn);
        expandBtn.setText("Expand All"); //$NON-NLS-1$
        expandBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                getExportItemsTreeViewer().expandAll();
            }
        });
        // setButtonLayoutData(expandBtn);

        Button collapseBtn = new Button(buttonComposite, SWT.PUSH);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(collapseBtn);
        collapseBtn.setText("Collapse All"); //$NON-NLS-1$
        collapseBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                getExportItemsTreeViewer().collapseAll();
            }
        });
        // setButtonLayoutData(collapseBtn);
    }

    /**
     *
     * A repository view with checkbox on the left.
     */

    /**
     * DOC nrousseau Comment method "dispose".
     */
    public void dispose() {
        getExportItemsTreeViewer().setCheckedElements(ArrayUtils.EMPTY_OBJECT_ARRAY);
        jobScriptExportWizardPage = null;
        repositoryNodes.clear();
        repositoryNodes = null;
        filteredCheckboxTree = null;

        realRepView = null;
    }

}
