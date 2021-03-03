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
package org.talend.designer.core.ui.editor.properties.controllers;

import java.util.ArrayList;
import java.util.List;

import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextParameter;

/**
 * Empty IContext implementation. <br/>
 *
 * $Id: FileinToXmlProcess.java 3737 2007-06-09 08:19:35 +0000 (星期六, 09 六月 2007) nrousseau $
 *
 */
public class EmptyContext implements IContext, Cloneable {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IContext#getContextParameterList()
     */
    public List<IContextParameter> getContextParameterList() {
        return new ArrayList<IContextParameter>();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IContext#getName()
     */
    public String getName() {
        return "Shadow"; //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IContext#isConfirmationNeeded()
     */
    public boolean isConfirmationNeeded() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IContext#setConfirmationNeeded(boolean)
     */
    public void setConfirmationNeeded(boolean confirmationNeeded) {
        // Read-only
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IContext#setContextParameterList(java.util.List)
     */
    public void setContextParameterList(List<IContextParameter> contextParameterList) {
        // Read-only
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IContext#setName(java.lang.String)
     */
    public void setName(String name) {
        // Read-only
    }

    @Override
    public IContext clone() {
        return this;
    }

    public boolean sameAs(IContext context) {
        // TODO Auto-generated method stub
        return false;
    }

    public IContextParameter getContextParameter(String parameterName) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IContext#getContextParameter(java.lang.String, java.lang.String)
     */
    @Override
    public IContextParameter getContextParameter(String sourceId, String paraName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean containsSameParameterIgnoreCase(String parameterName) {
        // TODO Auto-generated method stub
        return false;
    }
}
