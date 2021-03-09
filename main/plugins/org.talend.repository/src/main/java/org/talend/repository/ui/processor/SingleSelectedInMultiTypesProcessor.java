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
package org.talend.repository.ui.processor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.Viewer;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.model.RepositoryNode;

/**
 * This type processor is used to handle this case, when multiple types require to display and only one item is
 * selected. Created by Marvin Wang on Apr 19, 2013.
 */
public class SingleSelectedInMultiTypesProcessor extends SingleTypeProcessor {

    private List<ERepositoryObjectType> repObjectTypes;

    public SingleSelectedInMultiTypesProcessor(String processId) {
        this(processId, new ArrayList<ERepositoryObjectType>());
    }

    public SingleSelectedInMultiTypesProcessor(String processId, List<ERepositoryObjectType> repObjectTypes) {
        super(processId);
        this.repObjectTypes = repObjectTypes;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.ui.processor.MultiTypesProcessor#getTypes()
     */
    @Override
    protected List<ERepositoryObjectType> getTypes() {
        return repObjectTypes;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.ui.processor.SingleTypeProcessor#getType()
     */
    @Override
    protected ERepositoryObjectType getType() {
        return null;
    }

    @Override
    public boolean isSelectionValid(RepositoryNode node) {
        String repNodeId = node.getId();
        if (repNodeId != null) {
            if (repositoryTypes != null && repositoryTypes.length > 0) {
                for (String repositoryType : repositoryTypes) {
                    if (repNodeId.equals(repositoryType)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    protected boolean selectRepositoryNode(Viewer viewer, RepositoryNode parentNode, RepositoryNode node) {
        // Do nothing here. If required, add the appriate logic. At present, show all nodes.
        return true;
    }

}
