// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.components.localprovider;

import org.talend.designer.codegen.additionaljet.AbstractJetFileProvider;
import org.talend.designer.components.ComponentsLocalProviderPlugin;

/**
 * @author rdubois
 *
 */
public class MDMTriggerJetFileProvider extends AbstractJetFileProvider {

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.codegen.additionaljet.AbstractJetFileProvider#getBundleId()
     */
    @Override
    protected String getBundleId() {
        return ComponentsLocalProviderPlugin.PLUGIN_ID;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.codegen.additionaljet.AbstractJetFileProvider#getJetPath()
     */
    @Override
    protected String getJetPath() {
        return "resources/mdmTrigger"; //$NON-NLS-1$
    }

}
