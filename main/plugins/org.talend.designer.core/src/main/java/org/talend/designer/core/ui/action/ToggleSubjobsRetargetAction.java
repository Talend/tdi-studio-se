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
package org.talend.designer.core.ui.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.RetargetAction;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;

/**
 * bqian class global comment. Detailled comment
 */
public class ToggleSubjobsRetargetAction extends RetargetAction {

    public ToggleSubjobsRetargetAction() {
        super(ToggleSubjobsAction.ID, ToggleSubjobsAction.TEXT, IAction.AS_CHECK_BOX);
        setToolTipText(ToggleSubjobsAction.TEXT);
        ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();

        // TODO Provides appropriate hover and disabled images
        setImageDescriptor(ImageProvider.getImageDesc(ECoreImage.TOGGLE_SUBJOB));
        setDisabledImageDescriptor(ImageProvider.getImageDesc(ECoreImage.TOGGLE_SUBJOB_DISABLED));
    }
}
