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
package org.talend.designer.filemultischemas.ui.provider;

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TreeViewer;

/**
 * cLi class global comment. Detailled comment
 */
public abstract class SchemaDetailsCellModifier implements ICellModifier {

    private TreeViewer schemaDetailsViewer;

    public SchemaDetailsCellModifier(TreeViewer schemaDetailsViewer) {
        super();
        this.schemaDetailsViewer = schemaDetailsViewer;
    }

    protected TreeViewer getSchemaDetailsViewer() {
        return this.schemaDetailsViewer;
    }

}
