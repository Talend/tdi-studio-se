// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.sqlbuilder;

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.talend.core.model.metadata.IMetadataConnection;
import org.talend.core.model.metadata.builder.ConvertionHelper;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.process.IElement;
import org.talend.core.sqlbuilder.util.ConnectionParameters;
import org.talend.core.ui.services.ISQLBuilderService;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController;
import org.talend.sqlbuilder.erdiagram.ui.ErDiagramDialog;
import org.talend.sqlbuilder.repository.utility.SQLBuilderRepositoryNodeManager;
import org.talend.sqlbuilder.ui.OpenSQLBuilderDialogJob;
import org.talend.sqlbuilder.ui.SQLBuilderDialog;
import org.talend.sqlbuilder.util.UIUtils;

/**
 * DOC nrousseau class global comment. Detailled comment
 */
public class SQLBuilderService implements ISQLBuilderService {

    public void closeIfSQLBuilderDialog(Object shellData) {
        if (shellData instanceof SQLBuilderDialog) {
            ((SQLBuilderDialog) shellData).close();
        } else if (shellData instanceof ErDiagramDialog) {
            ((ErDiagramDialog) shellData).close();
        }
    }

    public void openSQLBuilderDialog(ConnectionParameters connParameters, Composite composite, IElement elem,
            String propertyName, CommandStack commandStack, Object abstractElementPropertySectionController,
            Object abstractMultiPageTalendEditor) {
        OpenSQLBuilderDialogJob openDialogJob = new OpenSQLBuilderDialogJob(connParameters, composite, elem, propertyName,
                commandStack, (AbstractElementPropertySectionController) abstractElementPropertySectionController);
        openDialogJob.schedule();
    }

    public Dialog openSQLBuilderDialog(Shell parentShell, String processName, ConnectionParameters connParameters) {
        SQLBuilderDialog sqlBuilder = new SQLBuilderDialog(parentShell);
        UIUtils.addSqlBuilderDialog(processName, sqlBuilder);
        sqlBuilder.setConnParameters(connParameters);
        // display a error message if the db connection is failed.
        if (connParameters != null) {
            DatabaseConnection connection = createConnection(connParameters);
            if (connection != null) {
                IMetadataConnection metadataConnection = ConvertionHelper.convert(connection);
                metadataConnection.setAdditionalParams(ConvertionHelper.convertAdditionalParameters(connection));
                UIUtils.checkConnection(parentShell, metadataConnection);
            }
        }
        return sqlBuilder;
    }

    public void updateSqlBuilderDialogTitle(String labelText, String name, String uniqueName) {
        UIUtils.updateSqlBuilderDialogTitle(labelText, name, uniqueName);
    }

    public void closeSqlBuilderDialogs(String name) {
        UIUtils.closeSqlBuilderDialogs(name);
    }

    public DatabaseConnection createConnection(ConnectionParameters parameters) {
        SQLBuilderRepositoryNodeManager manager = new SQLBuilderRepositoryNodeManager();
        return manager.createConnection(parameters);
    }

}
