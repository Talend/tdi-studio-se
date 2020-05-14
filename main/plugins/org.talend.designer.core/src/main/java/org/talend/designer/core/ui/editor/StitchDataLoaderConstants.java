// ============================================================================
//
// Copyright (C) 2006-2020 Talend Inc. - www.talend.com
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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.io.IOUtils;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.designer.core.model.components.StitchPseudoComponent;
import org.talend.utils.json.JSONArray;
import org.talend.utils.json.JSONException;
import org.talend.utils.json.JSONObject;

public class StitchDataLoaderConstants {

    public static final String CONNECTOR_PALETTE_TYPE = "SDL";

    public static final String CONNECTOR_FAMILY_NAME = "Stitch Data Loader";

    public static final String STITCH_DATA_CONNECTOR_JSON_URL = "https://www.stitchdata.com/integrations.json";

    public static final String UTM_PARAM_SUFFIX =
            "?utm_medium=tos&utm_source=talend&utm_campaign=toscomponent&utm_content=trystitch";

    private static final String DEFAULT_CONNECTOR_LIST_FILE_PATH = "stitch_connectors.json";

    public static List<StitchPseudoComponent> DATA_WAREHOUSE_LIST = Collections.emptyList();

    public static List<StitchPseudoComponent> INTEGRATION_SOURCE_LIST = Collections.emptyList();

    static {
        // load latest stitch pseudo connectors asynchronously
        CompletableFuture.runAsync(new Runnable() {

            @Override
            public void run() {
                loadStitchPseudoComponents();
            }
        });
    }

    private static void loadStitchPseudoComponents() {
        try {
            URL stitchConnectorURL = new URL(STITCH_DATA_CONNECTOR_JSON_URL);
            HttpsURLConnection con = (HttpsURLConnection) stitchConnectorURL.openConnection();
            InputStream ins = con.getInputStream();
            String resourceString = IOUtils.toString(ins, "UTF-8");
            ins.close();

            loadStitchPseudoComponentsFromJsonString(resourceString);
        } catch (IOException | JSONException e) {
            loadDefaultStitchConnectorList();
        }
    }

    private static void loadDefaultStitchConnectorList() {
        try {
            InputStream ins = StitchDataLoaderConstants.class.getResourceAsStream(DEFAULT_CONNECTOR_LIST_FILE_PATH);
            String resourceString = IOUtils.toString(ins, "UTF-8");
            loadStitchPseudoComponentsFromJsonString(resourceString);

        } catch (IOException | JSONException e) {
            ExceptionHandler.process(e);
        }
    }

    private static void loadStitchPseudoComponentsFromJsonString(String jsonString) throws JSONException {
        if (jsonString != null) {
            JSONObject jsonObj = new JSONObject(jsonString);

            JSONArray stitchSourcesArray = jsonObj.getJSONArray("stitch-sources");
            List<StitchPseudoComponent> sourceComponentList = new ArrayList<>();
            for (int i = 0; i < stitchSourcesArray.length(); i++) {
                JSONObject obj = stitchSourcesArray.getJSONObject(i);
                final String connectorName = obj.getString("name");
                final String url = obj.getString("url");
                final String description = getDescriptionForSource(connectorName);
                sourceComponentList.add(new StitchPseudoComponent(connectorName, url, description));
            }
            INTEGRATION_SOURCE_LIST = sourceComponentList;

            JSONArray stitchDestinationsArray = jsonObj.getJSONArray("stitch-destinations");
            List<StitchPseudoComponent> destinationComponentList = new ArrayList<>();
            for (int i = 0; i < stitchDestinationsArray.length(); i++) {
                JSONObject obj = stitchDestinationsArray.getJSONObject(i);
                final String connectorName = obj.getString("name");
                final String url = obj.getString("url");
                final String description = getDescriptionForDestination(connectorName);
                destinationComponentList.add(new StitchPseudoComponent(connectorName, url, description));
            }
            DATA_WAREHOUSE_LIST = destinationComponentList;
        }
    }

    private static String getDescriptionForSource(String connectorName) {
        final String description = String
                .format("Extract %s data and ingest it in the cloud destination of your choice using Stitch Data Loader.\n" //
                        + "Select this option to open your browser and try it for free.", connectorName);

        return description;
    }

    private static String getDescriptionForDestination(String connectorName) {
        final String description = String
                .format("Ingest data from over 100 popular sources to %s using Stitch Data Loader. \n" //
                        + "Select this option to open your browser and try it for free.", connectorName);

        return description;
    }
}
