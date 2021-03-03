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
package org.talend.repository.ui.actions.importproject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.talend.core.language.ECodeLanguage;

/**
 * DOC Administrator class global comment. Detailled comment <br/>
 *
 */

public class DemoProjectBean {

    private String projectName;

    private ECodeLanguage language = ECodeLanguage.JAVA;

    private String demoProjectFilePath;

    private String descriptionFilePath;

    private EDemoProjectFileType demoProjectFileType;

    private String descriptionContents;

    private String iconUrl;

    private String demoDesc;

    /**
     * the plugin id where the demo src is
     */
    private String pluginId;

    public String getPluginId() {
        return pluginId;
    }

    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    /**
     * Getter for archiveFilePath.
     *
     * @return the archiveFilePath
     */
    public String getDemoProjectFilePath() {
        return demoProjectFilePath;
    }

    /**
     * Sets the archiveFilePath.
     *
     * @param archiveFilePath the archiveFilePath to set
     */
    public void setDemoProjectFilePath(String archiveFilePath) {
        this.demoProjectFilePath = archiveFilePath;
    }

    /**
     * Getter for language.
     *
     * @return the language
     * @deprecated no need, only support java currently
     */
    @Deprecated
    public ECodeLanguage getLanguage() {
        return language;
    }

    /**
     * Sets the language.
     *
     * @param language the language to set
     * @deprecated no need, only support java currently
     */
    @Deprecated
    public void setLanguage(ECodeLanguage language) {
        this.language = language;
    }

    /**
     * Getter for projectName.
     *
     * @return the projectName
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * Sets the projectName.
     *
     * @param projectName the projectName to set
     */
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getDescriptionFilePath() {
        return this.descriptionFilePath;
    }

    public void setDescriptionFilePath(String descriptionFilePath) {
        this.descriptionFilePath = descriptionFilePath;
    }

    /**
     * Getter for demoProjectFileType.
     *
     * @return the demoProjectFileType
     */
    public EDemoProjectFileType getDemoProjectFileType() {
        return demoProjectFileType;
    }

    /**
     * Sets the demoProjectFileType.
     *
     * @param demoProjectFileType the demoProjectFileType to set
     */
    public void setDemoProjectFileType(EDemoProjectFileType demoProjectFileType) {
        this.demoProjectFileType = demoProjectFileType;
    }

    public String getDescriptionContents() {
        if (this.descriptionContents == null) {
            synchronized (DemoProjectBean.class) {
                this.descriptionContents = getDemoProjectDescriptionContent();
            }
        }
        return this.descriptionContents;
    }

    private String getDemoProjectDescriptionContent() {
        Bundle bundle = Platform.getBundle(getPluginId());
        if (bundle != null) {
            URL url = null;
            try {
                url = FileLocator.resolve(bundle.getEntry(getDescriptionFilePath()));
            } catch (IOException e) {
                //
            }
            if (url != null) {
                InputStream is = null;
                try {
                    try {
                        is = url.openStream();
                    } catch (IOException e) {
                        String filePath = new Path(url.getFile()).toOSString();
                        is = new FileInputStream(filePath);
                    }
                    StringWriter sw = new StringWriter();
                    int c = -1;
                    while ((c = is.read()) != -1) {
                        sw.write(c);
                    }
                    return sw.getBuffer().toString();
                } catch (IOException e) {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e1) {
                            //
                        }
                    }
                }
            }
        }

        return ""; //$NON-NLS-1$
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public void setDescriptionContents(String descriptionContents) {
        this.descriptionContents = descriptionContents;
    }

    public String getDemoDesc() {
        return demoDesc;
    }

    public void setDemoDesc(String demoDesc) {
        this.demoDesc = demoDesc;
    }

}
