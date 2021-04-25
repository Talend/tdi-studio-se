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
package org.talend.designer.core.model.components;

import org.talend.designer.core.i18n.Messages;

/**
 * DOC ycbai class global comment. Detailled comment
 */
public enum EOozieParameterName {

    OOZIE_PROPERTY_TYPENAME(Messages.getString("EOozieParameterName.propertyType")),

    HADOOP_APP_PATH(Messages.getString("EOozieParameterName.hadoopAppPath")), //$NON-NLS-1$

    JOBID_FOR_OOZIE(Messages.getString("EOozieParameterName.jobId")), //$NON-NLS-1$

    REPOSITORY_CONNECTION_ID(Messages.getString("EOozieParameterName.repositoryConnectionId")), //$NON-NLS-1$

    OOZIE_HADOOP_DISTRIBUTION(Messages.getString("EOozieParameterName.hadoopDistribution")),

    OOZIE_HADOOP_VERSION(Messages.getString("EOozieParameterName.hadoopVersion")),

    OOZIE_ENABLE_KERBEROS(Messages.getString("EOozieParameterName.enableKerberos")),

    OOZIE_NAME_NODE_PRINCIPAL(Messages.getString("EOozieParameterName.nameNodePrincipal")),

    OOZIE_USERNAME(Messages.getString("EOozieParameterName.userName")),

    OOZIE_GROUP(Messages.getString("EOozieParameterName.group")),

    OOZIE_USE_KEYTAB(Messages.getString("EOozieParameterName.useKeytab")),

    OOZIE_KT_PRINCIPAL(Messages.getString("EOozieParameterName.ktPrincipal")),

    OOZIE_KEY_TAB(Messages.getString("EOozieParameterName.keytab")),

    OOZIE_ENABLE_OO_KERBEROS(Messages.getString("EOozieParameterName.enableOoKerberos")),

    OOZIE_NAME_NODE_END_POINT(Messages.getString("EOozieParameterName.nameNodeEndPoint")),

    OOZIE_JOB_TRACKER_ENDPOINT(Messages.getString("EOozieParameterName.jobTrackerEndPoint")),

    OOZIE_END_POINT(Messages.getString("EOozieParameterName.oozieEndPointValue")),
    ;

    private String displayName;

    EOozieParameterName(String displayName) {
        this.displayName = displayName;
    }

    public String getName() {
        return this.toString();
    }

    public String getDisplayName() {
        return this.displayName;
    }

}
