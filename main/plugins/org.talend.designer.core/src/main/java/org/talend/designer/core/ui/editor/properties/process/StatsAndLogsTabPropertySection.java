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

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.talend.core.model.process.EComponentCategory;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.editor.process.ProcessPart;
import org.talend.designer.core.ui.editor.properties.DynamicTabbedPropertySection;
import org.talend.repository.model.RepositoryNode;

/**
 * DOC Administrator class global comment. Detailed comment <br/>
 *
 */
public class StatsAndLogsTabPropertySection extends DynamicTabbedPropertySection {

    public StatsAndLogsTabPropertySection() {
        super(EComponentCategory.STATSANDLOGS);
    }

    public void setInput(final IWorkbenchPart workbenchPart, final ISelection selection) {
        Object input = ((IStructuredSelection) selection).getFirstElement();
        if (input instanceof RepositoryNode) {
            // This is the only RepositoryNode that displays the Job.
            Process process = StatsAndLogsSectionFilter.getProcessPartByRepositoryNode((RepositoryNode) input);
            if (process == null) {
                return;
            }
            // make a mock processPart here for super.setInput();
            ProcessPart part = new ProcessPart();
            part.setModel(process);

            StructuredSelection sel = new StructuredSelection(part);
            super.setInput(workbenchPart, sel);
        } else {
            super.setInput(workbenchPart, selection);
        }
    }

    IWorkbenchPart oldPart;

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.properties.DynamicTabbedPropertySection#refresh()
     */
    @Override
    public void refresh() {
        addComponents(false);
        super.refresh();
    }
}
