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
package org.talend.webservice.helper;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;


/**
 * created by wwang on 2013-1-10
 * util to procss the path string
 *
 */
public class PathUtil {

    /**
     *
     * It is like String.indexOf(String),but not process the :anytype{*} part
     * @param path
     * @param sep
     * @return
     */
    public static int indexOfPath(String path,String sep) {
        if(path == null || sep == null || sep.length() == 0) {
            return -1;
        }

        if(sep.contains(":") || sep.contains("{") || sep.contains("}")) {
            throw new RuntimeException("separator can't contain :,{,}");
        }

        char first = sep.charAt(0);

        boolean isTypeDefine = false;

        for(int i=0;i<path.length();i++) {
            char c = path.charAt(i);

            if(c == '{') {
                isTypeDefine = true;
            } else if(c == '}') {
                isTypeDefine = false;
            }

            if(c != first) {
                while (++i < path.length()) {
                    char ch = path.charAt(i);

                    if(!isTypeDefine && (ch == first)) {//look for the first char that is not in type define substring.
                        break;
                    }

                    if(ch == '{') {
                        isTypeDefine = true;
                    } else if(ch == '}') {
                        isTypeDefine = false;
                    }

                };
            }

            if (i < path.length()) {//look for the rest that is not in type define substring.
                int j = i + 1;
                int end = j + sep.length() - 1;
                for (int k = 1; j < end && path.charAt(j) ==
                        sep.charAt(k); j++, k++);

                if (j == end) {
                    return i;
                }
            }
        }

        return -1;
    }

    /**
     *
     * It is like org.apache.commons.lang.StringUtils.split(String,String),but not process the :anytype{*} part
     * @param path
     * @param sep
     * @return
     */
    public static String[] splitPath(String path, String separatorChars) {
        if (path == null) {
            return null;
        }
        int len = path.length();
        if (len == 0) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        List list = new ArrayList();
        int i = 0, start = 0;
        boolean match = false;
        boolean lastMatch = false;

        boolean isTypeDefine = false;

        if (separatorChars == null) {
            // Null separator means use whitespace
            while (i < len) {
                char c = path.charAt(i);

                if(c == '{') {
                    isTypeDefine = true;
                } else if(c == '}') {
                    isTypeDefine = false;
                }

                if (Character.isWhitespace(c) && !isTypeDefine) {
                    if (match) {
                        lastMatch = true;
                        list.add(path.substring(start, i));
                        match = false;
                    }
                    start = ++i;
                    continue;
                }
                lastMatch = false;
                match = true;
                i++;
            }
        } else if (separatorChars.length() == 1) {
            // Optimise 1 character case
            char sep = separatorChars.charAt(0);
            while (i < len) {
                char c = path.charAt(i);

                if(c == '{') {
                    isTypeDefine = true;
                } else if(c == '}') {
                    isTypeDefine = false;
                }

                if ((c == sep) && !isTypeDefine) {
                    if (match) {
                        lastMatch = true;
                        list.add(path.substring(start, i));
                        match = false;
                    }
                    start = ++i;
                    continue;
                }
                lastMatch = false;
                match = true;
                i++;
            }
        } else {
            // standard case
            while (i < len) {
                char c = path.charAt(i);

                if(c == '{') {
                    isTypeDefine = true;
                } else if(c == '}') {
                    isTypeDefine = false;
                }

                if ((separatorChars.indexOf(c) >= 0) && !isTypeDefine) {
                    if (match) {
                        lastMatch = true;
                        list.add(path.substring(start, i));
                        match = false;
                    }
                    start = ++i;
                    continue;
                }
                lastMatch = false;
                match = true;
                i++;
            }
        }
        if (match) {
            list.add(path.substring(start, i));
        }
        return (String[]) list.toArray(new String[list.size()]);
    }

}
