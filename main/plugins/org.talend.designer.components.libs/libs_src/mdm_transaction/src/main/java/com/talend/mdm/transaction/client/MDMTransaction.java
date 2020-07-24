package com.talend.mdm.transaction.client;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
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
        client.getParams().setAuthenticationPreemptive(true);
        byte[] authBytes = (username + ":" + password).getBytes("UTF-8");
        String authString = Base64.getEncoder().encodeToString(authBytes);

        HttpMethod post = new PostMethod(url + "/" + id);
        post.setDoAuthentication(true);
        post.setRequestHeader("Authorization", "Basic " + authString);
        try {
            for (String cookie : cookies) {
                post.addRequestHeader("Cookie", cookie);
            }
            client.executeMethod(post);
        } catch (HttpException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } finally {
            post.releaseConnection();
        }

        int statuscode = post.getStatusCode();
        if (statuscode >= 400) {
            throw new MDMTransactionException("Commit failed. The commit operation has returned the code " + statuscode + ".");
        }
    }

    public void rollback() throws IOException {
        HttpClient client = new HttpClient();
        client.getParams().setAuthenticationPreemptive(true);
        byte[] authBytes = (username + ":" + password).getBytes("UTF-8");
        String authString = Base64.getEncoder().encodeToString(authBytes);

        HttpMethod delete = new DeleteMethod(url + "/" + id);
        delete.setDoAuthentication(true);
        delete.setRequestHeader("Authorization", "Basic " + authString);
        try {
            for (String cookie : cookies) {
                delete.addRequestHeader("Cookie", cookie);
            }
            client.executeMethod(delete);
        } catch (HttpException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } finally {
            delete.releaseConnection();
        }

        int statuscode = delete.getStatusCode();
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