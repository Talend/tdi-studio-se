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
package org.talend.hl7.test;

import org.talend.hl7.Field;
import org.talend.hl7.HL7Message;
import org.talend.hl7.Segment;
import org.talend.hl7.TalendHL7Util;

public class TestMain {

    public static void main(String[] args) throws Exception {

        String[][] paths = getMSH3();

        for (String[] arrPaths : paths) {
            for (String arrPath : arrPaths) {
                System.out.println(arrPath);
            }
        }
        HL7Message message = new HL7Message();
        Segment seg = new Segment("MSH");
        message.addSegment(seg);

        for (String[] path : paths) {
            Field field = TalendHL7Util.setValue(path[0], path[1]);
            seg.addField(field);
        }

        System.out.println(message.message());

    }

    public static String[][] getMSH1() {
        // expected: MSH|^~\&|||||20130917153756.409+0800||ADT^A54^ADT_A54|1101|T|2.6

        String[][] paths = { { "MSH-1", "|" }, { "MSH-2", "^~\\&" }, { "MSH-7", "20130917153756.409+0800" },
                { "MSH-9-1", "ADT" }, { "MSH-9-2", "A54" }, { "MSH-9-3", "ADT-A54" }, { "MSH-10", "1101" }, { "MSH-11", "T" },
                { "MSH-12", "2.6" } };
        return paths;
    }

    public static String[][] getMSH2() {
        // expected: MSH|^~\&|||||20130917153756.409+0800||ADT^A54&A55&56^ADT_A54|1101|T|2.6

        String[][] paths = { { "MSH-1", "|" }, { "MSH-9-1", "ADT" }, { "MSH-2", "^~\\&" },
                { "MSH-7", "20130917153756.409+0800" }, { "MSH-9-2", "A54" }, { "MSH-9-2-2", "A55" }, { "MSH-9-2-3", "A56" },
                { "MSH-9-3", "ADT-A54" }, { "MSH-10", "1101" }, { "MSH-11", "T" }, { "MSH-12", "2.6" } };
        return paths;
    }

    public static String[][] getMSH3() {
        // expected: MSH|^~\&|||||20130917153756.409+0800||ADT^A54&A55&56^ADT_A54|1101|T|2.6

        String[][] paths = { { "MSH-1", "|" }, { "MSH-9-1", "ADT" }, { "MSH-2", "^~\\&" },
                { "MSH-7", "20130917153756.409+0800" }, { "MSH-9-2", "A54" }, { "MSH-9(2)-2-2", "A55" },
                { "MSH-9(2)-2-3", "A56" }, { "MSH-9-3", "ADT-A54" }, { "MSH-10", "1101" }, { "MSH-11", "T" }, { "MSH-12", "2.6" } };
        return paths;
    }
}
