package org.talend.ms.crm.sdk;

import org.apache.xmlbeans.XmlObject;

import com.microsoft.schemas.xrm._2011.contracts.OrganizationServiceStub;

abstract class OperationProcessor<T extends Exception, V extends XmlObject, R extends XmlObject> {

    private final R requestDocument;

    public OperationProcessor(R requestDocument) {
        this.requestDocument = requestDocument;
    }

    protected R getRequestDocument() {
        return requestDocument;
    }

    public abstract V processOperation(OrganizationServiceStub orgStub) throws java.rmi.RemoteException, T;


}