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
package org.talend.sqlbuilder.dataset.dataset;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.actions.ActionGroup;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.SqlBuilderPlugin;
import org.talend.sqlbuilder.dataset.actions.AbstractDataSetTableContextAction;
import org.talend.sqlbuilder.dataset.actions.CopyTableAction;

/**
 * ActionGroup for DataSetTable. This group controls what context
 * menu actions are being shown.
 *
 * @author Davy Vanherbergen
 */
public class DataSetTableActionGroup extends ActionGroup {

    private Table ptable;

    private TableCursor pcursor;

    private CopyTableAction pcopyTableAction;

    /**
     * Construct a new action group for a given Table.
     *
     * @param table Table that displays the context menu
     * @param cursor TableCursor that displays the context menu
     */
    public DataSetTableActionGroup(Table table, TableCursor cursor) {
        ptable = table;
        pcursor = cursor;

        pcopyTableAction = new CopyTableAction();
        pcopyTableAction.setTable(ptable);
        pcopyTableAction.setTableCursor(pcursor);
    }


    /**
     * Fill the context menu with all the correct actions.
     * @see org.eclipse.ui.actions.ActionGroup#fillContextMenu(org.eclipse.jface.action.IMenuManager)
     */
    public void fillContextMenu(IMenuManager menu) {

        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint point = registry.getExtensionPoint("net.sourceforge.sqlexplorer", "dataSetTableContextAction"); //$NON-NLS-1$ //$NON-NLS-2$
        IExtension[] extensions = point.getExtensions();

        // add basic actions

        for (int i = 0; i < extensions.length; i++) {

            IExtension e = extensions[i];

            IConfigurationElement[] ces = e.getConfigurationElements();

            for (int j = 0; j < ces.length; j++) {
                try {

                    String group = ces[j].getAttribute("group"); //$NON-NLS-1$
                    if (group == null || !group.equalsIgnoreCase("export")) { //$NON-NLS-1$

                        // check if the action thinks it is suitable..
                        AbstractDataSetTableContextAction action = (AbstractDataSetTableContextAction) ces[j]
                                .createExecutableExtension("class"); //$NON-NLS-1$
                        action.setTable(ptable);
                        action.setTableCursor(pcursor);
                        if (action.isAvailable()) {
                            menu.add(action);
                        }
                    }

                } catch (Throwable ex) {
                    SqlBuilderPlugin.log(Messages.getString("DataSetTableActionGroup.logMessage1"), ex); //$NON-NLS-1$
                }
            }
        }

        menu.add(new Separator());

        // add export options


        MenuManager subMenu = new MenuManager(Messages.getString("DataSetTable.Actions.ExportSubMenu"));     //$NON-NLS-1$

        for (int i = 0; i < extensions.length; i++) {

            IExtension e = extensions[i];

            IConfigurationElement[] ces = e.getConfigurationElements();

            for (int j = 0; j < ces.length; j++) {
                try {

                    String group = ces[j].getAttribute("group"); //$NON-NLS-1$
                    if (group != null && group.equalsIgnoreCase("export")) { //$NON-NLS-1$

                        // check if the action thinks it is suitable..
                        AbstractDataSetTableContextAction action = (AbstractDataSetTableContextAction) ces[j]
                                .createExecutableExtension("class"); //$NON-NLS-1$
                        action.setTable(ptable);
                        action.setTableCursor(pcursor);
                        if (action.isAvailable()) {
                            subMenu.add(action);
                        }
                    }

                } catch (Throwable ex) {
                    SqlBuilderPlugin.log(Messages.getString("DataSetTableActionGroup.logMessage1"), ex); //$NON-NLS-1$
                }
            }
        }

        menu.add(subMenu);

        menu.add(new Separator());
        menu.add(pcopyTableAction);
    }


}
