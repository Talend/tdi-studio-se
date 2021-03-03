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
package org.talend.designer.unifiedcomponent.unifier;

import java.util.Map;
import java.util.Set;

import org.talend.designer.unifiedcomponent.delegate.service.IComponentDelegate;

/**
 * created by wchen on Dec 4, 2017 Detailled comment
 *
 */
public interface IComponentsUnifier {

    public IComponentDelegate getDelegateComponent();

    public void setDelegateComponent(IComponentDelegate component);

    public String getDisplayName();

    public String getComponentName();

    public Map<String, String> getParameterMapping();

    public Map<String, String> getConnectorMapping();

    public Set<String> getMappingExclude();

    public Set<String> getCategories();

    public Set<String> getFamilies();

}
