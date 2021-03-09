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
package org.talend.designer.core.utils;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.graphics.RGB;
import org.talend.commons.ui.utils.image.ColorUtils;
import org.talend.core.CorePlugin;
import org.talend.core.model.process.EConnectionType;

/**
 * ggu class global comment. Detailled comment
 */
public final class DesignerColorUtils {

    // public static final RGB SUBJOB_TITLE_COLOR = new RGB(160, 190, 240);
    public static final RGB SUBJOB_TITLE_COLOR = new RGB(92, 131, 150);

    public static final RGB SUBJOB_COLOR = new RGB(207, 226, 236);

    public static final RGB SUBJOB_AROUND_COLOR = new RGB(92, 131, 150);

    public static final RGB JOBLET_COLOR = new RGB(130, 240, 100);

    public static final RGB MR_COLOR = new RGB(186, 203, 219);

    public static final String SUBJOB_TITLE_COLOR_NAME = "subjobTitleColor"; //$NON-NLS-1$

    public static final String SUBJOB_COLOR_NAME = "subjobColor"; //$NON-NLS-1$

    public static final String JOBLET_COLOR_NAME = "jobletColor"; //$NON-NLS-1$

    public static final String MRGROUP_COLOR_NAME = "mrgroupColor"; //$NON-NLS-1$

    public static final String JOBDESIGNER_EGITOR_BACKGROUND_COLOR_NAME = "jobDesignerBackgroundColor"; //$NON-NLS-1$

    public static final RGB DEFAULT_EDITOR_COLOR = new RGB(250, 250, 250);

    public static final String READONLY_BACKGROUND_COLOR_NAME = "readOnlyBackgroundColor"; //$NON-NLS-1$

    public static final RGB DEFAULT_READONLY_COLOR = new RGB(0xE7, 0xE7, 0xE7);

    public static String getPreferenceConnectionName(EConnectionType connType) {
        if (connType == null) {
            return null;
        }
        return connType.getName() + "_COLOR"; //$NON-NLS-1$
    }

    public static RGB getPreferenceConnectionColor(EConnectionType connType, RGB defaultRGB) {
        if (connType == null || defaultRGB == null) {
            return defaultRGB;
        }
        String rgb = CorePlugin.getDefault().getDesignerCoreService().getPreferenceStore(getPreferenceConnectionName(connType));
        return ColorUtils.parseStringToRGB(rgb, defaultRGB);
    }

    public static RGB getPreferenceConnectionColor(EConnectionType connType) {
        return getPreferenceConnectionColor(connType, connType.getRGB());
    }

    /**
     * used for initialize preference.
     */
    public static void initPreferenceDefault(IPreferenceStore store) {
        if (store == null) { // store must be the designer core preference store.
            return;
        }

        // background
        store.setDefault(DesignerColorUtils.JOBDESIGNER_EGITOR_BACKGROUND_COLOR_NAME,
                StringConverter.asString(DesignerColorUtils.DEFAULT_EDITOR_COLOR));
        store.setDefault(DesignerColorUtils.READONLY_BACKGROUND_COLOR_NAME,
                StringConverter.asString(DesignerColorUtils.DEFAULT_READONLY_COLOR));
        // subjob
        store.setDefault(DesignerColorUtils.SUBJOB_COLOR_NAME, StringConverter.asString(DesignerColorUtils.SUBJOB_COLOR));
        store.setDefault(DesignerColorUtils.SUBJOB_TITLE_COLOR_NAME,
                StringConverter.asString(DesignerColorUtils.SUBJOB_TITLE_COLOR));
        // mr
        store.setDefault(DesignerColorUtils.JOBLET_COLOR_NAME, StringConverter.asString(DesignerColorUtils.JOBLET_COLOR));
        // mr
        store.setDefault(DesignerColorUtils.MRGROUP_COLOR_NAME, StringConverter.asString(DesignerColorUtils.MR_COLOR));
        // connection
        for (EConnectionType connType : EConnectionType.values()) {
            store.setDefault(getPreferenceConnectionName(connType), StringConverter.asString(connType.getRGB()));
        }
    }

    /**
     *
     * ggu Comment method "getPreferenceSubjobColor".
     *
     * @param name must be SUBJOB_TITLE_COLOR_NAME and SUBJOB_COLOR_NAME
     * @param defaultColor if can't found the preference value, will use the default color.
     * @return
     */
    public static RGB getPreferenceSubjobRGB(String name, RGB defaultColor) {
        if (name == null || defaultColor == null || (!name.equals(SUBJOB_COLOR_NAME) && !name.equals(SUBJOB_TITLE_COLOR_NAME))) {
            return defaultColor;
        }
        String colorStr = CorePlugin.getDefault().getDesignerCoreService().getPreferenceStore(name);
        return ColorUtils.parseStringToRGB(colorStr, defaultColor);
    }

    public static RGB getPreferenceMRGroupRGB(String name, RGB defaultColor) {
        if (name == null || defaultColor == null || !name.equals(MRGROUP_COLOR_NAME)) {
            return MR_COLOR;
        }
        String colorStr = CorePlugin.getDefault().getDesignerCoreService().getPreferenceStore(name);
        return ColorUtils.parseStringToRGB(colorStr, defaultColor);
    }

    public static RGB getPreferenceDesignerEditorRGB(String name, RGB defaultColor) {
        if (name == null || defaultColor == null || !name.equals(JOBDESIGNER_EGITOR_BACKGROUND_COLOR_NAME)) {
            return DEFAULT_EDITOR_COLOR;
        }
        String colorStr = CorePlugin.getDefault().getDesignerCoreService().getPreferenceStore(name);
        return ColorUtils.parseStringToRGB(colorStr, defaultColor);
    }

    public static RGB getPreferenceReadonlyRGB(String name, RGB defaultColor) {
        if (name == null || defaultColor == null || !name.equals(READONLY_BACKGROUND_COLOR_NAME)) {
            return DEFAULT_READONLY_COLOR;
        }
        String colorStr = CorePlugin.getDefault().getDesignerCoreService().getPreferenceStore(name);
        return ColorUtils.parseStringToRGB(colorStr, defaultColor);
    }

}
