package org.talend;

public class TalendJobProxy implements org.talend.TalendJob {

    private String _endpoint = null;

    private org.talend.TalendJob talendJob = null;

    private TalendJobHTTPClientConfiguration clientConfig = null;

    public TalendJobProxy() {
        _initTalendJobProxy();
    }

    public TalendJobProxy(String endpoint) {
        _endpoint = endpoint;
        _initTalendJobProxy();
    }

    public void setClientConfig(TalendJobHTTPClientConfiguration clientConfig) {
        this.clientConfig = clientConfig;
    }

    private void _initTalendJobProxy() {
        try {
            talendJob = (new org.talend.TalendJobServiceLocator()).getTalendJob();
            if (talendJob != null) {
                if (_endpoint != null)
                    ((javax.xml.rpc.Stub) talendJob)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
                else
                    _endpoint = (String) ((javax.xml.rpc.Stub) talendJob)._getProperty("javax.xml.rpc.service.endpoint.address");
            }

        } catch (javax.xml.rpc.ServiceException serviceException) {
        }
    }

    public String getEndpoint() {
        return _endpoint;
    }

    public void setEndpoint(String endpoint) {
        _endpoint = endpoint;
        if (talendJob != null)
            ((javax.xml.rpc.Stub) talendJob)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);

    }

    public org.talend.TalendJob getTalendJob() {
        if (talendJob == null)
            _initTalendJobProxy();
        return talendJob;
    }

    public java.lang.String[][] runJob(java.lang.String[] args) throws java.rmi.RemoteException {
        if (talendJob == null)
            _initTalendJobProxy();
        talendJob.setClientConfig(clientConfig);
        return talendJob.runJob(args);
    }

}
