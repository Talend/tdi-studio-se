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
package org.talend.repository.ftp;

import java.util.Map;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.talend.core.model.process.INode;
import org.talend.core.model.properties.FTPConnectionItem;
import org.talend.core.services.IFTPProviderService;
import org.talend.repository.ftp.ui.wizards.FTPWizard;
import org.talend.repository.model.RepositoryNode;

/**
 * DOC hwang class global comment. Detailled comment
 */
public class FTPProviderService implements IFTPProviderService {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.ui.IFTPProviderService#getRepositoryItem(org.talend.core.model.process.INode)
     */
    public FTPConnectionItem getRepositoryItem(INode node) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.ui.IFTPProviderService#isFTPNode(org.talend.core.model.process.INode)
     */
    public boolean isFTPNode(INode node) {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.ui.IFTPProviderService#isRepositorySchemaLine(org.talend.core.model.process.INode,
     * java.util.Map)
     */
    public boolean isRepositorySchemaLine(INode node, Map<String, Object> lineValue) {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.ui.IFTPProviderService#newFTPWizard(org.talend.core.ui.IWorkbench, boolean,
     * org.talend.repository.model.RepositoryNode, java.lang.String[])
     */
    public IWizard newWizard(IWorkbench workbench, boolean creation, RepositoryNode node, String[] existingNames) {
        if (node == null) {
            return null;
        }
        if (workbench == null) {
            workbench = PlatformUI.getWorkbench();
        }
        return new FTPWizard(workbench, creation, node, existingNames);
    }

}
