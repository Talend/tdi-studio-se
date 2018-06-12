/*******************************************************************************
 * Copyright (c) 2010 JVM Monitor project. All rights reserved. 
 * 
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.talend.designer.runtime.visualization;

/**
 * JVM attach handler. A client plug-in that contributes to the extension point
 * <tt>org.jvmmonitor.core.jvmAttachHandler</tt> will implement this interface.
 */
public interface IJvmAttachHandler {

    /**
     * Sets the local host.
     * 
     * @param localhost
     *            The local host
     */
    void setHost(IHost localhost);

    /**
     * Gets the state indicating if valid JDK is available on local host in a
     * sense that JDK has tools.jar at expected location.
     * 
     * @return <tt>true</tt> if valid JDK is available
     */
    boolean hasValidJdk();

    /**
     * update current active JVMs DOC jding Comment method "updatesActiveJvms".
     * 
     * @throws JvmCoreException
     */
    public void updatesActiveJvms() throws JvmCoreException;
}