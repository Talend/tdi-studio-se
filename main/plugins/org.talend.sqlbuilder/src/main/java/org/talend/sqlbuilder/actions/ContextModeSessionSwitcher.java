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

package org.talend.sqlbuilder.actions;

import org.eclipse.jface.action.ControlContribution;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.talend.core.model.metadata.builder.connection.Query;
import org.talend.core.model.metadata.builder.connection.impl.QueryImpl;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.editors.MultiPageSqlBuilderEditor;
import org.talend.sqlbuilder.repository.utility.SQLBuilderRepositoryNodeManager;
import org.talend.sqlbuilder.ui.editor.ISQLEditor;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class ContextModeSessionSwitcher extends ControlContribution {

    private ISQLEditor editor;

    private ContextModeAction contextmodeaction;

    private Button button;

    public Button getButton() {
        return this.button;
    }

    public void setButton(Button button) {
        this.button = button;
    }

    public ISQLEditor getEditor() {
        return this.editor;
    }

    public ContextModeAction getContextmodeaction() {
        return this.contextmodeaction;
    }

    public ContextModeSessionSwitcher(ISQLEditor editor, ContextModeAction contextmodeaction) {
        super("org.talend.sqlbuilder.ContextModeSessionSwitcher"); //$NON-NLS-1$
        this.editor = editor;
        this.contextmodeaction = contextmodeaction;
    }

    SQLBuilderRepositoryNodeManager nodeManager = new SQLBuilderRepositoryNodeManager();

    @Override
    protected Control createControl(Composite parent) {
        button = new Button(parent, SWT.CHECK);
        button.setText(Messages.getString("ContextModeSessionSwitcher.buttonText")); //$NON-NLS-1$
        final Query query = (QueryImpl) contextmodeaction.getQuery();
        // judge from repository node
        if (query != null) {
            button.setSelection(query.isContextMode());
            // judge from designer built-in
        } else {
            button.setSelection(contextmodeaction.getParameters().getIfContextButtonCheckedFromBuiltIn());
        }

        button.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                contextmodeaction.setChecked(button.getSelection());
                contextmodeaction.getParameters().setIfContextButtonCheckedFromBuiltIn(button.getSelection());
                // judge dirty
                MultiPageSqlBuilderEditor m = editor.getMultiPageEditor();
                m.updateEditorTitle(null);

            }

        });

        return button;
    }
}
