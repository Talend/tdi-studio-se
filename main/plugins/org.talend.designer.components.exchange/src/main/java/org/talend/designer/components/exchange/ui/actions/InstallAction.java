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

import org.eclipse.jface.action.Action;
import org.eclipse.ui.intro.IIntroSite;
import org.eclipse.ui.intro.config.IIntroAction;
import org.talend.designer.components.exchange.model.ComponentExtension;
import org.talend.designer.components.exchange.ui.htmlcontent.AvailableCompositeProvider;
import org.talend.designer.components.exchange.ui.htmlcontent.DownloadExtensionProvider;
import org.talend.designer.components.exchange.ui.views.ExchangeManager;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class InstallAction extends Action implements IIntroAction {

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.intro.config.IIntroAction#run(org.eclipse.ui.intro.IIntroSite, java.util.Properties)
     */
    public void run(IIntroSite site, Properties params) {
        if (params != null) {
            String number = (String) params.get(AvailableCompositeProvider.NUMBER);
            ComponentExtension select = DownloadExtensionProvider.componentMap.get(number);
            ExchangeManager.getInstance().setSelectedExtension(select);
            DownloadComponenentsAction downloadAction = new DownloadComponenentsAction();
            downloadAction.run();
            ExchangeManager.getInstance().setSelectedExtension(null);
        }
    }
}
