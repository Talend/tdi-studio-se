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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.intro.config.IIntroContentProviderSite;
import org.eclipse.ui.intro.config.IIntroXHTMLContentProvider;
import org.talend.designer.components.exchange.i18n.Messages;
import org.talend.designer.components.exchange.model.ComponentExtension;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class DownloadExtensionProvider implements IIntroXHTMLContentProvider {

    private List<ComponentExtension> fInstalledExtensions;

    private Map<String, ComponentExtension> fDownloadedExtensions;

    private static final String RELEASE_DATE_FORMAT = "yyyy-MM-dd"; //$NON-NLS-1$

    protected DateFormat formatter = new SimpleDateFormat(RELEASE_DATE_FORMAT);

    private static int count = 0;

    public static Map<String, ComponentExtension> componentMap = new HashMap<String, ComponentExtension>();

    public void setfInstalledExtensions(List<ComponentExtension> fInstalledExtensions) {
        this.fInstalledExtensions = fInstalledExtensions;
    }

    public void setfDownloadedExtensions(Map<String, ComponentExtension> fDownloadedExtensions) {
        this.fDownloadedExtensions = fDownloadedExtensions;
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
        if (ContentConstants.DOWNLOADEXTENSION_DATA.equals(id)) {
            Element tr = dom.createElement("tr");

            Element td = dom.createElement("td");
            String style = "border-top:1px solid #9BB9F5;border-left:1px solid #9BB9F5;background:#EBEBDB";
            if (fInstalledExtensions == null || fInstalledExtensions.isEmpty()) {
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
            if (fInstalledExtensions == null || fInstalledExtensions.isEmpty()) {
                style = style + ";border-bottom:1px solid #9BB9F5";
            }
            td.setAttribute("style", style);
            span = dom.createElement("span");
            span.setAttribute("class", "style_2 style_3");
            span.appendChild(dom.createTextNode("Downloaded Version"));
            td.appendChild(span);
            tr.appendChild(td);

            td = dom.createElement("td");
            style = "border-top:1px solid #9BB9F5;border-left:1px solid #cccccc;background:#EBEBDB";
            if (fInstalledExtensions == null || fInstalledExtensions.isEmpty()) {
                style = style + ";border-bottom:1px solid #9BB9F5";
            }
            td.setAttribute("style", style);
            span = dom.createElement("span");
            span.setAttribute("class", "style_2 style_3");
            span.appendChild(dom.createTextNode("Download Date"));
            td.appendChild(span);
            tr.appendChild(td);

            td = dom.createElement("td");
            style = "border-top:1px solid #9BB9F5;border-left:1px solid #cccccc;border-right:1px solid #9BB9F5;background:#EBEBDB";
            if (fInstalledExtensions == null || fInstalledExtensions.isEmpty()) {
                style = style + ";border-bottom:1px solid #9BB9F5";
            }
            td.setAttribute("style", style);
            span = dom.createElement("span");
            span.setAttribute("class", "style_2 style_3");
            span.appendChild(dom.createTextNode("Install/Update"));
            td.appendChild(span);
            tr.appendChild(td);

            parent.appendChild(tr);
            componentMap.clear();
            if (fInstalledExtensions != null) {
                Iterator ite = fInstalledExtensions.iterator();
                int size = fInstalledExtensions.size();
                count = 0;
                while (ite.hasNext()) {
                    count++;
                    ComponentExtension object = (ComponentExtension) ite.next();
                    // hide null entries directly
                    if (object.getLabel().equals("null")) {
                        continue;
                    }
                    // set 0.1 as a default version
                    if (object.getDownloadedVersion().equals("") || object.getDownloadedVersion().equals("null")) {
                        object.setDownloadedVersion("0.1");
                    }
                    Element trExtension = dom.createElement("tr");

                    Element tdExtension = dom.createElement("td");
                    style = "border-top:1px solid #cccccc;border-left:1px solid #9BB9F5";
                    if (count == size) {
                        style = "border-top:1px solid #cccccc;border-left:1px solid #9BB9F5;border-bottom:1px solid #9BB9F5";
                    }
                    if (count % 2 == 0) {
                        style = style + ";background:#F7F7F7";
                    }
                    tdExtension.setAttribute("style", style);
                    tdExtension.appendChild(dom.createTextNode(object.getLabel()));
                    trExtension.appendChild(tdExtension);

                    tdExtension = dom.createElement("td");
                    style = "border-top:1px solid #cccccc;border-left:1px solid #cccccc";
                    if (count == size) {
                        style = "border-top:1px solid #cccccc;border-left:1px solid #cccccc;border-bottom:1px solid #9BB9F5";
                    }
                    if (count % 2 == 0) {
                        style = style + ";background:#F7F7F7";
                    }
                    tdExtension.setAttribute("style", style);
                    // set 0.1 as a default version
                    tdExtension.appendChild(dom.createTextNode(object.getDownloadedVersion()));
                    trExtension.appendChild(tdExtension);

                    tdExtension = dom.createElement("td");
                    style = "border-top:1px solid #cccccc;border-left:1px solid #cccccc";
                    if (count == size) {
                        style = "border-top:1px solid #cccccc;border-left:1px solid #cccccc;border-bottom:1px solid #9BB9F5";
                    }
                    if (count % 2 == 0) {
                        style = style + ";background:#F7F7F7";
                    }
                    tdExtension.setAttribute("style", style);
                    tdExtension.appendChild(dom.createTextNode(formatter.format(object.getDateDownload())));
                    trExtension.appendChild(tdExtension);

                    if (object != null && fDownloadedExtensions != null
                            && fDownloadedExtensions.containsKey(object.getIdExtension())) {
                        if (object.getVersionExtension().equals(object.getDownloadedVersion())) {
                            tdExtension = dom.createElement("td");
                            style = "border-top:1px solid #cccccc;border-left:1px solid #cccccc;border-right:1px solid #9BB9F5";
                            if (count == size) {
                                style = "border-top:1px solid #cccccc;border-left:1px solid #cccccc;border-right:1px solid #9BB9F5;border-bottom:1px solid #9BB9F5";
                            }
                            if (count % 2 == 0) {
                                style = style + ";background:#F7F7F7";
                            }
                            tdExtension.setAttribute("style", style);
                            Text view = dom.createTextNode(Messages
                                    .getString("DownloadedExtensionsComposite.installedOperateStatus"));
                            tdExtension.appendChild(view);
                            trExtension.appendChild(tdExtension);
                        } else {
                            tdExtension = dom.createElement("td");
                            style = "border-top:1px solid #cccccc;border-left:1px solid #cccccc;border-right:1px solid #9BB9F5";
                            if (count == size) {
                                style = "border-top:1px solid #cccccc;border-left:1px solid #cccccc;border-right:1px solid #9BB9F5;border-bottom:1px solid #9BB9F5";
                            }
                            if (count % 2 == 0) {
                                style = style + ";background:#F7F7F7";
                            }
                            tdExtension.setAttribute("style", style);
                            Element hyperlink = dom.createElement("a");
                            String url = "http://org.eclipse.ui.intro/runAction?pluginId=org.talend.designer.components.exchange&"
                                    + "class=org.talend.designer.components.exchange.ui.actions.UpdateAction&"
                                    + "id=org.talend.designer.components.exchange.ui.actions.UpdateAction&"
                                    + AvailableCompositeProvider.NUMBER + "=" + count;
                            ;
                            hyperlink.setAttribute("href", url);
                            Text view = dom.createTextNode(Messages
                                    .getString("DownloadedExtensionsComposite.updateOperateStatus"));
                            hyperlink.appendChild(view);
                            tdExtension.appendChild(hyperlink);
                            trExtension.appendChild(tdExtension);
                        }
                    } else {
                        tdExtension = dom.createElement("td");
                        style = "border-top:1px solid #cccccc;border-left:1px solid #cccccc;border-right:1px solid #9BB9F5";
                        if (count == size) {
                            style = "border-top:1px solid #cccccc;border-left:1px solid #cccccc;border-right:1px solid #9BB9F5;border-bottom:1px solid #9BB9F5";
                        }
                        if (count % 2 == 0) {
                            style = style + ";background:#F7F7F7";
                        }
                        tdExtension.setAttribute("style", style);
                        Element hyperlink = dom.createElement("a");
                        String url = "http://org.eclipse.ui.intro/runAction?pluginId=org.talend.designer.components.exchange&"
                                + "class=org.talend.designer.components.exchange.ui.actions.InstallAction&"
                                + "id=org.talend.designer.components.exchange.ui.actions.InstallAction&"
                                + AvailableCompositeProvider.NUMBER + "=" + count;
                        hyperlink.setAttribute("href", url);
                        Text view = dom.createTextNode(Messages.getString("DownloadedExtensionsComposite.installOperateStatus"));
                        hyperlink.appendChild(view);
                        tdExtension.appendChild(hyperlink);
                        trExtension.appendChild(tdExtension);
                    }

                    componentMap.put("" + count, object);
                    parent.appendChild(trExtension);
                }
            }

        }
    }

}
