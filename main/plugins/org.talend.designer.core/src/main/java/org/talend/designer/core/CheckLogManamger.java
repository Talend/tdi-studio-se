package org.talend.designer.core;

import java.util.Collection;

import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.process.IProcess;
import org.talend.repository.ui.utils.Log4jPrefsSettingManager;
import org.talend.repository.ui.utils.UpdateLog4jJarUtils;

public class CheckLogManamger {

    public static boolean isSelectLog4j2() {
        return Log4jPrefsSettingManager.getInstance().isSelectLog4j2();
    }

    public static void updateLog4jToModuleList(Collection<ModuleNeeded> jarList, IProcess currentProcess,
            boolean isSelectLog4j2) {
        UpdateLog4jJarUtils.addLog4jToModuleList(jarList, isSelectLog4j2, currentProcess);
    }
}
