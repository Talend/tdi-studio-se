// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.ui.routine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.Property;
import org.talend.designer.core.i18n.Messages;

/**
 * ggu class global comment. Detailled comment
 */
public class ShowRoutineItemsDialog extends Dialog {

    private Map<Project, List<Property>> allRoutineItems = null;

    private List<RoutineItemRecord> existedRoutinesRecord = null;

    private boolean system;

    private TreeViewer routineViewer;

    private List<Property> selectedItems = new ArrayList<Property>();

    private ShowRoutineItemsLabelProvider labelProvider;

    public ShowRoutineItemsDialog(Shell parentShell, Map<Project, List<Property>> allRoutineItems,
            List<RoutineItemRecord> existedRoutinesRecord, boolean system) {
        super(parentShell);
        this.allRoutineItems = allRoutineItems;
        this.existedRoutinesRecord = existedRoutinesRecord;
        this.system = system;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        if (system) {
            newShell.setText(Messages.getString("ShowRoutineItemsDialog.systemTitle")); //$NON-NLS-1$
        } else {
            newShell.setText(Messages.getString("ShowRoutineItemsDialog.title")); //$NON-NLS-1$
        }
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        routineViewer = new TreeViewer(composite);

        labelProvider = new ShowRoutineItemsLabelProvider(allRoutineItems, existedRoutinesRecord);

        routineViewer.setContentProvider(labelProvider);
        routineViewer.setLabelProvider(labelProvider);
        routineViewer.setInput(allRoutineItems);
        routineViewer.setFilters(new ViewerFilter[] { new ShowRoutineItemsViewerFilter(allRoutineItems, system) });
        GridData layoutData = new GridData(GridData.FILL_BOTH);
        layoutData.heightHint = 150;
        layoutData.widthHint = 200;
        routineViewer.getTree().setLayoutData(layoutData);

        routineViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                updateButtons();
            }
        });
        return composite;
    }

    @Override
    protected Control createContents(Composite parent) {
        Control createContents = super.createContents(parent);
        updateButtons();
        return createContents;
    }

    @SuppressWarnings("unchecked")
    private void updateButtons() {
        IStructuredSelection selection = (IStructuredSelection) routineViewer.getSelection();
        boolean enabled = !selection.isEmpty();
        Iterator iterator = selection.iterator();
        while (iterator.hasNext()) {
            Object element = iterator.next();
            if (element instanceof Property && labelProvider.existed(element) || element instanceof Project) {
                enabled = false;
                break;
            }
        }
        Button button = getButton(IDialogConstants.OK_ID);
        if (button != null && !button.isDisposed()) {
            button.setEnabled(enabled);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void okPressed() {
        selectedItems.clear();
        List list = ((IStructuredSelection) routineViewer.getSelection()).toList();
        selectedItems.addAll(list);
        super.okPressed();
    }

    public Property[] getSelectedItems() {
        return this.selectedItems.toArray(new Property[0]);
    }

}
