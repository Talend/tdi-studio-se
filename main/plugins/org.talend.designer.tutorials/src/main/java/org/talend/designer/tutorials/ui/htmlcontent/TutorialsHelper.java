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

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.ui.internal.intro.impl.model.IntroContentProvider;
import org.eclipse.ui.internal.intro.impl.model.loader.ContentProviderManager;
import org.eclipse.ui.internal.intro.impl.model.loader.IntroContentParser;
import org.eclipse.ui.intro.config.IIntroContentProviderSite;
import org.eclipse.ui.intro.config.IIntroXHTMLContentProvider;
import org.talend.commons.ui.html.TalendHtmlModelUtil;
import org.talend.core.ui.branding.StartingConstants;
import org.talend.designer.tutorials.TutorialsPlugin;
import org.talend.designer.tutorials.i18n.Messages;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * created by hcyi on Mar 20, 2017 Detailled comment
 *
 */
public class TutorialsHelper {

    private static TutorialsHelper helper = null;

    private String content;

    private TutorialsHelper() {

    }

    public static TutorialsHelper getHelper() {
        if (helper == null) {
            helper = new TutorialsHelper();
        }
        return helper;
    }

    public String getHtmlContent() throws IOException {
        if (content == null || "".equals(content)) { //$NON-NLS-1$
            URL startingHtmlURL = TutorialsPlugin.getDefault().getBundle().getEntry("content/tutorials.html"); //$NON-NLS-1$;
            if (startingHtmlURL != null) {
                startingHtmlURL = FileLocator.toFileURL(startingHtmlURL);
                String result = startingHtmlURL.toExternalForm();
                if (result.startsWith("file:/")) { //$NON-NLS-1$
                    if (result.startsWith("file:///") == false) { //$NON-NLS-1$
                        result = "file:///" + result.substring(6); //$NON-NLS-1$
                    }
                }

                IntroContentParser parser = new IntroContentParser(result);
                Document dom = parser.getDocument();
                if (dom != null) {
                    resolveInternationalization(dom);
                // resolve all relative resources relative to content file. Do it before
                // inserting shared style to enable comparing fully qualified styles.
                TalendHtmlModelUtil.updateResourceAttributes(dom.getDocumentElement(), "/content/", TutorialsPlugin.getDefault()
                        .getBundle());
                    resolveDynamicContent(dom, null);
                    content = IntroContentParser.convertToString(dom);
                }
            }
        }
        if (content == null || "".equals(content)) { //$NON-NLS-1$
            throw new IOException("Can't find starting helper content"); //$NON-NLS-1$
        }
        return content;
    }

    private void resolveInternationalization(Document dom) {
        NodeList internationals = dom.getElementsByTagNameNS("*", //$NON-NLS-1$
                StartingConstants.KEY_INTERNATIONAL);
        Node[] nodes = TalendHtmlModelUtil.getArray(internationals);
        for (Node node : nodes) {
            Element internationalElement = (Element) node;
            internationalElement.getParentNode().replaceChild(
                    dom.createTextNode(Messages.getString(internationalElement.getAttribute(StartingConstants.ATT_ID))),
                    internationalElement);
        }
    }

    private Document resolveDynamicContent(Document dom, IIntroContentProviderSite site) {

        // get all content provider elements in DOM.
        NodeList contentProviders = dom.getElementsByTagNameNS("*", //$NON-NLS-1$
                IntroContentProvider.TAG_CONTENT_PROVIDER);

        // get the array version of the nodelist to work around DOM api design.
        // Node[] nodes = ModelUtil.getArray(contentProviders);
        Node[] nodes = TalendHtmlModelUtil.getArray(contentProviders);
        for (Node node : nodes) {
            Element contentProviderElement = (Element) node;
            IntroContentProvider provider = new IntroContentProvider(contentProviderElement, TutorialsPlugin.getDefault()
                    .getBundle());
            // If we've already loaded the content provider for this element,
            // retrieve it, otherwise load the class.
            IIntroXHTMLContentProvider providerClass = (IIntroXHTMLContentProvider) ContentProviderManager.getInst()
                    .getContentProvider(provider);
            if (providerClass == null) {
                // content provider never created before, create it.
                providerClass = (IIntroXHTMLContentProvider) ContentProviderManager.getInst().createContentProvider(provider,
                        site);
            }

            if (providerClass != null) {
                // create a div with the same id as the contentProvider, pass it
                // as the parent to create the specialized content, and then
                // replace the contentProvider element with this div.
                // Properties att = new Properties();
                // Element countryListElement = dom.getElementById(ContentConstants.COUNTRY_LIST_ID);
                Element element = provider.getElement();
                // att.setProperty(IIntroHTMLConstants.ATTRIBUTE_ID, provider.getId());
                // Element contentDiv = ModelUtil.createElement(dom, ModelUtil.TAG_DIV, att);
                providerClass.createContent(provider.getId(), element);

            } else {
                // we couldn't load the content provider, so add any alternate
                // text content if there is any.
                // INTRO: do it. 3.0 intro content style uses text element as
                // alt text. We can load XHTML content here.
            }
        }
        return dom;
    }
}
