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
package org.talend.designer.xmlmap.ui.dialog;

import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.talend.commons.ui.swt.formtools.LabelledText;
import org.talend.designer.xmlmap.model.emf.xmlmap.NodeType;
import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;
import org.talend.designer.xmlmap.util.XmlMapUtil;
import org.talend.metadata.managment.ui.wizard.metadata.xml.utils.StringUtil;

/**
 * DOC wchen class global comment. Detailled comment
 */
public class NameSpaceDialog extends TitleAreaDialog {

    private Text prefixText;

    private LabelledText nsValueLabel;

    private Button prefixBtn;

    private static final String DEFAULT = "Input the prefix and value of the namespace";

    private String prefixValue = "";

    private String nsValue;

    private TreeNode currentNode;

    private TreeNode parentNode;

    /**
     * wchen NameSpaceDialog constructor comment.
     *
     * @param parentShell
     */
    public NameSpaceDialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    protected Control createContents(Composite parent) {
        super.createContents(parent);
        this.getShell().setText("Namespace dialog");
        this.setTitle("Input new namespace");
        this.setMessage("Input the prefix and value of the namespace");
        validateField();
        return parent;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        super.createDialogArea(parent);
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(6, true));

        nsValueLabel = new LabelledText(composite, "Namespace", 5); //$NON-NLS-1$
        nsValueLabel.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                nsValue = nsValueLabel.getText();
                validateField();
            }

        });

        prefixBtn = new Button(composite, SWT.CHECK | SWT.RIGHT);
        prefixBtn.setText("Prefix");

        prefixBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (prefixText != null) {
                    if (prefixBtn.getSelection()) {
                        prefixText.setVisible(true);
                    } else {
                        prefixText.setVisible(false);
                        prefixText.setText("");
                    }
                    validateField();
                }
            }
        });

        prefixText = new Text(composite, SWT.BORDER); //$NON-NLS-1$
        GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.horizontalSpan = 5;
        prefixText.setLayoutData(gridData);
        prefixText.setVisible(false);
        prefixText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                prefixValue = prefixText.getText();
                validateField();
            }
        });

        if (currentNode != null) {
            nsValueLabel.setText(currentNode.getDefaultValue());

            String prefix = currentNode.getName();
            if (prefix != null && !"".equals(prefix) && !XmlMapUtil.DEFAULT_NAME_SPACE_PREFIX.equals(prefix)) {
                prefixBtn.setSelection(true);
                prefixText.setVisible(true);
                prefixText.setText(prefix);
            }
        }

        nsValue = nsValueLabel.getText();
        if (prefixText.isVisible()) {
            prefixValue = prefixText.getText();
        }
        return parent;

    }

    private void validateField() {
        if (nsValue == null || "".equals(nsValue)) {
            setMessage(DEFAULT, 5); //$NON-NLS-1$
            return;
        }

        if (!StringUtil.validateLabelForNameSpace(prefixValue)) {
            setMessage("Prefix value is invalid!", IMessageProvider.ERROR); //$NON-NLS-1$
            return;
        } else {
            setMessage(DEFAULT);
        }
        if (!StringUtil.validateLabelForFixedValue(nsValue)) {
            setMessage("NameSpace value is invalid!", IMessageProvider.ERROR); //$NON-NLS-1$
            return;
        } else {
            setMessage(DEFAULT);
        }

        if (!isValidPrefix(prefixValue)) {
            String errorMessage = "";
            if (prefixValue == null || "".equals(prefixValue)) {
                errorMessage = "The default prefix already eixst,please input a prefix!";
            } else {
                errorMessage = "Prefix '" + prefixValue + "' already exist!";
            }

            setMessage(errorMessage, IMessageProvider.ERROR); //$NON-NLS-1$
            return;
        } else {
            setMessage(DEFAULT);
        }
    }

    @Override
    public void setMessage(String newMessage, int newType) {
        super.setMessage(newMessage, newType);
        Button button = getButton(IDialogConstants.OK_ID);
        if (button != null && !button.isDisposed()) {
            if (!DEFAULT.equals(newMessage)) { // error
                button.setEnabled(false);
            } else {
                if (newType == 5) {
                    button.setEnabled(false);
                } else {
                    button.setEnabled(true);
                }
            }
        }
    }

    public String getPrefix() {
        return this.prefixValue;
    }

    public String getNSValue() {
        return this.nsValue;
    }

    private boolean isValidPrefix(String prefix) {
        if (parentNode == null) {
            return false;
        }
        if (prefix == null || "".equals(prefix)) {
            prefix = XmlMapUtil.DEFAULT_NAME_SPACE_PREFIX;
        }
        String xpath = XmlMapUtil.getXPath(parentNode.getXpath(), prefix, NodeType.NAME_SPACE);
        EList<TreeNode> children = parentNode.getChildren();
        boolean exist = false;
        for (TreeNode child : children) {
            if (child == currentNode) {
                continue;
            }
            if (NodeType.NAME_SPACE.equals(child.getNodeType()) && child.getXpath() != null && child.getXpath().equals(xpath)) {
                exist = true;
                break;
            }
        }
        return !exist;

    }

    public void setCurrentNode(TreeNode currentNode) {
        this.currentNode = currentNode;
    }

    public void setParentNode(TreeNode parentNode) {
        this.parentNode = parentNode;
    }

}
