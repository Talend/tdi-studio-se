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
import org.talend.sqlbuilder.erdiagram.ui.nodes.Column;
import org.talend.sqlbuilder.erdiagram.ui.nodes.Relation;

/**
 * DOC qzhang class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 *
 */
public class RelationCreateCommand extends Command {

    protected Relation relation;

    protected Column source;

    protected Column target = null;

    public RelationCreateCommand(Column source) {
        this.source = source;
    }

    public Column getSource() {
        return source;
    }

    public void setSource(Column source) {
        this.source = source;
    }

    public Column getTarget() {
        return target;
    }

    public void setTarget(Column target) {
        this.target = target;
    }

    @Override
    public boolean canExecute() {
        if (target != null) {
            if (source.equals(target) || target.getElementName().equals("*") //$NON-NLS-1$
                    || source.getTable().equals(target.getTable())) {
                return false;
            }
            List<Relation> relations = this.source.getOutputs();
            for (Relation rel : relations) {
                if (rel.getTarget().equals(target)) {
                    return false;
                }
            }
            relations = this.target.getInputs();
            for (Relation rel : relations) {
                if (rel.getSource().equals(source)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void execute() {
        relation = new Relation(source, target);
    }

}
