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
package org.talend.designer.core.ui.editor.dependencies;

import java.util.Collection;

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxViewerCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.resources.ResourceItem;
import org.talend.core.model.utils.JavaResourcesHelper;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.ui.context.CustomDialogCellEditor;
import org.talend.core.ui.context.model.table.ContextTableTabChildModel;
import org.talend.core.ui.context.model.table.ContextTableTabParentModel;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.dependencies.controls.SearchCellLabelProvider;
import org.talend.designer.core.ui.editor.dependencies.dialog.DependenciesContextSelectionDialog;
import org.talend.designer.core.ui.editor.dependencies.dialog.DependenciesResourceSelectionDialog;
import org.talend.designer.core.ui.editor.dependencies.model.JobContextTreeNode;
import org.talend.designer.core.ui.editor.dependencies.model.JobResourceDependencyModel;
import org.talend.designer.core.ui.editor.dependencies.util.ResourceContextHelper;
import org.talend.designer.core.ui.editor.dependencies.util.ResourceDependenciesUtil;

public class ManageResourcePanel extends Composite {

    private static final int COL_NAME = 0;

    private static final int COL_VERSION = 1;

    private static final int COL_PATH = 2;

    private static final int COL_CONTEXT = 3;

    private final TableViewer resourcesTV;

    private final SearchCellLabelProvider labelProvider;

    private final IJobDependenciesChangedListener dependenciesChangedListener;

    private final ToolItem addBtn, delBtn, copyBtn;

    private IProcess2 process;

    private CommandStack commandStack;

    public TableViewer getResourcesTV() {
        return resourcesTV;
    }

    public void setProcess(IProcess2 process) {
        this.process = process;
    }

    public void setCommandStack(CommandStack commandStack) {
        this.commandStack = commandStack;
    }

    public ManageResourcePanel(Composite parent, boolean isReadOnly, final IMessagePart messagePart,
            final IJobDependenciesChangedListener dependenciesChangedListener) {
        super(parent, SWT.NONE);
        this.dependenciesChangedListener = dependenciesChangedListener;

        setLayout(new GridLayout(2, false));

        resourcesTV = new TableViewer(this, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
        final Table table = resourcesTV.getTable();
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        resourcesTV.getTable().setEnabled(!isReadOnly);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);

        TableViewerColumn tableViewerColumn = new TableViewerColumn(resourcesTV, SWT.NONE, COL_NAME);
        tableViewerColumn.getColumn().setText(Messages.getString("ManageResourcePanel.reourceNameCol")); //$NON-NLS-1$
        tableViewerColumn.getColumn().setWidth(200);

        tableViewerColumn = new TableViewerColumn(resourcesTV, SWT.NONE, COL_VERSION);
        tableViewerColumn.getColumn().setText(Messages.getString("ManageResourcePanel.Version")); //$NON-NLS-1$
        tableViewerColumn.getColumn().setWidth(100);

        tableViewerColumn.setEditingSupport(new ComboboxEditingSupport(resourcesTV));

        tableViewerColumn = new TableViewerColumn(resourcesTV, SWT.NONE, COL_PATH);
        tableViewerColumn.getColumn().setText(Messages.getString("ManageResourcePanel.Path")); //$NON-NLS-1$
        tableViewerColumn.getColumn().setWidth(400);

        tableViewerColumn = new TableViewerColumn(resourcesTV, SWT.NONE, COL_CONTEXT);
        tableViewerColumn.getColumn().setText(Messages.getString("ManageResourcePanel.Context")); //$NON-NLS-1$
        tableViewerColumn.getColumn().setWidth(150);

        tableViewerColumn.setEditingSupport(new DialogEditingSupport(resourcesTV));

        labelProvider = new ResourceLabelProvider(resourcesTV);
        resourcesTV.setLabelProvider(labelProvider);
        resourcesTV.setContentProvider(ArrayContentProvider.getInstance());

        table.setLayoutData(new GridData(GridData.FILL_BOTH));

        if (!isReadOnly) {
            resourcesTV.addSelectionChangedListener(new ISelectionChangedListener() {

                @Override
                public void selectionChanged(SelectionChangedEvent event) {
                    JobResourceDependencyModel item = getSelectedItem();
                    delBtn.setEnabled(false);
                    if (item != null) {
                        if (item.isBuiltIn()) {
                            messagePart.setMessage(Messages.getString("ManageResourcePanel.usedBy",
                                    item.getItem().getProperty().getLabel(), item.getSelectedVersion(), item.getRefNodes())); // $NON-NLS-1$
                        } else {
                            delBtn.setEnabled(true);
                            messagePart
                                    .setMessage(item.getItem().getProperty().getLabel() + '(' + item.getSelectedVersion() + ')');
                        }
                    }
                    copyBtn.setEnabled(item != null);
                }
            });
            resourcesTV.getTable().addKeyListener(new KeyAdapter() {

                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.stateMask == SWT.NONE) {
                        if (delBtn.isEnabled() && e.keyCode == SWT.DEL) {
                            deleteData();
                        } else if (e.keyCode == SWT.INSERT) {
                            addData();
                        } else if (copyBtn.isEnabled() && e.keyCode == SWT.CR) {
                            copyPath();
                        }
                    }
                }
            });
        }

        final ToolBar tb = new ToolBar(this, SWT.FLAT | SWT.VERTICAL);
        tb.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));

        addBtn = new ToolItem(tb, SWT.PUSH);
        addBtn.setText(Messages.getString("JobDependenciesPanel.addBtn")); //$NON-NLS-1$
        // addBtn.setImage(ImageProvider.getImage(EImage.ADD_ICON));
        addBtn.setEnabled(!isReadOnly);

        delBtn = new ToolItem(tb, SWT.PUSH);
        delBtn.setText(Messages.getString("JobDependenciesPanel.removeBtn")); //$NON-NLS-1$
        // delBtn.setImage(ImageProvider.getImage(EImage.DELETE_ICON));
        delBtn.setEnabled(false);

        copyBtn = new ToolItem(tb, SWT.PUSH);
        copyBtn.setText(Messages.getString("ManageResourcePanel.CopyPath")); //$NON-NLS-1$
        copyBtn.setEnabled(false);

        if (!isReadOnly) {
            final SelectionListener selectionListener = new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (e.getSource() == addBtn) {
                        addData();
                    } else if (e.getSource() == delBtn) {
                        deleteData();
                    } else if (e.getSource() == copyBtn) {
                        copyPath();
                    }
                }
            };

            addBtn.addSelectionListener(selectionListener);
            delBtn.addSelectionListener(selectionListener);
            copyBtn.addSelectionListener(selectionListener);
        }
    }

    public void setInput(Collection<JobResourceDependencyModel> input) {
        resourcesTV.setInput(input);
    }

    public void setFilterString(String filterString) {
        labelProvider.setFilterString(filterString);
        resourcesTV.refresh();
        resourcesTV.getTable().redraw();
    }

    public void setShowBuiltIn(boolean showBuiltIn) {
        labelProvider.setShowBuiltIn(showBuiltIn);
        resourcesTV.refresh();
    }

    protected Collection<JobResourceDependencyModel> getInput() {
        return (Collection<JobResourceDependencyModel>) resourcesTV.getInput();
    }

    protected void fireDependenciesChangedListener() {
        dependenciesChangedListener.dependencesChanged(this);
    }

    protected void addData() {
        DependenciesResourceSelectionDialog dialog = new DependenciesResourceSelectionDialog(getShell());
        if (Dialog.OK == dialog.open()) {
            Item item = dialog.getResult().getObject().getProperty().getItem();
            if (item instanceof ResourceItem) {
                for (JobResourceDependencyModel rsmodel : getInput()) {
                    if (rsmodel.getItem().getProperty().getId().equals(item.getProperty().getId())) {
                        resourcesTV.setSelection(new StructuredSelection(rsmodel));
                        return;
                    }
                }
                JobResourceDependencyModel model = new JobResourceDependencyModel((ResourceItem) item);
                Property property = process.getProperty();
                String jobLabel = JavaResourcesHelper.getJobFolderName(property.getLabel(), property.getVersion());
                model.setResourceDepPath(ResourceDependenciesUtil.getResourcePath(model, jobLabel, null));
                getInput().add(model);
                resourcesTV.refresh();
                resourcesTV.setSelection(new StructuredSelection(model));
                ResourceDependenciesUtil.copyToExtResourceFolder(model, property.getId(), property.getVersion(), null, null);
                fireDependenciesChangedListener();
            }
        }
    }

    /**
     * Copy class path
     */
    protected void copyPath() {
        final JobResourceDependencyModel item = getSelectedItem();
        if (item != null) {
            Clipboard clipboard = new Clipboard(getDisplay());
            clipboard.setContents(new String[] { item.getResourceDepPath() }, new Transfer[] { TextTransfer.getInstance() });
            MessageDialog.openInformation(getShell(), Messages.getString("ManageResourcePanel.copyTitle"),
                    Messages.getString("ManageResourcePanel.copyMsg", item.getResourceDepPath())); //$NON-NLS-1$
        }
    }

    protected void deleteData() {
        JobResourceDependencyModel item = getSelectedItem();
        if (item != null) {
            getInput().remove(item);
            resourcesTV.refresh();
            ResourceDependenciesUtil.cleanContextFromAllEnvir(process, item.getItem().getProperty().getId());
            fireDependenciesChangedListener();
            Property property = process.getProperty();
            ResourceDependenciesUtil.deleteFromResourceFolder(item, property.getId(), property.getVersion());
            /**
             * if after want to set repository context value to default while deleting resource dependency, uncomment
             * follow
             */
            // if (StringUtils.isNotBlank(item.getContextSource()) &&
            // !IContextParameter.BUILT_IN.equals(item.getContextSource())) {
            // ResourceContextHelper helper = new ResourceContextHelper(null, null);
            // helper.updateRepositoryContext(item, null);
            // }
        }
    }

    private JobResourceDependencyModel getSelectedItem() {
        IStructuredSelection selection2 = (IStructuredSelection) resourcesTV.getSelection();
        return (JobResourceDependencyModel) selection2.getFirstElement();
    }

    private class ResourceLabelProvider extends SearchCellLabelProvider implements ITableLabelProvider, IFontProvider {

        public ResourceLabelProvider(final StructuredViewer structuredViewer) {
            super(structuredViewer);
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            final JobResourceDependencyModel item = (JobResourceDependencyModel) element;
            switch (columnIndex) {
            case COL_NAME:
                return item.toString();
            case COL_VERSION:
                return item.getSelectedVersion();
            case COL_PATH:
                return item.getResourceDepPath();
            case COL_CONTEXT:
                return item.getContextVar();
            default:
                return null;
            }
        }

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        @Override
        public Image getImage(Object element) {
            return null;
        }

        @Override
        protected boolean isBuiltIn(Object element) {
            return ((JobResourceDependencyModel) element).isBuiltIn();
        }
    }

    private class ComboboxEditingSupport extends EditingSupport {

        private ComboBoxViewerCellEditor comboBoxCellEditor;

        public ComboboxEditingSupport(ColumnViewer viewer) {
            super(viewer);
        }

        @Override
        protected void setValue(Object element, Object value) {
            final JobResourceDependencyModel model = (JobResourceDependencyModel) element;
            if (!model.getSelectedVersion().equals(value)) {
                Property property = process.getProperty();
                String jobLabel = JavaResourcesHelper.getJobFolderName(property.getLabel(), property.getVersion());
                model.setSelectedVersion((String) value);
                model.setResourceDepPath(ResourceDependenciesUtil.getResourcePath(model, jobLabel, (String) value));
                getViewer().update(element, null);
                try {
                    IRepositoryViewObject repoObject = ProxyRepositoryFactory.getInstance()
                            .getLastVersion(model.getItem().getProperty().getId());
                    ResourceDependenciesUtil.copyToExtResourceFolder(repoObject, property.getId(), property.getVersion(),
                            (String) value, null);
                } catch (PersistenceException e) {
                    ExceptionHandler.process(e);
                }
                fireDependenciesChangedListener();
            }
        }

        @Override
        protected Object getValue(Object element) {
            return ((JobResourceDependencyModel) element).getSelectedVersion();
        }

        @Override
        protected CellEditor getCellEditor(Object element) {
            // http://jira.talendforge.org/browse/TESB-6584 Xiaopeng Li
            if (comboBoxCellEditor == null) {
                comboBoxCellEditor = new ComboBoxViewerCellEditor((Composite) getViewer().getControl(),
                        SWT.READ_ONLY | SWT.CENTER);
                comboBoxCellEditor.setLabelProvider(new LabelProvider());
                comboBoxCellEditor.setContentProvider(ArrayContentProvider.getInstance());
            }
            comboBoxCellEditor.setInput(((JobResourceDependencyModel) element).getVersions());
            return comboBoxCellEditor;
        }

        @Override
        protected boolean canEdit(Object element) {
            return !((JobResourceDependencyModel) element).isBuiltIn();
        }
    }

    private class DialogEditingSupport extends EditingSupport {

        private CustomDialogCellEditor dialogCellEditor;

        private JobContextTreeNode result = null;

        public DialogEditingSupport(ColumnViewer viewer) {
            super(viewer);
        }

        @Override
        protected CellEditor getCellEditor(Object element) {
            if (dialogCellEditor == null) {
                dialogCellEditor = new CustomDialogCellEditor((Composite) getViewer().getControl()) {

                    @Override
                    protected Object openDialogBox(Control cellEditorWindow) {
                        JobResourceDependencyModel model = (JobResourceDependencyModel) element;
                        DependenciesContextSelectionDialog dialog = new DependenciesContextSelectionDialog(
                                cellEditorWindow.getShell(), process, commandStack, model);
                        if (Dialog.OK == dialog.open()) {
                            result = dialog.getResult();
                        } else {
                            result = null;
                        }
                        return result == null ? null : result.getName();
                    }
                };
            }
            return dialogCellEditor;
        }

        @Override
        protected boolean canEdit(Object element) {
            return !((JobResourceDependencyModel) element).isBuiltIn();
        }

        @Override
        protected Object getValue(Object element) {
            return ((JobResourceDependencyModel) element).getContextVar();
        }

        @Override
        protected void setValue(Object element, Object value) {
            JobResourceDependencyModel model = (JobResourceDependencyModel) element;
            if (result != null && value.equals(result.getName())) {
                if (result.getParent() == null) {
                    model.setContextSource(((ContextTableTabParentModel) result.getTreeData()).getSourceId());
                } else {
                    String sourceId = ((ContextTableTabChildModel) result.getTreeData()).getSourceId();
                    if (!IContextParameter.BUILT_IN.equals(sourceId) && !model.getContextVar().equals(result.getName())) {
                        ResourceContextHelper helper = new ResourceContextHelper(null, null);
                        helper.updateRepositoryContext(model, result);
                    }
                    model.setContextSource(sourceId);
                }
                model.setContextVar(result.getName());
            }
            getViewer().update(element, null);
            fireDependenciesChangedListener();
            process.getContextManager().fireContextsChangedEvent();
        }

    }

}
