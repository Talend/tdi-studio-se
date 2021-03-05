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
package org.talend.sqlbuilder.erdiagram.ui.nodes;

import org.eclipse.draw2d.geometry.Point;

/**
 * DOC qzhang class global comment. Detailled comment <br/>
 *
 * $Id: Relation.java 1 2006-12-25 下午02:57:25 +0000 (ææäº, 29 ä¹æ 2006) yzhang $
 *
 */
public class Relation extends Element {

    private boolean isConnected = false;

    private static final long serialVersionUID = 0L;

    public static final String PROP_SOURCE = "source"; //$NON-NLS-1$

    public static final String PROP_TARGET = "target"; //$NON-NLS-1$

    public static final String PROP_ENDPOINTS = "endPoints"; //$NON-NLS-1$

    private Column source;

    private Column target;

    private Point start;

    private Point end;

    /**
     * DOC qzhang Relation constructor comment.
     */
    public Relation(Column source, Column target) {
        reconnect(source, target);
    }

    /**
     * DOC qzhang Comment method "getSource".
     *
     * @return
     */
    public Column getSource() {
        return this.source;
    }

    /**
     * DOC qzhang Comment method "setSource".
     *
     * @param source
     */
    public void setSource(Column source) {
        this.source = source;
        this.source.addOutputRelation(this);
        fireStructureChange(PROP_SOURCE, source);

    }

    /**
     * DOC qzhang Comment method "getTarget".
     *
     * @return
     */
    public Column getTarget() {
        return this.target;
    }

    /**
     * DOC qzhang Comment method "setTarget".
     *
     * @param target
     */
    public void setTarget(Column target) {
        this.target = target;
        this.target.addInputRelation(this);
        fireStructureChange(PROP_TARGET, target);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.sqlbuider.erdiagram.model.Element#getElementName()
     */
    @Override
    public String getElementName() {
        // TODO Auto-generated method stub
        return null;
    }

    public void disconnect() {
        if (isConnected) {
            source.removeOutputRelation(this);
            target.removeInputRelation(this);
            isConnected = false;
        }
    }

    public void reconnect(Column newSource, Column newTarget) {
        disconnect();
        this.source = newSource;
        this.target = newTarget;
        reconnect();
    }

    public void reconnect() {
        if (!isConnected) {
            source.addOutputRelation(this);
            target.addInputRelation(this);
            isConnected = true;
        }
    }

    public Point getEnd() {
        return this.end;
    }

    public void setEndPoints(Point start, Point end) {
        setStart(start);
        setEnd(end);
    }

    /**
     * DOC qzhang Comment method "setEnd".
     *
     * @param end
     */
    private void setEnd(Point end) {
        this.end = end;
        fireStructureChange(PROP_ENDPOINTS, this.end);
    }

    public Point getStart() {
        return this.start;
    }

    private void setStart(Point start) {
        this.start = start;
        fireStructureChange(PROP_ENDPOINTS, this.start);
    }

}
