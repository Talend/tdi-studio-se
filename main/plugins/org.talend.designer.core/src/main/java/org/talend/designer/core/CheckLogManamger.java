package org.talend.designer.core;

import org.talend.repository.ui.utils.Log4jPrefsSettingManager;

public class CheckLogManamger {

    public static boolean isSelectLog4j2() {
        return Log4jPrefsSettingManager.getInstance().isSelectLog4j2();
    }
}
