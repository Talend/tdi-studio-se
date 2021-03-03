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
package org.talend.designer.business.diagram.custom.commands;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand;
import org.talend.designer.business.model.business.TalendItem;

public class ChangeTalendItemLabelCommand extends AbstractTransactionalCommand {

    private TalendItem talendItem;

    private String label;

    public ChangeTalendItemLabelCommand(TalendItem talendItem, String label) {
        super(TransactionUtil.getEditingDomain(talendItem), null, null);
        this.talendItem = talendItem;
        this.label = label;
    }

    @Override
    protected CommandResult doExecuteWithResult(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
        talendItem.setLabel(label);

        return CommandResult.newOKCommandResult();
    }
}
