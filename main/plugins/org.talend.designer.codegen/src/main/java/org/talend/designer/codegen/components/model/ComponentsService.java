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
package org.talend.designer.codegen.components.model;

import org.talend.core.model.components.IComponentFileNaming;
import org.talend.core.model.components.IComponentsFactory;
import org.talend.core.model.components.IComponentsService;
import org.talend.designer.core.model.components.ComponentFilesNaming;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ven., 29 sept. 2006) nrousseau $
 *
 */
public class ComponentsService implements IComponentsService {

    IComponentsFactory componentsFactory = null;

    IComponentFileNaming componentsFileNaming = null;

    public IComponentFileNaming getComponentFileNaming() {
        if (componentsFileNaming == null) {
            componentsFileNaming = ComponentFilesNaming.getInstance();
        }
        return componentsFileNaming;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.components.IComponentsService#getComponentsFactory()
     */
    public IComponentsFactory getComponentsFactory() {
        if (componentsFactory == null) {
            componentsFactory = new ComponentsFactory();
        }
        return componentsFactory;
    }

}
