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
package org.talend.designer.core.ui.editor;

import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.ui.editor.JobEditorInput;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.projectsetting.ProjectSettingManager;

/**
 * smallet class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class ProcessEditorInput extends JobEditorInput {

    public ProcessEditorInput(ProcessItem processItem, boolean load) throws PersistenceException {
        this(processItem, load, null, null);
    }

    public ProcessEditorInput(ProcessItem processItem, boolean load, Boolean lastVersion) throws PersistenceException {
        this(processItem, load, lastVersion, null);
    }

    public ProcessEditorInput(ProcessItem processItem, boolean load, Boolean lastVersion, Boolean readonly)
            throws PersistenceException {
        super(processItem, load, lastVersion, readonly);
    }

    protected void saveProcessBefore() {
        // use project setting true
        ProjectSettingManager.defaultUseProjectSetting(getLoadedProcess());
    }

    protected Process createProcess() {
        return new Process(getItem().getProperty());
    }

    public Process getLoadedProcess() {
        return (Process) loadedProcess;
    }

}
