// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.generic.ui;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.daikon.properties.presentation.Form;

/**
 * DOC hwang  class global comment. Detailled comment
 */
public class DBDynamicComposite extends DynamicComposite{

    public DBDynamicComposite(Composite parentComposite, int styles, EComponentCategory section, Element element, 
            ConnectionItem connectionItem, boolean isCompactView, Color backgroundColor, Form form, boolean drivedByForm) {
        super(parentComposite, styles, section, element, connectionItem, isCompactView, backgroundColor, form, drivedByForm);
    }

    @Override
    protected boolean isShouldCreateControl(IElementParameter curParam) {
        if(curParam.getName().equals("name")){
            return false;
        }
        return super.isShouldCreateControl(curParam);
    }
}
