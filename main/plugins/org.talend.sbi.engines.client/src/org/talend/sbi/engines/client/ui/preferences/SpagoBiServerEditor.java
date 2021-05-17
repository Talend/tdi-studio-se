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
package org.talend.sbi.engines.client.ui.preferences;

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
import org.talend.sbi.engines.client.Activator;
import org.talend.sbi.engines.client.i18n.Messages;
import org.talend.sbi.engines.client.ui.wizards.SpagoBiServerHelper;

/**
 * DOC cantoine class global comment. Detailled comment <br/>
 *
 * $Id: SpagoBiServerEditor.java 2738 2007-04-27 13:12:27Z cantoine $
 *
 */
public class SpagoBiServerEditor extends TableEditor {

    /**
     * DOC tguiu SpagoBiServerEditor constructor comment.
     *
     * @param name
     * @param labelText
     * @param parent
     */
    public SpagoBiServerEditor(String name, String labelText, Composite parent) {
        super(name, labelText, parent);
    }

    @Override
    protected Table createTable(Composite parent) {

        Table contextTable = new Table(parent, SWT.BORDER | SWT.SINGLE);
        contextTable.setLinesVisible(true);
        contextTable.setHeaderVisible(true);

        TableColumn engineName = new TableColumn(contextTable, SWT.NONE);
        engineName.setText(Messages.getString("SpagoBiServerEditor.engineColumnTitle")); //$NON-NLS-1$
        engineName.setWidth(150);

        TableColumn shortDescription = new TableColumn(contextTable, SWT.NONE);
        shortDescription.setText(Messages.getString("SpagoBiServerEditor.descriptionColumnTitle")); //$NON-NLS-1$
        shortDescription.setWidth(150);

        TableColumn host = new TableColumn(contextTable, SWT.NONE);
        host.setText(Messages.getString("SpagoBiServerEditor.hostColumnTitle")); //$NON-NLS-1$
        host.setWidth(150);

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
                    return SpagoBiServerHelper.getEngineName(value);
                }
                if (columnIndex == 1) {
                    return SpagoBiServerHelper.getShortDescription(value);
                }
                if (columnIndex == 2) {
                    return SpagoBiServerHelper.getHost(value);
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
        return SpagoBiServerHelper.writeString(items);
    }

    @Override
    protected String getNewInputObject() {
        Shell shell = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
        SpagoBiServerDialog dialog = new SpagoBiServerDialog(shell, computeCodeList());
        if (dialog.open() == Window.OK) {
            return SpagoBiServerHelper.getString(dialog.getEngineName(), dialog.getShortDescription(),
                    dialog.getHost(), dialog.getPort(), dialog.getLogin(), dialog.getPassword());
        }
        return null;
    }

    @Override
    protected String getExistingInputObject(String obj) {
        Shell shell = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
        // SpagoBiServerDialog dialog = new SpagoBiServerDialog(shell, computeCodeList(),
        // SpagoBiServerHelper.getEngineName(obj),
        // SpagoBiServerHelper.getShortDescription(obj), SpagoBiServerHelper.getHost(obj),
        // SpagoBiServerHelper.getPort(obj),
        // SpagoBiServerHelper.getLogin(obj), SpagoBiServerHelper.getPassword(obj), SpagoBiServerHelper
        // .getApplicationContext(obj));
        SpagoBiServerDialog dialog = new SpagoBiServerDialog(shell, computeCodeList(), SpagoBiServerHelper
                .getEngineName(obj), SpagoBiServerHelper.getShortDescription(obj), SpagoBiServerHelper.getHost(obj),
                SpagoBiServerHelper.getPort(obj), SpagoBiServerHelper.getLogin(obj), SpagoBiServerHelper
                        .getPassword(obj));

        if (dialog.open() == Window.OK) {
            return SpagoBiServerHelper.getString(dialog.getEngineName(), dialog.getShortDescription(),
                    dialog.getHost(), dialog.getPort(), dialog.getLogin(), dialog.getPassword());
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
            result.add(SpagoBiServerHelper.getEngineName(tmp));
        }
        return result;
    }

    @Override
    protected List<String> readString(String stringList) {
        return SpagoBiServerHelper.readString(stringList);
    }
}
