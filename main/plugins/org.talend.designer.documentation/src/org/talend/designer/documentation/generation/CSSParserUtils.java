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
package org.talend.designer.documentation.generation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleSheet;

import com.steadystate.css.parser.CSSOMParser;

/**
 * wzhang class global comment. Detailled comment
 */
public class CSSParserUtils {

    public static CSSRuleList parserCSSSelectors(Reader read, String url) {
        CSSOMParser cssparser = new CSSOMParser();
        CSSStyleSheet css = null;
        try {
            if (read == null)
                css = cssparser.parseStyleSheet(new InputSource("file:" + url), null, null); //$NON-NLS-1$
            else
                css = cssparser.parseStyleSheet(new InputSource(read), null, null);
        } catch (IOException e) {
            ExceptionHandler.process(e);
            return null;
        }

        return css.getCssRules();
    }

    public static String generateCssStyle(String cssFile, CSSRuleList ruleList, String content) {
        String cssContents = calculateValue(getContents(cssFile));
        content = calculateValue(content);

        for (int i = 0; i < ruleList.getLength(); i++) {
            CSSRule item = ruleList.item(i);
            String text = item.getCssText();
            Pattern p = Pattern.compile(".+\\{.*?\\}"); //$NON-NLS-1$
            Matcher matcher = p.matcher(text);
            if (matcher.matches()) {
                String value = text.substring(0, text.indexOf("{")); //$NON-NLS-1$
                // sometimes jdk api don't think string contains charseq if start with "*."
                if (value.startsWith("*.")) { //$NON-NLS-1$
                    value = value.substring(2);
                }
                if (content.indexOf(value) != -1) {
                    String cssTemStrings = content.substring(content.indexOf(value));
                    String cssPart = cssTemStrings.substring(value.length());
                    String originalValue = cssTemStrings.substring(0, value.length())
                            + cssPart.substring(0, cssPart.indexOf("}") + 1); //$NON-NLS-1$

                    if (cssContents.indexOf(value) != -1) {
                        String temStrings = cssContents.substring(cssContents.indexOf(value));
                        String part = temStrings.substring(value.length());
                        String newValue = temStrings.substring(0, value.length()) + part.substring(0, part.indexOf("}") + 1); //$NON-NLS-1$

                        content = content.replace(originalValue, newValue);
                    }
                }
            }
        }
        return content;
    }

    public static String getContents(String file) {
        FileInputStream inputstream;
        StringBuffer buffer = new StringBuffer();
        try {
            inputstream = new FileInputStream(file);
            String line;
            BufferedReader bufferreader = new BufferedReader(new InputStreamReader(inputstream));
            line = bufferreader.readLine();
            while (line != null) {
                buffer.append(line);
                line = bufferreader.readLine();
            }
            inputstream.close();
        } catch (FileNotFoundException e) {
            ExceptionHandler.process(e);
        } catch (IOException e) {
            ExceptionHandler.process(e);
        }
        return buffer.toString();
    }

    public static String calculateValue(String value) {
        if (value == null) {
            return null;
        }
        if (value.contains("\n")) { //$NON-NLS-1$
            value = value.replace("\n", " "); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (value.contains("\r")) { //$NON-NLS-1$
            value = value.replace("\r", " "); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (value.contains("\t")) { //$NON-NLS-1$
            value = value.replace("\t", " "); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (value.contains("{")) { //$NON-NLS-1$
            value = value.replace("{", " {"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return value;
    }

    public static void createCssFile(String content, String fileName) {
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            int length = content.length();

            String s = ""; //$NON-NLS-1$
            for (int i = 0; i < length; i++) {
                char c = content.charAt(i);
                s += c;
                if (c == '{' || c == '}') {
                    s += "\r\n"; //$NON-NLS-1$
                    out.write(s);
                    out.flush();
                    s = ""; //$NON-NLS-1$
                }
            }
            out.write(s);
            out.flush();
            out.close();
        } catch (IOException e) {
            ExceptionHandler.process(e);
        }

    }
}
