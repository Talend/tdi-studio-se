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
package org.talend.repository.ui.dialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.talend.commons.utils.time.TimeMeasure;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.i18n.Messages;
import org.talend.repository.viewer.ui.provider.RepoCommonViewerProvider;

/**
 * created by wchen on 2013-9-16 Detailled comment
 *
 */
public abstract class ExtendedNodeReviewDialog extends Dialog {

    protected Object result;

    protected TreeViewer treeViewer;

    private ViewerTextFilter textFilter;

    protected ERepositoryObjectType rootType;

    protected List<ERepositoryObjectType> typesToShow = new ArrayList<ERepositoryObjectType>();

    protected RepoCommonViewerProvider provider;

    /**
     * DOC wchen RepCommonReviewDialog constructor comment.
     *
     * @param parentShell
     */
    protected ExtendedNodeReviewDialog(Shell parentShell, ERepositoryObjectType rootType, ERepositoryObjectType typeToShow) {
        super(parentShell);
        this.rootType = rootType;
        if (typeToShow != null) {
            typesToShow.add(typeToShow);
        }
        typesToShow.addAll(getParentTypes(typesToShow));
        provider = new RepoCommonViewerProvider();
    }

    protected ExtendedNodeReviewDialog(Shell parentShell, ERepositoryObjectType rootType, List<ERepositoryObjectType> typesToShow) {
        super(parentShell);
        this.rootType = rootType;
        if (typesToShow != null) {
            this.typesToShow.addAll(typesToShow);
        }
        typesToShow.addAll(getParentTypes(typesToShow));
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Repository Content");
    }

    private List<ERepositoryObjectType> getParentTypes(List<ERepositoryObjectType> typesToShow) {
        List<ERepositoryObjectType> parentTypes = new ArrayList<ERepositoryObjectType>();
        for (ERepositoryObjectType type : typesToShow) {
            if (type.getParentTypesArray().length != 0) {
                for (ERepositoryObjectType parentType : type.getParentTypesArray()) {
                    parentTypes.add(parentType);
                }
                parentTypes.addAll(getParentTypes(Arrays.asList(type.getParentTypesArray())));
            }
        }
        return parentTypes;
    }

    @Override
    protected Control createContents(Composite parent) {
        Control control = super.createContents(parent);
        getButton(IDialogConstants.OK_ID).setEnabled(false);
        return control;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        TimeMeasure.step(RepositoryReviewDialog.class.getSimpleName(), "before createDialogArea..."); //$NON-NLS-1$
        GridData data = (GridData) container.getLayoutData();
        data.minimumHeight = 400;
        data.heightHint = 400;
        data.minimumWidth = 500;
        data.widthHint = 500;
        container.setLayoutData(data);

        textFilter = new ViewerTextFilter();

        createFilterField(container);

        treeViewer = provider.createViewer(container);

        treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                getButton(IDialogConstants.OK_ID).setEnabled(isValidSelection(event));
            }

        });
        treeViewer.addDoubleClickListener(new IDoubleClickListener() {

            public void doubleClick(DoubleClickEvent event) {
                if (getButton(IDialogConstants.OK_ID).isEnabled()) {
                    okPressed();
                }
            }
        });

        treeViewer.addFilter(textFilter);
        treeViewer.addFilter(new ViewerFilter() {

            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                return filterByType(viewer, parentElement, element);
            }
        });

        return container;
    }

    private void createFilterField(Composite container) {
        // create text filter
        Label label = new Label(container, SWT.NONE);
        label.setText(Messages.getString("RepositoryReviewDialog.jobNameFormat")); //$NON-NLS-1$
        label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        final Text text = new Text(container, SWT.BORDER);
        text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        text.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                String pattern = text.getText();
                pattern = pattern.replace("*", ".*"); //$NON-NLS-1$ //$NON-NLS-2$
                pattern = pattern.replace("?", "."); //$NON-NLS-1$ //$NON-NLS-2$
                pattern = "(?i)" + pattern + ".*"; //$NON-NLS-1$ //$NON-NLS-2$
                textFilter.setText(pattern);
                treeViewer.refresh();
            }
        });
    }

    protected abstract boolean filterByText(Viewer viewer, Object parentElement, Object element, String text);

    protected abstract boolean filterByType(Viewer viewer, Object parentElement, Object element);

    protected abstract boolean isValidSelection(SelectionChangedEvent event);

    public Object getResult() {
        return this.result;
    }

    class ViewerTextFilter extends ViewerFilter {

        private String text = null;

        public void setText(String text) {
            this.text = text;
        }

        @Override
        public boolean select(Viewer viewer, Object parentElement, Object element) {
            return filterByText(viewer, parentElement, element, text);
        }
    }

}
