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
package org.talend.repository.ui.wizards.newproject.copyfromeclipse;

import java.util.ArrayList;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dialogs.ExportPage;
import org.eclipse.ui.internal.dialogs.WizardActivityFilter;
import org.eclipse.ui.internal.dialogs.WizardCollectionElement;
import org.eclipse.ui.internal.dialogs.WizardContentProvider;
import org.eclipse.ui.internal.dialogs.WizardPatternFilter;
import org.eclipse.ui.model.AdaptableList;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.wizards.IWizardCategory;
import org.eclipse.ui.wizards.IWizardDescriptor;
import org.talend.repository.i18n.Messages;

/**
 * DOC guanglong.du class global comment. Detailled comment
 */
public class TalendExportPage extends ExportPage {

    private static final String STORE_SELECTED_EXPORT_WIZARD_ID = DIALOG_SETTING_SECTION_NAME + "STORE_SELECTED_EXPORT_WIZARD_ID"; //$NON-NLS-1$

    private static final String STORE_EXPANDED_EXPORT_CATEGORIES = DIALOG_SETTING_SECTION_NAME
            + "STORE_EXPANDED_EXPORT_CATEGORIES"; //$NON-NLS-1$

    TalendCategorizedWizardSelectionTree exportTree;

    /**
     * DOC guanglong.du TalendExportPage constructor comment.
     *
     * @param aWorkbench
     * @param currentSelection
     */
    public TalendExportPage(IWorkbench aWorkbench, IStructuredSelection currentSelection) {
        super(aWorkbench, currentSelection);
        // TODO Auto-generated constructor stub
    }

    protected Composite createTreeViewer(Composite parent) {
        IWizardCategory root = WorkbenchPlugin.getDefault().getExportWizardRegistry().getRootCategory();
        // exportTree = new TalendCategorizedWizardSelectionTree(root,
        // WorkbenchMessages.ExportWizard_selectDestination);
        exportTree = new TalendCategorizedWizardSelectionTree(root,
                Messages.getString("WorkbenchMessages.ExportWizard_selectDestination")); //$NON-NLS-1$
        Composite exportComp = exportTree.createControl(parent);
        exportTree.getViewer().addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                listSelectionChanged(event.getSelection());
            }
        });
        exportTree.getViewer().addDoubleClickListener(new IDoubleClickListener() {

            public void doubleClick(DoubleClickEvent event) {
                treeDoubleClicked(event);
            }
        });
        setTreeViewer(exportTree.getViewer());
        return exportComp;
    }

    public void saveWidgetValues() {
        storeExpandedCategories(STORE_EXPANDED_EXPORT_CATEGORIES, exportTree.getViewer());
        storeSelectedCategoryAndWizard(STORE_SELECTED_EXPORT_WIZARD_ID, exportTree.getViewer());
    }

    protected void updateMessage() {
        // setMessage(WorkbenchMessages.ImportExportPage_chooseExportDestination);
        setMessage(Messages.getString("WorkbenchMessages.ImportExportPage_chooseExportDestination")); //$NON-NLS-1$
        super.updateMessage();
    }

    protected void restoreWidgetValues() {
        IWizardCategory exportRoot = WorkbenchPlugin.getDefault().getExportWizardRegistry().getRootCategory();
        expandPreviouslyExpandedCategories(STORE_EXPANDED_EXPORT_CATEGORIES, exportRoot, exportTree.getViewer());
        selectPreviouslySelected(STORE_SELECTED_EXPORT_WIZARD_ID, exportRoot, exportTree.getViewer());

    }

    /**
     * Class to create a control that shows a categorized tree of wizard types.
     */
    class TalendCategorizedWizardSelectionTree extends CategorizedWizardSelectionTree {

        private final static int SIZING_LISTS_HEIGHT = 200;

        private IWizardCategory wizardCategories;

        private String message;

        private TreeViewer viewer;

        /**
         * Constructor for CategorizedWizardSelectionTree
         *
         * @param categories root wizard category for the wizard type
         * @param msg message describing what the user should choose from the tree.
         */
        protected TalendCategorizedWizardSelectionTree(IWizardCategory categories, String msg) {
            super(categories, msg);
            this.wizardCategories = categories;
            this.message = msg;
        }

        /**
         * Create the tree viewer and a message describing what the user should choose from the tree.
         *
         * @param parent Composite on which the tree viewer is to be created
         * @return Comoposite with all widgets
         */
        protected Composite createControl(Composite parent) {
            Font font = parent.getFont();

            // create composite for page.
            Composite outerContainer = new Composite(parent, SWT.NONE);
            outerContainer.setLayout(new GridLayout());
            outerContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
            outerContainer.setFont(font);

            Label messageLabel = new Label(outerContainer, SWT.NONE);
            if (message != null) {
                messageLabel.setText(message);
            }
            messageLabel.setFont(font);

            createFilteredTree(outerContainer);
            layoutTopControl(viewer.getControl());

            return outerContainer;
        }

        /**
         * Create the categorized tree viewer.
         *
         * @param parent
         */
        private void createFilteredTree(Composite parent) {
            // Create a FilteredTree for the categories and wizards
            FilteredTree filteredTree = new FilteredTree(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER,
                    new WizardPatternFilter(), true);
            viewer = filteredTree.getViewer();
            filteredTree.setFont(parent.getFont());

            viewer.setContentProvider(new TalendWizardContentProvider());
            viewer.setLabelProvider(new WorkbenchLabelProvider());
            // viewer.setComparator(DataTransferWizardCollectionComparator.INSTANCE);

            ArrayList inputArray = new ArrayList();
            boolean expandTop = false;

            if (wizardCategories != null) {
                if (wizardCategories.getParent() == null) {
                    IWizardCategory[] children = wizardCategories.getCategories();
                    for (int i = 0; i < children.length; i++) {
                        inputArray.add(children[i]);
                    }
                } else {
                    expandTop = true;
                    inputArray.add(wizardCategories);
                }
            }

            // ensure the category is expanded. If there is a remembered expansion it will be set later.
            if (expandTop) {
                viewer.setAutoExpandLevel(2);
            }

            AdaptableList input = new AdaptableList(inputArray);

            // filter wizard list according to capabilities that are enabled
            viewer.addFilter(new WizardActivityFilter());

            viewer.setInput(input);
        }

        /**
         *
         * @return the categorized tree viewer
         */
        protected TreeViewer getViewer() {
            return viewer;
        }

        /**
         * Layout for the given control.
         *
         * @param control
         */
        private void layoutTopControl(Control control) {
            GridData data = new GridData(GridData.FILL_BOTH);

            // int availableRows = DialogUtil.availableRows(control.getParent());
            int availableRows = TalendExportPage.availableRows(control.getParent());

            // Only give a height hint if the dialog is going to be too small
            if (availableRows > 50) {
                data.heightHint = SIZING_LISTS_HEIGHT;
            } else {
                data.heightHint = availableRows * 3;
            }

            control.setLayoutData(data);
        }

    }

    /**
     * Return the number of rows available in the current display using the current font.
     *
     * @param parent The Composite whose Font will be queried.
     * @return int The result of the display size divided by the font size.
     */
    public static int availableRows(Composite parent) {

        int fontHeight = (parent.getFont().getFontData())[0].getHeight();
        int displayHeight = parent.getDisplay().getClientArea().height;

        return displayHeight / fontHeight;
    }

    /**
     *
     * DOC guanglong.du TalendExportPage class global comment. Detailled comment
     */
    private class TalendWizardContentProvider extends WizardContentProvider {

        public Object[] getChildren(Object parentElement) {

            if (parentElement instanceof WizardCollectionElement) {
                ArrayList list = new ArrayList();
                WizardCollectionElement element = (WizardCollectionElement) parentElement;

                Object[] childCollections = element.getChildren();
                for (int i = 0; i < childCollections.length; i++) {
                    handleChild(childCollections[i], list);
                }

                Object[] childWizards = element.getWizards();
                for (int i = 0; i < childWizards.length; i++) {
                    handleChild(childWizards[i], list);
                }

                // flatten lists with only one category
                if (list.size() == 1 && list.get(0) instanceof WizardCollectionElement) {
                    return getChildren(list.get(0));
                }

                return list.toArray();
            } else if (parentElement instanceof AdaptableList) {
                AdaptableList aList = (AdaptableList) parentElement;
                Object[] children = aList.getChildren();
                ArrayList list = new ArrayList(children.length);
                for (int i = 0; i < children.length; i++) {
                    handleChild(children[i], list);
                }
                // if there is only one category, return it's children directly (flatten list)
                if (list.size() == 1 && list.get(0) instanceof WizardCollectionElement) {
                    return getChildren(list.get(0));
                }

                return list.toArray();
            } else {
                return new Object[0];
            }

        }

        private void handleChild(Object element, ArrayList list) {
            if (element instanceof WizardCollectionElement) {
                for (int i = 0; i < ((WizardCollectionElement) element).getWizards().length; i++) {
                    IWizardDescriptor wizard = ((WizardCollectionElement) element).getWizards()[i];
                    if (wizard.getId().equals("org.eclipse.ui.wizards.export.ZipFile")) {
                        ((WizardCollectionElement) element).remove(wizard);
                    }
                }

                if (hasChildren(element)) {
                    list.add(element);
                }
            } else {
                list.add(element);
            }
        }

    }
}
