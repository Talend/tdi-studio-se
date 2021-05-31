package org.talend.components.talendscp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;

import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.client.scp.AbstractScpClient;
import org.apache.sshd.client.scp.DefaultScpClient;
import org.apache.sshd.client.scp.DefaultScpStreamResolver;
import org.apache.sshd.client.scp.ScpClient;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.FactoryManager;
import org.apache.sshd.common.file.FileSystemFactory;
import org.apache.sshd.common.file.util.MockFileSystem;
import org.apache.sshd.common.file.util.MockPath;
import org.apache.sshd.common.scp.ScpFileOpener;
import org.apache.sshd.common.scp.ScpHelper;
import org.apache.sshd.common.scp.ScpTimestamp;
import org.apache.sshd.common.scp.ScpTransferEventListener;
import org.apache.sshd.common.util.ValidateUtils;


/*
What we do here is just overwrite the ScpHelper to throw Exception when scp return status 1
to keep the old behavior
 */

public class TalendScpClient extends DefaultScpClient {

    public TalendScpClient(ClientSession clientSession, ScpFileOpener fileOpener,
            ScpTransferEventListener eventListener) {
        super(clientSession, fileOpener, eventListener);
    }

    @Override public void upload(InputStream local, String remote, long size, Collection<PosixFilePermission> perms,
            ScpTimestamp time) throws IOException {
        int namePos = ValidateUtils.checkNotNullAndNotEmpty(remote, "No remote location specified").lastIndexOf('/');
        String name = (namePos < 0) ?
                remote :
                ValidateUtils.checkNotNullAndNotEmpty(remote.substring(namePos + 1), "No name value in remote=%s",
                        remote);
        Collection<Option> options = (time != null) ? EnumSet.of(Option.PreserveAttributes) : Collections.emptySet();
        String cmd = ScpClient.createSendCommand(remote, options);
        ClientSession session = getClientSession();
        ChannelExec channel = openCommandChannel(session, cmd);
        try (InputStream invOut = channel.getInvertedOut(); OutputStream invIn = channel.getInvertedIn()) {
            // NOTE: we use a mock file system since we expect no invocations for it
            ScpHelper helper = new TalendScpHelper(session, invOut, invIn, new MockFileSystem(remote), opener, listener);
            Path mockPath = new MockPath(remote);
            helper.sendStream(new DefaultScpStreamResolver(name, mockPath, perms, time, size, local, cmd),
                    options.contains(Option.PreserveAttributes), ScpHelper.DEFAULT_SEND_BUFFER_SIZE);
            handleCommandExitStatus(cmd, channel);
        } finally {
            channel.close(false);
        }
    }

    @Override protected <T> void runUpload(String remote, Collection<Option> options, Collection<T> local,
            AbstractScpClient.ScpOperationExecutor<T> executor) throws IOException {
        local = ValidateUtils.checkNotNullAndNotEmpty(local, "Invalid argument local: %s", local);
        remote = ValidateUtils.checkNotNullAndNotEmpty(remote, "Invalid argument remote: %s", remote);
        if (local.size() > 1) {
            options = addTargetIsDirectory(options);
        }

        String cmd = ScpClient.createSendCommand(remote, options);
        ClientSession session = getClientSession();
        ChannelExec channel = openCommandChannel(session, cmd);
        try {
            FactoryManager manager = session.getFactoryManager();
            FileSystemFactory factory = manager.getFileSystemFactory();
            FileSystem fs = factory.createFileSystem(session);

            try (InputStream invOut = channel.getInvertedOut(); OutputStream invIn = channel.getInvertedIn()) {
                ScpHelper helper = new TalendScpHelper(session, invOut, invIn, fs, opener, listener);
                executor.execute(helper, local, options);
            } finally {
                try {
                    fs.close();
                } catch (UnsupportedOperationException e) {
                    if (log.isDebugEnabled()) {
                        log.debug("runUpload({}) {} => {} - failed ({}) to close file system={}: {}", session, remote,
                                local, e.getClass().getSimpleName(), fs, e.getMessage());
                    }
                }
            }
            handleCommandExitStatus(cmd, channel);
        } finally {
            channel.close(false);
        }
    }

    @Override public void download(String remote, OutputStream local) throws IOException {
        String cmd = ScpClient.createReceiveCommand(remote, Collections.emptyList());
        ClientSession session = getClientSession();
        ChannelExec channel = openCommandChannel(session, cmd);
        try (InputStream invOut = channel.getInvertedOut(); OutputStream invIn = channel.getInvertedIn()) {
            // NOTE: we use a mock file system since we expect no invocations for it
            ScpHelper helper =
                    new TalendScpHelper(session, invOut, invIn, new MockFileSystem(remote), opener, listener);
            helper.receiveFileStream(local, ScpHelper.DEFAULT_RECEIVE_BUFFER_SIZE);
            handleCommandExitStatus(cmd, channel);
        } finally {

        }
    }

}
