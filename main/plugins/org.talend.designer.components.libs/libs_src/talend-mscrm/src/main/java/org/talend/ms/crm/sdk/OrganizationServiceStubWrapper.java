package org.talend.ms.crm.sdk;

import java.rmi.RemoteException;

import org.apache.axis2.AxisFault;
import org.apache.xmlbeans.XmlObject;
import org.talend.ms.crm.MSCRMClient;

import com.microsoft.schemas.xrm._2011.contracts.IOrganizationService_Associate_OrganizationServiceFaultFault_FaultMessage;
import com.microsoft.schemas.xrm._2011.contracts.IOrganizationService_Create_OrganizationServiceFaultFault_FaultMessage;
import com.microsoft.schemas.xrm._2011.contracts.IOrganizationService_Delete_OrganizationServiceFaultFault_FaultMessage;
import com.microsoft.schemas.xrm._2011.contracts.IOrganizationService_Disassociate_OrganizationServiceFaultFault_FaultMessage;
import com.microsoft.schemas.xrm._2011.contracts.IOrganizationService_Execute_OrganizationServiceFaultFault_FaultMessage;
import com.microsoft.schemas.xrm._2011.contracts.IOrganizationService_RetrieveMultiple_OrganizationServiceFaultFault_FaultMessage;
import com.microsoft.schemas.xrm._2011.contracts.IOrganizationService_Retrieve_OrganizationServiceFaultFault_FaultMessage;
import com.microsoft.schemas.xrm._2011.contracts.IOrganizationService_Update_OrganizationServiceFaultFault_FaultMessage;
import com.microsoft.schemas.xrm._2011.contracts.OrganizationServiceStub;
import com.microsoft.schemas.xrm._2011.contracts.services.AssociateDocument;
import com.microsoft.schemas.xrm._2011.contracts.services.AssociateResponseDocument;
import com.microsoft.schemas.xrm._2011.contracts.services.CreateDocument;
import com.microsoft.schemas.xrm._2011.contracts.services.CreateResponseDocument;
import com.microsoft.schemas.xrm._2011.contracts.services.DeleteDocument;
import com.microsoft.schemas.xrm._2011.contracts.services.DeleteResponseDocument;
import com.microsoft.schemas.xrm._2011.contracts.services.ExecuteDocument;
import com.microsoft.schemas.xrm._2011.contracts.services.ExecuteResponseDocument;
import com.microsoft.schemas.xrm._2011.contracts.services.RetrieveDocument;
import com.microsoft.schemas.xrm._2011.contracts.services.RetrieveMultipleDocument;
import com.microsoft.schemas.xrm._2011.contracts.services.RetrieveMultipleResponseDocument;
import com.microsoft.schemas.xrm._2011.contracts.services.RetrieveResponseDocument;
import com.microsoft.schemas.xrm._2011.contracts.services.UpdateDocument;
import com.microsoft.schemas.xrm._2011.contracts.services.UpdateResponseDocument;

/**
 * Wrapper created for automatic token renewal. It creates new OrganizationServiceStub from scratch to renew security
 * tokens.
 */
public class OrganizationServiceStubWrapper extends OrganizationServiceStub {

    private OrganizationServiceStub orgStub;

    private final MSCRMClient client;

    private final String discoveryServiceUrl;

    private final int maxConnectionRetries;

    private final int attemptsInterval;

    public OrganizationServiceStubWrapper(OrganizationServiceStub orgStub, MSCRMClient client, String discoveryServiceUrl,
            int maxConnectionRetries, int attemptsInterval)
            throws AxisFault {
        this.orgStub = orgStub;
        this.client = client;
        this.discoveryServiceUrl = discoveryServiceUrl;
        this.maxConnectionRetries = maxConnectionRetries;
        this.attemptsInterval = attemptsInterval;
    }

    public com.microsoft.schemas.xrm._2011.contracts.services.DisassociateResponseDocument disassociate(
            final com.microsoft.schemas.xrm._2011.contracts.services.DisassociateDocument disassociate)
            throws java.rmi.RemoteException,
            IOrganizationService_Disassociate_OrganizationServiceFaultFault_FaultMessage {
        return perform(OperationProcessorsFactory.createDisassociateProcessor(disassociate));
    }

    public RetrieveMultipleResponseDocument retrieveMultiple(final RetrieveMultipleDocument retrieveMultiple)
            throws java.rmi.RemoteException,
            IOrganizationService_RetrieveMultiple_OrganizationServiceFaultFault_FaultMessage {
        return perform(OperationProcessorsFactory.createRetrieveMultipleProcessor(retrieveMultiple));
    }

    public UpdateResponseDocument update(final UpdateDocument update)
            throws RemoteException, IOrganizationService_Update_OrganizationServiceFaultFault_FaultMessage {
        return perform(OperationProcessorsFactory.createUpdateProcessor(update));
    }

    public AssociateResponseDocument associate(final AssociateDocument associate)
            throws RemoteException, IOrganizationService_Associate_OrganizationServiceFaultFault_FaultMessage {
        return perform(OperationProcessorsFactory.createAssociateProcessor(associate));
    }

    public RetrieveResponseDocument retrieve(final RetrieveDocument retrieve)
            throws RemoteException, IOrganizationService_Retrieve_OrganizationServiceFaultFault_FaultMessage {
        return perform(OperationProcessorsFactory.createRetrieveProcessor(retrieve));
    }

    public DeleteResponseDocument delete(final DeleteDocument delete)
            throws RemoteException, IOrganizationService_Delete_OrganizationServiceFaultFault_FaultMessage {
        return perform(OperationProcessorsFactory.createDeleteProcessor(delete));
    }

    public CreateResponseDocument create(final CreateDocument create)
            throws RemoteException, IOrganizationService_Create_OrganizationServiceFaultFault_FaultMessage {
        return perform(OperationProcessorsFactory.createCreateProcessor(create));
    }

    public ExecuteResponseDocument execute(final ExecuteDocument execute)
            throws RemoteException, IOrganizationService_Execute_OrganizationServiceFaultFault_FaultMessage {
        return perform(OperationProcessorsFactory.createExecuteProcessor(execute));
    }

    private void renewToken() throws Exception {
        orgStub = client.doGetOnlineConnection(discoveryServiceUrl);
    }

    private void sleep() {
        try {
            Thread.sleep(attemptsInterval);
        } catch (InterruptedException e1) {
        }
    }

    private <T extends Exception, V extends XmlObject, R extends XmlObject> V perform(OperationProcessor<T, V, R> processor)
            throws RemoteException, T {
        V respDoc = null;
        boolean connectionFixed = true;
        int retries = maxConnectionRetries;
        while (connectionFixed) {
            try {
                respDoc = processor.processOperation(orgStub);
                break;
            } catch (AxisFault af) {
                connectionFixed = false;
                while (retries-- > 0) {
                    try {
                        renewToken();
                        connectionFixed = true;
                        break;
                    } catch (Exception e) {
                        if (retries > 0) {
                            sleep();
                        }
                    }
                }
                if (!connectionFixed) {
                    throw af;
                }
            }
        }
        return respDoc;
    }

}
