/**
 * TalendJob.java
 * 
 * This file was auto-generated from WSDL by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.talend;

public interface TalendJob extends java.rmi.Remote {

    public java.lang.String[][] runJob(java.lang.String[] args) throws java.rmi.RemoteException;

    public void setClientConfig(TalendJobHTTPClientConfiguration clientConfig);
}
