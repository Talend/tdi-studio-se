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
package org.talend.repository.ui.actions.importexport;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.core.repository.model.ILocalRepositoryFactory;
import org.talend.core.repository.model.IRepositoryFactory;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.i18n.Messages;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ven., 29 sept. 2006) nrousseau $
 *
 */
public class ReloadSystemRoutinesAction extends Action {

    private static Logger log = Logger.getLogger(ReloadSystemRoutinesAction.class);

    public ReloadSystemRoutinesAction() {
        super();
        this.setActionDefinitionId("reloadSystemRoutines"); //$NON-NLS-1$
    }

    @Override
    public void run() {
        try {
            IRepositoryFactory factory = ProxyRepositoryFactory.getInstance().getRepositoryFactoryFromProvider();
            if (factory instanceof ILocalRepositoryFactory) {
                ((ILocalRepositoryFactory) factory).synchronizeRoutines(null);
                // TDI-21143 : Studio repository view : remove all refresh call to repo view
                // IRepositoryView viewPart = RepositoryManagerHelper.findRepositoryView();
                // if (viewPart != null) {
                // viewPart.refresh(ERepositoryObjectType.ROUTINES);
                // }
                log.info(Messages.getString("ReloadSystemRoutinesAction.logInfo.sysRoutinesSuccessfullyReloaded")); //$NON-NLS-1$
            }
        } catch (PersistenceException e) {
            MessageBoxExceptionHandler.process(e);
        }
    }

}
