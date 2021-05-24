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
package org.talend.designer.components.exchange.ui.htmlcontent;

import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.talend.commons.ui.html.DynamicHtmlURL;
import org.talend.commons.ui.html.DynamicURLParser;

/**
 * DOC talend class global comment. Detailled comment
 */
public class MyExtensionLocationListener implements LocationListener {

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.swt.browser.LocationListener#changing(org.eclipse.swt.browser.LocationEvent)
     */
    public void changing(LocationEvent event) {
        String url = event.location;
        if (url == null)
            return;

        if (url.indexOf("&amp;") != -1) {
            url = url.replaceAll("&amp;", "&");
        }

        DynamicURLParser parser = new DynamicURLParser(url);
        if (parser.hasIntroUrl()) {
            // stop URL first.
            event.doit = false;
            // execute the action embedded in the IntroURL
            DynamicHtmlURL introURL = parser.getIntroURL();
            introURL.execute();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.swt.browser.LocationListener#changed(org.eclipse.swt.browser.LocationEvent)
     */
    public void changed(LocationEvent event) {

    }

}
