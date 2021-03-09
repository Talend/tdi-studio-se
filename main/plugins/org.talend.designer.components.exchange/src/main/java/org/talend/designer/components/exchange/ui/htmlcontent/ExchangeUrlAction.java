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
package org.talend.designer.components.exchange.ui.htmlcontent;

import java.util.Properties;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.intro.IIntroSite;
import org.eclipse.ui.intro.config.IIntroAction;
import org.talend.commons.ui.swt.dialogs.ErrorDialogWidthDetailArea;
import org.talend.designer.components.exchange.ExchangePlugin;
import org.talend.designer.components.exchange.i18n.Messages;
import org.talend.designer.components.exchange.ui.ReviewComponentDialog;
import org.talend.designer.components.exchange.ui.actions.RefreshComponenentsAction;
import org.talend.designer.components.exchange.ui.views.ExchangeManager;
import org.talend.designer.components.exchange.util.ExchangeUtils;
import org.talend.designer.components.exchange.util.ExchangeWebService;
import org.talend.designer.components.exchange.util.WebserviceStatus;

/**
 * DOC talend class global comment. Detailled comment
 */
public class ExchangeUrlAction implements IIntroAction {

    public static final String RETURN_TO_FIRST_PAGE = "RETURN_TO_FIRST_PAGE";

    public static final String EDIT_REVIEWS = "EDIT_REVIEWS";

    public static final String KEY_TYPE = "type";

    public void run(IIntroSite site, Properties params) {
        if (params != null) {
            if (EDIT_REVIEWS.equals(params.get(KEY_TYPE))) {
                Display current = Display.getCurrent();
                if (current == null) {
                    current = Display.getDefault();
                }

                Shell shell = null;
                if (current != null) {
                    shell = current.getActiveShell();
                }
                final Shell fShell = shell;
                if (ExchangeManager.getInstance().getSelectedExtension() != null) {
                    ReviewComponentDialog reviewDialog = new ReviewComponentDialog(shell);
                    if (reviewDialog.open() == IDialogConstants.OK_ID) {
                        final String title = reviewDialog.getTitle();
                        final String rate = reviewDialog.getRating() + "";
                        final String review = reviewDialog.getReview();
                        Display.getDefault().asyncExec(new Runnable() {

                            public void run() {
                                if (ExchangeUtils.checkUserAndPassword()) {
                                    WebserviceStatus ws = ExchangeWebService.insertReviewService(ExchangeManager.getInstance()
                                            .getSelectedExtension().getIdExtension(), ExchangeUtils.TYPEEXTENSION,
                                            ExchangeUtils.getUserName(), ExchangeUtils.getPasswordHash(), title, review, rate);
                                    if (ws.isResult()) {
                                        RefreshComponenentsAction action = new RefreshComponenentsAction();
                                        action.run(new String[] { RefreshComponenentsAction.REFRESH_AVAILABLES },
                                                ContentConstants.UL_LIST_AVAILABLE_EXTENSIONS);
                                    } else {
                                        String mainMsg = Messages
                                                .getString("AvailableExtensionsComposite.ViewDetail.InsertionReviewFailure")
                                                + " "
                                                + Messages
                                                        .getString("AvailableExtensionsComposite.ViewDetail.InsertionReviewFailureTip");
                                        new ErrorDialogWidthDetailArea(fShell, ExchangePlugin.PLUGIN_ID, mainMsg, ws
                                                .getMessageException());
                                    }
                                } else {
                                    MessageDialog.openInformation(
                                            fShell,
                                            Messages.getString("AvailableExtensionsComposite.ViewDetail.WriteReview"), Messages.getString("Exchange.logon.error")); //$NON-NLS-1$
                                }
                            }
                        });
                    }
                }
            }
        }

    }

}
