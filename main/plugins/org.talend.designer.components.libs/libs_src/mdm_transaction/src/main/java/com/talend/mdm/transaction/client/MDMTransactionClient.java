package com.talend.mdm.transaction.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;

@SuppressWarnings("nls")
public class MDMTransactionClient {

    public static MDMTransaction newTransaction(String url, String username, String password) throws IOException {
        HttpClient client = new HttpClient();
        client.getParams().setAuthenticationPreemptive(true);
        byte[] authBytes = (username + ":" + password).getBytes("UTF-8");
        String authString = Base64.getEncoder().encodeToString(authBytes);

        PutMethod put = new PutMethod(url);
        put.setDoAuthentication(true);
        put.setRequestHeader("Authorization", "Basic " + authString);
        String tid;
        List<String> cookies;
        try {
            client.executeMethod(put);
            tid = put.getResponseBodyAsString();
            cookies = parseCookies(put);
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
        result.setCookies(cookies);

        return result;
    }

    public static String getMDMTransactionURL(String url, boolean isNewServer) {
        if (url == null || "".equals(url)) {
            return "";
        }

        int count = 3;
        int i = 0;
        for (; i < url.length(); i++) {
            char c = url.charAt(i);
            if ('/' == c) {
                count--;
            }
            if (count == 0) {
                break;
            }
        }

        String result = url.substring(0, i);
        if (isNewServer) {
            result += "/talendmdm/services/rest/transactions";
        } else {
            result += "/datamanager/services/transactions";
        }

        return result;
    }

    public static List<String> getCookies(String url, String username, String password) throws IOException {
        HttpClient client = new HttpClient();
        client.getParams().setAuthenticationPreemptive(true);
        byte[] authBytes = (username + ":" + password).getBytes("UTF-8");
        String authString = Base64.getEncoder().encodeToString(authBytes);

        GetMethod get = new GetMethod(url);
        get.setDoAuthentication(true);
        get.setRequestHeader("Authorization", "Basic " + authString);
        List<String> cookies;
        try {
            client.executeMethod(get);
            cookies = parseCookies(get);
        } catch (HttpException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } finally {
            get.releaseConnection();
        }
        return cookies;
    }

    private static List<String> parseCookies(HttpMethod method) {
        List<String> cookies = new ArrayList<String>();
        Header[] setCookie = method.getResponseHeaders("Set-Cookie");
        for (Header header : setCookie) {
            cookies.add(header.getValue());
        }
        return cookies;
    }

}