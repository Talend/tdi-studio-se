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
package org.talend.repository.view.di.model;

import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.IImage;
import org.talend.core.repository.model.AbstractExtendRepositoryNode;

/**
 * DOC ggu class global comment. Detailled comment
 */
@Deprecated
public class RoutinesExtendNode extends AbstractExtendRepositoryNode {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.repository.IExtendRepositoryNode#getNodeImage()
     */
    @Override
    public IImage getNodeImage() {
        return ECoreImage.ROUTINE_ICON;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.repository.IExtendRepositoryNode#getOrdinal()
     */
    @Override
    public int getOrdinal() {
        return 1;
    }

}
