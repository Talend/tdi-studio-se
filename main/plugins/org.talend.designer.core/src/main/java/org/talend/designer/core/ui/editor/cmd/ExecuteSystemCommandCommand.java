// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.ui.editor.cmd;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.swt.dialogs.ProgressDialog;
import org.talend.commons.ui.swt.formtools.Form;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.runprocess.IMsgType;
import org.talend.designer.runprocess.IProcessMessage;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class ExecuteSystemCommandCommand extends Command {

    private static final String RETURN = "\n"; //$NON-NLS-1$

    private List<String> commandsList;

    public ExecuteSystemCommandCommand(List<String> commandsList) {
        super();
        this.commandsList = commandsList;
    }

    @Override
    public void execute() {
        if (commandsList == null || commandsList.isEmpty()) {
            return;
        }
        List<IProcessMessage> consoleMessList = new ArrayList<IProcessMessage>();

        final Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();

        CommandProgressDialog progressDialog = new CommandProgressDialog(shell, commandsList, consoleMessList);

        try {
            progressDialog.executeProcess();
        } catch (InvocationTargetException e) {
            addCommandMessages(consoleMessList, CommandMsgType.CORE_ERR, e.toString());
        } catch (InterruptedException e) {
            addCommandMessages(consoleMessList, CommandMsgType.CORE_ERR, e.toString());
        }
        if (!consoleMessList.isEmpty()) {
            ShowCommandMessage show = new ShowCommandMessage(shell, progressDialog.getCommands(), consoleMessList);
            show.open();
        }
    }

    /**
     *
     * ggu CommandProgressDialog class global comment. Detailled comment
     */
    class CommandProgressDialog extends ProgressDialog {

        private final List<String> commansList;

        private StringBuffer commandsMessage = new StringBuffer();

        private Collection<IProcessMessage> messages;

        public CommandProgressDialog(Shell parentShell, final List<String> commansList, Collection<IProcessMessage> messages) {
            super(parentShell);
            this.commansList = commandsList;
            this.messages = messages;
        }

        @Override
        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
            if (commansList == null || commansList.isEmpty()) {
                return;
            }
            monitor.beginTask(Messages.getString("ExecuteSystemCommandCommand.Command"), commansList.size()); //$NON-NLS-1$

            Runtime runtime = Runtime.getRuntime();
            try {
                for (String command : commansList) {
                    monitor.worked(1);
                    commandsMessage.append(command);
                    commandsMessage.append(RETURN);
                    if ("".equals(command.trim())) { //$NON-NLS-1$
                        continue;
                    }
                    final Process process = runtime.exec(command);

                    StringBuffer errorMassage = new StringBuffer();
                    StringBuffer resultMassage = new StringBuffer();

                    createResultThread(process.getErrorStream(), errorMassage).start();
                    createResultThread(process.getInputStream(), resultMassage).start();

                    process.waitFor();

                    addCommandMessages(messages, CommandMsgType.STD_OUT, resultMassage.toString());
                    addCommandMessages(messages, CommandMsgType.STD_ERR, errorMassage.toString());
                }
            } catch (IOException e) {
                addCommandMessages(messages, CommandMsgType.CORE_ERR, e.toString());
            } catch (InterruptedException e) {
                addCommandMessages(messages, CommandMsgType.CORE_ERR, e.toString());
            }
            monitor.done();
        }

        public String getCommands() {
            return commandsMessage.toString();
        }

    }

    /**
     *
     * ggu ShowCommandMessage class global comment. Detailled comment
     */
    class ShowCommandMessage extends Dialog {

        private static final int MINIMUM_HEIGHT = 100;

        private static final int MINIMUM_WIDTH = 400;

        private static final int WIDTH = 480;

        private static final int HEIGHT = 370;

        private String commands;

        private Collection<IProcessMessage> messages;

        private StyledText consoleText;

        protected ShowCommandMessage(Shell parentShell, String commands, Collection<IProcessMessage> messages) {
            super(parentShell);
            this.commands = commands;
            this.messages = messages;
            // setDefaultImage(ImageProvider.getImage(ECoreImage.PROCESS_ICON));
            setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
            setBlockOnOpen(true);
        }

        @Override
        protected Control createDialogArea(Composite parent) {
            Composite composite = (Composite) super.createDialogArea(parent);
            composite.setFont(parent.getFont());
            GridData gridData;
            Composite inner = Form.startNewGridLayout(composite, 1);
            inner.setFont(composite.getFont());
            //
            Label label = new Label(inner, SWT.NONE);
            label.setText(Messages.getString("ExecuteSystemCommandCommand.Label")); //$NON-NLS-1$
            gridData = new GridData(GridData.FILL_HORIZONTAL);
            gridData.heightHint = 20;
            label.setLayoutData(gridData);

            Text cmdText = new Text(inner, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY);
            cmdText.setText(commands);
            gridData = new GridData(GridData.FILL_HORIZONTAL);
            gridData.heightHint = 50;
            cmdText.setLayoutData(gridData);

            CTabFolder messTabFolder = new CTabFolder(inner, SWT.NONE);
            gridData = new GridData(GridData.FILL_BOTH);
            gridData.heightHint = MINIMUM_HEIGHT + 50;
            gridData.widthHint = MINIMUM_WIDTH + 50;
            messTabFolder.setLayoutData(gridData);
            messTabFolder.setBackground(inner.getBackground());
            // message
            CTabItem normalTabItem = new CTabItem(messTabFolder, SWT.NONE);
            normalTabItem.setText(Messages.getString("ExecuteSystemCommandCommand.ConsoleMessages")); //$NON-NLS-1$

            consoleText = new StyledText(messTabFolder, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY);
            gridData = new GridData(GridData.FILL_BOTH);
            gridData.minimumHeight = MINIMUM_HEIGHT;
            gridData.minimumWidth = MINIMUM_WIDTH;
            consoleText.setLayoutData(gridData);
            Font font = new Font(parent.getDisplay(), "courier", 8, SWT.NONE); //$NON-NLS-1$
            consoleText.setFont(font);

            fillConsole(messages);

            normalTabItem.setControl(consoleText);
            messTabFolder.setSelection(normalTabItem);
            return composite;
        }

        @Override
        protected void configureShell(Shell newShell) {
            super.configureShell(newShell);
            newShell.setMinimumSize(WIDTH, HEIGHT);
            // newShell.setSize(WIDTH, HEIGHT);
            newShell.setImage(ImageProvider.getImage(ECoreImage.PROCESS_ICON));
            newShell.setText(Messages.getString("ExecuteSystemCommandCommand.Title")); //$NON-NLS-1$

        }

        @Override
        protected void createButtonsForButtonBar(Composite parent) {
            createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        }

        private Display getDisplay() {
            return Display.getDefault();
        }

        private void fillConsole(Collection<IProcessMessage> messages) {
            if (messages == null) {
                return;
            }
            consoleText.setText(""); //$NON-NLS-1$
            for (IProcessMessage message : messages) {
                doAppendToConsole(message);
            }
            consoleText.setFocus();
        }

        private void doAppendToConsole(final IProcessMessage message) {
            StyleRange style = new StyleRange();
            style.start = consoleText.getText().length();
            style.length = message.getContent().length();
            if (message.getType() == CommandMsgType.CORE_ERR) {
                style.fontStyle = SWT.ITALIC;
            }
            Color color;
            switch ((CommandMsgType) message.getType()) {
            case CORE_ERR:
                color = getDisplay().getSystemColor(SWT.COLOR_DARK_RED);
                break;
            case STD_ERR:
                color = getDisplay().getSystemColor(SWT.COLOR_RED);
                break;
            case STD_OUT:
            default:
                color = getDisplay().getSystemColor(SWT.COLOR_BLACK);
                break;
            }
            style.foreground = color;

            consoleText.append(message.getContent());
            consoleText.setStyleRange(style);
        }

    }

    /**
     *
     * Type of the message.
     */
    enum CommandMsgType implements IMsgType {
        STD_OUT,
        STD_ERR,
        CORE_ERR
    }

    /**
     *
     * Message about a result.
     */
    class CommandMessage implements IProcessMessage {

        private IMsgType type;

        private String content;

        public CommandMessage(IMsgType type, String content) {
            super();

            if (type == null) {
                ExceptionHandler
                        .process(new IllegalArgumentException(Messages.getString("ExecuteSystemCommandCommand.typeNull"))); //$NON-NLS-1$
            }
            if (content == null) {
                ExceptionHandler.process(new IllegalArgumentException(Messages
                        .getString("ExecuteSystemCommandCommand.contentNull"))); //$NON-NLS-1$
            }

            this.type = type;
            this.content = content;
        }

        @Override
        public String getContent() {
            return this.content;
        }

        @Override
        public IMsgType getType() {
            return this.type;
        }

    }

    /**
     * get the executed command result messages.
     */
    private Thread createResultThread(final InputStream input, final StringBuffer result) {
        final int bufferSize = 1024;
        Thread thread = new Thread() {

            @Override
            public void run() {
                try {
                    BufferedInputStream outStreamProcess = new BufferedInputStream(input);
                    byte[] buffer = new byte[bufferSize];
                    int count = 0;
                    while ((count = outStreamProcess.read(buffer, 0, buffer.length)) != -1) {
                        result.append(new String(buffer, 0, count));
                    }
                    outStreamProcess.close();
                } catch (IOException ioe) {
                    ExceptionHandler.process(ioe);
                } finally {
                    try {
                        input.close();
                    } catch (IOException e) {
                        ExceptionHandler.process(e);
                    }
                }
            }
        };
        return thread;
    }

    /**
     * add messages.
     */
    private void addCommandMessages(Collection<IProcessMessage> messages, IMsgType type, final String mess) {
        if (messages == null || mess == null || type == null || mess.length() < 1 || mess.trim().length() < 1) {
            return;
        }
        CommandMessage cmdMess = new CommandMessage(type, mess + RETURN);
        messages.add(cmdMess);
    }
}
