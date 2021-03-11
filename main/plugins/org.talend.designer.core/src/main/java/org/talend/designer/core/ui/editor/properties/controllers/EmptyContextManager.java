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

import java.util.Arrays;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextListener;
import org.talend.core.model.process.IContextManager;

/**
 * DOC nrousseau FileinDelimitedToXmlProcess class global comment. Detailled comment <br/>
 *
 * $Id: FileinToXmlProcess.java 3737 2007-06-09 08:19:35 +0000 (星期六, 09 六月 2007) nrousseau $
 *
 */
public class EmptyContextManager implements IContextManager, Cloneable {

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.core.model.process.IContextManager#addContextListener(org.talend.core.model.process.IContextListener)
     */
    public void addContextListener(IContextListener listener) {
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IContextManager#fireContextsChangedEvent()
     */
    public void fireContextsChangedEvent() {
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IContextManager#getDefaultContext()
     */
    public IContext getDefaultContext() {
        return new EmptyContext();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IContextManager#getListContext()
     */
    public List<IContext> getListContext() {
        return Arrays.asList(new IContext[] { getDefaultContext() });
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.core.model.process.IContextManager#removeContextListener(org.talend.core.model.process.IContextListener
     * )
     */
    public void removeContextListener(IContextListener listener) {
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IContextManager#setDefaultContext(org.talend.core.model.process.IContext)
     */
    public void setDefaultContext(IContext context) {
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IContextManager#setListContext(java.util.List)
     */
    public void setListContext(List<IContext> listContext) {
    }

    public IContext getContext(String name) {
        return null;
    }

    public void saveToEmf(EList contextTypeList) {
    }

    public void loadFromEmf(EList contextTypeList, String defaultContextName) {
    }

    public boolean sameAs(IContextManager contextManager) {
        return false;
    }

    public boolean checkValidParameterName(String oldParameterName, String newParameterName) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void saveToEmf(EList contextTypeList, boolean useInternalId) {
        // TODO Auto-generated method stub

    }
}
