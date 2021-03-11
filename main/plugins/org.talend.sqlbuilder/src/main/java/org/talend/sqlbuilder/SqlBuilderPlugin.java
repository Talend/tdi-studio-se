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
package org.talend.sqlbuilder;

import java.sql.SQLException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Priority;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.talend.commons.exception.CommonExceptionHandler;
import org.talend.core.GlobalServiceRegister;
import org.talend.repository.model.IRepositoryService;

/**
 * The activator class controls the plug-in life cycle.
 */
public class SqlBuilderPlugin extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.talend.sqlbuilder"; //$NON-NLS-1$

    // The shared instance
    private static SqlBuilderPlugin plugin;

    /**
     * The constructor.
     */
    public SqlBuilderPlugin() {
        plugin = this;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance.
     *
     * @return the shared instance
     */
    public static SqlBuilderPlugin getDefault() {
        return plugin;
    }

    public static void log(String msg, Throwable e) {
        if (e instanceof SQLException) {
            if (isMethodNotSupportException(e)) {
                return;
            }
            CommonExceptionHandler.process(e, Priority.WARN);
        } else {
            CommonExceptionHandler.process(e);
        }
    }

    private static boolean isMethodNotSupportException(Throwable e) {
        boolean result = false;

        if (e instanceof SQLException) {
            String errorMsg = e.getMessage();
            String noSupportStrings[] = { ".*\\bmethod\\b[\\w\\s]*\\bnot\\b[\\w\\s]*\\bsupport.*", //$NON-NLS-1$
                    ".*\\bnot\\b[\\w\\s]*\\bsupport(ed)*\\b[\\w\\s]*\\bmethod.*", ".*\\bmethod\\b[\\w\\s]*\\bunsupport.*", //$NON-NLS-1$ //$NON-NLS-2$
                    ".*\\bunsupport(ed)*\\b[\\w\\s]*\\bmethod.*" }; //$NON-NLS-1$
            if (!StringUtils.isEmpty(errorMsg)) {
                String lowerCaseMsg = errorMsg.toLowerCase();
                for (String noSupportString : noSupportStrings) {
                    if (lowerCaseMsg.matches(noSupportString)) {
                        return true;
                    }
                }
            }
        }

        return result;
    }

    public IRepositoryService getRepositoryService() {
        return (IRepositoryService) GlobalServiceRegister.getDefault().getService(IRepositoryService.class);
    }
}
