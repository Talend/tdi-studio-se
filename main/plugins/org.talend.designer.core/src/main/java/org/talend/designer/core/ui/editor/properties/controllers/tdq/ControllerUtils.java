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
package org.talend.designer.core.ui.editor.properties.controllers.tdq;

import org.talend.core.model.process.Element;
import org.talend.designer.core.ui.editor.nodes.Node;

/**
 * The utils class for Controller
 */
public class ControllerUtils {

    /**
     * judge the controller wether from tGenKeyHadoop
     */
    public static boolean isFromTGenKey(Element element) {
        if (element instanceof Node) {
            Node source = (Node) element;
            if (source.getComponent().getName().equals("tGenKeyHadoop")) { //$NON-NLS-1$
                return true;
            }
        }
        return false;
    }
}
