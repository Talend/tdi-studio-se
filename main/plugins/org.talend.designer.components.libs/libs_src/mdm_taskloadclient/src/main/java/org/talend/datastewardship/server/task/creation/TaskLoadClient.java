// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.datastewardship.server.task.creation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Base64;

/**
 * DOC Starkey class global comment. Detailled comment
 */
public class TaskLoadClient {

	private String urlString;

	private String username;

	private String password;

	public TaskLoadClient(String urlString, String username, String password) {
		super();
		this.urlString = urlString;
		this.username = username;
		this.password = password;
	}

	/**
	 * DOC Starkey Comment method "doLoad".
	 *
	 * @param inputXmlFile
	 */
	public boolean doLoad(Reader inputXmlFile) throws Exception {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(inputXmlFile);
			StringBuffer sb = new StringBuffer();
			String line = reader.readLine();
			while (reader.readLine() != null) {
				sb = sb.append(line);
			}
			return doLoad(sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {

				}
		}
	}

	/**
	 * DOC Starkey Comment method "doLoad".
	 *
	 * @param inputTasks
	 * @return
	 * @throws Exception
	 */
	public boolean doLoad(String inputTasks) throws Exception {

		int responseCode = 0;
		URL url = new URL(urlString);
		HttpURLConnection urlc = (HttpURLConnection) url.openConnection();

		try {

			urlc.setDoOutput(true);
			urlc.setDoInput(true);
			urlc.setRequestMethod("POST");
			urlc.setConnectTimeout(50000);// set timeout
			urlc.setReadTimeout(50000);

			StringBuilder localStringBuilder = new StringBuilder();
			localStringBuilder.append(this.username).append(':').append(this.password);
			String str;
			try {
			  byte[] arrayOfByte1 = localStringBuilder.toString().getBytes("UTF-8");
			  byte[] arrayOfByte2 = Base64.getEncoder().encode(arrayOfByte1);
			  str = new String(arrayOfByte2, "UTF-8");
			}
			catch (UnsupportedEncodingException localUnsupportedEncodingException) {
			  str = null;
			}
			localStringBuilder.setLength(0);
			localStringBuilder.append("Basic ").append(str);

			urlc.setRequestProperty ("Authorization", localStringBuilder.toString());

			urlc.connect();

			inputTasks = URLEncoder.encode(inputTasks, "UTF-8");
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					urlc.getOutputStream(), "UTF-8"));
			out.write("j_username=" + username + "&j_password=" + password
					+ "&INPUT_TASKS=" + inputTasks);
			out.flush();
			out.close();

			// read response
			responseCode = urlc.getResponseCode();

			if (responseCode == urlc.HTTP_FORBIDDEN) {
				throw new TaskLoadException("Unauthorized exception! ");
			}

			if (responseCode == urlc.HTTP_INTERNAL_ERROR) {
				throw new TaskLoadException(urlc.getResponseMessage());
			}

			if (responseCode == urlc.HTTP_OK) {
				return true;
			} else {
				return false;
			}

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			urlc.disconnect();// disconnect
		}

	}

	public void validate() throws Exception {
		getHttpURLConnection(new URL(this.urlString));
	}

	private HttpURLConnection getHttpURLConnection(URL url) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setRequestMethod("POST");
		connection.setConnectTimeout(50000);
		connection.setReadTimeout(50000);
		connection.connect();
		return connection;
	}

}
