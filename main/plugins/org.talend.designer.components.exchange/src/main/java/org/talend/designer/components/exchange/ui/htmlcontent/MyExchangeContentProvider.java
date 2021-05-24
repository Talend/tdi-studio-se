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

import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.intro.config.IIntroContentProviderSite;
import org.eclipse.ui.intro.config.IIntroXHTMLContentProvider;
import org.talend.designer.components.exchange.model.ComponentExtension;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * DOC talend class global comment. Detailled comment
 */
public class MyExchangeContentProvider implements IIntroXHTMLContentProvider {

    public static Map<String, ComponentExtension> componentMap = new HashMap<String, ComponentExtension>();

    private ComponentExtension componentExtension;

    private List<ComponentExtension> myExtensions = new ArrayList<ComponentExtension>();

    private static final String RELEASE_DATE_FORMAT = "yyyy-MM-dd"; //$NON-NLS-1$

    private DateFormat formatter = new SimpleDateFormat(RELEASE_DATE_FORMAT);

    public void init(IIntroContentProviderSite site) {
    }

    public void createContent(String id, PrintWriter out) {
    }

    public void createContent(String id, Composite parent, FormToolkit toolkit) {
    }

    public void dispose() {
    }

    public void createContent(String id, Element parent) {
        Document dom = parent.getOwnerDocument();
        if (ContentConstants.LAST_AVAILABLE_VERSION.equals(id)) {
            if (componentExtension != null) {
                Element inputText = dom.createElement("input");
                inputText.setAttribute("id", "lastVersionAvailable_update");
                inputText.setAttribute("name", "lastVersionAvailable");
                inputText.setAttribute("type", "text");
                inputText.setAttribute("style", "width:300px;margin-left:5px");
                inputText.setAttribute("value", componentExtension.getLastVersionAvailable());
                parent.appendChild(inputText);
            }
        } else if (ContentConstants.EXTENSION_LABEL.equals(id)) {
            if (componentExtension != null) {
                Element inputText = dom.createElement("input");
                inputText.setAttribute("id", "extension_label_modify");
                inputText.setAttribute("name", "extension_label");
                inputText.setAttribute("type", "text");
                inputText.setAttribute("style", "width:300px;margin-left:5px;background:#AAAAAA;");
                inputText.setAttribute("value", componentExtension.getLabel());
                inputText.setAttribute("readonly", "true");
                parent.appendChild(inputText);
            }
        } else if (ContentConstants.LIST_MY_EXTENSION.equals(id)) {
            Element tr = dom.createElement("tr");

            Element td = dom.createElement("td");
            String style = "border-top:1px solid #9BB9F5;border-left:1px solid #9BB9F5;background:#EBEBDB";
            if (myExtensions == null || myExtensions.isEmpty()) {
                style = style + ";border-bottom:1px solid #9BB9F5";
            }
            td.setAttribute("style", style);
            Element span = dom.createElement("span");
            span.setAttribute("class", "style_2 style_3");
            span.appendChild(dom.createTextNode("Extension Name"));
            td.appendChild(span);
            tr.appendChild(td);

            td = dom.createElement("td");
            style = "border-top:1px solid #9BB9F5;border-left:1px solid #cccccc;background:#EBEBDB";
            if (myExtensions == null || myExtensions.isEmpty()) {
                style = style + ";border-bottom:1px solid #9BB9F5";
            }
            td.setAttribute("style", style);
            span = dom.createElement("span");
            span.setAttribute("class", "style_2 style_3");
            span.appendChild(dom.createTextNode("Version"));
            td.appendChild(span);
            tr.appendChild(td);

            td = dom.createElement("td");
            style = "border-top:1px solid #9BB9F5;border-left:1px solid #cccccc;background:#EBEBDB";
            if (myExtensions == null || myExtensions.isEmpty()) {
                style = style + ";border-bottom:1px solid #9BB9F5";
            }
            td.setAttribute("style", style);
            span = dom.createElement("span");
            span.setAttribute("class", "style_2 style_3");
            span.appendChild(dom.createTextNode("Upload Date"));
            td.appendChild(span);
            tr.appendChild(td);

            td = dom.createElement("td");
            style = "border-top:1px solid #9BB9F5;border-left:1px solid #cccccc;border-right:1px solid #9BB9F5;background:#EBEBDB";
            if (myExtensions == null || myExtensions.isEmpty()) {
                style = style + ";border-bottom:1px solid #9BB9F5";
            }
            td.setAttribute("style", style);
            span = dom.createElement("span");
            span.setAttribute("class", "style_2 style_3");
            span.appendChild(dom.createTextNode("Operation"));
            td.appendChild(span);
            tr.appendChild(td);

            parent.appendChild(tr);

            componentMap.clear();

            if (myExtensions == null || myExtensions.isEmpty()) {
                return;
            }
            for (int i = 0; i < myExtensions.size(); i++) {
                ComponentExtension extension = myExtensions.get(i);
                Element trExtension = dom.createElement("tr");

                // extension name
                Element tdExtension = dom.createElement("td");
                style = "border-top:1px solid #cccccc;border-left:1px solid #9BB9F5";
                if (i == myExtensions.size() - 1) {
                    style = "border-top:1px solid #cccccc;border-left:1px solid #9BB9F5;border-bottom:1px solid #9BB9F5";
                }
                if (i % 2 == 0) {
                    style = style + ";background:#F7F7F7";
                }
                tdExtension.setAttribute("style", style);
                tdExtension.appendChild(dom.createTextNode(extension.getLabel() == null ? "" : extension.getLabel()));
                trExtension.appendChild(tdExtension);

                // version
                tdExtension = dom.createElement("td");

                style = "border-top:1px solid #cccccc;border-left:1px solid #cccccc";
                if (i == myExtensions.size() - 1) {
                    style = "border-top:1px solid #cccccc;border-left:1px solid #cccccc;border-bottom:1px solid #9BB9F5";
                }
                if (i % 2 == 0) {
                    style = style + ";background:#F7F7F7";
                }
                tdExtension.setAttribute("style", style);
                tdExtension.appendChild(dom.createTextNode(extension.getLastVersionAvailable() == null ? "" : extension
                        .getLastVersionAvailable()));
                trExtension.appendChild(tdExtension);

                // upload date
                tdExtension = dom.createElement("td");
                style = "border-top:1px solid #cccccc;border-left:1px solid #cccccc";
                if (i == myExtensions.size() - 1) {
                    style = "border-top:1px solid #cccccc;border-left:1px solid #cccccc;border-bottom:1px solid #9BB9F5";
                }
                if (i % 2 == 0) {
                    style = style + ";background:#F7F7F7";
                }
                tdExtension.setAttribute("style", style);
                Date publicationDate = extension.getPublicationDate();
                tdExtension.appendChild(dom.createTextNode(publicationDate == null ? "" : formatter.format(publicationDate)));
                trExtension.appendChild(tdExtension);

                // operations
                tdExtension = dom.createElement("td");
                style = "border-top:1px solid #cccccc;border-left:1px solid #cccccc;border-right:1px solid #9BB9F5";
                if (i == myExtensions.size() - 1) {
                    style = "border-top:1px solid #cccccc;border-left:1px solid #cccccc;border-right:1px solid #9BB9F5;border-bottom:1px solid #9BB9F5";
                }
                if (i % 2 == 0) {
                    style = style + ";background:#F7F7F7";
                }
                tdExtension.setAttribute("style", style);
                String actionUrl = "http://org.eclipse.ui.intro/runAction?pluginId=org.talend.designer.components.exchange&"
                        + "class=org.talend.designer.components.exchange.ui.htmlcontent.MyExtensionAction&"
                        + "id=org.talend.designer.components.exchange.ui.htmlcontent.MyExtensionAction&";

                Element hyperlink = dom.createElement("a");
                String url = actionUrl + ContentConstants.KEY_TYPE + "=" + ContentConstants.UPLOAD_NEW_VERSION_ACTION + "&"
                        + ContentConstants.KEY_EXTENSION_ID + "=" + extension.getIdExtension();
                hyperlink.setAttribute("href", url);
                Element imgElem = dom.createElement("img");
                imgElem.setAttribute("src", "imgs/upload.gif");
                imgElem.setAttribute("title", "Upload New Version");
                hyperlink.appendChild(imgElem);
                tdExtension.appendChild(hyperlink);

                hyperlink = dom.createElement("a");
                url = actionUrl + ContentConstants.KEY_TYPE + "=" + ContentConstants.MODIFY_ACTION + "&"
                        + ContentConstants.KEY_EXTENSION_ID + "=" + extension.getIdExtension();
                hyperlink.setAttribute("href", url);
                imgElem = dom.createElement("img");
                imgElem.setAttribute("src", "imgs/modify.gif");
                imgElem.setAttribute("title", "Modify");
                hyperlink.appendChild(imgElem);
                tdExtension.appendChild(hyperlink);

                hyperlink = dom.createElement("a");
                url = actionUrl + ContentConstants.KEY_TYPE + "=" + ContentConstants.DELETE_ACTION + "&"
                        + ContentConstants.KEY_EXTENSION_ID + "=" + extension.getIdExtension();
                hyperlink.setAttribute("href", url);
                imgElem = dom.createElement("img");
                imgElem.setAttribute("src", "imgs/delete.gif");
                imgElem.setAttribute("title", "Delete");
                hyperlink.appendChild(imgElem);
                tdExtension.appendChild(hyperlink);

                trExtension.appendChild(tdExtension);

                componentMap.put(extension.getIdExtension(), extension);
                parent.appendChild(trExtension);
            }
        }

    }

    public void setComponentExtension(ComponentExtension componentExtension) {
        this.componentExtension = componentExtension;
    }

    public void setfContributeExtensions(List<ComponentExtension> myExtensions) {
        this.myExtensions = myExtensions;
    }

}
