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
package org.talend.sdk.component.studio.ui.composite.problemmanager;

import java.util.Map;
import java.util.Optional;

import org.eclipse.swt.widgets.Display;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.process.IElementParameter;

/**
 * DOC cmeng  class global comment. Detailled comment
 */
public class WizardProblemManager extends ColorfulProblemManager {

    private Optional<IWizardHandler> wizardHandler = Optional.ofNullable(null);

    public void setWizardHandler(IWizardHandler wizardHandler) {
        this.wizardHandler = Optional.ofNullable(wizardHandler);
    }

    public void updateWizardInfo() {
        try {
            Optional<String> errMsg = buildErrMsg();
            this.wizardHandler.ifPresent(c -> Display.getDefault().asyncExec(() -> c.showError(errMsg.orElse(null))));
            if (errMsg.isPresent()) {
                return;
            }

            Optional<String> warnMsg = buildWarnMsg();
            this.wizardHandler.ifPresent(c -> Display.getDefault().asyncExec(() -> c.showWarn(warnMsg.orElse(null))));
            if (warnMsg.isPresent()) {
                return;
            }

            Optional<String> infoMsg = buildInfoMsg();
            this.wizardHandler.ifPresent(c -> Display.getDefault().asyncExec(() -> c.showInfo(infoMsg.orElse(null))));
        } finally {
            this.wizardHandler.ifPresent(c -> Display.getDefault().asyncExec(() -> c.updateStatus()));
        }
    }

    private Optional<String> buildErrMsg() {
        return buildMsg(getErrMap());
    }

    private Optional<String> buildWarnMsg() {
        return buildMsg(getWarnMap());
    }

    private Optional<String> buildInfoMsg() {
        return buildMsg(getInfoMap());
    }

    private Optional<String> buildMsg(Map<IElementParameter, String> msgMap) {
        if (msgMap == null || msgMap.isEmpty()) {
            return Optional.ofNullable(null);
        }
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<IElementParameter, String> entry : msgMap.entrySet()) {
            try {
                builder.append(entry.getKey().getDisplayName()).append(": ").append(entry.getValue()).append("\n "); //$NON-NLS-1$ //$NON-NLS-2$
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
        }
        return Optional.of(builder.toString());
    }

    @Override
    public void setError(IElementParameter ep, String errMsg) {
        super.setError(ep, errMsg);
        updateWizardInfo();
    }

    @Override
    public void setWarn(IElementParameter ep, String warnMsg) {
        super.setWarn(ep, warnMsg);
        updateWizardInfo();
    }

    @Override
    public void setInfo(IElementParameter ep, String infoMsg) {
        super.setInfo(ep, infoMsg);
        updateWizardInfo();
    }

    public interface IWizardHandler {

        public void showError(String error);

        public void showWarn(String warn);

        public void showInfo(String info);

        public void updateStatus();
    }
}
