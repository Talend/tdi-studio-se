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
package org.talend.designer.core.ui.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IWorkbenchPart;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.IConnectionCategory;

/**
 * cli class global comment. Detailled comment
 *
 * this action process all of the no data connection.
 *
 * "connection.getLineStyle().hasConnectionCategory(IConnectionCategory.DATA)" is false for bug 8087
 */
public class ModifyConnectionOrderAction extends ModifyOutputOrderAction {

    private static final String PREFIX = "org.talend.designer.core.ui.editor.action.Modify"; //$NON-NLS-1$

    private static final String SUFFIX = "ConnectionOrderAction"; //$NON-NLS-1$

    private EConnectionType connType;

    public ModifyConnectionOrderAction(IWorkbenchPart part, EConnectionType connType) {
        super(part);
        Assert.isNotNull(connType);
        this.connType = connType;
        setId(calcOrderActionID(connType));
    }

    @Override
    protected Integer getConnectionCategory() {
        return null;
    }

    @Override
    protected EConnectionType getConnectionType() {
        return connType;
    }

    // ////////////////////////

    private static String calcOrderActionID(EConnectionType connType) {
        if (connType != null) {
            return PREFIX + connType.getDefaultLinkName() + SUFFIX;
        }
        return PREFIX;
    }

    private static boolean enable(EConnectionType connType) {
        if (connType != null && !connType.hasConnectionCategory(IConnectionCategory.DATA)) {
            if (connType == EConnectionType.PARALLELIZE ? !connType.getName().equals(EConnectionType.PARALLELIZE.name()) : true) {
                return true;
            }
        }
        return false;
    }

    public static List<IAction> getOrderActions(IWorkbenchPart part) {
        List<IAction> actions = new ArrayList<IAction>();
        if (part != null) {
            for (EConnectionType connType : EConnectionType.values()) {
                if (enable(connType)) {
                    actions.add(new ModifyConnectionOrderAction(part, connType));
                }
            }
        }
        return actions;
    }

    public static List<String> getOrderActionIDs() {
        List<String> actionIDs = new ArrayList<String>();
        for (EConnectionType connType : EConnectionType.values()) {
            if (enable(connType)) {
                actionIDs.add(calcOrderActionID(connType));
            }
        }
        String[] arrayIds = actionIDs.toArray(new String[0]);
        Arrays.sort(arrayIds);
        return Arrays.asList(arrayIds);
    }
}
