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
package org.talend.repository.model.migration;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.talend.core.model.general.Project;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.DynaEnum;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.RepositoryObject;
import org.talend.core.repository.model.ProjectRepositoryNode;
import org.talend.core.repository.recyclebin.RecycleBinManager;

/**
 * created by nrousseau on Jul 14, 2015 Detailled comment
 *
 */
public class BuildRecycleBinIndexMigrationTask extends AbstractItemMigrationTask {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.migration.IMigrationTask#getOrder()
     */
    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2015, 07, 20, 12, 0, 0);
        return gc.getTime();
    }

    @Override
    public ExecutionResult execute(Project project) {
        RecycleBinManager.getInstance().clearIndex(project);
        ExecutionResult result = super.execute(project);
        RecycleBinManager.getInstance().saveRecycleBin(project);
        return result;
    }


    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
        for (DynaEnum<? extends DynaEnum<?>> type : ERepositoryObjectType.values()) {
            ERepositoryObjectType objectType = (ERepositoryObjectType) type;
            if (objectType.isResouce()) {
                toReturn.add(objectType);
            }
        }
        return toReturn;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractItemMigrationTask#execute(org.talend.core.model.properties.Item)
     */
    @Override
    public ExecutionResult execute(Item item) {
        if (item.getState().isDeleted()) {
            RecycleBinManager.getInstance().addToRecycleBin(getProject(), item, true);
        } else if (item instanceof ConnectionItem) {
            if (ProjectRepositoryNode.getInstance().hasDeletedSubItem((ConnectionItem) item)) {
                RecycleBinManager.getInstance().addToRecycleBin(getProject(), item, true);
            }
        }
        return ExecutionResult.SUCCESS_NO_ALERT;
    }

    @Override
    public void unloadObject(IRepositoryViewObject object) {
        if (object instanceof RepositoryObject) {
            RepositoryObject obj = (RepositoryObject) object;
            obj.unload();
        }
    }

}
