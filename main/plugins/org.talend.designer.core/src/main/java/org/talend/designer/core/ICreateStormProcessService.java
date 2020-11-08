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
package org.talend.designer.core;

import org.eclipse.jface.action.IAction;
import org.talend.core.IService;
import org.talend.core.model.properties.Property;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.repository.model.RepositoryNode;

/**
 * DOC zwzhao class global comment. Detailled comment
 */
public interface ICreateStormProcessService extends IService {

    public Process createSparkProcess(Property property);

    public Process createSparkStreamProcess(Property property);

}
