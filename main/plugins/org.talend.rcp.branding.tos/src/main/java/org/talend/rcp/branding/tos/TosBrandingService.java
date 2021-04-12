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
package org.talend.rcp.branding.tos;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.osgi.framework.Bundle;
import org.talend.core.branding.AbstractTalendBrandingService;
import org.talend.core.branding.DefaultBrandingConfiguration;
import org.talend.core.ui.branding.IBrandingConfiguration;
import org.talend.rcp.branding.tos.i18n.Messages;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 *
 */
public class TosBrandingService extends AbstractTalendBrandingService {

    protected IBrandingConfiguration brandingConfigure;

    @Override
    public String getStartingBrowserId() {
        // won't show the starting page
        return null;
    }

    @Override
    public String getShortProductName() {
        return getProductName();
    }

    @Override
    public String getCorporationName() {
        return Messages.getString("corporationname"); //$NON-NLS-1$
    }

    @Override
    public ImageDescriptor getLoginVImage() {
        return Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, Messages.getString("loginimageleft")); //$NON-NLS-1$
    }

    @Override
    public ImageDescriptor getLoginHImage() {
        return Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, Messages.getString("loginimagehigh")); //$NON-NLS-1$
    }

    @Override
    public URL getLicenseFile() throws IOException {
        final Bundle b = Platform.getBundle(Activator.PLUGIN_ID);
        final URL url = FileLocator.toFileURL(FileLocator.find(b, new Path("resources/license.txt"), null)); //$NON-NLS-1$
        return url;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.ui.branding.IBrandingService#getBrandingConfiguration()
     */
    @Override
    public IBrandingConfiguration getBrandingConfiguration() {
        if (brandingConfigure == null) {
            brandingConfigure = new DefaultBrandingConfiguration();
        }
        return brandingConfigure;
    }

    @Override
    public String getAcronym() {
        return "tos_di";
    }

    @Override
    public String getJobLicenseHeader(String version) {
        return Messages.getString("TosBrandingService_job_license_header_content", this.getFullProductName(), version);
    }

    @Override
    public String getRoutineLicenseHeader(String version) {
        return Messages.getString("TosBrandingService_routine_license_header_content", this.getFullProductName(), version);
    }

    @Override
    public String getProductName() {
        return "Talend Open Studio";
    }

    @Override
    public String getOptionName() {
        return "for Data Integration";
    }

    @Override
    public String getUserManuals() {
        return "DI";
    }
}
