package org.talend.designer.oas.external;

import java.util.LinkedList;
import java.util.List;

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

/**
 * DOC dsergent class global comment. Detailled comment
 */
public class RestAPIMapping {

    private String id;

    private String outputFlow = "";

    private String httpVerb = "";

    private String uriPattern = "";

    private EESBMediaType consumes;

    private EESBMediaType produces;

    private List<APIParam> params = new LinkedList<APIParam>();

    /**
     * DOC dsergent RestAPIMapping constructor comment.
     */
    public RestAPIMapping(String id) {
        super();
        this.id = id;
    }

    /**
     * Getter for id.
     *
     * @return the id
     */
    public String getId() {
        return this.id;
    }

    /**
     * Getter for outputFlow.
     *
     * @return the outputFlow
     */
    public String getOutputFlow() {
        return this.outputFlow;
    }

    /**
     * Sets the outputFlow.
     *
     * @param outputFlow the outputFlow to set
     */
    public void setOutputFlow(String outputFlow) {
        this.outputFlow = outputFlow;
    }

    /**
     * Getter for httpVerb.
     *
     * @return the httpVerb
     */
    public String getHttpVerb() {
        return this.httpVerb;
    }

    /**
     * Sets the httpVerb.
     *
     * @param httpVerb the httpVerb to set
     */
    public void setHttpVerb(String httpVerb) {
        this.httpVerb = httpVerb;
    }

    /**
     * Getter for uriPattern.
     *
     * @return the uriPattern
     */
    public String getUriPattern() {
        return this.uriPattern;
    }

    /**
     * Sets the uriPattern.
     *
     * @param uriPattern the uriPattern to set
     */
    public void setUriPattern(String uriPattern) {
        this.uriPattern = uriPattern;
    }

    /**
     * Getter for consumes.
     *
     * @return the consumes
     */
    public EESBMediaType getConsumes() {
        return this.consumes;
    }

    /**
     * Sets the consumes.
     *
     * @param consumes the consumes to set
     */
    public void setConsumes(EESBMediaType consumes) {
        this.consumes = consumes;
    }

    /**
     * Getter for produces.
     *
     * @return the produces
     */
    public EESBMediaType getProduces() {
        return this.produces;
    }

    /**
     * Sets the produces.
     *
     * @param produces the produces to set
     */
    public void setProduces(EESBMediaType produces) {
        this.produces = produces;
    }

    public List<APIParam> getParams() {
        return params;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RestAPIMapping other = (RestAPIMapping) obj;
        if (this.id == null) {
            if (other.id != null)
                return false;
        } else if (!this.id.equals(other.id))
            return false;
        return true;
    }

}
