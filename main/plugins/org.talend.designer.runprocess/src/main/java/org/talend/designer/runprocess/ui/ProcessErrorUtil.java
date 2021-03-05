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
package org.talend.designer.runprocess.ui;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.talend.designer.runprocess.IProcessMessage;
import org.talend.designer.runprocess.ProcessMessage;
import org.talend.designer.runprocess.ProcessMessage.MsgType;
import org.talend.designer.runprocess.RunProcessContext;

/**
 * created by wchen on Dec 30, 2016 Detailled comment
 *
 */
public class ProcessErrorUtil {

    public static HashMap<String, IProcessMessage> getAllErrorMess(IProcessMessage psMess, RunProcessContext processContext) {
        HashMap<String, IProcessMessage> errorMessMap = null;
        if (psMess.getType().equals(MsgType.STD_ERR)) {
            errorMessMap = new HashMap<String, IProcessMessage>();
            String mess = psMess.getContent();
            String[] linesMess = mess.split("\n");//$NON-NLS-1$
            StringBuffer currentMess = new StringBuffer();
            String currenctJobName = processContext.getProcess().getName();
            for (int i = 0; i < linesMess.length; i++) {
                String tRunJobName = currenctJobName;
                String linemess = linesMess[i].trim();
                Pattern pattern = Pattern.compile("^Exception\\s*in\\s*component\\s*(\\w)+_(\\d)+\\s*\\((\\w)+\\)$");//$NON-NLS-1$
                Matcher m = pattern.matcher(linemess);
                if (m.find()) {
                    String[] allwords = linemess.split("\\s"); //$NON-NLS-1$
                    if (allwords.length == 5) {
                        String componentName = allwords[3];
                        tRunJobName = allwords[4].substring(1, allwords[4].length() - 1);

                        if (tRunJobName != null && tRunJobName.equals(currenctJobName)) {
                            if (i == 0) {
                                errorMessMap.put(componentName, psMess);
                            } else {
                                for (int j = i; j < linesMess.length; j++) {
                                    currentMess.append(linesMess[j] + "\n"); //$NON-NLS-1$
                                }
                                IProcessMessage currentProMess = new ProcessMessage(MsgType.STD_ERR, currentMess.toString());
                                errorMessMap.put(componentName, currentProMess);
                            }
                        }
                    }
                }

            }
        }
        return errorMessMap;
    }

}
