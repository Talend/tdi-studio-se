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
package org.talend.designer.components.exchange.ui.actions;

import java.util.Properties;

import org.eclipse.ui.intro.IIntroSite;
import org.eclipse.ui.intro.config.IIntroAction;
import org.talend.designer.components.exchange.ui.htmlcontent.ContentConstants;
import org.talend.designer.components.exchange.ui.views.ExchangeManager;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class FilterExtensionAction implements IIntroAction {

    private final String TEXT = "text";

    public void run(IIntroSite site, Properties params) {
        if (params != null) {
            String text = params.getProperty(TEXT);
            ExchangeManager.getInstance().setAvialableFilter(text == null ? "" : text);
            ExchangeManager.getInstance().generateXHTMLPage(ContentConstants.UL_LIST_AVAILABLE_EXTENSIONS,
                    new String[] { ContentConstants.INSERT_EXTENSION_DATA });
        }
    }

}
