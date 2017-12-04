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
package org.talend.componentdesigner.manager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.dom4j.DocumentException;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.talend.componentdesigner.ComponentDesigenerPlugin;
import org.talend.componentdesigner.ImageLib;
import org.talend.componentdesigner.PluginConstant;
import org.talend.componentdesigner.model.ILibEntry;
import org.talend.componentdesigner.model.componentpref.ComponentPref;
import org.talend.componentdesigner.model.enumtype.JetFileStamp;
import org.talend.componentdesigner.model.enumtype.ResourceLanguageType;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author rli
 * 
 */
public class ComponentFolderManager {

    private static String xmlSUFFIX = ".xml"; //$NON-NLS-1$

    private ComponentPref componentPref;

    private IProject project;

    private String componentFolderName;

    public void generateComponentContent(ComponentPref componentPref, IProject project) throws CoreException, IOException {
        this.componentPref = componentPref;
        this.project = project;
        this.componentFolderName = componentPref.getName();
        this.creatJetFiles();
        this.creatResourceFiles();
        this.creatXmlFile();
        this.addComponentImage();
        this.addComponentLib();
    }

    private void creatResourceFiles() throws CoreException {
        for (ResourceLanguageType resourceType : this.componentPref.getResourceLanguageTypes()) {
            creatResourceFile(resourceType);
        }
    }

    /**
     * DOC slanglois Comment method "creatResourceFile".
     * 
     * @param resourceType
     * @throws CoreException
     */
    private void creatResourceFile(ResourceLanguageType resourceType) throws CoreException {
        Properties properties = new Properties();

        // String s = "";
        String fileName = componentPref.getName() + resourceType.getNameSuffix();
        IFile f = creatEmptyFile(fileName);

        // add property for NAME, LONG NAME AND FAMILY
        properties.setProperty("LONG_NAME", componentPref.getLongName()); //$NON-NLS-1$

        // add property for HELP
        properties.setProperty("HELP", "org.talend.help." + componentPref.getName()); //$NON-NLS-1$ //$NON-NLS-2$

        // add properties for each PARAMETER of component file
        extractNodes("PARAMETER", properties); //$NON-NLS-1$
        // add properties for each RETURN of component
        extractNodes("RETURN", properties); //$NON-NLS-1$

        ByteArrayOutputStream propertiesOutputStream = null;
        InputStream inputStream = null;
        try {
            propertiesOutputStream = new ByteArrayOutputStream();
            properties.store(propertiesOutputStream, ""); //$NON-NLS-1$
            propertiesOutputStream.flush();
            propertiesOutputStream.close();
            inputStream = new ByteArrayInputStream(propertiesOutputStream.toByteArray());
            f.setContents(inputStream, true, false, null);
        } catch (IOException e) {
            // e.printStackTrace();
            org.talend.componentdesigner.exception.ExceptionHandler.process(e);
        } finally {
            try {
                if (propertiesOutputStream != null) {
                    propertiesOutputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                // e.printStackTrace();
                org.talend.componentdesigner.exception.ExceptionHandler.process(e);
            }
        }
    }

    /**
     * DOC mhirt Comment method "extractNodes".
     * 
     * @param stringToReturn
     * @param tagToFind
     * @return
     */
    private void extractNodes(String tagToFind, Properties properties) {
        NodeList nl = componentPref.getJavaXMLDocument().getElementsByTagName(tagToFind);
        if (nl != null) {
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                if (n != null) {
                    NamedNodeMap attributes = n.getAttributes();
                    n = attributes.getNamedItem("NAME"); //$NON-NLS-1$
                    if ((n != null) && (n.getNodeValue() != null) && (n.getNodeValue().compareTo("") != 0)) { //$NON-NLS-1$
                        properties.setProperty(n.getNodeValue().toUpperCase() + ".NAME", n.getNodeValue()); //$NON-NLS-1$
                    }
                }
            }
        }
        nl = null;
        nl = componentPref.getPerlXMLDocument().getElementsByTagName(tagToFind);
        if (nl != null) {
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                if (n != null) {
                    NamedNodeMap attributes = n.getAttributes();
                    n = attributes.getNamedItem("NAME"); //$NON-NLS-1$
                    if ((n != null) && (n.getNodeValue() != null) && (n.getNodeValue().compareTo("") != 0)) { //$NON-NLS-1$
                        properties.setProperty(n.getNodeValue().toUpperCase() + ".NAME", n.getNodeValue()); //$NON-NLS-1$
                    }
                }
            }
        }
    }

    /**
     * Copy the file resources from source component folder to destination component folder, but the .jar/.pm file will
     * not copy.
     * 
     * @param project
     * @param srcComponentFolderName
     * @param desComponentFolderName
     * @throws CoreException, IOException
     */
    public void copyComponent(IProject project, IPath srcComponentFolder, String desComponentFolderName) throws CoreException,
            IOException {
        IFolder srcFolder = project.getFolder(srcComponentFolder);
        IFolder desFolder = project.getFolder(desComponentFolderName);
        String srcComponentFolderName = srcComponentFolder.lastSegment();
        String newDestinationFileName;
        if (!desFolder.exists()) {
            desFolder.create(false, true, null);
        }
        for (IResource resource : srcFolder.members()) {
            if (!(resource instanceof IFile)) {
                continue;
            }
            IFile file = (IFile) resource;
            if (file.getFileExtension().equals("jar") || file.getFileExtension().equals("pm")) { //$NON-NLS-1$ //$NON-NLS-2$
                continue;
            }
            if (file.exists()) {
                newDestinationFileName = desComponentFolderName
                        + file.getName().substring(
                                file.getName().indexOf(srcComponentFolderName) + srcComponentFolderName.length());
                file.copy(desFolder.getFile(newDestinationFileName).getFullPath(), false, null);

                // modify NAME's value and HELP's value in properties file.
                if (file.getFileExtension().equals("properties")) { //$NON-NLS-1$

                    Properties properties = new Properties();
                    InputStream propertiesInputStream = desFolder.getFile(newDestinationFileName).getContents();
                    properties.load(propertiesInputStream);
                    propertiesInputStream.close();

                    properties.setProperty("NAME", desComponentFolderName); //$NON-NLS-1$
                    String help = properties.getProperty("HELP"); //$NON-NLS-1$
                    properties.setProperty("HELP", help.substring(0, help.lastIndexOf(".") + 1) + desComponentFolderName); //$NON-NLS-1$ //$NON-NLS-2$

                    ByteArrayOutputStream propertiesOutputStream = new ByteArrayOutputStream();
                    properties.store(propertiesOutputStream, ""); //$NON-NLS-1$
                    propertiesOutputStream.close();

                    InputStream inputStream = new ByteArrayInputStream(propertiesOutputStream.toByteArray());

                    desFolder.getFile(newDestinationFileName).setContents(inputStream, true, false, null);
                    inputStream.close();

                }
            }
        }
    }

    /**
     * creat all the language type jet files, include begin,main or end files.
     * 
     * @throws CoreException
     * @throws IOException
     */
    private void creatJetFiles() throws CoreException, IOException {
        for (JetFileStamp fileStamp : this.componentPref.getJetFileStamps()) {
            creatJetLanguageFile(fileStamp);
        }
    }

    private void creatJetLanguageFile(JetFileStamp fileStamp) throws CoreException, IOException {
        String fileName = componentPref.getName() + "_" + fileStamp.getFileStampName(); //$NON-NLS-1$
        switch (componentPref.getLanguageType()) {
        case BOTHLANGUAGETYPE:
            String[] suffixs = componentPref.getLanguageType().getFileSuffix().split(";"); //$NON-NLS-1$
            for (String suffix : suffixs) {
                creatTemplateJetFile(fileName + suffix);
            }
            break;
        default:
            creatTemplateJetFile(fileName + componentPref.getLanguageType().getFileSuffix());
        }
    }

    private void creatXmlFile() throws CoreException {

        // String fileName = componentPref.getName();
        switch (componentPref.getLanguageType()) {
        case BOTHLANGUAGETYPE:
            String[] suffixs = componentPref.getLanguageType().getNameSuffix().split(";"); //$NON-NLS-1$
            for (String nameSuffix : suffixs) {
                String xmlFileName = this.componentFolderName + nameSuffix + xmlSUFFIX;
                IFile file = creatEmptyFile(xmlFileName);
                Document document = componentPref.getCurrentTypeDocument(nameSuffix);
                writeXMLContent(file, document, "UTF-8"); //$NON-NLS-1$

            }
            break;
        default:
            String nameSuffix = componentPref.getLanguageType().getNameSuffix();
            String xmlFileName = this.componentFolderName + nameSuffix + xmlSUFFIX;
            IFile file = creatEmptyFile(xmlFileName);
            Document document = componentPref.getCurrentTypeDocument(nameSuffix);
            writeXMLContent(file, document, "UTF-8"); //$NON-NLS-1$
        }

    }

    private void addComponentImage() throws CoreException, FileNotFoundException {
        if (componentPref.getImageURL() == null) {
            copyFileFromSrc(ImageLib.getImageInputStream(ImageLib.COMPONENT_DEFAULT), componentPref.getName() + "_icon32.png"); //$NON-NLS-1$
        }
        copyFileFromSrc(componentPref.getImageURL());
    }

    private void creatTemplateJetFile(String fileName) throws CoreException, IOException {
        // FileInputStream templateFileStream = (FileInputStream) ComponentFolderManager.class
        // .getResourceAsStream("template.javajet");
        InputStream templateFileStream = null;
        try {
            templateFileStream = ComponentDesigenerPlugin.getDefault().getBundle().getEntry("/data/template.javajet") //$NON-NLS-1$
                    .openStream();
        } catch (FileNotFoundException e) {
            // e.printStackTrace();
            org.talend.componentdesigner.exception.ExceptionHandler.process(e);
        }
        // .getResourceAsStream("template.javajet");
        copyFileFromSrc(templateFileStream, fileName);
    }

    private IFile creatEmptyFile(String fileName) throws CoreException {
        IFolder folder = project.getFolder(this.componentFolderName);
        if (!folder.exists()) {
            folder.create(false, true, null);
        }
        IFile newFile = folder.getFile(fileName);
        if (!newFile.exists()) {
            newFile.create(new ByteArrayInputStream(new byte[0]), false, null);
        }
        return newFile;
    }

    private void copyFileFromSrc(String srcURL, String... particularPath) throws FileNotFoundException, CoreException {
        if (srcURL == null || srcURL.equals(PluginConstant.EMPTY_STRING)) {
            return;
        }

        IPath path = new Path(srcURL);
        IFolder folder = project.getFolder(this.componentFolderName);
        if (!folder.exists()) {
            folder.create(false, true, null);
        }

        String projectFolder = this.componentFolderName;
        if (particularPath != null) {
            for (String mParticularPath : particularPath) {
                projectFolder += "/" + mParticularPath; //$NON-NLS-1$
                folder = project.getFolder(projectFolder);
                if (!folder.exists()) {
                    folder.create(false, true, null);
                }
            }
        } else {
            projectFolder = this.componentFolderName;
        }

        IFile file = folder.getFile(path.lastSegment());
        if (file.exists()) {
            return;
        }
        FileInputStream fileStream = new FileInputStream(srcURL);
        file.create(fileStream, false, null);
    }

    private void copyFileFromSrc(InputStream inputStream, String fileName) throws CoreException {
        if (inputStream == null) {
            return;
        }
        IFolder folder = project.getFolder(this.componentFolderName);
        if (!folder.exists()) {
            folder.create(false, true, null);
        }
        IFile file = folder.getFile(fileName);
        if (file.exists()) {
            return;
        }
        file.create(inputStream, false, null);
    }

    private void addComponentLib() throws FileNotFoundException, CoreException {
        addLibEntries(componentPref.getLibEntries());

    }

    private void addLibEntries(ILibEntry[] libEntries) throws FileNotFoundException, CoreException {
        if (libEntries == null) {
            return;
        }
        for (int i = 0; i < libEntries.length; i++) {
            if (libEntries[i].isExternal()) {
                if (libEntries[i].getType() == ILibEntry.PM) {
                    copyFileFromSrc(libEntries[i].getLocation(), "modules", this.componentFolderName); //$NON-NLS-1$
                } else {
                    copyFileFromSrc(libEntries[i].getLocation());
                }
            }
        }
    }

    /**
     * DOC slanglois Comment method "writeXMLContent".
     * 
     * @param iFile
     * @param document
     * @param enCode
     * @throws CoreException
     */
    private void writeXMLContent(IFile iFile, Document document, String enCode) throws CoreException {
        PrintWriter pw = null;
        XMLWriter writer = null;
        byte[] byteArray = null;

        // get xml content as inputstream
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = null;
        try {
            transformer = tf.newTransformer();
        } catch (TransformerConfigurationException e1) {
            // e1.printStackTrace();
            org.talend.componentdesigner.exception.ExceptionHandler.process(e1);
        }
        DOMSource source = new DOMSource(document);
        transformer.setOutputProperty(OutputKeys.ENCODING, enCode);
        transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$

        ByteArrayOutputStream sw = new ByteArrayOutputStream();
        pw = new PrintWriter(sw);
        StreamResult result = new StreamResult(pw);
        try {
            transformer.transform(source, result);
        } catch (TransformerException e1) {
            // e1.printStackTrace();
            org.talend.componentdesigner.exception.ExceptionHandler.process(e1);
        }
        try {
            sw.flush();
        } catch (IOException e1) {
            // e1.printStackTrace();
            org.talend.componentdesigner.exception.ExceptionHandler.process(e1);
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
        byteArray = sw.toByteArray();

        // format the xml content
        SAXReader saxReader = new SAXReader();

        org.dom4j.Document dom4jDocument = null;
        try {
            dom4jDocument = saxReader.read(new ByteArrayInputStream(byteArray));
        } catch (DocumentException e1) {
            // e1.printStackTrace();
            org.talend.componentdesigner.exception.ExceptionHandler.process(e1);
        }

        /** format the output like the webBrowser */

        OutputFormat format = OutputFormat.createPrettyPrint();

        /** give the xml encoding */

        format.setEncoding(enCode);

        sw = new ByteArrayOutputStream();

        try {
            writer = new XMLWriter(sw, format);
        } catch (UnsupportedEncodingException e1) {
            // e1.printStackTrace();
            org.talend.componentdesigner.exception.ExceptionHandler.process(e1);
        }

        try {
            writer.write(dom4jDocument);
            writer.flush();
        } catch (IOException e1) {
            // e1.printStackTrace();
            org.talend.componentdesigner.exception.ExceptionHandler.process(e1);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    // e.printStackTrace();
                    org.talend.componentdesigner.exception.ExceptionHandler.process(e);
                }
            }
        }
        byteArray = sw.toByteArray();

        // write content
        iFile.setContents(new ByteArrayInputStream(byteArray), true, false, null);

    }
}
