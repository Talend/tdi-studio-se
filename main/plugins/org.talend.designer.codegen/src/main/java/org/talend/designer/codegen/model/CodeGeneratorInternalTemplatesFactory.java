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
package org.talend.designer.codegen.model;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.language.ECodeLanguage;
import org.talend.designer.codegen.CodeGeneratorActivator;
import org.talend.designer.codegen.additionaljet.AbstractJetFileProvider;
import org.talend.designer.codegen.additionaljet.CustomizeJetFilesProviderManager;
import org.talend.designer.codegen.config.EInternalTemplate;
import org.talend.designer.codegen.config.TemplateUtil;
import org.talend.designer.codegen.exception.CodeGeneratorException;

/**
 * Create a list of Available templates in the application.
 *
 * $Id$
 *
 */
public class CodeGeneratorInternalTemplatesFactory {

    private List<TemplateUtil> templates;

    private ECodeLanguage language;

    /**
     * Constructor.
     */
    public CodeGeneratorInternalTemplatesFactory() {
    }

    /**
     * init list of templates.
     */
    public void init() {
        templates = new ArrayList<>();

        initializeStubAdditionalJetFile();

        initializeAdditionalJetFileFromProviderExtension();
    }

    public static List<TemplateUtil> getTemplatesFrom(String pluginId, String directory) {
        List<TemplateUtil> templates = new ArrayList<>();
        try {
            // System.out.println("plugin:" + pluginId); //$NON-NLS-1$
            URL url = FileLocator.find(Platform.getBundle(pluginId), new Path(directory), null); // $NON-NLS-1$
            File fileDirectory = new File(FileLocator.toFileURL(url).getPath());
            for (File file : fileDirectory.listFiles()) {
                EInternalTemplate curentTemplate = null;
                if (file.getName().contains(EInternalTemplate.HEADER_ADDITIONAL.getTemplateName()) && file.getName()
                        .endsWith(TemplateUtil.EXT_SEP + ECodeLanguage.JAVA.getExtension() + TemplateUtil.TEMPLATE_EXT)) {
                    curentTemplate = EInternalTemplate.HEADER_ADDITIONAL;
                } else {
                    for (EInternalTemplate utilTemplate : EInternalTemplate.values()) {
                        if (file.getName().equals(utilTemplate.getTemplateName() + TemplateUtil.EXT_SEP
                                + ECodeLanguage.JAVA.getExtension() + TemplateUtil.TEMPLATE_EXT)) {
                            curentTemplate = utilTemplate;
                            break;
                        }
                    }
                }
                if (curentTemplate != null) {
                    TemplateUtil template = new TemplateUtil(curentTemplate);
                    template.setJetPluginRepository(pluginId);
                    template.setTemplateRelativeUri(directory + TemplateUtil.DIR_SEP + file.getName());
                    templates.add(template);
                    // System.out.println("----template:" + file.getName()); //$NON-NLS-1$
                }
            }

        } catch (IOException e) {
            ExceptionHandler.process(e);
        }
        return templates;

    }

    private void initializeStubAdditionalJetFile() {
        templates.addAll(getTemplatesFrom(CodeGeneratorActivator.PLUGIN_ID, TemplateUtil.JET_STUB_DIRECTORY));
    }

    private void initializeAdditionalJetFileFromProviderExtension() {
        CustomizeJetFilesProviderManager componentsProviderManager = CustomizeJetFilesProviderManager.getInstance();
        for (AbstractJetFileProvider componentsProvider : componentsProviderManager.getProviders()) {
            templates.addAll(componentsProvider.initializeStubAdditionalJetFile());
        }
    }

    /**
     * Return list of available templates.
     *
     * @return
     */
    public List<TemplateUtil> getTemplates() {
        return templates;
    }

    public void setCurrentLanguage(ECodeLanguage language) {
        this.language = language;
    }

    /**
     * DOC nrousseau Comment method "getTemplateFromType".
     *
     * @param type
     * @return
     * @throws CodeGeneratorException
     */
    public List<TemplateUtil> getTemplatesFromType(EInternalTemplate type) throws CodeGeneratorException {
        List<TemplateUtil> templatesFromTypes = new ArrayList<>();
        for (TemplateUtil template : getTemplates()) {
            if (template.getType() == type) {
                templatesFromTypes.add(template);
            }
        }
        return templatesFromTypes;
    }
}
