// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.documentation.generation;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.Bundle;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.image.ImageUtils;
import org.talend.commons.ui.runtime.image.ImageUtils.ICON_SIZE;
import org.talend.commons.utils.VersionUtils;
import org.talend.commons.utils.io.FilesUtils;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.context.ContextUtils;
import org.talend.core.model.general.Project;
import org.talend.core.model.genhtml.FileCopyUtils;
import org.talend.core.model.genhtml.HTMLDocUtils;
import org.talend.core.model.genhtml.HTMLHandler;
import org.talend.core.model.genhtml.IHTMLDocConstants;
import org.talend.core.model.genhtml.IJobSettingConstants;
import org.talend.core.model.genhtml.XMLHandler;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.documentation.generation.DocumentationPathProvider;
import org.talend.core.model.utils.ParameterValueUtil;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.core.repository.document.IDocumentationGenerator;
import org.talend.core.runtime.process.TalendProcessOptionConstants;
import org.talend.core.runtime.repository.item.ItemProductKeys;
import org.talend.core.runtime.util.ItemDateParser;
import org.talend.core.service.IMRProcessService;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.core.ui.documentation.generation.ExternalNodeComponentHandler;
import org.talend.core.ui.documentation.generation.InternalNodeComponentHandler;
import org.talend.core.ui.images.CoreImageProvider;
import org.talend.designer.codegen.ITalendSynchronizer;
import org.talend.designer.core.ICamelDesignerCoreService;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.designer.core.model.utils.emf.talendfile.ConnectionType;
import org.talend.designer.core.model.utils.emf.talendfile.ContextParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ParametersType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.documentation.i18.Messages;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.repository.ProjectManager;
import org.talend.repository.documentation.ExportFileResource;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.RepositoryConstants;
import org.w3c.dom.NodeList;
import org.w3c.dom.css.CSSRuleList;
import org.xml.sax.SAXException;

/**
 * This class is used for generating HTML file.
 * 
 * $Id: XMLGenerator.java 2007-3-8,下午01:09:34 ftang $
 * 
 */
public class HTMLDocGenerator implements IDocumentationGenerator {

    private Map<String, List> targetConnectionMap = null;

    private Map<String, List> sourceConnectionMap = null;

    private Map<String, String> picFilePathMap;

    private List<Map> mapList;

    private Map<String, ConnectionItem> repositoryConnectionItemMap;

    private static Map<String, String> repositoryDBIdAndNameMap;

    private IDesignerCoreService designerCoreService;

    private Map<String, Object> externalNodeHTMLMap = new HashMap();

    private ERepositoryObjectType repositoryObjectType;

    private ERepositoryObjectType itemType;

    private static Map<Integer, ByteArrayOutputStream> logoImageCache = new HashMap<Integer, ByteArrayOutputStream>();

    private static String userDocImageOldPath = ""; //$NON-NLS-1$

    private Project project;

    private boolean generateExtraSetting = true;

    private boolean generateStatsLogsSetting = true;

    public HTMLDocGenerator(Project project, ERepositoryObjectType docType, ERepositoryObjectType itemType) {
        this.project = project;
        this.designerCoreService = CorePlugin.getDefault().getDesignerCoreService();
        this.mapList = designerCoreService.getMaps();
        this.repositoryConnectionItemMap = mapList.get(0);
        this.repositoryDBIdAndNameMap = mapList.get(1);
        this.repositoryObjectType = docType;
        this.itemType = itemType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.documentation.generation.IDocumentationGenerator#getRepositoryObjectType()
     */
    @Override
    public ERepositoryObjectType getRepositoryObjectType() {
        return this.repositoryObjectType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.repository.documentation.generation.IDocumentationGenerator#getItemType()
     */
    @Override
    public ERepositoryObjectType getItemType() {
        return itemType;
    }

    @Override
    public void generateHTMLFile(ExportFileResource resource) {
        generateHTMLFile(resource, null);
    }

    /*
     * This method is used for generating HTML file base on an instance of <code>ExportFileResource</code> (non-Javadoc)
     * 
     * @see
     * org.talend.repository.documentation.generation.IDocumentationGenerator#generateHTMLFile(org.talend.repository
     * .documentation.ExportFileResource)
     */
    @Override
    public void generateHTMLFile(ExportFileResource resource, String cssFile) {
        try {

            // Store all pictures' path.
            List<URL> picList = new ArrayList<URL>(5);

            String jobName = resource.getItem().getProperty().getLabel();

            String jobVersion = resource.getItem().getProperty().getVersion();

            String tempFolderPath = checkTempDirIsExists(resource);

            handleXMLFile(resource, tempFolderPath);

            String picFolderPath = checkPicDirIsExists(resource, tempFolderPath);

            final Bundle b = Platform.getBundle(IHTMLDocConstants.REPOSITORY_PLUG_IN_ID);

            final URL xslFileUrl = FileLocator.toFileURL(FileLocator
                    .find(b, new Path(IHTMLDocConstants.MAIN_XSL_FILE_PATH), null));
            // final URL logoFileUrl = FileLocator.toFileURL(FileLocator.find(b,
            // new Path(IHTMLDocConstants.LOGO_FILE_PATH), null));

            File logoFile = new File(picFolderPath + File.separatorChar + IHTMLDocConstants.TALEND_LOGO_FILE_NAME);
            saveLogoImage(SWT.IMAGE_JPEG, logoFile);

            String xslFilePath = xslFileUrl.getPath();
            // String logoFilePath = logoFileUrl.getPath();
            // FileCopyUtils.copy(logoFilePath, picFolderPath + File.separatorChar
            // + IHTMLDocConstants.TALEND_LOGO_FILE_NAME);

            // if import a css template, generate a new xsl file
            String temXslPath = null;
            File file = new File(xslFilePath);
            temXslPath = HTMLDocUtils.getTmpFolder() + File.separator + file.getName();
            generateXslFile(xslFilePath, temXslPath, cssFile, tempFolderPath);
            // if no new xls generated, use default xsl
            File temFile = new File(temXslPath);
            if (!temFile.exists()) {
                temXslPath = null;
            }
            if (temXslPath == null) {
                temXslPath = xslFilePath;
            }

            picList.add(logoFile.toURL());

            Set keySet = picFilePathMap.keySet();
            for (Object key : keySet) {
                String value = picFilePathMap.get(key);
                FileCopyUtils.copy(value, picFolderPath + File.separatorChar + key);
                picList.add(new File(picFolderPath + File.separatorChar + key).toURL());
            }

            byte[] innerContent = null;
            ProcessType processType = null;
            if (resource.getItem() instanceof ProcessItem) {
                processType = ((ProcessItem) resource.getItem()).getProcess();
                innerContent = (byte[]) processType.getScreenshots().get("process"); //$NON-NLS-1$
            } else if (resource.getItem() instanceof JobletProcessItem) {
                processType = ((JobletProcessItem) resource.getItem()).getJobletProcess();
                innerContent = (byte[]) processType.getScreenshots().get("process"); //$NON-NLS-1$
            }

            if (innerContent != null) {
                ImageDescriptor imagedesc = ImageUtils.createImageFromData(innerContent);
                String picName = jobName + "_" + jobVersion + IHTMLDocConstants.JOB_PREVIEW_PIC_SUFFIX; //$NON-NLS-1$
                ImageUtils.save(imagedesc.createImage(), picFolderPath + File.separatorChar + picName, SWT.IMAGE_PNG);
                picList.add(new File(picFolderPath + File.separatorChar + picName).toURL());
            }
            for (NodeType node : (List<NodeType>) processType.getNode()) {
                String uniqueName = ""; //$NON-NLS-1$
                for (Object o : node.getElementParameter()) {
                    if (o instanceof ElementParameterType) {
                        if ("UNIQUE_NAME".equals(((ElementParameterType) o).getName())) { //$NON-NLS-1$
                            uniqueName = ((ElementParameterType) o).getValue();
                            break;
                        }
                    }
                }
                byte[] screenshot = (byte[]) processType.getScreenshots().get(uniqueName);
                if (screenshot != null && screenshot.length != 0) {
                    ImageDescriptor imagedesc = ImageUtils.createImageFromData(screenshot);
                    String picName = IHTMLDocConstants.EXTERNAL_NODE_PREVIEW + uniqueName
                            + IHTMLDocConstants.JOB_PREVIEW_PIC_SUFFIX;
                    ImageUtils.save(imagedesc.createImage(), picFolderPath + File.separatorChar + picName, SWT.IMAGE_PNG);
                    picList.add(new File(picFolderPath + File.separatorChar + picName).toURL());
                }
            }

            List<URL> resultFiles = parseXML2HTML(tempFolderPath, jobName + "_" + jobVersion, temXslPath); //$NON-NLS-1$

            addResources(resource, resultFiles);

            resource.addResources(IHTMLDocConstants.PIC_FOLDER_NAME, picList);

            // List<URL> externalList = getExternalHtmlPath();
            // resource.addResources(IHTMLDocConstants.EXTERNAL_FOLDER_NAME, externalList);

        } catch (Exception e) {
            e.printStackTrace();
            ExceptionHandler.process(e);

        }

        targetConnectionMap = null;
        sourceConnectionMap = null;
    }

    private void generateXslFile(String resource, String xslfile, String cssfile, String folder) {
        try {

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document document = builder.parse(new File(resource));
            org.w3c.dom.Element rootElement = document.getDocumentElement();

            NodeList list = rootElement.getElementsByTagName("style"); //$NON-NLS-1$
            org.w3c.dom.Element element = (org.w3c.dom.Element) list.item(0);
            String value = element.getChildNodes().item(0).getNodeValue();
            if (value != null) {
                if (folder != null) {
                    CSSParserUtils.createCssFile(value, folder + File.separator + "default.css"); //$NON-NLS-1$
                }
                if (cssfile != null && !cssfile.equals("")) { //$NON-NLS-1$
                    if (folder != null) {
                        String cssName = new File(cssfile).getName();
                        if (cssName.equalsIgnoreCase("default.css")) { //$NON-NLS-1$
                            cssName = "User_" + cssName; //$NON-NLS-1$
                        }
                        File file = new File(folder + File.separator + cssName);
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                        FileCopyUtils.copy(cssfile, folder + File.separator + cssName);
                    }
                    CSSRuleList ruleList = CSSParserUtils.parserCSSSelectors(null, cssfile);
                    if (ruleList == null) {
                        return;
                    } else {
                        String newValue = CSSParserUtils.generateCssStyle(cssfile, ruleList, value);
                        element.getChildNodes().item(0).setNodeValue(newValue);
                        // replace the old value and generate a new xsl file
                        DOMSource ds = new DOMSource(document);
                        StreamResult sr = new StreamResult(new File(xslfile));
                        TransformerFactory.newInstance().newTransformer().transform(ds, sr);
                    }
                }
            }

        } catch (ParserConfigurationException e) {
            ExceptionHandler.process(e);
        } catch (SAXException e) {
            ExceptionHandler.process(e);
        } catch (IOException e) {
            ExceptionHandler.process(e);
        } catch (TransformerConfigurationException e) {
            ExceptionHandler.process(e);
        } catch (TransformerException e) {
            ExceptionHandler.process(e);
        } catch (TransformerFactoryConfigurationError e) {
            ExceptionHandler.process(e);
        }
    }

    /*
     * This method is used for generating HTML file base on an instance of <code>ExportFileResource</code> (non-Javadoc)
     * 
     * @see
     * org.talend.repository.documentation.generation.IDocumentationGenerator#generateDocumentation(org.talend.repository
     * .documentation.ExportFileResource, java.lang.String, java.lang.String[])
     */
    @Override
    public void generateDocumentation(ExportFileResource resource, String targetPath, String... jobVersion) throws Exception {
        // Store all pictures' path.
        List<URL> picList = new ArrayList<URL>(5);

        String jobName = resource.getItem().getProperty().getLabel();

        String jobPath = resource.getItem().getProperty().getItem().getState().getPath();

        // Used for generating/updating all jobs' documentaiton only.
        String typePath = new Path(this.repositoryObjectType.getFolder()).lastSegment();
        if (jobName != null && !(jobName).equals(typePath.toLowerCase()) && targetPath.endsWith(typePath.toLowerCase())) {
            targetPath = targetPath + IPath.SEPARATOR + jobPath + IPath.SEPARATOR + jobName;
        }
        String version = ""; //$NON-NLS-1$

        // Checks if the job's version is specified, see it on "Export documentation" Dialog:
        if (jobVersion != null && jobVersion.length == 1) {
            version = jobVersion[0];
        } else {
            version = resource.getItem().getProperty().getVersion();
        }
        targetPath = targetPath + "_" + version; //$NON-NLS-1$

        File file = new File(targetPath);

        // Delete if folde is existing.
        if (file.exists()) {
            FilesUtils.removeFolder(file, true);
        }

        file.mkdirs();

        handleXMLFile(resource, targetPath, jobVersion);

        String picFolderPath = checkPicDirIsExists(resource, targetPath);

        // Gets the "org.talend.repository" plug-in:
        final Bundle b = Platform.getBundle("org.talend.repository"); //$NON-NLS-1$

        final URL xslFileUrl = FileLocator.toFileURL(FileLocator.find(b, new Path(IHTMLDocConstants.MAIN_XSL_FILE_PATH), null));
        // final URL logoFileUrl = FileLocator.toFileURL(FileLocator.find(b,
        // new Path(IHTMLDocConstants.LOGO_FILE_PATH), null));

        File logoFile = new File(picFolderPath + File.separatorChar + IHTMLDocConstants.TALEND_LOGO_FILE_NAME);
        saveLogoImage(SWT.IMAGE_JPEG, logoFile);

        String xslFilePath = xslFileUrl.getPath();
        // String logoFilePath = logoFileUrl.getPath();
        // FileCopyUtils.copy(logoFilePath, picFolderPath + File.separatorChar
        // + IHTMLDocConstants.TALEND_LOGO_FILE_NAME);

        // if set css file in preference.
        boolean isCheck = CorePlugin.getDefault().getPreferenceStore().getBoolean(ITalendCorePrefConstants.USE_CSS_TEMPLATE);
        String cssFile = CorePlugin.getDefault().getPreferenceStore().getString(ITalendCorePrefConstants.CSS_FILE_PATH);
        String temXslPath = null;
        if (isCheck && cssFile != null && !cssFile.equals("")) { //$NON-NLS-1$
            String tempFolderPath = checkTempDirIsExists(resource);
            temXslPath = tempFolderPath + File.separator + (new File(xslFilePath)).getName();
            File temXslFile = new File(temXslPath);
            if (temXslFile.exists()) {
                temXslFile.delete();
            }
            generateXslFile(xslFilePath, temXslPath, cssFile, null);
        }
        // if no new xls generated, use default xsl
        if (temXslPath != null) {
            File temFile = new File(temXslPath);
            if (!temFile.exists()) {
                temXslPath = xslFilePath;
            }
        } else {
            temXslPath = xslFilePath;
        }

        picList.add(logoFile.toURL());
        // Property property = item.getProperty();
        // String jobName = property.getLabel();
        // String jobVersion = property.getVersion();
        byte[] innerContent = null;
        ProcessType processType = null;
        if (resource.getItem() instanceof ProcessItem) {
            processType = ((ProcessItem) resource.getItem()).getProcess();
            innerContent = (byte[]) processType.getScreenshots().get("process"); //$NON-NLS-1$
        } else if (resource.getItem() instanceof JobletProcessItem) {
            processType = ((JobletProcessItem) resource.getItem()).getJobletProcess();
            innerContent = (byte[]) processType.getScreenshots().get("process"); //$NON-NLS-1$
            ;
        }

        if (innerContent != null) {
            String picName = jobName + "_" + version + IHTMLDocConstants.JOB_PREVIEW_PIC_SUFFIX; //$NON-NLS-1$
            ImageUtils.save(innerContent, picFolderPath + File.separatorChar + picName, SWT.IMAGE_PNG);
            picList.add(new File(picFolderPath + File.separatorChar + picName).toURL());

            // need to generate another pic for pdf
            ByteArrayInputStream bais = new ByteArrayInputStream(innerContent);
            Image pdfImage = new Image(null, bais);
            int width = pdfImage.getImageData().width;
            int percent = 22 * 32 * 100 / width;
            ImageUtils.save(ImageUtils.scale(pdfImage, percent), picFolderPath + File.separatorChar + "pdf_" + picName, //$NON-NLS-1$
                    SWT.IMAGE_PNG);
            picList.add(new File(picFolderPath + File.separatorChar + "pdf_" + picName).toURL()); //$NON-NLS-1$
            pdfImage.dispose();
        }
        if (processType != null) {
            for (NodeType node : (List<NodeType>) processType.getNode()) {
                String uniqueName = ""; //$NON-NLS-1$
                for (Object o : node.getElementParameter()) {
                    if (o instanceof ElementParameterType) {
                        if ("UNIQUE_NAME".equals(((ElementParameterType) o).getName())) { //$NON-NLS-1$
                            uniqueName = ((ElementParameterType) o).getValue();
                            break;
                        }
                    }
                }
                byte[] screenshot = (byte[]) processType.getScreenshots().get(uniqueName);
                if (screenshot != null && screenshot.length != 0) {
                    String picName = IHTMLDocConstants.EXTERNAL_NODE_PREVIEW + uniqueName
                            + IHTMLDocConstants.JOB_PREVIEW_PIC_SUFFIX;
                    ImageUtils.save(screenshot, picFolderPath + File.separatorChar + picName, SWT.IMAGE_PNG);
                    picList.add(new File(picFolderPath + File.separatorChar + picName).toURL());

                    // need to generate externalNode pic for pdf
                    ByteArrayInputStream bais = new ByteArrayInputStream(screenshot);
                    Image pdfImage = new Image(null, bais);
                    int width = pdfImage.getImageData().width;
                    int percent = 22 * 32 * 100 / width;
                    ImageUtils.save(ImageUtils.scale(pdfImage, percent), picFolderPath + File.separatorChar + "pdf_" + picName, //$NON-NLS-1$
                            SWT.IMAGE_PNG);
                    picList.add(new File(picFolderPath + File.separatorChar + picName).toURL());
                    pdfImage.dispose();
                }
            }
        }

        Set keySet = picFilePathMap.keySet();
        for (Object key : keySet) {
            String value = picFilePathMap.get(key);

            FileCopyUtils.copy(value, picFolderPath + File.separatorChar + key);

            picList.add(new File(picFolderPath + File.separatorChar + key).toURL());
        }

        List<URL> resultFiles = parseXml2HtmlPdf(targetPath, jobName + "_" + version, temXslPath); //$NON-NLS-1$

        resource.addResources(resultFiles);

        resource.addResources(IHTMLDocConstants.PIC_FOLDER_NAME, picList);

        HTMLDocUtils.deleteTempFiles();

        // List<URL> externalList = getExternalHtmlPath();
        // resource.addResources(IHTMLDocConstants.EXTERNAL_FOLDER_NAME, externalList);

        targetConnectionMap = null;
        sourceConnectionMap = null;
    }

    /**
     * Checks if pictures directory is existing.
     * 
     * @param resource
     */
    private static String checkPicDirIsExists(ExportFileResource resource, String tempFolderPath) {
        String picFolderPath = tempFolderPath + File.separator + IHTMLDocConstants.PIC_FOLDER_NAME;
        File file = new File(picFolderPath);
        if (!file.exists()) {
            file.mkdir();
        }
        return picFolderPath;

    }

    /**
     * Checks if temporary directory is existing.
     * 
     * @param resource
     * @return
     */
    private String checkTempDirIsExists(ExportFileResource resource) {
        String tempDirPath = HTMLDocUtils.getTmpFolder() + File.separator + resource.getDirectoryName() + "_" //$NON-NLS-1$
                + resource.getItem().getProperty().getVersion();
        File file = new File(tempDirPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return tempDirPath;
    }

    private List<URL> parseXml2HtmlPdf(String tempFolderPath, String jobName, String xslFilePath) throws Exception {
        // clear the cache, maybe need improve it latter.
        HTMLHandler.clearExternalNodeFileCache();
        String htmlFilePath = tempFolderPath + File.separatorChar + jobName + IHTMLDocConstants.HTML_FILE_SUFFIX;
        String xmlFilePath = tempFolderPath + File.separatorChar + jobName + IHTMLDocConstants.XML_FILE_SUFFIX;
        HTMLHandler.generateHTMLFile(tempFolderPath, xslFilePath, xmlFilePath, htmlFilePath, this.externalNodeHTMLMap);

        // for pdf
        File originalXmlFile = new File(xmlFilePath);
        if (originalXmlFile.exists()) {
            String pdfXmlPath = tempFolderPath + File.separatorChar + "pdf_" + jobName + IHTMLDocConstants.XML_FILE_SUFFIX; //$NON-NLS-1$
            File pdfXmlFile = new File(pdfXmlPath);
            if (pdfXmlFile.exists()) {
                pdfXmlFile.delete();
            }
            FilesUtils.copyFile(originalXmlFile, pdfXmlFile);

            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(pdfXmlPath);
            Attribute attri = (Attribute) document.selectNodes("/project/job/preview/@picture").get(0); //$NON-NLS-1$
            attri.setValue(IHTMLDocConstants.PICTUREFOLDERPATH + "pdf_" + jobName + IHTMLDocConstants.JOB_PREVIEW_PIC_SUFFIX); //$NON-NLS-1$

            List attributeList = document.selectNodes("/project/job/externalNodeComponents/component/@preview"); //$NON-NLS-1$
            for (Object element : attributeList) {
                Attribute at = (Attribute) element;
                String externalValue = at.getValue().substring(at.getValue().indexOf("/") + 1); //$NON-NLS-1$
                String value = IHTMLDocConstants.PICTUREFOLDERPATH + "pdf_" + externalValue; //$NON-NLS-1$
                at.setValue(value);
            }

            XMLHandler.generateXMLFile(tempFolderPath, pdfXmlPath, document);

            HTMLHandler.clearExternalNodeFileCache();
            String htmlPdfPath = tempFolderPath + File.separatorChar + "pdf_" + jobName + IHTMLDocConstants.HTML_FILE_SUFFIX; //$NON-NLS-1$
            HTMLHandler.generateHTMLFile(tempFolderPath, xslFilePath, pdfXmlPath, htmlPdfPath, this.externalNodeHTMLMap);
        }

        return getParsedUrl(tempFolderPath);
    }

    /**
     * Using xslt to parse the xml to html.
     * 
     * @param jobName
     * @param tempFolderPath
     * @param xslFileName
     * 
     * @return top folder path of this job.
     * @throws Exception
     */
    private List<URL> parseXML2HTML(String tempFolderPath, String jobName, String xslFilePath) throws Exception {
        // clear the cache, maybe need improve it latter.
        HTMLHandler.clearExternalNodeFileCache();
        String htmlFilePath = tempFolderPath + File.separatorChar + jobName + IHTMLDocConstants.HTML_FILE_SUFFIX;
        String xmlFilePath = tempFolderPath + File.separatorChar + jobName + IHTMLDocConstants.XML_FILE_SUFFIX;
        HTMLHandler.generateHTMLFile(tempFolderPath, xslFilePath, xmlFilePath, htmlFilePath, this.externalNodeHTMLMap);

        return getParsedUrl(tempFolderPath);
    }

    private List<URL> getParsedUrl(String folderPath) throws Exception {
        List<URL> list = new ArrayList<URL>(1);
        File tmpFolder = new File(folderPath);

        File[] files = tmpFolder.listFiles();
        for (int i = 0; i < files.length; i++) {
            // Checks if current file is html file or xml file, otherwise ignore it.
            if (!(files[i].isDirectory())) {
                list.add(files[i].toURL());
            }
        }

        // add for bug 6851
        File tmpFolder2 = new File(HTMLDocUtils.getTmpFolder());
        File[] listFiles = tmpFolder2.listFiles();
        for (int i = 0; i < listFiles.length; i++) {
            // Checks if current file is html file or xml file, otherwise ignore it.
            if (!(listFiles[i].isDirectory()) && listFiles[i].getName().endsWith(IHTMLDocConstants.XML_FILE_SUFFIX)) {
                list.add(listFiles[i].toURL());
            }
        }

        return list;
    }

    /**
     * Generates the xml file base on an instance of <code>ExportFileResource</code> and the temporary folder path.
     * 
     * @param resource
     * @param tempFolderPath
     * @param version
     */
    private void handleXMLFile(ExportFileResource resource, String tempFolderPath, String... version) throws Exception {
        Item item = resource.getItem();
        // Check if generate Job Extra / Stats&Logs Setting Info
        if (item instanceof ProcessItem) {
            if (GlobalServiceRegister.getDefault().isServiceRegistered(IMRProcessService.class)) {
                IMRProcessService mrProcessService = (IMRProcessService) GlobalServiceRegister.getDefault()
                        .getService(IMRProcessService.class);
                generateExtraSetting = !mrProcessService.isMapReduceItem(item);
                generateStatsLogsSetting = generateExtraSetting;
            } else if (isRouteProcess(item)) {
                generateStatsLogsSetting = generateExtraSetting = !isRouteProcess(item);
            }
        } else if (item instanceof JobletProcessItem) {
            generateStatsLogsSetting = false;
        }
        targetConnectionMap = new HashMap<String, List>();
        sourceConnectionMap = new HashMap<String, List>();
        getSourceAndTargetConnection(item);

        Document document = DocumentHelper.createDocument();
        Element projectElement = generateProjectInfo(document);

        Element jobElement = generateJobInfo(item, projectElement, version);

        // This two element see feature 4162
        generateContextInfo(item, jobElement);

        generateJobSettingInfo(item, jobElement);

        List<List> allList = seperateNodes(item);
        if (allList == null || allList.size() != 3) {
            return;
        }
        List<INode> allComponentsList = allList.get(0);
        List<INode> internalNodeComponentsList = allList.get(1);
        List<INode> externalNodeComponentsList = allList.get(2);

        if (allComponentsList.size() > 0) { // Generates information for 'Component List' part in exported HTML file.
            generateAllComponentsSummaryInfo(item, jobElement, allComponentsList);
        }

        Element internalNodeElement = jobElement.addElement("internalNodeComponents"); //$NON-NLS-1$
        Element externalNodeElement = jobElement.addElement("externalNodeComponents"); //$NON-NLS-1$

        if (internalNodeComponentsList.size() > 0) {
            InternalNodeComponentHandler internalNodeComponentHandler = new InternalNodeComponentHandler(this.picFilePathMap,
                    internalNodeElement, internalNodeComponentsList, this.sourceConnectionMap, this.targetConnectionMap,
                    this.designerCoreService, this.repositoryConnectionItemMap, this.repositoryDBIdAndNameMap,
                    externalNodeHTMLMap);

            // Generates internal node components information.
            internalNodeComponentHandler.generateComponentInfo();
        }

        if (externalNodeComponentsList.size() > 0) {
            ExternalNodeComponentHandler externalNodeComponentHandler = new ExternalNodeComponentHandler(this.picFilePathMap,
                    externalNodeElement, externalNodeComponentsList, this.sourceConnectionMap, this.targetConnectionMap,
                    this.designerCoreService, this.repositoryConnectionItemMap, this.repositoryDBIdAndNameMap,
                    externalNodeHTMLMap/*
                                        * ,
                                        */);
            // Generates external node components(tMap etc.) information.

            externalNodeComponentHandler.generateComponentInfo();
        }

        // Generates all connection information(include internal node and external node).
        EList connectionList = null;
        if (item instanceof ProcessItem) {
            connectionList = ((ProcessItem) item).getProcess().getConnection();
        } else if (item instanceof JobletProcessItem) {
            connectionList = (((JobletProcessItem) item).getJobletProcess().getConnection());
        }

        if (connectionList != null && connectionList.size() != 0) {
            generateConnectionsInfo(jobElement, connectionList);
        }

        String versionPath = "_"; //$NON-NLS-1$
        if (version != null && version.length == 1) {

            versionPath = versionPath + version[0];
        } else {
            versionPath = versionPath + item.getProperty().getVersion();
        }

        String filePath = tempFolderPath + File.separatorChar + item.getProperty().getLabel() + versionPath
                + IHTMLDocConstants.XML_FILE_SUFFIX;

        // This element see feature 4382
        if (item instanceof ProcessItem) {
            generateSourceCodeInfo((ProcessItem) item, jobElement);
        }

        XMLHandler.generateXMLFile(tempFolderPath, filePath, document);
    }

    /**
     * DOC qwei Comment method "generateSourceCodeInfo".
     */
    private void generateSourceCodeInfo(ProcessItem item, Element element) {
        if (CorePlugin.getDefault().getPreferenceStore().getBoolean(ITalendCorePrefConstants.DOC_GENERATESOURCECODE)) {
            IDesignerCoreService service = CorePlugin.getDefault().getDesignerCoreService();
            IProcess process = service.getProcessFromProcessItem(item);
            IProcessor processor = ProcessorUtilities
                    .getProcessor(process, null, process.getContextManager().getDefaultContext());
            // in fact, no need to do, because will encrypt via TDI-30368
            // hidePasswordInSourceCode4Doc(process);
            try {
                processor.cleanBeforeGenerate(TalendProcessOptionConstants.CLEAN_JAVA_CODES
                        | TalendProcessOptionConstants.CLEAN_CONTEXTS | TalendProcessOptionConstants.CLEAN_DATA_SETS);
                processor.generateCode(false, true, false);
            } catch (ProcessorException e) {
                ExceptionHandler.process(e);
            }
            Element sourceCodeInfoElement = DocumentHelper.createElement("sourcecodes"); //$NON-NLS-1$
            ITalendSynchronizer synchronizer = CorePlugin.getDefault().getCodeGeneratorService().createRoutineSynchronizer();
            // StringBuffer componentsCode = new StringBuffer();
            try {
                IFile codeFile = synchronizer.getFile(item);
                if (codeFile == null) {
                    return;
                }
                String tempStr = ""; //$NON-NLS-1$
                InputStream in = codeFile.getContents();
                BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
                while ((tempStr = buffer.readLine()) != null) {
                    Element codeElement = DocumentHelper.createElement("code"); //$NON-NLS-1$
                    // componentsCode.append(tempStr).append("\n");
                    codeElement.addAttribute("content", tempStr); //$NON-NLS-1$
                    sourceCodeInfoElement.add(codeElement);
                }
                buffer.close();
                in.close();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                // e.printStackTrace();
                ExceptionHandler.process(e);
            }

            element.add(sourceCodeInfoElement);
        }

    }

    /**
     * DOC qwei Comment method "hideSourcecodePassword".
     */
    // private void hidePasswordInSourceCode4Doc(IProcess process) {
    // List<? extends IElementParameter> processParam = process.getElementParameters();
    // for (IElementParameter elementParameter : processParam) {
    // elementParameter.setValue(ParameterValueUtil.getValue4Doc(elementParameter));
    // }
    // List<? extends INode> nodes = process.getGraphicalNodes();
    // for (INode node : nodes) {
    // List<? extends IElementParameter> nodeParamList = node.getElementParameters();
    // for (IElementParameter nodeParam : nodeParamList) {
    // nodeParam.setValue(ParameterValueUtil.getValue4Doc(nodeParam));
    // }
    // }
    // }

    /**
     * DOC YeXiaowei Comment method "genereateJobSettingInfo".
     * 
     * @return
     */
    private void generateJobSettingInfo(final Item item, final Element element) {
        Element jobSettingInfoElement = DocumentHelper.createElement("jobSetting"); //$NON-NLS-1$

        ParametersType jobDirectParams = null;

        if (item instanceof ProcessItem) {
            jobDirectParams = ((ProcessItem) item).getProcess().getParameters();
        } else if (item instanceof JobletProcessItem) {
            jobDirectParams = ((JobletProcessItem) item).getJobletProcess().getParameters();
        }

        if (jobDirectParams == null || jobDirectParams.getElementParameter() == null
                || jobDirectParams.getElementParameter().isEmpty()) {
            return;
        }

        EList params = jobDirectParams.getElementParameter();

        Map<String, String> nameValueMap = new HashMap<String, String>();

        for (Object param2 : params) {
            ElementParameterType param = (ElementParameterType) param2;
            nameValueMap.put(param.getName(), ParameterValueUtil.getValue4Doc(param));
        }
        // Main settinparam info

        // Extra setting
        Element extraElement = DocumentHelper.createElement("extra"); //$NON-NLS-1$
        jobSettingInfoElement.add(extraElement);

        if (item instanceof JobletProcessItem) {
            createSingleJobParameter(extraElement, nameValueMap, IJobSettingConstants.STARTABLE);
            element.add(jobSettingInfoElement);
            return;
        }

        createSingleJobParameter(extraElement, nameValueMap, IJobSettingConstants.COMP_DEFAULT_FILE_DIR);

        createSingleJobParameter(extraElement, nameValueMap, IJobSettingConstants.MULTI_THREAD_EXECATION);

        createSingleJobParameter(extraElement, nameValueMap, IJobSettingConstants.IMPLICIT_TCONTEXTLOAD);
        if (StringUtils.equals(nameValueMap.get(IJobSettingConstants.IMPLICIT_TCONTEXTLOAD), "true")) { //$NON-NLS-1$

            if (StringUtils.equals(nameValueMap.get(IJobSettingConstants.FROM_FILE_FLAG_IMPLICIT_CONTEXT), "true")) { //$NON-NLS-1$
                createSingleJobParameter(extraElement, nameValueMap, IJobSettingConstants.FROM_FILE_FLAG_IMPLICIT_CONTEXT);
                createSingleJobParameter(extraElement, nameValueMap, IJobSettingConstants.IMPLICIT_TCONTEXTLOAD_FILE);
            }

            if (StringUtils.equals(nameValueMap.get(IJobSettingConstants.FROM_DATABASE_FLAG_IMPLICIT_CONTEXT), "true")) { //$NON-NLS-1$
                createSingleJobParameter(extraElement, nameValueMap, IJobSettingConstants.FROM_DATABASE_FLAG_IMPLICIT_CONTEXT);

                // some params about databse setting
                createSingleJobParameter(extraElement, nameValueMap,
                        IJobSettingConstants.PROPERTY_TYPE_IMPLICIT_CONTEXT_PROPERTY_TYPE);
                if (!StringUtils.equalsIgnoreCase(
                        nameValueMap.get(IJobSettingConstants.PROPERTY_TYPE_IMPLICIT_CONTEXT_PROPERTY_TYPE), "built_in")) { //$NON-NLS-1$
                    restConnectionLabelById(nameValueMap,
                            IJobSettingConstants.PROPERTY_TYPE_IMPLICIT_CONTEXT_REPOSITORY_PROPERTY_TYPE);

                    createSingleJobParameter(extraElement, nameValueMap,
                            IJobSettingConstants.PROPERTY_TYPE_IMPLICIT_CONTEXT_REPOSITORY_PROPERTY_TYPE);
                }

                createSingleJobParameter(extraElement, nameValueMap, IJobSettingConstants.DB_TYPE_IMPLICIT_CONTEXT);
                createSingleJobParameter(extraElement, nameValueMap, IJobSettingConstants.HOST_IMPLICIT_CONTEXT);
                createSingleJobParameter(extraElement, nameValueMap, IJobSettingConstants.PORT_IMPLICIT_CONTEXT);
                createSingleJobParameter(extraElement, nameValueMap, IJobSettingConstants.DBNAME_IMPLICIT_CONTEXT);
                createSingleJobParameter(extraElement, nameValueMap, IJobSettingConstants.SCHEMA_DB_IMPLICIT_CONTEXT);
                createSingleJobParameter(extraElement, nameValueMap, IJobSettingConstants.USER_IMPLICIT_CONTEXT);
                createSingleJobParameter(extraElement, nameValueMap, IJobSettingConstants.PASS_IMPLICIT_CONTEXT);
                createSingleJobParameter(extraElement, nameValueMap, IJobSettingConstants.DBFILE_IMPLICIT_CONTEXT);
                createSingleJobParameter(extraElement, nameValueMap, IJobSettingConstants.DBTABLE_IMPLICIT_CONTEXT);
                createSingleJobParameter(extraElement, nameValueMap, IJobSettingConstants.QUERY_CONDITION_IMPLICIT_CONTEXT);

            }
            // print operation
            createSingleJobParameter(extraElement, nameValueMap, IJobSettingConstants.PRINT_OPERATIONS);

            // diable warnings
            createSingleJobParameter(extraElement, nameValueMap, IJobSettingConstants.DISABLE_WARNINGS);

        }

        // Stats & logs setting
        Element statsAndLotsElement = DocumentHelper.createElement("Statslogs"); //$NON-NLS-1$
        jobSettingInfoElement.add(statsAndLotsElement);

        createSingleJobParameter(statsAndLotsElement, nameValueMap, IJobSettingConstants.ON_STATCATCHER_FLAG);
        createSingleJobParameter(statsAndLotsElement, nameValueMap, IJobSettingConstants.ON_LOGCATCHER_FLAG);
        createSingleJobParameter(statsAndLotsElement, nameValueMap, IJobSettingConstants.ON_METERCATCHER_FLAG);

        createSingleJobParameter(statsAndLotsElement, nameValueMap, IJobSettingConstants.ON_CONSOLE_FLAG);

        createSingleJobParameter(statsAndLotsElement, nameValueMap, IJobSettingConstants.ON_FILES_FLAG);
        if (StringUtils.equals(nameValueMap.get(IJobSettingConstants.ON_FILES_FLAG), "true")) { //$NON-NLS-1$
            // add on file details
            createSingleJobParameter(statsAndLotsElement, nameValueMap, IJobSettingConstants.FILE_PATH);
            createSingleJobParameter(statsAndLotsElement, nameValueMap, IJobSettingConstants.FILENAME_LOGS);
            createSingleJobParameter(statsAndLotsElement, nameValueMap, IJobSettingConstants.FILENAME_METTER);
            createSingleJobParameter(statsAndLotsElement, nameValueMap, IJobSettingConstants.FILENAME_STATS);
        }

        createSingleJobParameter(statsAndLotsElement, nameValueMap, IJobSettingConstants.ON_DATABASE_FLAG);
        if (StringUtils.equals(nameValueMap.get(IJobSettingConstants.ON_DATABASE_FLAG), "true")) { //$NON-NLS-1$
            // add on database details
            createSingleJobParameter(statsAndLotsElement, nameValueMap, IJobSettingConstants.PROPERTY_TYPE);
            createSingleJobParameter(statsAndLotsElement, nameValueMap, IJobSettingConstants.PROPERTY_TYPE_PROPERTY_TYPE);
            if (!StringUtils.equalsIgnoreCase(nameValueMap.get(IJobSettingConstants.PROPERTY_TYPE_PROPERTY_TYPE), "built_in")) { //$NON-NLS-1$
                restConnectionLabelById(nameValueMap, IJobSettingConstants.PROPERTY_TYPE_REPOSITORY_PROPERTY_TYPE);

                createSingleJobParameter(statsAndLotsElement, nameValueMap,
                        IJobSettingConstants.PROPERTY_TYPE_REPOSITORY_PROPERTY_TYPE);
            }

            createSingleJobParameter(statsAndLotsElement, nameValueMap, IJobSettingConstants.DB_TYPE);
            createSingleJobParameter(statsAndLotsElement, nameValueMap, IJobSettingConstants.HOST);
            createSingleJobParameter(statsAndLotsElement, nameValueMap, IJobSettingConstants.PORT);
            createSingleJobParameter(statsAndLotsElement, nameValueMap, IJobSettingConstants.DBNAME);
            createSingleJobParameter(statsAndLotsElement, nameValueMap, IJobSettingConstants.PROPERTIES);
            createSingleJobParameter(statsAndLotsElement, nameValueMap, IJobSettingConstants.SCHEMA_DB);
            createSingleJobParameter(statsAndLotsElement, nameValueMap, IJobSettingConstants.USER);

            createSingleJobParameter(statsAndLotsElement, nameValueMap, IJobSettingConstants.PASS);
            createSingleJobParameter(statsAndLotsElement, nameValueMap, IJobSettingConstants.DBFILE);

            createSingleJobParameter(statsAndLotsElement, nameValueMap, IJobSettingConstants.TABLE_LOGS);
            createSingleJobParameter(statsAndLotsElement, nameValueMap, IJobSettingConstants.TABLE_METER);
            createSingleJobParameter(statsAndLotsElement, nameValueMap, IJobSettingConstants.TABLE_STATS);
        }

        createSingleJobParameter(statsAndLotsElement, nameValueMap, IJobSettingConstants.CATCH_REALTIME_STATS);
        createSingleJobParameter(statsAndLotsElement, nameValueMap, IJobSettingConstants.CATCH_RUNTIME_ERRORS);
        createSingleJobParameter(statsAndLotsElement, nameValueMap, IJobSettingConstants.CATCH_USER_ERRORS);
        createSingleJobParameter(statsAndLotsElement, nameValueMap, IJobSettingConstants.CATCH_USER_WARNING);

        // verson setting see job info

        element.add(jobSettingInfoElement);
    }

    /**
     * 
     * DOC YeXiaowei Comment method "createSingleJobParameter".
     * 
     * @param root
     * @param nameValue
     * @return
     */
    private Element createSingleJobParameter(final Element root, final Map<String, String> nameValueMap, final String name) {
        Element param = DocumentHelper.createElement("jobParameter"); //$NON-NLS-1$

        // Use label replace name
        String displayName = CorePlugin.getDefault().getDesignerCoreService().getDisplayForProcessParameterFromName(name);
        param.addAttribute("name", displayName); //$NON-NLS-1$
        param.addAttribute("value", HTMLDocUtils.checkString(nameValueMap.get(name))); //$NON-NLS-1$
        root.add(param);
        return param;
    }

    private void restConnectionLabelById(Map<String, String> nameValueMap, String name) {
        String id = nameValueMap.get(name);

        IProxyRepositoryFactory factory = CorePlugin.getDefault().getProxyRepositoryFactory();
        try {
            IRepositoryViewObject lastVersion = factory.getLastVersion(id);
            if (lastVersion != null) {
                // reset the
                nameValueMap.put(name, lastVersion.getProperty().getLabel());
            }
        } catch (PersistenceException e) {
            // nothing to do
        }
    }

    /**
     * DOC YeXiaowei Comment method "generateContextInfo".
     * 
     * @return
     */
    private void generateContextInfo(final Item item, final Element element) {

        EList contexts = null;

        if (item instanceof ProcessItem) {
            contexts = ((ProcessItem) item).getProcess().getContext();
        } else if (item instanceof JobletProcessItem) {
            contexts = ((JobletProcessItem) item).getJobletProcess().getContext();
        }

        if (contexts == null || contexts.isEmpty()) {
            return;
        }

        Element contextListElement = DocumentHelper.createElement("contextList"); // Context root //$NON-NLS-1$

        for (Object context2 : contexts) {
            // export single context infomation
            Element contextElement = DocumentHelper.createElement("context"); //$NON-NLS-1$
            ContextType context = (ContextType) context2;

            // Attributes
            contextElement.addAttribute("name", HTMLDocUtils.checkString(context.getName())); //$NON-NLS-1$

            IProxyRepositoryFactory repositoryFactory = CorePlugin.getDefault().getProxyRepositoryFactory();
            // Context parameters as children
            EList params = context.getContextParameter();
            if (params != null && !params.isEmpty()) {
                for (Object param2 : params) {
                    /*
                     * <contextParameter comment="Give server name" name="server" prompt="Default Server "
                     * promptNeeded="false" repositoryContextId="_crJMkCCQEd2Oweh7yRMWjQ" type=""
                     * value="'192.168.0.109'"/>
                     */
                    ContextParameterType param = (ContextParameterType) param2;
                    Element contextParamElement = DocumentHelper.createElement("contextParameter"); //$NON-NLS-1$
                    contextParamElement.addAttribute("name", HTMLDocUtils.checkString(param.getName())); //$NON-NLS-1$
                    contextParamElement.addAttribute("prompt", HTMLDocUtils.checkString(param.getPrompt())); //$NON-NLS-1$
                    contextParamElement.addAttribute("promptNeeded", HTMLDocUtils.checkString(Boolean.toString(param //$NON-NLS-1$
                            .isPromptNeeded())));
                    contextParamElement.addAttribute("type", HTMLDocUtils.checkString(param.getType())); //$NON-NLS-1$
                    contextParamElement.addAttribute("value", ParameterValueUtil.getValue4Doc(param)); //$NON-NLS-1$ 
                    // replace repository id with context label
                    if (param.getRepositoryContextId() != null) {
                        ContextItem contextItem = ContextUtils.getContextItemById2(param.getRepositoryContextId());
                        if (contextItem != null) { // bug 5978: repository link to context item might be lost.
                            String label = contextItem.getProperty().getLabel();
                            contextParamElement.addAttribute("source", HTMLDocUtils.checkString(label)); //$NON-NLS-1$
                        }
                    }
                    contextElement.add(contextParamElement);
                }
            }

            contextListElement.add(contextElement);
        }

        element.add(contextListElement);
    }

    /**
     * Generates all components summary information.
     * 
     * @param item
     * 
     * @param inputJobElement
     * @param allComponentsList
     */
    public void generateAllComponentsSummaryInfo(Item item, Element inputJobElement, List<INode> allComponentsList) {
        Element componentNameListElement = null;
        Point screenshotOffset = new Point();

        ProcessItem processItem = (ProcessItem) item;
        if (processItem.getProcess().getParameters() != null) {
            List<ElementParameterType> elemParamList = processItem.getProcess().getParameters().getElementParameter();
            getScreenShotOffset(screenshotOffset, elemParamList);
        }

        getComponentListInfo(inputJobElement, allComponentsList, componentNameListElement, screenshotOffset);
    }

    /**
     * DOC tang Comment method "getComponentListInfo".
     * 
     * @param inputJobElement
     * @param allComponentsList
     * @param componentNameListElement
     * @param screenshotOffset
     */
    protected void getComponentListInfo(Element inputJobElement, List<INode> allComponentsList, Element componentNameListElement,
            Point screenshotOffset) {
        int x = 0, y = 0, width = 0, height = 0;
        for (INode node : allComponentsList) {
            x = node.getPosX() + screenshotOffset.x;
            y = node.getPosY() + screenshotOffset.y;
            Image icon = CoreImageProvider.getComponentIcon(node.getComponent(), ICON_SIZE.ICON_32);
            if (icon != null) {
                ImageData imageData = icon.getImageData();
                if (imageData != null) {
                    width = imageData.width;
                    height = imageData.height;
                }
            }

            if (componentNameListElement == null) {
                componentNameListElement = inputJobElement.addElement("componentList"); //$NON-NLS-1$
            }
            Element componentItemElement = null;
            componentItemElement = componentNameListElement.addElement("componentItem"); //$NON-NLS-1$
            componentItemElement.addAttribute("name", node.getUniqueName()); //$NON-NLS-1$
            componentItemElement.addAttribute("link", node.getUniqueName()); //$NON-NLS-1$
            componentItemElement.addAttribute("type", node.getComponent().getName()); //$NON-NLS-1$
            componentItemElement.addAttribute("leftTopX", x + ""); //$NON-NLS-1$ //$NON-NLS-2$
            componentItemElement.addAttribute("leftTopY", y + ""); //$NON-NLS-1$ //$NON-NLS-2$
            componentItemElement.addAttribute("rightBottomX", x + width + ""); //$NON-NLS-1$ //$NON-NLS-2$
            componentItemElement.addAttribute("rightBottomY", y + height + ""); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * DOC tang Comment method "getScreenShotOffset".
     * 
     * @param screenshotOffset
     * @param processItem
     */
    protected void getScreenShotOffset(Point screenshotOffset, List<ElementParameterType> elemParamList) {
        for (ElementParameterType curElem : elemParamList) {
            if (curElem.getName().equals(IProcess.SCREEN_OFFSET_X)) {
                screenshotOffset.x = Integer.valueOf("".equals(HTMLDocUtils.checkString(curElem.getValue())) ? "0" : curElem //$NON-NLS-1$ //$NON-NLS-2$
                        .getValue());
            } else if (curElem.getName().equals(IProcess.SCREEN_OFFSET_Y)) {
                screenshotOffset.y = Integer.valueOf("".equals(HTMLDocUtils.checkString(curElem.getValue())) ? "0" : curElem //$NON-NLS-1$ //$NON-NLS-2$
                        .getValue());
            }
        }
    }

    /**
     * This method is used for seperating all nodes into internal and external.
     * 
     * @param item
     * @return
     */
    protected List<List> seperateNodes(Item item) {
        List<INode> internalNodeComponentList = new ArrayList<INode>();
        List<INode> externalNodeComponentList = new ArrayList<INode>();
        List<INode> allNodeComponentList = new ArrayList<INode>();
        List<List> componentsList = new ArrayList<List>();

        IProcess process = null;
        boolean isJobletProcessItem = item instanceof JobletProcessItem;
        boolean isProcessItem = item instanceof ProcessItem;
        if (isProcessItem) {
            process = CorePlugin.getDefault().getDesignerCoreService().getProcessFromProcessItem((ProcessItem) item, true);
        }
        // if (isJobletProcessItem) {
        // AbstractProcessProvider processProvider =
        // AbstractProcessProvider.findProcessProviderFromPID(IComponent.JOBLET_PID);
        // if (processProvider != null) {
        // process = processProvider.buildNewGraphicProcess(item);
        // }
        // }
        if (process == null) {
            return componentsList;
        }

        List<INode> graphicalNodeList = (List<INode>) process.getGraphicalNodes();

        for (INode node : graphicalNodeList) {
            // If component is not activate, do not need to get it's information
            if (!node.isActivate()) {
                continue;
            }

            allNodeComponentList.add(node);

            if (node.getExternalNode() != null) {
                externalNodeComponentList.add(node);
            } else {
                internalNodeComponentList.add(node);
            }
        }

        // Sorts the component list in alpha-order.
        Comparator comparator = getComparator();
        Collections.sort(allNodeComponentList, comparator);
        Collections.sort(internalNodeComponentList, comparator);
        Collections.sort(externalNodeComponentList, comparator);

        componentsList.add(allNodeComponentList);
        componentsList.add(internalNodeComponentList);
        componentsList.add(externalNodeComponentList);
        if (process instanceof IProcess2) {
            ((IProcess2) process).dispose();
        }
        return componentsList;
    }

    /**
     * Generates connections information base on <code>jobElement</code>,<code>connectionList</code>
     * 
     * @param jobElement
     * @param connectionList
     */
    private void generateConnectionsInfo(Element jobElement, EList connectionList) {
        Element connectionsElement = jobElement.addElement("connections"); //$NON-NLS-1$
        for (Object element : connectionList) {
            ConnectionType type = (ConnectionType) element;
            Element connectionElement = connectionsElement.addElement("connection"); //$NON-NLS-1$
            connectionElement.addAttribute("label", HTMLDocUtils.checkString(type.getLabel())); //$NON-NLS-1$
            connectionElement.addAttribute("lineStyle", HTMLDocUtils.checkString(type.getLineStyle() + "")); //$NON-NLS-1$ //$NON-NLS-2$
            connectionElement.addAttribute("metaname", HTMLDocUtils.checkString(type.getMetaname())); //$NON-NLS-1$
            connectionElement.addAttribute("offsetLabelX", HTMLDocUtils.checkString(type.getOffsetLabelX() + "")); //$NON-NLS-1$ //$NON-NLS-2$
            connectionElement.addAttribute("offsetLabelY", HTMLDocUtils.checkString(type.getOffsetLabelY() + "")); //$NON-NLS-1$ //$NON-NLS-2$
            connectionElement.addAttribute("source", HTMLDocUtils.checkString(type.getSource())); //$NON-NLS-1$
            connectionElement.addAttribute("target", HTMLDocUtils.checkString(type.getTarget())); //$NON-NLS-1$
        }
    }

    /**
     * Generates job(process) information in XML base on <code>ProcessItem</code> and project element.
     * 
     * @param item <code>ProcessItem</code>
     * @param projectElement <code>Element</code>
     * @param version
     * @return an instance of <code>Element</code>
     */
    private Element generateJobInfo(Item item, Element projectElement, String... version) {

        picFilePathMap = new HashMap<String, String>();
        // IProcess process = CorePlugin.getDefault().getDesignerCoreService().getProcessFromProcessItem(processItem);

        Property property = item.getProperty();
        String jobName = property.getLabel();
        String jobVersion = property.getVersion();
        Element jobElement = projectElement.addElement("job"); //$NON-NLS-1$
        jobElement.addAttribute("name", HTMLDocUtils.checkString(jobName)); //$NON-NLS-1$

        jobElement.addAttribute("author", HTMLDocUtils.checkString(property.getAuthor().toString())); //$NON-NLS-1$

        if (version != null && version.length == 1) {
            jobVersion = version[0];
        }
        if (isRouteProcess(item)) {
            jobElement.addAttribute("type", "route");//$NON-NLS-1$//$NON-NLS-2$
            ICamelDesignerCoreService camelService = (ICamelDesignerCoreService) GlobalServiceRegister.getDefault()
                    .getService(ICamelDesignerCoreService.class);
            camelService.appendRouteInfo2Doc(item, jobElement);
        }
        if (generateExtraSetting) {
            jobElement.addAttribute("i18n.job.extract.settings.type", "i18n.job.extract.settings");//$NON-NLS-1$//$NON-NLS-2$
        }
        if (generateStatsLogsSetting) {
            jobElement.addAttribute("i18n.job.stats.logs.type", "i18n.job.stats.logs");//$NON-NLS-1$//$NON-NLS-2$
        }
        jobElement.addAttribute("version", HTMLDocUtils.checkString(jobVersion)); //$NON-NLS-1$
        jobElement.addAttribute("purpose", HTMLDocUtils.checkString(property.getPurpose())); //$NON-NLS-1$
        jobElement.addAttribute("status", HTMLDocUtils.checkString(property.getStatusCode())); //$NON-NLS-1$

        //        jobElement.addAttribute("description", HTMLDocUtils.checkString(property.getDescription())); //$NON-NLS-1$

        jobElement
                .addAttribute(
                        "creation", HTMLDocUtils.checkDate(ItemDateParser.parseAdditionalDate(property, ItemProductKeys.DATE.getCreatedKey()))); //$NON-NLS-1$
        jobElement
                .addAttribute(
                        "modification", HTMLDocUtils.checkDate(ItemDateParser.parseAdditionalDate(property, ItemProductKeys.DATE.getModifiedKey()))); //$NON-NLS-1$

        Element descr = jobElement.addElement("description"); //$NON-NLS-1$
        // bug 22608
        String jobDescriptionStr = HTMLDocUtils.checkString(property.getDescription()).replaceAll("\\r\\n", "<br/>"); //$NON-NLS-1$ //$NON-NLS-2$
        StringBuffer sb = new StringBuffer();
        if (jobDescriptionStr != null) {
            String[] jobDescriptions = jobDescriptionStr.split("<br/>"); //$NON-NLS-1$
            for (String str : jobDescriptions) {
                String ss = str;
                if (str != null && str.length() > 120) {
                    while (ss.length() > 120) {
                        int k = ss.length() / 120;
                        for (int i = 0; i < k; i++) {
                            sb.append(ss.substring(0, 120) + "<br/>"); //$NON-NLS-1$
                            ss = ss.substring(120, ss.length());
                        }
                    }
                    sb.append(ss);
                } else {
                    sb.append(str + "<br/>"); //$NON-NLS-1$
                }
            }
        }
        descr.addCDATA(sb.toString());

        String picName = jobName + "_" + jobVersion + IHTMLDocConstants.JOB_PREVIEW_PIC_SUFFIX; //$NON-NLS-1$
        IPath filePath = null;
        if (item instanceof ProcessItem) {
            filePath = DocumentationPathProvider.getPathFileName(item, RepositoryConstants.IMG_DIRECTORY_OF_JOB_OUTLINE, picName);
        } else if (item instanceof JobletProcessItem) {
            filePath = DocumentationPathProvider.getPathFileName(item, RepositoryConstants.IMG_DIRECTORY_OF_JOBLET_OUTLINE,
                    picName);
        }
        Element previewElement = jobElement.addElement("preview"); //$NON-NLS-1$
        previewElement.addAttribute("picture", IHTMLDocConstants.PICTUREFOLDERPATH + picName); //$NON-NLS-1$

        // if (filePath == null) {
        //            previewElement.addAttribute("picture", ""); //$NON-NLS-1$ //$NON-NLS-2$
        // } else {
        // String filePathStr = filePath.toOSString();
        // File file = new File(filePathStr);
        // if (file.exists()) {
        //                previewElement.addAttribute("picture", IHTMLDocConstants.PICTUREFOLDERPATH + picName); //$NON-NLS-1$
        // picFilePathMap.put(picName, filePathStr);
        // }
        // }
        return jobElement;
    }

    /**
     * add by gliu for TESB-13788
     * 
     * @param item
     * @return
     */
    protected boolean isRouteProcess(Item item) {
        if (GlobalServiceRegister.getDefault().isServiceRegistered(ICamelDesignerCoreService.class)) {
            ICamelDesignerCoreService camelService = (ICamelDesignerCoreService) GlobalServiceRegister.getDefault()
                    .getService(ICamelDesignerCoreService.class);
            return camelService.isInstanceofCamel(item);
        }
        return false;
    }

    /**
     * Generates project element information in XML file.
     * 
     * @param document <code>Document</code>
     * @return an instance of <code>Element</code>
     */
    protected Element generateProjectInfo(Document document) {
        Element projectElement = document.addElement("project"); //$NON-NLS-1$
        generateMessages(projectElement);
        projectElement.addAttribute("name", getProject().getLabel()); //$NON-NLS-1$
        projectElement.addAttribute("logo", IHTMLDocConstants.PICTUREFOLDERPATH + IHTMLDocConstants.TALEND_LOGO_FILE_NAME); //$NON-NLS-1$
        projectElement.addAttribute("title", IHTMLDocConstants.TITLE_GEN + getFullProductName()); //$NON-NLS-1$
        projectElement.addAttribute("link", IHTMLDocConstants.WEBSITE_LINK); //$NON-NLS-1$
        projectElement.addAttribute("language", getProject().getLanguage().getName()); //$NON-NLS-1$
        //        projectElement.addAttribute("description", getProject().getDescription()); //$NON-NLS-1$
        projectElement.addAttribute("generatedDate", DateFormat.getDateTimeInstance().format(new Date())); //$NON-NLS-1$
        projectElement.addAttribute("versionName", getProductVersionName()); //$NON-NLS-1$
        projectElement.addAttribute("version", VersionUtils.getVersion()); //$NON-NLS-1$
        projectElement.addAttribute("docType", getDocTypeAttribute()); //$NON-NLS-1$
        String company = CorePlugin.getDefault().getPreferenceStore().getString(ITalendCorePrefConstants.DOC_COMPANY_NAME);
        if (company != null) {
            projectElement.addAttribute("company", company); //$NON-NLS-1$
        }

        Element proDesc = projectElement.addElement("pro-description"); //$NON-NLS-1$
        if (getProject().getDescription() != null) { // for bug 16854, project description can be null.
            proDesc.addCDATA(getProject().getDescription().replaceAll("\\r\\n", "<br/>")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return projectElement;
    }

    /**
     * 
     * wzhang Comment method "generateMessages". generate messages for i18n.
     * 
     * @param element
     * @return
     */
    protected Element generateMessages(Element element) {
        // Job.xsl and other common strings
        element.addAttribute("i18n.job.generated.documetation", Messages.HTMLDocGenerator_Generated_documentation); //$NON-NLS-1$
        element.addAttribute("i18n.job.project.name", Messages.HTMLDocGenerator_Project_Name); //$NON-NLS-1$
        element.addAttribute("i18n.job.generated.date", Messages.HTMLDocGenerator_GENERATION_DATE); //$NON-NLS-1$
        element.addAttribute("i18n.job.author", Messages.HTMLDocGenerator_AUTHOR); //$NON-NLS-1$
        element.addAttribute("i18n.job.summary", Messages.HTMLDocGenerator_Summary); //$NON-NLS-1$
        element.addAttribute("i18n.job.project.description", Messages.HTMLDocGenerator_Project_Description); //$NON-NLS-1$
        element.addAttribute("i18n.job.job.description", Messages.HTMLDocGenerator_Description); //$NON-NLS-1$
        element.addAttribute("i18n.job.job.preview.picture", Messages.HTMLDocGenerator_Preview_Picture); //$NON-NLS-1$
        if (generateExtraSetting || generateStatsLogsSetting) {
            element.addAttribute("i18n.job.job.setting", Messages.HTMLDocGenerator_Settings); //$NON-NLS-1$
        }
        element.addAttribute("i18n.job.context.list", Messages.HTMLDocGenerator_Context_List); //$NON-NLS-1$
        element.addAttribute("i18n.job.component.list", Messages.HTMLDocGenerator_Component_List); //$NON-NLS-1$
        element.addAttribute("i18n.job.components.description", Messages.HTMLDocGenerator_Components_Description); //$NON-NLS-1$
        element.addAttribute("i18n.job.source.code", Messages.HTMLDocGenerator_Source_Code); //$NON-NLS-1$
        element.addAttribute("i18n.job.properties", Messages.HTMLDocGenerator_Properties); //$NON-NLS-1$
        element.addAttribute("i18n.job.values", Messages.HTMLDocGenerator_Values); //$NON-NLS-1$
        element.addAttribute("i18n.job.name", Messages.HTMLDocGenerator_Name); //$NON-NLS-1$
        element.addAttribute("i18n.job.language", Messages.HTMLDocGenerator_Language); //$NON-NLS-1$
        element.addAttribute("i18n.job.description", Messages.HTMLDocGenerator_Description); //$NON-NLS-1$
        element.addAttribute("i18n.job.author.min", Messages.HTMLDocGenerator_Author); //$NON-NLS-1$
        element.addAttribute("i18n.job.version", Messages.HTMLDocGenerator_Version); //$NON-NLS-1$
        element.addAttribute("i18n.job.purpose", Messages.HTMLDocGenerator_Purpose); //$NON-NLS-1$
        element.addAttribute("i18n.job.status", Messages.HTMLDocGenerator_Status); //$NON-NLS-1$
        element.addAttribute("i18n.job.creation", Messages.HTMLDocGenerator_Creation); //$NON-NLS-1$
        element.addAttribute("i18n.job.modification", Messages.HTMLDocGenerator_Modification); //$NON-NLS-1$
        element.addAttribute("i18n.job.extract.settings", Messages.HTMLDocGenerator_Extra_settings); //$NON-NLS-1$
        element.addAttribute("i18n.job.value", Messages.HTMLDocGenerator_Value); //$NON-NLS-1$
        element.addAttribute("i18n.job.stats.logs", Messages.HTMLDocGenerator_Status + " & " + Messages.HTMLDocGenerator_Logs); //$NON-NLS-1$ 
        element.addAttribute("i18n.job.context", Messages.HTMLDocGenerator_Context); //$NON-NLS-1$
        element.addAttribute("i18n.job.promt", Messages.HTMLDocGenerator_Prompt); //$NON-NLS-1$
        element.addAttribute("i18n.job.need.promt", Messages.HTMLDocGenerator_Need_Prompt); //$NON-NLS-1$
        element.addAttribute("i18n.job.type", Messages.HTMLDocGenerator_Type); //$NON-NLS-1$
        element.addAttribute("i18n.job.source", Messages.HTMLDocGenerator_Source); //$NON-NLS-1$
        element.addAttribute("i18n.job.component.name", Messages.HTMLDocGenerator_Component_Name); //$NON-NLS-1$
        element.addAttribute("i18n.job.component.type", Messages.HTMLDocGenerator_Component_Type); //$NON-NLS-1$
        element.addAttribute("i18n.job.component", Messages.HTMLDocGenerator_Component); //$NON-NLS-1$
        element.addAttribute("i18n.job.unique.name", Messages.HTMLDocGenerator_UNIQUE_NAME); //$NON-NLS-1$
        element.addAttribute("i18n.job.input", Messages.HTMLDocGenerator_INPUT); //$NON-NLS-1$
        element.addAttribute("i18n.job.label", Messages.HTMLDocGenerator_LABEL); //$NON-NLS-1$
        element.addAttribute("i18n.job.output", Messages.HTMLDocGenerator_OUTPUT); //$NON-NLS-1$
        element.addAttribute("i18n.job.component.parameters", Messages.HTMLDocGenerator_Component_Parameters); //$NON-NLS-1$
        element.addAttribute("i18n.job.schema.for", Messages.HTMLDocGenerator_Schema_for + " "); //$NON-NLS-1$ 
        element.addAttribute("i18n.job.column", Messages.HTMLDocGenerator_Column); //$NON-NLS-1$
        element.addAttribute("i18n.job.key", Messages.HTMLDocGenerator_Key); //$NON-NLS-1$
        element.addAttribute("i18n.job.length", Messages.HTMLDocGenerator_Length); //$NON-NLS-1$
        element.addAttribute("i18n.job.precision", Messages.HTMLDocGenerator_Precision); //$NON-NLS-1$
        element.addAttribute("i18n.job.nullable", Messages.HTMLDocGenerator_Nullable); //$NON-NLS-1$
        element.addAttribute("i18n.job.comment", Messages.HTMLDocGenerator_Comment); //$NON-NLS-1$
        element.addAttribute("i18n.job.original.function.parameters", Messages.HTMLDocGenerator_Original_Function_Parameters); //$NON-NLS-1$
        element.addAttribute("i18n.job.ended", "ended"); //$NON-NLS-1$
        element.addAttribute("i18n.job.content", Messages.HTMLDocGenerator_content); //$NON-NLS-1$

        // special for Route
        element.addAttribute("i18n.route.manifest.type", Messages.HTMLDocGenerator_Route_Manifest_Type); //$NON-NLS-1$
        element.addAttribute("i18n.route.manifest.value", Messages.HTMLDocGenerator_Route_Manifest_Value); //$NON-NLS-1$
        element.addAttribute("i18n.route.resource.name", Messages.HTMLDocGenerator_Route_Resource_Name); //$NON-NLS-1$
        element.addAttribute("i18n.route.resource.version", Messages.HTMLDocGenerator_Route_Resource_Version); //$NON-NLS-1$
        element.addAttribute("i18n.route.resource.path", Messages.HTMLDocGenerator_Route_Resource_Path); //$NON-NLS-1$

        return element;
    }

    /**
     * ftang Comment method "addDocTypeAttribute".
     * 
     * @param projectElement
     */
    protected String getDocTypeAttribute() {
        if (ERepositoryObjectType.JOB_DOC == null) {
            return "Jobs"; //$NON-NLS-1$
        }
        return ERepositoryObjectType.JOB_DOC.toString();
    }

    /**
     * Add resources.
     * 
     * @param resource <code>ExportFileResource</code>
     * @param resultFiles a <code>List</code> of <code>URL</code>
     */
    private void addResources(ExportFileResource resource, List<URL> resultFiles) {
        resource.addResources(resultFiles);
    }

    /**
     * Get the current project.
     * 
     * @return an instance of <code>Project</code>
     */
    protected Project getProject() {
        if (this.project == null) {
            return ProjectManager.getInstance().getCurrentProject();
        }
        return this.project;
    }

    /**
     * Get source connections and target connections base on given <code>ProcessItem</code>.
     * 
     * @param item ProcessItem
     */
    protected void getSourceAndTargetConnection(Item item) {
        if (item instanceof ProcessItem) {
            EList connectionList = ((ProcessItem) item).getProcess().getConnection();
            if (connectionList != null && connectionList.size() != 0) {
                handleSourceAndTargetConnection(connectionList);
            }
        }
    }

    /**
     * Comment method "handleSourceAndTargetConnection".
     * 
     * @param sourceConnectionMap
     * @param targetConnectionMap
     * @param connectionList
     */
    protected void handleSourceAndTargetConnection(EList connectionList) {
        List<String> targetList = new ArrayList<String>();
        List<String> sourceList = new ArrayList<String>();
        for (Object element : connectionList) {
            ConnectionType type = (ConnectionType) element;
            if (!targetConnectionMap.containsKey(type.getSource())) {
                targetList = new ArrayList<String>();
            }
            if (!targetList.contains(type.getTarget())) {
                targetList.add(type.getTarget());
            }

            targetConnectionMap.put(type.getSource(), targetList);

            sourceList = new ArrayList<String>();
            if (sourceConnectionMap.containsKey(type.getTarget())) {
                sourceList = sourceConnectionMap.get(type.getTarget());
            }
            if (!sourceList.contains(type.getSource())) {
                sourceList.add(type.getSource());
            }
            sourceConnectionMap.put(type.getTarget(), sourceList);
        }

    }

    /**
     * 
     * DOC ggu Comment method "getExternalHtmlPath".<br>
     * 
     * add external Doc file list
     * 
     * @return
     * @throws MalformedURLException
     */
    private List<URL> getExternalHtmlPath() throws MalformedURLException {
        List<URL> externalList = new ArrayList<URL>();

        Set keySet = externalNodeHTMLMap.keySet();
        for (Object key : keySet) {
            if (externalNodeHTMLMap.get(key) instanceof URL) {
                URL html = (URL) externalNodeHTMLMap.get(key);
                if (html != null) {
                    externalList.add(html);// html

                    String htmlStr = html.toString();
                    String xmlStr = htmlStr.substring(0, htmlStr.lastIndexOf(IHTMLDocConstants.HTML_FILE_SUFFIX))
                            + IHTMLDocConstants.XML_FILE_SUFFIX;
                    externalList.add(new URL(xmlStr));// xml
                }

            }
        }

        return externalList;
    }

    /**
     * 
     * DOC ggu Comment method "getProductName".
     * 
     * @return
     */
    private String getFullProductName() {
        IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault()
                .getService(IBrandingService.class);

        return brandingService.getFullProductName();
    }

    private String getProductVersionName() {
        IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault()
                .getService(IBrandingService.class);

        return brandingService.getShortProductName() + IHTMLDocConstants.VERSION;
    }

    protected void saveLogoImage(int type, File file) throws IOException {
        boolean documentationPluginLoaded = PluginChecker.isDocumentationPluginLoaded();
        // get image from cache
        ByteArrayOutputStream result = logoImageCache.get(type);
        if (documentationPluginLoaded) {
            String userLogoPath = CorePlugin.getDefault().getPreferenceStore().getString(ITalendCorePrefConstants.DOC_USER_LOGO);
            if (userLogoPath != null && !"".equals(userLogoPath)) { //$NON-NLS-1$
                if (result == null || !userLogoPath.equals(userDocImageOldPath)) {
                    userDocImageOldPath = userLogoPath;
                    result = new ByteArrayOutputStream(3072);
                    File userLogo = new File(userLogoPath);
                    if (userLogo.exists()) {
                        Image image = new Image(Display.getCurrent(), userLogoPath);
                        ImageLoader imageLoader = new ImageLoader();
                        imageLoader.data = new ImageData[] { image.getImageData() };
                        imageLoader.save(result, type);
                        logoImageCache.put(type, result);
                        image.dispose();
                    }
                }
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(result.toByteArray());
                fos.close();
                return;
            }
        }
        // if (result == null) {
        result = new ByteArrayOutputStream(3072);
        IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault()
                .getService(IBrandingService.class);
        ImageData imageData = brandingService.getLoginHImage().getImageData();
        new ByteArrayOutputStream();

        ImageLoader imageLoader = new ImageLoader();
        imageLoader.data = new ImageData[] { imageData };

        imageLoader.save(result, type);
        // put image to cache, no need to generate next time
        logoImageCache.put(type, result);
        // }

        FileOutputStream fos = new FileOutputStream(file);
        fos.write(result.toByteArray());
        fos.close();

    }

    /**
     * It is used for component list sort.
     * 
     * @return Comparator.
     */
    private Comparator getComparator() {

        return new Comparator() {

            @Override
            public int compare(Object arg0, Object arg1) {

                if (arg0 == null || arg1 == null) {
                    return 0;
                }
                String name0 = ((INode) arg0).getUniqueName();
                String name1 = ((INode) arg1).getUniqueName();

                if (name0 == null || name1 == null) {
                    return 0;
                }

                return name0.compareTo(name1);
            }

        };
    }
}
