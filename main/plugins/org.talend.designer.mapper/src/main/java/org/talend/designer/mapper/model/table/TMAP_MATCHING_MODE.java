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

import org.talend.designer.components.lookup.common.ICommonLookup.MATCHING_MODE;
import org.talend.designer.mapper.i18n.Messages;
import org.talend.designer.mapper.ui.image.ImageInfo;

/**
 *
 * Matching modes.
 *
 */
public enum TMAP_MATCHING_MODE implements IUIMatchingMode {
    ALL_ROWS(
             MATCHING_MODE.ALL_ROWS,
             "TMAP_MATCHING_MODE.ALL_ROWS.label", //$NON-NLS-1$
             "TMAP_MATCHING_MODE.ALL_ROWS.tooltip", ImageInfo.ALL_MATCHES), //$NON-NLS-1$
    UNIQUE_MATCH(
                 MATCHING_MODE.UNIQUE_MATCH,
                 "TMAP_MATCHING_MODE.UNIQUE_MATCH.label", //$NON-NLS-1$
                 "TMAP_MATCHING_MODE.UNIQUE_MATCH.tooltip", ImageInfo.UNIQUE_MATCH), //$NON-NLS-1$
    FIRST_MATCH(
                MATCHING_MODE.FIRST_MATCH,
                "TMAP_MATCHING_MODE.FIRST_MATCH.label", //$NON-NLS-1$
                "TMAP_MATCHING_MODE.FIRST_MATCH.tooltip", ImageInfo.FIRST_MATCH), //$NON-NLS-1$
    LAST_MATCH(/* @deprecated: replaced by UNIQUE_MATCH */
               MATCHING_MODE.LAST_MATCH,
               "TMAP_MATCHING_MODE.LAST_MATCH.label", //$NON-NLS-1$
               "TMAP_MATCHING_MODE.LAST_MATCH.tooltip", ImageInfo.LAST_MATCH), //$NON-NLS-1$
    ALL_MATCHES(
                MATCHING_MODE.ALL_MATCHES,
                "TMAP_MATCHING_MODE.ALL_MATCHES.label", //$NON-NLS-1$
                "TMAP_MATCHING_MODE.ALL_MATCHES.tooltip", ImageInfo.ALL_MATCHES), ; //$NON-NLS-1$

    private String label;

    private String tooltipText;

    private MATCHING_MODE multipleMatchingMode;

    private MENU_TYPE menuType;

    private ImageInfo imageInfo;

    /**
     * Getter for menuType.
     *
     * @return the menuType
     */
    public MENU_TYPE getMenuType() {
        return menuType;
    }

    /**
     *
     * DOC amaumont LOOKUP_TYPE constructor comment.
     *
     * @param labelKey
     */
    TMAP_MATCHING_MODE(MATCHING_MODE multipleMatchingMode, String labelKey, String tooltipKey, ImageInfo imageInfo) {
        this.label = Messages.getString(labelKey);
        this.multipleMatchingMode = multipleMatchingMode;
        this.tooltipText = Messages.getString(tooltipKey);
        this.menuType = MENU_TYPE.ITEM;
        this.imageInfo = imageInfo;
    }

    /**
     * Getter for label.
     *
     * @return the label
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * Getter for multipleMatchingMode.
     *
     * @return the multipleMatchingMode
     */
    public MATCHING_MODE getMatchingMode() {
        return this.multipleMatchingMode;
    }

    public String getTooltipText() {
        return tooltipText;
    }

    public static IUIMatchingMode parse(String matchingMode) {
        TMAP_MATCHING_MODE multipleMatchingMode = null;
        TMAP_MATCHING_MODE[] tmapMultipleMatchingModes = values();
        for (TMAP_MATCHING_MODE tmapMultipleMatchingMode : tmapMultipleMatchingModes) {
            if (tmapMultipleMatchingMode.toString().equals(matchingMode)) {
                multipleMatchingMode = tmapMultipleMatchingMode;
                break;
            }
        }
        return multipleMatchingMode;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.mapper.model.table.IUITest#getImageInfo()
     */
    public ImageInfo getImageInfo() {
        return imageInfo;
    }

};
