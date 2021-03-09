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
package org.talend.designer.runprocess.jmx;

/**
 * JMX performance listener for ESB route running
 *
 */
public interface JMXPerformanceChangeListener {

    public abstract String getProcessName();

    public abstract void performancesChanged(String connId, int exchangesCompleted);
}
