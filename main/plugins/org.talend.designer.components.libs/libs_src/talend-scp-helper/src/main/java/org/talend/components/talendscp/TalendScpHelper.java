package org.talend.components.talendscp;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystem;

import org.apache.sshd.common.scp.ScpException;
import org.apache.sshd.common.scp.ScpFileOpener;
import org.apache.sshd.common.scp.ScpHelper;
import org.apache.sshd.common.scp.ScpTransferEventListener;
import org.apache.sshd.common.session.Session;

public class TalendScpHelper extends ScpHelper {

    public TalendScpHelper(Session session, InputStream in, OutputStream out, FileSystem fileSystem,
            ScpFileOpener opener, ScpTransferEventListener eventListener) {
        super(session, in, out, fileSystem, opener, eventListener);
    }

    @Override
    public int readAck(boolean canEof) throws IOException {
        int c = in.read();
        switch (c) {
        case -1:
            if (log.isDebugEnabled()) {
                log.debug("readAck({})[EOF={}] received EOF", this, canEof);
            }
            if (!canEof) {
                throw new EOFException("readAck - EOF before ACK");
            }
            break;
        case OK:
            if (log.isDebugEnabled()) {
                log.debug("readAck({})[EOF={}] read OK", this, canEof);
            }
            break;
        case WARNING: {
            if (log.isDebugEnabled()) {
                log.debug("readAck({})[EOF={}] read warning message", this, canEof);
            }

            String line = readLine();
            log.warn("readAck({})[EOF={}] - Received warning: {}", this, canEof, line);
            throw new ScpException("received error: " + line, c);
        }
        case ERROR: {
            if (log.isDebugEnabled()) {
                log.debug("readAck({})[EOF={}] read error message", this, canEof);
            }
            String line = readLine();
            if (log.isDebugEnabled()) {
                log.debug("readAck({})[EOF={}] received error: {}", this, canEof, line);
            }
            throw new ScpException("Received nack: " + line, c);
        }
        default:
            break;
        }
        return c;
    }
}
