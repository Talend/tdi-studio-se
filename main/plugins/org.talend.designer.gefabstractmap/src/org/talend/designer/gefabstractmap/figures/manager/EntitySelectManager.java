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
package org.talend.designer.gefabstractmap.figures.manager;

import org.talend.designer.gefabstractmap.figures.table.entity.TableEntityElement;

/**
 * wchen class global comment. Detailled comment
 */
public class EntitySelectManager {

    private static EntitySelectManager manager;

    private EntitySelectManager() {
        //
    }

    public static EntitySelectManager getManager() {
        if (manager == null) {
            manager = new EntitySelectManager();
        }
        return manager;
    }

    private TableEntityElement selection;

    public TableEntityElement getSelection() {
        return this.selection;
    }

    public void setSelection(TableEntityElement selection) {
        this.selection = selection;
    }

}
