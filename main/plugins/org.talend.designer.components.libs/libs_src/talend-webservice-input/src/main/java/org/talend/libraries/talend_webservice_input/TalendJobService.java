/**
 * TalendJobService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.talend;

public interface TalendJobService extends javax.xml.rpc.Service {
    public java.lang.String getTalendJobAddress();

    public org.talend.TalendJob getTalendJob() throws javax.xml.rpc.ServiceException;

    public org.talend.TalendJob getTalendJob(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
