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
package org.talend.designer.xmlmap.ui.tabs.table;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.jface.viewers.CellEditor;
import org.talend.commons.utils.data.bean.IBeanPropertyAccessors;
import org.talend.core.model.metadata.types.JavaType;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.ui.metadata.celleditor.JavaTypeComboValueAdapter;
import org.talend.designer.xmlmap.util.XmlMapUtil;

/**
 * DOC talend class global comment. Detailled comment
 */
public class TreeSchemaJavaTypeComboValueAdapter<B> extends JavaTypeComboValueAdapter<B> {

    private JavaType[] filteredTypes;

    private static String[] javaTypesLabelsArray = new String[0];

    public TreeSchemaJavaTypeComboValueAdapter(JavaType defaultJavaType, IBeanPropertyAccessors<B, Boolean> nullableAccessors) {
        super(defaultJavaType, nullableAccessors);
    }

    public Object getOriginalTypedValue(final CellEditor cellEditor, Object cellEditorTypedValue) {
        JavaType[] javaTypes = getFilterdJavaTypes();
        int i = (Integer) cellEditorTypedValue;
        if (i >= 0) {
            return javaTypes[i].getId();
        }
        // else {
        // return null;
        // }
        throw new IllegalStateException("No selection is invalid"); //$NON-NLS-1$
    }

    private JavaType[] getFilterdJavaTypes() {
        if (filteredTypes == null) {
            JavaType[] javaTypes = JavaTypesManager.getJavaTypes();
            List<JavaType> types = new ArrayList<JavaType>();
            for (int i = 0; i < javaTypes.length; i++) {
                if (!XmlMapUtil.DOCUMENT.equals(javaTypes[i].getId())) {
                    types.add(javaTypes[i]);
                }
            }
            filteredTypes = types.toArray(new JavaType[types.size()]);
        }
        return filteredTypes;
    }

    public String[] getTalendTypesLabels() {
        JavaType[] javaTypes = getFilterdJavaTypes();
        int lstSize = javaTypes.length;
        if (javaTypesLabelsArray.length != lstSize) {
            javaTypesLabelsArray = new String[lstSize];
            for (int i = 0; i < lstSize; i++) {
                javaTypesLabelsArray[i] = javaTypes[i].getLabel();
            }
        }
        return (String[]) ArrayUtils.clone(javaTypesLabelsArray);

    }

}
