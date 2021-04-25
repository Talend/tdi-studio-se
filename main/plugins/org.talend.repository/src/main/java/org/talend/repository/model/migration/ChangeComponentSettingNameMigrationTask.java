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
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.talend.core.model.components.IComponent;
import org.talend.core.model.general.Project;
import org.talend.core.model.migration.AbstractProjectMigrationTask;
import org.talend.core.model.properties.ComponentSetting;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.ui.component.ComponentsFactoryProvider;

/**
 * created by hwang on Jun 30, 2015 Detailled comment
 *
 */
public class ChangeComponentSettingNameMigrationTask extends AbstractProjectMigrationTask {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.migration.IProjectMigrationTask#execute(org.talend.core.model.general.Project)
     */
    @Override
    public ExecutionResult execute(Project project) {
        List<ComponentSetting> components = project.getEmfProject().getComponentsSettings();
        List<ComponentSetting> newAdded = new ArrayList<ComponentSetting>();
        Collection<IComponent> componentList = ComponentsFactoryProvider.getInstance().readComponents();
        for (ComponentSetting componentSetting : components) {
            for (IComponent comp : componentList) {
                if (comp != null && comp.getName().equals(componentSetting.getName())) {
                    componentSetting.setName(comp.getPaletteType() + "|" + comp.getName());
                    String family = componentSetting.getFamily();
                    if (family.contains("|")) {
                        String[] fams = family.split("\\|");
                        for (int i = 0; i < fams.length; i++) {
                            if (i == 0) {
                                componentSetting.setFamily(fams[0]);
                            } else {
                                ComponentSetting cs = PropertiesFactory.eINSTANCE.createComponentSetting();
                                cs.setName(comp.getPaletteType() + "|" + comp.getName());
                                cs.setHidden(componentSetting.isHidden());
                                cs.setFamily(fams[i]);
                                newAdded.add(cs);
                            }
                        }
                    }
                    break;
                }
            }
        }
        components.addAll(newAdded);
        return ExecutionResult.SUCCESS_NO_ALERT;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.migration.IMigrationTask#getOrder()
     */
    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2015, 06, 30, 18, 27, 0);
        return gc.getTime();
    }

}
