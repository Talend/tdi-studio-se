package com.talend.mdm.transaction.client;

import java.io.IOException;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MDMTransactionClient {

    private static final Log LOG = LogFactory.getLog(MDMTransactionClient.class);

    public static MDMTransaction newTransaction(String url, String username, String password) throws IOException {
        HttpClient client = new HttpClient();
        client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
        client.getParams().setAuthenticationPreemptive(true);

        PutMethod put = new PutMethod(url);
        put.setDoAuthentication(true);
        String tid;
        String sessionID;
        try {
            client.executeMethod(put);
            tid = put.getResponseBodyAsString();
            sessionID = parseSessionID(put);
        } catch (HttpException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } finally {
            put.releaseConnection();
        }

        MDMTransaction result = new MDMTransaction();
        result.setUrl(url);
        result.setId(tid);
        result.setUsername(username);
        result.setPassword(password);
        result.setSessionId(sessionID);

        return result;
    }

    public static String getMDMTransactionURL(String url, boolean isNewServer) {
        if(url == null || "".equals(url)) {
            return "";
        }

        int count = 3;
        int i=0;
        for(;i<url.length();i++) {
            char c = url.charAt(i);
            if('/' == c) {
                count--;
            }
            if(count == 0) {
                break;
            }
        }

        String result = url.substring(0, i);
        if(isNewServer){
            result += "/talendmdm/services/rest/transactions";
        } else {
            result += "/datamanager/services/transactions";
        }

        return result;
    }

    public static String getSessionID(String url, String username, String password) throws IOException {
        HttpClient client = new HttpClient();
        client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
        client.getParams().setAuthenticationPreemptive(true);

        GetMethod get = new GetMethod(url);
        get.setDoAuthentication(true);
        String sessionID;
        try {
            client.executeMethod(get);
            sessionID = parseSessionID(get);
        } catch (HttpException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } finally {
            get.releaseConnection();
        }
        return sessionID;
    }

    private static String parseSessionID(HttpMethod method) {
        String sessionID = null;
        String stickySession = MDMTransaction.getStickySession();
        Header[] setCookie = method.getResponseHeaders("Set-Cookie"); //$NON-NLS-1$
        for(Header header : setCookie) {
            String headerValue = header.getValue();
            if(headerValue.startsWith(stickySession + "=")) { //$NON-NLS-1$
                int beginIndex = (stickySession + "=").length(); //$NON-NLS-1$
                int endIndex = headerValue.indexOf(";", beginIndex); //$NON-NLS-1$
                sessionID = headerValue.substring(beginIndex, endIndex);
                break;
            }
        }
        if(sessionID == null) {
            if(LOG.isDebugEnabled()) {
                LOG.warn("Cookie for sticky session not found!"); //$NON-NLS-1$
            }
            sessionID = ""; //$NON-NLS-1$
        }
        return sessionID;
    }

    public static void main(String[] args) throws IOException {
        String sessionID = MDMTransactionClient.getSessionID("http://localhost:8621/talendmdm/services/rest/transactions", "administrator", "administrator");
        System.out.println(sessionID);

        MDMTransaction mt = MDMTransactionClient.newTransaction("http://localhost:8180/talendmdm/services/rest/transactions", "administrator", "administrator");
        mt.commit();

        MDMTransaction mt1 = MDMTransactionClient.newTransaction("http://localhost:8180/talendmdm/services/rest/transactions", "administrator", "administrator");
        mt1.rollback();

        String url = "http://localhost:8180/talend/TalendPort";
        String mdmurl = MDMTransactionClient.getMDMTransactionURL(url, true);
        System.out.println(mdmurl);
    }

}