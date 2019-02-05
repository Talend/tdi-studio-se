package org.talend.xml.sax;

/**
 * escape helper for xml special char
 * @author Administrator
 *
 */
public class EscapeEntityHelper {

	private StringBuffer buffer = new StringBuffer();
	
	private char attributeQuoteChar = '\"';
	
	
	/**
	 * escape xml text
	 * @param text
	 * @return
	 */
	public String escapeElementEntities(String text) {
		return escapeEntities(text,false);
	}
	
	/**
	 * escape xml attribute value
	 * @param text
	 * @return
	 */
	public String escapeAttributeEntities(String text) {
		return escapeEntities(text,true);
	}
	
	private String escapeEntities(String text,boolean isAttri) {
		char[] block = null;
        int i;
        int last = 0;
        int size = text.length();

        for (i = 0; i < size; i++) {
            String entity = null;
            char c = text.charAt(i);

            switch (c) {
                case '<':
                    entity = "&lt;";

                    break;

                case '>':
                    entity = "&gt;";

                    break;

                case '\'':

                    if (isAttri && attributeQuoteChar == '\'') {
                        entity = "&apos;";
                    }

                    break;

                case '\"':

                    if (isAttri && attributeQuoteChar == '\"') {
                        entity = "&quot;";
                    }

                    break;

                case '&':
                    entity = "&amp;";

                    break;

                default:
                	//do nothing now
                    break;
            }

            if (entity != null) {
                if (block == null) {
                    block = text.toCharArray();
                }

                buffer.append(block, last, i - last);
                buffer.append(entity);
                last = i + 1;
            }
        }

        if (last == 0) {
            return text;
        }

        if (last < size) {
            if (block == null) {
                block = text.toCharArray();
            }

            buffer.append(block, last, i - last);
        }

        String answer = buffer.toString();
        buffer.setLength(0);

        return answer;
	}
	
}
