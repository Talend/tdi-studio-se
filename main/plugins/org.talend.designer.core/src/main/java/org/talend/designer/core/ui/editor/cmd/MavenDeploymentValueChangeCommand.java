package org.talend.designer.core.ui.editor.cmd;

import org.eclipse.gef.commands.Command;
import org.talend.designer.core.ui.editor.process.Process;

public class MavenDeploymentValueChangeCommand extends Command {

    private Process process;

    private String type;

    private String newValue;

    public MavenDeploymentValueChangeCommand(Process process, String type, String value) {
        this.process = process;
        this.type = type;
        newValue = value;
    }

    @Override
    public void execute() {
        if (process != null) {
            process.getAdditionalProperties().put(type, newValue);
        }
    }

}
