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
package org.talend.designer.mapper.model.table;

import org.talend.designer.mapper.i18n.Messages;
import org.talend.designer.mapper.ui.image.ImageInfo;

/**
 *
 * Lookup types.
 *
 * $Id$
 *
 */
public enum TMAP_LOOKUP_MODE implements IUILookupMode {
    LOAD_ONCE(LOOKUP_MODE.LOAD_ONCE, "TMAP_LOOKUP_MODE.LOAD_ONCE.label", //$NON-NLS-1$
              "TMAP_LOOKUP_MODE.LOAD_ONCE.tooltip"//$NON-NLS-1$
              , ImageInfo.ARROW_RIGHT),

    LOAD_ONCE_AND_UPDATE(LOOKUP_MODE.LOAD_ONCE_AND_UPDATE, "TMAP_LOOKUP_MODE.LOAD_ONCE_AND_UPDATE.label", //$NON-NLS-1$
                       "TMAP_LOOKUP_MODE.LOAD_ONCE_AND_UPDATE.tooltip", ImageInfo.ARROW_RIGHT_EDIT), //$NON-NLS-1$

    RELOAD(LOOKUP_MODE.RELOAD, "TMAP_LOOKUP_MODE.RELOAD.label", //$NON-NLS-1$
           "TMAP_LOOKUP_MODE.RELOAD.tooltip", ImageInfo.REFRESH), //$NON-NLS-1$

    CACHE_OR_RELOAD(LOOKUP_MODE.CACHE_OR_RELOAD, "TMAP_LOOKUP_MODE.CACHE_OR_RELOAD.label",//$NON-NLS-1$
                    "TMAP_LOOKUP_MODE.CACHE_OR_RELOAD.tooltip", ImageInfo.REFRESH_CACHE), //$NON-NLS-1$
    ;

    private String label;

    private MENU_TYPE menuType;

    /**
     * Getter for menuType.
     *
     * @return the menuType
     */
    public MENU_TYPE getMenuType() {
        return menuType;
    }

    private LOOKUP_MODE multipleMatchingMode;

    private String tooltipText;

    private ImageInfo imageInfo;

    /**
     *
     * DOC amaumont LOOKUP_TYPE constructor comment.
     *
     * @param labelKey
     */
    TMAP_LOOKUP_MODE(LOOKUP_MODE multipleMatchingMode, String labelKey, String tooltipKey, ImageInfo imageInfo) {
        this.multipleMatchingMode = multipleMatchingMode;
        this.label = Messages.getString(labelKey);
        this.tooltipText = Messages.getString(tooltipKey);
        this.imageInfo = imageInfo;
        this.menuType = MENU_TYPE.ITEM;
    }

    /**
     * Getter for label.
     *
     * @return the label
     */
    public String getLabel() {
        return this.label;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.model.table.IUITest#getImageInfo()
     */
    public ImageInfo getImageInfo() {
        return imageInfo;
    }

    /**
     * Getter for multipleMatchingMode.
     *
     * @return the multipleMatchingMode
     */
    public LOOKUP_MODE getLookupMode() {
        return this.multipleMatchingMode;
    }

    public String getTooltipText() {
        return this.tooltipText;
    }

    public static IUILookupMode parse(String matchingMode) {
        TMAP_LOOKUP_MODE multipleMatchingMode = null;
        TMAP_LOOKUP_MODE[] tmapMultipleMatchingModes = values();
        for (TMAP_LOOKUP_MODE tmapMultipleMatchingMode : tmapMultipleMatchingModes) {
            if (tmapMultipleMatchingMode.toString().equals(matchingMode)) {
                multipleMatchingMode = tmapMultipleMatchingMode;
                break;
            }
        }
        return multipleMatchingMode;
    }

};
