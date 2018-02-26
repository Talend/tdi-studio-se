// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend;

import java.net.URL;
import java.util.List;

import com.salesforce.soap.partner.LoginResult;
import com.salesforce.soap.partner.QueryResult;
import com.salesforce.soap.partner.SessionHeader;
import com.salesforce.soap.partner.SforceServiceLocator;
import com.salesforce.soap.partner.SoapBindingStub;

public class Test4Feature8540 {

    SoapBindingStub binding = null;

    public static void main(String[] args) throws Exception {
        Test4Feature8540 worker = new Test4Feature8540();
        worker.doQuery();
    }

    public void doQuery() throws Exception {

        binding = (SoapBindingStub) new SforceServiceLocator()
                .getSoap(new URL("https://www.salesforce.com/services/Soap/u/16.0"));
        binding.setTimeout(60000);
        LoginResult loginResult = binding.login("musicatcher@gmail.com", "1234qwer");
        binding._setProperty(SoapBindingStub.ENDPOINT_ADDRESS_PROPERTY, loginResult.getServerUrl());

        SessionHeader sh = new SessionHeader();
        sh.setSessionId(loginResult.getSessionId());
        binding.setHeader(new SforceServiceLocator().getServiceName().getNamespaceURI(), "SessionHeader", sh);

        // com.sforce.soap.partner.QueryOptions qOptions = new com.sforce.soap.partner.QueryOptions();
        // qOptions.setBatchSize(new Integer(2));
        // binding.setHeader(
        // new com.sforce.soap.partner.SforceServiceLocator()
        // .getServiceName().getNamespaceURI(),
        // "QueryOptions", qOptions);

        QueryResult qr = binding
                .query("SELECT Name, Type, Phone, Account.CreatedBy.CreatedBy.CreatedBy.Email, Account.Owner.city, (SELECT Contact.LastName,  Contact.FirstName "
                        + "FROM Account.Contacts Order By Contact.LastName), (SELECT Note.Title FROM Account.Notes), Account.Owner.Country  "
                        + "FROM Account WHERE Name !='United Oil & Gas Corp.'");
        // QueryResult qr = binding
        // .query("select Name,Id,Type from Account");

        TopQueryResult topqr = new TopQueryResult();
        topqr.processTopQueryResult(qr);

        topqr.printResult();

        List<TopRecord> allTopRecords = topqr.getAllTopRecords();

    }
}
