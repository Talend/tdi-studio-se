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
package org.talend.designer.codegen.config;

import java.io.Serializable;

/**
 * DOC mhirt class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class LightJetBean implements Serializable {

    private static final long serialVersionUID = 2549445027941196550L;

    private String templateRelativeUri = ""; //$NON-NLS-1$

    private String className = ""; //$NON-NLS-1$

    private String methodName = ""; //$NON-NLS-1$

    private String version = ""; //$NON-NLS-1$

    private String language = ""; //$NON-NLS-1$

    private long crc = 0;

    /**
     * DOC mhirt LightJetBean constructor comment.
     *
     * @param templateRelativeUri
     * @param className
     * @param methodName
     * @param version
     */
    public LightJetBean(String templateRelativeUri, String className, String methodName, String version,
            String language, long crc) {
        this.templateRelativeUri = templateRelativeUri;
        this.className = className;
        this.methodName = methodName;
        this.version = version;
        this.language = language;
        this.crc = crc;
    }

    public LightJetBean(String templateRelativeUri, String version, long crc) {
        super();
        this.templateRelativeUri = templateRelativeUri;
        this.version = version;
        this.crc = crc;
    }

    /**
     * Getter for className.
     *
     * @return the className
     */
    public String getClassName() {
        return this.className;
    }

    /**
     * Sets the className.
     *
     * @param className the className to set
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * Getter for methodName.
     *
     * @return the methodName
     */
    public String getMethodName() {
        return this.methodName;
    }

    /**
     * Sets the methodName.
     *
     * @param methodName the methodName to set
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    /**
     * Getter for templateRelativeUri.
     *
     * @return the templateRelativeUri
     */
    public String getTemplateRelativeUri() {
        return this.templateRelativeUri;
    }

    /**
     * Sets the templateRelativeUri.
     *
     * @param templateRelativeUri the templateRelativeUri to set
     */
    public void setTemplateRelativeUri(String templateRelativeUri) {
        this.templateRelativeUri = templateRelativeUri;
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

    /**
     * Getter for crc.
     *
     * @return the crc
     */
    public long getCrc() {
        return this.crc;
    }

    /**
     * Sets the crc.
     *
     * @param crc the crc to set
     */
    public void setCrc(long crc) {
        this.crc = crc;
    }

    /**
     * Getter for language.
     *
     * @return the language
     */
    public String getLanguage() {
        return this.language;
    }

    /**
     * Sets the language.
     *
     * @param language the language to set
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LightJetBean other = (LightJetBean) obj;
        if (this.templateRelativeUri == null) {
            if (other.templateRelativeUri != null) {
                return false;
            }
        } else if (!this.templateRelativeUri.equals(other.templateRelativeUri)) {
            return false;
        } else if (this.crc != other.crc) {
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = prime + ((this.templateRelativeUri == null) ? 0 : this.templateRelativeUri.hashCode());
        return result;
    }

}
