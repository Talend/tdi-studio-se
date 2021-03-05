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
package org.talend.designer.core.utils;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.GregorianCalendar;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.utils.Hex;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.runtime.maven.MavenUrlHelper;

/**
 * DOC sbliu  class global comment. Detailled comment
 */
public class UpdateModuleListInComponents2MigrationTask extends UpdateModuleListInComponentsMigrationTask {

    @Override
    protected String parseJarNameForHexVaue(String jarName) {
        if (jarName.startsWith(MavenUrlHelper.MVN_PROTOCOL)) {
            try {
                String artifactId = MavenUrlHelper.parseMvnUrl(jarName).getArtifactId();
                String elemValue = new String(Hex.decodeHex(artifactId.toCharArray()), "UTF-8");

                if (ContextParameterUtils.isContainContextParam(elemValue)) {
                    jarName = artifactId;
                } else {
                    jarName = Hex.encodeHexString(jarName.replace(artifactId, elemValue).getBytes());
                }
            } catch (UnsupportedEncodingException e) {
                ExceptionHandler.process(e);
            }
        } else {
            jarName = super.parseJarNameForHexVaue(jarName);
        }

        return jarName;
    }

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2021, 1, 27, 12, 0, 0);
        return gc.getTime();
    }
}
