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
package org.talend.repository.ui.actions;

import org.eclipse.jface.action.Action;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.ui.component.ComponentPaletteUtilities;

/**
 * DOC hwang class global comment. Detailled comment
 */
public final class ShowStandardAction extends Action {

    private static ShowStandardAction showStandard = null;

    private ShowStandardAction() {
        super("&Standard"); //$NON-NLS-1$
        setId(getClass().getCanonicalName());
        setImageDescriptor(ImageProvider.getImageDesc(ECoreImage.STANDARD_DISICON));
    }

    public static ShowStandardAction getInstance() {
        if (showStandard == null) {
            showStandard = new ShowStandardAction();
        }
        return showStandard;
    }

    @Override
    public void run() {
        ComponentPaletteUtilities.updatePalette(false);
        ShowFavoriteAction.state = true;
        setEnabled(false);
        ShowFavoriteAction.getInstance().setEnabled(true);
    }

    public void doRun() {
        ComponentPaletteUtilities.updatePalette(false);
        ShowFavoriteAction.state = true;
        doSetEnable();
    }

    public void doSetEnable() {
        setEnabled(false);
        ShowFavoriteAction.getInstance().setEnabled(true);
        if (!this.isEnabled()) {
            setDisabledImageDescriptor(ImageProvider.getImageDesc(ECoreImage.STANDARD_ICON));
        }
    }

}
