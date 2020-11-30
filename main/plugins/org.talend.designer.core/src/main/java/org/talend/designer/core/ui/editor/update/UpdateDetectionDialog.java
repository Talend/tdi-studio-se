// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.ui.editor.update;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.talend.core.model.context.JobContext;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.update.EUpdateItemType;
import org.talend.core.model.update.EUpdateResult;
import org.talend.core.model.update.UpdateResult;
import org.talend.core.model.update.UpdatesConstants;
import org.talend.designer.core.i18n.Messages;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class UpdateDetectionDialog extends SelectionDialog {

    private static final String ITEMS = Messages.getString("UpdateDetectionDialog.Items"); //$NON-NLS-1$

    private static final String OPERATIONS = Messages.getString("UpdateDetectionDialog.Operations"); //$NON-NLS-1$

    private static final String REMARKS = Messages.getString("UpdateDetectionDialog.Remarks"); //$NON-NLS-1$

    private static final String DEFAULT_TITLE = Messages.getString("UpdateDetectionDialog.Title"); //$NON-NLS-1$

    private static final String DEFAULT_MESSAGE = Messages.getString("UpdateDetectionDialog.Messages"); //$NON-NLS-1$

    private static final String WARNING_MESSAGE = Messages.getString("UpdateDetectionDialog.WarningMessage"); //$NON-NLS-1$

    private static final String JOBLET_MESSAGE = Messages.getString("UpdateDetectionDialog.jobletMessage"); //$NON-NLS-1$

    private static final String READ_ONLY_JOB_WARNING_MESSAGE = Messages
            .getString("ProcessUpdateManager.ReadOnlyProcessUpdateWarningMessages"); //$NON-NLS-1$

    // sizing constants
    private static final int SIZING_SELECTION_WIDGET_HEIGHT = 400;

    private static final int SIZING_SELECTION_WIDGET_WIDTH = 550;

    private static final int SIZING_COLUMN_WIDTH = 25;

    private List<UpdateResult> inputElement;

    private List<UpdateResult> selectedElement;

    private CheckboxTreeViewer viewer;

    private UpdateViewerHelper helper;

    private Button selectButton;

    private Label messLabel;

    private Label imageLabe;

    private boolean canCancel = true;

    private boolean canDeselect = true;

    private boolean isJobReadOnly = false;

    private boolean onlySimpleShow = false;

    /**
     * ggu UpdateCheckDialog constructor comment.
     *
     * @param parentShell
     */
    public UpdateDetectionDialog(Shell parentShell, List<UpdateResult> input) {
        this(parentShell, input, false);
        setTitle(DEFAULT_TITLE);
        setMessage(DEFAULT_MESSAGE);
    }

    public UpdateDetectionDialog(Shell parentShell, List<UpdateResult> input, boolean onlySimpleShow) {
        this(parentShell, input, null, onlySimpleShow);
        setTitle(DEFAULT_TITLE);
    }

    public UpdateDetectionDialog(Shell parentShell, List<UpdateResult> input, String message, boolean onlySimpleShow) {
        super(parentShell);
        Assert.isNotNull(input);
        this.onlySimpleShow = onlySimpleShow;
        this.inputElement = input;
        setHelpAvailable(false);
        setTitle(UpdatesConstants.EMPTY);
        setShellStyle(SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL | getDefaultOrientation());
        removeDuplication();
        if (message != null) {
            setMessage(message);
        } else {
            setMessage(UpdatesConstants.EMPTY);
        }
        helper = new UpdateViewerHelper(this);
        checkInitialSelections();
    }

    public boolean isOnlySimpleShow() {
        return this.onlySimpleShow;
    }

    /**
     *
     * ggu Comment method "removeDuplication".
     *
     * for context mode
     */
    private void removeDuplication() {
        // context mode added
        List<UpdateResult> contextResult = new ArrayList<UpdateResult>();
        for (UpdateResult result : getInputElements()) {
            if (((result.getUpdateType() == EUpdateItemType.CONTEXT && result.getContextModeConnectionItem() != null)
                    || result.getUpdateType() == EUpdateItemType.CONTEXT_GROUP) && result.getResultType() == EUpdateResult.ADD) {
                contextResult.add(result);
            }
        }

        // filter
        List<ConnectionItem> connItems = new ArrayList<ConnectionItem>();
        List<UpdateResult> duplicatedResult = new ArrayList<UpdateResult>();
        Iterator<UpdateResult> iterator = contextResult.iterator();
        List tempItems = new ArrayList();
        List<String> contextGroups = new ArrayList<String>();
        while (iterator.hasNext()) {
            Map<Object, ConnectionItem> jobAndContext = new HashMap<Object, ConnectionItem>();
            UpdateResult result = iterator.next();
            if (result.getUpdateType() == EUpdateItemType.CONTEXT) {
                ConnectionItem item = result.getContextModeConnectionItem();
                jobAndContext.put(result.getJob(), item);
                if (tempItems.contains(jobAndContext)) { // duplicate
                    duplicatedResult.add(result);
                } else {
                    tempItems.add(item);
                }
            } else if (result.getUpdateType() == EUpdateItemType.CONTEXT_GROUP) {
                String key = result.getJobInfor();
                if (result.getUpdateObject() instanceof JobContext) {
                    key = key + ((JobContext) result.getUpdateObject()).getName();
                }
                if (contextGroups.contains(key)) {
                    duplicatedResult.add(result);
                } else {
                    contextGroups.add(key);
                }
            }

        }
        // remove
        getInputElements().removeAll(duplicatedResult);
    }

    protected UpdateViewerHelper getViewerHelper() {
        return this.helper;
    }

    public List<UpdateResult> getInputElements() {
        return this.inputElement;
    }

    public CheckboxTreeViewer getViewer() {
        return this.viewer;
    }

    @Override
    protected boolean canHandleShellCloseEvent() {
        return false;
    }

    private void checkInitialSelections() {
        for (UpdateResult result : getInputElements()) {
            if (!result.isFromItem()) {
                result.setChecked(true);
            }
            // result.setChecked(true);
            if (result.isReadOnlyProcess()) {
                result.setChecked(false);
                this.isJobReadOnly = true;
            }
            switch (result.getResultType()) {
            case RENAME:
                this.canCancel = false;
                this.canDeselect = false;
            case RELOAD:
            case PATH_UPDATE:
            case JOBLET_UPDATE:
                this.canCancel = false;
                this.canDeselect = true;
                return;
            default:
                this.canCancel = true;
                this.canDeselect = true;
            }
        }

    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        // Ok
        if (!isOnlySimpleShow()) {
            createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        }
        if (canCancel || isOnlySimpleShow()) {
            createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
        }
    }

    public void setSelectButtonLabel(String label) {
        if (selectButton != null && label != null) {
            selectButton.setText(label);
        }
    }

    private Composite createBottomButtonArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING * 4);
        composite.setLayout(gridLayout);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END));

        // expand
        Button expandButton = createButton(composite, 99, Messages.getString("UpdateDetectionDialog.Expand"), false); //$NON-NLS-1$
        expandButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (getViewer() != null) {
                    getViewer().expandAll();
                }
            }
        });
        // collapse
        Button collapseButton = createButton(composite, 98, Messages.getString("UpdateDetectionDialog.Collapse"), false); //$NON-NLS-1$
        collapseButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (getViewer() != null) {
                    getViewer().collapseAll();
                }
            }
        });
        if (!isOnlySimpleShow() && canDeselect) {
            // "select all" button
            // selectButton = createButton(composite, IDialogConstants.SELECT_ALL_ID,
            // WorkbenchMessages.SelectionDialog_selectLabel,
            // false);
            selectButton = createButton(composite, IDialogConstants.SELECT_ALL_ID,
                    Messages.getString("WorkbenchMessages.SelectionDialog_selectLabel"), false); //$NON-NLS-1$
            // init label;
            if (getViewerHelper() != null) {
                getViewerHelper().refreshSelectButton();
            }

            selectButton.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (getViewerHelper() != null) {
                        boolean state = false;

                        // if (!isJobReadOnly &&
                        // WorkbenchMessages.SelectionDialog_selectLabel.equals(selectButton.getText())) {
                        if (!isJobReadOnly && Messages.getString("WorkbenchMessages.SelectionDialog_selectLabel").equals( //$NON-NLS-1$
                                selectButton.getText())) {
                            state = true;
                        }
                        getViewerHelper().selectAll(state);
                    }
                }
            });
        }
        return composite;
    }

    @Override
    protected Control createDialogArea(Composite parent) {

        Composite composite = (Composite) super.createDialogArea(parent);
        initializeDialogUnits(composite);

        GridData data = new GridData(GridData.FILL_BOTH);
        data.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
        data.widthHint = SIZING_SELECTION_WIDGET_WIDTH;
        composite.setLayoutData(data);

        createHeadMessage(composite);
        createTreeTableView(composite);
        createBottomButtonArea(composite);

        if (getViewerHelper() != null) {
            getViewerHelper().initViewerState();
        }
        return composite;
    }

    @Override
    protected Label createMessageArea(Composite composite) {
        Label label = new Label(composite, SWT.WRAP);
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.verticalAlignment = GridData.VERTICAL_ALIGN_CENTER;
        label.setLayoutData(gridData);
        label.setFont(composite.getFont());
        return label;
    }

    private Composite createHeadMessage(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        if (!isOnlySimpleShow()) {
            imageLabe = new Label(composite, SWT.NONE);
            imageLabe.setImage(Display.getDefault().getSystemImage(SWT.ICON_WARNING));
            // imageLabe.setImage(ImageProvider.getImage(EImage.WARNING_ICON));
        }
        messLabel = createMessageArea(composite);

        Label label = new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR | SWT.SHADOW_OUT);
        label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        return composite;
    }

    /**
     * create table view.
     */
    private Composite createTreeTableView(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        int style = SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.VIRTUAL;
        if (!isOnlySimpleShow()) { // display check button.
            style = SWT.CHECK | style;
        }
        viewer = new CheckboxTreeViewer(new Tree(composite, style));
        final Tree tree = viewer.getTree();
        tree.setHeaderVisible(true);
        tree.setLinesVisible(true);
        tree.setLayoutData(new GridData(GridData.FILL_BOTH));
        addViewerListener();
        createColumns(tree);     
        viewer.setUseHashlookup(true);
        viewer.setContentProvider(new UpdateContentProvider());
        viewer.setLabelProvider(new UpdateLabelProvider());
        viewer.setSorter(new UpdateViewerSorter());
        viewer.setCheckStateProvider(new UpdateViewerCheckStateProvider());
        viewer.setInput(getInputElements());

        // viewer.setColumnProperties(new String[] { ITEMS, OPERATIONS, REMARKS });
        // viewer.setCellEditors(new CellEditor[] { null, null, null });
        // viewer.setCellModifier(new UpdateCellModifier());

        return composite;
    }

    /**
     * create table columns.
     */
    private void createColumns(final Tree tree) {
        TreeColumn column = new TreeColumn(tree, SWT.NONE);
        column.setText(ITEMS);
        column.setWidth(SIZING_COLUMN_WIDTH * 10);
        tree.setSortColumn(column);

        if (isOnlySimpleShow()) {
            column.setWidth(SIZING_COLUMN_WIDTH * 20);
        } else {
            column = new TreeColumn(tree, SWT.NONE);
            column.setText(OPERATIONS);
            column.setWidth(SIZING_COLUMN_WIDTH * 7);

            column = new TreeColumn(tree, SWT.NONE);
            column.setText(REMARKS);
            column.setWidth(SIZING_COLUMN_WIDTH * 6);
        }
    }

    private void addViewerListener() {
        viewer.addCheckStateListener(new ICheckStateListener() {

            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                if (readOnlyCheck(event)) {
                    updateReadOnlyJobWarnMessage();
                    return;
                }

                if (getViewerHelper() != null) {
                    getViewerHelper().updateCheckedState(event.getElement(), event.getChecked());
                    getViewerHelper().refreshDialogStatus();
                }
                updateParentState(event);
            }

            /**
             * Checks if current job is read-only mode or not.
             *
             * @param event
             */
            private boolean readOnlyCheck(CheckStateChangedEvent event) {
                Job currentJob = null;
                if (event.getElement() instanceof Job) {
                    currentJob = (Job) event.getElement();
                } else if (event.getElement() instanceof Category) {
                    currentJob = ((Category) event.getElement()).getParent();
                } else if (event.getElement() instanceof Item) {
                    currentJob = ((Item) event.getElement()).getParent().getParent();
                }

                if (currentJob == null) {
                    return false;
                }

                if (currentJob.isReadOnlyProcess()) {
                    event.getCheckable().setChecked(event.getElement(), false);
                    return true;
                }
                return false;
            }
            
            private void updateParentState(CheckStateChangedEvent event) {
                Job currentJob = null;
                if (event.getElement() instanceof Item) {
                    Item item = (Item)event.getElement();
                    item.setChecked(event.getChecked());
                    currentJob = item.getParent().getParent();
                } else if (event.getElement() instanceof Category) {
                    Category category = (Category)event.getElement();
                    for (Item item : category.getItems()) {
                        item.setChecked(event.getChecked());;
                    }
                    currentJob = category.getParent();
                } else if (event.getElement() instanceof Job) {
                    currentJob = (Job)event.getElement();
                    for (Category category: currentJob.getCategories()) {
                        for (Item item : category.getItems()) {
                            item.setChecked(event.getChecked());;
                        }
                    }
                }
                if (currentJob != null) {
                    viewer.refresh(currentJob);
                }
            }
        });
    }

    @Override
    protected void okPressed() {
        selectedElement = getCheckedElements();
        Tree tree = viewer.getTree();
        if (tree.isDisposed()) {
            return;
        }
        setResult(getInputElements());
        super.okPressed();

    }

    public void updateWarnMessage() {
        if (imageLabe != null && !imageLabe.isDisposed()) {
            imageLabe.setVisible(true);
        }
        if (messLabel != null && !messLabel.isDisposed()) {
            if (isJobletResult()) {
                messLabel.setText("\n" + WARNING_MESSAGE + "\n" + JOBLET_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                messLabel.setText("\n" + WARNING_MESSAGE); //$NON-NLS-1$
            }
        }
    }

    public void updateNomarlMessage() {
        if (imageLabe != null && !imageLabe.isDisposed()) {
            imageLabe.setVisible(false);
        }
        if (messLabel != null && !messLabel.isDisposed()) {
            if (isJobletResult()) {
                if (isOnlySimpleShow()) {
                    messLabel.setText(Messages.getString("UpdateDetectionDialog.ShowDependenciesMess") + "\n" + JOBLET_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
                } else {
                    messLabel.setText(DEFAULT_MESSAGE + "\n" + JOBLET_MESSAGE); //$NON-NLS-1$
                }
            } else {
                if (isOnlySimpleShow()) {
                    messLabel.setText(Messages.getString("UpdateDetectionDialog.ShowDependenciesMess")); //$NON-NLS-1$
                } else {
                    messLabel.setText(DEFAULT_MESSAGE);
                }
            }

        }
    }

    public void updateReadOnlyJobWarnMessage() {
        if (imageLabe != null && !imageLabe.isDisposed()) {
            imageLabe.setVisible(true);
        }
        if (messLabel != null && !messLabel.isDisposed()) {
            if (isJobletResult()) {
                messLabel.setText(READ_ONLY_JOB_WARNING_MESSAGE + "\n" + JOBLET_MESSAGE); //$NON-NLS-1$
            } else {
                messLabel.setText(READ_ONLY_JOB_WARNING_MESSAGE);
            }
        }
        if (selectButton != null && !selectButton.isDisposed()) {
            selectButton.setVisible(false);
        }
    }

    private boolean isJobletResult() {
        if (getInputElements() != null && getInputElements().size() > 0) {
            if (getInputElements().get(0).getRemark() != null
                    && getInputElements().get(0).getRemark().startsWith(UpdatesConstants.JOBLET + UpdatesConstants.COLON)) {
                return true;
            }
        }
        return false;
    }

    private List<UpdateResult> getCheckedElements() {
        Object[] objs = getViewer().getCheckedElements();
        List<UpdateResult> results = new ArrayList<UpdateResult>();
        if (objs != null) {
            for (Object obj : objs) {
                if (obj instanceof UpdateResult) {
                    results.add((UpdateResult) obj);
                } else if (obj instanceof Item) {
                    results.add(((Item) obj).getResultObject());
                }
            }
        }
        return results;
    }

    public List<UpdateResult> getSelectedElements() {
        return selectedElement;
    }

    public List<UpdateResult> getBuiltInElements() {
        List<UpdateResult> builtIn = new ArrayList<>();
        for(UpdateResult result : getInputElements()){
            if (!result.isChecked()
                    && (result.getResultType() == EUpdateResult.UPDATE || result.getResultType() == EUpdateResult.DELETE)) {
                builtIn.add(result);
            }
        }
        return builtIn;
    }
}

class UpdateViewerCheckStateProvider implements ICheckStateProvider{

    @Override
    public boolean isChecked(Object element) {
        if (element instanceof Job) {
            Job job = (Job) element;
            for (Category category : job.getCategories()) {
                for (Item item : category.getItems()) {
                    if (item.isChecked()) {
                        return true;
                    }
                }
            }
            return false;
        } else if (element instanceof Category) {
            Category category = (Category)element;
            for (Item item : category.getItems()) {
                if (item.isChecked()) {
                    return true;
                }
            }
        } else if (element instanceof Item) {
            return ((Item)element).isChecked();
        }
        return false;
    }

    @Override
    public boolean isGrayed(Object element) {
        int selectCount = 0;
        int unSelectCount = 0;
        if (element instanceof Job) {
            Job job = (Job) element;
            for (Category category : job.getCategories()) {
                for (Item item : category.getItems()) {
                    if (item.isChecked()) {
                        selectCount ++;
                    }else {
                        unSelectCount ++;
                    }
                }
            }
        } 
        if (element instanceof Category) {
            Category category = (Category)element;
            for (Item item : category.getItems()) {
                if (item.isChecked()) {
                    selectCount ++;
                }else {
                    unSelectCount ++;
                }
            }
        }
        if (selectCount > 0 && unSelectCount > 0) {
            return true;
        }
        return false;
    }
}
