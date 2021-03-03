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

import org.talend.designer.gefabstractmap.resource.ImageInfo;

/**
 *
 * Lookup types.
 *
 * $Id$
 *
 */
public enum XML_MAP_LOOKUP_MODE implements IUILookupMode {
    LOAD_ONCE(LOOKUP_MODE.LOAD_ONCE, "Load once", "Load once the lookup at subjob start", ImageInfo.ARROW_RIGHT),

    LOAD_ONCE_AND_UPDATE(
                         LOOKUP_MODE.LOAD_ONCE_AND_UPDATE,
                         "Load once and update",
                         "Load once the lookup at subjob start and add/update the lookup row during the process",
                         ImageInfo.ARROW_RIGHT_EDIT),

    RELOAD(LOOKUP_MODE.RELOAD, "Reload at each row", "Reload the lookup at each row", ImageInfo.REFRESH),

    CACHE_OR_RELOAD(
                    LOOKUP_MODE.CACHE_OR_RELOAD,
                    "Reload at each row (cache)",
                    "At each row, get result from the cache or reload the lookup",
                    ImageInfo.REFRESH_CACHE), ;

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
    XML_MAP_LOOKUP_MODE(LOOKUP_MODE multipleMatchingMode, String labelKey, String tooltipKey, ImageInfo imageInfo) {
        this.multipleMatchingMode = multipleMatchingMode;
        this.label = labelKey;
        this.tooltipText = tooltipKey;
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
        XML_MAP_LOOKUP_MODE multipleMatchingMode = null;
        XML_MAP_LOOKUP_MODE[] tmapMultipleMatchingModes = values();
        for (XML_MAP_LOOKUP_MODE tmapMultipleMatchingMode : tmapMultipleMatchingModes) {
            if (tmapMultipleMatchingMode.toString().equals(matchingMode)) {
                multipleMatchingMode = tmapMultipleMatchingMode;
                break;
            }
        }
        return multipleMatchingMode;
    }

};
