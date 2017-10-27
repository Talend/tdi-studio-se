
package com.marketo.mktows;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour ResultGetLead complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ResultGetLead">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="count" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="leadRecordList" type="{http://www.marketo.com/mktows/}ArrayOfLeadRecord" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResultGetLead", propOrder = {
    "count",
    "leadRecordList"
})
public class ResultGetLead {

    protected int count;
    @XmlElementRef(name = "leadRecordList", type = JAXBElement.class, required = false)
    protected JAXBElement<ArrayOfLeadRecord> leadRecordList;

    /**
     * Obtient la valeur de la propriété count.
     * 
     */
    public int getCount() {
        return count;
    }

    /**
     * Définit la valeur de la propriété count.
     * 
     */
    public void setCount(int value) {
        this.count = value;
    }

    /**
     * Obtient la valeur de la propriété leadRecordList.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfLeadRecord }{@code >}
     *     
     */
    public JAXBElement<ArrayOfLeadRecord> getLeadRecordList() {
        return leadRecordList;
    }

    /**
     * Définit la valeur de la propriété leadRecordList.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfLeadRecord }{@code >}
     *     
     */
    public void setLeadRecordList(JAXBElement<ArrayOfLeadRecord> value) {
        this.leadRecordList = value;
    }

}
