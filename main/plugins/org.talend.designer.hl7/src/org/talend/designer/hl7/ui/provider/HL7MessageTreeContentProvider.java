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
package org.talend.designer.hl7.ui.provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.talend.designer.hl7.model.IModel;
import org.talend.designer.hl7.model.SegmentModel;
import org.talend.designer.hl7.model.TypeModel;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Group;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Segment;
import ca.uhn.hl7v2.model.Structure;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class HL7MessageTreeContentProvider implements ITreeContentProvider {

    private List<SegmentModel> allSegmentFromGroup = new ArrayList<SegmentModel>();

    private List<SegmentModel> allSegmentsForMessage = new ArrayList<SegmentModel>();

    public Object[] getChildren(Object parentElement) {
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
                            values.add((Group) childReps[j]);
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

        // if (parentElement instanceof Type) {
        // if (!values.isEmpty()) {
        // values.clear();
        // }
        // Type type = (Type) parentElement;
        //
        // if (Varies.class.isAssignableFrom(type.getClass())) {
        // Type data = ((Varies) type).getData();
        // values.add(data);
        // } else {
        // if (Composite.class.isAssignableFrom(type.getClass())) {
        // Composite composite = (Composite) type;
        // Type[] components = composite.getComponents();
        // for (int i = 0; i < components.length; i++) {
        // values.addAll(Arrays.asList(components[i]));
        // }
        // return values.toArray();
        // }
        //
        // if (type.getExtraComponents().numComponents() > 0) {
        // for (int i = 0; i < type.getExtraComponents().numComponents(); i++) {
        // return getChildren(type.getExtraComponents().getComponent(i));
        // }
        // }
        // }
        // }

        return new Object[0];
    }

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

    public Object getParent(Object element) {
        if (element instanceof Structure) {
            Structure structure = (Structure) element;
            if (structure.getParent() != null) {
                return structure.getParent();
            }
        }
        if (element instanceof IModel) {
            return ((IModel) element).getParent();
        }
        return null;
    }

    public boolean hasChildren(Object element) {
        Object[] children = getChildren(element);
        return children != null && children.length > 0;
    }

    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof Object[]) {
            return (Object[]) inputElement;
        }
        if (inputElement instanceof Collection) {
            return ((Collection) inputElement).toArray();
        }
        return new Object[0];
    }

    public void dispose() {

    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

    }

    public List<SegmentModel> getAllSegmentsForMessage() {
        return this.allSegmentsForMessage;
    }

}
