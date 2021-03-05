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
package org.talend.designer.components.exchange;

import org.talend.designer.components.exchange.i18n.Messages;

/**
 *
 * DOC hcyi class global comment. Detailled comment
 */
public class ExchangeConstants {

    /**
     * File that store information about the components we have downloaded.
     */
    public static final String COMPONENT_MODEL_FILE = "component_extensions.xmi"; //$NON-NLS-1$

    public static String getReloadPaletteLable() {
        return Messages.getString("DownloadComponenentsAction.ReloadPalette"); //$NON-NLS-1$
    }

    public static String getDownloadTaskTitleLable() {
        return Messages.getString("DownloadComponenentsAction.DownloadTaskTitle"); //$NON-NLS-1$
    }

    public static String getDownloadTaskNameLable() {
        return Messages.getString("DownloadComponenentsAction.DownloadTaskName"); //$NON-NLS-1$
    }

}
