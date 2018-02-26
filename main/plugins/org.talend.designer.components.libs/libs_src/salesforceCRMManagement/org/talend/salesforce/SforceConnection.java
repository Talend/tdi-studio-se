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
package org.talend.salesforce;

import com.salesforce.soap.partner.CallOptions;
import com.salesforce.soap.partner.Create;
import com.salesforce.soap.partner.CreateResponse;
import com.salesforce.soap.partner.Delete;
import com.salesforce.soap.partner.DeleteResponse;
import com.salesforce.soap.partner.DescribeGlobal;
import com.salesforce.soap.partner.DescribeGlobalResponse;
import com.salesforce.soap.partner.DescribeSObject;
import com.salesforce.soap.partner.DescribeSObjectResponse;
import com.salesforce.soap.partner.DescribeSObjects;
import com.salesforce.soap.partner.DescribeSObjectsResponse;
import com.salesforce.soap.partner.GetDeleted;
import com.salesforce.soap.partner.GetDeletedResponse;
import com.salesforce.soap.partner.GetServerTimestamp;
import com.salesforce.soap.partner.GetServerTimestampResponse;
import com.salesforce.soap.partner.GetUpdated;
import com.salesforce.soap.partner.GetUpdatedResponse;
import com.salesforce.soap.partner.Query;
import com.salesforce.soap.partner.QueryAll;
import com.salesforce.soap.partner.QueryAllResponse;
import com.salesforce.soap.partner.QueryMore;
import com.salesforce.soap.partner.QueryMoreResponse;
import com.salesforce.soap.partner.QueryOptions;
import com.salesforce.soap.partner.QueryResponse;
import com.salesforce.soap.partner.Retrieve;
import com.salesforce.soap.partner.RetrieveResponse;
import com.salesforce.soap.partner.SessionHeader;
import com.salesforce.soap.partner.SforceServiceStub;
import com.salesforce.soap.partner.Update;
import com.salesforce.soap.partner.UpdateResponse;
import com.salesforce.soap.partner.Upsert;
import com.salesforce.soap.partner.UpsertResponse;
import com.salesforce.soap.partner.fault.ExceptionCode;

/**
 * created by bchen on Jul 9, 2014 Detailled comment
 * 
 */
public abstract class SforceConnection {

    protected SforceServiceStub stub;

    protected SessionHeader sh;
    
    public SessionHeader getSessionHeader(){
    	return sh;
    }

    protected CallOptions co;

    protected void renewSession() throws Exception {
        throw new Exception("Session expire, need to renew session");
    }

    protected DeleteResponse delete(Delete delete) throws Exception {
        try {
            return stub.delete(delete, sh, co, null, null, null, null, null, null, null, null, null, null);
        } catch (com.salesforce.soap.partner.UnexpectedErrorFault sfException) {
            if (ExceptionCode.INVALID_SESSION_ID.equals(sfException.getFaultMessage().getUnexpectedErrorFault()
                    .getExceptionCode())) {
                renewSession();
                return delete(delete);
            }
            throw sfException;
        }
    }

    protected CreateResponse create(Create create) throws Exception {
        try {
            return stub.create(create, sh, co, null, null, null, null, null, null, null, null, null, null, null);
        } catch (com.salesforce.soap.partner.UnexpectedErrorFault sfException) {
            if (ExceptionCode.INVALID_SESSION_ID.equals(sfException.getFaultMessage().getUnexpectedErrorFault()
                    .getExceptionCode())) {
                renewSession();
                return create(create);
            }
            throw sfException;
        }
    }

    protected UpdateResponse update(Update update) throws Exception {
        try {
            return stub.update(update, sh, co, null, null, null, null, null, null, null, null, null, null, null, null);
        } catch (com.salesforce.soap.partner.UnexpectedErrorFault sfException) {
            if (ExceptionCode.INVALID_SESSION_ID.equals(sfException.getFaultMessage().getUnexpectedErrorFault()
                    .getExceptionCode())) {
                renewSession();
                return update(update);
            }
            throw sfException;
        }
    }

    protected UpsertResponse upsert(Upsert upsert) throws Exception {
        try {
            return stub.upsert(upsert, sh, co, null, null, null, null, null, null, null, null, null, null, null, null);
        } catch (com.salesforce.soap.partner.UnexpectedErrorFault sfException) {
            if (ExceptionCode.INVALID_SESSION_ID.equals(sfException.getFaultMessage().getUnexpectedErrorFault()
                    .getExceptionCode())) {
                renewSession();
                return upsert(upsert);
            }
            throw sfException;
        }
    }

    protected GetServerTimestampResponse getServerTimestamp(GetServerTimestamp getServerTimestamp) throws Exception {
        try {
            return stub.getServerTimestamp(getServerTimestamp, sh, co);
        } catch (com.salesforce.soap.partner.UnexpectedErrorFault sfException) {
            if (ExceptionCode.INVALID_SESSION_ID.equals(sfException.getFaultMessage().getUnexpectedErrorFault()
                    .getExceptionCode())) {
                renewSession();
                return getServerTimestamp(getServerTimestamp);
            }
            throw sfException;
        }
    }

    protected GetUpdatedResponse getUpdated(GetUpdated getUpdated) throws Exception {
        try {
            return stub.getUpdated(getUpdated, sh, co);
        } catch (com.salesforce.soap.partner.UnexpectedErrorFault sfException) {
            if (ExceptionCode.INVALID_SESSION_ID.equals(sfException.getFaultMessage().getUnexpectedErrorFault()
                    .getExceptionCode())) {
                renewSession();
                return getUpdated(getUpdated);
            }
            throw sfException;
        }
    }

    protected RetrieveResponse retrieve(Retrieve retrieve) throws Exception {
        try {
            return stub.retrieve(retrieve, sh, co, null, null, null);
        } catch (com.salesforce.soap.partner.UnexpectedErrorFault sfException) {
            if (ExceptionCode.INVALID_SESSION_ID.equals(sfException.getFaultMessage().getUnexpectedErrorFault()
                    .getExceptionCode())) {
                renewSession();
                return retrieve(retrieve);
            }
            throw sfException;
        }
    }

    protected GetDeletedResponse getDeleted(GetDeleted getDeleted) throws Exception {
        try {
            return stub.getDeleted(getDeleted, sh, co);
        } catch (com.salesforce.soap.partner.UnexpectedErrorFault sfException) {
            if (ExceptionCode.INVALID_SESSION_ID.equals(sfException.getFaultMessage().getUnexpectedErrorFault()
                    .getExceptionCode())) {
                renewSession();
                return getDeleted(getDeleted);
            }
            throw sfException;
        }
    }

    protected QueryAllResponse queryAll(QueryAll queryAll, QueryOptions queryOptions) throws Exception {
        try {
            return stub.queryAll(queryAll, sh, co, queryOptions);
        } catch (com.salesforce.soap.partner.UnexpectedErrorFault sfException) {
            if (ExceptionCode.INVALID_SESSION_ID.equals(sfException.getFaultMessage().getUnexpectedErrorFault()
                    .getExceptionCode())) {
                renewSession();
                return queryAll(queryAll, queryOptions);
            }
            throw sfException;
        }
    }

    protected QueryMoreResponse queryMore(QueryMore queryMore, QueryOptions queryOptions) throws Exception {
        try {
            return stub.queryMore(queryMore, sh, co, queryOptions);
        } catch (com.salesforce.soap.partner.UnexpectedErrorFault sfException) {
            if (ExceptionCode.INVALID_SESSION_ID.equals(sfException.getFaultMessage().getUnexpectedErrorFault()
                    .getExceptionCode())) {
                renewSession();
                return queryMore(queryMore, queryOptions);
            }
            throw sfException;
        }
    }

    protected QueryResponse query(Query query, QueryOptions queryOptions) throws Exception {
        try {
            return stub.query(query, sh, co, queryOptions, null, null);
        } catch (com.salesforce.soap.partner.UnexpectedErrorFault sfException) {
            if (ExceptionCode.INVALID_SESSION_ID.equals(sfException.getFaultMessage().getUnexpectedErrorFault()
                    .getExceptionCode())) {
                renewSession();
                return query(query, queryOptions);
            }
            throw sfException;
        }
    }

    protected DescribeSObjectResponse describeSObject(DescribeSObject describeSObject) throws Exception {
        try {
            return stub.describeSObject(describeSObject, sh, co, null, null);
        } catch (com.salesforce.soap.partner.UnexpectedErrorFault sfException) {
            if (ExceptionCode.INVALID_SESSION_ID.equals(sfException.getFaultMessage().getUnexpectedErrorFault()
                    .getExceptionCode())) {
                renewSession();
                return describeSObject(describeSObject);
            }
            throw sfException;
        }
    }

    protected DescribeSObjectsResponse describeSObjects(DescribeSObjects describeSObjects) throws Exception {
        try {
            return stub.describeSObjects(describeSObjects, sh, co, null, null);
        } catch (com.salesforce.soap.partner.UnexpectedErrorFault sfException) {
            if (ExceptionCode.INVALID_SESSION_ID.equals(sfException.getFaultMessage().getUnexpectedErrorFault()
                    .getExceptionCode())) {
                renewSession();
                return describeSObjects(describeSObjects);
            }
            throw sfException;

        }
    }

    protected DescribeGlobalResponse describeGlobal(DescribeGlobal describeGlobal) throws Exception {
        try {
            return stub.describeGlobal(describeGlobal, sh, co, null);
        } catch (com.salesforce.soap.partner.UnexpectedErrorFault sfException) {
            if (ExceptionCode.INVALID_SESSION_ID.equals(sfException.getFaultMessage().getUnexpectedErrorFault()
                    .getExceptionCode())) {
                renewSession();
                return describeGlobal(describeGlobal);
            }
            throw sfException;

        }
    }
}
