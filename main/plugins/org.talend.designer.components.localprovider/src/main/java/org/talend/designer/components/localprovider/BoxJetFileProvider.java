package org.talend.designer.components.localprovider;

import org.talend.designer.codegen.additionaljet.AbstractJetFileProvider;
import org.talend.designer.components.ComponentsLocalProviderPlugin;

public class BoxJetFileProvider extends AbstractJetFileProvider {


    /* (non-Javadoc)
     * @see org.talend.designer.codegen.additionaljet.AbstractJetFileProvider#getBundleId()
     */
    @Override
    protected String getBundleId() {
        return ComponentsLocalProviderPlugin.PLUGIN_ID;
    }

    /* (non-Javadoc)
     * @see org.talend.designer.codegen.additionaljet.AbstractJetFileProvider#getJetPath()
     */
    @Override
    protected String getJetPath() {
        return "resources/box"; //$NON-NLS-1$
    }
	
}