// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.hl7.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.talend.core.model.metadata.MetadataToolHelper;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.HL7FileNode;
import org.talend.designer.hl7.model.PrimitiveModel;
import org.talend.designer.hl7.model.SegmentModel;
import org.talend.designer.hl7.model.TypeModel;
import org.talend.designer.hl7.ui.data.Element;
import org.talend.designer.hl7.ui.data.HL7TreeNode;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Group;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Segment;
import ca.uhn.hl7v2.model.Structure;

/**
 * DOC hwang class global comment. Detailled comment
 */
public class HL7PublicUtil {

    private int curOrder;

    public void initNodeOrder(HL7TreeNode node, Map<String, Integer> orderMap) {
        if (node == null) {
            return;
        }
        HL7TreeNode parent = node.getParent();
        if (parent == null) {
            curOrder = 1;
            node.setOrder(1);
            String path = getPath(node);
            orderMap.put(path, curOrder);
            curOrder++;
        }
        List<HL7TreeNode> childNode = node.getChildren();
        for (HL7TreeNode child : childNode) {
            child.setOrder(curOrder);
            String path = getPath(child);
            orderMap.put(path, curOrder);
            curOrder++;
            if (child.getChildren().size() > 0) {
                initNodeOrder(child, orderMap);
            }
        }

    }

    private int getNodeOrder(HL7TreeNode node, Map<String, Integer> orderMap) {
        if (node != null) {
            String path = getPath(node);
            return orderMap.get(path);
        }
        return 0;
    }

    public String getPath(HL7TreeNode treeNode) {
        StringBuffer path = new StringBuffer();
        HL7TreeNode tmp = treeNode;
        while (tmp != null) {
            path.insert(0, "/" + tmp.getLabel()); //$NON-NLS-1$
            tmp = tmp.getParent();
        }
        return path.toString();
    }

    public void tableLoader(Element element, String parentPath, List<HL7FileNode> table, String defaultValue,
            Map<String, Integer> orderMap) {
        HL7FileNode hl7FileNode = ConnectionFactory.eINSTANCE.createHL7FileNode();
        String currentPath = parentPath + "/" + element.getLabel();
        hl7FileNode.setFilePath(currentPath);
        hl7FileNode.setRelatedColumn(element.getColumnLabel());
        hl7FileNode.setAttribute(element.isMain() ? "main" : "branch");
        hl7FileNode.setDefaultValue(defaultValue);
        // xmlFileNode.setType(element.getDataType());
        hl7FileNode.setOrder(getNodeOrder(element, orderMap));
        hl7FileNode.setRepeatable(element.isRepetable());
        table.add(hl7FileNode);
        for (HL7TreeNode att : element.getAttributeChildren()) {
            hl7FileNode = ConnectionFactory.eINSTANCE.createHL7FileNode();
            hl7FileNode.setFilePath(att.getLabel());
            hl7FileNode.setRelatedColumn(att.getColumnLabel());
            hl7FileNode.setAttribute("attri");
            hl7FileNode.setDefaultValue(att.getDefaultValue());
            // xmlFileNode.setType(att.getDataType());
            hl7FileNode.setOrder(getNodeOrder(att, orderMap));
            hl7FileNode.setRepeatable(att.isRepetable());
            table.add(hl7FileNode);
        }
        for (HL7TreeNode att : element.getNameSpaceChildren()) {
            hl7FileNode = ConnectionFactory.eINSTANCE.createHL7FileNode();
            hl7FileNode.setFilePath(att.getLabel());
            hl7FileNode.setRelatedColumn(att.getColumnLabel());
            hl7FileNode.setAttribute("ns");
            hl7FileNode.setDefaultValue(att.getDefaultValue());
            // xmlFileNode.setType(att.getDataType());
            hl7FileNode.setOrder(getNodeOrder(att, orderMap));
            hl7FileNode.setRepeatable(att.isRepetable());
            table.add(hl7FileNode);
        }
        List<HL7TreeNode> children = element.getElementChildren();
        for (HL7TreeNode child : children) {
            // if (!child.isGroup() && !child.isLoop()) {
            tableLoader((Element) child, currentPath, table, child.getDefaultValue(), orderMap);
            // }
        }

    }

    public List<HL7TreeNode> getHL7TreeNodes(List<Message> messageList) {
        List<HL7TreeNode> list = new ArrayList<HL7TreeNode>();
        if (messageList == null) {
            return list;
        }
        for (Message message : messageList) {
            HL7TreeNode root = initTreeRoot(message);
            if (root instanceof Element) {
                // root = ((Element) root).getElementChildren().get(0);
                root.setParent(null);
                list.add(root);
            }
        }
        return list;
    }

    public HL7TreeNode initTreeRoot(Object treeNode) {
        HL7TreeNode node = new Element();
        String nodeLabel = getLabel(treeNode, true);
        node.setLabel(nodeLabel);
        node.setMain(true);
        List children = getFirstLevelChild(treeNode);
        if (children != null) {
            for (Object child : children) {
                String label = getLabel(child, true);
                HL7TreeNode childEle = cloneATreeNode(child, label);

                childEle.setLabel(label);
                if (!(child instanceof Group)) {
                    ((Element) childEle).setRow(label);
                }

                node.addChild(childEle);
            }
        }
        return node;
    }

    public HL7TreeNode cloneATreeNode(Object treeNode, String label) {
        HL7TreeNode node = new Element();
        String nodeLabel = getLabel(treeNode, true);
        node.setLabel(nodeLabel);
        String rowLabel = label;
        if (treeNode instanceof SegmentModel) {
            ((Element) node).setRow(nodeLabel);
            rowLabel = nodeLabel;
        } else if (!(treeNode instanceof Group)) {
            ((Element) node).setRow(rowLabel);
        }
        node.setMain(true);
        Object[] children = getChildList(treeNode);// treeNode.getChildren();
        if (children != null) {
            for (Object element : children) {

                if (getChildList(element).length > 0) {
                    String tlabel = getLabel(element, true);
                    String childLabel = rowLabel;
                    HL7TreeNode childEle = cloneATreeNode(element, rowLabel);
                    if (element instanceof SegmentModel) {
                        childLabel = tlabel;
                    }
                    childEle.setLabel(tlabel);
                    if (childEle instanceof Element) {
                        ((Element) childEle).setRow(childLabel);
                    }

                    node.addChild(childEle);
                } else {
                    HL7TreeNode childEle = new Element();
                    String tlabel = getLabel(element, false);
                    childEle.setLabel(tlabel);
                    if (childEle instanceof Element) {
                        ((Element) childEle).setRow(label);
                        ((Element) childEle).setColumnName(getLabel(element, true));
                    }
                    childEle.setMain(false);
                    node.addChild(childEle);
                }
            }
        }
        return node;
    }

    public Object[] getChildList(Object parentElement) {
        List values = new ArrayList();
        if (parentElement instanceof Message) {
            Message messParent = (Message) parentElement;
            String[] childNames = messParent.getNames();
            if (!values.isEmpty()) {
                values.clear();
            }
            for (int i = 0; i < childNames.length; i++) {
                try {
                    Structure[] childReps = messParent.getAll(childNames[i]);
                    for (int j = 0; j < childReps.length; j++) {
                        if (childReps[j] instanceof Message) {
                            values.add(childReps[j]);
                        }
                        if (childReps[j] instanceof Group) {
                            allSegmentFromGroup.clear();
                            getAllSegmentsFromGroup((Group) childReps[j]);
                            if (allSegmentFromGroup.size() > 0) {
                                values.addAll(Arrays.asList(allSegmentFromGroup.toArray(new SegmentModel[0])));
                            }
                        }
                        if (childReps[j] instanceof Segment) {
                            SegmentModel sModel = new SegmentModel((Segment) childReps[j], messParent, i, j);
                            if (sModel.getTypes() != null && sModel.getTypes().length > 0) {
                                values.add(sModel);
                                if (!allSegmentsForMessage.contains(sModel)) {
                                    allSegmentsForMessage.add(sModel);
                                }
                            }
                        }
                    }
                    // values.addAll(Arrays.asList(childReps));
                } catch (HL7Exception e) {
                    e.printStackTrace();
                }
            }
            return values.toArray();
        }

        if (parentElement instanceof Segment) {
            values.clear();
            Segment segment = (Segment) parentElement;
            SegmentModel sm = new SegmentModel(segment, segment, 0, 0);
            return sm.getTypes();
        }

        if (parentElement instanceof SegmentModel) {
            SegmentModel sm = (SegmentModel) parentElement;
            return sm.getTypes();
        }

        if (parentElement instanceof TypeModel) {
            TypeModel tm = (TypeModel) parentElement;
            return tm.getPrimitives();
        }

        if (parentElement instanceof Group) {
            values.clear();
            Group group = (Group) parentElement;
            String[] childNames = group.getNames();
            for (int i = 0; i < childNames.length; i++) {
                try {
                    Structure[] childReps = group.getAll(childNames[i]);
                    for (int j = 0; j < childReps.length; j++) {
                        if (childReps[j] instanceof Segment) {
                            SegmentModel sm = new SegmentModel((Segment) childReps[j], group, i, j);
                            if (sm.getTypes() != null && sm.getTypes().length > 0) {
                                values.add(sm);
                            }
                        } else {
                            values.add(childReps[j]);
                        }
                    }
                    // values.addAll(Arrays.asList(childReps));
                } catch (HL7Exception e) {
                    e.printStackTrace();
                }
            }

            return values.toArray();
        }
        return new Object[0];
    }

    private List<SegmentModel> allSegmentFromGroup = new ArrayList<SegmentModel>();

    private List<SegmentModel> allSegmentsForMessage = new ArrayList<SegmentModel>();

    private void getAllSegmentsFromGroup(Group group) {
        String[] childNames = group.getNames();
        for (int i = 0; i < childNames.length; i++) {
            try {
                Structure[] childReps = group.getAll(childNames[i]);
                for (int j = 0; j < childReps.length; j++) {
                    if (childReps[j] instanceof Segment) {
                        SegmentModel sm = new SegmentModel((Segment) childReps[j], group, i, j);
                        if (sm.getTypes() != null && sm.getTypes().length > 0) {
                            allSegmentFromGroup.add(sm);
                            if (!allSegmentsForMessage.contains(sm)) {
                                allSegmentsForMessage.add(sm);
                            }
                        }
                    } else if (childReps[j] instanceof Group) {
                        getAllSegmentsFromGroup((Group) childReps[j]);
                    }
                }
            } catch (HL7Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Set getChildren(Object parentElement) {
        // List values = new ArrayList();
        Set values = new HashSet();
        if (parentElement instanceof Message) {
            Message messParent = (Message) parentElement;
            String[] childNames = messParent.getNames();
            if (!values.isEmpty()) {
                values.clear();
            }
            for (int i = 0; i < childNames.length; i++) {
                try {
                    Structure[] childReps = messParent.getAll(childNames[i]);
                    for (int j = 0; j < childReps.length; j++) {
                        if (childReps[j] instanceof Message) {
                            values.add(childReps[j]);
                            if (getChildren(childReps[j]).size() > 0) {
                                values.addAll(getChildren(childReps[j]));
                            }
                        }
                        if (childReps[j] instanceof Group) {
                            values.add(childReps[j]);
                            allSegmentFromGroup.clear();
                            getAllSegmentsFromGroup((Group) childReps[j]);
                        }
                        if (childReps[j] instanceof Segment) {
                            SegmentModel sModel = new SegmentModel((Segment) childReps[j], messParent, i, j);
                            if (sModel.getTypes() != null && sModel.getTypes().length > 0) {
                                values.add(sModel);
                                if (getChildren(sModel).size() > 0) {
                                    values.addAll(getChildren(sModel));
                                }
                                if (!allSegmentsForMessage.contains(sModel)) {
                                    allSegmentsForMessage.add(sModel);
                                }
                            }
                        }
                    }
                    // values.addAll(Arrays.asList(childReps));
                } catch (HL7Exception e) {
                    e.printStackTrace();
                }
            }
            return values;
        }

        if (parentElement instanceof Segment) {
            values.clear();
            Segment segment = (Segment) parentElement;
            SegmentModel sm = new SegmentModel(segment, segment, 0, 0);
            TypeModel[] models = sm.getTypes();
            for (TypeModel model : models) {
                values.add(model);
                if (getChildren(model).size() > 0) {
                    values.addAll(getChildren(model));
                }
            }
            return values;
        }

        if (parentElement instanceof SegmentModel) {
            SegmentModel sm = (SegmentModel) parentElement;
            TypeModel[] models = sm.getTypes();
            for (TypeModel model : models) {
                values.add(model);
                if (getChildren(model).size() > 0) {
                    values.addAll(getChildren(model));
                }
            }
            return values;
        }

        if (parentElement instanceof TypeModel) {
            TypeModel tm = (TypeModel) parentElement;

            PrimitiveModel[] models = tm.getPrimitives();
            for (PrimitiveModel model : models) {
                values.add(model);
                if (getChildren(model).size() > 0) {
                    values.addAll(getChildren(model));
                }
            }
            return values;
            // return tm.getPrimitives();
        }

        if (parentElement instanceof Group) {
            values.clear();
            Group group = (Group) parentElement;
            String[] childNames = group.getNames();
            for (int i = 0; i < childNames.length; i++) {
                try {
                    Structure[] childReps = group.getAll(childNames[i]);
                    for (int j = 0; j < childReps.length; j++) {
                        if (childReps[j] instanceof Segment) {
                            SegmentModel sm = new SegmentModel((Segment) childReps[j], group, i, j);
                            if (sm.getTypes() != null && sm.getTypes().length > 0) {
                                values.add(sm);
                                if (getChildren(sm).size() > 0) {
                                    values.addAll(getChildren(sm));
                                }
                            }
                        } else {
                            values.add(childReps[j]);
                            if (getChildren(childReps[j]).size() > 0) {
                                values.addAll(getChildren(childReps[j]));
                            }
                        }
                    }
                    // values.addAll(Arrays.asList(childReps));
                } catch (HL7Exception e) {
                    e.printStackTrace();
                }
            }
            return values;
        }

        return values;
    }

    public List getFirstLevelChild(Object parentElement) {
        List values = new ArrayList();
        if (parentElement instanceof Message) {
            Message messParent = (Message) parentElement;
            String[] childNames = messParent.getNames();
            if (!values.isEmpty()) {
                values.clear();
            }
            for (int i = 0; i < childNames.length; i++) {
                try {
                    Structure[] childReps = messParent.getAll(childNames[i]);
                    for (int j = 0; j < childReps.length; j++) {
                        if (childReps[j] instanceof Message) {
                            values.add(childReps[j]);
                        }
                        if (childReps[j] instanceof Group) {
                            values.add(childReps[j]);
                            allSegmentFromGroup.clear();
                            getAllSegmentsFromGroup((Group) childReps[j]);
                        }
                        if (childReps[j] instanceof Segment) {
                            SegmentModel sModel = new SegmentModel((Segment) childReps[j], messParent, i, j);
                            if (sModel.getTypes() != null && sModel.getTypes().length > 0) {
                                values.add(sModel);
                                if (!allSegmentsForMessage.contains(sModel)) {
                                    allSegmentsForMessage.add(sModel);
                                }
                            }
                        }
                    }
                } catch (HL7Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return values;
    }

    public String getLabel(Object obj, boolean forMeatble) {
        String label = "";

        if (obj instanceof Message) {
            label = ((Message) obj).getName();
        }

        if (obj instanceof Segment) {
            label = ((Segment) obj).getName();
        }

        if (obj instanceof SegmentModel) {
            label = ((SegmentModel) obj).getDisplayName();
        }

        if (obj instanceof TypeModel) {
            label = ((TypeModel) obj).getDisplayName();
        }

        if (obj instanceof Group) {
            label = ((Group) obj).getName();
        }

        if (obj instanceof PrimitiveModel) {
            if (forMeatble) {
                label = ((PrimitiveModel) obj).getDisplayName();
                label = MetadataToolHelper.validateValue(label);
            } else {
                label = ((PrimitiveModel) obj).getDisplayName();
            }

        }

        return label;
    }
}
