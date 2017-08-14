
package com.marketo.mktows;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour ResultGetMultipleLeads complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ResultGetMultipleLeads">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="returnCount" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="remainingCount" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="newStreamPosition" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
@XmlType(name = "ResultGetMultipleLeads", propOrder = {
    "returnCount",
    "remainingCount",
    "newStreamPosition",
    "leadRecordList"
})
public class ResultGetMultipleLeads {

    protected int returnCount;
    protected int remainingCount;
    @XmlElement(required = true)
    protected String newStreamPosition;
    @XmlElementRef(name = "leadRecordList", type = JAXBElement.class, required = false)
    protected JAXBElement<ArrayOfLeadRecord> leadRecordList;

    /**
     * Obtient la valeur de la propriété returnCount.
     * 
     */
    public int getReturnCount() {
        return returnCount;
    }

    /**
     * Définit la valeur de la propriété returnCount.
     * 
     */
    public void setReturnCount(int value) {
        this.returnCount = value;
    }

    /**
     * Obtient la valeur de la propriété remainingCount.
     * 
     */
    public int getRemainingCount() {
        return remainingCount;
    }

    /**
     * Définit la valeur de la propriété remainingCount.
     * 
     */
    public void setRemainingCount(int value) {
        this.remainingCount = value;
    }

    /**
     * Obtient la valeur de la propriété newStreamPosition.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNewStreamPosition() {
        return newStreamPosition;
    }

    /**
     * Définit la valeur de la propriété newStreamPosition.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNewStreamPosition(String value) {
        this.newStreamPosition = value;
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
