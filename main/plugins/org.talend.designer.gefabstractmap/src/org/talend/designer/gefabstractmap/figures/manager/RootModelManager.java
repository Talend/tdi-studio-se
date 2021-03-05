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
package org.talend.designer.gefabstractmap.figures.manager;

import java.util.List;

import org.talend.designer.core.model.utils.emf.talendfile.AbstractExternalData;
import org.talend.designer.gefabstractmap.model.abstractmap.MapperTable;
import org.talend.designer.gefabstractmap.part.MapperRootEditPart;

/**
 * created by wchen on 2013-1-15 Detailled comment
 *
 */
public abstract class RootModelManager extends ModelManager {

    /**
     * DOC Administrator MapperModelManager constructor comment.
     *
     * @param model
     * @param editPart
     */
    public RootModelManager(AbstractExternalData model, MapperRootEditPart editPart) {
        super(model, editPart);
    }

    @Override
    public AbstractExternalData getModel() {
        return (AbstractExternalData) super.getModel();
    }

    @Override
    public MapperRootEditPart getEditPart() {
        return (MapperRootEditPart) super.getEditPart();
    }

    public abstract List<? extends MapperTable> getInputTables();

    public abstract List<? extends MapperTable> getOutputTables();

    public abstract List<? extends MapperTable> getVarTables();
}
