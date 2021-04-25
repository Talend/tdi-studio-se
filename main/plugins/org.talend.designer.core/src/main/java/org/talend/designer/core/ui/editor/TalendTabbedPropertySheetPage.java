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

import org.eclipse.gef.EditPart;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.contentoutline.ContentOutline;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.editor.outline.NodeTreeEditPart;

/**
 * The TabbedPropertySheetPage will only change when an EditPart is selected. This class was created due to a problem
 * with the default properties and the displayed ISection. In some cases it was possible to see some sections even
 * without select an EditPart.
 *
 * $Id$
 *
 */
public class TalendTabbedPropertySheetPage extends TabbedPropertySheetPage {

    public TalendTabbedPropertySheetPage(ITabbedPropertySheetPageContributor tabbedPropertySheetPageContributor) {
        super(tabbedPropertySheetPageContributor);
    }

    StructuredSelection oldSelection;

    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        ISelection newSelection;
        if (part instanceof AbstractMultiPageTalendEditor) {
            AbstractMultiPageTalendEditor mpte = (AbstractMultiPageTalendEditor) part;
            newSelection = mpte.getTalendEditor().getViewer().getSelection();
            if (selection instanceof StructuredSelection) {
                StructuredSelection structSel = (StructuredSelection) newSelection;
                if (structSel.size() != 1) {
                    return;
                }
                if (structSel.getFirstElement() instanceof EditPart) {
                    if (structSel.equals(oldSelection)) {
                        // if (getCurrentTab() != null) {
                        // getCurrentTab().setInput(part, selection);
                        // }
                    } else {
                        super.selectionChanged(part, selection);
                    }
                    oldSelection = structSel;
                }
            }
        } else if (part instanceof ContentOutline) {
            ContentOutline outline = (ContentOutline) part;
            newSelection = outline.getSelection();
            if (selection instanceof StructuredSelection) {
                StructuredSelection structSel = (StructuredSelection) newSelection;
                if (structSel.size() != 1) {
                    return;
                }
                if (structSel.getFirstElement() instanceof NodeTreeEditPart) {
                    if (structSel.equals(oldSelection)) {
                        // this.getCurrentTab().setInput(part, selection);
                    } else {
                        super.selectionChanged(part, selection);
                    }
                    oldSelection = structSel;
                }
            }
        }
    }

    public StructuredSelection getOldSelection() {
        return oldSelection;
    }
}
