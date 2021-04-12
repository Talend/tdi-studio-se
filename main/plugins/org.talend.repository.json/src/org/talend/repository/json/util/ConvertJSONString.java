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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import net.sf.json.JSON;
import net.sf.json.JSONSerializer;
import net.sf.json.xml.XMLSerializer;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IProject;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.utils.workbench.resources.ResourceUtils;
import org.talend.core.model.general.Project;
import org.talend.repository.ProjectManager;

/**
 * DOC wanghong class global comment. Detailled comment
 */
public class ConvertJSONString {

    final static int Brace = 0; // {

    final static int Bracket = 1; // [

    private int barceType = -1;

    private String originalJsonString = "";

    private String originalLoopString = "";

    private String jsonString4XML = null;

    private String loopString4XML = null;

    public static String ROOT = "ROOT";

    public static String ROOT_OBJECT = "ROOT_OBJECT";

    private static String currentFlag = null;

    public void barceType() {

        for (int c = 0; c < originalJsonString.length(); ++c) {
            if (originalJsonString.charAt(c) == '{') {
                barceType = Brace;
                break;
            } else if (originalJsonString.charAt(c) == '[') {
                barceType = Bracket;
                break;
            }
        }
    }

    public void setJsonString(String originalJsonString) {
        this.originalJsonString = originalJsonString;
    }

    public void setLoopString(String originalLoopString) {
        this.originalLoopString = originalLoopString;
    }

    public String getJsonString4XML() {
        return jsonString4XML;
    }

    public String getLoopString4XML() {
        return loopString4XML;
    }

    public void generate() {
        barceType();
        jsonString4XML = originalJsonString;
        loopString4XML = originalLoopString;
        if (Brace == barceType) {
            if (isNeedAddRoot(originalJsonString)) {
                jsonString4XML = "{ \"root\": " + originalJsonString + " }";
                loopString4XML = "root" + originalLoopString;
                currentFlag = ROOT;
            }
        } else if (Bracket == barceType) {
            jsonString4XML = "{ \"root\" : { \"object\": " + originalJsonString + " } }";
            loopString4XML = "root/object" + originalLoopString;
            currentFlag = ROOT_OBJECT;
        }
    }

    public boolean isNeedAddRoot(String originalJsonString) {
        boolean isNeedAddRoot = false;
        net.sf.json.JSONObject jso = net.sf.json.JSONObject.fromObject(originalJsonString);
        String jsonKey = "";
        Object firstObject = null;
        if (jso.names().size() == 1) {
            jsonKey = jso.names().get(0).toString();
            firstObject = jso.get(jsonKey);
        }
        if (jso.size() > 1
                || (firstObject != null && firstObject instanceof net.sf.json.JSONArray && ((net.sf.json.JSONArray) firstObject)
                        .size() > 1)) {
            isNeedAddRoot = true;
        }
        return isNeedAddRoot;
    }

    public static String getCurrentFlag() {
        return currentFlag;
    }

    public static void main(String[] args) {
        ConvertJSONString convertJSON = new ConvertJSONString();
        de.odysseus.staxon.json.JsonXMLConfig jsonConfig = new de.odysseus.staxon.json.JsonXMLConfigBuilder().multiplePI(false)
                .build();
        de.odysseus.staxon.json.JsonXMLInputFactory jsonXMLInputFactory = new de.odysseus.staxon.json.JsonXMLInputFactory(
                jsonConfig);
        javax.xml.stream.XMLOutputFactory xmlOutputFactory = javax.xml.stream.XMLOutputFactory.newInstance();

        // org.dom4j.io.SAXReader reader_tExtractJSONFields_1 = new org.dom4j.io.SAXReader();
        // org.dom4j.Document doc_tExtractJSONFields_1 = null;
        // java.util.HashMap xmlNameSpaceMap_tExtractJSONFields_1 = new java.util.HashMap<String, String>();
        // org.dom4j.XPath x_tExtractJSONFields_1 = null;
        // java.util.List<org.dom4j.tree.AbstractNode> nodeList_tExtractJSONFields_1 = null;
        // boolean isStructError = true;
        java.io.ByteArrayOutputStream outStream = new java.io.ByteArrayOutputStream();
        java.io.ByteArrayInputStream inStream = null;
        File file = new File("E:/builds/source_file.json");
        try {
            FileInputStream input = new FileInputStream(file);
            String jsonStr = IOUtils.toString(input);

            // String loopQuery_tExtractJSONFields_1 = "/rcp.authorized_plug-ins";
            // String oraginalJsonStr_tExtractJSONFields_1 = jsonStr_tExtractJSONFields_1;
            convertJSON.setJsonString(jsonStr);
            // cjs_tExtractJSONFields_1.setLoopString(loopQuery_tExtractJSONFields_1);

            convertJSON.generate();
            jsonStr = convertJSON.getJsonString4XML();
            // loopQuery_tExtractJSONFields_1 = cjs_tExtractJSONFields_1.getLoopString4XML();
            inStream = new ByteArrayInputStream(jsonStr.getBytes());
            javax.xml.stream.XMLEventReader xmlEventReader = jsonXMLInputFactory.createXMLEventReader(inStream);
            javax.xml.stream.XMLEventWriter xmLEventWriter = xmlOutputFactory.createXMLEventWriter(outStream);
            xmLEventWriter.add(xmlEventReader);
            // convert json string to xml
            String xmlStr = outStream.toString();
            // System.out.println(xmlStr_tExtractJSONFields_1);

            FileWriter writer = new FileWriter("D:/CLOUD1.xml");
            writer.write(xmlStr);
            writer.flush();
            writer.close();
            // xmlStr_tExtractJSONFields_1 =
            // "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><rcp.authorized_plug-ins><s1>4</s1><upk>false</upk><psn>org.eclipse.jface.databinging.n1_de</psn><v>3.6.0.v20100814043401</v><pa>MDM1,TIS,DI</pa><sd>false</sd></rcp.authorized_plug-ins><rcp.authorized_plug-ins><s1>1</s1><upk>false</upk><psn>org.eclipse.jface.databinging.n1_de</psn><v>3.6.0.v20100814043401</v><pa>TDQ,Test</pa><sd>false</sd></rcp.authorized_plug-ins></root>";
            xmLEventWriter.close();
            xmlEventReader.close();

            // doc_tExtractJSONFields_1 = reader_tExtractJSONFields_1.read(new
            // java.io.StringReader(xmlStr_tExtractJSONFields_1));
            // x_tExtractJSONFields_1 = doc_tExtractJSONFields_1.createXPath(loopQuery_tExtractJSONFields_1);
            // x_tExtractJSONFields_1.setNamespaceURIs(xmlNameSpaceMap_tExtractJSONFields_1);
            // nodeList_tExtractJSONFields_1 = (java.util.List<org.dom4j.tree.AbstractNode>) x_tExtractJSONFields_1
            // .selectNodes(doc_tExtractJSONFields_1);
            // isStructError = false;
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (java.lang.Exception ex_tExtractJSONFields_1) {
            ex_tExtractJSONFields_1.printStackTrace();
        } finally {
            try {
                outStream.close();
                if (inStream != null) {
                    inStream.close();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

    }

    public static String changeJsonToXml(String jsonPath) {
        Project project = ProjectManager.getInstance().getCurrentProject();
        IProject fsProject = null;
        try {
            fsProject = ResourceUtils.getProject(project);
        } catch (PersistenceException e2) {
            ExceptionHandler.process(e2);
        }
        if (fsProject == null) {
            return jsonPath;
        }
        String temPath = fsProject.getLocationURI().getPath() + File.separator + "temp" + File.separator + "jsonwizard"
                + File.separator;
        try {
            File file = new File(jsonPath);
            String filename = file.getName().replaceAll("\\.", "_");
            FileInputStream input = new FileInputStream(file);
            String jsonData = IOUtils.toString(input);
            XMLSerializer serializer = new XMLSerializer();
            JSON json = JSONSerializer.toJSON(jsonData);
            serializer.setRootName("JSONRoot");
            serializer.setTypeHintsEnabled(false);
            String xml = serializer.write(json);
            File xmlFolder = new File(temPath);
            if (!xmlFolder.exists()) {
                xmlFolder.mkdirs();
            }
            temPath = temPath + filename + ".xml";
            FileWriter writer = new FileWriter(temPath);
            writer.write(xml);
            writer.flush();
            writer.close();

        } catch (IOException e) {
            ExceptionHandler.process(e);
            return jsonPath;
        }
        return temPath;
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}
