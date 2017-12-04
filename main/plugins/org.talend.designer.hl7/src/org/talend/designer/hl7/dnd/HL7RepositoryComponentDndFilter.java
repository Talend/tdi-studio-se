// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.hl7.dnd;

import org.eclipse.emf.common.util.EList;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.metadata.builder.connection.HL7Connection;
import org.talend.core.model.properties.HL7ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.DefaultRepositoryComponentDndFilter;
import org.talend.repository.model.RepositoryNode;

/**
 * ggu class global comment. Detailled comment
 */
public class HL7RepositoryComponentDndFilter extends DefaultRepositoryComponentDndFilter {

    private static final String HL7_INPUT = "tHL7Input"; //$NON-NLS-1$

    private static final String HL7_OUTPUT = "tHL7Output"; //$NON-NLS-1$

    public HL7RepositoryComponentDndFilter() {
    }

    // add for bug TDI-19994
    @Override
    public boolean except(Item item, ERepositoryObjectType type, RepositoryNode seletetedNode, IComponent component,
            String repositoryType) {

        if (!(item instanceof HL7ConnectionItem) || component == null || repositoryType == null) {
            return false;
        }

        return !valid(item, type, seletetedNode, component, repositoryType);

    }

    @Override
    public boolean valid(Item item, ERepositoryObjectType type, RepositoryNode seletetedNode, IComponent component,
            String repositoryType) {
        if (!(item instanceof HL7ConnectionItem) || component == null || repositoryType == null) {
            return false;
        }
        boolean hl7Related = false;
        boolean hl7Output = false;
        if (item instanceof HL7ConnectionItem) {
            hl7Related = true;
            EList list = ((HL7Connection) ((HL7ConnectionItem) item).getConnection()).getRoot();
            if (list != null && list.size() > 0) {
                hl7Output = true;
            }
        }
        String componentProductname = component.getRepositoryType();
        boolean filter = true;
        if (hl7Output && !component.getName().equals(HL7_OUTPUT)) {
            filter = false;
        } else if (hl7Related && !hl7Output && !component.getName().equals(HL7_INPUT)) {// bug15632
            filter = false;
        }
        if (componentProductname != null && repositoryType.endsWith(componentProductname) && filter
                && validSub(item, type, seletetedNode, component, repositoryType)) {
            return true;
        }

        return false;
    }

}
