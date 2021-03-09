// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.ui.routine;

import org.eclipse.jface.viewers.LabelProvider;

/**
 * ggu class global comment. Detailled comment
 */
public class RoutineRecordLabelProvider extends LabelProvider {

    @Override
    public String getText(Object element) {
        if (element instanceof RoutineItemRecord) {
            return ((RoutineItemRecord) element).getLabel();
        }
        return super.getText(element);
    }

}
