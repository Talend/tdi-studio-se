
package com.marketo.mktows;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour ReqCampSourceType.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * <p>
 * <pre>
 * &lt;simpleType name="ReqCampSourceType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="MKTOWS"/>
 *     &lt;enumeration value="SALES"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ReqCampSourceType")
@XmlEnum
public enum ReqCampSourceType {

    MKTOWS,
    SALES;

    public String value() {
        return name();
    }

    public static ReqCampSourceType fromValue(String v) {
        return valueOf(v);
    }

}
