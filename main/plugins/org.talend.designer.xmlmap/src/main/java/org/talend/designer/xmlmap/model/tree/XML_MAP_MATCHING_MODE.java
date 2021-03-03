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
package org.talend.designer.xmlmap.model.tree;

import org.talend.designer.components.lookup.common.ICommonLookup.MATCHING_MODE;
import org.talend.designer.gefabstractmap.resource.ImageInfo;

/**
 *
 * Matching modes.
 *
 */
public enum XML_MAP_MATCHING_MODE implements IUIMatchingMode {
    ALL_ROWS(MATCHING_MODE.ALL_ROWS, "All rows", "Matches with all lookup's rows", ImageInfo.ALL_MATCHES),
    UNIQUE_MATCH(
                 MATCHING_MODE.UNIQUE_MATCH,
                 "Unique match",
                 "Matches only with the last matching loaded lookup''s row ",
                 ImageInfo.UNIQUE_MATCH),
    FIRST_MATCH(
                MATCHING_MODE.FIRST_MATCH,
                "First match",
                "Matches only with the first matching loaded lookup''s row",
                ImageInfo.FIRST_MATCH),
    ALL_MATCHES(MATCHING_MODE.ALL_MATCHES, "All matches", "Matches with all the matching lookup's row", ImageInfo.ALL_MATCHES), ;

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
    XML_MAP_MATCHING_MODE(MATCHING_MODE multipleMatchingMode, String labelKey, String tooltipKey, ImageInfo imageInfo) {
        this.label = labelKey;
        this.multipleMatchingMode = multipleMatchingMode;
        this.tooltipText = tooltipKey;
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
        XML_MAP_MATCHING_MODE multipleMatchingMode = null;
        XML_MAP_MATCHING_MODE[] tmapMultipleMatchingModes = values();
        for (XML_MAP_MATCHING_MODE tmapMultipleMatchingMode : tmapMultipleMatchingModes) {
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
