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
package org.talend.designer.business.diagram.views.jobsettings;

import java.util.List;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.IItemPropertySource;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.swt.actions.ITreeContextualAction;
import org.talend.core.CorePlugin;
import org.talend.core.model.business.BusinessType;
import org.talend.core.model.metadata.MetadataToolHelper;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.connection.SAPFunctionUnit;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.properties.SQLPatternItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.utils.RepositoryManagerHelper;
import org.talend.core.repository.model.ProjectRepositoryNode;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.business.diagram.custom.properties.RepositoryFactoryProxyLabelProvider;
import org.talend.designer.business.diagram.i18n.Messages;
import org.talend.designer.business.model.business.BusinessAssignment;
import org.talend.designer.business.model.business.BusinessFactory;
import org.talend.designer.business.model.business.BusinessPackage;
import org.talend.designer.business.model.business.Query;
import org.talend.designer.business.model.business.Routine;
import org.talend.designer.business.model.business.SAPFunction;
import org.talend.designer.business.model.business.SQLPattern;
import org.talend.designer.business.model.business.TableMetadata;
import org.talend.designer.business.model.business.TalendItem;
import org.talend.designer.business.model.business.provider.BusinessItemProviderAdapterFactory;
import org.talend.designer.core.ui.views.jobsettings.JobSettingsView;
import org.talend.designer.core.ui.views.jobsettings.tabs.AbstractTabComposite;
import org.talend.designer.core.utils.EmfPropertyHelper;
import org.talend.designer.core.utils.KeyHelper;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;
import org.talend.repository.model.nodes.IProjectRepositoryNode;
import org.talend.repository.ui.actions.AContextualAction;
import org.talend.repository.ui.actions.ActionsHelper;
import org.talend.repository.ui.views.IRepositoryView;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class BusinessAssignmentComposite extends AbstractTabComposite {

    private boolean enableControl;

    private Composite composite;

    private AdapterFactory adapterFactory;

    private TableViewer tableViewer;

    private RepositoryNode repositoryNode;

    /**
     * DOC Administrator BusinessAssignmentComposite constructor comment.
     *
     * @param parent
     * @param style
     * @param widgetFactory
     * @param obj
     */
    public BusinessAssignmentComposite(Composite parent, int style, TabbedPropertySheetWidgetFactory widgetFactory,
            ISelection selection) {
        super(parent, style, widgetFactory, null);

        Composite composite = widgetFactory.createFlatFormComposite(this);

        FormLayout layout = new FormLayout();
        setLayout(layout);

        FormData thisFormData = new FormData();
        thisFormData.left = new FormAttachment(0, 0);
        thisFormData.right = new FormAttachment(100, 0);
        thisFormData.top = new FormAttachment(0, 0);
        thisFormData.bottom = new FormAttachment(100, 0);
        setLayoutData(thisFormData);

        FormData compositeData = new FormData();
        compositeData.left = new FormAttachment(0, 0);
        compositeData.right = new FormAttachment(100, 0);
        compositeData.top = new FormAttachment(0, 0);
        compositeData.bottom = new FormAttachment(100, 0);
        composite.setLayoutData(compositeData);
        composite.setLayout(new FillLayout());

        BusinessType type = CorePlugin.getDefault().getDiagramModelService()
                .getBusinessModelType(((IStructuredSelection) selection).getFirstElement());
        if (BusinessType.SHAP == type || BusinessType.CONNECTION == type) {
            createControls(composite);
            setInput(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor(), selection);

        }

    }

    public void createControls(Composite parent) {

        composite = widgetFactory.createFlatFormComposite(parent);

        adapterFactory = new BusinessItemProviderAdapterFactory();

        tableViewer = new TableViewer(composite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        tableViewer.setContentProvider(new AdapterFactoryContentProvider(adapterFactory));
        tableViewer.setLabelProvider(new RepositoryFactoryProxyLabelProvider(adapterFactory));

        Table table = tableViewer.getTable();
        TableLayout tableLayout = new TableLayout();
        table.setLayout(tableLayout);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        final String[] columnProperties = new String[] {
                Messages.getString("AssignmentPropertySection.Type"), Messages.getString("AssignmentPropertySection.Name"), Messages.getString("AssignmentPropertySection.Comment") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        TableColumn column1 = new TableColumn(table, SWT.NONE);
        tableLayout.addColumnData(new ColumnPixelData(125, true));
        column1.setText(columnProperties[0]);

        TableColumn column2 = new TableColumn(table, SWT.NONE);
        tableLayout.addColumnData(new ColumnPixelData(125, true));
        column2.setText(columnProperties[1]);

        TableColumn column3 = new TableColumn(table, SWT.NONE);
        tableLayout.addColumnData(new ColumnWeightData(1, 150, true));
        column3.setText(columnProperties[2]);

        tableViewer.setColumnProperties(columnProperties);

        final IItemPropertyDescriptor itemPropertyDescriptor = getItemPropertyDescriptor();

        tableViewer.setCellModifier(new ICellModifier() {

            @Override
            public boolean canModify(Object element, String property) {
                return property.equals(columnProperties[2]);
            }

            @Override
            public Object getValue(Object element, String property) {
                return EmfPropertyHelper.getValue(itemPropertyDescriptor, element);

            }

            @Override
            public void modify(Object element, String property, Object value) {
                if (element instanceof TableItem) {
                    TableItem tableItem = (TableItem) element;
                    itemPropertyDescriptor.setPropertyValue(tableItem.getData(), value);
                }
            }

        });

        CellEditor[] cellEditors = new CellEditor[3];
        cellEditors[2] = new TextCellEditor(table);
        tableViewer.setCellEditors(cellEditors);

        // createKeyListener(table);
        createSelectionListener();
        createPopupMenu();
        createDoubleClickListener();

        handleLayout(parent, table, column1, column2, column3);

    }

    private void createDoubleClickListener() {
        tableViewer.addDoubleClickListener(new IDoubleClickListener() {

            @Override
            public void doubleClick(DoubleClickEvent event) {

                BusinessAssignment businessAssignment = getBusinessAssignment(event.getSelection());
                if (businessAssignment != null) {
                    repositoryNode = createRepositoryNode(businessAssignment);
                    if (repositoryNode != null) {
                        // List<ITreeContextualAction> contextualsActions =
                        // ActionsHelper.getRepositoryContextualsActions();
                        // for (ITreeContextualAction action : contextualsActions) {
                        // if (action.isReadAction() || action.isEditAction() || action.isPropertiesAction()) {
                        // action.init(null, new StructuredSelection(repositoryNode));
                        // if (action.isVisible() && action.isEnabled() && action.isDoubleClickAction()) {
                        // action.run();
                        // return;
                        // }
                        // }
                        // }

                        // FIXME, hide this function temp
                        // Action action =
                        // CorePlugin.getDefault().getRepositoryService().getRepositoryViewDoubleClickAction();
                        // action.run();

                    }
                }
            }

        });
    }

    private void createSelectionListener() {
        tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                BusinessAssignment businessAssignment = getBusinessAssignment(event.getSelection());
                if (businessAssignment != null) {
                    final IRepositoryView repositoryView = getRepositoryView();
                    if (repositoryView != null) {
                        IProjectRepositoryNode rootRepositoryNode = repositoryView.getRoot();

                        TalendItem item = businessAssignment.getTalendItem();
                        //
                        if (item instanceof Routine && rootRepositoryNode instanceof ProjectRepositoryNode) {
                            RepositoryNodeUtilities.expandParentNode(getRepositoryView(),
                                    ((ProjectRepositoryNode) rootRepositoryNode)
                                            .getRootRepositoryNode(ERepositoryObjectType.CODE));
                        }
                        selectChild(item, (IRepositoryNode) rootRepositoryNode);
                    }
                }
            }

            private void selectChild(TalendItem item, IRepositoryNode rootRepositoryNode) {

                try {
                    RepositoryNode curNode = null;
                    JobSettingsView viewer = (JobSettingsView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                            .getActivePage().findView(JobSettingsView.ID);
                    IRepositoryViewObject lastVersion = ProxyRepositoryFactory.getInstance().getLastVersion(item.getId());
                    if (lastVersion != null) {
                        curNode = RepositoryNodeUtilities.getRepositoryNode(lastVersion);
                        select(viewer, curNode);
                    } else if (item instanceof TableMetadata) {
                        MetadataTable table = MetadataToolHelper.getMetadataTableFromRepository(item.getId());
                        if (table != null) {
                            String id = item.getId().split(" - ")[0]; //$NON-NLS-1$

                            RepositoryNode node = RepositoryNodeUtilities.getMetadataTableFromConnection(item.getId());

                            IRepositoryView view = getRepositoryView();
                            if (view != null) {
                                RepositoryNodeUtilities.expandParentNode(view, node);
                                select(viewer, node);
                            }

                        }
                    } else if (item instanceof Query) {
                        org.talend.core.model.metadata.builder.connection.Query query = MetadataToolHelper
                                .getQueryFromRepository(item.getId());
                        if (query != null) {
                            String id = item.getId().split(" - ")[0]; //$NON-NLS-1$
                            IRepositoryView view = getRepositoryView();
                            RepositoryNode node = RepositoryNodeUtilities.getQueryFromConnection(item.getId());
                            RepositoryNodeUtilities.expandParentNode(view, node);
                            select(viewer, node);
                        }

                    } else if (item instanceof SAPFunction) {
                        SAPFunctionUnit function = MetadataToolHelper.getSAPFunctionFromRepository(item.getId());
                        if (function != null) {
                            IRepositoryView view = getRepositoryView();
                            RepositoryNode node = RepositoryNodeUtilities.getSAPFunctionFromConnection(item.getId());
                            RepositoryNodeUtilities.expandParentNode(view, node);
                            select(viewer, node);
                        }

                    } else {

                        for (IRepositoryNode node : rootRepositoryNode.getChildren()) {
                            RepositoryNode rNode = (RepositoryNode) node;
                            if (item instanceof SQLPattern
                                    && rNode.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.SQLPATTERNS) {
                                if (rNode.getType() == ENodeType.REPOSITORY_ELEMENT) {
                                    SQLPatternItem sqlItem = (SQLPatternItem) rNode.getObject().getProperty().getItem();
                                    if (sqlItem.isSystem() && item.getLabel().equals(rNode.getObject().getLabel())) {
                                        select(viewer, rNode);
                                    }
                                } else {
                                    selectChild(item, rNode);
                                }
                            } else if (item instanceof Routine
                                    && rNode.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.ROUTINES) {
                                if (rNode.getType() == ENodeType.REPOSITORY_ELEMENT) {
                                    RoutineItem sqlItem = (RoutineItem) rNode.getObject().getProperty().getItem();
                                    if (sqlItem.isBuiltIn() && item.getLabel().equals(rNode.getObject().getLabel())) {
                                        select(viewer, rNode);
                                    }
                                } else {
                                    selectChild(item, rNode);
                                }
                            }

                        }
                    }

                } catch (PersistenceException e) {
                    // TODO Auto-generated catch block
                    // e.printStackTrace();
                    ExceptionHandler.process(e);
                }

            }

            private void select(JobSettingsView viewer, RepositoryNode repositoryNode) {
                if (viewer == null) {
                    return;
                }
                if (repositoryNode == null) {
                    return;
                }
                CorePlugin.getDefault().getRepositoryService().removeRepositoryTreeViewListener(viewer);
                final IRepositoryView repositoryView = getRepositoryView();
                if (repositoryView != null) {
                    repositoryView.getViewer().setSelection(new StructuredSelection(repositoryNode));
                }
                CorePlugin.getDefault().getRepositoryService().addRepositoryTreeViewListener(viewer);
            }

        });
    }

    private BusinessAssignment getBusinessAssignment(ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection) selection;
            if (structuredSelection.size() == 1) {
                Object firstElement = structuredSelection.getFirstElement();
                if (firstElement instanceof BusinessAssignment) {
                    return (BusinessAssignment) firstElement;
                }
            }
        }
        return null;
    }

    private void createPopupMenu() {
        MenuManager menuMgr = new MenuManager("#PopUp"); //$NON-NLS-1$
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {

            @Override
            public void menuAboutToShow(IMenuManager mgr) {
                BusinessAssignment businessAssignment = getBusinessAssignment(tableViewer.getSelection());
                if (businessAssignment != null) {
                    repositoryNode = createRepositoryNode(businessAssignment);
                    if (repositoryNode != null) {
                        List<ITreeContextualAction> contextualsActions = ActionsHelper.getRepositoryContextualsActions();
                        for (ITreeContextualAction action : contextualsActions) {
                            if (action.isReadAction() || action.isEditAction() || action.isPropertiesAction()) {
                                action.init(null, new StructuredSelection(repositoryNode));
                                if (action.isVisible()) {
                                    ((AContextualAction) action).setAvoidUnloadResources(true);
                                    mgr.add(action);
                                }
                            }
                        }
                        if (ProjectManager.getInstance().isInCurrentMainProject(repositoryNode)) {
                            CorePlugin.getDefault().getDiagramModelService()
                                    .addDeleteAssignmentAction(mgr, tableViewer.getSelection());
                        }
                    }
                }
            }
        });
        Menu menu = menuMgr.createContextMenu(tableViewer.getControl());
        tableViewer.getControl().setMenu(menu);
    }

    private RepositoryNode createRepositoryNode(BusinessAssignment businessAssignment) {
        IRepositoryViewObject lastVersion;
        try {
            TalendItem item = businessAssignment.getTalendItem();
            lastVersion = ProxyRepositoryFactory.getInstance().getLastVersion(item.getId());

            if (lastVersion != null) {
                ERepositoryObjectType itemType = ERepositoryObjectType.getItemType(lastVersion.getProperty().getItem());
                RepositoryNode repositoryNode = new RepositoryNode(lastVersion,
                        RepositoryNodeUtilities.getParentRepositoryNodeFromSelection(lastVersion), ENodeType.REPOSITORY_ELEMENT);
                repositoryNode.setProperties(EProperties.CONTENT_TYPE, itemType);

                return repositoryNode;
            } else if (item instanceof SQLPattern) {
                IRepositoryViewObject object = getObjectFromSystem(item, ERepositoryObjectType.SQLPATTERNS);
                if (object != null) {
                    RepositoryNode repositoryNode = new RepositoryNode(object,
                            RepositoryNodeUtilities.getParentRepositoryNodeFromSelection(object), ENodeType.REPOSITORY_ELEMENT);
                    repositoryNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.SQLPATTERNS);

                    return repositoryNode;
                }
            } else if (item instanceof Routine) {
                IRepositoryViewObject object = getObjectFromSystem(item, ERepositoryObjectType.ROUTINES);
                if (object != null) {
                    RepositoryNode repositoryNode = new RepositoryNode(object,
                            RepositoryNodeUtilities.getParentRepositoryNodeFromSelection(object), ENodeType.REPOSITORY_ELEMENT);
                    repositoryNode.setProperties(EProperties.CONTENT_TYPE, ERepositoryObjectType.ROUTINES);

                    return repositoryNode;
                }
            } else if (item instanceof TableMetadata) {
                MetadataTable table = MetadataToolHelper.getMetadataTableFromRepository(item.getId());
                if (table != null) {
                    return RepositoryNodeUtilities.getMetadataTableFromConnection(item.getId());
                }

            } else if (item instanceof Query) {
                org.talend.core.model.metadata.builder.connection.Query query = MetadataToolHelper.getQueryFromRepository(item
                        .getId());
                if (query != null) {
                    return RepositoryNodeUtilities.getQueryFromConnection(item.getId());
                }
            } else if (item instanceof SAPFunction) {
                SAPFunctionUnit function = MetadataToolHelper.getSAPFunctionFromRepository(item.getId());
                if (function != null) {
                    return RepositoryNodeUtilities.getSAPFunctionFromConnection(item.getId());
                }
            }

        } catch (PersistenceException e) {
        }
        return null;
    }

    private IRepositoryViewObject getObjectFromSystem(TalendItem item, ERepositoryObjectType type) {
        try {
            List<IRepositoryViewObject> list = ProxyRepositoryFactory.getInstance().getAll(type);
            for (IRepositoryViewObject object : list) {
                if (item.getLabel().equals(object.getLabel())) {
                    return object;
                }
            }
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
        return null;
    }

    private IRepositoryView getRepositoryView() {
        return RepositoryManagerHelper.findRepositoryView();
    }

    private void handleLayout(Composite parent, Table table, TableColumn column1, TableColumn column2, TableColumn column3) {
        Object layoutData = parent.getLayoutData();
        if (layoutData instanceof GridData) {
            GridData gridData = (GridData) layoutData;
            gridData.grabExcessVerticalSpace = true;
            gridData.verticalAlignment = SWT.FILL;
        }

        FormData formData = new FormData();
        formData.left = new FormAttachment(0);
        formData.top = new FormAttachment(0);
        formData.right = new FormAttachment(100);
        formData.bottom = new FormAttachment(100);
        table.setLayoutData(formData);
    }

    private IItemPropertyDescriptor getItemPropertyDescriptor() {
        // PTODO mhelleboid find another way to itempropertysource without an eobject
        BusinessAssignment sampleBusinessAssignment = BusinessFactory.eINSTANCE.createBusinessAssignment();
        EStructuralFeature businessAssignment_Comment = BusinessPackage.eINSTANCE.getBusinessAssignment_Comment();
        IItemPropertySource itemPropertySource = EmfPropertyHelper
                .getItemPropertySource(adapterFactory, sampleBusinessAssignment);
        return EmfPropertyHelper.getItemPropertyDescriptor(itemPropertySource, sampleBusinessAssignment,
                businessAssignment_Comment);

    }

    private void createKeyListener(Table table) {
        table.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent event) {
                ISelection selection = tableViewer.getSelection();
                if (selection instanceof IStructuredSelection) {
                    IStructuredSelection structuredSelection = (IStructuredSelection) selection;

                    if (event.keyCode == SWT.DEL) {
                        // executeDeleteCommand(structuredSelection);
                    } else if (new KeyHelper().anyModifierPressed(event.keyCode)) {
                        // do nothing for Ctrl + Y / Ctrl +Z
                    } else if (new KeyHelper().cursorPressed(event.keyCode)) {
                        // do nothing for UP / DOWN
                    } else {
                        // Edit third column
                        tableViewer.editElement(structuredSelection.getFirstElement(), 2);
                    }
                }
            }
        });
    }

    public void setInput(IWorkbenchPart part, ISelection selection) {
        tableViewer.setInput(CorePlugin.getDefault().getDiagramModelService().getEObject(selection));
    }

    public void addSelectionChangedListener(ISelectionChangedListener listener) {
    }

    public void removeSelectionChangedListener(ISelectionChangedListener listener) {
    }

    public void setSelection(ISelection selection) {
    }

    public ISelection getSelection() {
        if (repositoryNode == null) {
            return new StructuredSelection();
        }
        return new StructuredSelection(repositoryNode);
    }

    @Override
    public void refresh() {
        super.refresh();
        if (tableViewer != null) {
            tableViewer.refresh();
        }
    }

}
