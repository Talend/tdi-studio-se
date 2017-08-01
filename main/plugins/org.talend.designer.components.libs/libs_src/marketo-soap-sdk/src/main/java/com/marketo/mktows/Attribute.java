
package com.marketo.mktows;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour Attribute complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="Attribute">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="attrName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="attrType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="attrValue" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Attribute", propOrder = {
    "attrName",
    "attrType",
    "attrValue"
})
public class Attribute {

    @XmlElement(required = true)
    protected String attrName;
    @XmlElementRef(name = "attrType", type = JAXBElement.class, required = false)
    protected JAXBElement<String> attrType;
    @XmlElement(required = true)
    protected String attrValue;

    /**
     * Obtient la valeur de la propriété attrName.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAttrName() {
        return attrName;
    }

    /**
     * Définit la valeur de la propriété attrName.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAttrName(String value) {
        this.attrName = value;
    }

    /**
     * Obtient la valeur de la propriété attrType.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getAttrType() {
        return attrType;
    }

    /**
     * Définit la valeur de la propriété attrType.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setAttrType(JAXBElement<String> value) {
        this.attrType = value;
    }

    /**
     * Obtient la valeur de la propriété attrValue.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAttrValue() {
        return attrValue;
    }

    /**
     * Définit la valeur de la propriété attrValue.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAttrValue(String value) {
        this.attrValue = value;
    }

}
