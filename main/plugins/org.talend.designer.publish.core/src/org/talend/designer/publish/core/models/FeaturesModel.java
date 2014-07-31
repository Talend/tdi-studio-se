// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;

public class FeaturesModel extends BaseModel {

	public static final String NAME_SUFFIX = "-feature";

	public static final String ESB_FEATURE_VERSION_RANGE = "[5,6)";
	
	public static final String CORRELATION_FEATURE_NAME = "tesb-policy-correlationid";
	
	public static final String TALEND_DATA_MAPPER_FEATURE_NAME = "talend-data-mapper";

	private final String name;

	private String configName;

	private Collection<String> subFeatures = new HashSet<String>();

	private Collection<BundleModel> subBundles = new HashSet<BundleModel>();

	private String[] contextList = new String[] { "Default" };
	private Map<String, Map<String, String>> contexts = new HashMap<String, Map<String, String>>();

	public FeaturesModel(String groupId, String namePrefix, String version) {
		super(groupId, namePrefix + NAME_SUFFIX, version, "pom");
		name = namePrefix;
	}

	public void setConfigName(String configName) {
		this.configName = configName;
	}

	public void addFeature(FeatureModel feature) {
		StringBuilder sb = new StringBuilder();
		sb.append("<feature version='");
		sb.append(feature.getVersion());
		sb.append("\'>");
		sb.append(feature.getArtifactId());
		sb.append("</feature>");
		subFeatures.add(sb.toString());
	}

	public boolean addBundle(BundleModel model) {
		return subBundles.add(model);
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
		StringBuilder sb = new StringBuilder();
		sb.append("<bundle>mvn:");
		sb.append(model.getGroupId());
		sb.append('/');
		sb.append(model.getArtifactId());
		sb.append('/');
		sb.append(model.getVersion());
		sb.append("</bundle>");
		return sb.toString();
	}

	public String getContent() {
		StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sb.append("<features name=\"").append(getArtifactId()).append("\" xmlns=\"http://karaf.apache.org/xmlns/features/v1.0.0\">\n");
		sb.append("\t<feature name=\"");
		sb.append(getArtifactId());
		sb.append("\" version=\"");
		sb.append(getVersion());
		sb.append("\">\n");
		// add sub features
		for (String s : subFeatures) {
			sb.append("\t\t");
			sb.append(s);
			sb.append("\n");
		}

		// add sub bundles
		for (BundleModel s : subBundles) {
			sb.append("\t\t");
			sb.append(toBundleString(s));
			sb.append("\n");
		}

		if (null == contexts || contexts.isEmpty()) {
			// add config
			sb.append("\t\t<config name=\"");
			sb.append(configName);
			sb.append("\">\n");
			sb.append("\t\t\ttalendcontext=\"");
			for (int i = 0; i < contextList.length; i++) {
				if (i != 0) {
					sb.append(',');
				}
				sb.append(StringEscapeUtils.escapeXml(contextList[i]));
			}
			sb.append("\"\n");
			sb.append("\t\t</config>\n");
		} else {
			// add contexts config
			for (Map.Entry<String, Map<String, String>> context : contexts.entrySet()) {
				sb.append("\t\t<config name=\"");
				sb.append(name).append(".talendcontext.").append(StringEscapeUtils.escapeXml(context.getKey()));
				sb.append("\">\n");
				for (Map.Entry<String, String> property : context.getValue().entrySet()) {
					sb.append("\t\t\t");
					sb.append(StringEscapeUtils.escapeXml(property.getKey()));
					sb.append('=');
					sb.append(StringEscapeUtils.escapeXml(property.getValue()));
					sb.append("\n");
				}
				sb.append("\t\t</config>\n");
			}
		}
		
		sb.append("\t</feature>\n");
		sb.append("</features>");

		return sb.toString();
	}

//	public static void main(String[] args) {
//		FeaturesModel featureModel = new FeaturesModel("aaa",
//				"CustomService", "1.0.0");
//		featureModel.addBundle(new BundleModel("talend", "job-control-bundle", "1.0"));
//		featureModel.addBundle(new BundleModel("talend", "ProviderJob", "1.0"));
//		featureModel.addBundle(new BundleModel("talend", "ESBProvider2", "1.0"));
//		featureModel.addFeature(new FeatureModel("custom-feature", "2.0"));
//		featureModel.setConfigName("aa.bb");
//		featureModel
//				.setContextList(new String[] { "Default", "Product", "Dev" });
//		System.out.println(featureModel.getContent());
//	}

}
