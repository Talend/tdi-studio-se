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
package org.talend.designer.core.ui.editor.properties;

import org.eclipse.gef.EditPart;
import org.eclipse.ui.views.properties.tabbed.ITypeMapper;

/**
 * Type of mapper returned by this function. It will select a basic EditPart. <br/>
 *
 * $Id$
 *
 */
public class GefEditorTypeMapper implements ITypeMapper {

    /**
     * @inheritDoc
     */
    public Class mapType(Object object) {
        Class type = object.getClass();
        if (object instanceof EditPart) {
            type = ((EditPart) object).getModel().getClass();
        }
        return type;
    }
}
