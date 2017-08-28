
package com.marketo.mktows;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour ImportToListStatusEnum.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * <p>
 * <pre>
 * &lt;simpleType name="ImportToListStatusEnum">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="PROCESSING"/>
 *     &lt;enumeration value="COMPLETE"/>
 *     &lt;enumeration value="FAILED"/>
 *     &lt;enumeration value="CANCELED"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ImportToListStatusEnum")
@XmlEnum
public enum ImportToListStatusEnum {

    PROCESSING,
    COMPLETE,
    FAILED,
    CANCELED;

    public String value() {
        return name();
    }

    public static ImportToListStatusEnum fromValue(String v) {
        return valueOf(v);
    }

}
