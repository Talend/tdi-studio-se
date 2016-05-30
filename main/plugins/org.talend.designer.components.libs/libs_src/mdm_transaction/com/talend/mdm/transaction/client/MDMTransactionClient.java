package com.talend.mdm.transaction.client;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;

public class MDMTransactionClient {

    public static MDMTransaction newTransaction(String url, String username, String password) throws IOException {
        HttpClient client = new HttpClient();
        client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

        PutMethod put = new PutMethod(url);
        put.setDoAuthentication(true);
        String tid = "";
        String sessionID = "";
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

    public static String getSessionID(String url, String username, String password) throws IOException {
        HttpClient client = new HttpClient();
        client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
        GetMethod get = new GetMethod(url);
        get.setDoAuthentication(true);
        String sessionID = "";
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

    public static String parseSessionID(HttpMethod method) {
        String setCookie = method.getResponseHeader("Set-Cookie").getValue(); //$NON-NLS-1$
        int beginIndex = setCookie.indexOf("JSESSIONID=") + 11; //$NON-NLS-1$
        int endIndex = setCookie.indexOf(";", beginIndex); //$NON-NLS-1$
        return setCookie.substring(beginIndex, endIndex);
    }

    public static String getMDMTransactionURL(String url) {
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
        result += "/datamanager/services/transactions";

        return result;
    }

    public static void main(String[] args) throws IOException {
        String sessionID = MDMTransactionClient.getSessionID("http://localhost:8000/datamanager/services/transactions", "administrator", "administrator");
        System.out.println(sessionID);

        MDMTransaction mt = MDMTransactionClient.newTransaction("http://localhost:8000/datamanager/services/transactions", "administrator", "administrator");
        mt.commit();

        MDMTransaction mt1 = MDMTransactionClient.newTransaction("http://localhost:8000/datamanager/services/transactions", "administrator", "administrator");
        mt1.rollback();

        String url = "http://localhost:8000/talend/TalendPort";
        String mdmurl = MDMTransactionClient.getMDMTransactionURL(url);
        System.out.println(mdmurl);
    }

}
