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
package org.talend.designer.gefabstractmap.part;

import java.util.ArrayList;
import java.util.List;

/**
 * created by Administrator on 2013-1-11 Detailled comment
 *
 */
public abstract class MapperTablePart extends BaseEditPart {

    public void updateChildrenConnections(List treeNodeParts, boolean selected) {
        for (Object obj : treeNodeParts) {
            if (obj instanceof TableEntityPart) {
                List connections = new ArrayList();
                TableEntityPart entityPart = (TableEntityPart) obj;
                connections.addAll(entityPart.getSourceConnections());
                connections.addAll(entityPart.getTargetConnections());
                for (Object connObj : connections) {
                    if (connObj instanceof BaseConnectionEditPart) {
                        ((BaseConnectionEditPart) connObj).updateForegroundColor(selected);
                    }
                }
                if (!entityPart.getChildren().isEmpty()) {
                    updateChildrenConnections(entityPart.getChildren(), selected);
                }
            }
        }
    }

    public abstract void highLightHeader(boolean highLight);

}
