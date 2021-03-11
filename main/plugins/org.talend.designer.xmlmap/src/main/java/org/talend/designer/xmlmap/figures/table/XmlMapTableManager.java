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
import org.talend.designer.gefabstractmap.part.MapperTablePart;
import org.talend.designer.xmlmap.model.emf.xmlmap.AbstractInOutTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.InputXmlTree;
import org.talend.designer.xmlmap.model.tree.LOOKUP_MODE;

/**
 * created by Administrator on 2013-1-14 Detailled comment
 *
 */
public class XmlMapTableManager extends TableManager {

    /**
     * DOC wchen XmlMapTableManager constructor comment.
     *
     * @param tableModel
     * @param tablePart
     */
    public XmlMapTableManager(AbstractInOutTree tableModel, MapperTablePart tablePart) {
        super(tableModel, tablePart);
    }

    @Override
    public AbstractInOutTree getModel() {
        return (AbstractInOutTree) super.getModel();
    }

    @Override
    public boolean isActivateCondensedTool() {
        return getModel().isActivateCondensedTool();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.newabstractmap.figures.table.TableManager#isExpressionFilterActivate()
     */
    @Override
    public boolean isActivateExpressionFilter() {
        return getModel().isActivateExpressionFilter();
    }

    @Override
    public boolean isMinimized() {
        return getModel().isMinimized();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.newabstractmap.figures.table.TableManager#setActivateCondensedTool(boolean)
     */
    @Override
    public void setActivateCondensedTool(boolean active) {
        getModel().setActivateCondensedTool(active);
        if (active) {
            if (getModel() instanceof InputXmlTree) {
                InputXmlTree inputXmlTree = (InputXmlTree) getModel();
                if (inputXmlTree.isLookup() && !inputXmlTree.getLookupMode().equals(LOOKUP_MODE.LOAD_ONCE.toString())) {
                    setActivateGlobalMap(true);
                }
            }
        } else {
            setActivateGlobalMap(false);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.newabstractmap.figures.table.TableManager#setActivateExpressionFilter(boolean)
     */
    @Override
    public void setActivateExpressionFilter(boolean active) {
        getModel().setActivateExpressionFilter(active);

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.newabstractmap.figures.table.TableManager#setMinimized(boolean)
     */
    @Override
    public void setMinimized(boolean minisized) {
        getModel().setMinimized(minisized);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.newabstractmap.figures.table.TableManager#getExpressionFilter()
     */
    @Override
    public String getExpressionFilter() {
        return getModel().getExpressionFilter();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.newabstractmap.figures.table.TableManager#setExpressionFilter(java.lang.String)
     */
    @Override
    public void setExpressionFilter(String filter) {
        getModel().setExpressionFilter(filter);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.gefabstractmap.figures.manager.TableManager#isActivateGlobalMap()
     */
    @Override
    public boolean isActivateGlobalMap() {
        if (getModel() instanceof InputXmlTree) {
            InputXmlTree inputXmlTree = (InputXmlTree) getModel();
            return inputXmlTree.isActivateGlobalMap();
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.gefabstractmap.figures.manager.TableManager#setActivateGlobalMap(boolean)
     */
    @Override
    public void setActivateGlobalMap(boolean active) {
        if (getModel() instanceof InputXmlTree) {
            InputXmlTree inputXmlTree = (InputXmlTree) getModel();
            inputXmlTree.setActivateGlobalMap(active);
        }
    }
}
