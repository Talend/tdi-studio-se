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
package org.talend.designer.xmlmap.figures;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.talend.designer.xmlmap.figures.table.GlobalMapContainer;
import org.talend.designer.xmlmap.figures.table.XmlMapTableManager;
import org.talend.designer.xmlmap.figures.treesettings.InputTreeSettingContainer;
import org.talend.designer.xmlmap.figures.treesettings.XmlMapFilterContainer;
import org.talend.designer.xmlmap.model.emf.xmlmap.InputXmlTree;

/**
 * wchen class global comment. Detailled comment
 */
public class InputXmlTreeFigure extends XmlmapInOutTreeFigure {

    public InputXmlTreeFigure(XmlMapTableManager tableModelManager) {
        super(tableModelManager);
        createContents();

    }

    @Override
    protected String getTreeDisplayName() {
        return (getInputXmlTree().isLookup() ? "lookup : " : "main :") + getInputXmlTree().getName();
    }

    @Override
    public void setBounds(Rectangle rect) {
        super.setBounds(rect);
    }

    public InputXmlTree getInputXmlTree() {
        return (InputXmlTree) getMappManager().getModel();
    }

    protected XmlMapTableManager getMappManager() {
        return super.getTableManager();
    }

    public void update(int type) {
        if (settingContainer != null) {
            settingContainer.update(type);
        }
        if (filterFigure != null) {
            filterFigure.update();
        }
        toolBarContainer.updateMinSizeImage();

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.xmlmap.figures.AbstractInOutTreeFigure#createTreeSettings(org.eclipse.draw2d.Figure)
     */
    @Override
    protected void createTreeSettings(Figure parent) {
        if (getInputXmlTree().isLookup()) {
            settingContainer = new InputTreeSettingContainer(getMappManager());
            parent.add(settingContainer);
            filterFigure = new XmlMapFilterContainer(getMappManager(), (Composite) getMappManager().getEditPart().getViewer()
                    .getControl());
            parent.add(filterFigure);
            globalMapContainer = new GlobalMapContainer(getMappManager());
            parent.add(globalMapContainer);
        }
    }
}
