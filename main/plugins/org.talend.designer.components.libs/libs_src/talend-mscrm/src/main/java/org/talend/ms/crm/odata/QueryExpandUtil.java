package org.talend.ms.crm.odata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QueryExpandUtil {

    public static List<String> getExpandConfig(List<String> fields, List<String> params) {
        if (fields == null || params == null || fields.size() != params.size()) {
            throw new IllegalArgumentException(
                    "Please make sure list of expands fields and params are not null and their size are same.");
        }



        if(fields.size() <= 0){
            return Collections.emptyList();
        }

        List<String> expands = new ArrayList<>();
        for(int i=0; i< fields.size(); i++){
            String name = fields.get(i);
            if("".equals(name.trim())){
                continue;
            }

            name = getSanitizedName(name);
            StringBuilder sb = new StringBuilder(name);
            final String param = params.get(i);
            if(!"".equals(param.trim())){
                sb.append('(');
                sb.append(param);
                sb.append(')');
            }
            expands.add(sb.toString());
        }

        return expands;
    }

    /**
     * expandable attrribute name are _entityid_value, should be transformed to entityid.
     * @param name
     * @return
     */
    public static String getSanitizedName(final String name){
        String sanitized = ('_' == name.charAt(0)) ? name.substring(1) : name;
        sanitized = (sanitized.endsWith("_value")) ? sanitized.substring(0, sanitized.length() - 6): sanitized;

        return sanitized;
    }

}
