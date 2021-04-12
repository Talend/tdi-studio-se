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
package org.talend.designer.xmlmap.editor;

import org.talend.designer.gefabstractmap.editor.MapperGraphicalViewer;
import org.talend.designer.gefabstractmap.figures.manager.FiguresManager;
import org.talend.designer.xmlmap.figures.manager.XmlMapFiguresManager;
import org.talend.designer.xmlmap.ui.tabs.MapperManager;

/**
 * DOC XYuser class global comment. Detailled comment
 */
public class XmlMapGraphicViewer extends MapperGraphicalViewer {

    public XmlMapGraphicViewer() {
    }

    @Override
    public MapperManager getMapperManager() {
        return (MapperManager) super.getMapperManager();
    }

    @Override
    public XmlMapFiguresManager getFiguresManager() {
        return (XmlMapFiguresManager) super.getFiguresManager();
    }

    @Override
    protected FiguresManager createFiguresManager() {
        return new XmlMapFiguresManager(this);
    }

}
