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
package org.talend.repository.generic.ui;

import org.eclipse.core.runtime.IPath;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.metadata.managment.ui.wizard.PropertiesWizard;
import org.talend.repository.generic.util.GenericConnectionUtil;

/**
 * created by ycbai on 2015年11月3日 Detailled comment
 *
 */
public class GenericConnPropertiesWizard extends PropertiesWizard {

    public GenericConnPropertiesWizard(IRepositoryViewObject repositoryViewObject, IPath path, boolean useLastVersion) {
        super(repositoryViewObject, path, useLastVersion);
    }

    @Override
    public boolean performFinish() {
        boolean done = super.performFinish();
        if (done) {
            Property property = object.getProperty();
            ConnectionItem gcItem = (ConnectionItem) property.getItem();
            boolean itemChanged = GenericConnectionUtil.synNamePropertyWithItem(gcItem);
            if (itemChanged) {
                try {
                    ProxyRepositoryFactory.getInstance().save(gcItem);
                } catch (PersistenceException e) {
                    done = false;
                    ExceptionHandler.process(e);
                }
            }
        }
        return done;
    }

}
