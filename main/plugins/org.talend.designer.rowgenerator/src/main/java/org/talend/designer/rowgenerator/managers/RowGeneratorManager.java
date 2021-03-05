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
package org.talend.designer.rowgenerator.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.gef.commands.CommandStack;
import org.talend.designer.rowgenerator.RowGeneratorComponent;

/**
 * qzhang class global comment. Detailled comment <br/>
 *
 * $Id: RowGeneratorManager.java,v 1.8 2007/01/31 10:31:05 pub Exp $
 *
 */
public class RowGeneratorManager {

    private static List<Map<String, Object>> orginEP = new ArrayList<Map<String, Object>>();

    private static String orginNumber = "10"; //$NON-NLS-1$

    private RowGeneratorComponent rowGeneratorComponent;

    private UIManager uiManager;

    private CommandStack commandStack;

    /**
     * qzhang RowGeneratorManager constructor comment.
     */
    public RowGeneratorManager(RowGeneratorComponent rowGeneratorComponent) {
        this.rowGeneratorComponent = rowGeneratorComponent;
        this.uiManager = new UIManager(this);
        orginEP.clear();
        initOrginEP();
    }

    /**
     * qzhang Comment method "initOrginEP".
     */
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    private void initOrginEP() {
        List<Map<String, Object>> lines = rowGeneratorComponent.getMapList();
        for (Map<String, Object> map : lines) {
            Map<String, Object> newMap = new HashMap<String, Object>();
            newMap.putAll(map);
            orginEP.add(newMap);
        }
        orginNumber = rowGeneratorComponent.getNumber();
    }

    public UIManager getUiManager() {
        return this.uiManager;
    }

    public void setUiManager(UIManager uiManager) {
        this.uiManager = uiManager;
    }

    public CommandStack getCommandStack() {
        return this.commandStack;
    }

    public void setCommandStack(CommandStack commandStack) {
        this.commandStack = commandStack;
    }

    public RowGeneratorComponent getRowGeneratorComponent() {
        return this.rowGeneratorComponent;
    }

    public void setRowGeneratorComponent(RowGeneratorComponent rowGeneratorComponent) {
        this.rowGeneratorComponent = rowGeneratorComponent;
    }

    public List<Map<String, Object>> getOrginEP() {
        return orginEP;
    }

    public String getOrginNumber() {
        return orginNumber;
    }

    public void setOrginNumber(String orginNumber) {
        RowGeneratorManager.orginNumber = orginNumber;
    }

}
