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
package org.talend.repository.ui.login.connections.network;

import org.apache.commons.lang3.StringUtils;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.utils.system.EclipseCommandLine;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.designer.core.IDesignerCoreService;

/**
 * DOC cmeng  class global comment. Detailled comment
 */
public class NetworkConfiguration {

    public static final String ARG_NETWORK_TIMEOUT = "-talend.studio.network.timeout";

    public static final int CONNECTION_TIMEOUT_MIN = 0;

    public static final int CONNECTION_TIMEOUT_MAX = 3600;

    public static final int CONNECTION_TIMEOUT_DEFAULT = 5;

    public static final int READ_TIMEOUT_MIN = 0;

    public static final int READ_TIMEOUT_MAX = 3600;

    public static final int READ_TIMEOUT_DEFAULT = 60;

    private static NetworkConfiguration instance;

    private static IDesignerCoreService designerCoreService;

    public static NetworkConfiguration getInstance() {
        if (instance == null) {
            instance = new NetworkConfiguration();
        }
        return instance;
    }

    private NetworkConfiguration() {
        String timeoutStr = EclipseCommandLine.getEclipseArgument(ARG_NETWORK_TIMEOUT);
        if (StringUtils.isNotBlank(timeoutStr)) {
            try {
                Integer timeout = Integer.valueOf(timeoutStr);
                setConnectionTimeout(timeout);
                setReadTimeout(timeout);
            } catch (Throwable e) {
                ExceptionHandler.process(e);
            }
        }
    }

    private static IDesignerCoreService getDesignerCoreService() {
        if (designerCoreService == null && GlobalServiceRegister.getDefault().isServiceRegistered(IDesignerCoreService.class)) {
            designerCoreService = CoreRuntimePlugin.getInstance().getDesignerCoreService();
        }
        return designerCoreService;
    }

    public int getConnectionTimeout() {
        int connectionTimeout = CONNECTION_TIMEOUT_DEFAULT;
        IDesignerCoreService iDesignerCoreService = getDesignerCoreService();
        if (iDesignerCoreService != null) {
            connectionTimeout = iDesignerCoreService.getTACConnectionTimeout();
        }
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        IDesignerCoreService iDesignerCoreService = getDesignerCoreService();
        if (iDesignerCoreService != null) {
            iDesignerCoreService.setTACConnectionTimeout(connectionTimeout);
        }
    }

    public int getReadTimeout() {
        int readTimeout = READ_TIMEOUT_DEFAULT;
        IDesignerCoreService iDesignerCoreService = getDesignerCoreService();
        if (iDesignerCoreService != null) {
            readTimeout = iDesignerCoreService.getTACReadTimeout();
        }
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        IDesignerCoreService iDesignerCoreService = getDesignerCoreService();
        if (iDesignerCoreService != null) {
            iDesignerCoreService.setTACReadTimeout(readTimeout);
        }
    }

    public void reset() {
        setReadTimeout(READ_TIMEOUT_DEFAULT);
        setConnectionTimeout(CONNECTION_TIMEOUT_DEFAULT);
    }

}
