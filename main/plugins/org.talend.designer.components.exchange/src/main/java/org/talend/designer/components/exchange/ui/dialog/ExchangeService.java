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
package org.talend.designer.components.exchange.ui.dialog;

import java.io.IOException;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.general.Project;
import org.talend.core.service.IExchangeService;
import org.talend.designer.components.exchange.ui.views.ExchangeEditorInput;
import org.talend.designer.components.exchange.util.ExchangeWebService;
import org.talend.registration.wizards.register.TalendForgeDialog;
import org.talend.repository.ProjectManager;

import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
 *
 * DOC hcyi class global comment. Detailled comment
 */
public class ExchangeService implements IExchangeService {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.general.IExchangeService#openExchangeDialog()
     */
    @Override
    public String openExchangeDialog() {
        ImportExchangeDialog dialog = new ImportExchangeDialog(Display.getCurrent().getActiveShell());
        dialog.open();
        return dialog.getSelectFile();
    }

    @Override
    public void openExchangeEditor() {
        IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        if (activePage != null) {
            Project proj = ProjectManager.getInstance().getCurrentProject();
            if (proj.getExchangeUser() == null
                    || checkUserAndPass(proj.getExchangeUser().getUsername(), proj.getExchangeUser().getPassword()) != null) {
                if (proj != null) {
                    if (proj.getExchangeUser().getUsername() == "" && proj.getExchangeUser().getPassword() == "") {
                        String userEmail = null;
                        if (proj.getAuthor() != null) {
                            userEmail = proj.getAuthor().getLogin();
                        }
                        TalendForgeDialog tfDialog = new TalendForgeDialog(DisplayUtils.getDefaultShell(), userEmail);
                        tfDialog.open();
                    }
                    // MessageDialog.openError(null, "Exchange", Messages.getString("Exchange.logon.error"));
                    return;
                }
            }
            try {
                ExchangeEditorInput input = new ExchangeEditorInput();
                IEditorPart findEditor = activePage.findEditor(input);
                if (findEditor == null) {
                    activePage.openEditor(input, "org.talend.designer.components.exchange.ui.views.ExchangeView");
                } else {
                    activePage.activate(findEditor);
                }
            } catch (PartInitException e) {
                ExceptionHandler.process(e);
            }
        }

    }

    @Override
    public String checkUserAndPass(String username, String password) {
        String errorMessage = "Wrong user or password";
        if (username == null || "".equals(username) || password == null || "".equals(password)) {
            return errorMessage;
        }
        JSONObject tokenMessage = new JSONObject();
        try {
            tokenMessage.put("username", username);
            tokenMessage.put("passwordHash", password);
            JSONObject token = new us.monoid.json.JSONObject();
            token.put("contributedExtension", tokenMessage);

            String u = ExchangeWebService.exchangeWSServer + "contributedExtension.php?data=" + token;
            JSONObject answer = ExchangeWebService.readJsonFromUrl(u);
            if (answer != null && answer.get("result") != null && answer.get("result").equals("ERROR USERNAME/PASSWORD")) {
                return errorMessage;
            }
        } catch (JSONException e) {
            // if there is no result , user and pass are right and can list the extensions
            return null;
        } catch (IOException e) {
            return errorMessage;
        }
        return null;
    }

}
