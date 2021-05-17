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
package org.talend.designer.unifiedcomponent.delegate;

import org.talend.designer.unifiedcomponent.delegate.service.IComponentDelegate;
import org.talend.designer.unifiedcomponent.resources.ComponentImage;

/**
 * created by wchen on Dec 4, 2017 Detailled comment
 *
 */
public class tDBRow implements IComponentDelegate {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.unifiedcomponent.comp.service.IUnifiedComponent#getName()
     */
    @Override
    public String getComponentName() {
        return "tDBRow";
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.unifiedcomponent.comp.service.IUnifiedComponent#getFamily()
     */
    @Override
    public String getFamily() {
        return IComponentDelegate.FAMILY;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.unifiedcomponent.comp.service.IUnifiedComponent#getImage()
     */
    @Override
    public ComponentImage getImage() {
        return ComponentImage.tDBRow;
    }

}
