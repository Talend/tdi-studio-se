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
package org.talend.designer.gefabstractmap.utils;

import java.util.ArrayList;
import java.util.List;

import org.talend.designer.gefabstractmap.model.abstractmap.MapperTable;
import org.talend.designer.gefabstractmap.model.abstractmap.MapperTableEntity;
import org.talend.designer.gefabstractmap.part.MapperTablePart;
import org.talend.designer.gefabstractmap.part.TableEntityPart;

/**
 * created by Administrator on 2013-1-11 Detailled comment
 *
 */
public class MapperUtils {

    public static final int DEFAULT_OFFSET = 5;

    public static final int DEFAULT_OFFSET_FILTER = 7;

    public static MapperTablePart getMapperTablePart(TableEntityPart entityPart) {
        if (entityPart.getParent() instanceof MapperTablePart) {
            return (MapperTablePart) entityPart.getParent();
        } else if (entityPart.getParent() instanceof TableEntityPart) {
            return getMapperTablePart((TableEntityPart) entityPart.getParent());
        }
        return null;

    }

    public static List<TableEntityPart> getFlatChildrenPartList(MapperTablePart treePart) {
        List<TableEntityPart> partList = new ArrayList<TableEntityPart>();
        List children = treePart.getChildren();
        for (int i = 0; i < children.size(); i++) {
            partList.add((TableEntityPart) children.get(i));
            partList.addAll(getFlatChildrenPartList((TableEntityPart) children.get(i)));
        }
        return partList;
    }

    private static List<TableEntityPart> getFlatChildrenPartList(TableEntityPart treeNode) {
        List<TableEntityPart> list = new ArrayList<TableEntityPart>();
        List children = treeNode.getChildren();
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i) instanceof TableEntityPart) {
                TableEntityPart child = (TableEntityPart) children.get(i);
                list.add(child);
                if (!child.getChildren().isEmpty()) {
                    list.addAll(getFlatChildrenPartList(child));
                }
            }
        }
        return list;

    }

    public static MapperTable getMapperTable(MapperTableEntity entity) {
        if (entity.eContainer() instanceof MapperTable) {
            return (MapperTable) entity.eContainer();
        } else if (entity.eContainer() instanceof MapperTableEntity) {
            return getMapperTable((MapperTableEntity) entity.eContainer());
        }
        return null;

    }

}
