// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.codegen.jet.JETCompiler;
import org.eclipse.emf.codegen.jet.JETException;
import org.eclipse.emf.codegen.jet.JETMark;
import org.osgi.framework.Bundle;

/**
 * ggu class global comment. Detailled comment
 */
public class TalendJETCompiler extends JETCompiler {

    private static final Pattern PLUGIN_VAR_PATTERN = Pattern.compile("@\\{(.*?)\\}", Pattern.CANON_EQ); //$NON-NLS-1$

    protected ClassLoader classLoader;

    public TalendJETCompiler(String templateURI, String encoding, ClassLoader classLoader) throws JETException {
        super(templateURI, encoding);
        this.classLoader = classLoader;
    }

    public TalendJETCompiler(String[] templateURIPath, String relativeTemplateURI, String encoding, ClassLoader classLoader)
            throws JETException {
        super(templateURIPath, relativeTemplateURI, encoding);
        this.classLoader = classLoader;
    }

    @Override
    protected void handleNewSkeleton() {
        String packageName = skeleton.getPackageName();
        String skeletonClassName = skeleton.getClassName();
        String qualifiedSkeletonClassName = (packageName.length() == 0 ? "" : packageName + ".") + skeletonClassName; //$NON-NLS-1$ //$NON-NLS-2$

        if (classLoader != null) {
            try {
                Class<?> theClass = classLoader.loadClass(qualifiedSkeletonClassName);
                if (theClass != null) {
                    skeleton.setClassName(skeletonClassName += "_"); //$NON-NLS-1$
                }
            } catch (Exception exception) {
                // Ignore
            }
        }
    }

    @Override
    public void handleDirective(String directive, JETMark start, JETMark stop, Map<String, String> attributes)
            throws JETException {

        Map<String, String> newAttributes = new HashMap<String, String>();
        newAttributes.putAll(attributes);

        if (directive.equals("include")) { //$NON-NLS-1$
            final String fileKey = "file"; //$NON-NLS-1$

            String fileURI = newAttributes.get(fileKey);
            String newFileURI = checkAndReplace(fileURI);
            if (newFileURI != null) {
                newAttributes.put(fileKey, newFileURI);
            }
        } else if (directive.equals("jet")) { //$NON-NLS-1$
            String skeletonKey = "skeleton"; //$NON-NLS-1$
            String skeletonURI = newAttributes.get(skeletonKey);
            if (skeletonURI != null) {
                String newSkeletonURI = checkAndReplace(skeletonURI);
                if (newSkeletonURI != null) {
                    newAttributes.put(skeletonKey, newSkeletonURI);
                }
            }
        }
        super.handleDirective(directive, start, stop, newAttributes);
    }

    @SuppressWarnings("deprecation")
    private String checkAndReplace(String fileURI) {
        if (fileURI != null) {
            Matcher matcher = PLUGIN_VAR_PATTERN.matcher(fileURI);
            if (matcher.find()) {
                // get the plugin name from fileURI
                String refPluginName = matcher.group(1);
                // retrieve the plugin URI by pluginName.
                Bundle refBundle = Platform.getBundle(refPluginName);
                if (refBundle != null) {
                    String realURI = Platform.getPlugin(refPluginName).getDescriptor().getInstallURL().toString();
                    // replace the old fileURI to new one by pluginURI
                    String newFileURI = fileURI.replaceFirst(PLUGIN_VAR_PATTERN.pattern(), realURI);
                    return newFileURI;

                }
            }
        }
        return null; // not replace
    }
}
