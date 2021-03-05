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
package org.talend.designer.core.test.util;

import org.talend.core.model.components.IComponent;
import org.talend.core.model.process.IProcess2;
import org.talend.designer.core.ui.editor.nodes.Node;

/**
 * created by nrousseau on Jun 27, 2012 Detailled comment
 *
 */
public final class NodeTestCreator {

    public static Node createSimpleInputNode(IProcess2 process) {
        return new Node(new SimpleInputComponent(), process);
    }

    public static Node createSimpleOutputNode(IProcess2 process) {
        return new Node(new SimpleOutputComponent(), process);
    }

    public static Node createSimpleOutputNodeNoPropagate(IProcess2 process) {
        IComponent component = new SimpleOutputComponent() {

            @Override
            public boolean isSchemaAutoPropagated() {
                return false;
            }

        };
        return new Node(component, process);
    }

}
