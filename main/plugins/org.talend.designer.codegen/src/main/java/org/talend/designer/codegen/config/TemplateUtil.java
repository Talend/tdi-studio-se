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
package org.talend.designer.codegen.config;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.talend.designer.core.model.components.ComponentBundleToPath;

/**
 * CodeGenerator Templates Ressources Utils.
 *
 * $Id$
 *
 */
public class TemplateUtil {

    public static final String JET_STUB_DIRECTORY = "jet_stub"; //$NON-NLS-1$

    // TODO SML Replace by File.separator
    public static final String DIR_SEP = "/"; //$NON-NLS-1$

    public static final String EXT_SEP = "."; //$NON-NLS-1$

    public static final String TEMPLATE_EXT = "jet"; //$NON-NLS-1$

    private String resourceName = ""; //$NON-NLS-1$

    private String version = ""; //$NON-NLS-1$

    public static final String RESOURCES_DIRECTORY_GENERIC = "generic"; //$NON-NLS-1$

    private String jetPluginRepository;

    private String templateRelativeUri;

    private EInternalTemplate template;

    /**
     * Constructor.
     *
     * @param resourceName
     */
    public TemplateUtil(EInternalTemplate template) {
        this.template = template;
        this.resourceName = template.getTemplateName();
        this.version = template.getVersion();
    }

    public TemplateUtil(String resourceName, String version) {
        this.resourceName = resourceName;
        this.version = version;
    }

    /**
     * Getter for resourceName.
     *
     * @return the resourceName
     */
    public String getResourceName() {
        return this.resourceName;
    }

    /**
     * Sets the resourceName.
     *
     * @param resourceName the resourceName to set
     */
    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    /**
     * Getter for version.
     *
     * @return the version
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Sets the version.
     *
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object arg0) {
        if (arg0 != null) {
            try {
                TemplateUtil t = (TemplateUtil) arg0;
                if (t.getResourceName() != null) {
                    if ((t.getResourceName().equals(this.getResourceName())) & (t.getVersion().equals(this.getVersion()))) {
                        return true;
                    }
                }
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    /**
     * Getter for jetPluginRepository.
     * @return the jetPluginRepository
     */
    public String getJetPluginRepository() {
        return jetPluginRepository;
    }

    /**
     * Sets the jetPluginRepository.
     * @param jetPluginRepository the jetPluginRepository to set
     */
    public void setJetPluginRepository(String jetPluginRepository) {
        this.jetPluginRepository = jetPluginRepository;
    }

    /**
     * Getter for templateRelativeUri.
     * @return the templateRelativeUri
     */
    public String getTemplateRelativeUri() {
        return templateRelativeUri;
    }

    /**
     * Sets the templateRelativeUri.
     * @param templateRelativeUri the templateRelativeUri to set
     */
    public void setTemplateRelativeUri(String templateRelativeUri) {
        this.templateRelativeUri = templateRelativeUri;
    }


    /**
     * Getter for template.
     * @return the template
     */
    public EInternalTemplate getType() {
        return this.template;
    }

    /**
     * Replacement of Platform.getPlugin(bundleName).getDescriptor().getInstallURL().toString()
     *
     * @param bundleName
     * @return
     */
    public static String getPlatformUrlOfBundle(String bundleName) {
    	if (ComponentBundleToPath.SHARED_STUDIO_CUSTOM_COMPONENT_BUNDLE.equals(bundleName)) {
    		String basePath = ComponentBundleToPath.getPathFromBundle(bundleName);
    		if (!basePath.endsWith("/")) {
    			basePath = basePath + "/";
    		}
    		return basePath;
    	} else {
            Bundle bundle = Platform.getBundle(bundleName);
            if (bundle == null) {
                return null;
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append("platform:/plugin/");
            sb.append(bundle.getSymbolicName());
            sb.append("_");
            sb.append(bundle.getVersion().toString());
            sb.append("/");   
            return sb.toString();	
    	}
    }
}
