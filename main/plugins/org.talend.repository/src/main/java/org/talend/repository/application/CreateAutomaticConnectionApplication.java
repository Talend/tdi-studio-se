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
package org.talend.repository.application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.service.datalocation.Location;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.core.model.general.ConnectionBean;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.repository.ui.login.connections.ConnectionUserPerReader;

/**
 * DOC hywang class global comment. Detailled comment
 */
public class CreateAutomaticConnectionApplication implements IApplication {

    private static String userKey = "-conuser"; //$NON-NLS-N$ can't use "user" via can't be caught

    private static String nameKey = "-conname"; //$NON-NLS-N$

    private static String descriptionKey = "-condes"; //$NON-NLS-N$

    private static String passwordKey = "-conpwd"; //$NON-NLS-N$

    private static String urlKey = "-conurl"; //$NON-NLS-N$

    private static String repositoryIdKey = "-conrepository"; //$NON-NLS-N$

    private static String workspaceKey = "-conworkspace"; //$NON-NLS-N$

    private String repositoryIdValue;

    public Object start(IApplicationContext context) throws Exception {
        // String[] commandlineArgs = Platform.getCommandLineArgs();
        try {
            String[] args = Platform.getApplicationArgs();
            ConnectionBean newconnection = splitParameters(args);
            List<ConnectionBean> connectionList = ConnectionUserPerReader.getInstance().readConnections();
            connectionList.add(newconnection);
            ConnectionUserPerReader.getInstance().saveConnections(connectionList);
            return IApplication.EXIT_OK;
        } catch (ParameterIncorrectSetException e) {
            MessageDialog.openError(DisplayUtils.getDefaultShell(false), "parameter errors!", e.getMessage()); // $NON-NLS-N$
            throw e;
        } finally {
            // releaseWorkspaceLock();
        }
    }

    public void stop() {
        // final IWorkbench workbench = PlatformUI.getWorkbench();
        // if (workbench == null) {
        // return;
        // }
        // final Display display = workbench.getDisplay();
        // display.syncExec(new Runnable() {
        //
        // public void run() {
        // if (!display.isDisposed()) {
        // workbench.close();
        // }
        // }
        // });
    }

    private ConnectionBean splitParameters(String[] args) throws ParameterIncorrectSetException {
        Map<String, String> argMap = new HashMap<String, String>();
        ConnectionBean bean = new ConnectionBean();
        List<String> exceptionMessages = new ArrayList<String>();
        List<String> argsList = Arrays.asList(args);
        int valueIndex = 0;
        String value = ""; //$NON-NLS-N$
        for (String current : argsList) {
            current = TalendTextUtils.removeQuotes(current);
            if (current.equals(userKey)) {
                valueIndex = argsList.indexOf(userKey) + 1;
                value = argsList.get(valueIndex);
                if (value.startsWith("-")) { // no value of this parameter
                    String message = "Create Automatic Connection error,parameter " + userKey + "'s value is incorrect.";
                    exceptionMessages.add(message);
                }
                argMap.put(userKey, value);
            } else if (current.equals(nameKey)) {
                valueIndex = argsList.indexOf(nameKey) + 1;
                value = argsList.get(valueIndex);
                if (value.startsWith("-")) {
                    String message = "Create Automatic Connection error,parameter " + nameKey + "'s value is incorrect.";
                    exceptionMessages.add(message);
                }
                argMap.put(nameKey, value);
            } else if (current.equals(descriptionKey)) {
                valueIndex = argsList.indexOf(descriptionKey) + 1;
                value = argsList.get(valueIndex);
                argMap.put(descriptionKey, value);
            } else if (current.equals(passwordKey)) {
                valueIndex = argsList.indexOf(passwordKey) + 1;
                value = argsList.get(valueIndex);
                if (value.startsWith("-")) {
                    String message = "Create Automatic Connection error,parameter " + passwordKey + "'s value is incorrect.";
                    exceptionMessages.add(message);
                }
                argMap.put(passwordKey, value);
            } else if (current.equals(repositoryIdKey)) {
                valueIndex = argsList.indexOf(repositoryIdKey) + 1;
                value = argsList.get(valueIndex);
                repositoryIdValue = value;
                if (value.startsWith("-")) {
                    String message = "Create Automatic Connection error,parameter " + repositoryIdKey + "'s value is incorrect.";
                    exceptionMessages.add(message);
                }
                argMap.put(repositoryIdKey, value);
            } else if (current.equals(urlKey)) {
                valueIndex = argsList.indexOf(urlKey) + 1;
                value = argsList.get(valueIndex);
                if (repositoryIdValue != null && repositoryIdValue.equals("Remote") && value.startsWith("-")) {
                    String message = "Create Automatic Connection error,parameter " + urlKey + "'s value is incorrect.";
                    exceptionMessages.add(message);
                }
                argMap.put(urlKey, value);
            } else if (current.equals(workspaceKey)) {
                valueIndex = argsList.indexOf(workspaceKey) + 1;
                value = argsList.get(valueIndex);
                if (value.startsWith("-")) {
                    String message = "Create Automatic Connection error,parameter " + workspaceKey + "'s value is incorrect.";
                    exceptionMessages.add(message);
                }
                argMap.put(workspaceKey, value);
            }
        }
        if (!exceptionMessages.isEmpty()) {
            String exceptionMessage = ""; //$NON-NLS-N$
            StringBuffer sb = new StringBuffer();
            for (String s : exceptionMessages) {
                sb.append(s);
                sb.append("\n"); //$NON-NLS-N$
            }
            exceptionMessage = sb.toString();
            ParameterIncorrectSetException exception = new ParameterIncorrectSetException(exceptionMessage);
            throw exception;
        }

        bean.setName(argMap.get(nameKey));
        bean.setDescription(argMap.get(descriptionKey));
        bean.setPassword(argMap.get(passwordKey));
        bean.setUser(argMap.get(userKey));
        bean.setRepositoryId(argMap.get(repositoryIdKey));
        bean.setWorkSpace(argMap.get(workspaceKey));
        bean.getDynamicFields().put("url", argMap.get(urlKey));
        bean.setComplete(true);
        return bean;
    }

    /**
     * hywang comment
     */
    class ParameterIncorrectSetException extends Exception {

        private static final long serialVersionUID = -3095904601822507608L;

        public ParameterIncorrectSetException() {
            super();
        }

        public ParameterIncorrectSetException(String message, Throwable cause) {
            super(message, cause);

        }

        public ParameterIncorrectSetException(String message) {
            super(message);
        }

        public ParameterIncorrectSetException(Throwable cause) {
            super(cause);
        }
    }

    private void releaseWorkspaceLock() {
        Location instanceLoc = Platform.getInstanceLocation();
        if (instanceLoc != null) {
            instanceLoc.release();
        }
    }
}
