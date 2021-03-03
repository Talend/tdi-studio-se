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

import org.eclipse.emf.ecore.EObject;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ItemState;
import org.talend.core.model.properties.Property;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.migration.AbstractMigrationTask;
import org.talend.migration.IProjectMigrationTask;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class CleanFoldersMigrationTask extends AbstractMigrationTask implements IProjectMigrationTask {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.IProjectMigrationTask#execute(org.talend.core.model.general.Project)
     */
    public ExecutionResult execute(Project project) {
        // if (ProxyRepositoryFactory.getInstance().getRepositoryFactoryFromProvider() instanceof
        // ILocalRepositoryFactory) {
        project.getEmfProject().getFolders().clear();
        List<EObject> toRemove = new ArrayList<EObject>();
        for (EObject object : project.getEmfProject().eResource().getContents()) {
            if (object instanceof ItemState || object instanceof Property) {
                toRemove.add(object);
            }
        }
        project.getEmfProject().eResource().getContents().removeAll(toRemove);
        try {
            ProxyRepositoryFactory.getInstance().saveProject(project);
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
        return ExecutionResult.SUCCESS_NO_ALERT;
        // }
        // return ExecutionResult.NOTHING_TO_DO;
    }

    public ExecutionResult execute(Project project, boolean doSave) {
        return execute(project);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.IProjectMigrationTask#execute(org.talend.core.model.general.Project,
     * org.talend.core.model.properties.Item)
     */
    public ExecutionResult execute(Project project, Item item) {
        return ExecutionResult.NOTHING_TO_DO;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.IProjectMigrationTask#getOrder()
     */
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2010, 3, 23, 12, 0, 0);
        return gc.getTime();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.IProjectMigrationTask#isApplicableOnItems()
     */
    public boolean isApplicableOnItems() {
        return false;
    }

    public boolean isDeprecated() {
        return false;
    }

}
