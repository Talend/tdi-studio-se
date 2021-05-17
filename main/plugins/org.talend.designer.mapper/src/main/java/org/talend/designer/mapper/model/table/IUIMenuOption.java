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
public interface IUIMenuOption {

    public String getLabel();

    public MENU_TYPE getMenuType();

    public String getTooltipText();

    public ImageInfo getImageInfo();

}
