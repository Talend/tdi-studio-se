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
package org.talend.designer.mapper.model.table;

import org.talend.designer.mapper.ui.image.ImageInfo;

/**
 *
 * DOC amaumont  class global comment. Detailled comment
 */
public class MenuSeparator implements IUIMenuOption{

    /* (non-Javadoc)
     * @see org.talend.designer.mapper.model.table.IUITest#getLabel()
     */
    public String getLabel() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.talend.designer.mapper.model.table.IUITest#getMenuType()
     */
    public MENU_TYPE getMenuType() {
        return MENU_TYPE.SEPARATOR;
    }

    /* (non-Javadoc)
     * @see org.talend.designer.mapper.model.table.IUITest#getTooltipText()
     */
    public String getTooltipText() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.talend.designer.mapper.model.table.IUITest#getImageInfo()
     */
    public ImageInfo getImageInfo() {
        // TODO Auto-generated method stub
        return null;
    }

}
