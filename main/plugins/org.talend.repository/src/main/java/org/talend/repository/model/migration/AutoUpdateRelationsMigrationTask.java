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

import java.util.Date;
import java.util.GregorianCalendar;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.general.Project;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.RepositoryObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.utils.XmiResourceManager;

/**
 * This migration task must be run each time, not only one time. Not standard migration task. This is to try to keep the
 * relation cache updated each time, no matter of bug / manual update from user or such.
 */
public class AutoUpdateRelationsMigrationTask extends AbstractJobMigrationTask {

    private XmiResourceManager rM;

    public AutoUpdateRelationsMigrationTask() {
        rM = ProxyRepositoryFactory.getInstance().getRepositoryFactoryFromProvider().getResourceManager();
    }

    @Override
    public ExecutionResult execute(Project project) {
        ExecutionResult result = super.execute(project);
        RelationshipItemBuilder.getInstance().saveRelations();
        return result;
    }

    @Override
    public ExecutionResult execute(Item item) {
        try {
            RelationshipItemBuilder.getInstance().addOrUpdateItem(item, true);
            // should make both the item resource and property resource to be unreachable objects so that JVM can
            // release the memory when gc
            if (item instanceof ProcessItem) {
                Resource processResource = ((ProcessItem) item).getProcess().eResource();
                Resource propertyResource = item.eResource();
                if (processResource != null && processResource.isLoaded() && (processResource instanceof XMIResource)) {
                    if (propertyResource != null) {
                        propertyResource.unload();
                    }
                    processResource.unload();
                    rM.resourceSet.getResources().remove(propertyResource);
                    rM.resourceSet.getResources().remove(processResource);
                }
            }
        } catch (Exception e) {
            ExceptionHandler.process(new Exception("Error in item:" + item.getProperty().getLabel(), e));
        }
        return ExecutionResult.SUCCESS_NO_ALERT;
    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2050, 02, 12, 12, 0, 0);
        return gc.getTime();
    }

    @Override
    public void unloadObject(IRepositoryViewObject object) {
        if (object instanceof RepositoryObject) {
            RepositoryObject obj = (RepositoryObject) object;
            obj.unload();
        }
    }
}
