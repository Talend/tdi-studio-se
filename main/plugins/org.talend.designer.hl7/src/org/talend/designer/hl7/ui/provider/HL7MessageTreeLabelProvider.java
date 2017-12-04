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
package org.talend.designer.hl7.ui.provider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.talend.designer.hl7.model.IModel;
import org.talend.designer.hl7.model.PrimitiveModel;
import org.talend.designer.hl7.model.TypeModel;

import ca.uhn.hl7v2.model.Group;
import ca.uhn.hl7v2.model.Structure;
import ca.uhn.hl7v2.model.Type;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class HL7MessageTreeLabelProvider extends LabelProvider implements ITableLabelProvider {

    private List<PrimitiveModel> allPrimitives = new ArrayList<PrimitiveModel>();

    public Image getColumnImage(Object element, int columnIndex) {
        return null;
    }

    public String getColumnText(Object element, int columnIndex) {

        if (element instanceof Type) {
            Type datatype = (Type) element;
            return datatype.getName();
        }

        if (element instanceof IModel) {
            if (element instanceof TypeModel) {
                TypeModel tm = (TypeModel) element;
                allPrimitives.addAll(Arrays.asList(tm.getPrimitives()));
            }
            return ((IModel) element).getDisplayName();
        }

        // if (element instanceof TypeModel) {
        // return ((TypeModel) element).getDisplayName();
        // }
        // if (element instanceof Varies) {
        // Varies varies = (Varies) element;
        // return varies.getName();
        // }
        if (element instanceof Group) {
            Group group = (Group) element;
            String groupName = group.getName();
            groupName = groupName.substring(groupName.lastIndexOf('.') + 1, groupName.length());
            return groupName;
        }
        // if (element instanceof SegmentModel) {
        // return ((SegmentModel) element).getDisplayName();
        // }

        if (element instanceof Structure) {
            Structure structure = (Structure) element;
            return structure.getName();
        }
        return null;
    }

    @Override
    public String getText(Object element) {
        return getColumnText(element, 0);
    }

    public List<PrimitiveModel> getAllPrimitives() {
        return this.allPrimitives;
    }

}
