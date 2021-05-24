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

import org.eclipse.gef.commands.Command;
import org.talend.sqlbuilder.erdiagram.ui.nodes.Column;
import org.talend.sqlbuilder.erdiagram.ui.nodes.Relation;

/**
 * DOC qzhang class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 *
 */
public class RelationReconnectionCommand extends Command {

    private Column newSource;

    private Column newTarget;

    private Column oldSource;

    private Column oldTarget;

    public RelationReconnectionCommand(Relation relation) {

    }

    public Column getNewSource() {
        return newSource;
    }

    public void setNewSource(Column newSource) {
        this.newSource = newSource;
    }

    public Column getNewTarget() {
        return newTarget;
    }

    public void setNewTarget(Column newTarget) {
        this.newTarget = newTarget;
    }

    public Column getOldSource() {
        return oldSource;
    }

    public void setOldSource(Column oldSource) {
        this.oldSource = oldSource;
    }

    public Column getOldTarget() {
        return oldTarget;
    }

    public void setOldTarget(Column oldTarget) {
        this.oldTarget = oldTarget;
    }
}
