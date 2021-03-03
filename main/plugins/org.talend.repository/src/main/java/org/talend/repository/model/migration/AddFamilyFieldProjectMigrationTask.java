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

import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;

import org.eclipse.emf.common.util.EList;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IComponentsFactory;
import org.talend.core.model.general.Project;
import org.talend.core.model.migration.AbstractProjectMigrationTask;
import org.talend.core.model.properties.ComponentSetting;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * yzhang class global comment. Detailled comment
 */
public class AddFamilyFieldProjectMigrationTask extends AbstractProjectMigrationTask {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.IProjectMigrationTask#execute(org.talend.core.model.general.Project)
     */
    @Override
    public ExecutionResult execute(Project project) {
        boolean needReset = false;
        EList list = project.getEmfProject().getComponentsSettings();
        for (Object obj : list) {
            ComponentSetting componentSetting = (ComponentSetting) obj;
            if (componentSetting.getFamily() == null) {
                needReset = true;
                break;
            }
        }
        if (needReset) {
            resetFamily(list);
        }
        return ExecutionResult.SUCCESS_WITH_ALERT;
    }

    /**
     * yzhang Comment method "resetFamily".
     *
     * @param list
     */
    private void resetFamily(EList list) {

        IComponentsFactory componentsFactory = ComponentsFactoryProvider.getInstance();
        Collection<IComponent> components = componentsFactory.readComponents();

        list.clear();

        for (IComponent component : components) {
            String[] families = component.getOriginalFamilyName().split(ComponentsFactoryProvider.FAMILY_SEPARATOR_REGEX);
            for (String family : families) {
                ComponentSetting setting = PropertiesFactory.eINSTANCE.createComponentSetting();
                setting.setFamily(family);
                setting.setName(component.getName());
                setting.setHidden(!component.isVisibleInComponentDefinition());
                list.add(setting);
            }
        }

        RepositoryContext repositoryContext = (RepositoryContext) CorePlugin.getContext().getProperty(
                Context.REPOSITORY_CONTEXT_KEY);
        IProxyRepositoryFactory prf = CorePlugin.getDefault().getProxyRepositoryFactory();
        try {
            prf.saveProject(repositoryContext.getProject());
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.IProjectMigrationTask#getOrder()
     */
    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2008, 7, 7, 13, 0, 0);
        return gc.getTime();
    }

}
