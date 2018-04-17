package com.talend.mdm.transaction.client;

import java.io.IOException;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.PostMethod;

@SuppressWarnings("nls")
public class MDMTransaction {

    private String url;

    private String id;

    private String username;

    private String password;

    private List<String> cookies;

    public void commit() throws IOException {
        HttpClient client = new HttpClient();
        client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

        HttpMethod method = new PostMethod(url + "/" + id);
        method.setDoAuthentication(true);
        try {
            for (String cookie : cookies) {
                method.addRequestHeader("Cookie", cookie);
            }
            client.executeMethod(method);
        } catch (HttpException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } finally {
            method.releaseConnection();
        }

        int statuscode = method.getStatusCode();
        if (statuscode >= 400) {
            throw new MDMTransactionException("Commit failed. The commit operation has returned the code " + statuscode + ".");
        }
    }

    public void rollback() throws IOException {
        HttpClient client = new HttpClient();
        client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

        HttpMethod method = new DeleteMethod(url + "/" + id);
        method.setDoAuthentication(true);
        try {
            for (String cookie : cookies) {
                method.addRequestHeader("Cookie", cookie);
            }
            client.executeMethod(method);
        } catch (HttpException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } finally {
            method.releaseConnection();
        }

        int statuscode = method.getStatusCode();
        if (statuscode >= 400) {
            throw new MDMTransactionException(
                    "Rollback failed. The rollback operation has returned the code " + statuscode + ".");
        }
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getCookies() {
        return cookies;
    }

    public void setCookies(List<String> cookies) {
        this.cookies = cookies;
    }

}