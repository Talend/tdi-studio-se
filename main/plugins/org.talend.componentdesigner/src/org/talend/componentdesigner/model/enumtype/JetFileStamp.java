// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.componentdesigner.model.enumtype;

import java.util.ArrayList;
import java.util.List;

import org.talend.componentdesigner.PluginConstant;

/**
 * All the type of jet files type. <br/>
 * 
 */
public enum JetFileStamp {
    JETBEGINSTAMP(PluginConstant.JETBEGIN_STAMP),
    JETMAINSTAMP(PluginConstant.JETMAIN_STAMP),
    JETENDSTAMP(PluginConstant.JETEND_STAMP);

    private final String fileStamp;

    JetFileStamp(String fileStampName) {
        this.fileStamp = fileStampName;
    }

    public String getFileStampName() {
        return this.fileStamp;
    }

    public static final List<JetFileStamp> findFileStamps(boolean begin, boolean main, boolean end) {
        List<JetFileStamp> jetFileStamps = new ArrayList<JetFileStamp>();
        if (begin) {
            jetFileStamps.add(JETBEGINSTAMP);
        }
        if (main) {
            jetFileStamps.add(JETMAINSTAMP);
        }
        if (end) {
            jetFileStamps.add(JETENDSTAMP);
        }
        return jetFileStamps;
    }

    public static final JetFileStamp findFileStamp(String fileStamp) {
        if (fileStamp.equals(PluginConstant.JETMAIN_STAMP)) {
            return JETMAINSTAMP;
        } else if (fileStamp.equals(PluginConstant.JETEND_STAMP)) {
            return JETENDSTAMP;
        } else {
            return JETBEGINSTAMP;
        }
    }
}
