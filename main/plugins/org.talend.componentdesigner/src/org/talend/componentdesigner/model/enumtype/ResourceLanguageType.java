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
package org.talend.componentdesigner.model.enumtype;

import org.talend.componentdesigner.PluginConstant;

/**
 * @author rli
 * 
 */
public enum ResourceLanguageType {

    DEFAULTRESOURCETYPE(PluginConstant.DEFAULTLANG_RESOURCE, PluginConstant.RESOURCELANGUAGE_DEFAULT),
    ZHRESOURCETYPE(PluginConstant.ZHLANG_RESOURCE, PluginConstant.RESOURCELANGUAGE_ZH),
    FRRESOURCETYPE(PluginConstant.FRLANG_RESOURCE, PluginConstant.RESOURCELANGUAGE_FR);

    private final String nameSuffix;

    private final String lang;

    ResourceLanguageType(String lang, String nameSuffix) {
        this.lang = lang;
        this.nameSuffix = nameSuffix;
    }

    public String getNameSuffix() {
        return this.nameSuffix;
    }

    public String getLang() {
        return this.lang;
    }

    /**
     * Get the corresponding ResourceLanguageType enum by the specified lang string.
     * 
     * @param priority
     * @return
     */
    public static final ResourceLanguageType find(String lang) {
        ResourceLanguageType type = null;
        if (lang.equals(PluginConstant.ZHLANG_RESOURCE)) {
            type = ZHRESOURCETYPE;
        } else if (lang.equals(PluginConstant.FRLANG_RESOURCE)) {
            type = FRRESOURCETYPE;
        } else {
            type = DEFAULTRESOURCETYPE;
        }
        return type;
    }
}
