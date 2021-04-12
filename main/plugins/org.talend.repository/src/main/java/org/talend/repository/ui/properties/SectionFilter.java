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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IFilter;
import org.talend.repository.model.RepositoryNode;

/**
 * DOC mhelleboid class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class SectionFilter implements IFilter {

    public boolean select(Object object) {
        return getRepositoryNode(object) != null;
    }

    public static RepositoryNode getRepositoryNode(Object object) {
        if (object instanceof RepositoryNode) {
            return (RepositoryNode) object;
        }

        if (object instanceof IAdaptable) {
            // see ProcessPart.getAdapter()
            IAdaptable adaptable = (IAdaptable) object;
            Object adapter = adaptable.getAdapter(RepositoryNode.class);
            if (adapter != null) {
                return (RepositoryNode) adapter;
            }
        }

        return null;
    }

}
