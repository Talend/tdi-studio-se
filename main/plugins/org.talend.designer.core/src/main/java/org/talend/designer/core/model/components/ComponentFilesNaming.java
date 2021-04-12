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
package org.talend.designer.core.model.components;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.talend.commons.runtime.xml.XmlUtil;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IComponentFileNaming;
import org.talend.core.model.temp.ECodePart;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ven., 29 sept. 2006) nrousseau $
 *
 */
public final class ComponentFilesNaming implements IComponentFileNaming {

    private static ComponentFilesNaming singleton;

    private ComponentFilesNaming() {

    }

    public static ComponentFilesNaming getInstance() {
        if (singleton == null) {
            singleton = new ComponentFilesNaming();
        }
        return singleton;
    }

    public List<String> getRequiredFilesNames(String componentName, String languageSuffix) {
        List<String> toReturn = new ArrayList<String>();
        // toReturn.add(getMainXMLFileName(componentName, languageSuffix));
        toReturn.add(getPropertiesFileName(componentName));
        // toReturn.add(getIcon32FileName(componentName));
        return toReturn;
    }

    public String getMainXMLFileName(String componentName, String languageSuffix) {
        return componentName + "_" + languageSuffix + XmlUtil.FILE_XML_SUFFIX; //$NON-NLS-1$
    }

    public String getPropertiesFileName(String componentName) {
        return componentName + "_messages.properties"; //$NON-NLS-1$
    }

    public String getBundleName(String componentName, String source) {
        String baseName = source.replace(File.separatorChar, '.');
        return baseName + "." + componentName + "." + componentName + "_messages"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public String getJetFileName(IComponent component, String languageSuffix, ECodePart codePart) {
        return component.getName() + "_" + codePart + "." + languageSuffix + "jet"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public String getJetFileName(String filePrefix, String languageSuffix, ECodePart codePart) {
        return filePrefix + "_" + codePart + "." + languageSuffix + "jet"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public String getIcon32FileName(String componentName) {
        return componentName + "_icon32.png"; //$NON-NLS-1$
    }

    public String getIcon24FileName(String componentName) {
        return componentName + "_icon24.png"; //$NON-NLS-1$
    }

    public String getIcon16FileName(String componentName) {
        return componentName + "_icon16.png"; //$NON-NLS-1$
    }
}
