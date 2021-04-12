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

/**
 * Interface defining the application's command IDs. Key bindings can be defined for specific commands. To associate an
 * action with a command, use IAction.setActionDefinitionId(commandId).
 *
 * @see org.eclipse.jface.action.IAction#setActionDefinitionId(String)
 */
public interface ICommandIds {

    public static final String EXEC_SQL = "org.talend.sqlbuilder.actions.ExecSQLAction"; //$NON-NLS-1$

    /**
     * DOC dev Comment method "unUse".
     */
    public void unUse();
}
