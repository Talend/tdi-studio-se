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
package org.talend.expressionbuilder.test.shadow;

import java.util.ArrayList;
import java.util.List;

import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.runprocess.shadow.TextElementParameter;
import org.talend.designer.runprocess.shadow.ShadowNode;

/**
 * DOC yzhang class global comment. Detailled comment <br/>
 *
 */
public class PerlRowNode extends ShadowNode {

    private List<IMetadataTable> metadatas;

    /**
     * yzhang PerlRowNode constructor comment.
     *
     * @param componentName
     */
    public PerlRowNode(String expression) {
        super("tPerlRow"); //$NON-NLS-1$

        TextElementParameter parameter = new TextElementParameter("CODE", "$output_row[id] =" + expression + ";"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        addParameter(parameter);

        IMetadataTable metadataTable = new MetadataTable();
        List<IMetadataColumn> listColumns = new ArrayList<IMetadataColumn>();
        VirtualMetadataColumn column = new VirtualMetadataColumn();
        listColumns.add(column);
        metadataTable.setListColumns(listColumns);
        metadatas = new ArrayList<IMetadataTable>();
        metadatas.add(metadataTable);

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.shadow.ShadowNode#getMetadataList()
     */
    @Override
    public List<IMetadataTable> getMetadataList() {
        return metadatas;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.shadow.ShadowNode#setMetadataList(java.util.List)
     */
    @Override
    public void setMetadataList(List<IMetadataTable> metadataList) {
        this.metadatas = metadataList;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.shadow.ShadowNode#isStart()
     */
    @Override
    public boolean isStart() {
        return true;

    }

}
