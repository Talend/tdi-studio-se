package org.talend.sdk.component.studio.ui.guessschema;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IElementParameter;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.properties.controllers.AbstractGuessSchemaProcess;

public class GuessSchemaRunnable implements IRunnableWithProgress {

    private final IContext context;

    private IElementParameter elementParameter;

    private String schema = null;

    private boolean canceled = false;

    public GuessSchemaRunnable(final IContext context,
            final IElementParameter elementParameter) {
        this.context = context;
        this.elementParameter = elementParameter;
    }

    @Override
    public void run(final IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
        try {
            TaCoKitGuessSchemaProcess gsp =
                    new TaCoKitGuessSchemaProcess(AbstractGuessSchemaProcess.getNewmockProperty(),
                            Node.class.cast(elementParameter.getElement()),
                            context,
                            discoverSchemaActionName());

            final Future<String> result = gsp.run();
            while (!result.isDone()) {
                if (monitor.isCanceled()) {
                    result.cancel(true);
                    canceled = true;
                    return;
                }
            }
            try {
                schema = result.get();
            } catch (ExecutionException e) {
                throw new InvocationTargetException(e);
            }
        } finally {
            monitor.done();
        }
    }

    private String discoverSchemaActionName() {
        return elementParameter.getValue() == null ? null
                : String.valueOf(elementParameter.getValue());
    }

    public synchronized String getSchema() {
        return schema;
    }

    public synchronized boolean isCanceled() {
        return canceled;
    }
}
