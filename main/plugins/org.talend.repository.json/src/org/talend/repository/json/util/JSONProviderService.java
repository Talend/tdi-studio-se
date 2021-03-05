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
package org.talend.repository.json.util;

import java.util.Map;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.ui.IWorkbench;
import org.talend.core.model.process.INode;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.ui.IJSONProviderService;
import org.talend.repository.json.node.JSONRepositoryNodeType;
import org.talend.repository.model.RepositoryNode;

/**
 * DOC wanghong class global comment. Detailled comment
 */
public class JSONProviderService implements IJSONProviderService {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.IProviderService#newWizard(org.eclipse.ui.IWorkbench, boolean,
     * org.talend.repository.model.RepositoryNode, java.lang.String[])
     */
    @Override
    public IWizard newWizard(IWorkbench workbench, boolean creation, RepositoryNode node, String[] existingNames) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.ui.IJSONProviderService#isJSONNode(org.talend.core.model.process.INode)
     */
    @Override
    public boolean isJSONNode(INode node) {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.ui.IJSONProviderService#isRepositorySchemaLine(org.talend.core.model.process.INode,
     * java.util.Map)
     */
    @Override
    public boolean isRepositorySchemaLine(INode node, Map<String, Object> lineValue) {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.ui.IJSONProviderService#getJSONRepositoryType()
     */
    @Override
    public ERepositoryObjectType getJSONRepositoryType() {
        return JSONRepositoryNodeType.JSON;
    }

}
