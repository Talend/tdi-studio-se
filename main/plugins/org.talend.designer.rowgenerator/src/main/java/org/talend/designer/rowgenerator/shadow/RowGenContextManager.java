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
package org.talend.designer.rowgenerator.shadow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextListener;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IContextParameter;

/**
 * qzhang class global comment. Detailled comment <br/>
 *
 * $Id: RowGenContextManager.java,v 1.1 2007/02/02 07:47:01 pub Exp $
 *
 */
public class RowGenContextManager implements IContextManager, Cloneable {

    private IContext defaultContext = new EmptyContext();

    /*
     * (non-Java)
     *
     * @see
     * org.talend.core.model.process.IContextManager#addContextListener(org.talend.core.model.process.IContextListener)
     */
    public void addContextListener(IContextListener listener) {
    }

    /*
     * (non-Java)
     *
     * @see org.talend.core.model.process.IContextManager#fireContextsChangedEvent()
     */
    public void fireContextsChangedEvent() {
    }

    /*
     * (non-Java)
     *
     * @see org.talend.core.model.process.IContextManager#getDefaultContext()
     */
    public IContext getDefaultContext() {
        return this.defaultContext;
    }

    /*
     * (non-Java)
     *
     * @see org.talend.core.model.process.IContextManager#getListContext()
     */
    public List<IContext> getListContext() {
        return Arrays.asList(new IContext[] { getDefaultContext() });
    }

    /*
     * (non-Java)
     *
     * @see
     * org.talend.core.model.process.IContextManager#removeContextListener(org.talend.core.model.process.IContextListener
     * )
     */
    public void removeContextListener(IContextListener listener) {
    }

    /*
     * (non-Java)
     *
     * @see org.talend.core.model.process.IContextManager#setDefaultContext(org.talend.core.model.process.IContext)
     */
    public void setDefaultContext(IContext context) {
        if (context != null) {
            for (IContextParameter param : context.getContextParameterList()) {
                this.defaultContext.getContextParameterList().add(param.clone());
            }
        }
    }

    /*
     * (non-Java)
     *
     * @see org.talend.core.model.process.IContextManager#setListContext(java.util.List)
     */
    public void setListContext(List<IContext> listContext) {
        // Read-only
    }

    public IContext getContext(String name) {
        return null;
    }

    /**
     * qzhang RowGenContextManager class global comment. Detailled comment <br/>
     *
     * $Id: RowGenContextManager.java,v 1.1 2007/02/02 07:47:01 pub Exp $
     *
     */
    private class EmptyContext implements IContext, Cloneable {

        List<IContextParameter> contextParameterList = new ArrayList<IContextParameter>();

        /*
         * (non-Java)
         *
         * @see org.talend.core.model.process.IContext#getContextParameterList()
         */
        public List<IContextParameter> getContextParameterList() {
            return this.contextParameterList;
        }

        /*
         * (non-Java)
         *
         * @see org.talend.core.model.process.IContext#getName()
         */
        public String getName() {
            return RowGenProcessMain.PREVIEW;
        }

        /*
         * (non-Java)
         *
         * @see org.talend.core.model.process.IContext#isConfirmationNeeded()
         */
        public boolean isConfirmationNeeded() {
            return false;
        }

        /*
         * (non-Java)
         *
         * @see org.talend.core.model.process.IContext#setConfirmationNeeded(boolean)
         */
        public void setConfirmationNeeded(boolean confirmationNeeded) {
            // Read-only
        }

        /*
         * (non-Java)
         *
         * @see org.talend.core.model.process.IContext#setContextParameterList(java.util.List)
         */
        public void setContextParameterList(List<IContextParameter> contextParameterList) {
            // Read-only
        }

        /*
         * (non-Java)
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
