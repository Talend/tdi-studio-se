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
package org.talend.designer.runprocess.ui.views;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;

/**
 * created by ldong on Mar 25, 2015 Detailled comment
 *
 */
public class ProcessViewSelectionProvider implements ISelectionProvider {

    private ISelection selection;

    public ProcessViewSelectionProvider(ISelection selection) {
        this.selection = selection;
    }

    @Override
    public void addSelectionChangedListener(ISelectionChangedListener listener) {

    }

    @Override
    public ISelection getSelection() {
        return selection;
    }

    @Override
    public void removeSelectionChangedListener(ISelectionChangedListener listener) {

    }

    @Override
    public void setSelection(ISelection selection) {
        this.selection = selection;
    }

}
