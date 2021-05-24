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
package org.talend.repository.json.ui.shadow;

import java.util.List;
import java.util.Map;

import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.runprocess.shadow.FileInputNode;
import org.talend.repository.ui.wizards.metadata.connection.files.json.EJsonReadbyMode;

/**
 * DOC wanghong class global comment. Detailled comment
 */
public class FileInputJsonNode4Input extends FileInputNode {

    private static final String INPUT_COMPONENT_NAME = "tFileInputJSON"; //$NON-NLS-1$

    private List<Map<String, String>> mapping = null;

    /**
     * Constructs a new FileInputNode.
     */
    public FileInputJsonNode4Input(String filename, String loopQuery, List<Map<String, String>> mapping, Integer loopLimit,
            String encoding, String readbyMode) {
        super(INPUT_COMPONENT_NAME, mapping.size());

        String limitLoop = ""; //$NON-NLS-1$
        if (loopLimit != null && loopLimit != 0) {
            limitLoop = Integer.toString(loopLimit);
        }

        String[] paramNames = null;
        if (EJsonReadbyMode.JSONPATH.getValue().equals(readbyMode)) {
            paramNames = new String[] {
                    "FILENAME", "JSON_LOOP_QUERY", "MAPPING_JSONPATH", "LIMIT", "ENCODING", "GENERATION_MODE", "READ_BY" ,"USE_LOOP_AS_ROOT" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
        } else {
            paramNames = new String[] { "FILENAME", "LOOP_QUERY", "MAPPING", "LIMIT", "ENCODING", "GENERATION_MODE", "READ_BY" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
        }
        // see bug 9785.
        Object[] paramValues = new Object[] { filename, loopQuery, mapping, limitLoop, encoding, "Dom4j", readbyMode ,false }; //$NON-NLS-1$
        // Object[] paramValues = new Object[] { filename, loopQuery, mapping, limitLoop, encoding, "Xerces" }; //$NON-NLS-1$

        IComponent component = ComponentsFactoryProvider.getInstance().get(INPUT_COMPONENT_NAME,
                ComponentCategory.CATEGORY_4_DI.getName());
        this.setElementParameters(component.createElementParameters(this));
        for (int i = 0; i < paramNames.length; i++) {
            if (paramValues[i] != null) {
                IElementParameter param = this.getElementParameter(paramNames[i]);
                if (param != null) {
                    param.setValue(paramValues[i]);
                }
            }
        }
    }

    public FileInputJsonNode4Input(String filename, String loopQuery, List<Map<String, String>> mapping, Integer loopLimit,
            String encoding) {
        this(filename, loopQuery, mapping, loopLimit, encoding, EJsonReadbyMode.XPATH.getValue());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.shadow.ShadowNode#getMappingList()
     */
    @Override
    public List<Map<String, String>> getMappingList() {
        return mapping;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.shadow.ShadowNode#setMappingList(java.util.List)
     */
    @Override
    public void setMappingList(List<Map<String, String>> mapping) {
        this.mapping = mapping;
    }
}
