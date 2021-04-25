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
package org.talend.repository.ui.properties;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.swt.actions.ITreeContextualAction;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.i18n.Messages;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryObject;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.ui.actions.ActionsHelper;

/**
 *
 * @deprecated
 */
public class VersionSection extends AbstractSection implements ISelectionProvider {

    private Composite composite;

    private TableViewer tableViewer;

    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat();

    /**
     */
    private static class IRepositoryObjectComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            return VersionUtils.compareTo(((IRepositoryObject) o1).getVersion(), ((IRepositoryObject) o2).getVersion());
        }
    }

    @Override
    public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
        super.createControls(parent, aTabbedPropertySheetPage);

        composite = getWidgetFactory().createFlatFormComposite(parent);

        tableViewer = new TableViewer(composite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        final Table table = tableViewer.getTable();
        TableLayout tableLayout = new TableLayout();
        table.setLayout(tableLayout);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        final String[] columnProperties = new String[] { Messages.getString("VersionSection.Version"), //$NON-NLS-1$
                Messages.getString("VersionSection.CreationDate"), //$NON-NLS-1$
                Messages.getString("VersionSection.ModificationDate"), Messages.getString("VersionSection.Status") }; //$NON-NLS-1$ //$NON-NLS-2$

        final TableColumn column1 = new TableColumn(table, SWT.NONE);
        tableLayout.addColumnData(new ColumnPixelData(125, true));
        column1.setText(columnProperties[0]);

        final TableColumn column2 = new TableColumn(table, SWT.NONE);
        tableLayout.addColumnData(new ColumnPixelData(125, true));
        column2.setText(columnProperties[1]);

        final TableColumn column3 = new TableColumn(table, SWT.NONE);
        tableLayout.addColumnData(new ColumnPixelData(125, true));
        column3.setText(columnProperties[2]);

        final TableColumn column4 = new TableColumn(table, SWT.NONE);
        tableLayout.addColumnData(new ColumnWeightData(1, 150, true));
        column4.setText(columnProperties[3]);

        tableViewer.setColumnProperties(columnProperties);

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

        tableViewer.setContentProvider(new IStructuredContentProvider() {

            public Object[] getElements(Object inputElement) {
            	IRepositoryViewObject repositoryObject = ((IRepositoryViewObject) inputElement);
                if (repositoryObject.getProperty() == null) {
                    return null;
                }

                RepositoryNode parentRepositoryNode = RepositoryNodeUtilities
                        .getParentRepositoryNodeFromSelection(repositoryObject);

                try {
                    List<IRepositoryViewObject> allVersion = ProxyRepositoryFactory.getInstance().getAllVersion(
                            repositoryObject.getId());
                    Collections.sort(allVersion, new IRepositoryObjectComparator());
                    Object[] objects = new Object[allVersion.size()];
                    for (int i = 0; i < objects.length; i++) {
                        IRepositoryViewObject repositoryObjectVersion = allVersion.get(i);
                        RepositoryNode repositoryNode = createRepositoryNode(parentRepositoryNode, repositoryObjectVersion);
                        objects[i] = repositoryNode;
                    }
                    return objects;
                } catch (PersistenceException e) {
                    return null;
                }
            }

            private RepositoryNode createRepositoryNode(RepositoryNode parentRepositoryNode,
                    IRepositoryViewObject repositoryObjectVersion) {
                ERepositoryObjectType itemType = ERepositoryObjectType.getItemType(repositoryObjectVersion.getProperty()
                        .getItem());

                RepositoryNode repositoryNode = new RepositoryNode(repositoryObjectVersion, parentRepositoryNode,
                        ENodeType.REPOSITORY_ELEMENT);
                repositoryNode.setProperties(EProperties.CONTENT_TYPE, itemType);
                repositoryNode.setProperties(EProperties.LABEL, repositoryObjectVersion.getLabel());
                return repositoryNode;
            }

            public void dispose() {
            }

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            }
        });

        tableViewer.setLabelProvider(new ITableLabelProvider() {

            public Image getColumnImage(Object element, int columnIndex) {
                return null;
            }

            public String getColumnText(Object element, int columnIndex) {
                RepositoryNode repositoryNode = (RepositoryNode) element;
                switch (columnIndex) {
                case 0:
                    return repositoryNode.getObject().getVersion();
                case 1:
                    if (repositoryNode.getObject().getCreationDate() != null) {
                        return FORMATTER.format(repositoryNode.getObject().getCreationDate());

                    } else {
                        return null;
                    }
                case 2:
                    if (repositoryNode.getObject().getModificationDate() != null) {
                        return FORMATTER.format(repositoryNode.getObject().getModificationDate());
                    } else {
                        return null;
                    }
                case 3:
                    if (repositoryNode.getObject().getStatusCode() != null) {
                        return repositoryNode.getObject().getStatusCode();
                    } else {
                        return null;
                    }
                default:
                    return null;
                }
            }

            public void addListener(ILabelProviderListener listener) {
            }

            public void dispose() {
            }

            public boolean isLabelProperty(Object element, String property) {
                return false;
            }

            public void removeListener(ILabelProviderListener listener) {
            }
        });

        MenuManager menuMgr = new MenuManager("#PopUp"); //$NON-NLS-1$
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {

            public void menuAboutToShow(IMenuManager mgr) {
                ISelection selection = tableViewer.getSelection();
                if (selection instanceof IStructuredSelection) {
                    IStructuredSelection structuredSelection = (IStructuredSelection) selection;

                    List<ITreeContextualAction> contextualsActions = ActionsHelper.getRepositoryContextualsActions();
                    for (ITreeContextualAction action : contextualsActions) {
                        if (action.isReadAction() || action.isEditAction() || action.isPropertiesAction()) {
                            action.init(null, structuredSelection);
                            if (action.isVisible()) {
                                mgr.add(action);
                            }
                        }
                    }
                }
            }
        });
        Menu menu = menuMgr.createContextMenu(tableViewer.getControl());
        tableViewer.getControl().setMenu(menu);

        Listener sortListener = new Listener() {

            private int direction = 1;

            public void handleEvent(Event e) {
                final TableColumn column = (TableColumn) e.widget;

                if (column == table.getSortColumn()) {
                    direction = -direction;
                }
                if (direction == 1) {
                    table.setSortDirection(SWT.DOWN);
                } else {
                    table.setSortDirection(SWT.UP);
                }

                table.setSortColumn(column);
                tableViewer.setSorter(new ViewerSorter() {

                    int index = 0;

                    @Override
                    public void sort(Viewer viewer, Object[] elements) {
                        while (index < table.getColumns().length && table.getColumn(index) != column) {
                            index++;
                        }
                        super.sort(viewer, elements);
                    }

                    @Override
                    public int compare(Viewer viewer, Object e1, Object e2) {
                        ITableLabelProvider labelProvider = (ITableLabelProvider) tableViewer.getLabelProvider();
                        String columnText = labelProvider.getColumnText(e1, index) != null ? labelProvider.getColumnText(e1,
                                index) : ""; //$NON-NLS-1$
                        String columnText2 = labelProvider.getColumnText(e2, index) != null ? labelProvider.getColumnText(e2,
                                index) : ""; //$NON-NLS-1$
                        return getComparator().compare(columnText, columnText2) * direction;
                    }
                });
            }
        };
        column1.addListener(SWT.Selection, sortListener);
        column2.addListener(SWT.Selection, sortListener);
        column3.addListener(SWT.Selection, sortListener);
        column4.addListener(SWT.Selection, sortListener);
        table.setSortColumn(column1);
        table.setSortDirection(SWT.DOWN);

        aTabbedPropertySheetPage.getSite().setSelectionProvider(this);
    }

    @Override
    public void refresh() {
        if (tableViewer.getContentProvider() != null) {
            if (getObject() != null && getObject().getProperty() != null) {
                tableViewer.setInput(getObject());
            } else {
                tableViewer.setInput(null);
            }
        }
    }

    @Override
    protected void beforeSave() {
    }

    @Override
    protected void enableControl(boolean enable) {
    }

    @Override
    protected void showControl(boolean visible) {
    }

    public void addSelectionChangedListener(ISelectionChangedListener listener) {
    }

    public void removeSelectionChangedListener(ISelectionChangedListener listener) {
    }

    public void setSelection(ISelection selection) {
        System.out.println("set"); //$NON-NLS-1$
    }

    @Override
    public ISelection getSelection() {
        refresh();
        return tableViewer.getSelection();
    }

}
