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
package org.talend.hl7;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Segment {

    private String segmentType = "";

    private List<Field> fields;

    private boolean isFieldsSorted = false;

    public Segment(String segmentType) {
        this.segmentType = segmentType;
        fields = new ArrayList<Field>();
    }

    public void addField(Field field) {
        if (field == null) {
            return;
        }
        fields.add(field);
    }

    public String getSegmentType() {
        return this.segmentType;
    }

    public Field getField(int intfield, int rep, int component, int subComponent) {
        Field resultField = null;
        Field field = new Field(intfield, rep, component, subComponent);
        for (Field tmpfield : fields) {
            if (field.compareTo(tmpfield) == 0) {
                resultField = tmpfield;
                break;
            }
        }
        return resultField;
    }

    public List<Field> getFields() {
        if (!this.isFieldsSorted) {
            Collections.sort(fields);
            this.isFieldsSorted = true;
        }
        return fields;
    }

}
