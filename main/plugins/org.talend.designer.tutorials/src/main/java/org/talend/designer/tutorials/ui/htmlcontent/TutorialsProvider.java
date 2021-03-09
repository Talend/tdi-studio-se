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
package org.talend.designer.tutorials.ui.htmlcontent;

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
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.intro.config.IIntroContentProviderSite;
import org.eclipse.ui.intro.config.IIntroXHTMLContentProvider;
import org.talend.commons.ui.html.TalendHtmlModelUtil;
import org.talend.core.prefs.IIntroHTMLConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * created by hcyi on Mar 20, 2017 Detailled comment
 *
 */
public class TutorialsProvider implements IIntroXHTMLContentProvider {

    private String oldCountry;

    private int countryToSelect = 0;

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.ui.intro.config.IIntroContentProvider#init(org.eclipse.ui.intro.config.IIntroContentProviderSite)
     */
    @Override
    public void init(IIntroContentProviderSite site) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.intro.config.IIntroContentProvider#createContent(java.lang.String, java.io.PrintWriter)
     */
    @Override
    public void createContent(String id, PrintWriter out) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.intro.config.IIntroContentProvider#createContent(java.lang.String,
     * org.eclipse.swt.widgets.Composite, org.eclipse.ui.forms.widgets.FormToolkit)
     */
    @Override
    public void createContent(String id, Composite parent, FormToolkit toolkit) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.intro.config.IIntroContentProvider#dispose()
     */
    @Override
    public void dispose() {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.intro.config.IIntroXHTMLContentProvider#createContent(java.lang.String, org.w3c.dom.Element)
     */
    @Override
    public void createContent(String id, Element parent) {
        if (ContentConstants.INSERT_DATA.equals(id)) {
            String countryList[] = initiateCountryList();
            Document dom = parent.getOwnerDocument();
            for (int i = 0; i < countryList.length; i++) {

                Properties att = new Properties();
                if (i == countryToSelect) {
                    att.setProperty(IIntroHTMLConstants.ATTRIBUTE_SELECTED, "true");
                }
                Element option = TalendHtmlModelUtil.createElement(dom, TalendHtmlModelUtil.TAG_OPTION, att);
                option.setTextContent(countryList[i]);
                parent.appendChild(option);
            }
        }
    }

    private String[] initiateCountryList() {
        SortedSet<String> countryList = new TreeSet<String>();
        for (Locale locale : Locale.getAvailableLocales()) {
            if (locale.getDisplayCountry().compareTo("") != 0) { //$NON-NLS-1$
                countryList.add(locale.getDisplayCountry());
            }
        }

        String defaultCountry = Locale.getDefault().getDisplayCountry();
        int i = 0;

        if (oldCountry != null) {
            for (String country : countryList) {

                if (country.equals(oldCountry)) {
                    countryToSelect = i;
                    break;
                }
                i++;
            }
        } else {
            for (String country : countryList) {
                if (country.equals(defaultCountry)) {
                    countryToSelect = i;
                    break;
                }
                i++;
            }
        }
        return countryList.toArray(new String[] {});
    }
}
