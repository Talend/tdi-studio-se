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
package org.talend.repository.json.service;

import org.talend.core.AbstractRepositoryContextUpdateService;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.repository.json.util.JSONConnectionContextHelper;
import org.talend.repository.model.json.JSONFileConnection;

public class JsonFileConnectionContextUpdateService extends AbstractRepositoryContextUpdateService {

    @Override
    public boolean updateContextParameter(Connection conn, String oldValue, String newValue) {
        boolean isModified = false;
        if (conn.isContextMode()) {
            if (conn instanceof JSONFileConnection) {
                JSONFileConnection jsonConn = (JSONFileConnection) conn;
                boolean isInputModel = jsonConn.isInputModel();
                if (isInputModel) {
                    if (jsonConn.getJSONFilePath() != null && jsonConn.getJSONFilePath().equals(oldValue)) {
                        jsonConn.setJSONFilePath(newValue);
                        isModified = true;
                    } else if (jsonConn.getEncoding() != null && jsonConn.getEncoding().equals(oldValue)) {
                        jsonConn.setEncoding(newValue);
                        isModified = true;
                    } else {
                        String xpathQuery = JSONConnectionContextHelper.getConnectionXPathQuery(jsonConn);
                        if (xpathQuery != null && xpathQuery.equals(oldValue)) {
                            JSONConnectionContextHelper.setConnectionXPathQuery(jsonConn, newValue);
                            isModified = true;
                        }
                    }
                } else {
                    if (jsonConn.getOutputFilePath() != null && jsonConn.getOutputFilePath().equals(oldValue)) {
                        jsonConn.setOutputFilePath(newValue);
                        isModified = true;
                    }
                }
            }
        }
        return isModified;
    }

    @Override
    public boolean accept(Connection connection) {
        if (connection instanceof JSONFileConnection) {
            return true;
        }
        return false;
    }

}
