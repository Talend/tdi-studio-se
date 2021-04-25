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
package org.talend.designer.dbmap.ui.dnd;

import java.util.Arrays;
import java.util.List;

import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.DefaultRepositoryComponentDndFilter;
import org.talend.core.repository.RepositoryComponentManager;
import org.talend.core.repository.RepositoryComponentSetting;
import org.talend.repository.model.RepositoryNode;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class ETLMapRepositoryComponentDndFilter extends DefaultRepositoryComponentDndFilter {

    public ETLMapRepositoryComponentDndFilter() {
    }

    @Override
    public boolean valid(Item item, ERepositoryObjectType type, RepositoryNode seletetedNode, IComponent component,
            String repositoryType) {
        RepositoryComponentSetting setting = RepositoryComponentManager.getSetting(item, type);
        if (setting == null || component == null) {
            return false;
        }
        String originalFamilyName = component.getOriginalFamilyName();
        if (originalFamilyName.startsWith("ELT")) { //$NON-NLS-1$
            EDatabaseTypeName[] dbTypes = setting.getDbTypes();
            if (dbTypes != null && dbTypes.length > 0) {
                String product = null;
                List<EDatabaseTypeName> list = Arrays.asList(dbTypes);
                if (list.contains(EDatabaseTypeName.ORACLEFORSID) || list.contains(EDatabaseTypeName.ORACLESN)
                        || list.contains(EDatabaseTypeName.ORACLE_OCI) || list.contains(EDatabaseTypeName.ORACLE_CUSTOM)) {
                    product = EDatabaseTypeName.ORACLEFORSID.getProduct();
                } else {
                    product = dbTypes[0].getDisplayName();
                }
                // maybe, it's not good like this
                if (product != null && originalFamilyName.toUpperCase().endsWith(product.toUpperCase())) {
                    if (type == ERepositoryObjectType.METADATA_CONNECTIONS) {
                        if (component.getName().toUpperCase().endsWith(MAP)) {
                            return true;
                        }
                    }
                    if (type == ERepositoryObjectType.METADATA_CON_TABLE) {
                        if (!component.getName().toUpperCase().endsWith(MAP)) {
                            return true;
                        }
                    }
                }
            }

        }
        return false;

    }

}
