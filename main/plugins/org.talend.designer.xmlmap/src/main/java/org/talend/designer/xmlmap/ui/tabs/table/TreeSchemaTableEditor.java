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

import java.util.ArrayList;
import java.util.List;

import org.talend.commons.ui.swt.extended.table.ExtendedTableModel;
import org.talend.designer.xmlmap.model.emf.xmlmap.AbstractInOutTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.InputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;
import org.talend.designer.xmlmap.ui.tabs.table.utils.SchemaTableUtils;
import org.talend.designer.xmlmap.util.XmlMapUtil;

/**
 * created by wchen on 2013-1-23 Detailled comment
 *
 */
public class TreeSchemaTableEditor extends ExtendedTableModel<TreeNode> {

    AbstractInOutTree absTree;

    int pageCount = 1;

    int currentPage = 1;

    int maxColumns = 100;

    public TreeSchemaTableEditor(AbstractInOutTree absTree) {
        this.absTree = absTree;
        initData();
    }

    private void initData() {
        registerDataList(getSelectedInputTreeSchemaModel());
    }

    private List<TreeNode> getSelectedInputTreeSchemaModel() {

        List<TreeNode> beanList = new ArrayList<TreeNode>();
        List<TreeNode> tableModel = getAllBeanList();
        pageCount = tableModel.size() / maxColumns + (tableModel.size() % maxColumns == 0 ? 0 : 1);

        if (currentPage <= pageCount) {
            int startIndex = (currentPage - 1) * maxColumns;
            int endIndex = currentPage * maxColumns;
            endIndex = Math.min(tableModel.size(), endIndex);
            beanList.addAll(tableModel.subList(startIndex, endIndex));
        }

        return beanList;
    }

    private List<TreeNode> getAllBeanList() {
        List<TreeNode> tableModel = new ArrayList<TreeNode>();
        if (absTree != null) {
            List<TreeNode> nodes = new ArrayList<TreeNode>();
            if (absTree instanceof OutputXmlTree) {
                nodes.addAll(((OutputXmlTree) absTree).getNodes());
            } else {
                nodes.addAll(((InputXmlTree) absTree).getNodes());
            }
            for (TreeNode node : nodes) {
                if (XmlMapUtil.DOCUMENT.equals(node.getType())) {
                    tableModel.addAll(SchemaTableUtils.getTreeSchemaEnties(node));
                }
            }
        }
        return tableModel;
    }

    public void changePageIfNeeded(TreeNode firstSelection) {
        List<TreeNode> updatedList = getSelectedInputTreeSchemaModel();
        // update page
        if (updatedList.contains(firstSelection)) {
            List<TreeNode> beansList = getBeansList();
            List<TreeNode> copyToRemove = new ArrayList<TreeNode>(beansList);
            copyToRemove.removeAll(updatedList);
            if (!copyToRemove.isEmpty()) {
                removeAll(copyToRemove);
            }
            // check if any new node is added
            for (int i = 0; i < updatedList.size(); i++) {
                if (!beansList.contains(updatedList.get(i))) {
                    add(updatedList.get(i), i);
                }
            }
        } else {
            // change page
            List<TreeNode> allBeanList = getAllBeanList();
            int indexOf = allBeanList.indexOf(firstSelection);
            if (indexOf != -1) {
                currentPage = indexOf == 0 ? 1 : (indexOf / maxColumns + (indexOf % maxColumns == 0 ? 0 : 1));
            }
            removeAll();
            addAll(getSelectedInputTreeSchemaModel());
        }

    }

    //
    public void beanListModified() {
        List<TreeNode> beansList = getBeansList();
        List<TreeNode> copyToRemove = new ArrayList<TreeNode>(beansList);
        List<TreeNode> updatedList = getSelectedInputTreeSchemaModel();
        copyToRemove.removeAll(updatedList);
        if (!copyToRemove.isEmpty()) {
            removeAll(copyToRemove);
        }
        // check if any new node is added
        for (int i = 0; i < updatedList.size(); i++) {
            if (!beansList.contains(updatedList.get(i))) {
                add(updatedList.get(i), i);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.commons.ui.swt.extended.table.AbstractExtendedControlModel#getName()
     */
    @Override
    public String getName() {
        return absTree.getName();
    }

    public AbstractInOutTree getXmlTree() {
        return absTree;
    }

    public void setMaxColumns(int maxColumns) {
        this.maxColumns = maxColumns;
    }

    /**
     * Sets the currentPage.
     *
     * @param currentPage the currentPage to set
     */
    public void setCurrentPage(int page) {
        int newPage = page;
        if (newPage < 1) {
            newPage = 1;
        }
        if (newPage > pageCount) {
            newPage = pageCount;
        }
        if (this.currentPage != newPage) {
            this.currentPage = newPage;
            removeAll();
            addAll(getSelectedInputTreeSchemaModel());
        }
    }

    /**
     * Getter for currentPage.
     *
     * @return the currentPage
     */
    public int getCurrentPage() {
        return this.currentPage;
    }

    /**
     * Getter for pageCount.
     *
     * @return the pageCount
     */
    public int getPageCount() {
        return this.pageCount;
    }
}
