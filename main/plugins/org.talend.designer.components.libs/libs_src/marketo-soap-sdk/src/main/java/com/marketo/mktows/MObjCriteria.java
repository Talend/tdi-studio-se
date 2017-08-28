
package com.marketo.mktows;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour MObjCriteria complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="MObjCriteria">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="attrName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="comparison" type="{http://www.marketo.com/mktows/}ComparisonEnum"/>
 *         &lt;element name="attrValue" type="{http://www.w3.org/2001/XMLSchema}anySimpleType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MObjCriteria", propOrder = {
    "attrName",
    "comparison",
    "attrValue"
})
public class MObjCriteria {

    @XmlElement(required = true)
    protected String attrName;
    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected ComparisonEnum comparison;
    @XmlElement(required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected Object attrValue;

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
     * Obtient la valeur de la propriété comparison.
     * 
     * @return
     *     possible object is
     *     {@link ComparisonEnum }
     *     
     */
    public ComparisonEnum getComparison() {
        return comparison;
    }

    /**
     * Définit la valeur de la propriété comparison.
     * 
     * @param value
     *     allowed object is
     *     {@link ComparisonEnum }
     *     
     */
    public void setComparison(ComparisonEnum value) {
        this.comparison = value;
    }

    /**
     * Obtient la valeur de la propriété attrValue.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getAttrValue() {
        return attrValue;
    }

    /**
     * Définit la valeur de la propriété attrValue.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setAttrValue(Object value) {
        this.attrValue = value;
    }

}
