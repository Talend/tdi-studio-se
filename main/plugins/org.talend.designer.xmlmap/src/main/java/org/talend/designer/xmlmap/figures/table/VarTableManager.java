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
package org.talend.designer.xmlmap.figures.table;

import org.talend.designer.gefabstractmap.figures.manager.TableManager;
import org.talend.designer.gefabstractmap.part.VarTablePart;
import org.talend.designer.xmlmap.model.emf.xmlmap.VarTable;

/**
 * DOC wchen class global comment. Detailled comment
 */
public class VarTableManager extends TableManager {

    public VarTableManager(VarTable mapperTable, VarTablePart tablePart) {
        super(mapperTable, tablePart);
    }

    @Override
    public VarTable getModel() {
        return (VarTable) super.getModel();
    }

    @Override
    public VarTablePart getEditPart() {
        return (VarTablePart) super.getEditPart();
    }

    @Override
    public void setMinimized(boolean minisized) {
        getModel().setMinimized(minisized);
    }

    @Override
    public boolean isMinimized() {
        return getModel().isMinimized();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.newabstractmap.figures.manager.TableManager#isActivateCondensedTool()
     */
    @Override
    public boolean isActivateCondensedTool() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.newabstractmap.figures.manager.TableManager#isActivateExpressionFilter()
     */
    @Override
    public boolean isActivateExpressionFilter() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.newabstractmap.figures.manager.TableManager#getExpressionFilter()
     */
    @Override
    public String getExpressionFilter() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.newabstractmap.figures.manager.TableManager#setExpressionFilter(java.lang.String)
     */
    @Override
    public void setExpressionFilter(String filter) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.newabstractmap.figures.manager.TableManager#setActivateCondensedTool(boolean)
     */
    @Override
    public void setActivateCondensedTool(boolean active) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.newabstractmap.figures.manager.TableManager#setActivateExpressionFilter(boolean)
     */
    @Override
    public void setActivateExpressionFilter(boolean active) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.gefabstractmap.figures.manager.TableManager#isActivateGlobalMap()
     */
    @Override
    public boolean isActivateGlobalMap() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.gefabstractmap.figures.manager.TableManager#setActivateGlobalMap(boolean)
     */
    @Override
    public void setActivateGlobalMap(boolean active) {
        // TODO Auto-generated method stub

    }

}
