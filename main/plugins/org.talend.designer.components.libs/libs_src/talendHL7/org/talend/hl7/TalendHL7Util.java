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


public class TalendHL7Util {

    public static Field setValue(String path, String value) {
        int specs[] = parseSpecPath(path);
        Field field = new Field(specs[0], specs[1], specs[2], specs[3]);
        field.setValue(value);
        return field;
    }

    public static int[] parseSpecPath(String strSpec) {
        int[] result = new int[4];
        java.util.StringTokenizer tok = new java.util.StringTokenizer(strSpec, "-", false);
        if (tok.hasMoreTokens()) {
            tok.nextToken();
        }
        int i = 1;
        while (tok.hasMoreTokens()) {
            String tmp = tok.nextToken();
            if (i == 1) { // find the rep
                if (tmp.indexOf("(") > 0 && tmp.indexOf(")") > tmp.indexOf("(")) {
                    result[0] = Integer.parseInt(tmp.substring(0, tmp.indexOf("(")));
                    result[1] = Integer.parseInt(tmp.substring(tmp.indexOf("(") + 1, tmp.indexOf(")"))) - 1;
                } else {
                    result[0] = Integer.parseInt(tmp);
                    result[1] = 0;
                }
            } else {
                result[i] = Integer.parseInt(tmp);
            }
            i++;
        }

        if (result[1] < 0) {
            result[1] = 0;
        }

        if (result[2] == 0) {
            result[2] = 1;
        }
        if (result[3] == 0) {
            result[3] = 1;
        }
        return result;
    }

}
