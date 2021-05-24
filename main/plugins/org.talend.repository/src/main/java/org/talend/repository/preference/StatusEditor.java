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
package org.talend.repository.preference;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.talend.commons.ui.swt.preferences.TableEditor;
import org.talend.core.model.properties.helper.StatusHelper;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * DOC tguiu class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class StatusEditor extends TableEditor {

    /**
     * DOC tguiu StatusEditor constructor comment.
     *
     * @param name
     * @param labelText
     * @param parent
     */
    public StatusEditor(String name, String labelText, Composite parent) {
        super(name, labelText, parent);
    }

    @Override
    protected Table createTable(Composite parent) {
        Table contextTable = new Table(parent, SWT.BORDER | SWT.SINGLE);
        contextTable.setLinesVisible(true);
        contextTable.setHeaderVisible(true);

        TableColumn colName = new TableColumn(contextTable, SWT.NONE);
        colName.setText(Messages.getString("StatusEditor.codeColumnTitle")); //$NON-NLS-1$
        colName.setWidth(60);
        TableColumn colValue = new TableColumn(contextTable, SWT.NONE);
        colValue.setText(Messages.getString("StatusEditor.labelColumnTitle")); //$NON-NLS-1$
        colValue.setWidth(150);
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        if (factory.isUserReadOnlyOnCurrentProject()) {
            parent.setEnabled(false);
        }
        return contextTable;
    }

    @Override
    protected IStructuredContentProvider createContentProvider() {
        return new IStructuredContentProvider() {

            public Object[] getElements(Object inputElement) {
                return ((List) inputElement).toArray();
            }

            public void dispose() {
            }

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            }

        };
    }

    @Override
    protected ITableLabelProvider createLabelProvider() {
        return new ITableLabelProvider() {

            public Image getColumnImage(Object element, int columnIndex) {
                return null;
            }

            public String getColumnText(Object element, int columnIndex) {
                String value = ((String) element);
                if (columnIndex == 0) {
                    return StatusHelper.getCode(value);
                }
                if (columnIndex == 1) {
                    return StatusHelper.getLabel(value);
                }
                throw new IllegalStateException();
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

        };
    }

    @Override
    protected String writeString(List<String> items) {
        return StatusHelper.writeString(items);
    }

    @Override
    protected String getNewInputObject() {
        Shell shell = RepositoryPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
        StatusDialog dialog = new StatusDialog(shell, computeCodeList());
        if (dialog.open() == Window.OK) {
            return StatusHelper.getString(dialog.getCode(), dialog.getLabel());
        }
        return null;
    }

    @Override
    protected String getExistingInputObject(String obj) {
        Shell shell = RepositoryPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
        StatusDialog dialog = new StatusDialog(shell, computeCodeList(), StatusHelper.getCode(obj), StatusHelper.getLabel(obj));
        if (dialog.open() == Window.OK) {
            return StatusHelper.getString(dialog.getCode(), dialog.getLabel());
        }
        return null;
    }

    /**
     * DOC tguiu Comment method "computeCodeList".
     *
     * @return
     */
    private List computeCodeList() {
        List result = new ArrayList();
        for (String tmp : getList()) {
            result.add(StatusHelper.getCode(tmp));
        }
        return result;
    }

    @Override
    protected List<String> readString(String stringList) {
        return StatusHelper.readString(stringList);
    }
}
