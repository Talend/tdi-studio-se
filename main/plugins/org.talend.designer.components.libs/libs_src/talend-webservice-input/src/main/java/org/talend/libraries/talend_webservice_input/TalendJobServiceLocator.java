/**
 * TalendJobServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.talend;

public class TalendJobServiceLocator extends org.apache.axis.client.Service implements org.talend.TalendJobService {

    public TalendJobServiceLocator() {
    }


    public TalendJobServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public TalendJobServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for TalendJob
    private java.lang.String TalendJob_address = "http://localhost:8080/TalendJob";

    public java.lang.String getTalendJobAddress() {
        return TalendJob_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String TalendJobWSDDServiceName = "TalendJob";

    public java.lang.String getTalendJobWSDDServiceName() {
        return TalendJobWSDDServiceName;
    }

    public void setTalendJobWSDDServiceName(java.lang.String name) {
        TalendJobWSDDServiceName = name;
    }

    public org.talend.TalendJob getTalendJob() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(TalendJob_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getTalendJob(endpoint);
    }

    public org.talend.TalendJob getTalendJob(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            org.talend.TalendJobSoapBindingStub _stub = new org.talend.TalendJobSoapBindingStub(portAddress, this);
            _stub.setPortName(getTalendJobWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setTalendJobEndpointAddress(java.lang.String address) {
        TalendJob_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (org.talend.TalendJob.class.isAssignableFrom(serviceEndpointInterface)) {
                org.talend.TalendJobSoapBindingStub _stub = new org.talend.TalendJobSoapBindingStub(new java.net.URL(TalendJob_address), this);
                _stub.setPortName(getTalendJobWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("TalendJob".equals(inputPortName)) {
            return getTalendJob();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://talend.org", "TalendJobService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://talend.org", "TalendJob"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("TalendJob".equals(portName)) {
            setTalendJobEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
