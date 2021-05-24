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
package org.talend.designer.core.ui.editor.properties.controllers;

import java.util.List;

import org.talend.core.IService;
import org.talend.core.model.metadata.IMetadataColumn;

public interface IDCService extends IService {

    public void setupRestHelperInstance(String host, String port, String userName, String password, String directory,
            boolean https);

    public void addColumnsToSchema(String entityName, String componentName, String prefix, List<IMetadataColumn> columnList);
}
