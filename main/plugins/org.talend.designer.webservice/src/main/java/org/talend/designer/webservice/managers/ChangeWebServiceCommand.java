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
package org.talend.designer.webservice.managers;

import org.eclipse.gef.commands.Command;
import org.talend.core.model.process.INode;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class ChangeWebServiceCommand extends Command {

    private INode node;

    public ChangeWebServiceCommand(INode node) {
        this.node = node;
    }

    @Override
    public void execute() {
        // TODO Auto-generated method stub
        super.execute();
    }

}
