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
package org.talend.designer.core.ui.editor;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.StitchPseudoComponent;
import org.talend.utils.json.JSONArray;
import org.talend.utils.json.JSONException;
import org.talend.utils.json.JSONObject;

public class StitchDataLoaderConstants {

    public static final String CONNECTOR_PALETTE_TYPE = "SDL";

    public static final String CONNECTOR_FAMILY_NAME = "Stitch Data Loader";

    public static final String STITCH_DATA_CONNECTOR_JSON_URL = "https://www.stitchdata.com/integrations.json";

    private static final String DEFAULT_CONNECTOR_LIST_FILE_PATH = "stitch_connectors.json";

    private static String utmParamSuffix = StringUtils.EMPTY;

    private static List<StitchPseudoComponent> integrationSourceList = Collections.emptyList();

    private static List<StitchPseudoComponent> dataWarehouseList = Collections.emptyList();

    static {
        // load latest stitch pseudo connectors and UTM parameters asynchronously
        CompletableFuture.runAsync(new Runnable() {

            @Override
            public void run() {
                loadLatestStitchPseudoComponents();
            }
        });
    }

    public static String getUTMParamSuffix() {
        return utmParamSuffix;
    }

    protected static List<StitchPseudoComponent> getIntegrationSourceList() {
        return integrationSourceList;
    }

    protected static List<StitchPseudoComponent> getDataWarehouseList() {
        return dataWarehouseList;
    }

    private static void loadLatestStitchPseudoComponents() {
        try {
            URL stitchConnectorURL = new URL(STITCH_DATA_CONNECTOR_JSON_URL);
            HttpsURLConnection urlc = (HttpsURLConnection) stitchConnectorURL.openConnection();
            urlc.setConnectTimeout(2000);
            urlc.setReadTimeout(2000);
            InputStream ins = urlc.getInputStream();
            String resourceString = IOUtils.toString(ins, "UTF-8");
            ins.close();
            readFromJsonString(resourceString);
        } catch (IOException | JSONException e) {
            // fallback to default connector list
            loadDefaultStitchConnectorList();
        }
    }

    private static void loadDefaultStitchConnectorList() {
        try {
            InputStream ins = StitchDataLoaderConstants.class.getResourceAsStream(DEFAULT_CONNECTOR_LIST_FILE_PATH);
            String resourceString = IOUtils.toString(ins, "UTF-8");
            readFromJsonString(resourceString);
        } catch (IOException | JSONException e) {
            integrationSourceList = Collections.emptyList();
            dataWarehouseList = Collections.emptyList();
            utmParamSuffix = StringUtils.EMPTY;
            ExceptionHandler.process(e);
        }
    }

    private static String encodeValue(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, "UTF-8");
    }

    private static void readFromJsonString(String jsonString) throws JSONException, UnsupportedEncodingException {
        if (jsonString != null) {
            JSONObject jsonObj = new JSONObject(jsonString);

            JSONArray stitchSourcesArray = jsonObj.optJSONArray("stitch-sources");
            if (stitchSourcesArray != null) {
                List<StitchPseudoComponent> sourceComponentList = new ArrayList<>();
                for (int i = 0; i < stitchSourcesArray.length(); i++) {
                    JSONObject obj = stitchSourcesArray.getJSONObject(i);
                    sourceComponentList.add(createComponentFromJsonObject(obj));
                }
                integrationSourceList = sourceComponentList;
            }

            JSONArray stitchDestinationsArray = jsonObj.optJSONArray("stitch-destinations");
            if (stitchDestinationsArray != null) {
                List<StitchPseudoComponent> destinationComponentList = new ArrayList<>();
                for (int i = 0; i < stitchDestinationsArray.length(); i++) {
                    JSONObject obj = stitchDestinationsArray.getJSONObject(i);
                    destinationComponentList.add(createComponentFromJsonObject(obj));
                }
                dataWarehouseList = destinationComponentList;
            }

            JSONObject utmParamObject = jsonObj.optJSONObject("utm-params");
            if (utmParamObject != null) {
                StringBuilder utmBuilder = new StringBuilder();
                utmBuilder.append("?utm_medium=").append(encodeValue(utmParamObject.getString("utm_medium")));
                utmBuilder.append("&utm_source=").append(encodeValue(utmParamObject.getString("utm_source")));
                utmBuilder.append("&utm_campaign=").append(encodeValue(utmParamObject.getString("utm_campaign")));
                utmBuilder.append("&utm_content=").append(encodeValue(utmParamObject.getString("utm_content")));
                utmParamSuffix = utmBuilder.toString();
            }
        }
    }

    private static StitchPseudoComponent createComponentFromJsonObject(JSONObject obj) throws JSONException {
        final String connectorName = obj.getString("name");
        final String category = obj.getString("category");
        final String url = obj.getString("url");
        final String description = getDescriptionForSource(connectorName);
        return new StitchPseudoComponent(connectorName, category, url, description);
    }

    private static String getDescriptionForSource(String connectorName) {
        return Messages.getString("StitchDataLoaderConstants.descriptionForIntegrationSource", connectorName);
    }

    private static String getDescriptionForDestination(String connectorName) {
        return Messages.getString("StitchDataLoaderConstants.descriptionForDataWarehouseDestination", connectorName);
    }

}
