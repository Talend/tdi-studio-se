// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.componentdesigner.ui.composite;

import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.talend.componentdesigner.PluginConstant;
import org.talend.componentdesigner.ui.composite.xmltree.TreeNodeData;
import org.w3c.dom.Element;

/**
 * DOC rli class global comment. Detailled comment
 */
public class TreeNodeAttrCompsite extends Composite {

    private static final String MARK = ":"; //$NON-NLS-1$

    private final TreeNodeData nodeData;

    public TreeNodeAttrCompsite(Composite parent, int style, TreeNodeData nodeData) {
        super(parent, style);
        this.nodeData = nodeData;
        GridLayout layout = new GridLayout(2, false);
        setLayout(layout);
    }

    public void creatPart(Map<String, String[]> attrsValuesAvailableMap, Map<String, String> attrsToolTipsMap) {

        GridData gd = null;
        if ((!nodeData.isHasTreeAttr()) && (!nodeData.isHasChildTreeNode())) {
            Label label = new Label(this, SWT.NONE);
            label.setText(nodeData.getXMLNode().getNodeName() + MARK);
            gd = new GridData();
            gd.horizontalSpan = 2;
            label.setLayoutData(gd);
            final Text nodeText = new Text(this, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
            gd = new GridData(GridData.FILL_BOTH);
            gd.horizontalSpan = 2;
            nodeText.setLayoutData(gd);
            nodeText.setText(nodeData.getBodayText());

            nodeText.addModifyListener(new ModifyListener() {

                public void modifyText(ModifyEvent e) {
                    nodeData.getXMLNode().setTextContent(nodeText.getText());
                    nodeData.setBodayText(nodeText.getText());
                }
            });
        } else {
            for (Object attrName : nodeData.getTreeAttrNames()) {
                final Label attrLabel = new Label(this, SWT.NONE);
                attrLabel.setText(attrName + MARK);
                attrLabel.setLayoutData(new GridData());
                String[] valuesArray = attrsValuesAvailableMap.get(attrName);
                String tooTipsText = attrsToolTipsMap.get(attrName);

                if (valuesArray == null) {
                    final Text attrText = new Text(this, SWT.BORDER);
                    gd = new GridData(GridData.FILL_HORIZONTAL);
                    attrText.setLayoutData(gd);
                    if (nodeData.getAttrValue((String) attrName) != null) {
                        attrText.setText(nodeData.getAttrValue((String) attrName));
                    }

                    if (tooTipsText != null) {
                        attrText.setToolTipText(tooTipsText);
                    }

                    attrText.addModifyListener(new ModifyListener() {

                        public void modifyText(ModifyEvent e) {
                            String attrName = attrLabel.getText().split(MARK)[0];
                            if (attrText.getText().equals(PluginConstant.EMPTY_STRING)) {
                                ((Element) nodeData.getXMLNode()).removeAttribute(attrName);
                                nodeData.removeAttrValue(attrName);
                            } else {
                                ((Element) nodeData.getXMLNode()).setAttribute(attrName, attrText.getText());
                                nodeData.putAttrValue(attrName, attrText.getText());
                            }
                        }

                    });
                } else {
                    final Combo attrCombo = new Combo(this, SWT.BORDER);
                    gd = new GridData(GridData.FILL_HORIZONTAL);
                    attrCombo.setLayoutData(gd);
                    attrCombo.setItems(valuesArray);
                    String attrValue = nodeData.getAttrValue((String) attrName);
                    if (attrValue != null) {
                        int index = -1;
                        for (int i = 0; i < valuesArray.length; i++) {
                            if (valuesArray[i].equals(attrValue)) {
                                index = i;
                                break;
                            }
                        }

                        if (index == -1) {
                            attrCombo.setText(attrValue);
                        } else {
                            attrCombo.select(index);

                        }
                    }

                    if (tooTipsText != null) {
                        attrCombo.setToolTipText(tooTipsText);
                    }

                    attrCombo.addModifyListener(new ModifyListener() {

                        public void modifyText(ModifyEvent e) {
                            String attrName = attrLabel.getText().split(MARK)[0];
                            if (attrCombo.getText().equals(PluginConstant.EMPTY_STRING)) {
                                ((Element) nodeData.getXMLNode()).removeAttribute(attrName);
                                nodeData.removeAttrValue(attrName);
                            } else {
                                ((Element) nodeData.getXMLNode()).setAttribute(attrName, attrCombo.getText());
                                nodeData.putAttrValue(attrName, attrCombo.getText());
                            }
                        }

                    });
                }

            }
        }
    }
}
