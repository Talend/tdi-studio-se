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
package org.talend.expressionbuilder.test;

import java.util.List;

import org.talend.commons.runtime.model.expressionbuilder.Variable;

/**
 * yzhang class global comment. Detailled comment <br/>
 *
 * $Id: ExpressionGenerator.java 上午10:06:29 2007-7-24 +0000 (2007-7-24) yzhang $
 *
 */
public class ExpressionGenerator {

    protected static String nl;

    public static synchronized ExpressionGenerator create(String lineSeparator) {
        nl = lineSeparator;
        ExpressionGenerator result = new ExpressionGenerator();
        nl = null;
        return result;
    }

    protected static final String NL = nl == null ? (System.getProperties().getProperty("line.separator")) : nl; //$NON-NLS-1$

    protected static final String TEXT_1 = "// ============================================================================" //$NON-NLS-1$
            + NL
            + "//" //$NON-NLS-1$
            + NL
            + "// Talend Community Edition" //$NON-NLS-1$
            + NL
            + "//" //$NON-NLS-1$
            + NL
            + "// Copyright (C) 2006-2021 Talend Inc. - www.talend.com" //$NON-NLS-1$
            + NL
            + "//" //$NON-NLS-1$
            + NL
            + "// This library is free software; you can redistribute it and/or" //$NON-NLS-1$
            + NL
            + "// modify it under the terms of the GNU Lesser General Public" //$NON-NLS-1$
            + NL
            + "// License as published by the Free Software Foundation; either" //$NON-NLS-1$
            + NL
            + "// version 2.1 of the License." //$NON-NLS-1$
            + NL
            + "//" //$NON-NLS-1$
            + NL
            + "// This library is distributed in the hope that it will be useful," //$NON-NLS-1$
            + NL
            + "// but WITHOUT ANY WARRANTY; without even the implied warranty of" //$NON-NLS-1$
            + NL
            + "// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU" //$NON-NLS-1$
            + NL
            + "// Lesser General Public License for more details." //$NON-NLS-1$
            + NL
            + "//" //$NON-NLS-1$
            + NL
            + "// You should have received a copy of the GNU General Public License" //$NON-NLS-1$
            + NL
            + "// along with this program; if not, write to the Free Software" //$NON-NLS-1$
            + NL
            + "// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA." //$NON-NLS-1$
            + NL
            + "//" //$NON-NLS-1$
            + NL
            + "// ============================================================================"; //$NON-NLS-1$

    protected static final String TEXT_2 = NL + "package routines;\t" + NL + "" + NL + "import java.io.IOException;" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            + NL + "import java.io.PrintWriter;" + NL + "import java.net.Socket;" + NL //$NON-NLS-1$ //$NON-NLS-2$
            + "import java.net.UnknownHostException;" + NL + "" + NL + "public class ExpressionVariableTest {" + NL //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            + "" + NL + "    private Socket client;" + NL + "" + NL + "    private PrintWriter writer;" + NL + "    "; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

    protected static final String TEXT_3 = NL + "    private String "; //$NON-NLS-1$

    protected static final String TEXT_4 = "=\""; //$NON-NLS-1$

    protected static final String TEXT_5 = "\";" + NL + "    "; //$NON-NLS-1$ //$NON-NLS-2$

    protected static final String TEXT_6 = NL
            + NL
            + NL
            + "    public ExpressionVariableTest() {" //$NON-NLS-1$
            + NL
            + "        try {" //$NON-NLS-1$
            + NL
            + "            client = new Socket(\"localhost\", 6666);" //$NON-NLS-1$
            + NL
            + "            writer = new java.io.PrintWriter(new java.io.BufferedWriter(new java.io.OutputStreamWriter(client" //$NON-NLS-1$
            + NL + "                    .getOutputStream())), true);" + NL //$NON-NLS-1$
            + "        } catch (UnknownHostException e) {" + NL + "            e.printStackTrace();" + NL //$NON-NLS-1$ //$NON-NLS-2$
            + "        } catch (IOException e) {" + NL + "            e.printStackTrace();" + NL + "        }" + NL //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            + "" + NL + "    }" + NL + "" + NL + "    private void exec() {" + NL //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            + "        String msg = String.valueOf("; //$NON-NLS-1$

    protected static final String TEXT_7 = ");" + NL + "        sendMsg(msg);" + NL + "    }" + NL + "" + NL //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            + "    public void sendMsg(String msg) {" + NL + "        writer.print(msg);" + NL //$NON-NLS-1$ //$NON-NLS-2$
            + "        writer.flush();" + NL + "        try {" + NL + "            writer.close();" + NL //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            + "            client.close();" + NL + "            writer = null;" + NL + "            client = null;" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            + NL + "        } catch (IOException e) {" + NL + "            e.printStackTrace();" + NL + "        }" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            + NL + "    }" + NL + "" + NL + "    public static void main(String[] args) {" + NL //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            + "\t\tnew ExpressionVariableTest().exec();" + NL + "    }" + NL + "}"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    protected static final String TEXT_8 = NL;

    public String generate(Object argument) {
        final StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(TEXT_1);

        ExpressionArguments args = (ExpressionArguments) argument;
        String expression = args.getExpression();
        List<Variable> variables = args.getVariables();

        stringBuffer.append(TEXT_2);

        for (Variable var : variables) {

            stringBuffer.append(TEXT_3);
            stringBuffer.append(var.getName());
            stringBuffer.append(TEXT_4);
            stringBuffer.append(var.getValue());
            stringBuffer.append(TEXT_5);

        }

        stringBuffer.append(TEXT_6);
        stringBuffer.append(expression);
        stringBuffer.append(TEXT_7);
        stringBuffer.append(TEXT_8);
        return stringBuffer.toString();
    }
}
