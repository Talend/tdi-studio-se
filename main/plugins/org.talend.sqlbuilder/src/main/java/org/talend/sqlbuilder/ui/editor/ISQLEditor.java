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
package org.talend.sqlbuilder.ui.editor;

import org.eclipse.swt.widgets.Shell;
import org.talend.core.model.metadata.builder.connection.Query;
import org.talend.repository.model.RepositoryNode;
import org.talend.sqlbuilder.editors.MultiPageSqlBuilderEditor;
import org.talend.sqlbuilder.ui.ISQLBuilderDialog;

/**
 * This interface is responsible for defining methods for SQLBuilderEditorComposite class.
 *
 * @author ftang
 *
 */
public interface ISQLEditor {

    /**
     * Gets Shell.
     *
     * @return an instance of Shell.
     */
    Shell getShell();

    /**
     * Checks if sql result length is limited.
     */
    boolean getIfLimit();

    /**
     * Gets the allowed max result length .
     *
     * @return
     */
    String getMaxResult();

    /**
     * Gets repository node.
     *
     * @return an instance of RepositoryNode
     */
    RepositoryNode getRepositoryNode();

    /**
     *
     * Gets sql query.
     *
     * @return
     */
    String getSQLToBeExecuted();

    /**
     * Save current editor's text as a file.
     */
    void doSaveAs();

    /**
     * Clear current editor's text.
     */
    void clearText();

    /**
     *
     * Refresh actions availability on the toolbar.
     */
    void refresh(boolean b);

    /**
     * Gets repository name.
     *
     * @return a string
     */
    String getRepositoryName();

    /**
     * Gets the flag for indicating current editor whether is the default one.
     */
    boolean getDefaultEditor();

    /**
     * Sets the content of editor.
     */
    void setEditorContent(String string);

    /**
     * Saves current editor's sql text into dbstructure composite.
     */
    Query doSaveSQL(Query query2, boolean as);

    /**
     * Sets repository node.
     *
     * @param node
     */
    void setRepositoryNode(RepositoryNode node);

    MultiPageSqlBuilderEditor getMultiPageEditor();

    /**
     * Getter for dialog.
     *
     * @return the dialog
     */
    public ISQLBuilderDialog getDialog();

}
