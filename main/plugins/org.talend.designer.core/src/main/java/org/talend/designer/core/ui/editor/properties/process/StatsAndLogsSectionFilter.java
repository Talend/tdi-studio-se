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
package org.talend.designer.core.ui.editor.properties.process;

import org.eclipse.jface.viewers.IFilter;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PlatformUI;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.designer.core.ui.editor.ProcessEditorInput;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.editor.process.ProcessPart;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.IRepositoryNode.EProperties;

/**
 * Section filter. <br/>
 *
 */
public class StatsAndLogsSectionFilter implements IFilter {

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.IFilter#select(java.lang.Object)
     */
    public boolean select(Object toTest) {
        if (toTest instanceof ProcessPart) {
            return true;
        }
        if (toTest instanceof RepositoryNode) {
            RepositoryNode node = (RepositoryNode) toTest;
            if (node.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.PROCESS) {
                if (getProcessPartByRepositoryNode(node) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Find out if the editor related to the node is open.
     *
     * @param node
     * @return
     */
    public static Process getProcessPartByRepositoryNode(RepositoryNode node) {
        IEditorReference[] editors = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();

        for (int i = 0; i < editors.length; i++) {
            try {
                IEditorInput editorInput = editors[i].getEditorInput();
                if (editorInput instanceof ProcessEditorInput) {
                    ProcessEditorInput processEditorInput = (ProcessEditorInput) editorInput;
                    if (node.getObject().getProperty().equals(processEditorInput.getRepositoryNode().getObject().getProperty())) {
                        return processEditorInput.getLoadedProcess();
                    }
                }
            } catch (Exception e) {
                continue;
            }

        }
        return null;
    }

}
