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
package org.talend.designer.runprocess.ui;

import java.util.Arrays;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.talend.commons.ui.runtime.swt.tableviewer.celleditor.DateDialog;
import org.talend.commons.ui.utils.PathUtils;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.ui.context.DefaultCellEditorFactory;
import org.talend.designer.runprocess.i18n.Messages;

/**
 * DOC nrousseau class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class PromptDialog extends Dialog {

    /**
     *
     */
    private static final int Y_POSITION = 100;

    /**
     *
     */
    private static final int X_POSITION = 300;

    /**
     *
     */
    private static final int CELLPADDING = 10;

    /**
     *
     */
    private static final int MINIMUM_WIDTH = 50;

    /**
     *
     */
    private static final int MARGIN_HEIGHT = 4;

    /**
     *
     */
    private static final int MARGIN_WIDTH = 7;

    /**
     *
     */
    private static final int MAX_LABEL_LENGTH_ONE_LINE = 16;

    IContext context;

    /**
     * DOC nrousseau PromptDialog constructor comment.
     *
     * @param parentShell
     */
    protected PromptDialog(Shell parentShell, IContext context) {
        super(parentShell);
        this.context = context;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        final Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new FillLayout());

        final ScrolledComposite sc = new ScrolledComposite(composite, SWT.H_SCROLL | SWT.V_SCROLL);

        final Composite child = new Composite(sc, SWT.NONE);
        child.setLayout(new GridLayout(1, false));

        // Prompt for context values ?
        for (final IContextParameter parameter : context.getContextParameterList()) {
            if (parameter.isPromptNeeded()) {
                if (DefaultCellEditorFactory.isBoolean(parameter.getType())) {
                    final Composite composite2 = new Composite(child, SWT.NONE);
                    final GridLayout gridLayout = new GridLayout(2, false);
                    gridLayout.horizontalSpacing = 5;
                    gridLayout.marginBottom = 0;
                    gridLayout.marginHeight = 0;
                    gridLayout.marginLeft = 0;
                    gridLayout.marginRight = 0;
                    gridLayout.marginTop = 0;
                    gridLayout.marginWidth = 0;

                    composite2.setLayout(gridLayout);
                    composite2.setLayoutData(new GridData(GridData.BEGINNING));
                    Label label = new Label(composite2, SWT.NONE);
                    label.setText(parameter.getPrompt());
                    label.setAlignment(SWT.LEFT);
                    GridData data = new GridData(GridData.FILL_HORIZONTAL);
                    data.minimumWidth = MINIMUM_WIDTH;
                    label.setLayoutData(data);
                    final Button button = new Button(composite2, SWT.CHECK);
                    button.setSelection(Boolean.parseBoolean(parameter.getValue()));
                    button.addSelectionListener(new SelectionAdapter() {

                        /*
                         * (non-Javadoc)
                         *
                         * @see
                         * org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
                         */
                        @Override
                        public void widgetSelected(SelectionEvent e) {
                            parameter.setValue((new Boolean(button.getSelection())).toString());
                        }

                    });
                } else {
                    Label label = new Label(child, SWT.NONE);
                    label.setText(parameter.getPrompt());
                    label.setAlignment(SWT.LEFT);
                    GridData data = new GridData(GridData.FILL_HORIZONTAL);
                    data.minimumWidth = MINIMUM_WIDTH;
                    label.setLayoutData(data);
                    createParameterComposite(child, parameter, label);
                }
            }
        }

        sc.setContent(child);

        // Set the minimum size
        // sc.setMinSize(400, 400);

        // Expand both horizontally and vertically
        sc.setExpandHorizontal(true);
        sc.setExpandVertical(true);
        sc.addControlListener(new ControlAdapter() {

            public void controlResized(ControlEvent e) {
                Rectangle r = sc.getClientArea();
                sc.setMinSize(child.computeSize(r.width, SWT.DEFAULT));
            }
        });
        return composite;
    }

    /**
     * DOC qiang.zhang Comment method "createParameterComposite".
     *
     * @param parent
     * @param parameter
     * @param label
     */
    private void createParameterComposite(final Composite parent, final IContextParameter parameter, Label label) {
        final Composite child = new Composite(parent, SWT.NONE);
        final GridLayout layout = new GridLayout(2, false);
        layout.marginLeft = 0;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        child.setLayout(layout);
        child.setLayoutData(new GridData(GridData.FILL_BOTH));

        if (DefaultCellEditorFactory.isList(parameter.getType())) {
            createListParameterArea(parameter, label, child);
            return;
        }

        final Text text = new Text(child, SWT.SINGLE | SWT.BORDER);
        text.setText(parameter.getValue());

        text.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                parameter.setValue(text.getText());
            }
        });

        String stringTip = ""; //$NON-NLS-1$
        String comment = ""; //$NON-NLS-1$
        if (parameter.getType().equalsIgnoreCase("String") && LanguageManager.getCurrentLanguage() == ECodeLanguage.PERL) { //$NON-NLS-1$
            stringTip = Messages.getString("PromptDialog.stringTip"); //$NON-NLS-1$
        }

        comment = parameter.getComment();

        if (!stringTip.equals("")) { //$NON-NLS-1$
            comment = comment + " " + stringTip; //$NON-NLS-1$
        }
        label.setToolTipText(comment);
        text.setToolTipText(comment);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.minimumWidth = MINIMUM_WIDTH;
        text.setLayoutData(data);
        if (DefaultCellEditorFactory.isDate(parameter.getType())) {
            text.setEditable(false);
            final Button b = new Button(child, SWT.NONE);
            b.setText("..."); //$NON-NLS-1$
            b.addSelectionListener(new SelectionAdapter() {

                /*
                 * (non-Javadoc)
                 *
                 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
                 */
                @Override
                public void widgetSelected(SelectionEvent e) {
                    DateDialog d = new DateDialog(((Button) e.getSource()).getShell());
                    int res = d.open();
                    if (res == Dialog.OK) {
                        text.setText(DefaultCellEditorFactory.getAddQuoteString(d.getTalendDateString()));
                    }
                }

            });
        } else if (DefaultCellEditorFactory.isFile(parameter.getType())) {
            text.setEditable(false);
            final Button b = new Button(child, SWT.NONE);
            b.setText("..."); //$NON-NLS-1$
            b.addSelectionListener(new SelectionAdapter() {

                /*
                 * (non-Javadoc)
                 *
                 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
                 */
                @Override
                public void widgetSelected(SelectionEvent e) {
                    FileDialog d = new FileDialog(((Button) e.getSource()).getShell());
                    if (text.getText() != null) {
                        d.setFileName(PathUtils.getOSPath(DefaultCellEditorFactory.getRemoveQuoteString(text.getText())));
                    }
                    String open = d.open();
                    if (open != null) {
                        text.setText(DefaultCellEditorFactory.getAddQuoteString(PathUtils.getPortablePath(open)));
                    }
                }

            });
        } else if (DefaultCellEditorFactory.isDirectory(parameter.getType())) {
            text.setEditable(false);
            final Button b = new Button(child, SWT.NONE);
            b.setText("..."); //$NON-NLS-1$
            b.addSelectionListener(new SelectionAdapter() {

                /*
                 * (non-Javadoc)
                 *
                 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
                 */
                @Override
                public void widgetSelected(SelectionEvent e) {
                    DirectoryDialog d = new DirectoryDialog(((Button) e.getSource()).getShell());
                    if (text.getText() != null) {
                        d.setFilterPath(PathUtils.getOSPath(DefaultCellEditorFactory.getRemoveQuoteString(text.getText())));
                    }
                    String open = d.open();
                    if (open != null) {
                        open = PathUtils.getPortablePath(open);
                        open += "/"; //$NON-NLS-1$
                        text.setText(DefaultCellEditorFactory.getAddQuoteString(open));
                    }
                }

            });
        } else if (DefaultCellEditorFactory.isPassword(parameter.getType())) {
            text.setEchoChar('*');
        }
    }

    /**
     * DOC bqian Comment method "createListParameterArea".
     *
     * @param parameter
     * @param label
     * @param parent
     */
    private void createListParameterArea(final IContextParameter parameter, Label label, final Composite parent) {

        final CCombo combo = new CCombo(parent, SWT.BORDER);
        combo.setEditable(false);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.minimumWidth = MINIMUM_WIDTH;
        combo.setLayoutData(data);

        String[] valueList = parameter.getValueList();
        if (valueList != null) {
            combo.setItems(valueList);
            int index = Arrays.binarySearch(valueList, parameter.getValue());
            if (index >= 0) {
                combo.select(index);
            } else {
                combo.select(0);
            }
        }
        parameter.setInternalValue(combo.getText());
        combo.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                parameter.setValue(combo.getText());
            }
        });
        if (parameter.getComment() != null) {
            if (!parameter.getComment().equals("")) { //$NON-NLS-1$
                label.setToolTipText(parameter.getComment());
                combo.setToolTipText(parameter.getComment());
            }
        }

    }

    @Override
    protected void setShellStyle(int newShellStyle) {
        newShellStyle = newShellStyle | SWT.RESIZE;
        super.setShellStyle(newShellStyle);
    }

    protected void setSize(Shell shell, Point size) {
        Point centerScreen = new Point(shell.getDisplay().getClientArea().width / 2,
                shell.getDisplay().getClientArea().height / 2);

        Point newPosition = new Point(size.x / 2, size.x / 2);
        newPosition.x = centerScreen.x - newPosition.x;
        newPosition.y = centerScreen.y - newPosition.y;
        shell.setLocation(newPosition);
        shell.setSize(size.x + (size.x / 2), size.y + (size.y / 2));
    }

    @Override
    protected void configureShell(final Shell newShell) {
        super.configureShell(newShell);
        Text t = new Text(newShell, SWT.None);
        int height = t.getLineHeight() + CELLPADDING;
        t.dispose();
        int nbParams = 0;
        for (IContextParameter parameter : context.getContextParameterList()) {
            if (parameter.isPromptNeeded()) {
                nbParams++;
            }
        }
        Point dialogSize = new Point(X_POSITION, Math.min((height * nbParams) + Y_POSITION, 400));
        setSize(newShell, dialogSize);

        newShell.setText(Messages.getString("PromptDialog.title", context.getName())); //$NON-NLS-1$

    }
}
