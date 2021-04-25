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

import java.util.List;
import java.util.Properties;

import org.eclipse.ui.intro.IIntroSite;
import org.eclipse.ui.intro.config.IIntroAction;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.designer.components.exchange.model.ComponentExtension;
import org.talend.designer.components.exchange.model.ExchangeFactory;
import org.talend.designer.components.exchange.model.VersionRevision;
import org.talend.designer.components.exchange.ui.actions.DeleteExtensionAction;
import org.talend.designer.components.exchange.ui.actions.InsertionExtensionAction;
import org.talend.designer.components.exchange.ui.actions.ModifyExtensionAction;
import org.talend.designer.components.exchange.ui.actions.UploadRevisionAction;
import org.talend.designer.components.exchange.ui.views.ExchangeManager;
import org.talend.utils.json.JSONException;
import org.talend.utils.json.JSONObject;

/**
 * DOC talend class global comment. Detailled comment
 */
public class MyExtensionAction implements IIntroAction {

    public void run(IIntroSite site, Properties params) {
        if (params != null) {
            Object actionType = params.get(ContentConstants.KEY_TYPE);
            if (ContentConstants.UPLOAD_MY_EXTENSION.equals(actionType)) {
                try {
                    String values = (String) params.get(ContentConstants.EXTENSIONVALUES);
                    if (values != null) {
                        List<VersionRevision> versionRevisions = ExchangeManager.getInstance().getVersionRevisions();
                        ComponentExtension extension = ExchangeFactory.eINSTANCE.createComponentExtension();
                        setValuesForExension(extension, values, versionRevisions);
                        ExchangeManager.getInstance().setSelectedExtension(extension);
                        InsertionExtensionAction uploadAction = new InsertionExtensionAction(extension);
                        uploadAction.run();
                    }
                } catch (JSONException e) {
                    ExceptionHandler.process(e);
                }

            } else if (ContentConstants.UPDATE_MY_EXTENSION.equals(actionType)
                    || ContentConstants.MODIFY_MY_EXTENSION.equals(actionType)) {
                try {
                    String values = (String) params.get(ContentConstants.EXTENSIONVALUES);
                    if (values != null) {
                        List<VersionRevision> versionRevisions = ExchangeManager.getInstance().getVersionRevisions();
                        ComponentExtension selectedExtension = ExchangeManager.getInstance().getSelectedExtension();
                        setValuesForExension(selectedExtension, values, versionRevisions);
                        if (ContentConstants.UPDATE_MY_EXTENSION.equals(actionType)) {
                            UploadRevisionAction uploadAction = new UploadRevisionAction(selectedExtension);
                            uploadAction.run();
                        } else {
                            ModifyExtensionAction modifyAction = new ModifyExtensionAction();
                            modifyAction.run();
                        }
                    }
                } catch (JSONException e) {
                    ExceptionHandler.process(e);
                }

            } else if (ContentConstants.UPLOAD_NEW_VERSION_ACTION.equals(actionType)
                    || ContentConstants.MODIFY_ACTION.equals(actionType)) {
                Object object = params.get(ContentConstants.KEY_EXTENSION_ID);
                String key = null;
                if (object instanceof String) {
                    key = (String) object;
                }
                ComponentExtension componentExtension = MyExchangeContentProvider.componentMap.get(key);
                if (componentExtension != null) {
                    ExchangeManager.getInstance().setSelectedExtension(componentExtension);
                    if (ContentConstants.UPLOAD_NEW_VERSION_ACTION.equals(actionType)) {
                        ExchangeManager.getInstance().generateXHTMLPage(ContentConstants.UL_UPLOAD_EXTENSION_VERSION,
                                new String[] { ContentConstants.LAST_AVAILABLE_VERSION });
                    } else {
                        ExchangeManager.getInstance().generateXHTMLPage(ContentConstants.UL_MODIFY_MY_EXTENSION_PAGE,
                                new String[] { ContentConstants.EXTENSION_LABEL });
                    }
                }
            } else if (ContentConstants.DELETE_ACTION.equals(actionType)) {
                Object object = params.get(ContentConstants.KEY_EXTENSION_ID);
                String key = null;
                if (object instanceof String) {
                    key = (String) object;
                }
                ComponentExtension componentExtension = MyExchangeContentProvider.componentMap.get(key);
                ExchangeManager.getInstance().setSelectedExtension(componentExtension);
                if (componentExtension != null) {
                    final DeleteExtensionAction delAction = new DeleteExtensionAction(componentExtension);
                    if (delAction != null) {
                        delAction.run();
                    }
                }
            }

        }

    }

    private void setValuesForExension(ComponentExtension componentExtension, String values, List<VersionRevision> versionRevisions)
            throws JSONException {

        JSONObject jsObject = new JSONObject(values);
        if (jsObject.has(ContentConstants.EXTENSION_VALUE_LABEL)) {
            componentExtension.setLabel(jsObject.getString(ContentConstants.EXTENSION_VALUE_LABEL));
        }
        if (jsObject.has(ContentConstants.EXTENSION_VALUE_LASTVERSIONAVAILABLE)) {
            componentExtension.setLastVersionAvailable(jsObject.getString(ContentConstants.EXTENSION_VALUE_LASTVERSIONAVAILABLE));
        }
        if (jsObject.has(ContentConstants.EXTENSION_VALUE_DESCRIPTION)) {
            componentExtension.setDescription(jsObject.getString(ContentConstants.EXTENSION_VALUE_DESCRIPTION));
        }
        if (jsObject.has(ContentConstants.EXTENSION_VALUE_COMPATIBLES)) {
            String compatibles = jsObject.getString(ContentConstants.EXTENSION_VALUE_COMPATIBLES);

            String filter = null;
            if (jsObject.has(ContentConstants.EXTENSION_VALUE_Filter)) {
                filter = jsObject.getString(ContentConstants.EXTENSION_VALUE_Filter);
            }
            String revision = filterVersionRevisionsToString(compatibles, filter, versionRevisions);
            componentExtension.setListVersionCompatibles(revision);
        }
        if (jsObject.has(ContentConstants.EXTENSION_VALUE_FILENAME)) {
            componentExtension.setFilename(jsObject.getString(ContentConstants.EXTENSION_VALUE_FILENAME));
        }
    }

    private String filterVersionRevisionsToString(String status, String version, List<VersionRevision> fVersionRevisions) {
        if (fVersionRevisions == null || fVersionRevisions.isEmpty()) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        if (status.equals("all")) {
            for (VersionRevision ver : fVersionRevisions) {
                sb.append(ver.getVersionId());
                sb.append(",");
            }
            return sb.substring(0, sb.length() - 1);
        } else if (status.equals("older")) {
            for (VersionRevision ver : fVersionRevisions) {
                if (ver.getVersionName().equalsIgnoreCase(version)) {
                    break;
                }
                sb.append(ver.getVersionId());
                sb.append(",");
            }
            return sb.substring(0, sb.length() - 1);
        } else if (status.equals("newer")) {
            boolean newerb = false;
            for (VersionRevision ver : fVersionRevisions) {
                if (ver.getVersionName().equalsIgnoreCase(version)) {
                    newerb = true;
                }
                if (newerb) {
                    sb.append(ver.getVersionId());
                    sb.append(",");
                }

            }
            return sb.substring(0, sb.length() - 1);
        } else if (status.equals("except")) {
            for (VersionRevision ver : fVersionRevisions) {
                if (!ver.getVersionName().equalsIgnoreCase(version)) {
                    sb.append(ver.getVersionId());
                    sb.append(",");
                }
            }
            return sb.substring(0, sb.length() - 1);
        } else if (status.equals("only")) {
            for (VersionRevision ver : fVersionRevisions) {
                if (ver.getVersionName().equalsIgnoreCase(version)) {
                    sb.append(ver.getVersionId());
                    break;
                }
            }
            return sb.toString();
        }

        return "";
    }

}
