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
package org.talend.designer.core.utils;

import org.eclipse.gef.commands.Command;
import org.talend.designer.core.ui.editor.nodes.Node;

/**
 * DOC YeXiaowei class global comment. Detailled comment
 */
public interface ISampleCodeFactory {

    /**
     *
     * DOC YeXiaowei Comment method "generateCode".
     *
     * @param node
     * @return
     */
    public Command generateCodeForParameters(final Node node);

}
