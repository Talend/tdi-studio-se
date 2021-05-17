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
package org.talend.repository.constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.talend.repository.ui.wizards.exportjob.JavaJobScriptsExportWSWizardPage.JobExportType;

public class BuildJobConstants {

    public static final Map<JobExportType, String> oldBuildTypeMap = new HashMap<>();

    public static final List<String> esbComponents;

    public static final Set<String> ESB_CXF_COMPONENTS;

    static {
        // from the extension point
        oldBuildTypeMap.put(JobExportType.POJO, "STANDALONE");
        oldBuildTypeMap.put(JobExportType.OSGI, "OSGI");
        oldBuildTypeMap.put(JobExportType.MSESB, "REST_MS");
        oldBuildTypeMap.put(JobExportType.MSESB_IMAGE, "REST_MS");

        esbComponents = Collections.unmodifiableList(Arrays.asList("tRESTClient", "tRESTRequest", "tRESTResponse", "tESBConsumer",
                "tESBProviderFault", "tESBProviderRequest", "tESBProviderResponse", "tRouteInput", "tREST"));

        // components contain cxf-core-**.jar
        ESB_CXF_COMPONENTS = Collections
                .unmodifiableSet(new HashSet<>(Arrays.asList("tMicrosoftCrmInput", "tMicrosoftCrmOutput", "tWebService")));
    }
}
