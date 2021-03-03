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
package org.talend.expressionbuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.Bundle;
import org.talend.commons.runtime.model.expressionbuilder.Variable;
import org.talend.commons.runtime.xml.XSDValidator;
import org.talend.expressionbuilder.i18n.Messages;
import org.talend.utils.files.FileUtils;
import org.talend.utils.xml.XmlUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * yzhang class global comment. Detailled comment <br/>
 *
 * $Id: ExpressionFileOperation.java 下午02:44:14 2007-8-3 +0000 (2007-8-3) yzhang $
 *
 */
public class ExpressionFileOperation {

    private static final String SCHEMA_VALIDATOR = "http://java.sun.com/xml/jaxp/properties/schemaSource"; //$NON-NLS-1$

    private static final String SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage"; //$NON-NLS-1$

    private static final String SCHEMA_XSD = "talend_expression_variables_schema.xsd"; //$NON-NLS-1$

    private static final String PLUGIN_ID = "org.talend.expressionbuilder"; //$NON-NLS-1$

    /**
     * yzhang Comment method "savingExpression".
     *
     * @return
     * @throws IOException
     * @throws ParserConfigurationException
     */
    public boolean saveExpressionToFile(File file, List<Variable> variables, String expressionContent)
            throws IOException, ParserConfigurationException {

        final DocumentBuilderFactory fabrique = XmlUtils.getSecureDocumentBuilderFactory();

        final Bundle b = Platform.getBundle(PLUGIN_ID);
        final URL url = FileLocator.toFileURL(FileLocator.find(b, new Path(SCHEMA_XSD), null));
        final File schema = new File(url.getPath());

        fabrique.setAttribute(SCHEMA_LANGUAGE, "http://www.w3.org/2001/XMLSchema"); //$NON-NLS-1$
        fabrique.setAttribute(SCHEMA_VALIDATOR, schema);
        fabrique.setValidating(true);

        final DocumentBuilder analyseur = fabrique.newDocumentBuilder();
        analyseur.setErrorHandler(new ErrorHandler() {

            public void error(final SAXParseException exception) throws SAXException {
                throw exception;
            }

            public void fatalError(final SAXParseException exception) throws SAXException {
                throw exception;
            }

            public void warning(final SAXParseException exception) throws SAXException {
                throw exception;
            }

        });

        Document document = analyseur.newDocument();
        Element expressionElement = document.createElement("expression"); //$NON-NLS-1$
        document.appendChild(expressionElement);

        Attr content = document.createAttribute(Messages.getString("ExpressionFileOperation.content")); //$NON-NLS-1$
        content.setNodeValue(expressionContent);
        expressionElement.setAttributeNode(content);

        for (Variable variable : variables) {

            Element variableElement = document.createElement("variable"); //$NON-NLS-1$
            expressionElement.appendChild(variableElement);

            Attr name = document.createAttribute(Messages.getString("ExpressionFileOperation.name")); //$NON-NLS-1$
            name.setNodeValue(variable.getName());
            variableElement.setAttributeNode(name);

            Attr value = document.createAttribute(Messages.getString("ExpressionFileOperation.value")); //$NON-NLS-1$
            value.setNodeValue(variable.getValue());
            variableElement.setAttributeNode(value);

            Attr talendType = document.createAttribute(Messages.getString("ExpressionFileOperation.talendType")); //$NON-NLS-1$
            //$NON-NLS-1$
            talendType.setNodeValue(variable.getTalendType());
            variableElement.setAttributeNode(talendType);

            Attr nullable = document.createAttribute(Messages.getString("ExpressionFileOperation.nullable")); //$NON-NLS-1$
            //$NON-NLS-1$
            nullable.setNodeValue(String.valueOf(variable.isNullable()));
            variableElement.setAttributeNode(nullable);

        }

        FileWriter writer = new FileWriter(file);
        FileUtils.writeXMLFile(document, writer);
        writer.close();
        return true;

    }

    /**
     * yzhang Comment method "getExpressionFromFile".
     *
     * @param file
     * @return
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public List importExpressionFromFile(File file, Shell shell) throws IOException, ParserConfigurationException,
            SAXException {
        if (file != null) {
            List list = new ArrayList();

            if (!file.isFile()) {
                openDialog(shell);
                return list;
            } else {
                final Bundle b = Platform.getBundle(PLUGIN_ID);
                final URL url = FileLocator.toFileURL(FileLocator.find(b, new Path(SCHEMA_XSD), null));
                final File schema = new File(url.getPath());
                final Document document = XSDValidator.checkXSD(file, schema);

                final NodeList expressionNodes = document.getElementsByTagName(Messages
                        .getString("ExpressionFileOperation.expression")); //$NON-NLS-1$
                if (expressionNodes.getLength() == 1) {

                    Node expressionNode = expressionNodes.item(0);
                    NamedNodeMap epxressionAttrs = expressionNode.getAttributes();
                    Node contentNode = epxressionAttrs.getNamedItem(Messages
                            .getString("ExpressionFileOperation.content")); //$NON-NLS-1$
                    list.add(contentNode.getNodeValue());

                }

                final NodeList variableNodes = document.getElementsByTagName(Messages
                        .getString("ExpressionFileOperation.variable")); //$NON-NLS-1$
                for (int i = 0; i < variableNodes.getLength(); i++) {

                    Node variableNode = variableNodes.item(i);
                    NamedNodeMap varAttrs = variableNode.getAttributes();

                    Node nameNode = varAttrs.getNamedItem(Messages.getString("ExpressionFileOperation.name")); //$NON-NLS-1$
                    Node valueNode = varAttrs.getNamedItem(Messages.getString("ExpressionFileOperation.value")); //$NON-NLS-1$
                    Node talendTypeNode = varAttrs.getNamedItem(Messages
                            .getString("ExpressionFileOperation.talendType")); //$NON-NLS-1$
                    Node nullableNode = varAttrs.getNamedItem(Messages.getString("ExpressionFileOperation.nullable")); //$NON-NLS-1$

                    Variable variable = new Variable();
                    variable.setName(nameNode.getNodeValue());
                    variable.setValue(valueNode.getNodeValue());
                    variable.setTalendType(talendTypeNode.getNodeValue());
                    String s = nullableNode.getNodeValue();
                    Boolean boo = Boolean.valueOf(s);
                    if (boo == null) {
                        boo = false;
                    }
                    variable.setNullable(boo);
                    list.add(variable);

                }

                return list;
            }
        }
        return null;

    }

    private void openDialog(Shell shell) {
        MessageBox box = new MessageBox(shell, SWT.ICON_WARNING);
        box.setText(Messages.getString("ExpressionFileOperation.warn")); //$NON-NLS-1$
        box.setMessage(Messages.getString("OpenDialog.Warn")); //$NON-NLS-1$
        int count = box.open();
    }

}
