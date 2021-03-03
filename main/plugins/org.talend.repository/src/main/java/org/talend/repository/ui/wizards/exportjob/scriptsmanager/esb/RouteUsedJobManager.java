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
package org.talend.repository.ui.wizards.exportjob.scriptsmanager.esb;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.talend.core.model.properties.ProcessItem;
import org.talend.designer.runprocess.IProcessor;
import org.talend.repository.documentation.ExportFileResource;

import aQute.bnd.osgi.Analyzer;

/**
 * Exclusively used for route used job through cTalnedJob component. Which means the job must contains
 * tRouteInput/tRouteOutput node.<br>
 * The export service instance of generated job bundle will provide detailed information, such as {@link #routeName},
 * {@link #routeVersion}, {@link #bundleName}, ...
 *
 * @author GaoZone
 */
public class RouteUsedJobManager extends JobJavaScriptOSGIForESBManager {

    private String routeName;

    private String bundleName;

    private String routeVersion;

    private String groupId;

    private String artifactId;

    private String artifactVersion;

    public RouteUsedJobManager(Map<ExportChoice, Object> exportChoiceMap, String context) {
        super(exportChoiceMap, context, null, IProcessor.NO_STATISTICS, IProcessor.NO_TRACES);
        setMultiNodes(false);
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public void setBundleName(String bundleName) {
        this.bundleName = bundleName;
    }

    public void setRouteVersion(String routeVersion) {
        this.routeVersion = routeVersion;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public void setArtifactVersion(String artifactVersion) {
        this.artifactVersion = artifactVersion;
    }

    @Override
    protected Analyzer createAnalyzer(ExportFileResource libResource, ProcessItem processItem) throws IOException {
        Analyzer analyzer = super.createAnalyzer(libResource, processItem);

        Map<String, String> extraParams = new LinkedHashMap<String, String>();
        extraParams.put("version", getSelectedJobVersion());
        extraParams.put("bundleName", bundleName);
        extraParams.put("bundleVersion", getBundleVersion());
        extraParams.put("routeName", routeName);
        extraParams.put("routeVersion", routeVersion);
        extraParams.put("groupId", groupId);
        extraParams.put("artifactId", artifactId);
        extraParams.put("artifactVersion", artifactVersion);
        appendExportServiceParams(analyzer, extraParams);

        return analyzer;
    }

    private void appendExportServiceParams(Analyzer analyzer, Map<String, String> extraParams) {
        if (!extraParams.isEmpty()) {
            StringBuilder sb = new StringBuilder(analyzer.getProperty(Analyzer.EXPORT_SERVICE));
            for (Entry<String, String> e : extraParams.entrySet()) {
                if (e.getKey() == null || e.getValue() == null) {
                    continue;
                }
                sb.append(';').append(e.getKey() + "=" + e.getValue());
            }
            analyzer.setProperty(Analyzer.EXPORT_SERVICE, sb.toString());
        }
    }

}
