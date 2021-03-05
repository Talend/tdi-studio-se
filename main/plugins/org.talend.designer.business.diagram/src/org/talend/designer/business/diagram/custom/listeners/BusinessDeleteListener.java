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
package org.talend.designer.business.diagram.custom.listeners;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.CorePlugin;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryObject;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.documentation.ERepositoryActionName;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class BusinessDeleteListener implements PropertyChangeListener {

    private IProxyRepositoryFactory factory = null;

    public BusinessDeleteListener() {
        factory = CorePlugin.getDefault().getProxyRepositoryFactory();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent event) {

        boolean isDeleteBusinessLogical = event.getPropertyName().equals(ERepositoryActionName.DELETE_TO_RECYCLE_BIN.getName());
        boolean isDeleteBusinessPhysical = event.getPropertyName().equals(ERepositoryActionName.DELETE_FOREVER.getName());

        if (!isDeleteBusinessLogical && !isDeleteBusinessPhysical) {
            return;
        }

        if (!(event.getNewValue() instanceof IRepositoryObject)) {
            return;
        }
        IRepositoryObject object = (IRepositoryObject) event.getNewValue();
        if (object.getRepositoryObjectType() != ERepositoryObjectType.BUSINESS_PROCESS) {
            return;
        }

        object = (IRepositoryObject) event.getNewValue();

        if (object != null) {

            try {

                deleteSVGObjectLogicalOrPhysical(object, isDeleteBusinessLogical);

            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }
        }
    }

    public void deleteSVGObjectLogicalOrPhysical(IRepositoryObject object, boolean isDeleteBusinessLogical)
            throws PersistenceException {
        IRepositoryViewObject svgObjectToDelete = ProxyRepositoryFactory.getInstance().getLastVersion("svg_" + object.getId());

        if (svgObjectToDelete != null) {
            try {
                if (isDeleteBusinessLogical) {
                    factory.deleteObjectLogical(svgObjectToDelete);
                } else {
                    factory.deleteObjectPhysical(svgObjectToDelete);
                }
            } catch (Exception e) {
                ExceptionHandler.process(e);
                return;
            }
        }
    }

}
