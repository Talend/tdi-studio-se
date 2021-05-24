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
package org.talend.designer.runprocess;

import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.process.IContainerEntry;
import org.talend.core.model.process.Problem;
import org.talend.designer.runprocess.i18n.Messages;

/**
 * DOC chuang class global comment. Detailled comment
 */
public class ErrorDetailDialog extends SelectionDialog {

    private List<IContainerEntry> errors;

    private TreeViewer viewer;

    private boolean isTestcase;

    /**
     * DOC chuang ErrorDetailDialog constructor comment.
     *
     * @param parentShell
     */
    public ErrorDetailDialog(Shell parentShell, List<IContainerEntry> errors) {
        super(parentShell);
        setShellStyle(SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL | getDefaultOrientation());
        setHelpAvailable(false);
        setTitle(Messages.getString("ErrorDetailDialog.findError")); //$NON-NLS-1$
        setMessage(Messages.getString("ErrorDetailDialog.errorExist")); //$NON-NLS-1$
        this.errors = errors;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, true);
        if (!isTestcase) {
            createButton(parent, IDialogConstants.OK_ID, "Continue", false); //$NON-NLS-1$
        }
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        initializeDialogUnits(composite);

        GridData data = new GridData(GridData.FILL_BOTH);
        // size of dialog
        data.heightHint = 300;
        data.widthHint = 500;
        composite.setLayoutData(data);
        createMessageArea(composite);
        createTreeTableViewer(composite);

        return composite;
    }

    /**
     * DOC chuang Comment method "createTreeTableView".
     *
     * @param parent
     */
    private void createTreeTableViewer(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        viewer = new TreeViewer(composite, SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);

        // init tree
        final Tree tree = viewer.getTree();
        tree.setHeaderVisible(true);
        tree.setLinesVisible(true);
        tree.setLayoutData(new GridData(GridData.FILL_BOTH));

        // create tree column
        TreeColumn column = new TreeColumn(tree, SWT.NONE);
        column.setText("Resource"); //$NON-NLS-1$
        column.setWidth(150);

        column = new TreeColumn(tree, SWT.NONE);
        column.setText("Description"); //$NON-NLS-1$
        column.setWidth(300);

        viewer.setContentProvider(new ErrorDetailContentProvider());
        viewer.setLabelProvider(new ErrorDetailLabelProvider());
        viewer.setInput(errors);
        viewer.expandAll();
    }

    /**
     *
     * DOC chuang ErrorDetailDialog class global comment. Detailled comment
     */
    class ErrorDetailContentProvider implements ITreeContentProvider {

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
         */
        @Override
        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof IContainerEntry) {
                return ((IContainerEntry) parentElement).getChildren().toArray();
            }
            return null;
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
         */
        @Override
        public Object getParent(Object element) {
            return null;
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
         */
        @Override
        public boolean hasChildren(Object element) {
            if (element instanceof IContainerEntry) {
                return ((IContainerEntry) element).hasChildren();
            }
            return false;
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
         */
        @Override
        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof List) {
                return ((List) inputElement).toArray();
            }
            return null;
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.viewers.IContentProvider#dispose()
         */
        @Override
        public void dispose() {
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
         * java.lang.Object, java.lang.Object)
         */
        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

        }

    }

    /**
     *
     * DOC chuang ErrorDetailDialog class global comment. Detailled comment
     */
    class ErrorDetailLabelProvider implements ITableLabelProvider {

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
         */
        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            if (columnIndex == 0) {
                if (element instanceof IContainerEntry) {
                    return ((IContainerEntry) element).getImage();
                }
            } else if (columnIndex == 1) {
                if (element instanceof Problem) {
                    return ImageProvider.getImage(EImage.ERROR_SMALL);
                }
            }
            return null;
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
         */
        @Override
        public String getColumnText(Object element, int columnIndex) {
            if (columnIndex == 0) {
                if (element instanceof IContainerEntry) {
                    return ((IContainerEntry) element).getLabel();
                }
            } else if (columnIndex == 1) {
                if (element instanceof Problem) {
                    return ((Problem) element).getDescription();
                }
            }
            return null;
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
         */
        @Override
        public void addListener(ILabelProviderListener listener) {

        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
         */
        @Override
        public void dispose() {

        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
         */
        @Override
        public boolean isLabelProperty(Object element, String property) {
            return false;
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
         */
        @Override
        public void removeListener(ILabelProviderListener listener) {
        }

    }

    /**
     * Getter for isRunTestcase.
     *
     * @return the isRunTestcase
     */
    public boolean isTestcase() {
        return this.isTestcase;
    }

    /**
     * Sets the isRunTestcase.
     *
     * @param isRunTestcase the isRunTestcase to set
     */
    public void setTestcase(boolean isTestcase) {
        this.isTestcase = isTestcase;
    }
}
