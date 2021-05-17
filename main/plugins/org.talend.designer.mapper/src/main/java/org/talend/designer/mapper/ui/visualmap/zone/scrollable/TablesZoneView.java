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
package org.talend.designer.mapper.ui.visualmap.zone.scrollable;

import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.talend.designer.mapper.managers.MapperManager;
import org.talend.designer.mapper.ui.dnd.InsertionIndicator;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public abstract class TablesZoneView extends Composite {

    protected static final int MARGIN_TOP_ZONE_WITHOUT_ACTION_BAR = 10;

    private InsertionIndicator insertionIndicator;

    private MapperManager mapperManager;

    public TablesZoneView(Composite parent, int style, MapperManager mapperManager) {
        super(parent, style);
        this.mapperManager = mapperManager;
        initLayout();
    }

    /**
     * DOC amaumont Comment method "getFormLayout".
     *
     * @return
     */
    public abstract Layout initLayout();

    public InsertionIndicator getInsertionIndicator() {
        return this.insertionIndicator;
    }

    /**
     * Call this method when a form layout is defined for the <code>TablesZoneView</code>.
     */
    public void initInsertionIndicator() {
        this.insertionIndicator = new InsertionIndicator(this, mapperManager);
    }

    @Override
    public void layout() {
        // System.out.println("TablesZoneView layout " + toString() + (i++) );
        if (super.getLayout() == null) {
            initLayout();
        }
        super.layout();
    }

    @Override
    public FormLayout getLayout() {
        // System.out.println("TablesZoneView getLayout " + toString() + (j++) );
        FormLayout formLayout = (FormLayout) super.getLayout();
        if (formLayout == null) {
            formLayout = (FormLayout) initLayout();
        }
        return formLayout;
    }

    @Override
    public void setLayout(Layout layout) {
        // System.out.println("TablesZoneView getLayout " + toString() + (k++) );
        // TODO Auto-generated method stub
        super.setLayout(layout);
    }

}
