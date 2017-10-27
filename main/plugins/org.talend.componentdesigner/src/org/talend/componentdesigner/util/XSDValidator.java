// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.componentdesigner.util;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.talend.componentdesigner.i18n.internal.Messages;
import org.talend.core.model.ModelPlugin;

/**
 * DOC xtan class global comment. Detailled comment
 */
public class XSDValidator {

    private static final Bundle XSD_CONTAINER_BUNDLE = Platform.getBundle(ModelPlugin.MODEL_PLUGIN_ID);

    private static final String XSD_PATH = "model/Component.xsd"; //$NON-NLS-1$

    private static final String SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage"; //$NON-NLS-1$

    private static final String SCHEMA_VALIDATOR = "http://java.sun.com/xml/jaxp/properties/schemaSource"; //$NON-NLS-1$

    public String checkXSD(String selectedFile) throws Exception {
        //
        try {
            URL url = XSD_CONTAINER_BUNDLE.getResource(XSD_PATH); //$NON-NLS-1$
            url = FileLocator.toFileURL(url);
            String componentXSDFile = url.getFile();
            //
            javax.xml.validation.SchemaFactory factorytXSDValidator = javax.xml.validation.SchemaFactory
                    .newInstance("http://www.w3.org/2001/XMLSchema"); //$NON-NLS-1$
            java.io.File schemaLocationtXSDValidator = new java.io.File(componentXSDFile);
            String message = null;

            TalendErrorHandler errorHandler = new TalendErrorHandler();

            try {
                javax.xml.validation.Schema schematXSDValidator = factorytXSDValidator.newSchema(schemaLocationtXSDValidator);
                javax.xml.validation.Validator validatortXSDValidator = schematXSDValidator.newValidator();
                java.io.File xmlfiletXSDValidator = new java.io.File(selectedFile);
                javax.xml.transform.Source sourcetXSDValidator = new javax.xml.transform.stream.StreamSource(xmlfiletXSDValidator);

                validatortXSDValidator.setErrorHandler(errorHandler);

                validatortXSDValidator.validate(sourcetXSDValidator);

            } catch (org.xml.sax.SAXParseException extXSDValidator) {
                errorHandler.error(extXSDValidator);
            }
            message = errorHandler.returnMessage();

            return message;

        } catch (Exception e) {
            org.talend.componentdesigner.exception.ExceptionHandler.process(e);
        }

        return null;
    }

    /**
     * DOC gke ValidateWithComponentXSDActionProvider class global comment. Detailled comment
     */
    class TalendErrorHandler implements org.xml.sax.ErrorHandler {

        String errorMessage = ""; //$NON-NLS-1$

        public void warning(org.xml.sax.SAXParseException ex) throws org.xml.sax.SAXException {
            errorMessage = errorMessage
                    + Messages.getString("ValidateComponentXMLActionProvider.warning", String.valueOf(ex.getLineNumber())) + " : " //$NON-NLS-1$//$NON-NLS-2$
                    + ex.getMessage();
        }

        public void error(org.xml.sax.SAXParseException ex) throws org.xml.sax.SAXException {
            errorMessage = errorMessage
                    + Messages.getString("ValidateComponentXMLActionProvider.error", String.valueOf(ex.getLineNumber())) + " : " //$NON-NLS-1$//$NON-NLS-2$
                    + ex.getMessage();
        }

        public void fatalError(org.xml.sax.SAXParseException ex) throws org.xml.sax.SAXException {

            errorMessage = errorMessage
                    + Messages.getString("ValidateComponentXMLActionProvider.fatalerror", String.valueOf(ex.getLineNumber())) + " : " //$NON-NLS-1$//$NON-NLS-2$
                    + ex.getMessage();

        }

        private String returnMessage() {
            return errorMessage == null ? "" : errorMessage; //$NON-NLS-1$
        }

    }

}
