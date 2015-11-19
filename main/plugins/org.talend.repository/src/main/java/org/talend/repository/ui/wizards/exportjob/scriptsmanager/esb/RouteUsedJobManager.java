// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
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
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.talend.core.model.properties.ProcessItem;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.repository.documentation.ExportFileResource;

import aQute.bnd.osgi.Analyzer;

/**
 * Exclusively used for route used job through cTalnedJob component. Which means
 * the job must contains tRouteInput/tRouteOutput node.<br>
 * The export service instance of generated job bundle will provide detailed information,
 * such as {@link #routeName}, {@link #routeVersion}, {@link #bundleName}, ...
 *
 * @author GaoZone
 */
public class RouteUsedJobManager extends JobJavaScriptOSGIForESBManager {

    private static final String DLL = ".dll";
    private static final String OSGI_OS = System.getProperty("osgi.os");
    private static final String  OSGI_ARCH = System.getProperty("osgi.arch");
    
    private String routeName;
	private String bundleName;
	private String routeVersion;
	private String groupId;
	private String artifactId;
	private String artifactVersion;
    private List<URL> dllLibraries = new java.util.ArrayList<URL>();

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
		
        // TESB-15680: Add bundle manifest using the Bundle-NativeCode header
        appendBundleNativeCodeForDll(analyzer, libResource);

		return analyzer;
	}

    /**
     * Add Bundle-NativeCode in Manifest
     */
    private void appendBundleNativeCodeForDll(Analyzer analyzer, ExportFileResource libResource) {

        StringBuilder sb = new StringBuilder();
        for (URL url : libResource.getResourcesByRelativePath("")) {
            String path = url.toString();
            if (path.endsWith(DLL)) {
                dllLibraries.add(url);
                sb.append(path.substring(path.lastIndexOf('/') + 1, path.length())).append(';').append("osname=").append(OSGI_OS)
                        .append(';').append("processor=").append(OSGI_ARCH).append(',');
            }
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
            analyzer.setProperty(Analyzer.BUNDLE_NATIVECODE, sb.toString());
        }
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

    /*
     * Add DLLs in root path
     * 
     * @see
     * org.talend.repository.ui.wizards.exportjob.scriptsmanager.esb.JobJavaScriptOSGIForESBManager#getExportResources
     * (org.talend.repository.documentation.ExportFileResource[], java.lang.String[])
     */
    @Override
    public List<ExportFileResource> getExportResources(ExportFileResource[] processes, String... codeOptions)
            throws ProcessorException {
        List<ExportFileResource> list = super.getExportResources(processes, codeOptions);
        if (dllLibraries.size() > 0) {
            ExportFileResource dllResource = new ExportFileResource(null, ""); //$NON-NLS-1$;
            dllResource.addResources(dllLibraries);
            list.add(dllResource);
        }
        return list;
    }
}
