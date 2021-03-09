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
package org.talend.designer.dbmap.ui.visualmap.zone.scrollable;

import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.talend.designer.dbmap.managers.MapperManager;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: InputTablesZoneView.java 1443 2007-01-12 12:24:00Z amaumont $
 *
 */
public class InputTablesZoneView extends TablesZoneView {

    private FormLayout formLayout;

    public InputTablesZoneView(Composite parent, int style, MapperManager mapperManager) {
        super(parent, style, mapperManager);
    }

    @Override
    public Layout initLayout() {
        formLayout = new FormLayout();
        formLayout.marginLeft = 30;
        formLayout.marginRight = 40;
        formLayout.marginTop = MARGIN_TOP_ZONE_WITHOUT_ACTION_BAR;
        formLayout.marginBottom = 10;
        formLayout.marginWidth = 0;
        formLayout.marginHeight = 0;
        formLayout.spacing = 10;
        setLayout(formLayout);
        return formLayout;
    }

    public void setMarginLeft(int marginLeft) {
        formLayout.marginLeft = marginLeft;
        this.layout();
    }

    public int getMarginLeft() {
        return formLayout.marginLeft;
    }

}
