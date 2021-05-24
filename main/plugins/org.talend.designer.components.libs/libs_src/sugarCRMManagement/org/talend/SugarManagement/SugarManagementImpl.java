// ============================================================================
//
// Talend Community Edition
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//
// ============================================================================
package org.talend.SugarManagement;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.rpc.ServiceException;

import org.talend.sugarws.Entry_value;
import org.talend.sugarws.Field;
import org.talend.sugarws.Get_entry_list_result;
import org.talend.sugarws.Module_fields;
import org.talend.sugarws.Module_list;
import org.talend.sugarws.Name_value;
import org.talend.sugarws.Set_entry_result;
import org.talend.sugarws.SugarsoapLocator;
import org.talend.sugarws.SugarsoapPortType;
import org.talend.sugarws.User_auth;

/**
 * SugarManagementImpl Class Util class implementing all web services methods of SugarsoapPortType class <br/>
 *
 */
public class SugarManagementImpl implements SugarManagement {

    private String username;

    private String password;

    private String endPoint;

    private String applicationName;

    private String version;

    private SugarsoapPortType connection;

    private Set_entry_result loginInfo;

    public SugarManagementImpl(String username, String password, String endPoint, String applicationName, String version) {
        this.username = username;
        this.password = password;
        this.endPoint = endPoint;
        this.applicationName = applicationName;
        this.version = version;
    }

    public void login() throws ServiceException, MalformedURLException, RemoteException {

        User_auth USER_SOAP = new User_auth(username, encryptMd5(password), version);

        SugarsoapLocator locator = new SugarsoapLocator();

        connection = locator.getsugarsoapPort(new URL(endPoint));

        loginInfo = connection.login(USER_SOAP, applicationName);

    }

    public void logout() throws RemoteException {
        if (connection != null && loginInfo != null) {
            connection.logout(loginInfo.getId());
        }
    }

    public String[] getTables() throws RemoteException {

        Module_list modules_list = connection.get_available_modules(loginInfo.getId());
        return modules_list.getModules();

    }

    public Field[] getFields(String moduleName) throws RemoteException {

        Module_fields mf = connection.get_module_fields(loginInfo.getId(), moduleName);
        return mf.getModule_fields();
    }

    public Get_entry_list_result getRecords(String moduleName, int offset, int maxResults) throws RemoteException {

        return connection.get_entry_list(loginInfo.getId(), moduleName, "", "id asc", offset, null, maxResults, 0);
    }

    public Get_entry_list_result getRecordswithQuery(String moduleName, String query, String[] fileds, int offset,
            int maxResults) throws RemoteException {

        return connection.get_entry_list(loginInfo.getId(), moduleName, query, "id asc", offset, fileds, maxResults, 0);
    }

    /**
     * insert when arrNameValuesToEnter doesn't cotain the "id". update when arrNameValuesToEnter cotains the "id".
     */
    public String setRecord(String moduleName, Name_value[] arrNameValuesToEnter) throws RemoteException {

        Set_entry_result set_entry_result = connection.set_entry(loginInfo.getId(), moduleName, arrNameValuesToEnter);
        return set_entry_result.getId();
    }

    /**
     * insert when arrNameValuesToEnter doesn't cotain the "id".
     */
    public void insertRecord(String moduleName, Name_value[] arrNameValuesToEnter) throws RemoteException {

        setRecord(moduleName, arrNameValuesToEnter);
    }

    /**
     * update when arrNameValuesToEnter cotains the "id".
     */
    public void updateRecord(String moduleName, Name_value[] arrNameValuesToEnter) throws RemoteException {

        setRecord(moduleName, arrNameValuesToEnter);
    }

    /**
     * update the select records with query string.
     */
    public void updateRecordsWithQuery(String moduleName, String query, Name_value[] arrNameValuesToEnter)
            throws RemoteException {

        // String selectFields[] = new String[]{"id"};

        if(arrNameValuesToEnter == null || arrNameValuesToEnter.length == 0 ) return;

        Get_entry_list_result getEntryListResult = connection.get_entry_list(loginInfo.getId(), moduleName, query,
                "id asc", 0, null, 100, 0);

        // read the record from remote, one time 100 records.
        while (getEntryListResult.getResult_count() != 0) {
            Entry_value[] entry_value = getEntryListResult.getEntry_list();
            // get every record
            for (int i = 0; i < entry_value.length; i++) {
                Name_value[] nameValue = entry_value[i].getName_value_list();

                //avoid the "id" in "arrNameValuesToEnter" is different the "id" in "nameValue". So need a temp.
                Name_value[] temp = null;
                String id = getValuefromInput("id", arrNameValuesToEnter);
                if (id == null) {
                    temp = new Name_value[arrNameValuesToEnter.length + 1];
                }
                else
                {
                    temp = new Name_value[arrNameValuesToEnter.length];
                }

                temp[0] = new Name_value("id", getValuefromInput("id", nameValue));

                for (int j = 0, k = 1; j < arrNameValuesToEnter.length; j++)
                {
                    String name = arrNameValuesToEnter[j].getName();
                    if("id".endsWith(name)) continue;
                    temp[k] =  new Name_value(name, getValuefromInput(name, arrNameValuesToEnter));
                    k++;
                }

                // only update, not insert
                connection.set_entry(loginInfo.getId(), moduleName, temp);
            }

            getEntryListResult = connection.get_entry_list(loginInfo.getId(), moduleName, query, "id asc",
                    getEntryListResult.getNext_offset(), null, 100, 0);
        }
    }

    /**
     * update depend on the "id": if arrNameValuesToEnter contains "id", it will update the "query" records. if
     * arrNameValuesToEnter doesn't contain "id", it will insert.
     */
    public void insertOrUpdateById(String moduleName, Name_value[] arrNameValuesToEnter) throws RemoteException {
        String idValue = getValuefromInput("id", arrNameValuesToEnter);
        if (idValue == null)
            insertRecord(moduleName, arrNameValuesToEnter);
        else {
            StringBuffer queryStr = new StringBuffer();
            queryStr.append("id='");
            queryStr.append(idValue);
            queryStr.append("'");
            updateRecordsWithQuery(moduleName, queryStr.toString(), arrNameValuesToEnter);
        }
    }

    private String getValuefromInput(String name, Name_value[] arrNameValuesToEnter) {
        // get the "id"
        for (int j = 0; j < arrNameValuesToEnter.length; j++) {

            if (name.endsWith(arrNameValuesToEnter[j].getName())) {
                return arrNameValuesToEnter[j].getValue();
            }

        }

        return null;
    }

    private String encryptMd5(String input) {

        try {
            StringBuffer sb = new StringBuffer();
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte buf[] = input.getBytes();
            byte[] md5 = md.digest(buf);

            for (int i = 0; i < md5.length; i++) {
                String tmpStr = "0" + Integer.toHexString((0xff & md5[i]));
                sb.append(tmpStr.substring(tmpStr.length() - 2));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        }
        return input;
    }
}
