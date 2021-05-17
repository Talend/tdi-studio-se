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
package org.talend.designer.components.exchange.ui.views;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.swt.browser.Browser;
import org.eclipse.ui.internal.intro.impl.model.IntroContentProvider;
import org.eclipse.ui.internal.intro.impl.model.loader.ContentProviderManager;
import org.eclipse.ui.internal.intro.impl.model.loader.IntroContentParser;
import org.eclipse.ui.intro.config.IIntroXHTMLContentProvider;
import org.talend.commons.ui.html.TalendHtmlModelUtil;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.prefs.IIntroHTMLConstants;
import org.talend.designer.components.exchange.ExchangeConstants;
import org.talend.designer.components.exchange.ExchangePlugin;
import org.talend.designer.components.exchange.model.Category;
import org.talend.designer.components.exchange.model.ComponentExtension;
import org.talend.designer.components.exchange.model.VersionRevision;
import org.talend.designer.components.exchange.ui.htmlcontent.AvailableCompositeProvider;
import org.talend.designer.components.exchange.ui.htmlcontent.ContentConstants;
import org.talend.designer.components.exchange.ui.htmlcontent.DownloadExtensionProvider;
import org.talend.designer.components.exchange.ui.htmlcontent.ExchangeContentProvider;
import org.talend.designer.components.exchange.ui.htmlcontent.HtmlContentUtil;
import org.talend.designer.components.exchange.ui.htmlcontent.MyExchangeContentProvider;
import org.talend.designer.components.exchange.util.ExchangeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * DOC talend class global comment. Detailled comment
 */
public class ExchangeManager {

    private List<ComponentExtension> fAvailableExtensions = new ArrayList<ComponentExtension>();

    private List<ComponentExtension> fContributedExtensions = new ArrayList<ComponentExtension>();

    private List<ComponentExtension> fInstalledExtensions = new ArrayList<ComponentExtension>();

    private List<VersionRevision> fVersionRevisions = new ArrayList<VersionRevision>();

    private Map<String, ComponentExtension> fDownloadedExtensions = new HashMap<String, ComponentExtension>();

    private Document dom;

    private Node[] contentProviderElements;

    private String avialableFilter;

    private ComponentExtension selectedExtension;

    private static ExchangeManager manager;

    private Browser browser;

    private ExchangeManager() {
        initDocument();
    }

    public static ExchangeManager getInstance() {
        if (manager == null) {
            manager = new ExchangeManager();
        }
        return manager;
    }

    public static void resteInstance() {
        manager = null;
    }

    public void initDocument() {
        try {
            dom = HtmlContentUtil.readHtmlToDocument("content/exchange_all.html");
            if (dom != null) {
                NodeList nodes = dom.getElementsByTagNameNS("*", //$NON-NLS-1$
                        IntroContentProvider.TAG_CONTENT_PROVIDER);
                // get the array version of the nodelist to work around DOM api design.
                // contentProviderElements = ModelUtil.getArray(nodes);
                contentProviderElements = TalendHtmlModelUtil.getArray(nodes);
            }
        } catch (IOException e) {
            ExceptionHandler.process(e);
        }
        setSelectedExtension(null);
        setAvialableFilter(null);
    }

    public void generateXHTMLPage() {
        // get/cache all content provider elements in DOM.
        if (browser != null) {
            String content = "";
            if (dom != null) {
                content = getResolvedContent(null, contentProviderElements);
            }
            browser.setText(content);
        }
    }

    private String getResolvedContent(String ulToShow, Node[] contentProviderElements) {
        resolveDynamicContent(dom, contentProviderElements);

        // insert base meta-tag,
        // ModelUtil.insertBase(dom, ModelUtil.getParentFolderOSString(""));

        // resolve all relative resources relative to content file. Do it before
        // inserting shared style to enable comparing fully qualified styles.
        TalendHtmlModelUtil.updateResourceAttributes(dom.getDocumentElement(), "/content/", ExchangePlugin.getDefault()
                .getBundle());

        if (ulToShow != null) {
            refreshTabContent(dom, ulToShow);
        }

        return IntroContentParser.convertToString(dom);
    }

    private void refreshTabContent(Document dom, String ulToShow) {
        // Element elementById = ModelUtil.getElementById(dom, ContentConstants.DIV_MAIN, ModelUtil.TAG_DIV);
        Element elementById = TalendHtmlModelUtil.getElementById(dom, ContentConstants.DIV_MAIN, TalendHtmlModelUtil.TAG_DIV);
        if (elementById != null) {
            NodeList elementsByTagName = elementById.getElementsByTagName("ul");
            // Node[] ulNodes = ModelUtil.getArray(elementsByTagName);
            Node[] ulNodes = TalendHtmlModelUtil.getArray(elementsByTagName);
            List uls = Arrays.asList(ContentConstants.availableUls);
            for (Node ul : ulNodes) {
                String ulId = ((Element) ul).getAttribute(IIntroHTMLConstants.ATTRIBUTE_ID);
                if (ulToShow != null && ulToShow.equals(ulId)) {
                    ((Element) ul).setAttribute("class", "ulblock");
                    // reset tabs
                    // Element menueUL = ModelUtil.getElementById(dom, ContentConstants.UL_MENU, "ul");
                    Element menueUL = TalendHtmlModelUtil.getElementById(dom, ContentConstants.UL_MENU, "ul");
                    if (menueUL != null) {
                        NodeList menues = menueUL.getElementsByTagName("li");
                        // Node[] menueNodes = ModelUtil.getArray(menues);
                        Node[] menueNodes = TalendHtmlModelUtil.getArray(menues);

                        if (ContentConstants.UL_LIST_AVAILABLE_EXTENSIONS.equals(ulToShow)
                                || ContentConstants.UL_EXTENSION_DETAILS.equals(ulToShow)) {
                            for (Node menu : menueNodes) {
                                String menuId = ((Element) menu).getAttribute(IIntroHTMLConstants.ATTRIBUTE_ID);
                                if (ContentConstants.LI_TAB1.equals(menuId)) {
                                    ((Element) menu).setAttribute("class", "hover");
                                } else {
                                    ((Element) menu).setAttribute("class", "leave");
                                }
                            }
                        } else if (ContentConstants.UL_DOWNLOADED_EXTENSIONS.equals(ulToShow)) {
                            for (Node menu : menueNodes) {
                                String menuId = ((Element) menu).getAttribute(IIntroHTMLConstants.ATTRIBUTE_ID);
                                if (ContentConstants.LI_TAB2.equals(menuId)) {
                                    ((Element) menu).setAttribute("class", "hover");
                                } else {
                                    ((Element) menu).setAttribute("class", "leave");
                                }
                            }
                        } else if (ContentConstants.UL_LIST_MY_EXTENSIONS.equals(ulToShow)
                                || ContentConstants.UL_CREAT_NEW_EXTENSION.equals(ulToShow)
                                || ContentConstants.UL_UPLOAD_EXTENSION_VERSION.equals(ulToShow)
                                || ContentConstants.UL_MODIFY_MY_EXTENSION_PAGE.equals(ulToShow)) {
                            for (Node menu : menueNodes) {
                                String menuId = ((Element) menu).getAttribute(IIntroHTMLConstants.ATTRIBUTE_ID);
                                if (ContentConstants.LI_TAB3.equals(menuId)) {
                                    ((Element) menu).setAttribute("class", "hover");
                                } else {
                                    ((Element) menu).setAttribute("class", "leave");
                                }
                            }
                        }
                    }

                } else if (uls.contains(ulId)) {
                    ((Element) ul).setAttribute("class", "ulhide");
                }
            }
        }
    }

    /**
     *
     * DOC talend Comment method "generateXHTMLPage".
     *
     * @param ulToShow the id of
     * <ul>
     * to show
     * @param contentProviderIds all ids must from the same content provider
     */
    public void generateXHTMLPage(String ulToShow, String[] contentProviderIds) {
        // regenerate all in case contentProviderIds are null
        if (browser != null) {
            List<Node> filterdNodes = new ArrayList<Node>();
            if (contentProviderIds != null && contentProviderIds.length > 0) {
                for (String providerId : contentProviderIds) {
                    for (Node node : contentProviderElements) {
                        if (providerId != null
                                && providerId.equals(((Element) node).getAttribute(IIntroHTMLConstants.ATTRIBUTE_ID))) {
                            filterdNodes.add(node);
                            break;
                        }
                    }
                }
            }
            String content = "";
            if (dom != null) {
                content = getResolvedContent(ulToShow, filterdNodes.toArray(new Node[filterdNodes.size()]));
            }
            browser.setText(content);
        }
    }

    /**
     *
     * DOC talend Comment method "resolveDynamicContent".
     *
     * @param dom
     * @param nodes original contentProvider nods parsed from html file
     * @return
     */
    private Document resolveDynamicContent(Document dom, Node[] nodes) {
        for (int i = 0; i < nodes.length; i++) {
            Element contentProviderElement = (Element) nodes[i];
            IntroContentProvider provider = new IntroContentProvider(contentProviderElement, ExchangePlugin.getDefault()
                    .getBundle());
            IIntroXHTMLContentProvider providerClass = (IIntroXHTMLContentProvider) ContentProviderManager.getInst()
                    .getContentProvider(provider);
            if (providerClass == null)
                providerClass = (IIntroXHTMLContentProvider) ContentProviderManager.getInst().createContentProvider(provider,
                        null);

            if (providerClass != null) {
                if (providerClass instanceof ExchangeContentProvider) {
                    if (getSelectedExtension() != null) {
                        ((ExchangeContentProvider) providerClass).setComponentExtension(getSelectedExtension());
                    } else {
                        ((ExchangeContentProvider) providerClass).setComponentExtension(getSelectedExtension());
                    }
                } else if (providerClass instanceof AvailableCompositeProvider) {
                    if (getAvialableFilter() == null) {
                        ((AvailableCompositeProvider) providerClass).setfAvailableExtensions(fAvailableExtensions);
                    } else {
                        List<ComponentExtension> fAvailableExtensionsFilter = new ArrayList<ComponentExtension>();
                        for (ComponentExtension Extension : fAvailableExtensions) {
                            if (Extension.getLabel().toUpperCase().contains(getAvialableFilter().toUpperCase())) {
                                fAvailableExtensionsFilter.add(Extension);
                            }
                        }
                        ((AvailableCompositeProvider) providerClass).setfAvailableExtensions(fAvailableExtensionsFilter);
                    }
                } else if (providerClass instanceof DownloadExtensionProvider) {
                    loadDownloadedExtensionsFromFile();
                    ((DownloadExtensionProvider) providerClass).setfInstalledExtensions(fInstalledExtensions);
                    ((DownloadExtensionProvider) providerClass).setfDownloadedExtensions(fDownloadedExtensions);
                } else if (providerClass instanceof MyExchangeContentProvider) {
                    ((MyExchangeContentProvider) providerClass).setComponentExtension(getSelectedExtension());
                    ((MyExchangeContentProvider) providerClass).setfContributeExtensions(new ArrayList<ComponentExtension>(
                            fContributedExtensions));
                }

                // revert the content contentprovider before generate the dynamic content
                reinjectDynamicContent(dom, contentProviderElement);

                Properties att = new Properties();
                att.setProperty(IIntroHTMLConstants.ATTRIBUTE_ID, provider.getId());
                // Element contentDiv = ModelUtil.createElement(dom, ModelUtil.TAG_DIV, att);
                Element contentDiv = TalendHtmlModelUtil.createElement(dom, TalendHtmlModelUtil.TAG_DIV, att);
                providerClass.createContent(provider.getId(), contentDiv);
                contentProviderElement.getParentNode().replaceChild(contentDiv, contentProviderElement);

            } else {
                // we couldn't load the content provider, so add any alternate
                // text content if there is any.
                // INTRO: do it. 3.0 intro content style uses text element as
                // alt text. We can load XHTML content here.
            }
        }
        return dom;
    }

    private void reinjectDynamicContent(Document dom, Element contentProviderElement) {

        Element contentProviderDiv = TalendHtmlModelUtil.getElementById(dom,
                contentProviderElement.getAttribute(IIntroHTMLConstants.ATTRIBUTE_ID), TalendHtmlModelUtil.TAG_DIV);
        if (contentProviderDiv != null) {
            contentProviderDiv.getParentNode().replaceChild(contentProviderElement, contentProviderDiv);
        }
    }

    private void loadDownloadedExtensionsFromFile() {
        try {
            List<ComponentExtension> extensions = ExchangeUtils.loadDownloadedComponents(ExchangeConstants.COMPONENT_MODEL_FILE);
            for (ComponentExtension ext : extensions) {
                fDownloadedExtensions.put(ext.getIdExtension(), ext);
            }
        } catch (Throwable e) {
            // do nothing, the file may not exist because this is the first time
            // we use this view and we haven't
            // installed any extensions
        }
    }

    public ComponentExtension getSelectedExtension() {
        return this.selectedExtension;
    }

    public void setSelectedExtension(ComponentExtension selected) {
        this.selectedExtension = selected;
    }

    public void updateAvailableExtensions(List<ComponentExtension> extensions) {
        fAvailableExtensions.clear();
        if (extensions != null && !extensions.isEmpty()) {
            fAvailableExtensions = extensions;
        }
    }

    public void updateInstalledExtensions(List<ComponentExtension> extensions) {
        fInstalledExtensions.clear();
        if (extensions != null && !extensions.isEmpty()) {
            fInstalledExtensions = extensions;
        }
    }

    public void updateContributedExtensions(List<ComponentExtension> extensions) {
        fContributedExtensions.clear();
        if (extensions != null && !extensions.isEmpty()) {
            fContributedExtensions = extensions;
        }
    }

    public void updateVersionRevisionsAndCategorys(List<VersionRevision> versionRevisions, List<Category> categorys) {
        fVersionRevisions.clear();
        if (versionRevisions != null && !versionRevisions.isEmpty()) {
            fVersionRevisions = versionRevisions;
        }
    }

    public List<VersionRevision> getVersionRevisions() {
        if (fVersionRevisions == null) {
            return new ArrayList<VersionRevision>();
        }
        return this.fVersionRevisions;
    }

    public String getAvialableFilter() {
        return avialableFilter;
    }

    public void setAvialableFilter(String avialableFilter) {
        this.avialableFilter = avialableFilter;
    }

    public void saveDownloadedExtensionsToFile(ComponentExtension extension) {
        try {
            if (fDownloadedExtensions.get(extension.getIdExtension()) == null) {
                ArrayList<ComponentExtension> components = new ArrayList<ComponentExtension>(fDownloadedExtensions.values());
                components.add(extension);
                ExchangeUtils.saveDownloadedComponents(ExchangeConstants.COMPONENT_MODEL_FILE, components);
            }
        } catch (IOException e) {
            ExceptionHandler.process(e);
        }
    }

    public void setBrowser(Browser browser) {
        this.browser = browser;
    }

    public Browser getBrowser() {
        return browser;
    }

    public Document getDocument() {
        return this.dom;
    }
}
