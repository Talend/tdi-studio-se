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
package org.talend.repository.ui.actions.routines;

import org.eclipse.swt.graphics.Image;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;

public class AssignRoutinesToJarAction extends AbstractAssignCodesToJarAction {

    @Override
    protected String getActionText() {
        return "Assign Routine to..."; //$NON-NLS-1$
    }

    @Override
    protected Image getImage() {
        return ImageProvider.getImage(ECoreImage.ROUTINE_ICON);
    }

    @Override
    protected ERepositoryObjectType getTargetCodesJarType() {
        return ERepositoryObjectType.ROUTINESJAR;
    }

    @Override
    protected ERepositoryObjectType getCodeType() {
        return ERepositoryObjectType.ROUTINES;
    }

    @Override
    protected boolean isTargetItem(Item item) {
        return "RoutineItem".equals(item.eClass().getName()); //$NON-NLS-1$ ;
    }

}
