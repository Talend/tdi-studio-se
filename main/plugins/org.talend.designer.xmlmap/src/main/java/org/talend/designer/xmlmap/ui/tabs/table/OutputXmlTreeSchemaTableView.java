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

import org.eclipse.swt.widgets.Composite;
import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;

/**
 * created by talend2 on 2012-8-8 Detailled comment
 *
 */
public class OutputXmlTreeSchemaTableView extends XmlTreeSchemaTableView {

    /**
     * DOC talend2 OutputXmlTreeSchemaTableView constructor comment.
     *
     * @param extendedTableModel
     * @param parent
     */
    public OutputXmlTreeSchemaTableView(Composite parentComposite, int mainCompositeStyle) {
        super(parentComposite, mainCompositeStyle);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected String validateEntry(String newValue, TreeNode bean, int beanPosition) {
        return null;
    }

}
