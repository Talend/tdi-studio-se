package org.talend.ms.crm.sdk;

import java.rmi.RemoteException;

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
import com.microsoft.schemas.xrm._2011.contracts.services.DisassociateDocument;
import com.microsoft.schemas.xrm._2011.contracts.services.DisassociateResponseDocument;
import com.microsoft.schemas.xrm._2011.contracts.services.ExecuteDocument;
import com.microsoft.schemas.xrm._2011.contracts.services.ExecuteResponseDocument;
import com.microsoft.schemas.xrm._2011.contracts.services.RetrieveDocument;
import com.microsoft.schemas.xrm._2011.contracts.services.RetrieveMultipleDocument;
import com.microsoft.schemas.xrm._2011.contracts.services.RetrieveMultipleResponseDocument;
import com.microsoft.schemas.xrm._2011.contracts.services.RetrieveResponseDocument;
import com.microsoft.schemas.xrm._2011.contracts.services.UpdateDocument;
import com.microsoft.schemas.xrm._2011.contracts.services.UpdateResponseDocument;

public class OperationProcessorsFactory {

    static class DisassociateOperationProcessor extends
            OperationProcessor<IOrganizationService_Disassociate_OrganizationServiceFaultFault_FaultMessage, DisassociateResponseDocument, DisassociateDocument> {

        public DisassociateOperationProcessor(DisassociateDocument requestDocument) {
            super(requestDocument);
        }

        public DisassociateResponseDocument processOperation(OrganizationServiceStub orgStub)
                throws RemoteException, IOrganizationService_Disassociate_OrganizationServiceFaultFault_FaultMessage {
            return orgStub.disassociate(getRequestDocument());
        }

    }

    static class RetrieveMultipleOperationProcessor extends
            OperationProcessor<IOrganizationService_RetrieveMultiple_OrganizationServiceFaultFault_FaultMessage, RetrieveMultipleResponseDocument, RetrieveMultipleDocument> {

        public RetrieveMultipleOperationProcessor(RetrieveMultipleDocument requestDocument) {
            super(requestDocument);
        }

        public RetrieveMultipleResponseDocument processOperation(OrganizationServiceStub orgStub)
                throws RemoteException, IOrganizationService_RetrieveMultiple_OrganizationServiceFaultFault_FaultMessage {
            return orgStub.retrieveMultiple(getRequestDocument());
        }

    }

    static class UpdateOperationProcessor extends 
            OperationProcessor<IOrganizationService_Update_OrganizationServiceFaultFault_FaultMessage, UpdateResponseDocument, UpdateDocument> {

        public UpdateOperationProcessor(UpdateDocument requestDocument) {
            super(requestDocument);
        }

        public UpdateResponseDocument processOperation(OrganizationServiceStub orgStub)
                throws RemoteException, IOrganizationService_Update_OrganizationServiceFaultFault_FaultMessage {
            return orgStub.update(getRequestDocument());
        }

    };

    static class AssociateOperationProcessor extends 
            OperationProcessor<IOrganizationService_Associate_OrganizationServiceFaultFault_FaultMessage, AssociateResponseDocument, AssociateDocument> {

        public AssociateOperationProcessor(AssociateDocument requestDocument) {
            super(requestDocument);
        }

        public AssociateResponseDocument processOperation(OrganizationServiceStub orgStub)
                throws RemoteException, IOrganizationService_Associate_OrganizationServiceFaultFault_FaultMessage {
            return orgStub.associate(getRequestDocument());
        }

    }

    static class RetrieveOperationProcessor extends
            OperationProcessor<IOrganizationService_Retrieve_OrganizationServiceFaultFault_FaultMessage, RetrieveResponseDocument, RetrieveDocument> {

        public RetrieveOperationProcessor(RetrieveDocument requestDocument) {
            super(requestDocument);
        }

        public RetrieveResponseDocument processOperation(OrganizationServiceStub orgStub)
                throws RemoteException, IOrganizationService_Retrieve_OrganizationServiceFaultFault_FaultMessage {
            return orgStub.retrieve(getRequestDocument());
        }

    }

    static class DeleteOperationProcessor extends
            OperationProcessor<IOrganizationService_Delete_OrganizationServiceFaultFault_FaultMessage, DeleteResponseDocument, DeleteDocument> {

        public DeleteOperationProcessor(DeleteDocument requestDocument) {
            super(requestDocument);
        }

        public DeleteResponseDocument processOperation(OrganizationServiceStub orgStub)
                throws RemoteException, IOrganizationService_Delete_OrganizationServiceFaultFault_FaultMessage {
            return orgStub.delete(getRequestDocument());
        }

    }

    static class CreateOperationProcessor extends
            OperationProcessor<IOrganizationService_Create_OrganizationServiceFaultFault_FaultMessage, CreateResponseDocument, CreateDocument> {

        public CreateOperationProcessor(CreateDocument requestDocument) {
            super(requestDocument);
        }

        public CreateResponseDocument processOperation(OrganizationServiceStub orgStub)
                throws RemoteException, IOrganizationService_Create_OrganizationServiceFaultFault_FaultMessage {
            return orgStub.create(getRequestDocument());
        }

    }

    static class ExecuteOperationProcessor extends
            OperationProcessor<IOrganizationService_Execute_OrganizationServiceFaultFault_FaultMessage, ExecuteResponseDocument, ExecuteDocument> {

        public ExecuteOperationProcessor(ExecuteDocument requestDocument) {
            super(requestDocument);
        }

        public ExecuteResponseDocument processOperation(OrganizationServiceStub orgStub)
                throws RemoteException, IOrganizationService_Execute_OrganizationServiceFaultFault_FaultMessage {
            return orgStub.execute(getRequestDocument());
        }

    }

    public static DeleteOperationProcessor createDeleteProcessor(DeleteDocument requestDoc) {
        return new DeleteOperationProcessor(requestDoc);
    }

    public static DisassociateOperationProcessor createDisassociateProcessor(DisassociateDocument requestDoc) {
        return new DisassociateOperationProcessor(requestDoc);
    }

    public static RetrieveMultipleOperationProcessor createRetrieveMultipleProcessor(RetrieveMultipleDocument requestDoc) {
        return new RetrieveMultipleOperationProcessor(requestDoc);
    }

    public static UpdateOperationProcessor createUpdateProcessor(UpdateDocument requestDoc) {
        return new UpdateOperationProcessor(requestDoc);
    }

    public static AssociateOperationProcessor createAssociateProcessor(AssociateDocument requestDoc) {
        return new AssociateOperationProcessor(requestDoc);
    }

    public static RetrieveOperationProcessor createRetrieveProcessor(RetrieveDocument requestDoc) {
        return new RetrieveOperationProcessor(requestDoc);
    }

    public static CreateOperationProcessor createCreateProcessor(CreateDocument requestDoc) {
        return new CreateOperationProcessor(requestDoc);
    }

    public static ExecuteOperationProcessor createExecuteProcessor(ExecuteDocument requestDoc) {
        return new ExecuteOperationProcessor(requestDoc);
    }

}
