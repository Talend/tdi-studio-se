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
package org.talend.designer.runprocess.shadow;

import java.util.List;

import org.talend.core.model.metadata.IMetadataTable;

/**
 * yzhang class global comment. Detailled comment <br/>
 *
 * $Id: FileOutputCSVForLDIF.java 下午03:30:59 2007-8-15 +0000 (2007-8-15) yzhang $
 *
 */
public class FileOutputDelimitedForLDIF extends FileOutputDelimitedNode {

    List<IMetadataTable> metadataList;

    /**
     * yzhang FileOutputCSVForLDIF constructor comment.
     *
     * @param filename
     * @param encoding
     */
    public FileOutputDelimitedForLDIF(String filename, String encoding) {
        super(filename, encoding);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.shadow.ShadowNode#getMetadataList()
     */
    @Override
    public List<IMetadataTable> getMetadataList() {
        return metadataList;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.shadow.ShadowNode#setMetadataList(java.util.List)
     */
    @Override
    public void setMetadataList(List<IMetadataTable> metadataList) {
        this.metadataList = metadataList;
    }

}
