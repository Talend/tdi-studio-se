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
 * $Id: OutputTablesZoneView.java 968 2006-12-12 10:59:26Z amaumont $
 *
 */
public class OutputTablesZoneView extends TablesZoneView {

    public OutputTablesZoneView(Composite parent, int style, MapperManager mapperManager) {
        super(parent, style, mapperManager);
    }

    @Override
    public Layout initLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.marginLeft = 40;
        formLayout.marginRight = 10;
        formLayout.marginTop = MARGIN_TOP_ZONE_WITHOUT_ACTION_BAR;
        formLayout.marginBottom = 10;
        formLayout.marginWidth = 0;
        formLayout.marginHeight = 0;
        formLayout.spacing = 10;
        setLayout(formLayout);
        return formLayout;
    }

}
