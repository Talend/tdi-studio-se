
package com.marketo.mktows;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour ForeignSysType.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * <p>
 * <pre>
 * &lt;simpleType name="ForeignSysType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="CUSTOM"/>
 *     &lt;enumeration value="SFDC"/>
 *     &lt;enumeration value="NETSUITE"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ForeignSysType")
@XmlEnum
public enum ForeignSysType {

    CUSTOM,
    SFDC,
    NETSUITE;

    public String value() {
        return name();
    }

    public static ForeignSysType fromValue(String v) {
        return valueOf(v);
    }

}
