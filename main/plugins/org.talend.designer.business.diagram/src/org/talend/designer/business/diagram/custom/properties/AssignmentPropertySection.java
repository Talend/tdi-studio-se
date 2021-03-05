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
package org.talend.designer.business.diagram.custom.properties;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.IItemPropertySource;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.diagram.ui.properties.sections.AbstractModelerPropertySection;
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
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.swt.actions.ITreeContextualAction;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.utils.RepositoryManagerHelper;
import org.talend.core.repository.model.ProjectRepositoryNode;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.business.diagram.custom.commands.UnassignTalendItemsFromBusinessAssignmentCommand;
import org.talend.designer.business.diagram.custom.util.EmfPropertyHelper;
import org.talend.designer.business.diagram.custom.util.KeyHelper;
import org.talend.designer.business.diagram.i18n.Messages;
import org.talend.designer.business.model.business.BusinessAssignment;
import org.talend.designer.business.model.business.BusinessFactory;
import org.talend.designer.business.model.business.BusinessPackage;
import org.talend.designer.business.model.business.provider.BusinessItemProviderAdapterFactory;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;
import org.talend.repository.ui.actions.ActionsHelper;
import org.talend.repository.ui.views.IRepositoryView;

/**
 * DOC mhelleboid class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class AssignmentPropertySection extends AbstractModelerPropertySection implements ISelectionProvider {

    private Composite composite;

    private BusinessItemProviderAdapterFactory adapterFactory;

    private TableViewer tableViewer;

    private RepositoryNode repositoryNode;

    @Override
    public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
        super.createControls(parent, aTabbedPropertySheetPage);

        composite = getWidgetFactory().createFlatFormComposite(parent);

        // PTODO mhelleboid externalize tableviewer creation

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

            public boolean canModify(Object element, String property) {
                return property.equals(columnProperties[2]);
            }

            public Object getValue(Object element, String property) {
                return EmfPropertyHelper.getValue(itemPropertyDescriptor, element);
            }

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

        createKeyListener(table);
        createSelectionListener();
        createPopupMenu();
        createDoubleClickListener();

        handleLayout(parent, table, column1, column2, column3);

        aTabbedPropertySheetPage.getSite().setSelectionProvider(this);
    }

    private void createDoubleClickListener() {
        tableViewer.addDoubleClickListener(new IDoubleClickListener() {

            public void doubleClick(DoubleClickEvent event) {
                BusinessAssignment businessAssignment = getBusinessAssignment(event.getSelection());
                if (businessAssignment != null) {
                    repositoryNode = createRepositoryNode(businessAssignment);
                    if (repositoryNode != null) {
                        List<ITreeContextualAction> contextualsActions = ActionsHelper.getRepositoryContextualsActions();
                        for (ITreeContextualAction action : contextualsActions) {
                            if (action.isReadAction() || action.isEditAction() || action.isPropertiesAction()) {
                                action.init(null, new StructuredSelection(repositoryNode));
                                if (action.isVisible() && action.isDoubleClickAction()) {
                                    action.run();
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    private void createSelectionListener() {

        tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                BusinessAssignment businessAssignment = getBusinessAssignment(event.getSelection());
                if (businessAssignment != null) {
                    String id = businessAssignment.getTalendItem().getId();
                    IRepositoryNode rootRepositoryNode = ProjectRepositoryNode.getInstance();
                    final IRepositoryView repositoryView = RepositoryManagerHelper.findRepositoryView();
                    if (repositoryView != null) {
                        selectChild(repositoryView, id, rootRepositoryNode);
                    }
                }
            }

            private void selectChild(IRepositoryView repositoryView, String id, IRepositoryNode rootRepositoryNode) {

                for (IRepositoryNode repositoryNode : rootRepositoryNode.getChildren()) {
                    if (repositoryNode.getId() != null && repositoryNode.getId().equals(id)) {
                        repositoryView.getViewer().setSelection(new StructuredSelection(repositoryNode));
                    } else {
                        selectChild(repositoryView, id, (RepositoryNode) repositoryNode);
                    }
                }
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
                                    mgr.add(action);
                                }
                            }
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
            lastVersion = ProxyRepositoryFactory.getInstance().getLastVersion(businessAssignment.getTalendItem().getId());

            if (lastVersion != null) {
                ERepositoryObjectType itemType = ERepositoryObjectType.getItemType(lastVersion.getProperty().getItem());
                RepositoryNode repositoryNode = new RepositoryNode(lastVersion,
                        RepositoryNodeUtilities.getParentRepositoryNodeFromSelection(lastVersion), ENodeType.REPOSITORY_ELEMENT);
                repositoryNode.setProperties(EProperties.CONTENT_TYPE, itemType);

                return repositoryNode;
            }
        } catch (PersistenceException e) {
        }
        return null;
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

            public void keyPressed(KeyEvent event) {
                ISelection selection = tableViewer.getSelection();
                if (selection instanceof IStructuredSelection) {
                    IStructuredSelection structuredSelection = (IStructuredSelection) selection;

                    if (event.keyCode == SWT.DEL) {
                        executeDeleteCommand(structuredSelection);
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

    @Override
    public void setInput(IWorkbenchPart part, ISelection selection) {
        super.setInput(part, selection);
        tableViewer.setInput(getEObject());
    }

    private void executeDeleteCommand(IStructuredSelection structuredSelection) {
        UnassignTalendItemsFromBusinessAssignmentCommand command = new UnassignTalendItemsFromBusinessAssignmentCommand(
                getEditingDomain(), true);
        for (Iterator iter = structuredSelection.iterator(); iter.hasNext();) {
            Object object = (Object) iter.next();
            if (object instanceof BusinessAssignment) {
                BusinessAssignment businessAssignment = (BusinessAssignment) object;
                command.addBusinessAssignment(businessAssignment);
            }
        }

        List<ICommand> commands = new ArrayList<ICommand>();
        commands.add(command);

        executeAsCompositeCommand(Messages.getString("AssignmentPropertySection.DeleteAssignment"), commands); //$NON-NLS-1$
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
}
