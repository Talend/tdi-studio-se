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
package org.talend.designer.xmlmap.ui.tabs.table;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.talend.commons.ui.swt.advanced.dataeditor.AbstractDataTableEditorView;
import org.talend.commons.ui.swt.advanced.dataeditor.ExtendedToolbarView;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.designer.xmlmap.model.emf.xmlmap.NodeType;
import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;
import org.talend.designer.xmlmap.ui.tabs.table.toolbar.XmlMapExtendedToolbarView;
import org.talend.designer.xmlmap.util.XmlMapUtil;
import org.talend.metadata.managment.ui.wizard.metadata.xml.utils.StringUtil;

/**
 * WCHEN talend class global comment. Detailled comment
 */
public abstract class AbstractXmlTreeSchemaTableView extends AbstractDataTableEditorView<TreeNode> {

    public static final String ID_COLUMN_XPATH = "xpath";

    protected boolean isValidName = true;

    protected Composite parentComposite;

    private Composite mainComposite;

    private Label titleLabel;

    public AbstractXmlTreeSchemaTableView(Composite parentComposite, int mainCompositeStyle) {
        super(parentComposite, mainCompositeStyle, false);
    }

    protected String validateXPath(String newValue, int beanPosition) {
        if (beanPosition == -1) {
            return null;
        }
        isValidName = true;
        if (newValue == null || "".equals(newValue)) {
            isValidName = false;
            return "Name can't be null";
        }
        TreeNode bean = getExtendedTableModel().getBeansList().get(beanPosition);

        if (NodeType.NAME_SPACE == bean.getNodeType()) {
            final String validateNameSpace = validateNameSpace(newValue, beanPosition);
            if (validateNameSpace != null) {
                return validateNameSpace;
            }
        }

        if (!StringUtil.validateLabelForXML(newValue)) {
            if (NodeType.ELEMENT == bean.getNodeType()) {
                isValidName = false;
                return "Element name is invalid";
            } else if (NodeType.ATTRIBUT == bean.getNodeType()) {
                isValidName = false;
                return "Attribute name is invalid";
            }
        }

        return validateEntry(newValue, bean, beanPosition);
    }

    protected String validateEntry(String newValue, TreeNode bean, int beanPosition) {
        String newXPath = bean.getXpath();
        newXPath = newXPath.substring(0, newXPath.lastIndexOf(bean.getName()));
        newXPath = newXPath + newValue;
        if (getExtendedTableModel() != null) {
            for (int i = 0; i < getExtendedTableModel().getBeansList().size(); i++) {
                if (i == beanPosition) {
                    continue;
                }
                TreeNode entry = getExtendedTableModel().getBeansList().get(i);
                if (newXPath.equals(entry.getXpath())) {
                    isValidName = false;
                    return "Name alrady existed";
                }
            }
        }

        return null;
    }

    public void refresh() {
        this.getTableViewerCreator().getTableViewer().refresh();
    }

    protected String validateNameSpace(String newValue, int beanPosition) {
        if ((newValue.indexOf("(") != -1 || newValue.indexOf(")") != -1)
                && !newValue.equals(XmlMapUtil.DEFAULT_NAME_SPACE_PREFIX)) {
            isValidName = false;
            return "Namespace Prefix is invalid";
        }

        if (getExtendedTableModel() != null) {
            for (int i = 0; i < getExtendedTableModel().getBeansList().size(); i++) {
                if (i == beanPosition) {
                    continue;
                }
                TreeNode entry = getExtendedTableModel().getBeansList().get(i);
                if (NodeType.NAME_SPACE == entry.getNodeType() && newValue.equals(entry.getName())) {
                    isValidName = false;
                    return "Namespace Prefix already exist";
                }

            }
        }

        return null;

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.commons.ui.swt.advanced.dataeditor.AbstractDataTableEditorView#getExtendedTableModel()
     */
    @Override
    public TreeSchemaTableEditor getExtendedTableModel() {
        // TODO Auto-generated method stub
        return (TreeSchemaTableEditor) super.getExtendedTableModel();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.commons.ui.swt.advanced.dataeditor.AbstractDataTableEditorView#setTableViewerCreatorOptions(org.talend
     * .commons.ui.swt.tableviewer.TableViewerCreator)
     */
    @Override
    protected void setTableViewerCreatorOptions(TableViewerCreator<TreeNode> newTableViewerCreator) {
        super.setTableViewerCreatorOptions(newTableViewerCreator);
        // newTableViewerCreator.setLazyLoad(true);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.commons.ui.swt.advanced.dataeditor.AbstractDataTableEditorView#initToolBar()
     */
    @Override
    protected ExtendedToolbarView initToolBar() {
        return new XmlMapExtendedToolbarView(getMainComposite(), SWT.NONE, getExtendedTableViewer());
    }

}
