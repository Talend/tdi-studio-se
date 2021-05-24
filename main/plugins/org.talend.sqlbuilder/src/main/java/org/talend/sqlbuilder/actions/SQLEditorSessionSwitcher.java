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

import java.util.List;

import org.eclipse.jface.action.ControlContribution;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.talend.repository.model.RepositoryNode;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.dbstructure.DBTreeProvider;
import org.talend.sqlbuilder.editors.MultiPageSqlBuilderEditor;
import org.talend.sqlbuilder.repository.utility.SQLBuilderRepositoryNodeManager;
import org.talend.sqlbuilder.ui.editor.ISQLEditor;

/**
 * This class is responsible for representing repository name as items into combo component. <br/>
 *
 * @author ftang
 *
 */
public class SQLEditorSessionSwitcher extends ControlContribution {

    private ISQLEditor editor;

    private Combo sessionCombo;

    /**
     * SQLEditorSessionSwitcher constructor.
     *
     * @param editor
     */
    public SQLEditorSessionSwitcher(ISQLEditor editor) {

        super("org.talend.sqlbuilder.sessionswitcher"); //$NON-NLS-1$

        this.editor = editor;
    }

    SQLBuilderRepositoryNodeManager nodeManager = new SQLBuilderRepositoryNodeManager();

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.action.ControlContribution#createControl(org.eclipse.swt.widgets.Composite)
     */
    protected Control createControl(Composite parent) {

        sessionCombo = new Combo(parent, SWT.READ_ONLY);
        sessionCombo.setToolTipText(Messages.getString("SQLEditor.Actions.ChooseSession.ToolTip")); //$NON-NLS-1$

        List<String> repositoryNameList = nodeManager.getALLReposotoryNodeNames();
        sessionCombo.setItems(repositoryNameList.toArray(new String[repositoryNameList.size()]));

        sessionCombo.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                String repoName = sessionCombo.getText();
                RepositoryNode node = nodeManager.getRepositoryNodebyName(repoName);
                if (!repoName.equals(DBTreeProvider.BUILT_IN)) {
                    MultiPageSqlBuilderEditor multiPageEditor = editor.getMultiPageEditor();
                    multiPageEditor.setRepositoryNode(node);
                }
                // editor.refresh(true);
            }
        });

        return sessionCombo;
    }

    /**
     * Refreshes selected repository.
     */
    public void refreshSelectedRepository() {
        sessionCombo.setText(editor.getRepositoryName());
    }

}
