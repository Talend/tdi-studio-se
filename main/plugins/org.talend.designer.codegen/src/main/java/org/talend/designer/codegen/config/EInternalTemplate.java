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
package org.talend.designer.codegen.config;

/**
 * Internal Templates for Code Generator, must be always available to encapsulate rela parts of code.
 *
 * $Id$
 *
 */
public enum EInternalTemplate {
    HEADER("header", "0.0.1"), //$NON-NLS-1$ //$NON-NLS-2$
    FOOTER("footer", "0.0.1"), //$NON-NLS-1$ //$NON-NLS-2$
    CONTEXT("context", "0.0.1"), //$NON-NLS-1$ //$NON-NLS-2$
    SUBPROCESS_HEADER("subprocess_header", "0.0.1"), //$NON-NLS-1$ //$NON-NLS-2$
    SUBPROCESS_FOOTER("subprocess_footer", "0.0.1"), //$NON-NLS-1$ //$NON-NLS-2$
    SUBPROCESS_HEADER_ROUTE("subprocess_header_route", "0.0.1"), //$NON-NLS-1$ //$NON-NLS-2$
    SUBPROCESS_FOOTER_ROUTE("subprocess_footer_route", "0.0.1"), //$NON-NLS-1$ //$NON-NLS-2$
    SUBTREE_BEGIN("subtree_begin", "0.0.1"), //$NON-NLS-1$ //$NON-NLS-2$
    SUBTREE_END("subtree_end", "0.0.1"), //$NON-NLS-1$ //$NON-NLS-2$
    CLOSE_BLOCKS_CODE("close_blocks_code", "0.0.1"), //$NON-NLS-1$ //$NON-NLS-2$
    PART_HEADER("component_part_header", "0.0.1"), //$NON-NLS-1$ //$NON-NLS-2$
    PART_FOOTER("component_part_footer", "0.0.1"), //$NON-NLS-1$ //$NON-NLS-2$
    PART_ENDMAIN("component_part_endmain", "0.0.1"), //$NON-NLS-1$ //$NON-NLS-2$
    PROCESSINFO("footer_process_info", "0.0.1"), //$NON-NLS-1$ //$NON-NLS-2$
    ITERATE_SUBPROCESS_HEADER("iterate_subprocess_header", "0.0.1"), //$NON-NLS-1$ //$NON-NLS-2$
    ITERATE_SUBPROCESS_FOOTER("iterate_subprocess_footer", "0.0.1"), //$NON-NLS-1$ //$NON-NLS-2$
    DEFAULT_TEMPLATE("default_template", "0.0.1"), //$NON-NLS-1$ //$NON-NLS-2$
    HANDLE_REJECTS_START("handle_rejects_start", "0.0.1"), //$NON-NLS-1$ //$NON-NLS-2$
    HANDLE_REJECTS_END("handle_rejects_end", "0.0.1"), //$NON-NLS-1$ //$NON-NLS-2$

    MR_HEADER("mr_header", "0.0.1"), //$NON-NLS-1$ //$NON-NLS-2$
    MR_FOOTER("mr_footer", "0.0.1"), //$NON-NLS-1$ //$NON-NLS-2$
    MR_COMPONENT_MRCONFIG_HEADER("mr_component_mrconfig_header", "0.0.1"), //$NON-NLS-1$ //$NON-NLS-2$
    MR_COMPONENT_MRCONFIG_FOOTER("mr_component_mrconfig_footer", "0.0.1"), //$NON-NLS-1$ //$NON-NLS-2$
    MR_SUBPROCESS_HEADER("mr_subprocess_header", "0.0.1"), //$NON-NLS-1$ //$NON-NLS-2$
    MR_SUBPROCESS_RUN("mr_subprocess_run", "0.0.1"), //$NON-NLS-1$ //$NON-NLS-2$
    MR_SUBPROCESS_FOOTER("mr_subprocess_footer", "0.0.1"), //$NON-NLS-1$ //$NON-NLS-2$

    // just to have compiled templates
    FOOTER_ROUTE("footer_route", "0.0.1"), //$NON-NLS-1$ //$NON-NLS-2$
    HEADER_ROUTE("header_route", "0.0.1"), //$NON-NLS-1$ //$NON-NLS-2$
    MICROSERVICE_APP_SECURITY("microservice_app_security","0.0.1"), //$NON-NLS-1$ //$NON-NLS-2$
    CAMEL_RUNIF("camel_run_if", "0.0.1"), //$NON-NLS-1$ //$NON-NLS-2$
    CAMEL_SPECIALLINKS("camel_speciallinks", "0.0.1"), //$NON-NLS-1$ //$NON-NLS-2$
    MICROSERVICE_APP("microservice_app", "0.0.1"), //$NON-NLS-1$ //$NON-NLS-2$

    HEADER_ADDITIONAL("header_additional", "0.0.1"), //$NON-NLS-1$ //$NON-NLS-2$

    SPARK_HEADER("spark_header", "0.0.1"), //$NON-NLS-1$ //$NON-NLS-2$
    SPARK_FOOTER("spark_footer", "0.0.1"), //$NON-NLS-1$ //$NON-NLS-2$
    SPARK_POST_PROCESSING_HEADER("spark_post_proccessing_header", "0.0.1"), //$NON-NLS-1$ //$NON-NLS-2$
    SPARK_POST_PROCESSING_FOOTER("spark_post_proccessing_footer", "0.0.1"), //$NON-NLS-1$ //$NON-NLS-2$
    SPARK_SUBPROCESS_HEADER("spark_subprocess_header", "0.0.1"), //$NON-NLS-1$ //$NON-NLS-2$
    SPARK_SUBPROCESS_FOOTER("spark_subprocess_footer", "0.0.1"), //$NON-NLS-1$ //$NON-NLS-2$
    SPARKSTREAMING_HEADER("sparkstreaming_header", "0.0.1"), //$NON-NLS-1$ //$NON-NLS-2$
    SPARKSTREAMING_FOOTER("sparkstreaming_footer", "0.0.1"), //$NON-NLS-1$ //$NON-NLS-2$
    SPARKSTREAMING_SUBPROCESS_HEADER("sparkstreaming_subprocess_header", "0.0.1"), //$NON-NLS-1$ //$NON-NLS-2$
    SPARKSTREAMING_POST_PROCESSING_HEADER("sparkstreaming_post_processing_header", "0.0.1"), //$NON-NLS-1$ //$NON-NLS-2$
    SPARKSTREAMING_POST_PROCESSING_FOOTER("sparkstreaming_post_processing_footer", "0.0.1"), //$NON-NLS-1$ //$NON-NLS-2$
    SPARKSTREAMING_CHECKPOINT_SUBPROCESS_HEADER("sparkstreaming_checkpoint_subprocess_header", "0.0.1"), //$NON-NLS-1$ //$NON-NLS-2$
    SPARKSTREAMING_SUBPROCESS_FOOTER("sparkstreaming_subprocess_footer", "0.0.1"), //$NON-NLS-1$ //$NON-NLS-2$
    SPARKSTREAMING_CHECKPOINT_SUBPROCESS_FOOTER("sparkstreaming_checkpoint_subprocess_footer", "0.0.1"), //$NON-NLS-1$ //$NON-NLS-2$
    SPARK_COMPONENT_SPARKCONFIG_HEADER("spark_component_sparkconfig_header", "0.0.1"), //$NON-NLS-1$ //$NON-NLS-2$
    SPARK_COMPONENT_SPARKCONFIG_FOOTER("spark_component_sparkconfig_footer", "0.0.1"); //$NON-NLS-1$ //$NON-NLS-2$

    private String templateName;

    private String version;

    /**
     *
     * @param templateName
     */
    private EInternalTemplate(String templateName, String version) {
        this.templateName = templateName;
        this.version = version;
    }

    /**
     * Getter for templateName.
     *
     * @return the templateName
     */
    public String getTemplateName() {
        return this.templateName;
    }

    /**
     * Getter for version.
     *
     * @return the version
     */
    public String getVersion() {
        return this.version;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return this.templateName;
    }
}
