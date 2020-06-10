// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.runprocess.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.talend.commons.exception.ExceptionHandler;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
 * DOC hwang  class global comment. Detailled comment
 */
public class JobVMArgumentsUtil {

    private static final List<String> EMPTY_STRING_LIST = Collections.unmodifiableList(new ArrayList<String>());

    public static final String[] DEFAULT_JVM_ARGS = new String[] { "-Xms256M", "-Xmx1024M" };

    public List<String> readString(String stringList) {
        if (stringList == null || "".equals(stringList)) { //$NON-NLS-1$
            return EMPTY_STRING_LIST;
        }
        ArrayList<String> result = new ArrayList<String>(50);
        try {
            JSONObject root = new JSONObject(stringList);
            Object obj =  root.get("JOB_RUN_VM_ARGUMENTS");//$NON-NLS-1$
            if(obj != null && (obj instanceof JSONArray)){
                JSONArray array = (JSONArray) obj;
                for(int i=0;i<array.length();i++){
                    result.add((String) array.get(i));
                }
            }
        } catch (JSONException e) {
            ExceptionHandler.process(e);
        }
        return result;
    }


    public String writeString(List<String> items) {
        JSONObject root = new JSONObject();
        JSONArray args = new JSONArray();
        int size = items.size();
        for (int i = 0; i < size; i++) {
            String vm = items.get(i).trim();
            args.put(vm);
        }
        try {
            root.put("JOB_RUN_VM_ARGUMENTS", args); //$NON-NLS-1$
        } catch (JSONException e) {
            ExceptionHandler.process(e);
        }
        return root.toString();
    }

}
