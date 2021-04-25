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
import org.eclipse.swt.widgets.Composite;
import org.talend.designer.xmlmap.figures.table.XmlMapTableManager;
import org.talend.designer.xmlmap.figures.treesettings.OutputTreeSettingContainer;
import org.talend.designer.xmlmap.figures.treesettings.XmlMapFilterContainer;
import org.talend.designer.xmlmap.figures.treetools.TreeToolBarContainer;

/**
 * wchen class global comment. Detailled comment
 */
public class OutputXmlTreeFigure extends XmlmapInOutTreeFigure {

    public OutputXmlTreeFigure(XmlMapTableManager tableModelManager) {
        super(tableModelManager);
        createContents();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.xmlmap.figures.AbstractInOutTreeFigure#getTreeDisplayName()
     */
    @Override
    protected String getTreeDisplayName() {
        return getTableManager().getModel().getName();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.xmlmap.figures.AbstractInOutTreeFigure#createTreeSettings(org.eclipse.draw2d.Figure)
     */
    @Override
    protected void createTreeSettings(Figure parent) {
        settingContainer = new OutputTreeSettingContainer(getTableManager());
        parent.add(settingContainer);

        filterFigure = new XmlMapFilterContainer(getTableManager(), (Composite) getTableManager().getEditPart().getViewer()
                .getControl());
        parent.add(filterFigure);

    }

    public void update(int type) {
        settingContainer.update(type);
        filterFigure.update();
        toolBarContainer.updateMinSizeImage();
        ((TreeToolBarContainer) toolBarContainer).updateLoopFunctionButton();
    }

}
