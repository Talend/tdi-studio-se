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
package org.talend.designer.core.ui;

import org.eclipse.gef.EditPart;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.part.MultiPageSelectionProvider;

/**
 * Extension of the standard MultiPageSelectionProvider. This class avoid to send all events to the TalendEditor (Gef
 * part) <br/>
 *
 * $Id: MultiPageTalendSelectionProvider.java 7038 2007-11-15 14:05:48Z plegall $
 *
 */
public class MultiPageTalendSelectionProvider extends MultiPageSelectionProvider implements ISelectionProvider {

    public void fireSelectionChanged(SelectionChangedEvent event) {
        ISelection sel = event.getSelection();
        if (sel instanceof StructuredSelection) {
            StructuredSelection structSel = (StructuredSelection) sel;
            if (structSel.getFirstElement() instanceof EditPart) {
                super.fireSelectionChanged(event);
            }
        }
    }

    public MultiPageTalendSelectionProvider(AbstractMultiPageTalendEditor multiPageEditor) {
        super(multiPageEditor);
    }

}
