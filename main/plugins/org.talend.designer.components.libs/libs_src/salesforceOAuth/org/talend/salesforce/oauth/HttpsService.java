package org.talend.salesforce.oauth;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;

// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

/**
 * created by bchen on Aug 26, 2013 Detailled comment
 * 
 */
public class HttpsService {

    Server server;

    String code;

    /**
     * DOC bchen HttpService constructor comment.
     * 
     * @throws Exception
     */
    public HttpsService(int port) throws Exception {
        server = new Server();

        SslSelectChannelConnector ssl_connector = new SslSelectChannelConnector();
        ssl_connector.setPort(port);
        SslContextFactory cf = ssl_connector.getSslContextFactory();
        cf.setKeyStorePath(HttpsService.class.getResource("tsalesforce").toString());
        cf.setKeyStorePassword("talend");
        cf.setKeyManagerPassword("talend");
        server.addConnector(ssl_connector);

        server.setHandler(new CallBackHandler());

        server.start();
    }

    public void stop() throws Exception {
        server.stop();
        server.join();
    }

    public String getCode() {
        return code;
    }

    class CallBackHandler extends AbstractHandler {

        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
                throws IOException, ServletException {
            response.setContentType("text/html;charset=utf-8");
            response.setStatus(HttpServletResponse.SC_OK);
            if (code == null) {
                code = request.getParameter("code");
                if (code != null) {
                    response.getWriter().println("<p>Successful to get authorization code:" + code + "</p>");
                }
            }
            response.flushBuffer();
        }
    }
}
