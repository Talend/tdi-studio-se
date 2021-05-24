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

import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import org.eclipse.emf.common.util.EList;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.intro.config.IIntroContentProviderSite;
import org.eclipse.ui.intro.config.IIntroXHTMLContentProvider;
import org.talend.designer.components.exchange.model.AvailableExtensionViewDetail;
import org.talend.designer.components.exchange.model.ComponentExtension;
import org.talend.designer.components.exchange.util.ExchangeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * DOC talend class global comment. Detailled comment
 */
public class ExchangeContentProvider implements IIntroXHTMLContentProvider {

    protected DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    private ComponentExtension componentExtension;

    public void init(IIntroContentProviderSite site) {
    }

    public void createContent(String id, PrintWriter out) {
    }

    public void createContent(String id, Composite parent, FormToolkit toolkit) {
    }

    public void dispose() {
    }

    public void createContent(String id, Element parent) {
        if (componentExtension != null) {
            Document dom = parent.getOwnerDocument();
            if (ContentConstants.LEFT_NAME_PART.equals(id)) {
                Element span = dom.createElement("span");
                span.setAttribute("class", "style_1 style_2 style_3");
                span.appendChild(dom.createTextNode(componentExtension.getLabel()));
                parent.appendChild(span);
                parent.appendChild(dom.createElement("br"));

                span = dom.createElement("span");
                span.appendChild(dom.createTextNode("Version " + componentExtension.getVersionExtension()));
                parent.appendChild(span);
                parent.appendChild(dom.createElement("br"));

                span = dom.createElement("span");
                span.appendChild(dom.createTextNode(formatter.format(componentExtension.getPublicationDate())));
                parent.appendChild(span);
            } else if (ContentConstants.COMPONENT_DESCRIPTION.equals(id)) {
                String description = componentExtension.getDescription();
                if (description != null) {
                    String[] split = description.split("/n/r|\\n\\r");
                    for (int i = 0; i < split.length; i++) {
                        String text = split[i];
                        if (!text.trim().equals("")) {
                            parent.appendChild(dom.createTextNode(text));
                        }
                        if (i < split.length - 1) {
                            String text1 = split[i + 1];
                            if (!text1.trim().equals("")) {
                                parent.appendChild(dom.createElement("br"));
                            }
                        }
                    }
                }
            } else if (ContentConstants.RATE_IMANGE.equals(id)) {
                String rateImage = getRateImage(componentExtension.getRate());
                Element imgElem = dom.createElement("img");
                imgElem.setAttribute("src", rateImage);
                parent.appendChild(imgElem);
            } else if (ContentConstants.INSTALL_COMPONENT.equals(id)) {
                Element hyperlink = dom.createElement("a");
                String url = "http://org.eclipse.ui.intro/runAction?pluginId=org.talend.designer.components.exchange&"
                        + "class=org.talend.designer.components.exchange.ui.actions.DownloadComponenentsAction&"
                        + "id=org.talend.designer.components.exchange.ui.actions.DownloadComponenentsAction";
                hyperlink.setAttribute("href", url);

                Element imgElem = dom.createElement("img");
                imgElem.setAttribute("src", "imgs/install.png");
                hyperlink.appendChild(imgElem);

                parent.appendChild(hyperlink);
            } else if (ContentConstants.EXTENSION_REVIEWS.equals(id)) {
                EList<AvailableExtensionViewDetail> reviews = componentExtension.getReviews();
                for (AvailableExtensionViewDetail detail : reviews) {
                    String desc = " " + detail.getTitle() + "\n\t" + detail.getComment();
                    String image = getRateImage(detail.getReviewrate());
                    Element imgElem = dom.createElement("img");
                    imgElem.setAttribute("src", image);
                    parent.appendChild(imgElem);
                    Element b = dom.createElement("b");
                    b.setTextContent(detail.getTitle());
                    parent.appendChild(b);
                    parent.appendChild(dom.createTextNode(" : "+detail.getComment()+ " ("+detail.getAuthor()+")"));
                    parent.appendChild(dom.createElement("br"));
                }
            } else if (ContentConstants.WRITE_REVIEWS.equals(id)) {
                Element hyperlink = dom.createElement("a");
                String url = "http://org.eclipse.ui.intro/runAction?pluginId=org.talend.designer.components.exchange&"
                        + "class=org.talend.designer.components.exchange.ui.htmlcontent.ExchangeUrlAction&"
                        + "id=org.talend.designer.components.exchange.ui.htmlcontent.ExchangeUrlAction&" + "type=EDIT_REVIEWS";
                hyperlink.setAttribute("href", url);
                hyperlink.appendChild(dom.createTextNode("write a review"));
                parent.appendChild(hyperlink);
            } else if (ContentConstants.GET_EXTENSION_IMAGE.equals(id)) {
                String idExtension = componentExtension.getIdExtension();
                String typeExtension = ExchangeUtils.TYPEEXTENSION != null ? ExchangeUtils.TYPEEXTENSION : "tos";
                String imageUrl = "http://www.talendforge.org/exchange/" + typeExtension + "/upload_" + typeExtension
                        + "/extension-" + idExtension + "/thumbnail.jpg";

                boolean touchFile =false;
                try {
                    // Get the image
                    URL url = new URL(imageUrl);
                    InputStream is = url.openStream();
                    is.close();
                    touchFile = true;
                } catch (Exception e) {
                    touchFile =false;
                }

                if (touchFile) {
                    Element imgElem = dom.createElement("img");
                    imgElem.setAttribute("src", imageUrl);
                    imgElem.setAttribute("align", "middle");
                    parent.appendChild(imgElem);
                }
            }
        }
    }

    public void setComponentExtension(ComponentExtension componentExtension) {
        this.componentExtension = componentExtension;
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
