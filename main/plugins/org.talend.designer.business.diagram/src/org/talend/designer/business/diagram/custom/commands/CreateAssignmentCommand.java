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

import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand;
import org.talend.designer.business.diagram.custom.util.RepositoryHelper;
import org.talend.designer.business.diagram.i18n.Messages;
import org.talend.designer.business.model.business.BusinessAssignment;
import org.talend.designer.business.model.business.BusinessFactory;
import org.talend.designer.business.model.business.BusinessItem;
import org.talend.designer.business.model.business.Repository;
import org.talend.designer.business.model.business.TalendItem;

public class CreateAssignmentCommand extends AbstractTransactionalCommand {

    List items;

    private BusinessItem businessItem;

    /**
     * DOC mhelleboid createAssignmentCommand constructor comment.
     *
     * @param domain
     * @param label
     * @param affectedFiles
     */
    public CreateAssignmentCommand(TransactionalEditingDomain domain) {
        super(domain, Messages.getString("CreateAssignmentCommand.CreateAssignement"), null); //$NON-NLS-1$
    }

    /**
     * DOC mhelleboid Comment method "setBusinessItem".
     *
     * @param businessItem
     */
    public void setBusinessItem(BusinessItem businessItem) {
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
        // PTODO mhelleboid check if assignment already exist

        RepositoryHelper repositoryHelper = new RepositoryHelper();
        Repository repository = businessItem.getBusinessProcess().getLocalRepositoryCopy();
        TalendItem talendItem = null;
        if (repository == null) {
            repository = repositoryHelper.createLocalRepositoryCopy(businessItem.getBusinessProcess());
        }
        for (Object item : getItems()) {
            BusinessAssignment assignment = BusinessFactory.eINSTANCE.createBusinessAssignment();
            talendItem = repositoryHelper.getTalendItem(repository, item);
            if (talendItem == null) {
                talendItem = repositoryHelper.createTalendItem(repository, item);
            }

            if (talendItem == null) {
                return CommandResult.newErrorCommandResult(Messages.getString("CreateAssignmentCommand.CannotAssign")); //$NON-NLS-1$
            }
            assignment.setBusinessItem(businessItem);
            assignment.setTalendItem(talendItem);

        }

        return CommandResult.newOKCommandResult();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.core.commands.operations.AbstractOperation#canExecute()
     */
    @Override
    public boolean canExecute() {
        // PTODO mhelleboid
        // RepositoryHelper repositoryHelper = new RepositoryHelper();
        // TalendItem talendItem = repositoryHelper.createTalendItem(item);
        // if (talendItem == null)
        // {
        // return false;
        // }
        return true;
    }

    public List getItems() {
        return this.items;
    }

    public void setItems(List items) {
        this.items = items;
    }

}
