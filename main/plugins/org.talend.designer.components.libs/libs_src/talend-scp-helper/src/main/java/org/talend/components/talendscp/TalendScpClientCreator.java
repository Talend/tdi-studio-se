package org.talend.components.talendscp;

import org.apache.sshd.client.scp.AbstractScpClientCreator;
import org.apache.sshd.client.scp.ScpClient;
import org.apache.sshd.client.scp.ScpClientCreator;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.scp.ScpFileOpener;
import org.apache.sshd.common.scp.ScpTransferEventListener;

public class TalendScpClientCreator extends AbstractScpClientCreator implements ScpClientCreator {
    public static final TalendScpClientCreator INSTANCE = new TalendScpClientCreator();

    @Override
    public ScpClient createScpClient(ClientSession session) {
        return this.createScpClient(session, this.getScpFileOpener(), this.getScpTransferEventListener());
    }

    @Override public ScpClient createScpClient(ClientSession clientSession, ScpFileOpener scpFileOpener,
            ScpTransferEventListener scpTransferEventListener) {
        return new TalendScpClient(clientSession, scpFileOpener, scpTransferEventListener);
    }
}
