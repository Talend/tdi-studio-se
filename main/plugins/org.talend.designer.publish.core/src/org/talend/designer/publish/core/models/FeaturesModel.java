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
package org.talend.designer.publish.core.models;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.talend.utils.xml.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class FeaturesModel extends BaseModel {

    public static final String NAME_SUFFIX = "-feature";

    public static final String CORRELATION_FEATURE_NAME = "tesb-policy-correlationid";

    public static final String TALEND_DATA_MAPPER_FEATURE_NAME = "talend-data-mapper";

    private final String name;

    private String configName;

    private Collection<FeatureModel> subFeatures = new HashSet<FeatureModel>();

    private Collection<BundleModel> subBundles = new HashSet<BundleModel>();

    private String[] contextList = new String[] { "Default" };

    private Map<String, Map<String, String>> contexts = new HashMap<String, Map<String, String>>();

    public FeaturesModel(String groupId, String namePrefix, String version) {
        super(groupId, namePrefix + NAME_SUFFIX, version);
        name = namePrefix;
    }

    @Override
    public String getExtension() {
        return "xml";
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public void addFeature(FeatureModel feature) {
        subFeatures.add(feature);
    }

    public boolean addBundle(BundleModel model) {
        return subBundles.add(model);
    }

    public Collection<FeatureModel> getFeatures() {
        return subFeatures;
    }

    public Collection<BundleModel> getBundles() {
        return subBundles;
    }

    private void setContextList(String[] contextList) {
        if (contextList != null) {
            this.contextList = contextList;
        }
    }

    public void setContexts(Map<String, Map<String, String>> contexts) {
        Collection<String> contextNames = new HashSet<String>(Arrays.asList(this.contextList));
        contextNames.addAll(contexts.keySet());
        setContextList((String[]) contextNames.toArray(new String[0]));
        for (Map.Entry<String, Map<String, String>> context : contexts.entrySet()) {
            String contextName = context.getKey();
            if (this.contexts.containsKey(contextName)) {
                this.contexts.get(contextName).putAll(context.getValue());
            } else {
                this.contexts.put(contextName, context.getValue());
            }
        }
        this.contexts = contexts;
    }

    private static String toBundleString(BundleModel model) {
        StringBuilder sb = new StringBuilder("mvn:");
        sb.append(model.getGroupId());
        sb.append('/');
        sb.append(model.getArtifactId());
        sb.append('/');
        sb.append(model.getVersion());
        return sb.toString();
    }

    public InputStream getContent() {
        try {
            return internalGetContent();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream internalGetContent() throws Exception {
        Document document = XmlUtils.getSecureDocumentBuilderFactory().newDocumentBuilder().newDocument();

        Element features = document.createElement("features");
        features.setAttribute("xmlns", "http://karaf.apache.org/xmlns/features/v1.6.0");
        features.setAttribute("name", getArtifactId());
        document.appendChild(features);

        Element feature = document.createElement("feature");
        feature.setAttribute("name", getArtifactId());
        feature.setAttribute("version", getVersion());
        features.appendChild(feature);

        // add sub features
        for (FeatureModel fm : subFeatures) {
            Element subFeature = document.createElement("feature");
            if (null != fm.getVersion() && !fm.getVersion().isEmpty()) {
                subFeature.setAttribute("version", fm.getVersion());
            }
            subFeature.appendChild(document.createTextNode(fm.getArtifactId()));
            feature.appendChild(subFeature);
        }

        // add sub bundles
        for (BundleModel bm : subBundles) {
            Element bundle = document.createElement("bundle");
            bundle.appendChild(document.createTextNode(toBundleString(bm)));
            feature.appendChild(bundle);
        }

        if (null == contexts || contexts.isEmpty()) {
            // add config
            Element config = document.createElement("config");
            config.setAttribute("name", configName);
            config.setAttribute("override", "true");
            StringBuilder sb = new StringBuilder("talendcontext=\"");
            for (int i = 0; i < contextList.length; i++) {
                if (i != 0) {
                    sb.append(',');
                }
                sb.append(contextList[i]);
            }
            sb.append('"');
            config.appendChild(document.createTextNode(sb.toString()));
            feature.appendChild(config);
        } else {
            // add contexts config
            for (Map.Entry<String, Map<String, String>> context : contexts.entrySet()) {
                Element config = document.createElement("config");
                config.setAttribute("name", name + ".talendcontext." + context.getKey());
                config.setAttribute("override", "true");
                StringBuilder sb = new StringBuilder("\n");
                for (Map.Entry<String, String> property : context.getValue().entrySet()) {
                    sb.append(property.getKey());
                    sb.append('=');
                    sb.append(property.getValue());
                    sb.append('\n');
                }
                config.appendChild(document.createTextNode(sb.toString()));
                feature.appendChild(config);
            }
        }

        Transformer transformer = XmlUtils.getXmlSecureTransform();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(document), new StreamResult(os));
        return new ByteArrayInputStream(os.toByteArray());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.publish.core.models.BaseModel#getClassifier()
     */
    @Override
    public String getClassifier() {
        return "features";
    }

    // public static void main(String[] args) throws java.io.IOException {
    // FeaturesModel featureModel = new FeaturesModel("aaa", "CustomService", "1.0.0");
    // featureModel.addBundle(new BundleModel("talend", "job-control-bundle", "1.0"));
    // featureModel.addBundle(new BundleModel("talend", "ProviderJob", "1.0"));
    // featureModel.addBundle(new BundleModel("talend", "ESBProvider2", "1.0"));
    // featureModel.addFeature(new FeatureModel("custom-feature", "2.0"));
    // featureModel.setContexts(java.util.Collections.singletonMap("name", java.util.Collections.singletonMap("key",
    // "開始")));
    // InputStream is = featureModel.getContent();
    // byte[] b = new byte[is.available()];
    // is.read(b);
    // System.out.println(new String(b));
    // }
}
