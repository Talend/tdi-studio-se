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
package org.talend.designer.xmlmap.figures.manager;

import java.util.ArrayList;
import java.util.List;

import org.talend.designer.gefabstractmap.figures.manager.FiguresManager;
import org.talend.designer.gefabstractmap.model.abstractmap.MapperTable;
import org.talend.designer.xmlmap.editor.XmlMapGraphicViewer;
import org.talend.designer.xmlmap.model.emf.xmlmap.InputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlMapData;
import org.talend.designer.xmlmap.parts.InputXmlTreeEditPart;
import org.talend.designer.xmlmap.parts.OutputXmlTreeEditPart;
import org.talend.designer.xmlmap.parts.XmlMapDataEditPart;

/**
 * wchen class global comment. Detailled comment
 */
public class XmlMapFiguresManager extends FiguresManager {

    public XmlMapFiguresManager(XmlMapGraphicViewer graphicViewer) {
        super(graphicViewer);

    }

    @Override
    public XmlMapDataEditPart getRootEditPart() {
        return (XmlMapDataEditPart) super.getRootEditPart();
    }

    @Override
    public InputXmlTreeEditPart getCurrentSelectedInputTable() {
        return (InputXmlTreeEditPart) super.getCurrentSelectedInputTable();
    }

    @Override
    public OutputXmlTreeEditPart getCurrentSelectedOutputTable() {
        return (OutputXmlTreeEditPart) super.getCurrentSelectedOutputTable();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.newabstractmap.figures.manager.FiguresManager#getInputTables()
     */
    @Override
    protected List<MapperTable> getInputTables() {
        List<MapperTable> inputTables = new ArrayList<MapperTable>();
        XmlMapData mapData = (XmlMapData) getRootEditPart().getModel();
        inputTables.addAll(mapData.getInputTrees());
        return inputTables;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.newabstractmap.figures.manager.FiguresManager#getOutputTables()
     */
    @Override
    protected List<MapperTable> getOutputTables() {
        List<MapperTable> outputTables = new ArrayList<MapperTable>();
        XmlMapData mapData = (XmlMapData) getRootEditPart().getModel();
        outputTables.addAll(mapData.getOutputTrees());
        return outputTables;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.newabstractmap.figures.manager.FiguresManager#isErrorRejectTable()
     */
    @Override
    protected boolean isErrorRejectTable(MapperTable table) {
        if (table instanceof OutputXmlTree) {
            return ((OutputXmlTree) table).isErrorReject();
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.newabstractmap.figures.manager.FiguresManager#isLookup(org.talend.designer.newabstractmap
     * .abstractmapper.MapperTable)
     */
    @Override
    protected boolean isLookup(MapperTable table) {
        if (table instanceof InputXmlTree) {
            return ((InputXmlTree) table).isLookup();
        }
        return false;
    }
}
