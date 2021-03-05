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
import org.talend.repository.ui.dialog.ProjectSettingDialog;

/**
 * created by cmeng on Feb 4, 2015 Detailled comment
 *
 */
public class OpenPaletteFilterAction extends Action {

    private static OpenPaletteFilterAction openPaletteFilterAction = null;

    private OpenPaletteFilterAction() {
        super("PaletteFilter"); //$NON-NLS-1$
        setId(getClass().getCanonicalName());
        setImageDescriptor(ImageProvider.getImageDesc(ECoreImage.PALETTE_FILTER));
    }

    public static OpenPaletteFilterAction getInstance() {
        if (openPaletteFilterAction == null) {
            openPaletteFilterAction = new OpenPaletteFilterAction();
        }
        return openPaletteFilterAction;
    }

    @Override
    public void run() {
        new ProjectSettingDialog().open("org.talend.repository.preference.PaletteSettingPage"); //$NON-NLS-1$
    }

}
