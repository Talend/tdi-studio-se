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
package org.talend.designer.unifiedcomponent.delegate.service;

import org.talend.commons.ui.runtime.image.IImage;

/**
 * created by wchen on Dec 1, 2017 Detailled comment
 *
 */
public interface IComponentDelegate {

    public static String FAMILY = "Databases/DB Common";

    public String getComponentName();

    public String getFamily();

    public IImage getImage();

}
