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
package org.talend.repository.ui.properties;

import org.eclipse.jface.viewers.IFilter;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.IRepositoryNode.ENodeType;

/**
 * DOC mhelleboid class global comment. Detailled comment <br/>
 *
 * $Id: SectionFilter.java 295 2006-11-02 08:28:03 +0000 (jeu., 02 nov. 2006) smallet $
 *
 */
public class ExcludeFolderSectionFilter extends SectionFilter implements IFilter {

    public boolean select(Object object) {
        RepositoryNode node = getRepositoryNode(object);

        if (node == null) {
            return false;
        }

        if (node.getType() == ENodeType.STABLE_SYSTEM_FOLDER || node.getType() == ENodeType.SYSTEM_FOLDER
                || node.getType() == ENodeType.SIMPLE_FOLDER) {
            return false;
        }

        return true;
    }

}
