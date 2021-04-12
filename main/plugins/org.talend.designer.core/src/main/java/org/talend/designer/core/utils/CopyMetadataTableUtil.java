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
package org.talend.designer.core.utils;

import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataToolHelper;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.runtime.IAdditionalInfo;
import org.talend.core.runtime.services.IGenericWizardService;

/**
 * @author hwang
 *
 */
public class CopyMetadataTableUtil {
	
	public void copyTable(IMetadataTable newOutputMetadata, INode inode, String currentConnector) {
    	for (INodeConnector connector : inode.getListConnector()) {
            if (hasSameSchema(connector, currentConnector)) {
                // if there is some other schema dependant of this one, modify them
                MetadataToolHelper.copyTable(newOutputMetadata, inode.getMetadataFromConnector(connector.getName()));
                updateComponentSchema(inode, inode.getMetadataFromConnector(connector.getName()));
            }
        }
    }
	
    private boolean hasSameSchema(final INodeConnector connector, String currentConnector) {
        return (!connector.getName().equals(currentConnector)) && connector.getBaseSchema().equals(currentConnector)
                && !isTacokit(connector);
    }
    
    private boolean isTacokit(final INodeConnector connector) {
        return (IAdditionalInfo.class.isInstance(connector))
                && "tacokit".equals(IAdditionalInfo.class.cast(connector).getInfo("CONNECTOR_TYPE"));
    }
    
    private void updateComponentSchema(INode selectedNode, IMetadataTable table) {
        IGenericWizardService wizardService = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IGenericWizardService.class)) {
            wizardService = (IGenericWizardService) GlobalServiceRegister.getDefault().getService(IGenericWizardService.class);
        }
        if (wizardService != null) {
            wizardService.updateComponentSchema(selectedNode, table);
        }
    }
}
