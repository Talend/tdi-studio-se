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
package org.talend.repository.model;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * DOC zwxue class global comment. Detailled comment
 *
 * @deprecated
 */
public class DeploymentConfsModel {

    private String confName;

    private boolean deleted;

    private Map<String, String> modules = new LinkedHashMap<>();

    private Set<Object> elements = new LinkedHashSet<>();

    public DeploymentConfsModel(String confName) {
        super();
        this.confName = confName;
    }

    public String getConfName() {
        return confName;
    }

    public void setConfName(String confName) {
        this.confName = confName;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Map<String, String> getModules() {
        return modules;
    }

    public Set<Object> getElements() {
        return elements;
    }

}
