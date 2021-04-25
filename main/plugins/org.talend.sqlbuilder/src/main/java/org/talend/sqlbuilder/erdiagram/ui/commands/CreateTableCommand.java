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
package org.talend.sqlbuilder.erdiagram.ui.commands;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.sqlbuilder.erdiagram.ui.nodes.ErDiagram;
import org.talend.sqlbuilder.erdiagram.ui.nodes.Table;
import org.talend.sqlbuilder.repository.utility.EMFRepositoryNodeManager;
import org.talend.sqlbuilder.ui.ISQLBuilderDialog;
import org.talend.sqlbuilder.util.UIUtils;

/**
 * DOC qzhang class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 *
 */
public class CreateTableCommand extends Command {

    protected List<MetadataTable> metaTables;

    private List<Table> tables;

    private ErDiagram erDiagram;

    /**
     * DOC admin CreateTableCommand constructor comment.
     */
    public CreateTableCommand(ErDiagram erDiagram, List<MetadataTable> tables) {
        this.metaTables = tables;
        this.erDiagram = erDiagram;
        this.tables = erDiagram.getTables();
        setLabel("CreateTableCommand"); //$NON-NLS-1$
        setTableNames();
    }

    private List<String> tableNames = new ArrayList<String>();

    private void setTableNames() {
        for (Table table : tables) {
            tableNames.add(table.getElementName());
        }
    }

    private List<String[]> fks;

    @Override
    public void execute() {
        for (MetadataTable metadataTable : metaTables) {
            if (!tableNames.contains(metadataTable.getSourceName())) {
                Table table = new Table();
                table.setMetadataTable(metadataTable, null);
                table.setErDiagram(erDiagram);
                erDiagram.addTable(table);
                erDiagram.getMetadataTables().add(metadataTable);
            }
        }
        IRunnableWithProgress progress = new IRunnableWithProgress() {

            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                monitor.beginTask("", IProgressMonitor.UNKNOWN); //$NON-NLS-1$
                try {
                    fks = EMFRepositoryNodeManager.getInstance().getPKFromTables(erDiagram.getMetadataTables(),
                            erDiagram.getErDiagramComposite().getDialog().getSelectedContext());
                } finally {
                    monitor.done();
                }
            }
        };
        ISQLBuilderDialog dialog = erDiagram.getErDiagramComposite().getDialog();
        UIUtils.runWithProgress(progress, true, dialog.getProgressMonitor(), dialog.getShell());
        erDiagram.setRelations(fks);
    }

    @Override
    public void redo() {
        super.redo();
    }

    @Override
    public void undo() {
        super.undo();
    }

}
