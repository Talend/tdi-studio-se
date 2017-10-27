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

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.w3c.dom.Document;

/**
 * DOC rli class global comment. Detailled comment
 */
public class XMLUtil {

    /**
     * save the xml file.
     * 
     * @param fileName
     */
    public static void toSave(String fileName, Document document, String enCode) {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            DOMSource source = new DOMSource(document);
            transformer.setOutputProperty(OutputKeys.ENCODING, enCode);
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
            PrintWriter pw = new PrintWriter(new FileOutputStream(fileName));
            StreamResult result = new StreamResult(pw);
            transformer.transform(source, result);
        } catch (TransformerException mye) {
            // mye.printStackTrace();
            org.talend.componentdesigner.exception.ExceptionHandler.process(mye);
        } catch (IOException exp) {
            // exp.printStackTrace();
            org.talend.componentdesigner.exception.ExceptionHandler.process(exp);
        }
    }

    /**
     * format the exist xml file.
     * 
     * @param filename
     * @return
     */
    public static int formatXMLFile(String filename, String enCode) {

        int returnValue = 0;

        try {

            SAXReader saxReader = new SAXReader();

            org.dom4j.Document document = saxReader.read(new File(filename));

            XMLWriter writer = null;

            /** format the output like the webBrowser */

            OutputFormat format = OutputFormat.createPrettyPrint();

            /** give the xml encoding */

            format.setEncoding(enCode);

            writer = new XMLWriter(new FileWriter(new File(filename)), format);

            writer.write(document);

            writer.close();

            /** succes will retun 1 */

            returnValue = 1;

        } catch (Exception ex) {

            // ex.printStackTrace();
            org.talend.componentdesigner.exception.ExceptionHandler.process(ex);

        }

        return returnValue;

    }
}
