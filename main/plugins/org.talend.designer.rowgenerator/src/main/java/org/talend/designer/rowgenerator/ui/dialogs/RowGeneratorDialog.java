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
package org.talend.designer.rowgenerator.ui.dialogs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.talend.core.CorePlugin;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.IConnection;
import org.talend.designer.rowgenerator.RowGenMain;
import org.talend.designer.rowgenerator.managers.UIManager;
import org.talend.designer.rowgenerator.ui.tabs.FunParaTableView2;

/**
 * yzhang class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (忙聵聼忙聹聼盲潞聰, 29 盲鹿聺忙聹聢 2006) nrousseau $
 *
 */
public class RowGeneratorDialog extends Dialog {

    private String title;

    private Rectangle size;

    private Image icon;

    private boolean maximized;

    private RowGenMain rowGenMain;

    private IMetadataTable metadataTable;

    private Set<String> preOutputColumnSet = new HashSet<String>();

    private Map<String, String> changedNameColumns = new HashMap<String, String>();

    /**
     * zhangyi RowGeneratorDialog constructor comment.
     *
     * @param parentShell
     */
    public RowGeneratorDialog(Shell parentShell, RowGenMain rowGenMain) {
        super(parentShell);
        this.rowGenMain = rowGenMain;
        metadataTable = rowGenMain.getRowGenManager().getRowGeneratorComponent().getMetadataList().get(0);
        // record the preColumn
        List<IMetadataColumn> listColumns = metadataTable.getListColumns();
        for (IMetadataColumn preColumn : listColumns) {
            preOutputColumnSet.add(preColumn.getLabel());
        }
        setShellStyle(getShellStyle() | SWT.DIALOG_TRIM | SWT.MIN | SWT.MAX | SWT.APPLICATION_MODAL | SWT.RESIZE);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite panel = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
        layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
        layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        panel.setLayout(layout);
        panel.setLayoutData(new GridData(GridData.FILL_BOTH));
        applyDialogFont(panel);

        rowGenMain.createUI(panel, true);
        rowGenMain.updateTableTitleColumn();
        rowGenMain.updateComponentsSize();

        return panel;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);

        newShell.setText(title);
        newShell.setImage(icon);

        newShell.addListener(SWT.Resize, new Listener() {

            /*
             * (non-Javadoc)
             *
             * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
             */
            public void handleEvent(Event event) {
                rowGenMain.updateTableTitleColumn();
            }
        });

        if (maximized) {
            newShell.setMaximized(true);
        } else {
            newShell.setBounds(size);
        }
    }

    /**
     * Sets the title.
     *
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Sets the size.
     *
     * @param size the size to set
     */
    public void setSize(Rectangle size) {
        this.size = size;
    }

    /**
     * Sets the icon.
     *
     * @param icon the icon to set
     */
    public void setIcon(Image icon) {
        this.icon = icon;
    }

    /**
     * Sets the maximizedSize.
     *
     * @param maximizedSize the maxmimizedSize to set
     */
    public void setMaximized(boolean maximized) {
        this.maximized = maximized;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        UIManager uiManager = rowGenMain.getRowGenManager().getUiManager();
        FunParaTableView2 editor = uiManager.getGeneratorUI().getTabFolderEditors().getParameterEditor();
        editor.notifyOkPressed();
        uiManager.closeRowGenerator(SWT.OK, true);
        rowGenMain.buildExternalData();
        super.okPressed();
        // bug 20749
        String componentName = "";
        IComponent iComponent = rowGenMain.getRowGenManager().getRowGeneratorComponent().getComponent();
        if (iComponent != null && iComponent instanceof IComponent) {
            componentName = iComponent.getName();
        }
        if (!"tRowGenerator".equals(componentName)) {
            // see bug 7471
            List<? extends IConnection> connection = rowGenMain.getRowGenManager().getRowGeneratorComponent()
                    .getOutgoingConnections();
            IConnection curConnection = null;
            for (IConnection conn : connection) {
                IMetadataTable metadataTable = conn.getMetadataTable();
                if (metadataTable != null) {
                    String tabName = metadataTable.getTableName();
                    if (tabName.equals(metadataTable.getTableName())) {
                        curConnection = conn;
                    }
                }
            }
            if (curConnection != null) {
                Set<String> addedColumns = new HashSet<String>();
                changedNameColumns = rowGenMain.getGeneratorUI().getChangedNameColumns();
                for (String changedColName : changedNameColumns.keySet()) {
                    String columnName = changedNameColumns.get(changedColName);
                    if (preOutputColumnSet.contains(columnName)) {
                        preOutputColumnSet.remove(columnName);
                        preOutputColumnSet.add(changedColName);
                    }
                }
                for (IMetadataColumn curColumn : metadataTable.getListColumns()) {
                    if (!(preOutputColumnSet.contains(curColumn.getLabel()))) {
                        addedColumns.add(curColumn.getLabel());
                    }
                }
                CorePlugin.getDefault().getDesignerCoreService()
                        .updateTraceColumnValues(curConnection, changedNameColumns, addedColumns);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
     */
    @Override
    protected void cancelPressed() {
        UIManager uiManager = rowGenMain.getRowGenManager().getUiManager();
        uiManager.closeRowGenerator(SWT.CANCEL, true);
        if (uiManager.getRowGenResponse() == SWT.CANCEL) {
            rowGenMain.buildExternalData();
            super.cancelPressed();
        }

    }

    @Override
    public boolean close() {
        UIManager uiManager = rowGenMain.getRowGenManager().getUiManager();
        if (uiManager.getRowGenResponse() == SWT.NONE) {
            uiManager.closeRowGenerator(SWT.CANCEL, true);
            if (uiManager.getRowGenResponse() == SWT.NONE) {
                return false;
            }
            rowGenMain.buildExternalData();
        }
        boolean returnValue = super.close();
        return returnValue;
    }

}
