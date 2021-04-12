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
package org.talend.designer.components.exchange.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Path;
import org.talend.designer.components.exchange.i18n.Messages;
import org.talend.designer.components.exchange.model.Category;
import org.talend.designer.components.exchange.model.ExchangeFactory;
import org.talend.designer.components.exchange.model.VersionRevision;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import us.monoid.web.AbstractContent;
import us.monoid.web.FormData;
import us.monoid.web.Resty;
import us.monoid.web.TextResource;
import us.monoid.web.mime.MultipartContent;

/**
 * DOC hcyi class global comment. Detailled comment
 */
public class ExchangeWebService {

    public final static String exchangeWSServer = "http://www.talendforge.org/exchange/webservices/"; //$NON-NLS-1$

    public void webServiceToken(String[] args) {
        Resty r = new Resty();

        JSONObject token = new us.monoid.json.JSONObject();

        try {
            JSONObject tokenMessage = new JSONObject();
            tokenMessage.put("version", "4.2.2r56343");
            tokenMessage.put("uniqueId", "XXXXXX");
            tokenMessage.put("typeStudio", "TOS");

            JSONObject properties = new JSONObject();
            properties.put("tos.count.localprojects", "10");
            properties.put("tos.count.jobs", "48");
            properties.put("tos.count.jobsperproject", "5");
            // ... //
            tokenMessage.put("properties", properties);
            token.put("tokenStudio", tokenMessage);

            AbstractContent ac = Resty.content(token);
            MultipartContent mpc = Resty.form(new FormData("data", ac));
            TextResource result = r.text("http://www.talend.com/TalendRegisterWS/tokenstudio.php", mpc);
            System.out.println(result.toString());

        } catch (JSONException e) {
            //
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * DOC hcyi Comment method "searchExtensionJSONArray".
     *
     * @return
     */
    public static JSONArray searchExtensionJSONArray(String typeExtension, String versionStudio, String category) {
        JSONObject tokenMessage = new JSONObject();
        JSONArray o = null;
        try {
            tokenMessage.put("typeExtension", typeExtension);
            tokenMessage.put("versionStudio", versionStudio);
            tokenMessage.put("search", category);

            JSONObject token = new us.monoid.json.JSONObject();
            token.put("searchExtension", tokenMessage);
            String u = exchangeWSServer + "availableExtension.php?data=" + token;
            JSONObject answer = readJsonFromUrl(u);
            if (answer != null) {
                JSONObject p = (JSONObject) answer.get("resultSearch");
                o = p.getJSONArray("extensions");
            }
        } catch (JSONException e) {
            //
        } catch (IOException e) {
            e.printStackTrace();
        }
        return o;
    }

    public static JSONObject searchExtensionViewDetail(String idExtension, String typeExtension) {
        Resty r = new Resty();

        JSONObject token = new us.monoid.json.JSONObject();
        JSONObject extension = new us.monoid.json.JSONObject();
        try {
            JSONObject tokenMessage = new JSONObject();
            tokenMessage.put("typeExtension", typeExtension);
            tokenMessage.put("idExtension", idExtension);
            token.put("viewDetail", tokenMessage);
            AbstractContent ac = Resty.content(token);
            MultipartContent mpc = Resty.form(new FormData("data", ac));
            TextResource result = r.text(exchangeWSServer + "availableExtension.php", mpc);
            JSONObject jbo = new JSONObject(result.toString());

            JSONObject resultViewDetail = jbo.getJSONObject("resultViewDetail");
            extension = resultViewDetail.getJSONObject("extension");

        } catch (JSONException e) {
            //
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return extension;
    }

    /**
     *
     * DOC hcyi Comment method "searchContributedExtensionJSONArray".
     *
     * @return
     */
    public static JSONArray searchContributedExtensionJSONArray(String username, String passwordHash) {
        JSONObject tokenMessage = new JSONObject();
        JSONArray o = null;
        try {
            tokenMessage.put("username", username);
            tokenMessage.put("passwordHash", passwordHash);
            JSONObject token = new us.monoid.json.JSONObject();
            token.put("contributedExtension", tokenMessage);

            String u = exchangeWSServer + "contributedExtension.php?data=" + token;
            JSONObject answer = readJsonFromUrl(u);
            if (answer != null) {
                JSONObject p = (JSONObject) answer.get("listContributedExtension");
                o = p.getJSONArray("extensions");
            }
        } catch (JSONException e) {
            //
        } catch (IOException e) {
            e.printStackTrace();
        }
        return o;
    }

    /**
     *
     * DOC hcyi Comment method "searchInstalledExtensionJSONArray".
     *
     * @return
     */
    public static JSONArray searchInstalledExtensionJSONArray(String username, String passwordHash) {
        JSONObject tokenMessage = new JSONObject();
        JSONArray o = null;
        try {
            tokenMessage.put("username", username);
            tokenMessage.put("passwordHash", passwordHash);
            JSONObject token = new us.monoid.json.JSONObject();
            token.put("installedExtension", tokenMessage);

            String u = exchangeWSServer + "installedExtension.php?data=" + token;
            JSONObject answer = readJsonFromUrl(u);
            if (answer != null) {
                JSONObject p = (JSONObject) answer.get("listInstalledExtension");
                o = p.getJSONArray("extensions");
            }
        } catch (JSONException e) {
            //
        } catch (IOException e) {
            e.printStackTrace();
        }
        return o;
    }

    /**
     *
     * DOC hcyi Comment method "downloadingExtensionService".
     *
     * @param idExtension
     * @param typeExtension
     * @param username
     * @param passwordHash
     * @return
     */
    public static WebserviceStatus downloadingExtensionService(String idExtension, String typeExtension, String username,
            String passwordHash) {
        WebserviceStatus ws = new WebserviceStatus();
        ws.setResult(false);
        JSONObject tokenMessage = new JSONObject();
        try {
            tokenMessage.put("typeExtension", typeExtension);
            tokenMessage.put("idExtension", idExtension);
            tokenMessage.put("username", username);
            tokenMessage.put("passwordHash", passwordHash);
            JSONObject token = new us.monoid.json.JSONObject();
            token.put("downloadedExtension", tokenMessage);

            String u = exchangeWSServer + "downloadedExtension.php?data=" + token;
            JSONObject answer = readJsonFromUrl(u);
            if (answer != null) {
                JSONObject resultObj = (JSONObject) answer.get("resultDownloadExtension");
                String linkDownload = resultObj.getString("linkDownload");
                //
                ws.setValue(linkDownload);
                ws.setResult(true);
                ws.setMessageException(Messages.getString("ExchangeWebService.downloadingExtensionSuccessful")); //$NON-NLS-1$
            }
        } catch (JSONException e) {
            //
            ws.setMessageException(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            ws.setMessageException(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            ws.setMessageException(e.getMessage());
        }
        return ws;
    }

    /**
     *
     * DOC hcyi Comment method "deleteExtensionService".
     *
     * @param idExtension
     * @param typeExtension
     * @param username
     * @param passwordHash
     * @return
     */
    public static WebserviceStatus deleteExtensionService(String idExtension, String typeExtension, String username,
            String passwordHash) {
        WebserviceStatus ws = new WebserviceStatus();
        ws.setResult(false);
        JSONObject tokenMessage = new JSONObject();
        try {
            tokenMessage.put("username", username);
            tokenMessage.put("passwordHash", passwordHash);
            tokenMessage.put("idExtension", idExtension);
            tokenMessage.put("typeExtension", typeExtension);
            JSONObject token = new us.monoid.json.JSONObject();
            token.put("extension", tokenMessage);
            String u = exchangeWSServer + "deleteExtension.php?data=" + token;
            JSONObject result = readJsonFromUrl(u);
            //
            if (result != null) {
                Object object = result.get("result");
                if (object != null && object.equals("DELETE OK")) {
                    ws.setResult(true);
                    ws.setMessageException(Messages.getString("ExchangeWebService.deleteExtensionSuccessful")); //$NON-NLS-1$
                } else {
                    ws.setMessageException(object.toString());
                }
            }
        } catch (JSONException e) {
            //
            ws.setMessageException(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            ws.setMessageException(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            ws.setMessageException(e.getMessage());
        }
        return ws;
    }

    /**
     *
     * DOC hcyi Comment method "searchLastRevisionForExtensionService".
     *
     * @param idExtension
     * @param username
     * @param passwordHash
     * @return
     */
    public static WebserviceStatus searchLastRevisionForExtensionService(String idExtension, String username, String passwordHash) {
        WebserviceStatus ws = new WebserviceStatus();
        ws.setResult(false);
        JSONObject tokenMessage = new JSONObject();
        try {
            tokenMessage.put("username", username);
            tokenMessage.put("passwordHash", passwordHash);
            tokenMessage.put("idExtension", idExtension);
            JSONObject token = new us.monoid.json.JSONObject();
            token.put("extension", tokenMessage);

            String u = exchangeWSServer + "modifyExtension.php?data=" + token;
            JSONObject answer = readJsonFromUrl(u);
            if (answer != null) {
                Object object = answer.get("lastRevision");
                if (object != null && !object.equals("")) {
                    ws.setValue(object.toString());
                    ws.setResult(true);
                }
            }
        } catch (JSONException e) {
            //
            ws.setMessageException(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            ws.setMessageException(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            ws.setMessageException(e.getMessage());
        }
        return ws;
    }

    /**
     *
     * DOC hcyi Comment method "searchCategoryExtensionJSONArray".
     *
     * @param typeExtension
     * @return
     */
    public static List<Category> searchCategoryExtensionJSONArray(String typeExtension) {
        List<Category> fCategorys = new ArrayList<Category>();
        JSONObject tokenMessage = new JSONObject();
        try {
            tokenMessage.put("typeExtension", typeExtension);
            JSONObject token = new us.monoid.json.JSONObject();
            token.put("extension", tokenMessage);

            String u = exchangeWSServer + "listCategoryExtension.php?data=" + token;
            JSONObject resultObj = readJsonFromUrl(u);
            if (resultObj != null) {
                JSONObject p = (JSONObject) resultObj.get("listCategory");
                JSONArray resultArry = p.getJSONArray("category");
                if (resultArry != null) {
                    for (int i = 0; i < resultArry.length(); i++) {
                        JSONObject extensionObj = resultArry.getJSONObject(i);
                        if (extensionObj != null) {
                            Category fCategory = ExchangeFactory.eINSTANCE.createCategory();
                            fCategory.setCategoryId(extensionObj.getString("id_category"));
                            fCategory.setCategoryName(extensionObj.getString("name"));
                            fCategorys.add(fCategory);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            //
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fCategorys;
    }

    /**
     *
     * DOC hcyi Comment method "searchVersionRevisionJSONArray".
     *
     * @param typeExtension
     * @return
     */
    public static List<VersionRevision> searchVersionRevisionJSONArray(String typeExtension) {
        List<VersionRevision> fVersionRevisions = new ArrayList<VersionRevision>();
        JSONObject tokenMessage = new JSONObject();
        try {
            tokenMessage.put("typeExtension", typeExtension);
            JSONObject token = new us.monoid.json.JSONObject();
            token.put("extension", tokenMessage);
            String u = exchangeWSServer + "listVersionRevision.php?data=" + token;
            JSONObject resultObj = readJsonFromUrl(u);
            if (resultObj != null) {
                JSONObject p = (JSONObject) resultObj.get("listVersion");
                JSONArray resultArry = p.getJSONArray("version");
                if (resultArry != null) {
                    for (int i = 0; i < resultArry.length(); i++) {
                        JSONObject extensionObj = resultArry.getJSONObject(i);
                        if (extensionObj != null) {
                            VersionRevision versionRevision = ExchangeFactory.eINSTANCE.createVersionRevision();
                            versionRevision.setVersionId(extensionObj.getString("id_version"));
                            versionRevision.setVersionName(extensionObj.getString("version"));
                            fVersionRevisions.add(versionRevision);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            //
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fVersionRevisions;
    }

    /**
     *
     * DOC hcyi Comment method "insertReviewService".
     *
     * @param idExtension
     * @param typeExtension
     * @param username
     * @param passwordHash
     * @param title
     * @param description
     * @param userRating
     * @return
     */
    public static WebserviceStatus insertReviewService(String idExtension, String typeExtension, String username,
            String passwordHash, String title, String description, String userRating) {
        WebserviceStatus ws = new WebserviceStatus();
        ws.setResult(false);
        Resty r = new Resty();
        JSONObject tokenMessage = new JSONObject();
        try {
            tokenMessage.put("typeExtension", typeExtension);
            tokenMessage.put("idExtension", idExtension);
            tokenMessage.put("username", username);
            tokenMessage.put("passwordHash", passwordHash);
            tokenMessage.put("title", title);
            tokenMessage.put("description", description);
            tokenMessage.put("userRating", userRating);
            JSONObject token = new us.monoid.json.JSONObject();
            token.put("review", tokenMessage);
            AbstractContent ac = Resty.content(token);
            MultipartContent mpc = Resty.form(new FormData("data", ac));
            TextResource textResult = r.text(exchangeWSServer + "downloadedExtension.php", mpc);
            JSONObject resultObject = new JSONObject(textResult.toString());
            //
            Object object = resultObject.get("result");
            if (object != null && object.equals("INSERT OK")) {
                ws.setResult(true);
                ws.setMessageException(Messages.getString("ExchangeWebService.insertReviewSuccessful")); //$NON-NLS-1$
            } else {
                ws.setMessageException(object.toString());
            }
        } catch (JSONException e) {
            ws.setMessageException(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            ws.setMessageException(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            ws.setMessageException(e.getMessage());
        }
        return ws;
    }

    public static String asHex(byte[] buf) {
        char[] hexArray = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        char[] chars = new char[2 * buf.length];
        int v;
        for (int j = 0; j < buf.length; j++) {
            v = buf[j] & 0xFF;
            chars[j * 2] = hexArray[v / 16];
            chars[j * 2 + 1] = hexArray[v % 16];
        }

        return new String(chars);
    }

    /**
     *
     * DOC hcyi Comment method "insertionRevisionService".
     *
     * @param idExtension
     * @param typeExtension
     * @param username
     * @param passwordHash
     * @param version
     * @param listVersionCompatibles
     * @param filename
     * @param content
     * @param description
     * @param agreement
     * @return
     */
    public static WebserviceStatus insertionRevisionService(String idExtension, String typeExtension, String username,
            String passwordHash, String version, String listVersionCompatibles, String filename, String content,
            String description, String agreement) {
        byte[] fileBytes = null;
        try {
            File f = new File(filename);
            if (f != null) {
                FileInputStream fis = new FileInputStream(f);
                if (fis != null) {
                    int len = fis.available();
                    fileBytes = new byte[len];
                    fis.read(fileBytes);
                }
            }

        } catch (Exception e) {
            //
        }

        WebserviceStatus ws = new WebserviceStatus();
        ws.setResult(false);
        Resty r = new Resty();
        JSONObject tokenMessage = new JSONObject();
        try {
            tokenMessage.put("username", username);
            tokenMessage.put("passwordHash", passwordHash);
            tokenMessage.put("idExtension", idExtension);
            tokenMessage.put("typeExtension", typeExtension);
            tokenMessage.put("version", version);
            tokenMessage.put("versionCompatibles", listVersionCompatibles);
            tokenMessage.put("filename", new Path(filename).lastSegment());
            tokenMessage.put("content", asHex(fileBytes));
            tokenMessage.put("description", description == null ? "" : description.replace(" ", "%20"));
            tokenMessage.put("agreement", agreement);
            JSONObject token = new us.monoid.json.JSONObject();
            token.put("newRevision", tokenMessage);
            AbstractContent ac = Resty.content(token);
            MultipartContent mpc = Resty.form(new FormData("data", ac));
            TextResource textResult = r.text(exchangeWSServer + "addRevision.php", mpc);
            JSONObject resultObject = new JSONObject(textResult.toString());
            JSONObject result = (JSONObject) resultObject.get("resultNewRevision");
            String idRevision = result.getString("idRevision");
            //
            ws.setValue(idRevision);
            ws.setResult(true);
            ws.setMessageException(Messages.getString("ExchangeWebService.insertionRevisionSuccessful")); //$NON-NLS-1$
        } catch (JSONException e) {
            ws.setMessageException(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            ws.setMessageException(e.getMessage());
        }
        return ws;
    }

    /**
     *
     * DOC hcyi Comment method "updateRevisionService".
     *
     * @param idExtension
     * @param typeExtension
     * @param username
     * @param passwordHash
     * @param version
     * @param listVersionCompatibles
     * @param description
     * @param agreement
     * @return
     */
    public static WebserviceStatus updateRevisionService(String idRevision, String typeExtension, String username,
            String passwordHash, String version, String listVersionCompatibles, String description, String agreement) {
        WebserviceStatus ws = new WebserviceStatus();
        ws.setResult(false);
        JSONObject tokenMessage = new JSONObject();
        try {
            tokenMessage.put("username", username);
            tokenMessage.put("passwordHash", passwordHash);
            tokenMessage.put("idRevision", idRevision);
            tokenMessage.put("typeExtension", typeExtension);
            tokenMessage.put("version", version == null ? "0.1" : version.replace(" ", "%20"));
            tokenMessage.put("listVersionCompatibles", listVersionCompatibles);
            tokenMessage.put("description", description == null ? "" : description.replace(" ", "%20"));
            tokenMessage.put("agreement", agreement);
            JSONObject token = new us.monoid.json.JSONObject();
            token.put("revision", tokenMessage);

            String u = exchangeWSServer + "publishExtension.php?data=" + token;
            JSONObject resultObj = readJsonFromUrl(u);
            //
            if (resultObj != null) {
                Object object = resultObj.get("result");
                if (object != null && object.equals("UPDATE OK")) {
                    ws.setResult(true);
                    ws.setMessageException(Messages.getString("ExchangeWebService.updateRevisionSuccessful")); //$NON-NLS-1$
                } else {
                    ws.setMessageException(object.toString());
                }
            }
        } catch (JSONException e) {
            ws.setMessageException(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            ws.setMessageException(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            ws.setMessageException(e.getMessage());
        }
        return ws;
    }

    /**
     *
     * DOC hcyi Comment method "insertionExtensionService".
     *
     * @param typeExtension
     * @param username
     * @param passwordHash
     * @param category
     * @param name
     * @param description
     * @return
     */
    public static WebserviceStatus insertionExtensionService(String typeExtension, String username, String passwordHash,
            String category, String name, String description) {
        WebserviceStatus ws = new WebserviceStatus();
        ws.setResult(false);
        Resty r = new Resty();
        JSONObject tokenMessage = new JSONObject();
        try {
            tokenMessage.put("username", username);
            tokenMessage.put("passwordHash", passwordHash);
            tokenMessage.put("typeExtension", typeExtension);
            tokenMessage.put("category", category);
            tokenMessage.put("name", name);
            tokenMessage.put("description", description);
            JSONObject token = new us.monoid.json.JSONObject();
            token.put("newExtension", tokenMessage);

            AbstractContent ac = Resty.content(token);
            MultipartContent mpc = Resty.form(new FormData("data", ac));
            TextResource textResult = r.text(exchangeWSServer + "publishExtension.php", mpc);
            JSONObject resultObject = new JSONObject(textResult.toString());
            JSONObject object = (JSONObject) resultObject.get("resultNewExtension");
            String idExtension = object.getString("idExtension");
            //
            ws.setValue(idExtension);
            ws.setResult(true);
            ws.setMessageException(Messages.getString("ExchangeWebService.insertionExtensionSuccessful")); //$NON-NLS-1$
        } catch (JSONException e) {
            ws.setMessageException(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            ws.setMessageException(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            ws.setMessageException(e.getMessage());
        }
        return ws;
    }

    public static JSONObject readJsonFromUrl(String url) throws IOException {
        InputStream is = new URL(url).openStream();
        String jsonText = "";
        JSONObject json = null;
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            jsonText = readAll(rd);
            json = new JSONObject(jsonText);
        } catch (Exception e) {
            System.out.println(jsonText);
        } finally {
            is.close();
        }
        return json;
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

}
