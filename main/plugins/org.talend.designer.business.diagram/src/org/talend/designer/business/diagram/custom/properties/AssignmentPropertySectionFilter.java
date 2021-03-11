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
package org.talend.designer.business.diagram.custom.properties;

import org.eclipse.jface.viewers.IFilter;
import org.talend.designer.business.diagram.custom.edit.parts.BaseBusinessItemRelationShipEditPart;
import org.talend.designer.business.diagram.custom.edit.parts.BusinessItemShapeEditPart;

/**
 * DOC mhelleboid class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class AssignmentPropertySectionFilter implements IFilter {

    /**
     * DOC mhelleboid AssignmentPropertySectionFilter constructor comment.
     */
    public AssignmentPropertySectionFilter() {
        super();
    }

    public boolean select(Object object) {
        if (object instanceof BusinessItemShapeEditPart)
            return true;
        if (object instanceof BaseBusinessItemRelationShipEditPart)
            return true;
        return false;
    }
}
