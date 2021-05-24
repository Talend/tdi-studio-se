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
package org.talend.designer.codegen.components.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.talend.commons.CommonsPlugin;
import org.talend.commons.exception.BusinessException;
import org.talend.commons.runtime.xml.XSDValidator;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.ModelPlugin;
import org.talend.designer.core.model.components.ComponentFilesNaming;
import org.xml.sax.SAXException;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ven., 29 sept. 2006) nrousseau $
 *
 */
public class ComponentFileChecker {

    private static final Bundle XSD_CONTAINER_BUNDLE = Platform.getBundle(ModelPlugin.MODEL_PLUGIN_ID);

    private static final String XSD_PATH = "model/Component.xsd"; //$NON-NLS-1$

    public static void checkComponentFolder(File currentFolder, String languageSuffix) throws BusinessException {
        checkFiles(currentFolder, languageSuffix);
        File xmlMainFile = new File(currentFolder, ComponentFilesNaming.getInstance().getMainXMLFileName(currentFolder.getName(),
                languageSuffix));
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(xmlMainFile);
            // do not check anymore XSD when be in headless mode.
            // check is mainly usefull for GUI to be able to check why component is not loaded after be developped.
            // if be in headless mode (like commandline), it's supposed to use only stable components
            if (!CommonsPlugin.isHeadless()) {
                checkXSD(xmlMainFile);
            }
        } catch (FileNotFoundException e) {
            ExceptionHandler.process(e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    //
                }
            }
        }

    }

    private static void checkXSD(File file) throws MalformedMainXMLComponentFileException {
        if (Platform.getOS().equals(Platform.OS_AIX)) {
            return;
        }
        Path path = new Path(XSD_PATH);
        try {
            URL url = FileLocator.toFileURL(FileLocator.find(XSD_CONTAINER_BUNDLE, path, null));
            File schema = new File(url.getPath());
            XSDValidator.checkXSD(file, schema);
        } catch (IOException e) {
            throw new MalformedMainXMLComponentFileException("Cannot find xsd (" + path.lastSegment() + ")", e); //$NON-NLS-1$ //$NON-NLS-2$
        } catch (SAXException e) {
            throw new MalformedMainXMLComponentFileException("Does not match xsd (" + path.lastSegment() + ")", e); //$NON-NLS-1$ //$NON-NLS-2$
        } catch (ParserConfigurationException e) {
            throw new MalformedMainXMLComponentFileException("Cannot check xsd (" + path.lastSegment() + ")", e); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    private static void checkFiles(File folder, String languageSuffix) throws MissingComponentFileException {
        String mainXmlFileName = ComponentFilesNaming.getInstance().getMainXMLFileName(folder.getName(), languageSuffix);
        File mainXmlFile = new File(folder, mainXmlFileName);
        if (!mainXmlFile.exists()) {
            throw new MissingMainXMLComponentFileException("Cannot find file \"" + mainXmlFile.getName() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
        }

        for (String currentFileToCheck : ComponentFilesNaming.getInstance().getRequiredFilesNames(folder.getName(),
                languageSuffix)) {
            File file = new File(folder, currentFileToCheck);
            if (!file.exists()) {
                throw new MissingComponentFileException("Cannot find file \"" + file.getName() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
    }
}
