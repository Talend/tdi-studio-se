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
package org.talend.designer.runprocess.shadow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.runprocess.shadow.ObjectElementParameter;
import org.talend.core.model.runprocess.shadow.TextElementParameter;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.repository.model.preview.WSDLSchemaBean;

/**
 * DOC qwei class global comment. Detailled comment
 */
public class WSDLSchemaInputNode extends FileInputNode {

    /**
     * DOC qwei WSDLSchemaInputNode constructor comment.
     *
     * @param nodeType
     */
    private List<IMetadataTable> metadatas = null;

    private String encoding = ""; //$NON-NLS-1$

    /**
     * Constructs a new WSDLSchemaInputNode.
     *
     * @param schemaBean
     * @param string
     */
    public WSDLSchemaInputNode(String fileName, String encoding, List<IMetadataTable> metadatas, WSDLSchemaBean schemaBean,
            ArrayList parameters) {
        super("tWebServiceInput"); //$NON-NLS-1$
        this.encoding = encoding;
        addParameters(schemaBean, parameters);
        setMetadataList(metadatas);
    }

    /**
     * DOC qwei Comment method "addParameters".
     *
     * @param schemaBean
     * @param parameters
     */
    private void addParameters(WSDLSchemaBean schemaBean, ArrayList parameters) {
        IElementParameter param = new TextElementParameter("ENDPOINT", schemaBean.getWslUrl()); //$NON-NLS-1$
        addParameter(param);
        if (LanguageManager.getCurrentLanguage().equals(ECodeLanguage.JAVA)) {
            addJavaParameters(schemaBean);
        } else if (LanguageManager.getCurrentLanguage().equals(ECodeLanguage.PERL)) {
            addPerlParameters(schemaBean);
        }
        param = new TextElementParameter("METHOD", schemaBean.getMethod()); //$NON-NLS-1$
        addParameter(param);

        // fix preview
        param = new TextElementParameter("ADVANCED_USE", Boolean.toString(false)); //$NON-NLS-1$
        addParameter(param);

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        if (parameters != null) {
            for (Object string : parameters) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("VALUE", TalendTextUtils.addQuotes(string.toString())); //$NON-NLS-1$
                list.add(map);
            }
        }
        param = new ObjectElementParameter("PARAMS", list); //$NON-NLS-1$
        param.setListItemsDisplayCodeName(new String[] { "VALUE" }); //$NON-NLS-1$
        addParameter(param);

    }

    private void addJavaParameters(WSDLSchemaBean schemaBean) {
        IElementParameter param = new TextElementParameter("NEED_AUTH", "" + schemaBean.isNeedAuth()); //$NON-NLS-1$ //$NON-NLS-2$
        addParameter(param);

        param = new TextElementParameter("AUTH_USERNAME", schemaBean.getUserName()); //$NON-NLS-1$
        addParameter(param);

        param = new TextElementParameter("AUTH_PASSWORD", schemaBean.getPassword()); //$NON-NLS-1$
        addParameter(param);

        param = new TextElementParameter("UES_PROXY", "" + schemaBean.isUseProxy()); //$NON-NLS-1$ //$NON-NLS-2$
        addParameter(param);

        param = new TextElementParameter("PROXY_HOST", schemaBean.getProxyHost()); //$NON-NLS-1$
        addParameter(param);

        param = new TextElementParameter("PROXY_PORT", schemaBean.getProxyPort()); //$NON-NLS-1$
        addParameter(param);

        param = new TextElementParameter("PROXY_USERNAME", schemaBean.getProxyUser()); //$NON-NLS-1$
        addParameter(param);

        param = new TextElementParameter("PROXY_PASSWORD", schemaBean.getProxyPassword()); //$NON-NLS-1$
        addParameter(param);

        param = new TextElementParameter("TIMEOUT", "" + schemaBean.getTimeOut()); //$NON-NLS-1$
        addParameter(param);
        if (!schemaBean.getIsInputModel()) {
            param = new TextElementParameter("PORT_NAME", schemaBean.getPortName()); //$NON-NLS-1$
            addParameter(param);

            param = new TextElementParameter("PORT_NS", schemaBean.getPortNS()); //$NON-NLS-1$
            addParameter(param);

            param = new TextElementParameter("SERVICE_NAME", schemaBean.getServerName()); //$NON-NLS-1$
            addParameter(param);

            param = new TextElementParameter("SERVICE_NS", "" + schemaBean.getServerNS()); //$NON-NLS-1$
            addParameter(param);
        }

    }

    private void addPerlParameters(WSDLSchemaBean schemaBean) {
        IElementParameter param = new TextElementParameter("WSDL", schemaBean.getEndpointURI()); //$NON-NLS-1$
        addParameter(param);
        param = new TextElementParameter("ENCODING", this.encoding); //$NON-NLS-1$
        addParameter(param);
    }

    /**
     * Getter for metadatas.
     *
     * @return the metadatas
     */
    public List<IMetadataTable> getMetadatas() {
        return this.metadatas;
    }

    /**
     * Sets the metadatas.
     *
     * @param metadatas the metadatas to set
     */
    public void setMetadatas(List<IMetadataTable> metadatas) {
        this.metadatas = metadatas;
    }

}
