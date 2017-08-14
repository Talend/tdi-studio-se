
package com.marketo.mktows;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour ImportToListModeEnum.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * <p>
 * <pre>
 * &lt;simpleType name="ImportToListModeEnum">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="UPSERTLEADS"/>
 *     &lt;enumeration value="LISTONLY"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ImportToListModeEnum")
@XmlEnum
public enum ImportToListModeEnum {

    UPSERTLEADS,
    LISTONLY;

    public String value() {
        return name();
    }

    public static ImportToListModeEnum fromValue(String v) {
        return valueOf(v);
    }

}
