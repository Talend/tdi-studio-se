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
package org.talend.expressionbuilder.test.shadow;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.talend.commons.runtime.model.expressionbuilder.Variable;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.exception.RuntimeExceptionHandler;
import org.talend.core.model.context.JobContextParameter;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.designer.rowgenerator.RowGeneratorComponent;
import org.talend.designer.rowgenerator.data.Function;
import org.talend.designer.rowgenerator.managers.UIManager;
import org.talend.designer.rowgenerator.shadow.LogRowNode;
import org.talend.designer.rowgenerator.shadow.RowGenProcess;
import org.talend.designer.rowgenerator.shadow.RowGenProcessMain;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.expressionbuilder.ui.ExpressionBuilderDialog;

/**
 * yzhang class global comment. Detailled comment <br/>
 *
 * $Id: TestProcess.java 下午02:58:17 2007-7-17 +0000 (2007-7-17) yzhang $
 *
 */
public class ExpressionTestMain {

    public static final String EXPRESSION_BUILDER = "Expression_builder"; //$NON-NLS-1$

    // for bug 13625
    public static final String EXPRESSION_CONTEXT = RowGenProcessMain.PREVIEW; //$NON-NLS-1$

    private IProcess expressionBuilderTestProcess;

    private final StyledText text;

    private Process process;

    /**
     * yzhang TestProcess constructor comment.
     */
    public ExpressionTestMain(Function function, StyledText resultText) {
        text = resultText;

        if (function != null) {
            IContext context2 = new org.talend.core.model.context.JobContext(EXPRESSION_CONTEXT);

            List<IContextParameter> params = new ArrayList<IContextParameter>();
            JobContextParameter contextParameter = new JobContextParameter();
            contextParameter.setName(EXPRESSION_BUILDER);
            contextParameter.setValue(EXPRESSION_BUILDER);
            contextParameter.setType("String"); //$NON-NLS-1$
            params.add(contextParameter);
            context2.setContextParameterList(params);

            if (UIManager.isJavaProject()) {

                Property property = PropertiesFactory.eINSTANCE.createProperty();
                property.setLabel(EXPRESSION_BUILDER + "_RowGenerator2"); //$NON-NLS-1$
                property.setId(EXPRESSION_BUILDER + "_RowGenerator2"); //$NON-NLS-1$

                RowGeneratorComponent rowGeneratorNode = new VirtualRowGeneratorNode(function);
                rowGeneratorNode.setNumber("1"); //$NON-NLS-1$

                expressionBuilderTestProcess = new RowGenProcess(property, rowGeneratorNode);

            } else {
                String expression = ExpressionBuilderDialog.getExpressionComposite().getExpression();
                List<Variable> variables = ExpressionBuilderDialog.getTestComposite().getVariableList();
                for (Variable variable : variables) {
                    expression = expression.replaceAll(variable.getName(), variable.getValue());
                }

                PerlRowNode inNode = new PerlRowNode(expression);
                LogRowNode outNode = new LogRowNode("tLogRow", inNode.getMetadataList().get(0)); //$NON-NLS-1$

                expressionBuilderTestProcess = new AnyIntoLogRowProcess<PerlRowNode>(inNode, outNode);

            }

            startProcess(expressionBuilderTestProcess, context2);
        }

    }

    /**
     * yzhang Comment method "startProcess".
     *
     * @param p
     * @param context
     */
    private void startProcess(IProcess p, IContext context) {
        final IProcessor processor = ProcessorUtilities.getProcessor(p, null, context);
        new Thread(new Runnable() {

            public void run() {
                try {
                    process = processor.run(IProcessor.NO_STATISTICS, IProcessor.NO_TRACES, null);
                    Thread.sleep(500);
                    Display.getDefault().asyncExec(new Runnable() {

                        public void run() {
                            feedBackTestResult();
                        }
                    });
                } catch (Exception e) {
                    // e.printStackTrace();
                    ExceptionHandler.process(e);
                }

            }
        }).start();
        // int counter = 0;
        // while (process == null && counter < 6) {
        // try {
        // System.out.println("Thread : stop 500ms. ");
        // Thread.sleep(500);
        // counter++;
        // } catch (InterruptedException e) {
        // RuntimeExceptionHandler.process(e);
        // }
        // }
    }

    /**
     * yzhang Comment method "feedBackTestResult".
     */
    private void feedBackTestResult() {

        StringBuffer testResult = new StringBuffer();
        try {
            // Thread.sleep(500);
            BufferedReader readerOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader readerError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            Thread.sleep(500);
            boolean ready = readerOut.ready();
            // System.out.println(ready);
            if (ready) {
                testResult.append(readerOut.readLine() + "\n"); //$NON-NLS-1$
            }
            Thread.sleep(500);
            text.setText(testResult.toString());
            if (readerError.ready()) {
                testResult.append(readerError.readLine() + "\n"); //$NON-NLS-1$
                text.setText(testResult.toString());
                Color red = new Color(text.getDisplay(), new RGB(255, 0, 0));
                StyleRange style = new StyleRange(0, testResult.length(), red, null, SWT.NORMAL);
                text.setStyleRange(style);
            }
        } catch (Exception e) {
            RuntimeExceptionHandler.process(e);
        }
    }
}
