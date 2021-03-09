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
package org.talend.designer.components.exchange.jobs;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.language.ECodeLanguage;
import org.talend.designer.components.exchange.model.AvailableExtensionViewDetail;
import org.talend.designer.components.exchange.model.ComponentExtension;
import org.talend.designer.components.exchange.model.ExchangeFactory;
import org.talend.designer.components.exchange.model.Language;
import org.talend.designer.components.exchange.model.RevisionInfo;
import org.talend.designer.components.exchange.util.ExchangeUtils;
import org.talend.designer.components.exchange.util.ExchangeWebService;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONObject;

/**
 *
 * DOC hcyi class global comment. Detailled comment
 */
public class ComponentSearcher {

    private static final String RELEASE_DATE_FORMAT = "yyyy-MM-dd"; //$NON-NLS-1$

    private static DateFormat formatter = new SimpleDateFormat(RELEASE_DATE_FORMAT);

    /**
     * Find available components.
     *
     * @param version The tos version.
     * @param language The project language.
     * @return
     */
    public static List<ComponentExtension> getAvailableComponentExtensions(String typeExtensionTemp, String versionStudioTemp,
            ECodeLanguage language, String category) {
        List<ComponentExtension> extensions = new ArrayList<ComponentExtension>();
        try {
            JSONArray extensionsJSONArray = ExchangeWebService.searchExtensionJSONArray(typeExtensionTemp, versionStudioTemp,
                    category);
            if (extensionsJSONArray != null) {
                int size = extensionsJSONArray.length();
                for (int i = 0; i < size; i++) {
                    JSONObject extensionObj = extensionsJSONArray.getJSONObject(i);
                    Map<String, ComponentExtension> extensionsMap = new HashMap<String, ComponentExtension>();
                    ComponentExtension extension = extensionsMap.get(extensionObj.getString("label"));
                    if (extension == null) {
                        extension = ExchangeFactory.eINSTANCE.createComponentExtension();
                        //
                        JSONObject extensionsViewDetailObj = ExchangeWebService.searchExtensionViewDetail(
                                extensionObj.getString("idExtension"), "tos");
                        extension.setIdExtension(extensionObj.getString("idExtension"));
                        extension.setLabel(extensionObj.getString("label"));
                        extension.setAuthor(extensionObj.getString("author"));
                        extension.setRate(extensionObj.getString("rate"));
                        extension.setTypeExtension(extensionObj.getString("typeExtension"));
                        extension.setVersionExtension(extensionObj.getString("versionExtension"));
                        if (extensionsViewDetailObj.length() > 0) {
                            extension.setLastVersionAvailable(extensionsViewDetailObj.getString("lastVersionAvailable"));
                            extension.setPublicationDate(formatter.parse(extensionsViewDetailObj.getString("publicationDate")));
                            extension.setDescription(extensionsViewDetailObj.getString("description"));
                            //
                            List<AvailableExtensionViewDetail> reviews = new ArrayList<AvailableExtensionViewDetail>();
                            if ((extensionsViewDetailObj.get("reviews")) != null
                                    && (extensionsViewDetailObj.get("reviews") instanceof JSONArray)) {
                                JSONArray o = extensionsViewDetailObj.getJSONArray("reviews");
                                int sumRate = 0;
                                int sizeDetail = o.length();
                                for (int j = 0; j < sizeDetail; j++) {
                                    JSONObject review = o.getJSONObject(j);
                                    AvailableExtensionViewDetail extensionViewDetail = ExchangeFactory.eINSTANCE
                                            .createAvailableExtensionViewDetail();
                                    extensionViewDetail.setAuthor(review.getString("author"));
                                    extensionViewDetail.setTitle(review.getString("title"));
                                    extensionViewDetail.setComment(review.getString("comment"));
                                    extensionViewDetail.setReviewrate(review.getString("rate"));
                                    reviews.add(extensionViewDetail);
                                    sumRate = sumRate + Integer.parseInt(review.getString("rate"));
                                }
                                //
                                if (sizeDetail > 0 && sumRate > 0) {
                                    int im = sumRate / sizeDetail;
                                    extension.setRate(im + "");
                                }
                            }
                            extension.getReviews().addAll((Collection<? extends AvailableExtensionViewDetail>) reviews);
                        }
                        extensionsMap.put(extension.getIdExtension(), extension);
                        extensions.add(extension);
                    }
                }
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }

        return extensions;

    }

    public static ComponentExtension getAvailableExtensionViewDetail(String idExtension, String typeExtension) {
        ComponentExtension extension = null;

        try {
            JSONObject extensionJSONObject = ExchangeWebService.searchExtensionViewDetail(idExtension, typeExtension);
            Map<String, ComponentExtension> extensionsMap = new HashMap<String, ComponentExtension>();
            extension = extensionsMap.get(extensionJSONObject.getString("label"));
            if (extension == null) {
                extension = ExchangeFactory.eINSTANCE.createComponentExtension();
                extension.setIdExtension(extensionJSONObject.getString("idExtension"));
                extension.setLabel(extensionJSONObject.getString("label"));
                extension.setLastVersionAvailable(extensionJSONObject.getString("lastVersionAvailable"));
                extension.setPublicationDate(formatter.parse(extensionJSONObject.getString("publicationDate")));
                extension.setDescription(extensionJSONObject.getString("description"));
                extension.setRate(extensionJSONObject.getString("rate"));
                extension.setAuthor(extensionJSONObject.getString("author"));
                //
                List<AvailableExtensionViewDetail> reviews = new ArrayList<AvailableExtensionViewDetail>();
                JSONArray o = extensionJSONObject.getJSONArray("Reviews");
                int size = o.length();
                for (int i = 0; i < size; i++) {
                    JSONObject review = o.getJSONObject(i);
                    AvailableExtensionViewDetail extensionViewDetail = ExchangeFactory.eINSTANCE
                            .createAvailableExtensionViewDetail();
                    extensionViewDetail.setAuthor(review.getString("author"));
                    extensionViewDetail.setTitle(review.getString("title"));
                    extensionViewDetail.setComment(review.getString("comment"));
                    extensionViewDetail.setReviewrate(review.getString("rate"));
                    reviews.add(extensionViewDetail);
                }
                extensionsMap.put(extension.getIdExtension(), extension);
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }

        return extension;

    }

    public static List<ComponentExtension> getImportComponentExtensions(String version, ECodeLanguage language, String type) {
        List<ComponentExtension> extensions = new ArrayList<ComponentExtension>();

        try {
            List<RevisionInfo> revisions = ExchangeUtils.getRevisionList(version, getLanguageId(language), type);

            Map<String, ComponentExtension> extensionsMap = new HashMap<String, ComponentExtension>();

            for (RevisionInfo revision : revisions) {
                ComponentExtension extension = extensionsMap.get(revision.getExtension_id());
                if (extension == null) {
                    extension = ExchangeFactory.eINSTANCE.createComponentExtension();
                    extension.setIdExtension(revision.getExtension_id() + "");
                    extension.setLabel(revision.getExtension_name());
                    extension.setAuthor(revision.getAuthor_name());
                    extension.setLanguage(Language.get(getLanguageId(language)));
                    extension.setDescription(revision.getExtension_description());
                    extension.setLinkDownload(revision.getDownload_url());
                    extension.setFilename(revision.getFilename());
                    extension.setVersionExtension(revision.getRevision_name());
                    extensionsMap.put(revision.getExtension_id() + "", extension);
                    extensions.add(extension);
                }
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }

        return extensions;

    }

    /**
     * Find Contributed components.
     *
     * @param version The tos version.
     * @param language The project language.
     * @return
     */
    public static List<ComponentExtension> getContributedExtensions(String version, ECodeLanguage language, String username,
            String passwordHash) {
        List<ComponentExtension> extensions = new ArrayList<ComponentExtension>();
        try {
            JSONArray extensionsJSONArray = ExchangeWebService.searchContributedExtensionJSONArray(username, passwordHash);
            if (extensionsJSONArray != null) {
                int size = extensionsJSONArray.length();
                for (int i = 0; i < size; i++) {
                    JSONObject extensionObj = extensionsJSONArray.getJSONObject(i);
                    Map<String, ComponentExtension> extensionsMap = new HashMap<String, ComponentExtension>();
                    ComponentExtension extension = extensionsMap.get(extensionObj.getString("name"));
                    if (extension == null) {
                        extension = ExchangeFactory.eINSTANCE.createComponentExtension();
                        //
                        extension.setIdExtension(extensionObj.getString("idExtension"));
                        extension.setTypeExtension(extensionObj.getString("typeExtension"));
                        extension.setLabel(extensionObj.getString("name"));
                        if (extensionObj.getString("dateExtension") != null
                                && !"".equals(extensionObj.getString("dateExtension"))) {
                            extension.setPublicationDate(formatter.parse(extensionObj.getString("dateExtension")));
                        } else {
                            extension.setPublicationDate(formatter.parse(formatter.format(new Date())));
                        }
                        extension.setLastVersionAvailable(extensionObj.getString("lastVersion"));
                        extensionsMap.put(extension.getIdExtension(), extension);
                        extensions.add(extension);
                    }
                }
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }

        return extensions;

    }

    /**
     * Find Installed components.
     *
     * @param version The tos version.
     * @param language The project language.
     * @return
     */
    public static List<ComponentExtension> getInstalledExtensions(String version, ECodeLanguage language, String username,
            String passwordHash) {
        List<ComponentExtension> extensions = new ArrayList<ComponentExtension>();
        try {
            JSONArray extensionsJSONArray = ExchangeWebService.searchInstalledExtensionJSONArray(username, passwordHash);
            if (extensionsJSONArray != null) {
                int size = extensionsJSONArray.length();
                for (int i = 0; i < size; i++) {
                    JSONObject extensionObj = extensionsJSONArray.getJSONObject(i);
                    Map<String, ComponentExtension> extensionsMap = new HashMap<String, ComponentExtension>();
                    ComponentExtension extension = extensionsMap.get(extensionObj.getString("name"));
                    if (extension == null) {
                        extension = ExchangeFactory.eINSTANCE.createComponentExtension();
                        //
                        extension.setIdExtension(extensionObj.getString("idExtension"));
                        extension.setTypeExtension(extensionObj.getString("typeExtension"));
                        extension.setLabel(extensionObj.getString("name"));
                        extension.setDateDownload(formatter.parse(extensionObj.getString("dateDownload")));
                        extension.setDownloadedVersion(extensionObj.getString("downloadedVersion"));
                        extension.setVersionExtension(extensionObj.getString("lastVersion"));
                        extensionsMap.put(extension.getIdExtension(), extension);
                        extensions.add(extension);
                    }
                }
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }

        return extensions;

    }

    /**
     * Search components according to user input.
     *
     * @param components All available components.
     * @param query The user input.
     * @param featureIds The field to search, e.g. name or description.
     * @return
     * @throws Exception
     */
    public static List<ComponentExtension> filterComponentExtensions(List<ComponentExtension> components, String query,
            int[] featureIds) throws Exception {
        return components;
    }

    /**
     * Find the components that have been installed.
     *
     * @param components
     * @return
     */
    public static List<ComponentExtension> getInstalledExtensions(List<ComponentExtension> components) {
        List<ComponentExtension> installed = new ArrayList<ComponentExtension>();
        for (ComponentExtension component : components) {
        }
        return installed;

    }

    /**
     * Convert the project language to id.
     *
     * @param language
     * @return
     */
    private static int getLanguageId(ECodeLanguage language) {
        switch (language) {
        case JAVA:
            return Language.JAVA_VALUE;
        case PERL:
            return Language.PERL_VALUE;
        }
        // unknow language
        return -1;
    }
}
