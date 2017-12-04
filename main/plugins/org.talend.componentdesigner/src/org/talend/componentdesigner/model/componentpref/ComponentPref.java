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
package org.talend.componentdesigner.model.componentpref;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.talend.componentdesigner.PluginConstant;
import org.talend.componentdesigner.model.ILibEntry;
import org.talend.componentdesigner.model.enumtype.JetFileStamp;
import org.talend.componentdesigner.model.enumtype.LanguageType;
import org.talend.componentdesigner.model.enumtype.ResourceLanguageType;
import org.talend.componentdesigner.model.libentry.JarLibEntry;
import org.talend.componentdesigner.model.libentry.PmLibEntry;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @author rli
 * 
 */
public class ComponentPref {

    private static final String FIELDS_SEPARATOR = "#"; //$NON-NLS-1$

    private static final String EQUEL_FIELDS_SEPARATOR = "="; //$NON-NLS-1$

    private static final String INTERNAL_FIELDS_SEPARATOR = ";"; //$NON-NLS-1$

    private String name;

    private String longName;

    private LanguageType languageType;

    private List<ResourceLanguageType> resourceLanguageTypes;

    private List<JetFileStamp> jetFileStamps;

    private String imageURL;

    private ILibEntry[] libEntries;

    private String javaXMLFileName;

    private String perlXMLFileName;

    private Document javaXMLDocument;

    private Document perlXMLDocument;

    private Node[] javaImportNodes;

    private Node[] perlImportNodes;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for longName.
     * 
     * @return the longName
     */
    public String getLongName() {
        return longName;
    }

    /**
     * Sets the longName.
     * 
     * @param longName the longName to set
     */
    public void setLongName(String longName) {
        this.longName = longName;
    }

    /**
     * @return the languageType
     */
    public LanguageType getLanguageType() {
        return languageType;
    }

    /**
     * @param languageType the languageType to set
     */
    public void setLanguageType(LanguageType languageType) {
        this.languageType = languageType;
    }

    /**
     * @return the resourceLanguageTypes
     */
    public List<ResourceLanguageType> getResourceLanguageTypes() {
        return resourceLanguageTypes;
    }

    /**
     * @param resourceLanguageTypes the resourceLanguageTypes to set
     */
    public void setResourceLanguageTypes(List<ResourceLanguageType> resourceLanguageTypes) {
        this.resourceLanguageTypes = resourceLanguageTypes;
    }

    /**
     * @return the jetFileTypes
     */
    public List<JetFileStamp> getJetFileStamps() {
        return jetFileStamps;
    }

    /**
     * @param jetFileStamps the jetFileTypes to set
     */
    public void setJetFileStamps(List<JetFileStamp> jetFileStamps) {
        this.jetFileStamps = jetFileStamps;
    }

    /**
     * @return the imageURL
     */
    public String getImageURL() {
        return imageURL;
    }

    /**
     * @param imageURL the srcImageName to set
     */
    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    /**
     * @return the libFileURL
     */
    public ILibEntry[] getLibEntries() {
        return libEntries;
    }

    /**
     * @param libFileURL the libFileName to set
     */
    public void setLibEntries(ILibEntry[] libEntries) {
        this.libEntries = libEntries;
    }

    public static ComponentPref writeFromString(String s) {
        ComponentPref toReturn = new ComponentPref();
        try {
            String[] st = s.split(FIELDS_SEPARATOR, -1);
            int i = 0;
            toReturn.setName(st[i++]);
            toReturn.setLanguageType(LanguageType.find(st[i++]));
            String fieldString;
            String singleString;
            String[] internalFields;

            // 1. parse to jetFileTypes
            fieldString = st[i++];
            internalFields = fieldString.split(INTERNAL_FIELDS_SEPARATOR, 0);
            List<JetFileStamp> stampList = new ArrayList<JetFileStamp>();
            for (int j = 0; j < internalFields.length; j++) {
                singleString = internalFields[j];
                stampList.add(JetFileStamp.findFileStamp(singleString));
            }
            toReturn.setJetFileStamps(stampList);

            // 2. parse to resourceFileTypes
            fieldString = st[i++];
            internalFields = fieldString.split(INTERNAL_FIELDS_SEPARATOR, 0);
            List<ResourceLanguageType> resourceLangList = new ArrayList<ResourceLanguageType>();
            for (int j = 0; j < internalFields.length; j++) {
                singleString = internalFields[j];
                resourceLangList.add(ResourceLanguageType.find(singleString));
            }
            toReturn.setResourceLanguageTypes(resourceLangList);

            // 3. parse to resourceFileTypes
            fieldString = st[i++];
            if (!fieldString.equals(PluginConstant.EMPTY_STRING)) {
                toReturn.setImageURL(fieldString);
            }

            // 4. parse to library entries
            fieldString = st[i++];
            if (fieldString.equals(PluginConstant.EMPTY_STRING)) {
                return toReturn;
            }
            internalFields = fieldString.split(INTERNAL_FIELDS_SEPARATOR, 0);
            handleLibEntries(internalFields, toReturn);

        } catch (ArrayIndexOutOfBoundsException e) {
            // e.printStackTrace();
            org.talend.componentdesigner.exception.ExceptionHandler.process(e);
        }
        return toReturn;
    }

    private static void handleLibEntries(String[] internalFields, ComponentPref componentBean) {
        ILibEntry[] libArrays = new ILibEntry[internalFields.length];
        for (int j = 0; j < internalFields.length; j++) {
            String[] entryStrings = internalFields[j].split(EQUEL_FIELDS_SEPARATOR);
            int k = 0;
            String libName = entryStrings[k++];
            String isExternal = entryStrings[k++];
            String location = entryStrings[k++];
            Object obj = null;
            if (Boolean.valueOf(isExternal)) {
                obj = new Path(location);
            } else {
                obj = ResourcesPlugin.getWorkspace().getRoot().getProject(PluginConstant.COMPONENT_PROJECT).findMember(location);
            }
            if (libName.matches("(?i).*\\.(jar)\\b")) { //$NON-NLS-1$
                libArrays[j] = new JarLibEntry(obj);
            } else if (libName.matches("(?i).*\\.(pm)\\b")) { //$NON-NLS-1$
                libArrays[j] = new PmLibEntry(obj);
            } else {
                // Nothing
            }

        }
        componentBean.setLibEntries(libArrays);
    }

    /**
     * Parse fields of current class to string.
     * 
     * @return
     */
    public String readToString() {
        StringBuffer fields = new StringBuffer(getName() + FIELDS_SEPARATOR + getLanguageType().getNameSuffix()
                + FIELDS_SEPARATOR);
        // 1. append jetFileTypes
        if (this.getJetFileStamps().size() > 0) {
            for (JetFileStamp fileStamp : getJetFileStamps()) {
                fields.append(fileStamp.getFileStampName());
                fields.append(INTERNAL_FIELDS_SEPARATOR);
            }
        }
        fields.append(FIELDS_SEPARATOR);

        // 2. append resourceFileTypes.
        if (this.resourceLanguageTypes.size() > 0) {
            for (ResourceLanguageType resourceType : resourceLanguageTypes) {
                fields.append(resourceType.getLang());
                fields.append(INTERNAL_FIELDS_SEPARATOR);
            }
        }
        fields.append(FIELDS_SEPARATOR);

        // 3. append imageUrl String.
        if (this.imageURL == null) {
            fields.append(PluginConstant.EMPTY_STRING);
        } else {
            fields.append(imageURL);
        }
        fields.append(FIELDS_SEPARATOR);

        // 4. append library entries string.
        if (libEntries != null && this.libEntries.length > 0) {
            for (ILibEntry entry : this.libEntries) {
                fields.append(entry.getName());
                fields.append(EQUEL_FIELDS_SEPARATOR);
                fields.append(entry.isExternal());
                fields.append(EQUEL_FIELDS_SEPARATOR);
                fields.append(entry.getLocation());
                fields.append(INTERNAL_FIELDS_SEPARATOR);
            }
        }
        return fields.toString();
    }

    /**
     * Getter for javaXMLFileName.
     * 
     * @return the javaXMLFileName
     */
    public String getJavaXMLFileName() {
        return javaXMLFileName;
    }

    /**
     * Sets the javaXMLFileName.
     * 
     * @param xmlFileName the javaXMLFileName to set
     */
    public void setJavaXMLFileName(String xmlFileName) {
        this.javaXMLFileName = xmlFileName;
    }

    /**
     * Getter for perlXMLFileName.
     * 
     * @return the perlXMLFileName
     */
    public String getPerlXMLFileName() {
        return perlXMLFileName;
    }

    /**
     * Sets the perlXMLFileName.
     * 
     * @param xmlFileName the perlXMLFileName to set
     */
    public void setPerlXMLFileName(String xmlFileName) {
        this.perlXMLFileName = xmlFileName;
    }

    /**
     * Getter for javaXMLDocument.
     * 
     * @return the javaXMLDocument
     */
    public Document getJavaXMLDocument() {
        return javaXMLDocument;
    }

    /**
     * Sets the javaXMLDocument.
     * 
     * @param xmlDocument the javaXMLDocument to set
     */
    public void setJavaXMLDocument(Document xmlDocument) {
        this.javaXMLDocument = xmlDocument;
    }

    /**
     * Getter for perlXMLDocument.
     * 
     * @return the perlXMLDocument
     */
    public Document getPerlXMLDocument() {
        return perlXMLDocument;
    }

    /**
     * Sets the perlXMLDocument.
     * 
     * @param xmlDocument the perlXMLDocument to set
     */
    public void setPerlXMLDocument(Document xmlDocument) {
        this.perlXMLDocument = xmlDocument;
    }

    /**
     * Getter for javaImportNodes.
     * 
     * @return the javaImportNodes
     */
    public Node[] getJavaImportNodes() {
        return this.javaImportNodes;
    }

    /**
     * Sets the javaImportNodes.
     * 
     * @param javaImportNodes the javaImportNodes to set
     */
    public void setJavaImportNodes(Node[] javaImportNodes) {
        this.javaImportNodes = javaImportNodes;
    }

    /**
     * Getter for perImportNodes.
     * 
     * @return the perImportNodes
     */
    public Node[] getPerlImportNodes() {
        return this.perlImportNodes;
    }

    /**
     * Sets the perlImportNodes.
     * 
     * @param perlImportNodes the perlImportNodes to set
     */
    public void setPerlImportNodes(Node[] perlImportNodes) {
        this.perlImportNodes = perlImportNodes;
    }

    public Document getJavaXmlDocument() {
        // Node[] importNodes = this.getJavaImportNodes();
        //
        // Document documentCopy = null;
        //
        // if (importNodes != null && importNodes.length != 0) {
        // Node importsNode = importNodes[0].getParentNode();
        // for (Node node : importNodes) {
        // importsNode.removeChild(node);
        // }
        //
        // documentCopy = (Document) this.javaXMLDocument.cloneNode(true);
        //
        // for (Node node : importNodes) {
        // importsNode.appendChild(node);
        // }
        // } else {
        // documentCopy = this.javaXMLDocument;
        // }
        // return documentCopy;
        return this.javaXMLDocument;
    }

    public Document getPerlXmlDocument() {
        // Node[] importNodes = this.getPerlImportNodes();
        //
        // Document documentCopy = null;
        //
        // if (importNodes != null && importNodes.length != 0) {
        // Node importsNode = importNodes[0].getParentNode();
        // for (Node node : importNodes) {
        // importsNode.removeChild(node);
        // }
        //
        // documentCopy = (Document) this.perlXMLDocument.cloneNode(true);
        //
        // for (Node node : importNodes) {
        // importsNode.appendChild(node);
        // }
        // } else {
        // documentCopy = this.perlXMLDocument;
        // }
        // return documentCopy;
        return this.perlXMLDocument;

    }

    public Document getCurrentTypeDocument(String xmlType) {
        if (xmlType.equals(PluginConstant.JAVA_XML_SUFFIX)) {
            return this.getJavaXmlDocument();
        } else {
            return this.getPerlXmlDocument();
        }
    }

}
