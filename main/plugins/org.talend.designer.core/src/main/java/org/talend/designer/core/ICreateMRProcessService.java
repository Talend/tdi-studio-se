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
package org.talend.designer.core;

import org.eclipse.jface.action.IAction;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.IService;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.designer.core.ui.editor.ProcessEditorInput;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.repository.model.RepositoryNode;

/**
 * DOC talend class global comment. Detailled comment
 */
public interface ICreateMRProcessService extends IService {

    public Process createMRProcess(Property property);

    public ProcessEditorInput getMRProcessEditorInput(ProcessItem processItem, boolean load) throws PersistenceException;

    public ProcessEditorInput getMRProcessEditorInput(ProcessItem processItem, boolean load, boolean lastVersion,
            boolean readOnly) throws PersistenceException;

    public IAction getEditProcessAction(RepositoryNode result);
}
