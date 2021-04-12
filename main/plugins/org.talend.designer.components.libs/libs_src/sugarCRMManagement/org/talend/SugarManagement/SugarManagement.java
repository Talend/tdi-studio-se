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
import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import org.talend.sugarws.Field;
import org.talend.sugarws.Get_entry_list_result;
import org.talend.sugarws.Name_value;

public interface SugarManagement {

    public void login() throws ServiceException, MalformedURLException, RemoteException;

    public void logout() throws RemoteException;

    public String[] getTables() throws RemoteException;

    public Field[] getFields(String moduleName) throws RemoteException;

    public Get_entry_list_result getRecords(String moduleName, int offset, int maxResults) throws RemoteException;

    public Get_entry_list_result getRecordswithQuery(String moduleName, String query, String[] fileds, int offset, int maxResults) throws RemoteException;

    public String setRecord(String moduleName, Name_value[] arrNameValuesToEnter) throws RemoteException;

    //public void insertRecord(String moduleName, Name_value[] arrNameValuesToEnter) throws RemoteException;

    //public void updateRecord(String moduleName, Name_value[] arrNameValuesToEnter) throws RemoteException;

    //public void updateRecordsWithQuery(String moduleName, String query, Name_value[] arrNameValuesToEnter) throws RemoteException;

    //public void insertOrUpdateById(String moduleName, Name_value[] arrNameValuesToEnter) throws RemoteException;
}
