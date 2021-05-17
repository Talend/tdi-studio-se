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
import org.talend.designer.gefabstractmap.model.abstractmap.MapperTableEntity;
import org.talend.designer.gefabstractmap.part.TableEntityPart;
import org.talend.designer.gefabstractmap.utils.MapperUtils;

/**
 * DOC wchen class global comment. Detailled comment
 */
public class TableEntityManager extends ModelManager {

    private MapperTable mapperTable;

    public TableEntityManager(MapperTableEntity entityModel, TableEntityPart entityPart) {
        super(entityModel, entityPart);
        this.mapperTable = MapperUtils.getMapperTable(entityModel);
    }

    @Override
    public MapperTableEntity getModel() {
        return (MapperTableEntity) super.getModel();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.newabstractmap.figures.table.ModelManager#getEditPart()
     */
    @Override
    public TableEntityPart getEditPart() {
        return (TableEntityPart) super.getEditPart();
    }

    public MapperTable getMapperTable() {
        return this.mapperTable;
    }

    public boolean isTableMinimized() {
        return false;
    }

    // public abstract Object getValue(int featureID);
    //
    // public abstract void updateEntity(int typ, int featureID, String newValue);

}
