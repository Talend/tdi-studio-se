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

import java.util.ArrayList;
import java.util.List;

import org.talend.core.model.utils.TalendTextUtils;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Composite;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.GenericComposite;
import ca.uhn.hl7v2.model.GenericPrimitive;
import ca.uhn.hl7v2.model.Primitive;
import ca.uhn.hl7v2.model.Structure;
import ca.uhn.hl7v2.model.Type;
import ca.uhn.hl7v2.model.Varies;
import ca.uhn.hl7v2.util.Terser;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class TypeModel extends AbstractStructureModel {

    private Type type;

    private int index;

    private int rep;

    private Terser terser;

    private PrimitiveModel[] primitives;

    public Type getType() {
        return this.type;
    }

    public TypeModel(Type type, Structure parent, int rep, int index) {
        this.type = type;
        this.parent = parent;
        this.rep = rep;
        this.index = index;
        generatePrimitive();
    }

    @Override
    protected void generateDisplayName() {
        String typeDisplay = type == null ? "" : TalendTextUtils.LBRACKET + this.type.getName() + TalendTextUtils.RBRACKET;
        this.displayName = getParent().getName() + "-" + index + "(1)" + typeDisplay; //$NON-NLS-1$ //$NON-NLS-2$

    }

    private void generatePrimitive() {
        if (type != null) {
            List<PrimitiveModel> pms = new ArrayList<PrimitiveModel>();
            int numComp = Terser.numComponents(this.type);
            for (int k = 1; k <= numComp; k++) {
                int numSubComp = Terser.numSubComponents(this.type, k);
                for (int m = 1; m <= numSubComp; m++) {
                    Primitive p = getPrimitive(this.type, k, m);
                    PrimitiveModel pm = new PrimitiveModel(this, p, k, m);
                    pms.add(pm);
                }
            }
            primitives = pms.toArray(new PrimitiveModel[0]);
        } else {
            PrimitiveModel pm = new PrimitiveModel(this, null, 1, 1);
            primitives = new PrimitiveModel[] { pm };
        }
    }

    public Primitive getPrimitive(Type type, int component, int subcomponent) {
        Type comp = getComponent(type, component);
        Type sub = getComponent(comp, subcomponent);
        return getPrimitive(sub);
    }

    private int numStandardComponents(Type t) {
        int n = 0;
        if (Varies.class.isAssignableFrom(t.getClass())) {
            n = numStandardComponents(((Varies) t).getData());
        } else if (Composite.class.isAssignableFrom(t.getClass())) {
            n = ((Composite) t).getComponents().length;
        } else {
            n = 1;
        }
        return n;
    }

    private Type getComponent(Type type, int comp) {
        Type ret = null;
        if (Varies.class.isAssignableFrom(type.getClass())) {
            Varies v = (Varies) type;

            try {
                if (comp > 1 && GenericPrimitive.class.isAssignableFrom(v.getData().getClass())) {
                    v.setData(new GenericComposite(v.getMessage()));
                }
            } catch (DataTypeException de) {
                String message = "Unexpected exception copying data to generic composite: " + de.getMessage();
                throw new Error(message);
            }

            ret = getComponent(v.getData(), comp);
        } else {
            if (Primitive.class.isAssignableFrom(type.getClass()) && comp == 1) {
                ret = type;
            } else if (GenericComposite.class.isAssignableFrom(type.getClass())
                    || (Composite.class.isAssignableFrom(type.getClass()) && comp <= numStandardComponents(type))) {
                // note that GenericComposite can return components > number of standard components

                try {
                    ret = ((Composite) type).getComponent(comp - 1);
                } catch (Exception e) {
                    throw new Error("Internal error: HL7Exception thrown on getComponent(x) where x < # standard components.", e);
                }
            } else {
                ret = type.getExtraComponents().getComponent(comp - numStandardComponents(type) - 1);
            }
        }
        return ret;
    }

    private Primitive getPrimitive(Type type) {
        Primitive p = null;
        if (Varies.class.isAssignableFrom(type.getClass())) {
            p = getPrimitive(((Varies) type).getData());
        } else if (Composite.class.isAssignableFrom(type.getClass())) {
            try {
                p = getPrimitive(((Composite) type).getComponent(0));
            } catch (HL7Exception e) {
                throw new Error("Internal error: HL7Exception thrown on Composite.getComponent(0).");
            }
        } else if (type instanceof Primitive) {
            p = (Primitive) type;
        }
        return p;
    }

    public PrimitiveModel[] getPrimitives() {
        return this.primitives;
    }
}
