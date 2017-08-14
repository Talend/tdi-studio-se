
package com.marketo.mktows;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour LeadKeyRef.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * <p>
 * <pre>
 * &lt;simpleType name="LeadKeyRef">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="IDNUM"/>
 *     &lt;enumeration value="COOKIE"/>
 *     &lt;enumeration value="EMAIL"/>
 *     &lt;enumeration value="LEADOWNEREMAIL"/>
 *     &lt;enumeration value="SFDCACCOUNTID"/>
 *     &lt;enumeration value="SFDCCONTACTID"/>
 *     &lt;enumeration value="SFDCLEADID"/>
 *     &lt;enumeration value="SFDCLEADOWNERID"/>
 *     &lt;enumeration value="SFDCOPPTYID"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "LeadKeyRef")
@XmlEnum
public enum LeadKeyRef {

    IDNUM,
    COOKIE,
    EMAIL,
    LEADOWNEREMAIL,
    SFDCACCOUNTID,
    SFDCCONTACTID,
    SFDCLEADID,
    SFDCLEADOWNERID,
    SFDCOPPTYID;

    public String value() {
        return name();
    }

    public static LeadKeyRef fromValue(String v) {
        return valueOf(v);
    }

}
