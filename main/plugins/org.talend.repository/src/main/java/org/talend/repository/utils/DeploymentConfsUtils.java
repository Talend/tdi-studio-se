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
package org.talend.repository.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.model.Model;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.designer.maven.model.TalendMavenConstants;
import org.talend.designer.maven.tools.AggregatorPomsHelper;
import org.talend.designer.maven.utils.PomIdsHelper;
import org.talend.designer.maven.utils.PomUtil;
import org.talend.repository.model.DeploymentConfsModel;
import org.talend.utils.json.JSONException;
import org.talend.utils.json.JSONObject;
import org.talend.utils.json.JSONTokener;

/**
 * DOC zwxue class global comment. Detailled comment
 *
 * @deprecated
 */
public class DeploymentConfsUtils {

    private final String JSON_FILE_NAME = "conf.json"; //$NON-NLS-1$

    private Project project;

    private AggregatorPomsHelper aggregatorPomsHelper;

    public DeploymentConfsUtils(Project project) {
        this.project = project;
        aggregatorPomsHelper = new AggregatorPomsHelper(project.getTechnicalLabel());
    }

    public Map<String, DeploymentConfsModel> loadConfs() {
        Map<String, DeploymentConfsModel> confs = new LinkedHashMap<>();
        IFolder deploymentsFolder = aggregatorPomsHelper.getDeploymentsFolder();
        try {
            for (IResource resource : deploymentsFolder.members()) {
                if (resource instanceof IFolder) {
                    IFolder confFolder = (IFolder) resource;
                    IFile jsonFile = confFolder.getFile(JSON_FILE_NAME);
                    String confName = confFolder.getName();
                    if (jsonFile.exists()) {
                        DeploymentConfsModel confsModel = new DeploymentConfsModel(confName);
                        retrieveModulesFromJsonFile(confsModel.getModules(), jsonFile.getLocation().toFile());
                        confs.put(confName, confsModel);
                    }
                }
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        return confs;
    }

    private String readJsonFile(File file) {
        String json = null;
        BufferedReader reader = null;
        try {
            StringBuilder builder = new StringBuilder();
            reader = new BufferedReader(new FileReader(file));
            String str = null;
            while ((str = reader.readLine()) != null) {
                builder.append(str + System.getProperty("line.separator")); //$NON-NLS-1$
            }
            json = builder.toString();
        } catch (Exception e) {
            ExceptionHandler.process(e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                //
            }
        }
        return json;
    }

    private void retrieveModulesFromJsonFile(Map<String, String> modules, File file) throws Exception {
        modules.clear();
        String jsonStr = readJsonFile(file);
        JSONObject object = new OrderedJSONObject(jsonStr);
        Iterator<String> iterator = object.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            String value = object.getString(key);
            modules.put(key, value);
        }
    }

    public void writeConfModelToJsonFile(DeploymentConfsModel confModel) throws Exception {
        IFolder deploymentsFolder = aggregatorPomsHelper.getDeploymentsFolder();
        IFolder confFolder = deploymentsFolder.getFolder(confModel.getConfName());
        if (!confFolder.exists()) {
            confFolder.create(true, true, null);
        }
        IFile jsonFile = confFolder.getFile(JSON_FILE_NAME);
        Map<String, String> modules = confModel.getModules();
        if (!modules.isEmpty()) {
            JSONObject object = new OrderedJSONObject(modules);
            InputStream source = new ByteArrayInputStream(object.toString().getBytes());
            if (!jsonFile.exists()) {
                jsonFile.create(source, true, null);
            } else {
                jsonFile.setContents(source, true, false, null);
            }
            updateConfPom(confFolder, modules.values());
        }
    }

    public void removeDeletedConfsFolder(Set<String> confsToRemove) throws Exception {
        IFolder deploymentsFolder = aggregatorPomsHelper.getDeploymentsFolder();
        for (IResource resource : deploymentsFolder.members()) {
            if (resource instanceof IFolder) {
                IFolder confFolder = (IFolder) resource;
                if (confsToRemove.contains(confFolder.getName())) {
                    confFolder.delete(true, false, null);
                }
            }
        }
        deploymentsFolder.refreshLocal(IResource.DEPTH_ONE, null);
    }

    public String getModuleId(IRepositoryViewObject object) {
        return object.getId() + "|" + object.getVersion(); //$NON-NLS-1$
    }

    public String calculateModulePath(IRepositoryViewObject object) {
        String realVersion = object.getVersion();
        Property property = object.getProperty();
        IPath path = getJobProjectPath(property, realVersion);
        path = path.makeRelativeTo(aggregatorPomsHelper.getProjectPomsFolder().getLocation());
        String pathStr = "../../" + path.toPortableString(); //$NON-NLS-1$
        return pathStr;
    }

    public static IPath getJobProjectPath(Property property, String realVersion) {
        return AggregatorPomsHelper.getItemPomFolder(property, realVersion).getLocation();
    }

    private void updateConfPom(IFolder confFolder, Collection<String> modules) throws Exception {
        IFile pomFile = confFolder.getFile(TalendMavenConstants.POM_FILE_NAME);
        Model model = new Model();
        model.setModelVersion("4.0.0"); //$NON-NLS-1$
        model.setGroupId(
                TalendMavenConstants.DEFAULT_GROUP_ID + "." + project.getTechnicalLabel().toLowerCase() + ".deployments"); //$NON-NLS-1$ //$NON-NLS-2$
        model.setArtifactId(confFolder.getName());
        model.setVersion(PomIdsHelper.getProjectVersion());
        model.setPackaging(TalendMavenConstants.PACKAGING_POM);

        List<String> list = model.getModules();
        if (list == null) {
            list = new ArrayList<>();
            model.setModules(list);
        }
        list.addAll(modules);
        PomUtil.savePom(null, model, pomFile);
    }

    class OrderedJSONObject extends JSONObject {

        OrderedJSONObject(Map map) {
            this.map = (map == null) ? new LinkedHashMap() : map;
        }

        OrderedJSONObject(String source) throws JSONException {
            map = new LinkedHashMap();
            JSONTokener x = new JSONTokener(source);
            char c;
            String key;

            if (x.nextClean() != '{') {
                throw x.syntaxError("A JSONObject text must begin with '{'");
            }
            for (;;) {
                c = x.nextClean();
                switch (c) {
                case 0:
                    throw x.syntaxError("A JSONObject text must end with '}'");
                case '}':
                    return;
                default:
                    x.back();
                    key = x.nextValue().toString();
                }

                /*
                 * The key is followed by ':'. We will also tolerate '=' or '=>'.
                 */

                c = x.nextClean();
                if (c == '=') {
                    if (x.next() != '>') {
                        x.back();
                    }
                } else if (c != ':') {
                    throw x.syntaxError("Expected a ':' after a key");
                }
                putOnce(key, x.nextValue());

                /*
                 * Pairs are separated by ','. We will also tolerate ';'.
                 */

                switch (x.nextClean()) {
                case ';':
                case ',':
                    if (x.nextClean() == '}') {
                        return;
                    }
                    x.back();
                    break;
                case '}':
                    return;
                default:
                    throw x.syntaxError("Expected a ',' or '}'");
                }
            }
        }

        @Override
        public Iterator sortedKeys() {
            // won't do sort
            return map.keySet().iterator();
        }

    }

}
