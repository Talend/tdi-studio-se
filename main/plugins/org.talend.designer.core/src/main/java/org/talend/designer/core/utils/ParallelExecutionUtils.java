package org.talend.designer.core.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.types.TypesManager;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.ui.editor.nodes.Node;

public class ParallelExecutionUtils {

    private static final String HASH_KEYS = "HASH_KEYS";

    private static final String HASH_PARTION = "HASH_PARTITION";

    private static final String MERGE_SORT = "IS_SORTING";

    private static final String SPECIAL_SORT_COMPONENT = "tSortRow";

    public static boolean compareKeyPartions(IConnection parConnection, Node needToPar) {
        String partitioning = needToPar.getComponent().getPartitioning();
        boolean isSame = false;
        String[] partitionKey = partitioning.split("\\.");
        boolean canCompare = partitionKey.length > 1 ? true : false;
        if (canCompare) {
            // before compare,in case there is a Integer value exist in the column list
            if (isPartitionKeysExist(parConnection)) {
                reSetParKeyValuesForCon(parConnection);
            }
            IElementParameter parTableCon = parConnection.getElementParameter(HASH_KEYS);
            IElementParameter parTableNode = needToPar.getElementParameter(partitionKey[0]);
            if (parTableNode != null) {
                String clumnKeyListName = "KEY_COLUMN";// for the partition key
                String clumnNodeListName = partitionKey[1];

                List<String> parKeyValues = new ArrayList<String>();
                List<String> columnKeyValues = new ArrayList<String>();

                ElementParameter nodeElemForList = null;
                for (Map conColumnListMap : (List<Map>) parTableCon.getValue()) {
                    if (conColumnListMap.get(clumnKeyListName) instanceof String) {
                        parKeyValues.add((String) conColumnListMap.get(clumnKeyListName));
                    }
                }

                for (Object nodeItemList : parTableNode.getListItemsValue()) {
                    if (((ElementParameter) nodeItemList).getFieldType().equals(EParameterFieldType.PREV_COLUMN_LIST)
                            || ((ElementParameter) nodeItemList).getFieldType().equals(EParameterFieldType.COLUMN_LIST)) {
                        nodeElemForList = (ElementParameter) nodeItemList;
                        break;
                    }
                }
                if (nodeElemForList != null) {
                    for (Map nodeColumnListMap : (List<Map>) parTableNode.getValue()) {
                        Object value = nodeColumnListMap.get(clumnNodeListName);
                        if (nodeColumnListMap.get(clumnNodeListName) instanceof String) {
                            columnKeyValues.add((String) value);
                        } else if (value instanceof Integer) {
                            Integer index = (Integer) value;
                            if (nodeElemForList.getListItemsDisplayName().length > index) {
                                columnKeyValues.add(nodeElemForList.getListItemsDisplayName()[index]);
                            }
                        }
                    }
                }
                if (columnKeyValues.size() > 0) {
                    if (columnKeyValues.equals(parKeyValues)) {
                        isSame = true;
                    } else {
                        isSame = false;
                    }
                }
            }
        }
        return isSame;
    }

    public static INode getNextPartionerTargetNode(IConnection con) {
        INode targetNode = null;
        if (con.getTarget() != null) {
            String partitioning = con.getTarget().getComponent().getPartitioning();
            if (!(partitioning.equals("NONE") || partitioning.equals("AUTO"))) {
                targetNode = con.getTarget();
            } else {
                for (IConnection nextCon : con.getTarget().getOutgoingConnections()) {
                    if (nextCon.getLineStyle().hasConnectionCategory(IConnectionCategory.MAIN)
                            || nextCon.getLineStyle().hasConnectionCategory(IConnectionCategory.MERGE)) {
                        return getNextPartionerTargetNode(nextCon);
                    }
                }
            }
        }
        return targetNode;
    }

    public static void setHashKeysFromTarget(IConnection con, Node target) {
        List<String> targetKeyColumnList = getColumnListFromTargetNode(target);
        IElementParameter parTableCon = con.getElementParameter(HASH_KEYS);
        boolean isExistHashValue = false;
        if (parTableCon != null) {
            ((List) parTableCon.getValue()).clear();
            if (targetKeyColumnList.size() > 0) {
                con.getElementParameter(HASH_PARTION).setValue(true);

                Object[] itemCon = parTableCon.getListItemsValue();
                String clumnKeyListName = "";
                for (Object itemList : itemCon) {
                    if (((ElementParameter) itemList).getFieldType().equals(EParameterFieldType.PREV_COLUMN_LIST)
                            || ((ElementParameter) itemList).getFieldType().equals(EParameterFieldType.COLUMN_LIST)) {
                        clumnKeyListName = ((ElementParameter) itemList).getName();
                        break;
                    }
                }
                for (String partionValue : targetKeyColumnList) {
                    for (Object keyParMap : ((List) parTableCon.getValue())) {
                        Map existKeyMap = (Map) keyParMap;
                        if (existKeyMap.get(clumnKeyListName).equals(partionValue)) {
                            isExistHashValue = true;
                            break;
                        }
                    }
                    if (!isExistHashValue) {
                        Map partionKeyMap = new HashMap<String, String>();
                        partionKeyMap.put(clumnKeyListName, partionValue);
                        ((List) parTableCon.getValue()).add(partionKeyMap);
                    }
                }
            }
        }
    }

    public static List<String> getColumnListFromTargetNode(Node target) {
        List<String> columnKeyValues = new ArrayList<String>();
        IElementParameter parTableNode = target.getElementParameterFromField(EParameterFieldType.TABLE);
        if (parTableNode != null) {
            Object[] itemNode = parTableNode.getListItemsValue();
            ElementParameter clumnNodeList = null;
            for (Object itemList : itemNode) {
                if (((ElementParameter) itemList).getFieldType().equals(EParameterFieldType.PREV_COLUMN_LIST)
                        || ((ElementParameter) itemList).getFieldType().equals(EParameterFieldType.COLUMN_LIST)) {
                    clumnNodeList = ((ElementParameter) itemList);
                    break;
                }
            }
            if (clumnNodeList != null) {
                for (Map nodeColumnListMap : (List<Map>) parTableNode.getValue()) {
                    Object value = nodeColumnListMap.get(clumnNodeList.getName());
                    if (nodeColumnListMap.get(clumnNodeList.getName()) instanceof String) {
                        columnKeyValues.add((String) value);
                    } else if (value instanceof Integer) {
                        Integer index = (Integer) value;
                        if (clumnNodeList.getListItemsDisplayName().length > index) {
                            columnKeyValues.add(clumnNodeList.getListItemsDisplayName()[index]);
                        }
                    }
                }
            }
        }
        return columnKeyValues;
    }

    public static void reSetParKeyValuesForCon(IConnection parConnection) {
        IElementParameter parTableCon = parConnection.getElementParameter(HASH_KEYS);
        if (parTableCon != null) {
            Object[] itemCon = parTableCon.getListItemsValue();
            ElementParameter conElemForList = null;
            String clumnKeyListName = "";
            for (Object itemList : itemCon) {
                if (((ElementParameter) itemList).getFieldType().equals(EParameterFieldType.PREV_COLUMN_LIST)
                        || ((ElementParameter) itemList).getFieldType().equals(EParameterFieldType.COLUMN_LIST)) {
                    conElemForList = (ElementParameter) itemList;
                    clumnKeyListName = ((ElementParameter) itemList).getName();
                }
            }
            List<String> parKeyValues = new ArrayList<String>();
            if (conElemForList != null) {
                for (Map conColumnListMap : (List<Map>) parTableCon.getValue()) {
                    if (conColumnListMap.get(clumnKeyListName) instanceof String) {
                        parKeyValues.add((String) conColumnListMap.get(clumnKeyListName));
                    }
                    if (conColumnListMap.get(clumnKeyListName) instanceof Integer) {
                        Integer index = (Integer) conColumnListMap.get(clumnKeyListName);
                        parKeyValues.add((String) conElemForList.getListItemsValue()[index]);
                        // if the value of key is Integer index,need to set it back to the value
                        if (conElemForList.getListItemsDisplayName().length > index) {
                            conColumnListMap.put(clumnKeyListName, conElemForList.getListItemsDisplayName()[index]);
                        }
                    }
                }
            }
        }
    }

    public static boolean isConClumnsContainsPartionKey(IConnection con, Node firstParNode) {
        List<String> columnListForNode = getColumnListFromTargetNode(firstParNode);
        List<String> columnListForCon = getColumnList(con.getMetadataTable());

        for (String clumnNode : columnListForNode) {
            if (columnListForCon.contains(clumnNode)) {
                return true;
            }
        }
        return false;

    }

    public static boolean isPartitionKeysExist(IConnection con) {
        IElementParameter parTableCon = con.getElementParameter(HASH_KEYS);
        if (parTableCon != null) {
            Object[] itemParKey = parTableCon.getListItemsValue();
            if (((List<Map>) parTableCon.getValue()).size() > 0) {
                return true;
            }
        }
        return false;
    }

    public static void setHashKeysForCon(IConnection con) {
        List<String> conKeyColumnList = getKeyColumnList(con.getMetadataTable());
        IElementParameter parTableCon = con.getElementParameter(HASH_KEYS);
        boolean isExistHashValue = false;
        if (parTableCon != null) {
            if (conKeyColumnList.size() > 0) {
                ((List) parTableCon.getValue()).clear();
                con.getElementParameter(HASH_PARTION).setValue(true);

                Object[] itemCon = parTableCon.getListItemsValue();
                String clumnKeyListName = "";
                for (Object itemList : itemCon) {
                    if (((ElementParameter) itemList).getFieldType().equals(EParameterFieldType.PREV_COLUMN_LIST)
                            || ((ElementParameter) itemList).getFieldType().equals(EParameterFieldType.COLUMN_LIST)) {
                        clumnKeyListName = ((ElementParameter) itemList).getName();
                        break;
                    }
                }
                for (String partionValue : conKeyColumnList) {
                    for (Object keyParMap : ((List) parTableCon.getValue())) {
                        Map existKeyMap = (Map) keyParMap;
                        if (existKeyMap.get(clumnKeyListName).equals(partionValue)) {
                            isExistHashValue = true;
                            break;
                        }
                    }
                    if (!isExistHashValue) {
                        Map partionKeyMap = new HashMap<String, String>();
                        partionKeyMap.put(clumnKeyListName, partionValue);
                        ((List) parTableCon.getValue()).add(partionKeyMap);
                    }
                }
            } else {
                con.getElementParameter(HASH_PARTION).setValue(false);
            }
        }
    }

    public static List<String> getKeyColumnList(IMetadataTable table) {
        List<String> columnList = new ArrayList<String>();
        if (table != null) {
            for (IMetadataColumn column : table.getListColumns()) {
                if (column.isKey()) {
                    String label = column.getLabel();
                    columnList.add(label);
                }
            }
        }
        return columnList;
    }

    public static List<String> getColumnList(IMetadataTable table) {
        List<String> columnList = new ArrayList<String>();
        if (table != null) {
            for (IMetadataColumn column : table.getListColumns()) {
                String label = column.getLabel();
                columnList.add(label);
            }
        }
        return columnList;
    }

    public static boolean existPreviousPar(Node currentNode) {
        // To judge if there has par/col on previous connection
        boolean hasInPreviousCon = false;
        for (IConnection con : currentNode.getIncomingConnections()) {
            if ((con.getElementParameter(EParameterName.PARTITIONER.getName()) != null && con
                    .getElementParameter(EParameterName.PARTITIONER.getName()).getValue().equals(true))) {
                hasInPreviousCon = true;
                break;
            }
        }
        return hasInPreviousCon;
    }

    public static boolean existPreviousOtherCon(Node currentNode) {
        // To judge if there has a con such as ON_SUBJOB_OK
        boolean hasInPreviousCon = false;
        for (IConnection con : currentNode.getIncomingConnections()) {
            if ((con.getLineStyle() == EConnectionType.ON_SUBJOB_OK || con.getLineStyle() == EConnectionType.ON_SUBJOB_ERROR
                    || con.getLineStyle() == EConnectionType.RUN_IF || con.getLineStyle() == EConnectionType.ROUTE_WHEN
                    || con.getLineStyle() == EConnectionType.ROUTE_CATCH || con.getLineStyle() == EConnectionType.ON_COMPONENT_OK || con
                        .getLineStyle() == EConnectionType.ON_COMPONENT_ERROR)) {

            }
            hasInPreviousCon = true;
            break;
        }
        return hasInPreviousCon;
    }

    public static boolean existPreviousDepar(Node currentNode) {
        // To judge if there has par/col on previous connection
        boolean hasInPreviousCon = false;
        for (IConnection con : currentNode.getIncomingConnections()) {
            if ((con.getElementParameter(EParameterName.DEPARTITIONER.getName()) != null && con
                    .getElementParameter(EParameterName.DEPARTITIONER.getName()).getValue().equals(true))) {
                hasInPreviousCon = true;
                break;
            }
        }
        return hasInPreviousCon;
    }

    public static boolean existNextDepar(Node currentNode) {
        // To judge if there has par/col on previous connection
        boolean hasNextCon = false;
        for (IConnection con : currentNode.getOutgoingConnections()) {
            if ((con.getElementParameter(EParameterName.DEPARTITIONER.getName()) != null && con
                    .getElementParameter(EParameterName.DEPARTITIONER.getName()).getValue().equals(true))) {
                hasNextCon = true;
                break;
            }
        }
        return hasNextCon;
    }

    public static boolean existPreviousRepar(Node currentNode) {
        // To judge if there has par/col on previous connection
        boolean hasInPreviousCon = false;
        for (IConnection con : currentNode.getIncomingConnections()) {
            if ((con.getElementParameter(EParameterName.REPARTITIONER.getName()) != null && con
                    .getElementParameter(EParameterName.REPARTITIONER.getName()).getValue().equals(true))) {
                hasInPreviousCon = true;
                break;
            }
        }
        return hasInPreviousCon;
    }

    public static boolean existPreviousNone(Node currentNode) {
        // To judge if there has par/col on previous connection
        boolean hasInPreviousCon = false;
        for (IConnection con : currentNode.getIncomingConnections()) {
            if ((con.getElementParameter(EParameterName.NONE.getName()) != null && con
                    .getElementParameter(EParameterName.NONE.getName()).getValue().equals(true))) {
                hasInPreviousCon = true;
                break;
            }
        }
        return hasInPreviousCon;
    }

    public static IConnection getPreviousParCon(Node previousNode) {
        if (previousNode.getIncomingConnections().size() > 0) {
            for (IConnection con : previousNode.getIncomingConnections()) {
                if ((con.getElementParameter(EParameterName.PARTITIONER.getName()) != null && con
                        .getElementParameter(EParameterName.PARTITIONER.getName()).getValue().equals(true))
                        || (con.getElementParameter(EParameterName.REPARTITIONER.getName()) != null && con
                                .getElementParameter(EParameterName.REPARTITIONER.getName()).getValue().equals(true))) {
                    return con;
                } else {
                    return getPreviousParCon((Node) con.getSource());
                }
            }
        }
        return null;
    }

    public static boolean isExistPreviousParCon(Node previousNode) {
        Set<String> checkedNodeSet = new HashSet<String>();
        boolean existPreviousParCon = isExistPreviousParCon(previousNode, checkedNodeSet);
        return existPreviousParCon;
    }

    private static boolean isExistPreviousParCon(Node previousNode, Set<String> checkedSet) {
        boolean hasParInPreviousCon = false;
        if (previousNode == null || checkedSet.contains(previousNode.getUniqueName())) {
            // already checked, return
            return hasParInPreviousCon;
        }
        checkedSet.add(previousNode.getUniqueName());
        if (previousNode.getIncomingConnections().size() > 0) {
            for (IConnection con : previousNode.getIncomingConnections()) {
                if (con.getElementParameter(EParameterName.PARTITIONER.getName()) != null
                        && con.getElementParameter(EParameterName.PARTITIONER.getName()).getValue().equals(true)
                        || con.getElementParameter(EParameterName.REPARTITIONER.getName()) != null
                        && con.getElementParameter(EParameterName.REPARTITIONER.getName()).getValue().equals(true)) {
                    hasParInPreviousCon = true;
                    break;
                } else {
                    hasParInPreviousCon = isExistPreviousParCon((Node) con.getSource(), checkedSet);
                    if (hasParInPreviousCon) {
                        break;
                    }
                }
            }
        }
        return hasParInPreviousCon;
    }

    public static boolean isExistNextDeparCon(Node nextNode) {
        boolean hasDeparInNextCon = false;
        if (nextNode.getOutgoingConnections().size() > 0) {
            for (IConnection con : nextNode.getOutgoingConnections()) {
                if (con.getElementParameter(EParameterName.DEPARTITIONER.getName()) != null
                        && con.getElementParameter(EParameterName.DEPARTITIONER.getName()).getValue().equals(true)
                        || con.getElementParameter(EParameterName.DEPARTITIONER.getName()) != null
                        && con.getElementParameter(EParameterName.DEPARTITIONER.getName()).getValue().equals(true)) {
                    hasDeparInNextCon = true;
                    break;
                } else {
                    hasDeparInNextCon = isExistNextDeparCon((Node) con.getTarget());
                }
            }
        }
        return hasDeparInNextCon;
    }

    public static boolean isExistPreviouDeparCon(Node previousNode) {
        boolean hasDeparInPreviousCon = false;
        if (previousNode.getIncomingConnections().size() > 0) {
            for (IConnection con : previousNode.getIncomingConnections()) {
                if (con.getElementParameter(EParameterName.DEPARTITIONER.getName()) != null
                        && con.getElementParameter(EParameterName.DEPARTITIONER.getName()).getValue().equals(true)) {
                    hasDeparInPreviousCon = true;
                    break;
                } else {
                    hasDeparInPreviousCon = isExistPreviouDeparCon((Node) con.getSource());
                }
            }
        }
        return hasDeparInPreviousCon;
    }

    public static boolean isExistPartitioningCon(Node startNode) {
        boolean existPartitioningCon = false;
        for (IConnection con : startNode.getOutgoingConnections()) {
            if ((con.getElementParameter(EParameterName.DEPARTITIONER.getName()) != null && con
                    .getElementParameter(EParameterName.DEPARTITIONER.getName()).getValue().equals(true))
                    || (con.getElementParameter(EParameterName.PARTITIONER.getName()) != null && con
                            .getElementParameter(EParameterName.PARTITIONER.getName()).getValue().equals(true))
                    || (con.getElementParameter(EParameterName.REPARTITIONER.getName()) != null && con
                            .getElementParameter(EParameterName.REPARTITIONER.getName()).getValue().equals(true))) {
                existPartitioningCon = true;
                break;
            } else {
                existPartitioningCon = isExistPartitioningCon((Node) con.getTarget());
            }
        }
        return existPartitioningCon;
    }

    public static boolean isExistParallelCon(Node startNode) {
        boolean existPartitioningCon = false;
        for (IConnection con : startNode.getOutgoingConnections()) {
            if ((con.getElementParameter(EParameterName.PARTITIONER.getName()) != null && con
                    .getElementParameter(EParameterName.PARTITIONER.getName()).getValue().equals(true))) {
                existPartitioningCon = true;
                break;
            } else {
                existPartitioningCon = isExistParallelCon((Node) con.getTarget());
                if (existPartitioningCon) {
                    break;
                }
            }
        }
        return existPartitioningCon;
    }

    public static void setDBType(IMetadataTable metaTable, String dbmsid) {
        List<IMetadataColumn> listColumns = metaTable.getListColumns();
        for (IMetadataColumn column : listColumns) {
            String talendType = column.getTalendType();
            String type = column.getType();
            if (dbmsid != null) {
                if (!TypesManager.checkDBType(dbmsid, talendType, type)) {
                    column.setType(TypesManager.getDBTypeFromTalendType(dbmsid, talendType));
                }
            }
        }
    }

    public static void copyTable(String dbmsId, IMetadataTable source, IMetadataTable target) {
        setDBType(source, dbmsId);
        copyTable(source, target);
    }

    public static void copyTable(IMetadataTable source, IMetadataTable target) {
        if (source == null || target == null) {
            return;
        }
        List<IMetadataColumn> columnsToRemove = new ArrayList<IMetadataColumn>();
        List<String> readOnlycolumns = new ArrayList<String>();
        for (IMetadataColumn column : target.getListColumns(true)) {
            if (!column.isCustom()) {
                columnsToRemove.add(column);
            }
            if (column.isReadOnly()) {
                readOnlycolumns.add(column.getLabel());
            }
        }
        target.getListColumns().removeAll(columnsToRemove);
        target.getListUnusedColumns().removeAll(columnsToRemove);

        List<IMetadataColumn> columnsTAdd = new ArrayList<IMetadataColumn>();
        for (IMetadataColumn column : source.getListColumns(true)) {
            IMetadataColumn targetColumn = target.getColumn(column.getLabel());
            IMetadataColumn newTargetColumn = column.clone();
            if (targetColumn == null) {
                columnsTAdd.add(newTargetColumn);
                newTargetColumn.setReadOnly(target.isReadOnly() || readOnlycolumns.contains(newTargetColumn.getLabel()));
            } else {
                if (!targetColumn.isReadOnly()) {
                    target.getListColumns().remove(targetColumn);
                    newTargetColumn.setCustom(targetColumn.isCustom());
                    newTargetColumn.setCustomId(targetColumn.getCustomId());
                    columnsTAdd.add(newTargetColumn);
                }
            }
        }
        target.getListColumns().addAll(columnsTAdd);
        target.sortCustomColumns();
        target.setLabel(source.getLabel());
    }

    public static boolean isExistParallelConn(IConnection[] cons) {
        boolean hasParallel = false;
        for (IConnection conn : cons) {
            IElementParameter param = conn.getElementParameter(EParameterName.PARTITIONER.getName());
            if (param != null && Boolean.TRUE.equals(param.getValue())) {
                hasParallel = true;
            }
        }
        return hasParallel;
    }

    public static void setMergeSortByConditions(IConnection currentReparCon) {
        if (currentReparCon.getSource().getComponent().getName().equals(SPECIAL_SORT_COMPONENT)) {
            currentReparCon.getElementParameter(MERGE_SORT).setValue(true);
        } else {
            Node previousSortNode = ParallelExecutionUtils.getFirstPreviousSortNode(currentReparCon.getSource());
            for (IConnection outConOfSort : previousSortNode.getOutgoingConnections()) {
                if ((Boolean) outConOfSort.getPropertyValue(EParameterName.PARTITIONER.getName())
                        || (Boolean) outConOfSort.getPropertyValue(EParameterName.REPARTITIONER.getName())) {
                    if ((Boolean) outConOfSort.getElementParameter(MERGE_SORT).getValue()) {
                        currentReparCon.getElementParameter(MERGE_SORT).setValue(false);
                    } else {
                        currentReparCon.getElementParameter(MERGE_SORT).setValue(true);
                    }
                }
            }
        }
    }

    public static IConnection getPreviousMainCon(Node previousNode) {
        for (IConnection con : previousNode.getIncomingConnections()) {
            if (con.getLineStyle().equals(EConnectionType.FLOW_MAIN)) {
                return con;
            }
        }
        return null;
    }

    public static IConnection getFirstPreviousParCon(Node previousNode) {
        for (IConnection con : previousNode.getIncomingConnections()) {
            if ((con.getElementParameter(EParameterName.PARTITIONER.getName()) != null && con
                    .getElementParameter(EParameterName.PARTITIONER.getName()).getValue().equals(true))
                    || (con.getElementParameter(EParameterName.REPARTITIONER.getName()) != null && con
                            .getElementParameter(EParameterName.REPARTITIONER.getName()).getValue().equals(true))) {
                return con;
            }
        }
        return null;
    }

    public static Node getFirstPreviousSortNode(INode previousNode) {
        for (IConnection con : previousNode.getIncomingConnections()) {
            if (con.getSource().getComponent().getName().equals(SPECIAL_SORT_COMPONENT)) {
                if (con.getLineStyle().equals(EConnectionType.FLOW_MAIN)) {
                    return (Node) con.getSource();
                }
            } else {
                return getFirstPreviousSortNode(con.getSource());
            }
        }
        return null;
    }

    public static IConnection getRepeatMergeSortCon(IConnection currentConn) {
        Node previousSortNode = ParallelExecutionUtils.getFirstPreviousSortNode(currentConn.getSource());
        if (previousSortNode != null) {
            for (IConnection outConOfSort : previousSortNode.getOutgoingConnections()) {
                if (outConOfSort.getElementParameter(EParameterName.PARTITIONER.getName()).getValue().equals(true)
                        || outConOfSort.getElementParameter(EParameterName.REPARTITIONER.getName()).getValue().equals(true)) {
                    if ((Boolean) outConOfSort.getElementParameter(MERGE_SORT).getValue()) {
                        return outConOfSort;
                    }
                }
            }
        }
        return null;
    }
}
