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
package org.talend.designer.scd.model;

/**
 * DOC hcw class global comment. Detailled comment
 */
public class Versioning {

    public static final String ACTIVE_LABEL = "active"; //$NON-NLS-1$

    public static final String VERSION_LABEL = "version"; //$NON-NLS-1$

    public static final String END_LABEL = "end"; //$NON-NLS-1$

    public static final String START_LABEL = "start"; //$NON-NLS-1$

    public static final String PREFIX = "scd_"; //$NON-NLS-1$

    public static final String DEFAULT_START_NAME = PREFIX + START_LABEL;

    public static final String DEFAULT_END_NAME = PREFIX + END_LABEL;

    public static final String DEFAULT_VERSION_NAME = PREFIX + VERSION_LABEL;

    public static final String DEFAULT_ACTIVE_NAME = PREFIX + ACTIVE_LABEL;

    private String startName = DEFAULT_START_NAME;

    private VersionStartType startType = VersionStartType.JOB_START_TIME;

    private String startComplement;

    private String endName = DEFAULT_END_NAME;

    private VersionEndType endType = VersionEndType.NULL;

    private String endComplement;

    private String versionName = DEFAULT_VERSION_NAME ;

    private String activeName = DEFAULT_ACTIVE_NAME;

    private boolean versionChecked;

    private boolean activeChecked;

    public boolean isVersionChecked() {
        return versionChecked;
    }

    public void setVersionChecked(boolean versionChecked) {
        if (versionName == null) {
            versionName = DEFAULT_VERSION_NAME;
        }
        this.versionChecked = versionChecked;
    }

    public boolean isActiveChecked() {
        return activeChecked;
    }

    public void setActiveChecked(boolean activeChecked) {
        if (activeName == null) {
            activeName = DEFAULT_ACTIVE_NAME;
        }
        this.activeChecked = activeChecked;
    }

    public String getStartName() {
        return startName;
    }

    public void setStartName(String startName) {
        this.startName = startName;
    }

    public VersionStartType getStartType() {
        return startType;
    }

    public void setStartType(VersionStartType startType) {
        this.startType = startType;
    }

    public String getStartComplement() {
        return startComplement;
    }

    public void setStartComplement(String startComplement) {
        this.startComplement = startComplement;
    }

    public String getEndName() {
        return endName;
    }

    public void setEndName(String endName) {
        this.endName = endName;
    }

    public VersionEndType getEndType() {
        return endType;
    }

    public void setEndType(VersionEndType endType) {
        this.endType = endType;
    }

    public String getEndComplement() {
        return endComplement;
    }

    public void setEndComplement(String endComplement) {
        this.endComplement = endComplement;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getActiveName() {
        return activeName;
    }

    public void setActiveName(String activeName) {
        this.activeName = activeName;
    }

}
