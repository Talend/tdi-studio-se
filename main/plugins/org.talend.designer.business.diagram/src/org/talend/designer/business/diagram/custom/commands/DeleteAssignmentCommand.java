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

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.talend.designer.business.model.business.BusinessItem;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class DeleteAssignmentCommand extends AbstractTransactionalCommand {

    private ISelection selection;

    private BusinessItem businessItem;

    /**
     * DOC Administrator DeleteAssignmentCommand constructor comment.
     *
     * @param domain
     * @param label
     * @param affectedFiles
     */
    public DeleteAssignmentCommand(BusinessItem businessItem, ISelection selection) {
        super(TransactionUtil.getEditingDomain(businessItem), null, null);
        this.selection = selection;
        this.businessItem = businessItem;
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
        List assignements = businessItem.getAssignments();
        Object firstElement = null;
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection iSelection = (IStructuredSelection) selection;
            firstElement = iSelection.getFirstElement();
        }
        Iterator iterator = assignements.iterator();
        while (iterator.hasNext()) {
            Object assignment = iterator.next();
            if (firstElement == assignment) {
                iterator.remove();
            }
            // if (assignment instanceof BusinessAssignment) {
            // TalendItem item = ((BusinessAssignment) assignment).getTalendItem();
            // if (item != null) {
            // if (item.getId().equals(node.getId())) {
            // iterator.remove();
            // } else if (item instanceof SQLPattern || item instanceof Routine || item instanceof TableMetadata
            // || item instanceof Query || item instanceof SapFunctionMetadata) {
            // if (item.getLabel().equals(node.getProperties(EProperties.LABEL))) {
            // iterator.remove();
            // }
            //
            // }
            // }
            //
            // }
        }

        return CommandResult.newOKCommandResult();

    }

}
