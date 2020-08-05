// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.talend.commons.CommonsPlugin;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.utils.workbench.resources.ResourceUtils;
import org.talend.repository.i18n.Messages;
import org.talend.repository.ui.actions.importproject.ImportProjectBean;
import org.talend.repository.ui.exception.ImportInvalidObjectException;
import org.talend.utils.io.FilesUtils;

/**
 *
 * class global comment. Detailled comment
 */
public class AfterImportProjectUtil {

    private static Logger LOGGER = Logger.getLogger(AfterImportProjectUtil.class);

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
                if (action instanceof AbsAfterImportProjectAction) {
                    ((AbsAfterImportProjectAction) action).setImportProjectAs(true);
                }
                action.run(projectBean.newProject);
            } catch (ImportInvalidObjectException ex) {
                projectBean.getInvalidViewObjectList().addAll(ex.getInvalidViewObjectList());
                throw ex;
            } finally {
                if (action instanceof AbsAfterImportProjectAction) {
                    ((AbsAfterImportProjectAction) action).projectBean = null;
                }
            }
        }
    }

    public static void deleteTempFolderAfterImport(List<File> tempFolders) {
        for (File folder : tempFolders) {
            FilesUtils.deleteFolder(folder, true);
        }
    }

    public static void deleteImprotedInvalidProject(String projectLable) {
        List<String> projectLabelList = new ArrayList<String>();
        projectLabelList.add(projectLable);
        deleteImprotedInvalidProject(projectLabelList);
    }

    public static void deleteImprotedInvalidProject(List<String> projectLableList) {
        StringBuilder sb = new StringBuilder();
        for (String projectLabel : projectLableList) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(projectLabel);
            try {
                IProject project = ResourceUtils.getProject(projectLabel);
                if (project.exists()) {
                    project.delete(true, new NullProgressMonitor());
                }
            } catch (PersistenceException | CoreException e) {
                ExceptionHandler.process(e);
            }
        }
        if (CommonsPlugin.isHeadless()) {
            LOGGER.warn(Messages.getString("ImportProjectAction.messageDialogContent.projectImportedFailed", sb.toString()));
        } else {
            MessageDialog.openWarning(Display.getDefault().getActiveShell(), Messages.getString("ImportProjectAction.messageDialogTitle.projectFailed"), //$NON-NLS-1$
                    Messages.getString("ImportProjectAction.messageDialogContent.projectImportedFailed", sb.toString()));//$NON-NLS-1$
        }
    }
}
