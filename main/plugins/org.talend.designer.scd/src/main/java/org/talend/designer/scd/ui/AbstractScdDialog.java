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
package org.talend.designer.scd.ui;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SearchPattern;
import org.eclipse.ui.help.IWorkbenchHelpSystem;
import org.talend.designer.scd.ScdManager;
import org.talend.designer.scd.i18n.Messages;

/**
 * DOC hcw class global comment. Detailled comment
 */
public abstract class AbstractScdDialog extends TrayDialog {

    // protected static final int SECTION_HEIGHT = 125;
    //
    // protected static final int SECTION_WIDTH = 395;

    protected ScdManager scdManager;

    protected Text filterText;

    protected FieldSection unusedFields;

    protected FieldSection type0Fields;

    protected FieldSection type1Fields;

    protected FieldSection sourceKeys;

    protected Type2Section type2Fields;

    protected SurrogateSection surrogateKeys;

    protected Type3Section type3Fields;

    protected Shell shell;

    protected Button okButton;

    /**
     * DOC hcw AbstractScdDialog constructor comment.
     *
     * @param shell
     */
    public AbstractScdDialog(Shell shell) {
        super(shell);
        this.shell = shell;
        setShellStyle(SWT.BORDER | SWT.CLOSE | SWT.MIN | SWT.MAX | SWT.TITLE | SWT.APPLICATION_MODAL | SWT.RESIZE);
    }

    // /**
    // * DOC hcw AbstractScdDialog constructor comment.
    // *
    // * @param parentShell
    // */
    // public AbstractScdDialog(IShellProvider parentShell) {
    // super(parentShell);
    // }

    /**
     * Create contents of the dialog
     *
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        GridLayoutFactory.swtDefaults().applyTo(container);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(container, "org.talend.designer.scd.scdDialog"); //$NON-NLS-1$
        // getShell().addListener(SWT.Close, new Listener() {
        //
        // public void handleEvent(Event event) {
        // showWarningDialog();
        // }
        //
        // });
        createScdContents(container);
        return container;
    }

    @Override
    protected void cancelPressed() {
        setReturnCode(CANCEL);
        showWarningDialog();
    }

    /**
     * Prompt the user for saving before closing the dialog.
     */
    protected void showWarningDialog() {

        // unmodified
        //    boolean isNotSaveSetting = MessageDialog.openQuestion(shell, Messages.getString("UIManager.MessageBox.title"), //$NON-NLS-1$
        //                Messages.getString("UIManager.MessageBox.Content")); //$NON-NLS-1$
        // if (!isNotSaveSetting) {
        // setReturnCode(OK);
        // saveState();
        // }
        // close();

        // fixed 0018156: can not close a dialog of tOracleSCD.
        Display.getCurrent().asyncExec(new Runnable() {

            public void run() {
                boolean isNotSaveSetting = MessageDialog.openQuestion(shell, Messages.getString("UIManager.MessageBox.title"), //$NON-NLS-1$
                        Messages.getString("UIManager.MessageBox.Content")); //$NON-NLS-1$
                if (!isNotSaveSetting) {
                    setReturnCode(OK);
                    saveState();
                } else {
                    setReturnCode(CANCEL);
                }
                close();
            }
        });
    }

    abstract Control createScdContents(Composite container);

    /**
     * DOC hcw Comment method "createFilter".
     *
     * @param container
     * @return
     */
    protected ViewerFilter createFilter(Composite container) {
        Composite composite = new Composite(container, SWT.NONE);
        GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.FILL).applyTo(composite);
        composite.setLayout(new FormLayout());

        final Button filterButton = new Button(composite, SWT.PUSH);
        filterText = new Text(composite, SWT.BORDER);
        filterText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                Shell dialogShell = AbstractScdDialog.this.getShell();
                if (dialogShell != null) {
                    dialogShell.setDefaultButton(filterButton);
                }
            }
        });
        filterText.setFocus();

        FormData formData = new FormData();
        formData.right = new FormAttachment(100);
        formData.top = new FormAttachment(0);
        filterButton.setLayoutData(formData);

        formData = new FormData();
        formData.top = new FormAttachment(filterButton, 0, SWT.CENTER);
        formData.left = new FormAttachment(0);
        formData.right = new FormAttachment(filterButton, 0, SWT.LEFT);
        filterText.setLayoutData(formData);

        filterButton.setText(Messages.getString("AbstractScdDialog.filter")); //$NON-NLS-1$
        filterButton.addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {

            }

            public void widgetSelected(SelectionEvent e) {
                filterText.setFocus();
                applyFilter();
            }

        });

        ViewerFilter filter = new ViewerFilter() {

            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                String pattern = filterText.getText();
                SearchPattern matcher = new SearchPattern();
                matcher.setPattern(pattern);
                return matcher.matches(element.toString());
            }
        };

        return filter;
    }

    /**
     * DOC hcw Comment method "applyFilter".
     */
    protected void applyFilter() {
        unusedFields.getTableViewer().refresh();
    }

    /**
     * Create contents of the button bar
     *
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.getString("AbstractScdDialog.SCDEditor")); //$NON-NLS-1$
    }

    public void addContextHelp(Control control, final String contextId) {
        final IWorkbenchHelpSystem helpSystem = PlatformUI.getWorkbench().getHelpSystem();
        helpSystem.setHelp(control, contextId);
        control.addMouseTrackListener(new MouseTrackAdapter() {

            @Override
            public void mouseEnter(MouseEvent e) {
                if (getTray() != null) {
                    helpSystem.displayHelp(contextId);
                }
            }
        });
    }

    @Override
    protected void handleShellCloseEvent() {
        showWarningDialog();
        // fixed 0018156: can not close a dialog of tOracleSCD.
        // super.handleShellCloseEvent();
    }

    public abstract void saveState();

}
