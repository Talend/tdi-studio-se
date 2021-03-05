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
package org.talend.presentation.onboarding.resource.tos;

import org.talend.presentation.onboarding.interfaces.IOnBoardingJsonI18n;
import org.talend.presentation.onboarding.resource.tos.i18n.Messages;

public class OnBoardingJsonI18n implements IOnBoardingJsonI18n {

    public OnBoardingJsonI18n() {
        // nothing need to do
    }

    @Override
    public String getI18NString(String key) {
        return Messages.getString(key);
    }

}
