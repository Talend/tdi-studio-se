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
package org.talend.designer.core.ui.editor.properties.controllers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultElement;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataColumn;
import org.talend.core.model.metadata.types.JavaType;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.designer.core.ui.editor.cmd.ChangeMetadataCommand;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.uniserv.AutoApi;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class RetrieveSchemaHelper {

    public static Command retrieveSchemasCommand(Node node) {

        IElementParameter param = node.getElementParameter("SCHEMA");
        IMetadataTable inputMeta = node.getMetadataFromConnector("FLOW");
        IMetadataTable inputMetaCopy = inputMeta.clone(true);

        IElementParameter outParam = node.getElementParameter("SCHEMA_OUT");
        IMetadataTable outputMeta = node.getMetadataFromConnector(outParam.getContext());
        IMetadataTable outputMetaCopy = outputMeta.clone(true);

        File xmlFile = new File(TalendTextUtils.removeQuotes(node.getElementParameter("PATH_JOBDEF").getValue().toString()));
        if (!xmlFile.exists())
            try {
                xmlFile.createNewFile();
            } catch (IOException e1) {
                ExceptionHandler.process(e1);
            }
        SAXReader saxReader = new SAXReader();
        Document document;
        AutoApi a = null;
        try {

            // get the schema file from server
            a = new AutoApi();
            String hostName = TalendTextUtils.removeQuotes(node.getElementParameter("HOSTNAME").getValue().toString());
            int port = Integer.parseInt(TalendTextUtils.removeQuotes(node.getElementParameter("PORT").getValue().toString()));
            String mandant = TalendTextUtils.removeQuotes(node.getElementParameter("MANDANT").getValue().toString());
            String userName = TalendTextUtils.removeQuotes(node.getElementParameter("USERNAME").getValue().toString());
            String passWord = TalendTextUtils.removeQuotes(node.getElementParameter("PASSWORD").getValue().toString());
            String jobDir = TalendTextUtils.removeQuotes(node.getElementParameter("JOB_DIR").getValue().toString());
            String jobName = TalendTextUtils.removeQuotes(node.getElementParameter("JOB_NAME").getValue().toString());
            String jobDef = TalendTextUtils.removeQuotes(node.getElementParameter("PATH_JOBDEF").getValue().toString());
            a.openConnection(hostName, port, mandant, userName, passWord);
            a.getJobDefinitionFile(jobDir, jobName, jobDef);

            document = saxReader.read(xmlFile);
            List inputList = document.selectNodes("//Job//Lines//Line//Steps//Input//Sources//Source//Format//Fields//Field");
            List inputMetaColumnList = new ArrayList<MetadataColumn>();
            for (int i = 0; i < inputList.size(); i++) {
                IMetadataColumn imc = new MetadataColumn();
                DefaultElement de = (DefaultElement) inputList.get(i);
                Element nameElement = de.element("Name");
                imc.setLabel(nameElement.getData().toString());
                Element lengthElement = de.element("Length");
                if (!"".equals(lengthElement.getData().toString()) && !"0".equals(lengthElement.getData().toString())) {
                    imc.setLength(Integer.parseInt(lengthElement.getData().toString()));
                }
                Element typeElement = de.element("Type");
                JavaType javaType = JavaTypesManager.getJavaTypeFromName(typeElement.getData().toString());
                if (javaType != null) {
                    imc.setTalendType(javaType.getId());
                } else {
                    imc.setTalendType(JavaTypesManager.STRING.getId());
                }
                inputMetaColumnList.add(imc);
            }
            inputMetaCopy.setListColumns(inputMetaColumnList);

            List outputList = document.selectNodes("//Job//Lines//Line//Steps//Output//Targets//Target//Format//Fields//Field");
            List outputMetaColumnList = new ArrayList<MetadataColumn>();
            for (int i = 0; i < outputList.size(); i++) {
                IMetadataColumn imc = new MetadataColumn();
                DefaultElement de = (DefaultElement) outputList.get(i);
                Element nameElement = de.element("Name");
                imc.setLabel(nameElement.getData().toString());
                Element lengthElement = de.element("Length");
                if (!"".equals(lengthElement.getData().toString()) && !"0".equals(lengthElement.getData().toString())) {
                    imc.setLength(Integer.parseInt(lengthElement.getData().toString()));
                }
                Element typeElement = de.element("Type");
                JavaType javaType = JavaTypesManager.getJavaTypeFromName(typeElement.getData().toString());
                if (javaType != null) {
                    imc.setTalendType(javaType.getId());
                } else {
                    imc.setTalendType(JavaTypesManager.STRING.getId());
                }
                outputMetaColumnList.add(imc);
            }
            outputMetaCopy.setListColumns(outputMetaColumnList);

            // set advanced setting info
            DefaultElement de = (DefaultElement) document
                    .selectObject("//Job//Lines//Line//Steps//Input//Sources//Source//Format//FileInfo//Record//FieldSeparator");
            int separator = Integer.parseInt(de.getData().toString());
            node.getElementParameter("IN_FIELD_SEP").setValue(
                    TalendTextUtils.addQuotes(new Character((char) separator).toString()));

            de = (DefaultElement) document
                    .selectObject("//Job//Lines//Line//Steps//Input//Sources//Source//Format//FileInfo//Record//HeaderRecordCount");
            node.getElementParameter("IN_HEADER_COUNT").setValue(de.getData().toString());

            de = (DefaultElement) document
                    .selectObject("//Job//Lines//Line//Steps//Input//Sources//Source//Format//FileInfo//FileLocation//Directory");
            node.getElementParameter("IN_DIR").setValue(TalendTextUtils.addQuotes(de.getData().toString()));

            de = (DefaultElement) document
                    .selectObject("//Job//Lines//Line//Steps//Input//Sources//Source//Format//FileInfo//FileLocation//FileName");
            node.getElementParameter("IN_FILENAME").setValue(TalendTextUtils.addQuotes(de.getData().toString()));

            de = (DefaultElement) document
                    .selectObject("//Job//Lines//Line//Steps//Input//Sources//Source//Format//FileInfo//FileLocation");
            node.getElementParameter("IN_MODE").setValue(de.attribute("Mode").getValue());

            de = (DefaultElement) document
                    .selectObject("//Job//Lines//Line//Steps//Output//Targets//Target//Format//FileInfo//Record//FieldSeparator");
            separator = Integer.parseInt(de.getData().toString());
            node.getElementParameter("OUT_FIELD_SEP").setValue(
                    TalendTextUtils.addQuotes(new Character((char) separator).toString()));

            de = (DefaultElement) document
                    .selectObject("//Job//Lines//Line//Steps//Output//Targets//Target//Format//FileInfo//Record//HeaderRecordCount");
            node.getElementParameter("OUT_HEADER_COUNT").setValue(de.getData().toString());

            de = (DefaultElement) document
                    .selectObject("//Job//Lines//Line//Steps//Output//Targets//Target//Format//FileInfo//FileLocation//Directory");
            node.getElementParameter("OUT_DIR").setValue(TalendTextUtils.addQuotes(de.getData().toString()));

            de = (DefaultElement) document
                    .selectObject("//Job//Lines//Line//Steps//Output//Targets//Target//Format//FileInfo//FileLocation//FileName");
            node.getElementParameter("OUT_FILENAME").setValue(TalendTextUtils.addQuotes(de.getData().toString()));

            de = (DefaultElement) document
                    .selectObject("//Job//Lines//Line//Steps//Output//Targets//Target//Format//FileInfo//FileLocation");
            node.getElementParameter("OUT_MODE").setValue(de.attribute("Mode").getValue());
        } catch (Exception e) {
            ExceptionHandler.process(e);
        } finally{
            try
            {
                a.closeConnection();
            }catch (Exception e){
                ExceptionHandler.process(e);
            }
        }

        CompoundCommand cc = new CompoundCommand();
        cc.add(new ChangeMetadataCommand(node, param, inputMeta, inputMetaCopy));
        cc.add(new ChangeMetadataCommand(node, param, outputMeta, outputMetaCopy));
        return cc;
    }
}
