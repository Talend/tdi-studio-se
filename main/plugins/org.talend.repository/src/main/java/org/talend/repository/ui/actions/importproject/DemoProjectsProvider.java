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
package org.talend.repository.ui.actions.importproject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.axis.utils.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.SafeRunner;
import org.talend.commons.utils.resource.FileExtensions;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.core.runtime.repository.demo.IDemoProjectValidator;
import org.talend.core.utils.RegistryReader;

/**
 * DOC ggu class global comment. Detailled comment
 */
public final class DemoProjectsProvider extends RegistryReader {

    private final static DemoProjectsProvider instance = new DemoProjectsProvider();

    public static DemoProjectsProvider getInstance() {
        return instance;
    }

    static class DemoProvider {

        String id;

        String name;

        String description;

        String overrideId;

        int order;

        String descHtml;

        String projectUrl;

        String iconUrl;

        IConfigurationElement curConfigElement;

        IDemoProjectValidator validator;

        /*
         *
         */
        String pluginId;

    }

    private final static Logger log = Logger.getLogger(DemoProjectsProvider.class);

    private Map<String, DemoProvider> allDemoProviders;

    private DemoProjectsProvider() {
        super(CoreRuntimePlugin.PLUGIN_ID, "demoProjects_provider"); //$NON-NLS-1$
    }

    public DemoProjectBean[] getDemoProjects() {
        init();

        Map<String, DemoProvider> finalDemoProviders = new HashMap<String, DemoProvider>(allDemoProviders);

        // for override.
        List<String> overrideIds = new ArrayList<String>();
        // for invalid
        List<String> invalidIds = new ArrayList<String>();
        Iterator<String> iterator = finalDemoProviders.keySet().iterator();
        while (iterator.hasNext()) {
            String id = iterator.next();
            DemoProvider demoProvider = finalDemoProviders.get(id);
            String overrideId = demoProvider.overrideId;
            // existed
            if (overrideId != null && finalDemoProviders.containsKey(overrideId)) {
                overrideIds.add(overrideId);
            }
            // not valid
            IDemoProjectValidator validator = demoProvider.validator;
            if (validator != null && !validator.validate(demoProvider.curConfigElement)) {
                invalidIds.add(id);
            }
        }
        // remove the override
        for (String overrideId : overrideIds) {
            finalDemoProviders.remove(overrideId);
        }
        // remove the invalid
        for (String invalidId : invalidIds) {
            finalDemoProviders.remove(invalidId);
        }

        List<DemoProvider> finalDemoProvidersList = new ArrayList<DemoProvider>(finalDemoProviders.values());
        // sort by order.
        Collections.sort(finalDemoProvidersList, new Comparator<DemoProvider>() {

            @Override
            public int compare(DemoProvider dp1, DemoProvider dp2) {
                return dp1.order - dp2.order;
            }
        });

        // create demo bean
        List<DemoProjectBean> finalDemoProjects = new ArrayList<DemoProjectBean>();
        for (DemoProvider demoProvider : finalDemoProvidersList) {
            DemoProjectBean demoBean = new DemoProjectBean();
            demoBean.setProjectName(demoProvider.name);
            demoBean.setPluginId(demoProvider.pluginId);
            demoBean.setDescriptionFilePath(demoProvider.descHtml);
            demoBean.setDemoProjectFilePath(demoProvider.projectUrl);
            demoBean.setIconUrl(demoProvider.iconUrl);
            demoBean.setDemoDesc(demoProvider.description);
            if (demoBean.getDemoProjectFilePath().endsWith(FileExtensions.ZIP_FILE_SUFFIX)
                    || demoBean.getDemoProjectFilePath().endsWith(FileExtensions.TAR_FILE_SUFFIX)
                    || demoBean.getDemoProjectFilePath().endsWith(FileExtensions.TAR_GZ_FILE_SUFFIX)) {
                demoBean.setDemoProjectFileType(EDemoProjectFileType.ARCHIVE);
            } else {
                demoBean.setDemoProjectFileType(EDemoProjectFileType.FOLDER);
            }
            finalDemoProjects.add(demoBean);
        }

        return finalDemoProjects.toArray(new DemoProjectBean[0]);
    }

    void init() {
        if (allDemoProviders == null) {
            synchronized (DemoProjectsProvider.class) {
                allDemoProviders = new HashMap<String, DemoProvider>();
                readRegistry();
            }
        }
    }

    @Override
    protected boolean readElement(final IConfigurationElement element) {
        if ("demoProvider".equals(element.getName())) { //$NON-NLS-1$
            SafeRunner.run(new RegistryReader.RegistrySafeRunnable() {

                @Override
                public void run() throws Exception {
                    String id = element.getAttribute("id"); //$NON-NLS-1$
                    String name = element.getAttribute("name"); //$NON-NLS-1$
                    String description = element.getAttribute("description"); //$NON-NLS-1$
                    String descHtml = element.getAttribute("descHtml"); //$NON-NLS-1$
                    String projectUrl = element.getAttribute("projectUrl"); //$NON-NLS-1$
                    String iconUrl = element.getAttribute("icon"); //$NON-NLS-1$
                    if (StringUtils.isEmpty(id) || StringUtils.isEmpty(name) || StringUtils.isEmpty(descHtml)
                            || StringUtils.isEmpty(projectUrl)) {
                        log.error("Some provider miss some attributes: id=" + id + ", name= " + name); //$NON-NLS-1$//$NON-NLS-2$
                        return;
                    }

                    String overrideId = element.getAttribute("overrideId"); //$NON-NLS-1$
                    if (StringUtils.isEmpty(overrideId)) {
                        overrideId = null;
                    }
                    String orderStr = element.getAttribute("order"); //$NON-NLS-1$
                    if (StringUtils.isEmpty(orderStr)) {
                        orderStr = null;
                    }
                    int order = -1;
                    try {
                        order = Integer.parseInt(orderStr);
                    } catch (NumberFormatException e) {
                        //
                    }

                    DemoProvider demoProvider = new DemoProvider();
                    demoProvider.id = id;
                    demoProvider.name = name;
                    demoProvider.description = description;
                    demoProvider.order = order;
                    demoProvider.overrideId = overrideId;
                    demoProvider.descHtml = descHtml;
                    demoProvider.projectUrl = projectUrl;
                    demoProvider.iconUrl = iconUrl;

                    demoProvider.curConfigElement = element;
                    demoProvider.pluginId = element.getContributor().getName();

                    // validator
                    try {
                        demoProvider.validator = (IDemoProjectValidator) element.createExecutableExtension("validator"); //$NON-NLS-1$;
                    } catch (CoreException e) {
                        //
                    }
                    allDemoProviders.put(id, demoProvider);
                }
            });
        }
        return false;
    }

}
