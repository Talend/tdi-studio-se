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
import org.talend.core.model.business.BusinessAlignment;
import org.talend.designer.business.model.business.BusinessItem;

/**
 * wchen class global comment. Detailled comment
 */
public class ChangeBusinessItemAlignmentCommand extends AbstractTransactionalCommand {

    private BusinessItem item;

    private BusinessAlignment alignment;

    private BusinessAlignment alignmentGroup;

    /**
     *wchen ChangeBusinessItemAlignmentCommand constructor comment.
     *
     * @param domain
     * @param label
     * @param affectedFiles
     */
    public ChangeBusinessItemAlignmentCommand(BusinessItem businessItem, BusinessAlignment alignment,
            BusinessAlignment alignmentGroup) {
        super(TransactionUtil.getEditingDomain(businessItem), null, null);
        this.item = businessItem;
        this.alignment = alignment;
        this.alignmentGroup = alignmentGroup;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand#doExecuteWithResult(org.eclipse
     * .core.runtime.IProgressMonitor, org.eclipse.core.runtime.IAdaptable)
     */
    @Override
    protected CommandResult doExecuteWithResult(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
        if (BusinessAlignment.VERTICAL.equals(alignmentGroup)) {
            item.setVAlignment(this.alignment.toString());
        } else if (BusinessAlignment.HORIZONTAL.equals(alignmentGroup)) {
            item.setHAlignment(this.alignment.toString());
        }

        return CommandResult.newOKCommandResult();
    }
}
