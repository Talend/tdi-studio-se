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
package org.talend.designer.core.ui.action;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PlatformUI;
import org.talend.core.model.process.ElementParameterParser;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.designer.core.ui.MultiPageTalendEditor;

/**
 * bqian Save the job if it is refered by a tRunJob. <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 *
 */
public class SaveJobBeforeRunAction extends Action {

    private static final String T_RUN_JOB_NAME = "tRunJob"; //$NON-NLS-1$

    IProcess activeProcess;

    /**
     * DOC bqian SaveJobBeforeRunAction constructor comment.
     *
     * @param activeProcess
     */
    public SaveJobBeforeRunAction(IProcess activeProcess) {
        this.activeProcess = activeProcess;
    }

    public void run() {
        boolean saveMyself = false;
        List<? extends INode> nodes = filterTRunJobNode();
        if (nodes.isEmpty()) {
            return;
        }
        IEditorReference[] references = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                .getEditorReferences();
        for (int i = 0; i < references.length; i++) {
            IEditorPart part = references[i].getEditor(false);
            if (!(part instanceof MultiPageTalendEditor)) {
                continue;
            }
            MultiPageTalendEditor editor = (MultiPageTalendEditor) part;
            saveJob(nodes, editor);
        }
    }

    /**
     * DOC bqian Comment method "saveJob".
     *
     * @param nodes
     * @param editor
     */
    private void saveJob(List<? extends INode> nodes, MultiPageTalendEditor editor) {
        for (Iterator<? extends INode> iter = nodes.iterator(); iter.hasNext();) {
            INode node = iter.next();
            boolean saveBeforeRun = Boolean.parseBoolean(ElementParameterParser.getValue(node, "__SAVE_BEFORE_RUN__")); //$NON-NLS-1$
            String process = ElementParameterParser.getValue(node, "__PROCESS_TYPE_PROCESS__"); //$NON-NLS-1$
            String editorName = editor.getProcess().getName();
            if (saveBeforeRun && process.indexOf(editorName) > -1) {
                editor.doSave(new NullProgressMonitor());
            }
        }
    }

    private List<? extends INode> filterTRunJobNode() {
        List<? extends INode> list = new ArrayList<INode>(activeProcess.getGeneratingNodes());
        for (Iterator<? extends INode> iter = list.iterator(); iter.hasNext();) {
            INode node = iter.next();
            if (!node.getComponent().getName().equals(T_RUN_JOB_NAME)) {
                iter.remove();
            }
        }
        return list;
    }
}
