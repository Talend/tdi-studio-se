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
package org.talend.repository.generic.internal;

import java.util.List;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.RepositoryNode;

/**
 * created by ycbai on 2015年9月21日 Detailled comment
 *
 */
public interface IGenericWizardInternalService {

    public ERepositoryObjectType createRepositoryType(String type, String label, String alias, String folder, int ordinal);

    public RepositoryNode createRepositoryNode(RepositoryNode curParentNode, String label, ERepositoryObjectType type,
            ENodeType nodeType);

    public ComponentService getComponentService();

    public ComponentWizard getComponentWizard(String name, String location);

    public List<ComponentWizard> getComponentWizardsForProperties(ComponentProperties properties, String location);

    public ComponentWizard getTopLevelComponentWizard(ComponentProperties properties, String location);

}
