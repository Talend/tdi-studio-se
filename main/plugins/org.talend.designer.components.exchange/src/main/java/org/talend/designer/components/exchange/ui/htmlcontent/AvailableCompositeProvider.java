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
import java.text.DecimalFormat;
import java.util.ArrayList;
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
import org.w3c.dom.Text;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class AvailableCompositeProvider implements IIntroXHTMLContentProvider {

    public static final String NUMBER = "NUMBER";

    private List<ComponentExtension> fAvailableExtensions = new ArrayList<ComponentExtension>();

    public static Map<String, ComponentExtension> componentMap = new HashMap<String, ComponentExtension>();

    public void setfAvailableExtensions(List<ComponentExtension> fAvailableExtensions) {
        this.fAvailableExtensions = fAvailableExtensions;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.ui.intro.config.IIntroContentProvider#init(org.eclipse.ui.intro.config.IIntroContentProviderSite)
     */
    public void init(IIntroContentProviderSite site) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.intro.config.IIntroContentProvider#createContent(java.lang.String, java.io.PrintWriter)
     */
    public void createContent(String id, PrintWriter out) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.intro.config.IIntroContentProvider#createContent(java.lang.String,
     * org.eclipse.swt.widgets.Composite, org.eclipse.ui.forms.widgets.FormToolkit)
     */
    public void createContent(String id, Composite parent, FormToolkit toolkit) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.intro.config.IIntroContentProvider#dispose()
     */
    public void dispose() {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.intro.config.IIntroXHTMLContentProvider#createContent(java.lang.String, org.w3c.dom.Element)
     */
    public void createContent(String id, Element parent) {

        Document dom = parent.getOwnerDocument();
        if (ContentConstants.INSERT_EXTENSION_DATA.equals(id)) {
            Element tr = dom.createElement("tr");

            Element td = dom.createElement("td");
            String style = "border-top:1px solid #9BB9F5;border-left:1px solid #9BB9F5;background:#EBEBDB";
            if (fAvailableExtensions == null || fAvailableExtensions.isEmpty()) {
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
            if (fAvailableExtensions == null || fAvailableExtensions.isEmpty()) {
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
            if (fAvailableExtensions == null || fAvailableExtensions.isEmpty()) {
                style = style + ";border-bottom:1px solid #9BB9F5";
            }
            td.setAttribute("style", style);
            span = dom.createElement("span");
            span.setAttribute("class", "style_2 style_3");
            span.appendChild(dom.createTextNode("Rating"));
            td.appendChild(span);
            tr.appendChild(td);

            td = dom.createElement("td");
            style = "border-top:1px solid #9BB9F5;border-left:1px solid #cccccc;background:#EBEBDB";
            if (fAvailableExtensions == null || fAvailableExtensions.isEmpty()) {
                style = style + ";border-bottom:1px solid #9BB9F5";
            }
            td.setAttribute("style", style);
            span = dom.createElement("span");
            span.setAttribute("class", "style_2 style_3");
            span.appendChild(dom.createTextNode("Author"));
            td.appendChild(span);
            tr.appendChild(td);

            td = dom.createElement("td");
            style = "border-top:1px solid #9BB9F5;border-left:1px solid #cccccc;border-right:1px solid #9BB9F5;background:#EBEBDB";
            if (fAvailableExtensions == null || fAvailableExtensions.isEmpty()) {
                style = style + ";border-bottom:1px solid #9BB9F5";
            }
            td.setAttribute("style", style);
            span = dom.createElement("span");
            span.setAttribute("class", "style_2 style_3");
            span.appendChild(dom.createTextNode("View"));
            td.appendChild(span);
            tr.appendChild(td);

            parent.appendChild(tr);

            componentMap.clear();
            if (fAvailableExtensions != null) {
                for (int i = 0; i < fAvailableExtensions.size(); i++) {
                    ComponentExtension extension = fAvailableExtensions.get(i);
                    // hide null entries directly
                    if (extension.getLabel().equals("null")) {
                        continue;
                    }
                    // set 0.1 as a default version
                    if (extension.getVersionExtension().equals("") || extension.getVersionExtension().equals("null")) {
                        extension.setVersionExtension("0.1");
                    }
                    Element trExtension = dom.createElement("tr");

                    Element tdExtension = dom.createElement("td");
                    style = "border-top:1px solid #cccccc;border-left:1px solid #9BB9F5";
                    if (i == fAvailableExtensions.size() - 1) {
                        style = "border-top:1px solid #cccccc;border-left:1px solid #9BB9F5;border-bottom:1px solid #9BB9F5";
                    }
                    if (i % 2 == 0) {
                        style = style + ";background:#F7F7F7";
                    }
                    tdExtension.setAttribute("style", style);
                    tdExtension.appendChild(dom.createTextNode(extension.getLabel()));
                    trExtension.appendChild(tdExtension);

                    tdExtension = dom.createElement("td");
                    style = "border-top:1px solid #cccccc;border-left:1px solid #cccccc";
                    if (i == fAvailableExtensions.size() - 1) {
                        style = "border-top:1px solid #cccccc;border-left:1px solid #cccccc;border-bottom:1px solid #9BB9F5";
                    }
                    if (i % 2 == 0) {
                        style = style + ";background:#F7F7F7";
                    }
                    tdExtension.setAttribute("style", style);
                    tdExtension.appendChild(dom.createTextNode(extension.getVersionExtension()));
                    trExtension.appendChild(tdExtension);

                    tdExtension = dom.createElement("td");
                    style = "border-top:1px solid #cccccc;border-left:1px solid #cccccc";
                    if (i == fAvailableExtensions.size() - 1) {
                        style = "border-top:1px solid #cccccc;border-left:1px solid #cccccc;border-bottom:1px solid #9BB9F5";
                    }
                    if (i % 2 == 0) {
                        style = style + ";background:#F7F7F7";
                    }
                    tdExtension.setAttribute("style", style);
                    String rateImage = getRateImage(extension.getRate());
                    Element imgElem = dom.createElement("img");
                    imgElem.setAttribute("src", rateImage);
                    tdExtension.appendChild(imgElem);
                    trExtension.appendChild(tdExtension);

                    tdExtension = dom.createElement("td");
                    style = "border-top:1px solid #cccccc;border-left:1px solid #cccccc";
                    if (i == fAvailableExtensions.size() - 1) {
                        style = "border-top:1px solid #cccccc;border-left:1px solid #cccccc;border-bottom:1px solid #9BB9F5";
                    }
                    if (i % 2 == 0) {
                        style = style + ";background:#F7F7F7";
                    }
                    tdExtension.setAttribute("style", style);
                    tdExtension.appendChild(dom.createTextNode(extension.getAuthor()));
                    trExtension.appendChild(tdExtension);

                    tdExtension = dom.createElement("td");
                    style = "border-top:1px solid #cccccc;border-left:1px solid #cccccc;border-right:1px solid #9BB9F5";
                    if (i == fAvailableExtensions.size() - 1) {
                        style = "border-top:1px solid #cccccc;border-left:1px solid #cccccc;border-right:1px solid #9BB9F5;border-bottom:1px solid #9BB9F5";
                    }
                    if (i % 2 == 0) {
                        style = style + ";background:#F7F7F7";
                    }
                    tdExtension.setAttribute("style", style);
                    Element hyperlink = dom.createElement("a");
                    hyperlink.setAttribute(NUMBER, "" + i);
                    String url = "http://org.eclipse.ui.intro/runAction?pluginId=org.talend.designer.components.exchange&"
                            + "class=org.talend.designer.components.exchange.ui.actions.OpenExtensionViewDetailAction&"
                            + "id=org.talend.designer.components.exchange.ui.actions.OpenExtensionViewDetailAction&" + "NUMBER="
                            + i;
                    hyperlink.setAttribute("href", url);
                    Text view = dom.createTextNode("view/download");
                    hyperlink.appendChild(view);
                    tdExtension.appendChild(hyperlink);
                    trExtension.appendChild(tdExtension);

                    componentMap.put(hyperlink.getAttribute(NUMBER), extension);
                    parent.appendChild(trExtension);
                }
            }
        }

    }

    private String getRateImage(String rate) {
        int num = 0;
        if (rate != null && !"".equals(rate)) {
            double rates = Double.parseDouble(rate);
            DecimalFormat df = new DecimalFormat("#");
            num = Integer.parseInt(df.format(rates));
            switch (num) {
            case 0:
                return "imgs/star.png";
            case 1:
                return "imgs/star1.png";
            case 2:
                return "imgs/star2.png";
            case 3:
                return "imgs/star3.png";
            case 4:
                return "imgs/star4.png";
            case 5:
                return "imgs/star5.png";

            default:
                return "imgs/star.png";
            }
        }
        return "imgs/star.png";
    }

}
