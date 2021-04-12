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
package org.talend.designer.runprocess;

import javax.management.MBeanServerConnection;

import org.talend.core.IService;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.properties.Property;
import org.talend.designer.runprocess.java.JavaProcessor;

/**
 * DOC yyan class global comment. Detailled comment
 */
public interface IESBRunContainerService extends IService {

    boolean isRuntimeEnable();

    JavaProcessor createJavaProcessor(IProcess process, Property property, boolean filenameFromLabel);

    MBeanServerConnection getJMXServerConnection();
}
