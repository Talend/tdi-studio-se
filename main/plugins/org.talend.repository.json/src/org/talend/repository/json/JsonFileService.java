// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.json;

import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.service.IJsonFileService;
import org.talend.metadata.managment.ui.wizard.documentation.LinkUtils;
import org.talend.repository.model.json.JSONFileConnection;

/**
 * created by wchen on 2013-6-5 Detailled comment
 *
 */
public class JsonFileService implements IJsonFileService {

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.core.service.IJsonFileService#getFilePathFromRepository(org.talend.core.model.metadata.builder.connection
     * .Connection, java.lang.String, org.talend.core.model.process.IElement)
     */
    @Override
    public boolean changeFilePathFromRepository(Object jsonConneciton, IElementParameter filePathParm, IElement elem, Object value) {
        boolean isParamChanged = false;
        if (jsonConneciton instanceof JSONFileConnection && "FILE_PATH".equals(filePathParm.getRepositoryValue())) {
            if (LinkUtils.isRemoteFile(TalendTextUtils.removeQuotes(String.valueOf(value)))) {
                elem.setPropertyValue("USEURL", true);
                IElementParameter elementParameter = elem.getElementParameter("URLPATH");
                if (elementParameter != null) {
                    elementParameter.setRepositoryValueUsed(true);
                }
                isParamChanged = true;
            } else {
                IElementParameter elementParameter = elem.getElementParameter("FILENAME");
                if (elementParameter != null) {
                    elementParameter.setRepositoryValueUsed(true);
                }
                elem.setPropertyValue("USEURL", false);
            }
            elem.setPropertyValue("URLPATH", value);
            // store the filepath in this param also
            elem.setPropertyValue(filePathParm.getName(), value);
        }
        return isParamChanged;
    }

}
