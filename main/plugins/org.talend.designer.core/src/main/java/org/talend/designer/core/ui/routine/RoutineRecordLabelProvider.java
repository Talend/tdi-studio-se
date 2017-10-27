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
package org.talend.designer.core.ui.routine;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;

/**
 * ggu class global comment. Detailled comment
 */
public class RoutineRecordLabelProvider extends LabelProvider {

    @Override
    public Image getImage(Object element) {
        if (element instanceof RoutineItemRecord && ((RoutineItemRecord) element).hasProblem()) {
            return ImageProvider.getImage(EImage.ERROR_ICON);
        }
        return super.getImage(element);
    }

    @Override
    public String getText(Object element) {
        if (element instanceof RoutineItemRecord) {
            return ((RoutineItemRecord) element).getLabel();
        }
        return super.getText(element);
    }

}
