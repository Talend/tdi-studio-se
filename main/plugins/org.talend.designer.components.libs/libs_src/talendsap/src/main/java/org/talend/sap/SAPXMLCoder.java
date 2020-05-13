package org.talend.sap;

/**
 * sap <-> valid xml element name by coder
 *
 * first char :
 * 0 <-> _0
 * 1 <-> _1
 * 2 <-> _2
 * ...
 * 9  <-> _9
 *
 * any location char
 * / <-> _-
 */
public class SAPXMLCoder {

    /**
     * convert sapname to xmlname
     * @param sapname
     * @return
     */
    public static String encode(String sapname) {
        if(sapname == null) {
            return sapname;
        }

        StringBuilder sb = new StringBuilder();
        for(int i=0;i<sapname.length();i++) {
            char c = sapname.charAt(i);

            if(i==0) {
                if(isDigit(c)) {
                    sb.append('_').append(c);
                } else if(c == '/'){
                    sb.append("_-");
                } else {
                    sb.append(c);
                }
            } else {
                if(c == '/'){
                    sb.append("_-");
                } else {
                    sb.append(c);
                }
            }
        }

        return sb.toString();
    }

    /**
     * convert xmlname to sapname
     * @param xmlname
     * @return
     */
    static String decode(String xmlname) {
        if(xmlname == null) {
            return xmlname;
        }

        StringBuilder sb = new StringBuilder();
        for(int i=0;i<xmlname.length();i++) {
            char c = xmlname.charAt(i);

            if(i==0) {
                if(c == '_') {
                    int next_index = i + 1;
                    if(next_index  < xmlname.length())  {
                        char next = xmlname.charAt(next_index);
                        if(isDigit(next)) {
                            sb.append(next);
                            i++;
                        } else if(next == '-') {
                            sb.append('/');
                            i++;
                        } else {
                            sb.append(c);
                        }
                    } else {
                        sb.append(c);
                    }
                } else {
                    sb.append(c);
                }
            } else {
                if(c == '_') {
                    int next_index = i + 1;
                    if(next_index  < xmlname.length())  {
                        char next = xmlname.charAt(next_index);
                        if(next == '-') {
                            sb.append('/');
                            i++;
                        } else {
                            sb.append(c);
                        }
                    } else {
                        sb.append(c);
                    }
                } else {
                    sb.append(c);
                }
            }
        }

        return sb.toString();
    }

    private static boolean isDigit(char c) {
        return c>=48 && c<=57;
    }

    public static void main(String[] args) {
        //ecnode
        System.out.println(encode("/CMT/TLND_TABLE"));
        System.out.println(encode("0CMT0TLND_TABLE"));
        System.out.println(encode("1CMT1TLND_TABLE"));

        //decode
        System.out.println(decode(encode("/CMT/TLND_TABLE")));
        System.out.println(decode(encode("0CMT0TLND_TABLE")));
        System.out.println(decode(encode("1CMT1TLND_TABLE")));
    }
}
