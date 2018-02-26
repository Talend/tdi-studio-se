// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.ui.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.talend.repository.ui.actions.importproject.ImportProjectBean;

/**
 * 
 * class global comment. Detailled comment
 */
public class AfterImportProjectUtil {

    /**
     * 
     */
    private static final String CLASS = "class"; //$NON-NLS-1$

    /**
     * The extension id
     */
    private static final String EXTENSION_POINT = "org.talend.repository.after_import_project_action"; //$NON-NLS-1$

    private static Log log = LogFactory.getLog(AfterImportProjectUtil.class);

    public static List<IAfterImportProjectAction> getAfterImportProjectActions() {
        try {
            IExtensionRegistry registry = Platform.getExtensionRegistry();
            IConfigurationElement[] configurationElements = registry.getConfigurationElementsFor(EXTENSION_POINT);
            List<IAfterImportProjectAction> models = new ArrayList<IAfterImportProjectAction>();
            for (int i = 0; i < configurationElements.length; i++) {
                IConfigurationElement element = configurationElements[i];

                IAfterImportProjectAction modelcalss = (IAfterImportProjectAction) element.createExecutableExtension(CLASS);
                models.add(modelcalss);
            }

            return models;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return new ArrayList<IAfterImportProjectAction>();
    }

    public static void runAfterImportProjectActions(ImportProjectBean projectBean) throws Exception{
        for (IAfterImportProjectAction action : getAfterImportProjectActions()) {
            if (action instanceof AbsAfterImportProjectAction) {
                ((AbsAfterImportProjectAction) action).projectBean = projectBean;
            }
            try {
                action.run(projectBean.newProject);
            } finally {
                if (action instanceof AbsAfterImportProjectAction) {
                    ((AbsAfterImportProjectAction) action).projectBean = null;
                }
            }
        }
    }
}
