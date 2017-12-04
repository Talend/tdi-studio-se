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
package org.talend.designer.hl7.model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Segment;
import ca.uhn.hl7v2.model.Structure;
import ca.uhn.hl7v2.model.Type;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class SegmentModel extends AbstractStructureModel {

    private Segment seg;

    private int folderIndex, subfolderIndex;

    private TypeModel[] types;

    public SegmentModel(Segment seg, Structure parent, int folderIndex, int subfolderIndex) {
        this.seg = seg;
        this.parent = parent;
        this.folderIndex = folderIndex;
        this.subfolderIndex = subfolderIndex;
        generateDataTypes();
    }

    @Override
    protected void generateDisplayName() {
        this.displayName = seg.getName();
    }

    private void generateDataTypes() {
        int number = this.seg.numFields();
        ArrayList<TypeModel> datatypes = new ArrayList<TypeModel>();
        Method method = null;
        try {
            for (Method curMethod : seg.getClass().getDeclaredMethods()) {
                if (curMethod.getName().equals("createNewTypeWithoutReflection")) {
                    method = curMethod;
                    method.setAccessible(true);
                    break;
                }
            }

            if (method != null) {
                // only access we have to get all the types is reflection (protected method).
                // so add test of null in case this method doesn't exist, even if it should be in every subclass.
                for (int i = 0; i < number; i++) {

                    Type type = (Type) method.invoke(seg, i);
                    TypeModel tm = new TypeModel(type, seg, 0, i + 1);
                    datatypes.add(tm);
                }
            }
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (method == null) {
            // use old code in case the reflection didn't work.
            // but it means it won't get optional fields.
            try {
                int lastNotEmptyFiledIndex = 0;
                for (int i = 1; i < number; i++) {
                    Type[] reps = seg.getField(i);
                    if (reps.length > 0) {
                        lastNotEmptyFiledIndex = i;
                    }
                }
                for (int i = 1; i <= lastNotEmptyFiledIndex; i++) {
                    Type[] reps = seg.getField(i);
                    if (reps.length > 0) {
                        for (int j = 0; j < reps.length; j++) {
                            TypeModel tm = new TypeModel(reps[j], seg, j, i);
                            datatypes.add(tm);
                        }
                    } else {
                        // for empty column
                        TypeModel tm = new TypeModel(null, seg, 0, i);
                        datatypes.add(tm);
                    }

                }
            } catch (HL7Exception e) {
                e.printStackTrace();
            }
        }
        this.types = datatypes.toArray(new TypeModel[0]);
    }

    public TypeModel[] getTypes() {
        return this.types;
    }

    public Segment getSeg() {
        return this.seg;
    }

    public int getFolderIndex() {
        return this.folderIndex;
    }

    public int getSubfolderIndex() {
        return this.subfolderIndex;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SegmentModel) {
            return this.getSeg().getName().equals(((SegmentModel) obj).getSeg().getName());
        }
        return super.equals(obj);
    }

}
