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
package org.talend.designer.xmlmap.parts;

import java.util.List;

import org.talend.designer.gefabstractmap.figures.manager.RootModelManager;
import org.talend.designer.gefabstractmap.model.abstractmap.MapperTable;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlMapData;

/**
 * created by Administrator on 2013-1-15 Detailled comment
 *
 */
public class XmlMapDataManager extends RootModelManager {

    /**
     * DOC Administrator XmlMapDataManager constructor comment.
     *
     * @param model
     * @param editPart
     */
    public XmlMapDataManager(XmlMapData model, XmlMapDataEditPart editPart) {
        super(model, editPart);
    }

    @Override
    public XmlMapData getModel() {
        return (XmlMapData) super.getModel();
    }

    @Override
    public XmlMapDataEditPart getEditPart() {
        return (XmlMapDataEditPart) super.getEditPart();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.newabstractmap.figures.manager.RootModelManager#getInputTables()
     */
    @Override
    public List<? extends MapperTable> getInputTables() {
        return getModel().getInputTrees();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.newabstractmap.figures.manager.RootModelManager#getOutputTables()
     */
    @Override
    public List<? extends MapperTable> getOutputTables() {
        return getModel().getOutputTrees();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.newabstractmap.figures.manager.RootModelManager#getVarTables()
     */
    @Override
    public List<? extends MapperTable> getVarTables() {
        return getModel().getVarTables();
    }

}
