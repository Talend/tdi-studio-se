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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.Platform;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.utils.workbench.resources.ResourceUtils;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.repository.utils.Log4jUtil;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.repository.ProjectManager;
import org.talend.repository.constants.Log4jPrefsConstants;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.RepositoryConstants;

public class Log4jPrefsSettingManager {

    private static Log4jPrefsSettingManager log4jPrefs = null;

    private Log4jPrefsSettingManager() {

    }

    public static synchronized Log4jPrefsSettingManager getInstance() {
        if (log4jPrefs == null) {
            log4jPrefs = new Log4jPrefsSettingManager();
        }
        return log4jPrefs;
    }

    public boolean isLog4jPrefsExist() {

        try {
            IProject project = ResourceUtils.getProject(ProjectManager.getInstance().getCurrentProject());
            IFolder prefSettingFolder = ResourceUtils.getFolder(project, RepositoryConstants.SETTING_DIRECTORY, false);
            IFile presLog4jFile = prefSettingFolder.getFile(Log4jPrefsConstants.LOG4J_RESOURCES
                    + Log4jPrefsConstants.LOG4j_PREFS_SUFFIX);
            if (presLog4jFile.exists()) {
                return true;
            }
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Preferences getLog4jPreferences(String log4jPrefsNode, boolean create) {

        try {
            IProject project = ResourceUtils.getProject(ProjectManager.getInstance().getCurrentProject());
            if (create) {
                return new ProjectScope(project).getNode(Log4jPrefsConstants.LOG4J_RESOURCES).node(log4jPrefsNode);
            }
            Preferences node = Platform.getPreferencesService().getRootNode().node(ProjectScope.SCOPE);
            if (!node.nodeExists(project.getName())) {
                return null;
            }
            node = node.node(project.getName());
            if (!node.nodeExists(Log4jPrefsConstants.LOG4J_RESOURCES)) {
                return null;
            }
            node = node.node(Log4jPrefsConstants.LOG4J_RESOURCES);
            if (!node.nodeExists(log4jPrefsNode)) {
                return null;
            }
            return node.node(log4jPrefsNode);
        } catch (BackingStoreException e) {
            ExceptionHandler.process(e);
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
        return null;
    }

    public String getValueOfPreNode(String nodeName) {
        Preferences nodePre = null;
        nodePre = getLog4jPreferences(nodeName, false);
        if (nodePre == null) {
            return Boolean.FALSE.toString();
        }
        return nodePre.get(nodeName, null);
    }

    public void saveLog4jNodeIntoPref(final String prefNode, final String value) {
        try {
            Preferences log4jSettings = null;
            if (!isLog4jPrefsExist()) { // if not exist,create it
                log4jSettings = getLog4jPreferences(prefNode, true);
            } else {
                log4jSettings = getLog4jPreferences(prefNode, false);
            }
            if (log4jSettings != null) {
                log4jSettings.put(prefNode, value);
                // save log4j activate to prefs
                log4jSettings.flush();
            }
        } catch (BackingStoreException e) {
            ExceptionHandler.process(e);
        }

    }

    public Preferences createTalendLog4jPrefs(String prefNode, String value) {
        Preferences log4jSettings = null;
        try {
            log4jSettings = getLog4jPreferences(prefNode, true);
            if (log4jSettings != null) {
                log4jSettings.put(prefNode, value);
            }
            // create log4j prefs file
            log4jSettings.flush();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
        return log4jSettings;
    }

    public String getDefaultTemplateString(String templateName) {
        IRunProcessService service = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IRunProcessService.class)) {
            service = (IRunProcessService) GlobalServiceRegister.getDefault().getService(IRunProcessService.class);
        }
        if (service != null) {
            return service.getLogTemplate(templateName);
        }
        return "";
    }

    public static String[] getLevel() {
        String[] levels = { Log4jPrefsConstants.TRACE, Log4jPrefsConstants.DEBUG, Log4jPrefsConstants.INFO,
                Log4jPrefsConstants.WARNING, Log4jPrefsConstants.ERROR, Log4jPrefsConstants.FATAL, Log4jPrefsConstants.OFF };
        return levels;
    }

    public boolean addLog4jToJarList(Collection<String> jarList) {
        boolean isSelectLog4j2 = Log4jPrefsSettingManager.getInstance().isSelectLog4j2();
        List<String> moduleNeededList = new ArrayList<String>();
        List<String> moduleDeleteList = new ArrayList<String>();
        if (isSelectLog4j2) {
            boolean foundLog4j2CoreJar = false;
            boolean foundLog4j2ApiJar = false;
            for (String jar : jarList) {
                if (jar.matches("log4j-\\d+\\.\\d+\\.\\d+\\.jar")) { //$NON-NLS-1$
                    moduleDeleteList.add(jar);
                }
                if (jar.matches("log4j-core-\\d+\\.\\d+\\.\\d+\\.jar")) { //$NON-NLS-1$
                    foundLog4j2CoreJar = true;
                }
                if (jar.matches("log4j-api-\\d+\\.\\d+\\.\\d+\\.jar")) { //$NON-NLS-1$
                    foundLog4j2ApiJar = true;
                }
            }
            if (!foundLog4j2CoreJar) {
                moduleNeededList.add("log4j-core-2.12.1.jar");//$NON-NLS-1$

            }
            if (!foundLog4j2ApiJar) {
                moduleNeededList.add("log4j-api-2.12.1.jar");//$NON-NLS-1$
            }

        } else {
            boolean foundLog4jJar = false;
            for (String jar : jarList) {
                if (jar.matches("log4j-\\d+\\.\\d+\\.\\d+\\.jar")) { //$NON-NLS-1$
                    foundLog4jJar = true;
                }
                if (jar.matches("log4j-core-\\d+\\.\\d+\\.\\d+\\.jar")) { //$NON-NLS-1$
                    moduleDeleteList.add(jar);
                }
                if (jar.matches("log4j-api-\\d+\\.\\d+\\.\\d+\\.jar")) { //$NON-NLS-1$
                    moduleDeleteList.add(jar);
                }
            }
            if (!foundLog4jJar) {
                moduleNeededList.add("log4j-1.2.17.jar");//$NON-NLS-1$
            }

        }
        jarList.removeAll(moduleDeleteList);
        jarList.addAll(moduleNeededList);

        return moduleNeededList.size() > 0;
    }

    public void checkLog4jState() {
        if (isPreEnableAndStudioNot()) {
            throw new IllegalStateException(Messages.getString("Log4jSettingPage.IlleagalExp")); //$NON-NLS-1$
        }
    }

    public boolean isPreEnableAndStudioNot() {
        if (!Log4jUtil.isEnable() && Boolean.parseBoolean(getValueOfPreNode(Log4jPrefsConstants.LOG4J_ENABLE_NODE))) {
            return true;
        }
        return false;
    }

    public boolean isLog4jEnable() {
        if (!org.talend.core.PluginChecker.isCoreTISPluginLoaded()) {
            return false;
        }
        if (Log4jUtil.isEnable() && Boolean.parseBoolean(getValueOfPreNode(Log4jPrefsConstants.LOG4J_ENABLE_NODE))) {
            return true;
        }
        return false;
    }

    public boolean isSelectLog4j2() {
        if (!org.talend.core.PluginChecker.isCoreTISPluginLoaded()) {
            return false;
        }
        if (Log4jUtil.isEnable() && Boolean.parseBoolean(getValueOfPreNode(Log4jPrefsConstants.LOG4J_SELECT_VERSION2))) {
            return true;
        }
        return false;
    }
}
