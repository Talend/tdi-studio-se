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
package org.talend.repository.json.node;

import org.talend.commons.ui.runtime.image.IImage;
import org.talend.commons.ui.runtime.repository.IExtendRepositoryNode;
import org.talend.repository.json.util.JSONImage;
import org.talend.repository.model.RepositoryNode;

/**
 * DOC wanghong class global comment. Detailled comment
 */
public class JSONExtendNode implements IExtendRepositoryNode {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.repository.IExtendRepositoryNode#getNodeImage()
     */
    @Override
    public IImage getNodeImage() {
        return JSONImage.JSON_ICON;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.repository.IExtendRepositoryNode#getOrdinal()
     */
    @Override
    public int getOrdinal() {
        return 49;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.repository.IExtendRepositoryNode#getChildren()
     */
    @Override
    public Object[] getChildren() {
        return new RepositoryNode[0];
    }

}
