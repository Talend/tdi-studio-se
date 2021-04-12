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

import org.talend.designer.gefabstractmap.model.abstractmap.MapperTable;
import org.talend.designer.gefabstractmap.part.MapperTablePart;

/**
 * created by wchen on 2013-1-14 Detailled comment
 *
 */
public abstract class TableManager extends ModelManager {

    /**
     * DOC wchen TableManager constructor comment.
     */
    public TableManager(MapperTable mapperTable, MapperTablePart tablePart) {
        super(mapperTable, tablePart);
    }

    @Override
    public MapperTable getModel() {
        return (MapperTable) super.getModel();
    }

    @Override
    public MapperTablePart getEditPart() {
        return (MapperTablePart) super.getEditPart();
    }

    public abstract boolean isActivateCondensedTool();

    public abstract boolean isActivateExpressionFilter();

    public abstract String getExpressionFilter();

    public abstract void setExpressionFilter(String filter);

    public abstract boolean isMinimized();

    public abstract void setActivateCondensedTool(boolean active);

    public abstract void setActivateExpressionFilter(boolean active);

    public abstract void setMinimized(boolean minisized);

    public abstract boolean isActivateGlobalMap();

    public abstract void setActivateGlobalMap(boolean active);
}
