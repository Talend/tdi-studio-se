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
package org.talend.designer.gefabstractmap.manager;

import org.eclipse.gef.GraphicalViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.talend.core.model.process.AbstractExternalNode;
import org.talend.designer.core.model.utils.emf.talendfile.AbstractExternalData;

/**
 * created by Administrator on 2013-1-10 Detailled comment
 *
 */
public abstract class AbstractMapperManager implements ISelectionChangedListener {

    private AbstractExternalNode component;

    private AbstractExternalData externalData;

    private GraphicalViewer graphicalViewer;

    /**
     * DOC Administrator AbstractMapperManager constructor comment.
     */
    public AbstractMapperManager(AbstractExternalNode component, AbstractExternalData externalData) {
        this.component = component;
        this.externalData = externalData;
    }

    public AbstractExternalNode getMapperComponent() {
        return this.component;
    }

    public AbstractExternalData getExternalData() {
        return this.externalData;
    }

    /**
     * Getter for graphicalViewer.
     *
     * @return the graphicalViewer
     */
    public GraphicalViewer getGraphicalViewer() {
        return this.graphicalViewer;
    }

    /**
     * Sets the graphicalViewer.
     *
     * @param graphicalViewer the graphicalViewer to set
     */
    public void setGraphicalViewer(GraphicalViewer graphicalViewer) {
        this.graphicalViewer = graphicalViewer;
    }
}
