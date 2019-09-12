package org.talend.designer.core;

import java.util.Collection;

import org.talend.core.model.general.ModuleNeeded;
import org.talend.repository.ui.utils.Log4jPrefsSettingManager;

public class CheckLogManamger {

    public static boolean isSelectLog4j2() {
        return Log4jPrefsSettingManager.getInstance().isSelectLog4j2();
    }

    public static void updateLog4jToModuleList(Collection<ModuleNeeded> jarList) {
        Log4jPrefsSettingManager.getInstance().addLog4jToModuleList(jarList);
    }
}
