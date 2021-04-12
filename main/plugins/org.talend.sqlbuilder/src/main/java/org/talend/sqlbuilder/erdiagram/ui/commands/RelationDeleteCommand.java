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

import java.util.List;

import org.eclipse.gef.commands.Command;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.erdiagram.ui.nodes.Relation;

/**
 * DOC qzhang class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 *
 */
public class RelationDeleteCommand extends Command {

    private List<Relation> relations;

    public RelationDeleteCommand(List<Relation> relations) {
        this.relations = relations;
        setLabel(Messages.getString("RelationDeleteCommand.label")); //$NON-NLS-1$
    }

    public void execute() {
        for (Relation rel : relations) {
            rel.disconnect();
        }
    }
}
