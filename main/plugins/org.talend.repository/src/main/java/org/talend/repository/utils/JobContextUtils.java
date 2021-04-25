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
package org.talend.repository.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.talend.core.model.properties.ProcessItem;
import org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

public class JobContextUtils {

    public static Map<String, Map<String, String>> getContextsMap(ProcessItem processItem) {
        Map<String, Map<String, String>> contextValues = new HashMap<String, Map<String, String>>();
        ProcessType process = processItem.getProcess();
        if (process != null) {
            EList<?> context = process.getContext();
            if (context != null) {
                for (Object next : context) {
                    ContextType ct = (ContextType) next;
                    Map<String, String> contextParams = getContextParametersMapByGroup(processItem, ct.getName());
                    contextValues.put(ct.getName(), contextParams);
                }
            }
        }
        return contextValues;
    }

    public static Map<String, String> getDefaultContextMap(ProcessItem processItem) {
        if (processItem != null) {
            ProcessType process = processItem.getProcess();
            if (process != null) {
                return getContextParametersMapByGroup(processItem, process.getDefaultContext());
            }
        }
        return Collections.emptyMap();
    }

    private static Map<String, String> getContextParametersMapByGroup(ProcessItem processItem, String contextGroup) {
        ProcessType process = processItem.getProcess();
        if (process != null && contextGroup != null) {
            EList<?> context = process.getContext();
            if (context != null) {
                for (Object next : context) {
                    ContextType ct = (ContextType) next;
                    if (ct.getName().equals(contextGroup)) {
                        Map<String, String> contextParams = new HashMap<String, String>();
                        EList<ContextParameterType> params = ct.getContextParameter();
                        for (ContextParameterType param : params) {
                            contextParams.put(param.getName(), param.getRawValue());
                        }
                        return contextParams;
                    }
                }
            }
        }
        return Collections.emptyMap();
    }
}
