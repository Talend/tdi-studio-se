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
package org.talend.designer.core.ui.preferences;

import org.talend.core.PluginChecker;
import org.talend.core.model.utils.TalendPropertiesUtil;
import org.talend.core.ui.preference.hidden.IHidePreferencePageValidator;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class ExchangeHidePreferencePageValidator implements IHidePreferencePageValidator {

    /**
     * DOC ggu ExchangeHidePreferencePageValidator constructor comment.
     */
    public ExchangeHidePreferencePageValidator() {
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.prefs.hidden.IHidePreferencePageValidator#validate()
     */
    @Override
    public boolean validate() {
        return !PluginChecker.isExchangeSystemLoaded() || TalendPropertiesUtil.isHideExchange();
    }

}
