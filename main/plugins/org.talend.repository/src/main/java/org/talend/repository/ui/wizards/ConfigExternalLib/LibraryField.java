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
package org.talend.repository.ui.wizards.ConfigExternalLib;

import java.util.List;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.properties.RoutinesJarType;
import org.talend.designer.core.model.utils.emf.component.IMPORTType;
import org.talend.repository.i18n.Messages;

/**
 * DOC tguiu class global comment. Detailled comment <br/>
 *
 * $Id: StatusEditor.java 1937 2007-02-09 06:34:36 +0000 (Fri, 09 Feb 2007) bqian $
 *
 */
public class LibraryField extends TableField {

    private boolean readOnly;

    /**
     * DOC tguiu StatusEditor constructor comment.
     *
     * @param name
     * @param labelText
     * @param parent
     */
    public LibraryField(String name, Composite parent) {
        super(name, parent);
    }

    public LibraryField(String name, Composite parent, boolean isReadOnly) {
        super(name, parent, isReadOnly);
        this.readOnly = isReadOnly;
    }

    @Override
    protected Table createTable(Composite parent) {
        Table contextTable = new Table(parent, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION);
        contextTable.setLinesVisible(false);
        contextTable.setHeaderVisible(true);

        TableColumn moduleNameColumn = new TableColumn(contextTable, SWT.LEFT);
        moduleNameColumn.setText(Messages.getString("LibraryField.moduleColumn")); //$NON-NLS-1$
        moduleNameColumn.setWidth(120);

        TableColumn colName = new TableColumn(contextTable, SWT.NONE);
        colName.setText(Messages.getString("LibraryField.requiredColumn")); //$NON-NLS-1$
        colName.setWidth(70);

        TableColumn moduleUriColumn = new TableColumn(contextTable, SWT.LEFT);
        moduleUriColumn.setText(Messages.getString("LibraryField.mavenUriColumn")); //$NON-NLS-1$
        moduleUriColumn.setWidth(300);

        TableColumn descriptionColumn = new TableColumn(contextTable, SWT.NONE);
        descriptionColumn.setText(Messages.getString("LibraryField.descriptionColumn")); //$NON-NLS-1$
        descriptionColumn.setWidth(200);

        contextTable.addListener(SWT.MouseUp, new Listener() {

            @Override
            public void handleEvent(Event event) {
                if (!readOnly) {
                    final Table contextTable = (Table) event.widget;
                    TableItem[] selection = contextTable.getSelection();
                    if (selection.length > 0) {
                        final TableItem item = selection[0];
                        if (item.getBounds(1).contains(event.x, event.y)) {
                            IMPORTType it = (IMPORTType) getList().get(contextTable.getSelectionIndex());
                            if (it.eContainer() instanceof RoutineItem || it.eContainer() instanceof RoutinesJarType) {
                                it.setREQUIRED(!it.isREQUIRED());
                                setInput(getList());
                            }
                        }
                    }
                }
            }
        });
        return contextTable;
    }

    @Override
    protected IStructuredContentProvider createContentProvider() {
        return new IStructuredContentProvider() {

            @Override
            public Object[] getElements(Object inputElement) {
                return ((List) inputElement).toArray();
            }

            @Override
            public void dispose() {
            }

            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            }

        };
    }

    @Override
    protected ITableLabelProvider createLabelProvider() {
        return new ITableLabelProvider() {

            @Override
            public Image getColumnImage(Object element, int columnIndex) {
                if (element instanceof IMPORTType) {
                    IMPORTType type = (IMPORTType) element;
                    if (columnIndex == 1) {
                        if (type.isREQUIRED()) {
                            return ImageProvider.getImage(ECoreImage.MODULE_REQUIRED_ICON);
                        } else {
                            return ImageProvider.getImage(ECoreImage.MODULE_NOTREQUIRED_ICON);
                        }
                    }
                }
                return null;
            }

            @Override
            public String getColumnText(Object element, int columnIndex) {
                if (element instanceof IMPORTType) {
                    IMPORTType type = (IMPORTType) element;
                    switch (columnIndex) {
                    case 0:
                        return type.getMODULE();
                    case 2:
                        return type.getMVN();
                    case 3:
                        return type.getMESSAGE();
                    default:
                        return ""; //$NON-NLS-1$
                    }
                }
                return ""; //$NON-NLS-1$
            }

            @Override
            public void addListener(ILabelProviderListener listener) {
            }

            @Override
            public void dispose() {
            }

            @Override
            public boolean isLabelProperty(Object element, String property) {
                return false;
            }

            @Override
            public void removeListener(ILabelProviderListener listener) {
            }
        };
    }

    @Override
    protected List getNewInputObject() {
        return null;
    }
}
