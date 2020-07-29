// ============================================================================
//
// Copyright (C) 2006-2020 Talend Inc. - www.talend.com
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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IViewSite;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.RepositoryManager;
import org.talend.core.repository.model.ProjectRepositoryNode;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.ui.view.RepositoryLabelProvider;
import org.talend.core.runtime.projectsetting.RuntimeLineageManager;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.views.properties.WidgetFactory;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.preference.ProjectSettingPage;
import org.talend.repository.ui.views.IRepositoryView;
import org.talend.repository.viewer.ui.provider.RepositoryContentProvider;
import org.talend.repository.viewer.ui.viewer.CheckboxRepositoryTreeViewer;

/**
 * 
 * created by hcyi on Jul 23, 2020 Detailled comment
 *
 */
public class RuntimeLineageTreeViewPage extends ProjectSettingPage {

    private IRepositoryView repositoryView;

    private CheckboxRepositoryTreeViewer viewer;

    private AllJobContentProvider contentProvider;

    private List<RepositoryNode> checkedObjects = new ArrayList<RepositoryNode>();

    private List<RepositoryNode> addedObjects = new ArrayList<RepositoryNode>();

    private List<RepositoryNode> removedObjects = new ArrayList<RepositoryNode>();

    private WidgetFactory widgetFactory = new WidgetFactory();

    private Button useRuntimeLineageAllButton;

    private RuntimeLineageManager runtimeLineageManager = null;

    public RuntimeLineageTreeViewPage() {
        super();
        this.noDefaultAndApplyButton();
        runtimeLineageManager = new RuntimeLineageManager();
    }

    @Override
    protected Control createContents(Composite parent) {
        runtimeLineageManager.load();
        Composite composite = widgetFactory.createComposite(parent, SWT.NONE);
        composite.setLayout(new FillLayout());
        GridLayout layout = new GridLayout();
        layout.marginLeft = 5;
        layout.marginRight = 5;
        layout.numColumns = 1;
        layout.makeColumnsEqualWidth = true;
        composite.setLayout(layout);

        useRuntimeLineageAllButton = new Button(composite, SWT.CHECK);
        useRuntimeLineageAllButton.setText(Messages.getString("ExtraComposite.RuntimeLineageSettings.all")); //$NON-NLS-1$

        repositoryView = RepositoryManager.getRepositoryView();
        createRuntimeLineageTree(composite);

        IProxyRepositoryFactory facto = ProxyRepositoryFactory.getInstance();
        if (facto.isUserReadOnlyOnCurrentProject()) {
            composite.setEnabled(false);
        }

        //
        useRuntimeLineageAllButton.setSelection(runtimeLineageManager.isUseRuntimeLineageAll());
        hideTreeViewer(runtimeLineageManager.isUseRuntimeLineageAll());
        addListeners();
        return composite;
    }

    private void createRuntimeLineageTree(Composite composite) {
        Group g = widgetFactory.createGroup(composite, Messages.getString("ExtraComposite.RuntimeLineageSettings.select")); //$NON-NLS-1$
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

        RepositoryNode[] nodes = contentProvider.getContents();
        List<RepositoryNode> objects = new ArrayList<RepositoryNode>();
        if (nodes != null) {
            for (RepositoryNode n : nodes) {
                processItems(objects, n);
                for (RepositoryNode node : objects) {
                    if (runtimeLineageManager.isRuntimeLineageSetting(node.getId(), node.getObject().getVersion())) {
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

    private void hideTreeViewer(boolean hide) {
        viewer.getTree().setEnabled(!useRuntimeLineageAllButton.getSelection());
    }

    private void addListeners() {
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

        useRuntimeLineageAllButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                hideTreeViewer(!useRuntimeLineageAllButton.getSelection());
            }

        });
    }

    private void save() {
        if (viewer == null) {
            return;
        }
        boolean useALL = useRuntimeLineageAllButton.getSelection();

        final IRunnableWithProgress runnable = new IRunnableWithProgress() {

            @Override
            public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                monitor.beginTask(Messages.getString("ExtraComposite.RuntimeLineageSettings.save"), //$NON-NLS-1$
                        (checkedObjects.size()) * 100);
                runtimeLineageManager.save(checkedObjects, useALL);
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

    @Override
    public void refresh() {
        // TODO
    }

    @Override
    public void dispose() {
        if (widgetFactory != null) {
            widgetFactory.dispose();
        }
        super.dispose();
    }

    @Override
    protected void performApply() {
        save();
        super.performApply();
    }

    @Override
    public boolean performOk() {
        save();
        return super.performOk();
    }
}
