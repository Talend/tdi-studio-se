package com.talend.mdm.transaction.client;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.PostMethod;

public class MDMTransaction {

    public static final String JVM_STICKY_SESSION = "sticky_session"; //$NON-NLS-1$
    public static final String DEFAULT_STICKY_SESSION = "JSESSIONID"; //$NON-NLS-1$

	private String url;
	private String id;
	private String username;
	private String password;
	private String sessionId;
	
	public void commit() throws IOException {
		HttpClient client = new HttpClient();
		client.getState().setCredentials(AuthScope.ANY,
				new UsernamePasswordCredentials(username, password));

		HttpMethod method = new PostMethod(url + "/" + id);
		method.setDoAuthentication(true);
		try {
		    method.setRequestHeader("Cookie", getStickySession() + "=" + sessionId); //$NON-NLS-1$ //$NON-NLS-2$
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
		client.getState().setCredentials(AuthScope.ANY,
				new UsernamePasswordCredentials(username, password));

		HttpMethod method = new DeleteMethod(url + "/" + id);
		method.setDoAuthentication(true);
		try {
		    method.setRequestHeader("Cookie", getStickySession() + "=" + sessionId); //$NON-NLS-1$ //$NON-NLS-2$
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
		    throw new MDMTransactionException("Rollback failed. The rollback operation has returned the code " + statuscode + ".");
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

	public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

	public static String getStickySession() {
        String stickySession = System.getProperty(JVM_STICKY_SESSION);
        if(stickySession == null) {
            stickySession = DEFAULT_STICKY_SESSION;
        }
        return stickySession;
    }
}