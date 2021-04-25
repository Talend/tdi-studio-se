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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.commands.Command;
import org.talend.sqlbuilder.erdiagram.ui.nodes.Column;
import org.talend.sqlbuilder.erdiagram.ui.nodes.ErDiagram;
import org.talend.sqlbuilder.erdiagram.ui.nodes.Relation;
import org.talend.sqlbuilder.erdiagram.ui.nodes.Table;

/**
 * DOC qzhang class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 *
 */
public class DeleteTableCommand extends Command {

    private ErDiagram erDiagram;

    private List<Table> tables;

    private List<Column> columns;

    /**
     * DOC admin DeleteTableCommand constructor comment.
     */
    public DeleteTableCommand(ErDiagram erDiagram, List<Table> tables) {
        this.erDiagram = erDiagram;
        this.tables = tables;
        columns = new ArrayList<Column>();
        for (Table table : tables) {
            for (Object obj : table.getColumns()) {
                if (obj instanceof Column) {
                    if (!((Column) obj).getElementName().equals("*")) { //$NON-NLS-1$
                        columns.add((Column) obj);
                    }
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.commands.Command#execute()
     */
    @Override
    public void execute() {
        for (Table table : this.tables) {
            erDiagram.removeTable(table);
            for (Object obj : table.getColumns()) {
                if (obj instanceof Column) {
                    List<Relation> inputs = ((Column) obj).getInputs();
                    List<Relation> outputs = ((Column) obj).getOutputs();
                    for (Relation relation : inputs) {
                        Column preColumn = relation.getSource();
                        if (!columns.contains(preColumn)) {
                            preColumn.removeOutputRelation(relation);
                        }
                    }
                    for (Relation relation : outputs) {
                        Column preColumn = relation.getTarget();
                        if (!columns.contains(preColumn)) {
                            preColumn.removeInputRelation(relation);
                        }
                    }
                }

            }
        }
    }
}
