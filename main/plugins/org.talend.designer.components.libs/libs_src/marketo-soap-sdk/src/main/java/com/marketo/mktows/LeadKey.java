
package com.marketo.mktows;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour LeadKey complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="LeadKey">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="keyType" type="{http://www.marketo.com/mktows/}LeadKeyRef"/>
 *         &lt;element name="keyValue" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LeadKey", propOrder = {
    "keyType",
    "keyValue"
})
public class LeadKey {

    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected LeadKeyRef keyType;
    @XmlElement(required = true)
    protected String keyValue;

    /**
     * Obtient la valeur de la propriété keyType.
     * 
     * @return
     *     possible object is
     *     {@link LeadKeyRef }
     *     
     */
    public LeadKeyRef getKeyType() {
        return keyType;
    }

    /**
     * Définit la valeur de la propriété keyType.
     * 
     * @param value
     *     allowed object is
     *     {@link LeadKeyRef }
     *     
     */
    public void setKeyType(LeadKeyRef value) {
        this.keyType = value;
    }

    /**
     * Obtient la valeur de la propriété keyValue.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKeyValue() {
        return keyValue;
    }

    /**
     * Définit la valeur de la propriété keyValue.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKeyValue(String value) {
        this.keyValue = value;
    }

}
