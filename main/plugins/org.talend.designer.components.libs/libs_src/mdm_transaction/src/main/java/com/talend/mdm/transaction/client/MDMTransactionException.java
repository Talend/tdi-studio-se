package com.talend.mdm.transaction.client;

/**
 * 
 * throw it if server return >=400 http code when commit or rollback fail
 *
 */
public class MDMTransactionException extends RuntimeException {
    
    public MDMTransactionException() {
        super();
    }

    public MDMTransactionException(String message, Throwable cause) {
        super(message, cause);

    }

    public MDMTransactionException(String message) {
        super(message);
    }

    public MDMTransactionException(Throwable cause) {
        super(cause);
    }

}
