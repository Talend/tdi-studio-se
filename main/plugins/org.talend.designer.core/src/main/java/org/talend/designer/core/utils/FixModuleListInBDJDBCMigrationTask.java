// ============================================================================
//
// Copyright (C) 2006-2020 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.utils;

import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.lang3.StringUtils;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.maven.MavenUrlHelper;

/**
 * created by bhe on Dec 12, 2020 Detailled comment
 *
 */
public class FixModuleListInBDJDBCMigrationTask extends UpdateModuleListInComponentsMigrationTask {

    protected ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();


    public String getMavenUriForJar(String jarName) {
        jarName = TalendTextUtils.removeQuotes(jarName);

        if (StringUtils.isEmpty(jarName) || !jarName.startsWith(MavenUrlHelper.MVN_PROTOCOL)) {
            return jarName;
        }

        String[] vals = jarName.split("/");
        if (vals.length > 3 && vals[0].equals("mvn:org.talend.libraries") && vals[2].equals("6.0.0-SNAPSHOT")
                && (vals[1].equals("context") || vals[1].startsWith("((String)context"))) {
            String ctx = vals[1] + "." + vals[vals.length - 1];
            boolean containContext = containContext(ctx);
            if (containContext) {
                return ctx;
            }
        }

        return jarName;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.IProjectMigrationTask#getOrder()
     */
    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2020, 12, 12, 12, 0, 0);
        return gc.getTime();
    }
}
