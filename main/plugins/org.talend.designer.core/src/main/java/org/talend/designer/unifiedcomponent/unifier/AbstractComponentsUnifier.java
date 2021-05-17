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
package org.talend.designer.unifiedcomponent.unifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.FileLocator;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.components.ComponentCategory;
import org.talend.designer.unifiedcomponent.delegate.service.IComponentDelegate;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * created by wchen on Dec 5, 2017 Detailled comment
 *
 */
public abstract class AbstractComponentsUnifier implements IComponentsUnifier {

    // key : unified component name, value : a list of json object mapped to this unified component
    protected Map<String, JSONObject> unifiedMap = new HashMap<String, JSONObject>();

    protected IComponentDelegate unifiedComp;

    public AbstractComponentsUnifier() {
        readJson();
    }

    protected void readJson() {
        try {
            BufferedReader br = null;
            JSONArray jsonArray = new JSONArray();
            File file = new File(FileLocator.toFileURL(this.getClass().getResource("unifier.json")).getFile());
            if (file.exists()) {
                br = new BufferedReader(new FileReader(file));
                StringBuffer buffer = new StringBuffer();
                String line = null;
                while ((line = br.readLine()) != null) {
                    buffer.append(line);
                }
                jsonArray = JSONArray.fromObject(buffer.toString());
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    String unifiedKey = object.getString("component_delegate");
                    unifiedMap.put(unifiedKey, object);
                }
            }
        } catch (IOException e) {
            ExceptionHandler.process(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.unifiedcomponent.service.IComponentUnifier#getUnifiedComponent()
     */
    @Override
    public IComponentDelegate getDelegateComponent() {
        return unifiedComp;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.unifiedcomponent.service.IComponentUnifier#setUnifiedComponent(org.talend.designer.
     * unifiedcomponent.comp.service.IUnifiedComponent)
     */
    @Override
    public void setDelegateComponent(IComponentDelegate component) {
        this.unifiedComp = component;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.unifiedcomponent.service.IComponentUnifier#getComponentName()
     */
    @Override
    public String getComponentName() {
        JSONObject jsonObject = unifiedMap.get(unifiedComp.getComponentName());
        if (jsonObject != null) {
            return jsonObject.getString("component");
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.unifiedcomponent.service.IComponentUnifier#getParamMap()
     */
    @Override
    public Map<String, String> getParameterMapping() {
        JSONObject jsonObject = unifiedMap.get(unifiedComp.getComponentName());
        if (jsonObject != null) {
            if (jsonObject.has("parameter")) {
                JSONObject paramObject = jsonObject.getJSONObject("parameter");
                if (paramObject.has("mapping")) {
                    JSONObject mappingObj = paramObject.getJSONObject("mapping");
                    Map<String, String> repValueAndParamMap = new HashMap<String, String>();
                    for (Object object : mappingObj.keySet()) {
                        String repValue = object.toString();
                        repValueAndParamMap.put(repValue, mappingObj.getString(repValue));
                    }
                    return repValueAndParamMap;
                }
            }
        }

        return new HashMap<String, String>();
    }

    @Override
    public Set<String> getMappingExclude() {
        Set<String> mappingExclude = new HashSet<String>();
        JSONObject jsonObject = unifiedMap.get(unifiedComp.getComponentName());
        if (jsonObject != null) {
            if (jsonObject.has("parameter")) {
                JSONObject paramObject = jsonObject.getJSONObject("parameter");
                if (paramObject.has("exclude")) {
                    String string = paramObject.getString("exclude");
                    String[] split = string.split(",");
                    for (String category : split) {
                        mappingExclude.add(category);
                    }
                }
            }
        }
        return mappingExclude;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.unifiedcomponent.unifier.IComponentsUnifier#getConnectorMapping()
     */
    @Override
    public Map<String, String> getConnectorMapping() {
        JSONObject jsonObject = unifiedMap.get(unifiedComp.getComponentName());
        if (jsonObject != null) {
            if (jsonObject.has("connector")) {
                JSONObject paramObject = jsonObject.getJSONObject("connector");
                if (paramObject.has("mapping")) {
                    JSONObject mappingObj = paramObject.getJSONObject("mapping");
                    Map<String, String> repValueAndParamMap = new HashMap<String, String>();
                    for (Object object : mappingObj.keySet()) {
                        String repValue = object.toString();
                        repValueAndParamMap.put(repValue, mappingObj.getString(repValue));
                    }
                    return repValueAndParamMap;
                }
            }
        }

        return new HashMap<String, String>();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.unifiedcomponent.unifier.IComponentsUnifier#getCategories()
     */
    @Override
    public Set<String> getCategories() {
        Set<String> supportedCategories = new HashSet<String>();
        JSONObject jsonObject = unifiedMap.get(unifiedComp.getComponentName());
        if (jsonObject != null && jsonObject.has("category")) {
            String categories = jsonObject.getString("category");
            String[] split = categories.split(",");
            for (String category : split) {
                supportedCategories.add(category);
            }
        }
        if (supportedCategories.isEmpty()) {
            supportedCategories.add(ComponentCategory.CATEGORY_4_DI.getName());
        }
        return supportedCategories;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.unifiedcomponent.unifier.IComponentsUnifier#getFamilies()
     */
    @Override
    public Set<String> getFamilies() {
        Set<String> hideFamilies = new HashSet<String>();
        JSONObject jsonObject = unifiedMap.get(unifiedComp.getComponentName());
        if (jsonObject != null && jsonObject.has("family")) {
            String families = jsonObject.getString("family");
            String[] split = families.split(",");
            for (String category : split) {
                hideFamilies.add(category);
            }
        }
        return hideFamilies;
    }
}
