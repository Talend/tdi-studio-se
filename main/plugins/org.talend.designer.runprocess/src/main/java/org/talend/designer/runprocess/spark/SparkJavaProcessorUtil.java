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
package org.talend.designer.runprocess.spark;

import org.talend.designer.runprocess.ui.ProcessManager;

/**
 * created by rdubois on 8 juin 2015 Detailled comment
 *
 */

public class SparkJavaProcessorUtil {

    public static boolean isStatistics() {
        ProcessManager pm = ProcessManager.getInstance();
        boolean stats = false;
        if (pm != null && pm.getStat() != null) {
            stats = pm.getStat();
        }

        return stats;
    }
}
