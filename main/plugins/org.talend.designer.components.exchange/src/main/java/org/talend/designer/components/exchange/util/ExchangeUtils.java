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
package org.talend.designer.components.exchange.util;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.discovery.tools.ManagedProperties;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.revisions.Revision;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.osgi.framework.Version;
import org.talend.commons.runtime.model.emf.EmfHelper;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.utils.StringUtils;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.components.IComponentsFactory;
import org.talend.core.model.general.Project;
import org.talend.core.runtime.util.SharedStudioUtils;
import org.talend.core.ui.component.ComponentPaletteUtilities;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.components.exchange.ExchangePlugin;
import org.talend.designer.components.exchange.model.ComponentExtension;
import org.talend.designer.components.exchange.model.ExchangePackage;
import org.talend.designer.components.exchange.model.RevisionInfo;
import org.talend.designer.components.exchange.model.VersionRevision;
import org.talend.repository.ProjectManager;

/**
 * DOC hcyi class global comment. Detailled comment
 */
public class ExchangeUtils {

    public static final String FOLDER_DOWNLOADED = "downloaded"; //$NON-NLS-1$

    public static String REVISION_LIST_URL = "http://talendforge.org/exchange/tos/api/get_revision_list.php"; //$NON-NLS-1$

    private static Pattern VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)(\\.(RC|M)\\d+)?_r\\d+"); //$NON-NLS-1$

    private static Pattern DEFAULT_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)\\.*(\\d*)"); //$NON-NLS-1$

    public final static String exchangeWSServer = "http://www.talendforge.org/exchange/webservices/"; //$NON-NLS-1$

    public static String TYPEEXTENSION = "tos"; //$NON-NLS-1$

    public static String VERSIONSTUDIO = "4.2"; //$NON-NLS-1$

    public static String CATEGORY = "6"; //$NON-NLS-1$

    private static MultiValueMap versionMap = new MultiValueMap();

    /**
     * Make sure that the version match x.x.x or x.x.xMx or x.x.xRCx, where x are all digit.
     *
     * @param version
     * @return
     */
    public static String normalizeVersion(String version) {
        Matcher matcher = VERSION_PATTERN.matcher(version);
        if (matcher.matches()) {
            String str = version.substring(0, version.indexOf("_r")); //$NON-NLS-1$
            return str.replaceAll("\\.RC", "RC").replaceAll("\\.M", "M"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        } else {
            // try again, ignore M, RC
            matcher = DEFAULT_PATTERN.matcher(version);
            matcher.find();
            return matcher.group();
        }
    }

    /**
     * This method is used for generating current T.O.S version.
     *
     * @return
     */
    public static String getCurrentTosVersion(boolean normalize) {
        String version = VersionUtils.getVersion();
        if (normalize) {
            version = normalizeVersion(version);
        }
        return version;
    }

    public static String getMainVersion(String version) {
        Pattern pattern = Pattern.compile("(\\d+\\.\\d+).*"); //$NON-NLS-1$
        Matcher matcher = pattern.matcher(version);
        if (matcher.matches()) {
            version = matcher.group(1);
        }
        return version;
    }

    public static List parseJsonObject(String jsonContent, Class clazz) throws Exception {
        // need factory for creating parser to use
        List objList = new ArrayList();

        // for 4.1.0 the is no json param on server ,so jsonContent is "wrong parameters for version"
        if (!jsonContent.startsWith("[")) {
            return objList;
        }
        JsonFactory jf = new JsonFactory();
        JsonNode node = new ObjectMapper().reader().readTree(jf.createJsonParser(new StringReader(jsonContent)));
        List<Object> list = new ObjectMapper().readValue(node.traverse(), List.class);
        for (Object source : list) {
            Object obj = clazz.newInstance();
            BeanUtils.copyProperties(obj, source);
            objList.add(obj);
        }
        return objList;
    }

    public static String sendGetRequest(String urlAddress) throws Exception {
        HttpClient httpclient = new HttpClient();
        GetMethod getMethod = new GetMethod(urlAddress);
        String proxyUser = ManagedProperties.getProperty("http.proxyUser");
        String proxyPassword = ManagedProperties.getProperty("http.proxyPassword");
        String proxyHost = ManagedProperties.getProperty("http.proxyHost");
        proxyHost = proxyHost != null ? proxyHost : "";
        String proxyPort = ManagedProperties.getProperty("http.proxyPort");
        if (proxyHost.length() != 0) {
            UsernamePasswordCredentials creds = new UsernamePasswordCredentials(
                    proxyUser != null ? proxyUser : "", proxyPassword != null ? proxyPassword : "");
            httpclient.getState().setProxyCredentials(AuthScope.ANY, creds);
            HostConfiguration hcf = new HostConfiguration();
            hcf.setProxy(proxyHost, Integer.parseInt(proxyPort));
            httpclient.executeMethod(hcf, getMethod);
        } else {
            httpclient.executeMethod(getMethod);
        }
        String response = getMethod.getResponseBodyAsString();
        getMethod.releaseConnection();
        return response;
    }

    public static String sendPostRequest(String urlAddress, Map<String, String> parameters) throws Exception {
        HttpClient httpclient = new HttpClient();
        PostMethod postMethod = new PostMethod(urlAddress);
        if (parameters != null) {
            NameValuePair[] postData = new NameValuePair[parameters.size()];
            int i = 0;
            for (String key : parameters.keySet()) {
                String value = parameters.get(key);
                postData[i++] = new NameValuePair(key, value);
            }
            postMethod.addParameters(postData);
        }

        httpclient.executeMethod(postMethod);
        String response = postMethod.getResponseBodyAsString();
        postMethod.releaseConnection();
        return response;
    }

    public static ECodeLanguage getCurrentLanguage() {
        return LanguageManager.getCurrentLanguage();
    }

    /**
     * Get the folder that will store downloaded component.
     *
     * @return
     */
    public static File getComponentFolder() {
        return getComponentFolder(FOLDER_DOWNLOADED);
    }

    /**
     * Get the folder that will store downloaded component.
     *
     * @return
     */
    public static File getComponentFolder(String componentfolder) {
    	if (SharedStudioUtils.isSharedStudioMode()) {
    		File componentFolder = SharedStudioUtils.getSharedStudioComponentsExtFolder();
    		return new File (componentFolder, componentfolder);
    	} else {
            URL url = FileLocator.find(ExchangePlugin.getDefault().getBundle(), new Path(componentfolder), null);
            try {
                URL fileUrl = FileLocator.toFileURL(url);
                return new File(fileUrl.getPath());
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
            return null;
    	}
    }

    /**
     * Displays the given preference page.
     *
     * @param pageId the fully qualified id of the preference page, e.g.
     * <code>org.eclipse.jdt.debug.ui.preferences.VMPreferencePage</code>
     *
     */
    public static void showPreferencePage(Shell shell, String pageId) {
        PreferencesUtil.createPreferenceDialogOn(shell, pageId, new String[] { pageId }, null).open();
    }

    /**
     * Reload all components from folder and update palette.
     */
    public static void reloadComponents() {
        // reload from folder, see ComponentsFactory and UserComponentsProvider
        IComponentsFactory componentsFactory = ComponentsFactoryProvider.getInstance();
        componentsFactory.resetCache();
        // update the palette view, the position of the new component is
        // determined by the FAMILY value in the
        // component's property file
        ComponentPaletteUtilities.updatePalette();
    }

    public static void deleteComponent(ComponentExtension component) {
        // File installFolder = new File(component.getInstalledLocation());
        // if (installFolder != null && installFolder.exists()) {
        // FilesUtils.removeFolder(installFolder, true);
        // }
        //
        // reloadComponents();
    }

    /**
     * Activate the user job.
     *
     * @param job
     */
    public static void scheduleUserJob(Job job) {
        job.setUser(true);
        job.setPriority(Job.INTERACTIVE);
        job.schedule();
        job.wakeUp(); // start as soon as possible
    }

    /**
     * Return true if revision1 is newer than revision2.
     *
     * @param revision1
     * @param revision2
     * @return
     */
    public static boolean isRevisionNewerThan(Revision revision1, Revision revision2) {
        return false;
    }

    // public static IAction findViewAction(String id) {
    // IAction action = null;
    // ExchangeView view = ExchangeUtils.getExchangeView();
    // action = ((ActionContributionItem) view.getViewSite().getActionBars().getToolBarManager().find(id)).getAction();
    // return action;
    // }

    /**
     * Save the emf model of downloaded components to file.
     *
     * @param fileName
     * @param components
     * @throws IOException
     */
    public static void saveDownloadedComponents(String fileName, List<ComponentExtension> components) throws IOException {
        File file = new File(getComponentFolder("downloaded"), fileName);
        EmfHelper.saveEmfModel(ExchangePackage.eINSTANCE, components, file.getAbsolutePath());
    }

    /**
     * Load the emf model of downloaded components from file.
     *
     * @param fileName
     * @return
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public static List<ComponentExtension> loadDownloadedComponents(String fileName) throws IOException {
        File file = new File(getComponentFolder("downloaded"), fileName);
        return EmfHelper.loadEmfModel(ExchangePackage.eINSTANCE, file.getAbsolutePath());
    }

    /**
     * Save the emf model of installed components to file.
     *
     * @param fileName
     * @param components
     * @throws IOException
     */
    public static void saveInstalledComponents(String fileName, List<ComponentExtension> components) throws IOException {
        File file = new File(getComponentFolder("components"), fileName);
        EmfHelper.saveEmfModel(ExchangePackage.eINSTANCE, components, file.getAbsolutePath());
    }

    /**
     * Load the emf model of installed components from file.
     *
     * @param fileName
     * @return
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public static List<ComponentExtension> loadInstalledComponents(String fileName) throws IOException {
        File file = new File(getComponentFolder("components"), fileName);
        return EmfHelper.loadEmfModel(ExchangePackage.eINSTANCE, file.getAbsolutePath());
    }

    public static String getPasswordHash() {
        Project proj = ProjectManager.getInstance().getCurrentProject();
        return proj.getExchangeUser().getPassword();
    }

    public static String getUserName() {
        Project proj = ProjectManager.getInstance().getCurrentProject();
        return proj.getExchangeUser().getUsername();
    }

    public static boolean checkUserAndPassword() {
        if (getUserName() != null && !getUserName().equals("") && getPasswordHash() != null && !getPasswordHash().equals("")) {
            return true;
        }
        return false;
    }

    public static String[] getVersionList(List<VersionRevision> fVersionRevisions) {
        versionMap.clear();
        try {

            Pattern pattern = Pattern.compile("(\\d+\\.\\d+).*"); //$NON-NLS-1$
            for (VersionRevision info : fVersionRevisions) {
                String name = info.getVersionName();
                Matcher matcher = pattern.matcher(name);
                if (matcher.matches()) {
                    versionMap.put(matcher.group(1), String.valueOf(info.getVersionId()));
                }
            }
            // sort version
            List<String> versions = new ArrayList<String>(versionMap.keySet());
            Collections.sort(versions, new Comparator<String>() {

                @Override
                public int compare(String o1, String o2) {
                    Version ver1 = new Version(o1);
                    Version ver2 = new Version(o2);
                    return ver2.compareTo(ver1);
                }

            });
            return versions.toArray(new String[versions.size()]);
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        return new String[0];
    }

    public static List<RevisionInfo> getRevisionList(String version, int language, String type) throws Exception {
        StringBuffer url = new StringBuffer();
        url.append(REVISION_LIST_URL).append("?categories=").append(type).append("&version="); //$NON-NLS-1$ //$NON-NLS-2$
        String[] branch = getBranch(version);
        url.append(StringUtils.join(branch, ",")); //$NON-NLS-1$
        String jsonContent = sendGetRequest(url.toString());
        return parseJsonObject(jsonContent, RevisionInfo.class);

    }

    public static String[] getBranch(String version) {
        version = getMainVersion(version);
        if (versionMap.getCollection(version) == null) {

        }
        Collection branch = versionMap.getCollection(version);
        if (branch != null) {
            return (String[]) branch.toArray(new String[branch.size()]);
        } else {
            return new String[0];
        }
    }
}
