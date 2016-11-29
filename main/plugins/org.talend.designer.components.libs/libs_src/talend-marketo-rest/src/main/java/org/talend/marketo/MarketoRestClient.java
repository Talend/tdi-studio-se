/**
 * Please modify this class to meet your needs
 * This class is not complete
 */

package org.talend.marketo;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.talend.marketo.response.AuthenticationResponse;
import org.talend.marketo.response.RequestResult;
import org.talend.marketo.response.ResultBasic;
import org.talend.marketo.response.ResultGetActivityTypes;
import org.talend.marketo.response.ResultGetLeadActivities;
import org.talend.marketo.response.ResultGetLeadChanges;
import org.talend.marketo.response.ResultGetStaticList;
import org.talend.marketo.response.ResultSync;
import org.talend.marketo.type.ActivityType;
import org.talend.marketo.type.AuthenticationInfo;
import org.talend.marketo.type.LeadActivityRecord;
import org.talend.marketo.type.LeadChangeRecord;
import org.talend.marketo.type.ListRecord;
import org.talend.marketo.type.MarketoError;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * This class was created by jzhao 2015-8-25 09:24:56
 * 
 */

public class MarketoRestClient {

    private String accessToken;
    private WebClient webClient;
    private AuthenticationInfo authInfo;
    private String basicPath = "/rest";
    private Map<Integer, String> aupportedActivitis;

    public MarketoRestClient(String endpoint, String secretKey,
            String clientAccessID) throws Exception {
        initClient(endpoint);
        this.authInfo = new AuthenticationInfo(secretKey, clientAccessID);
    }

    public void initClient(String endpoint) throws URISyntaxException {

        URI basicURI = new URI(endpoint);
        if (basicURI.getPath() != null) {
            basicPath = basicURI.getPath();
        }

        webClient = WebClient
                .create(new URI(basicURI.getScheme(), basicURI.getHost(), null,
                        null)).type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE);
    }

    public void setTimeOut(int timeout) {
        WebClient.getConfig(webClient).getHttpConduit().getClient()
                .setConnectionTimeout(timeout);
        WebClient.getConfig(webClient).getHttpConduit().getClient()
                .setReceiveTimeout(timeout);
    }

    private AuthenticationResponse getAccessToken(AuthenticationInfo authInfo2)
            throws Exception {
        if (authInfo != null) {
            webClient.resetQuery();
            webClient.replacePath("/identity/oauth/token");
            webClient.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE);

            Response response = webClient.post("grant_type=client_credentials&client_secret=" + authInfo.getSecretKey()
                    + "&client_id=" + authInfo.getClientAccessID());
            if (response.getStatus() == 200 && response.hasEntity()) {
                webClient.type(MediaType.APPLICATION_JSON_TYPE);
                InputStream inStream = response.readEntity(InputStream.class);
                Reader reader = new InputStreamReader(inStream);
                Gson gson = new Gson();
                return gson.fromJson(reader, AuthenticationResponse.class);
            } else {
                throw new Exception(
                        "Authentication failed! Please check your setting!");
            }
        } else {
            throw new Exception(
                    "Authentication information is empty, please check!");
        }
    }

    public void refreshToken() throws Exception {
        AuthenticationResponse response = getAccessToken(authInfo);
        if (response != null) {
            if (response.getError() == null) {
                this.accessToken = response.getAccess_token();
            } else {
                throw new Exception(response.getError() + " : "
                        + response.getErrorDescription());
            }
        }
    }

    public boolean isAvailable() {
        return accessToken != null;
    }

    public RequestResult executeGetRequest(Class<?> resultClass)
            throws Exception {

        Response response = webClient.get();
        if (response.getStatus() == 200 && response.hasEntity()) {
            InputStream inStream = response.readEntity(InputStream.class);
            Reader reader = new InputStreamReader(inStream);
            Gson gson = new Gson();
            return (RequestResult) gson.fromJson(reader, resultClass);
        } else {
            throw new Exception(
                    "Reauest failed! Please check your request ssetting!");
        }

    }

    public RequestResult executePostRequest(Class<?> resultClass,
            JsonObject inputJson) throws Exception {
    	
    	return doPost(resultClass,inputJson.toString());
    }
    
    public RequestResult executePostRequest(Class<?> resultClass,
            String postContent) throws Exception {
    	
    	webClient.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE);
    	return doPost(resultClass,postContent);
    }
    
    public RequestResult doPost(Class<?> resultClass,String postContent) throws Exception{
    	
    	 Response response = webClient.post(postContent);
         
         if (response.getStatus() == 200 && response.hasEntity()) {
         	
             InputStream inStream = response.readEntity(InputStream.class);
             Reader reader = new InputStreamReader(inStream);
             Gson gson = new Gson();
             return (RequestResult) gson.fromJson(reader, resultClass);
         } 
             throw new Exception( "Reauest failed! Please check your request ssetting!");
    	
    }

    public String getPageToken(String sinceDatetime) throws Exception {

        webClient.resetQuery();
        webClient.replacePath(basicPath + "/v1/activities/pagingtoken.json")
                .query("access_token", accessToken)
                .query("sinceDatetime", sinceDatetime);

        ResultBasic getResponse = (ResultBasic) executeGetRequest(ResultBasic.class);
        if (getResponse != null) {
            return getResponse.getNextPageToken();
        }
        return null;

    }

    public ResultBasic getLead(String[] fields, String filterType,
            String filterValue) throws Exception {
        String[] filterValues = new String[] { filterValue };
        return getMultipleLeads(fields, filterType, filterValues, null, null);
    }

    public ResultBasic getMultipleLeads(String[] fields, String filterType,
            String[] filterValues, Integer batchSize, String nextPageToken)
            throws Exception {

        webClient.resetQuery();
        webClient.replacePath(basicPath + "/v1/leads.json");
        webClient.query("_method", "GET");
        webClient.query("access_token", accessToken);
        
        String postContent ="filterType="+filterType;
        if (fields != null && fields.length > 0) {
        	postContent+="&fields="+MarketoUtils.unionString(",", fields);
        }
        
        if (filterValues != null && filterValues.length > 0) {
        	postContent+="&filterValues="+MarketoUtils.unionString(",", filterValues);
        }
        
        if (batchSize != null) {
            postContent+="&batchSize="+batchSize;
        }
        
        
        if (nextPageToken != null && nextPageToken.length() > 0) {
            postContent+="&nextPageToken="+nextPageToken;
        }
        ResultBasic rb = (ResultBasic)executePostRequest(ResultBasic.class,postContent);
        if(rb.isSuccess()){
        	return rb;
        }
        throw new Exception(rb.getErrors().toString());

        
    }

    public ResultBasic getLeadsByListId(String[] fields, Integer batchSize,
            String nextPageToken, Integer listId) throws Exception {

        webClient.resetQuery();
        webClient.replacePath(basicPath + "/v1/list/" + listId + "/leads.json")
                .query("access_token", accessToken);
        if (fields != null && fields.length > 0) {
            webClient.query("fields", MarketoUtils.unionString(",", fields));
        }
        if (batchSize != null) {
            webClient.query("batchSize", batchSize);
        }
        if (nextPageToken != null) {
            webClient.query("nextPageToken", nextPageToken);
        }
        ResultBasic rb = (ResultBasic) executeGetRequest(ResultBasic.class);
        if(rb.isSuccess()){
        	return rb;
        }
        throw new Exception(rb.getErrors().toString());
    }

    public Integer getListIdByName(String listName) throws Exception {

        webClient.resetQuery();
        webClient.replacePath(basicPath + "/v1/lists.json")
                .query("access_token", accessToken).query("name", listName);

        ResultGetStaticList getResponse = (ResultGetStaticList) executeGetRequest(ResultGetStaticList.class);

        if (getResponse != null && getResponse.isSuccess()) {
            if (getResponse.getResult().size() > 0) {
                for (ListRecord listObject : getResponse.getResult()) {
                    return listObject.getId();
                }
            }
        }else if(!getResponse.isSuccess()){
        	throw new Exception(getResponse.getErrors().toString());
        }
        return null;
    }

    public ResultGetLeadActivities getLeadActivities(
            List<Integer> activityTypeIds, String nextPageToken,
            Integer batchSize, Integer listId) throws Exception {

        webClient.resetQuery();
        webClient.replacePath(basicPath + "/v1/activities.json").query(
                "access_token", accessToken);
        if (nextPageToken != null && nextPageToken.length() > 0) {
            webClient.query("nextPageToken", nextPageToken);
        }
        if (activityTypeIds != null) {
            for (Integer activityTypeId : activityTypeIds) {
                if (activityTypeId != null) {
                    webClient.query("activityTypeIds", activityTypeId);
                }
            }
        }
        if (batchSize != null) {
            webClient.query("batchSize", batchSize);
        }
        if (listId != null) {
            webClient.query("listId", listId);
        }
        ResultGetLeadActivities rga = (ResultGetLeadActivities) executeGetRequest(ResultGetLeadActivities.class);
        if(rga.isSuccess()){
        	return rga;
        }
        throw new Exception(rga.getErrors().toString());
    }

    public Map<Integer, String> getServerActivityTypes() throws Exception {

        webClient.resetQuery();
        webClient.replacePath(basicPath + "/v1/activities/types.json").query(
                "access_token", accessToken);
        ResultGetActivityTypes getResponse = (ResultGetActivityTypes) executeGetRequest(ResultGetActivityTypes.class);
        Map<Integer, String> typeNames = new HashMap<Integer, String>();
        List<ActivityType> typeMaps = getResponse.getResult();
        for (ActivityType typeMap : typeMaps) {
            typeNames.put(typeMap.getId(), typeMap.getName());
        }
        return typeNames;
    }

    public ResultGetLeadChanges getLeadChanges(String nextPageToken,
            Integer batchSize, Integer listId, String[] fields)
            throws Exception {

        webClient.resetQuery();
        webClient.replacePath(basicPath + "/v1/activities/leadchanges.json")
                .query("access_token", accessToken);
        if (nextPageToken != null && nextPageToken.length() > 0) {
            webClient.query("nextPageToken", nextPageToken);
        }

        if (batchSize != null) {
            webClient.query("batchSize", batchSize);
        }
        if (listId != null) {
            webClient.query("listId", listId);
        }
        if (fields != null && fields.length > 0) {
            webClient.query("fields", MarketoUtils.unionString(",", fields));
        }
        ResultGetLeadChanges rglc = (ResultGetLeadChanges) executeGetRequest(ResultGetLeadChanges.class);
        if(rglc.isSuccess()){
        	return rglc;
        }
        throw new Exception(rglc.getErrors().toString());

    }

    public ResultSync syncLead(String action, String lookupField,
            Map<String, Object> leadObject, boolean asyncProcessing,
            String partitionName) throws Exception {

        List<Map<String, Object>> leadsObjects = new ArrayList<Map<String, Object>>();
        leadsObjects.add(leadObject);
        return syncMultipleLeads(action, lookupField, leadsObjects,
                asyncProcessing, partitionName);

    }

    public ResultSync syncMultipleLeads(String action, String lookupField,
            List<Map<String, Object>> leadsObjects, boolean asyncProcessing,
            String partitionName) throws Exception {

        webClient.resetQuery();
        webClient.replacePath(basicPath + "/v1/leads.json").query(
                "access_token", accessToken);

        JsonObject inputJson = new JsonObject();
        Gson gson = new Gson();
        if (action != null) {
            inputJson.addProperty("action", action);
        }
        if (lookupField != null) {
            inputJson.addProperty("lookupField", lookupField);
        }
        inputJson.addProperty("asyncProcessing", asyncProcessing);
        if (partitionName != null) {
            inputJson.addProperty("partitionName", partitionName);
        }
        inputJson.add("input", gson.toJsonTree(leadsObjects));
        ResultSync rs = (ResultSync) executePostRequest(ResultSync.class,inputJson);
        if(rs.isSuccess()){
        	return rs;
        }
        throw new Exception(rs.getErrors().toString());

    }

    public ResultSync listOperation(String operation, int listId,
            List<Integer> leadIds) throws Exception {

        webClient.resetQuery();
        if (!"ISMEMBEROFLIST".equals(operation)) {
            webClient.replacePath(basicPath + "/v1/lists/" + listId
                    + "/leads.json");
        } else {
            webClient.replacePath(basicPath + "/v1/lists/" + listId
                    + "/leads/ismember.json");
        }
        webClient.query("access_token", accessToken);
        if ("ISMEMBEROFLIST".equals(operation)) {
            webClient.query("_method", "GET");
        } else if ("REMOVEFROMLIST".equals(operation)) {
            webClient.query("_method", "DELETE");
        }

        // Map<String, JsonArray> inputData = new HashMap<String, JsonArray>();
        JsonArray json = new JsonArray();
        for (Integer leadId : leadIds) {
            JsonObject leadKey = new JsonObject();
            leadKey.addProperty("id", leadId);
            json.add(leadKey);
        }
        // inputData.put("input", json);
        JsonObject jsonObj = new JsonObject();
        jsonObj.add("input", json);
        
        ResultSync rs = (ResultSync) executePostRequest(ResultSync.class, jsonObj);
        if(rs.isSuccess()){
        	return rs;
        }
        throw new Exception(rs.getErrors().toString());
        
    }

    public Map<String, String> readActivity(LeadActivityRecord record) {
        Map<String, String> activity = new HashMap<String, String>();
        activity.put("id", String.valueOf(record.getId()));
        activity.put("leadId", Integer.toString(record.getLeadId()));
        activity.put("activityDate",
                MarketoUtils.formatAsW3C(record.getActivityDate()));
        activity.put("activityTypeId",
                Integer.toString(record.getActivityTypeId()));
        activity.put("activityTypeValue",
                getActivityTypeNameById(record.getActivityTypeId()));
        activity.put("primaryAttributeValueId",
                record.getPrimaryAttributeValueId());
        activity.put("primaryAttributeValue", record.getPrimaryAttributeValue());

        if (record.getAttributes() != null) {
            for (Map<String, String> attr : record.getAttributes()) {
                activity.put(attr.get("name"), attr.get("value"));
            }
        }
        return activity;
    }

    public Map<String, String> readChange(LeadChangeRecord record) {
        Map<String, String> change = new HashMap<String, String>();
        change.put("id", String.valueOf(record.getId()));
        change.put("leadId", Long.toString(record.getLeadId()));
        change.put("activityDate",
                MarketoUtils.formatAsW3C(record.getActivityDate()));
        change.put("activityTypeId",
                Integer.toString(record.getActivityTypeId()));
        change.put("activityTypeValue",
                getActivityTypeNameById(record.getActivityTypeId()));
        Gson gson = new Gson();
        change.put("fields", gson.toJson(record.getFields()));
        if (record.getAttributes() != null) {
            for (Map<String, String> attr : record.getAttributes()) {
                change.put(attr.get("name"), attr.get("value"));
            }
        }
        return change;
    }

    public boolean isAccessTokenExpired(List<MarketoError> errors) {
        if (errors != null) {
            for (MarketoError error : errors) {
                if ("602".equals(error.getCode())) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getActivityTypeNameById(int activityId) {
        if (aupportedActivitis == null) {
            aupportedActivitis = getLocalActivityTypes();
        }
        return aupportedActivitis.get(activityId);
    }

    public Map<Integer, String> getLocalActivityTypes() {
        return new HashMap<Integer, String>() {
            {
                put(1, "Visit Webpage");
                put(2, "Fill Out Form");
                put(3, "Click Link");
                put(6, "Send Email");
                put(7, "Email Delivered");
                put(8, "Email Bounced");
                put(9, "Unsubscribe Email");
                put(10, "Open Email");
                put(11, "Click Email");
                put(12, "New Lead");
                put(13, "Change Data Value");
                put(19, "Sync Lead to SFDC");
                put(21, "Convert Lead");
                put(22, "Change Score");
                put(23, "Change Owner");
                put(24, "Add to List");
                put(25, "Remove from List");
                put(26, "SFDC Activity");
                put(27, "Email Bounced Soft");
                put(29, "Delete Lead from SFDC");
                put(30, "SFDC Activity Updated");
                put(32, "Merge Leads");
                put(34, "Add to Opportunity");
                put(35, "Remove from Opportunity");
                put(36, "Update Opportunity");
                put(37, "Delete Lead");
                put(38, "Send Alert");
                put(39, "Send Sales Email");
                put(40, "Open Sales Email");
                put(41, "Click Sales Email");
                put(42, "Add to SFDC Campaign");
                put(43, "Remove from SFDC Campaign");
                put(44, "Change Status in SFDC Campaign");
                put(45, "Receive Sales Email");
                put(46, "Interesting Moment");
                put(47, "Request Campaign");
                put(48, "Sales Email Bounced");
                put(100, "Change Lead Partition");
                put(101, "Change Revenue Stage");
                put(102, "Change Revenue Stage Manually");
                put(104, "Change Status in Progression");
                put(106, "Enrich with Data.com");
                put(108, "Change Segment");
                put(110, "Call Webhook");
                put(111, "Sent Forward to Friend Email");
                put(112, "Received Forward to Friend Email");
                put(113, "Add to Nurture");
                put(114, "Change Nurture Track");
                put(115, "Change Nurture Cadence");
                put(400, "Share Content");
                put(401, "Vote in Poll");
                put(405, "Click Shared Link");
            }
        };
    }
}
