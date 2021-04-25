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
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.CorePlugin;
import org.talend.core.model.general.Project;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.ComponentSetting;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * wchen class global comment. Detailled comment
 */
public class VisibleComponentSettingsMigrationTask extends AbstractJobMigrationTask {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractItemMigrationTask#execute(org.talend.core.model.properties.Item)
     */
    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        IDesignerCoreService designerCoreService = CorePlugin.getDefault().getDesignerCoreService();
        Project currentProject = ProjectManager.getInstance().getCurrentProject();
        List<ComponentSetting> componentsSettings = (List<ComponentSetting>) currentProject.getEmfProject()
                .getComponentsSettings();

        boolean modified = false;
        if (processType != null) {
            EList nodes = processType.getNode();
            for (Object node : nodes) {
                NodeType nodeType = (NodeType) node;
                for (ComponentSetting setting : componentsSettings) {
                    if (setting.getName().equals(nodeType.getComponentName())) {
                        if (setting.isHidden()) {
                            setting.setHidden(false);
                            modified = true;
                        }
                    }
                }
            }

        }

        if (modified) {
            IProxyRepositoryFactory prf = CorePlugin.getDefault().getProxyRepositoryFactory();
            try {
                prf.saveProject(currentProject);
            } catch (Exception ex) {
                ExceptionHandler.process(ex);
                return ExecutionResult.FAILURE;
            }
            return ExecutionResult.SUCCESS_NO_ALERT;
        }

        return ExecutionResult.NOTHING_TO_DO;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.IProjectMigrationTask#getOrder()
     */
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2009, 11, 10, 12, 0, 0);
        return gc.getTime();
    }

}
