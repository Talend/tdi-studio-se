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
package org.talend.repository.json.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import net.sf.json.JSON;
import net.sf.json.JSONSerializer;
import net.sf.json.xml.XMLSerializer;

import org.apache.commons.io.IOUtils;

/**
 * DOC wanghong class global comment. Detailled comment
 */
public class Test {

    /**
     * DOC wanghong Comment method "main".
     *
     * @param args
     */
    public static void main(String[] args) {

        // InputStream is = Test.class.getResourceAsStream("D:/CLOUD_BPM_EE_MPX_generated.json");

        try {
            File file = new File("D:/CLOUD_BPM_EE_MPX_generated.json");
            FileInputStream input = new FileInputStream(file);
            String jsonData = IOUtils.toString(input);
            XMLSerializer serializer = new XMLSerializer();
            JSON json = JSONSerializer.toJSON(jsonData);
            serializer.setRootName("JSONRoot");
            serializer.setTypeHintsEnabled(false);
            String xml = serializer.write(json);
            // System.out.println(xml);
            FileWriter writer = new FileWriter("D:/CLOUD.xml");
            writer.write(xml);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
