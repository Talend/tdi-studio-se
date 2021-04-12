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
package org.talend.designer.core;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.requests.CreationFactory;

/**
 * DOC wchen class global comment. Detailled comment
 */
public interface IExtendedNodeHandler {

    public CreationFactory getCreationFactory(Object template, String editorID);

    public boolean updateComponent(Object dragModel, EditPart targetEditPart, CommandStack cs);
}
