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
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.designer.core.i18n.Messages;

/**
 * ggu class global comment. Detailled comment
 */
public class ShowRoutineItemsDialog extends Dialog {

    private Map<Project, List<Property>> allItems = null;

    private List<RoutineItemRecord> existedRecords = null;

    private ERepositoryObjectType type;

    private TreeViewer viewer;

    private List<Property> selectedItems = new ArrayList<Property>();

    private ShowRoutineItemsLabelProvider labelProvider;

    public ShowRoutineItemsDialog(Shell parentShell, Map<Project, List<Property>> allItems, List<RoutineItemRecord> currentRecords,
            ERepositoryObjectType type) {
        super(parentShell);
        this.allItems = allItems;
        this.existedRecords = currentRecords;
        this.type = type;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        if (type == ERepositoryObjectType.ROUTINES) {
            newShell.setText(Messages.getString("ShowGlobalRoutineItemsDialog.title")); //$NON-NLS-1$
        } else if (type == ERepositoryObjectType.ROUTINESJAR) {
            newShell.setText(Messages.getString("ShowRoutinesJarItemsDialog.title")); //$NON-NLS-1$
        } else if (type == ERepositoryObjectType.BEANSJAR) {
            newShell.setText(Messages.getString("ShowBeansJarItemsDialog.title")); //$NON-NLS-1$
        }
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        viewer = new TreeViewer(composite);

        labelProvider = new ShowRoutineItemsLabelProvider(allItems, existedRecords);

        viewer.setContentProvider(labelProvider);
        viewer.setLabelProvider(labelProvider);
        viewer.setInput(allItems);
        viewer.setFilters(new ViewerFilter[] { new ShowRoutineItemsViewerFilter(allItems) });
        GridData layoutData = new GridData(GridData.FILL_BOTH);
        layoutData.heightHint = 150;
        layoutData.widthHint = 200;
        viewer.getTree().setLayoutData(layoutData);

        viewer.addSelectionChangedListener(new ISelectionChangedListener() {

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

    @SuppressWarnings("rawtypes")
    private void updateButtons() {
        IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
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

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected void okPressed() {
        selectedItems.clear();
        List list = ((IStructuredSelection) viewer.getSelection()).toList();
        selectedItems.addAll(list);
        super.okPressed();
    }

    public Property[] getSelectedItems() {
        return this.selectedItems.toArray(new Property[0]);
    }

}
