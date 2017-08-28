
package com.marketo.mktows;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour ResultGetLeadChanges complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ResultGetLeadChanges">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="returnCount" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="remainingCount" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="newStartPosition" type="{http://www.marketo.com/mktows/}StreamPosition"/>
 *         &lt;element name="leadChangeRecordList" type="{http://www.marketo.com/mktows/}ArrayOfLeadChangeRecord" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResultGetLeadChanges", propOrder = {
    "returnCount",
    "remainingCount",
    "newStartPosition",
    "leadChangeRecordList"
})
public class ResultGetLeadChanges {

    protected int returnCount;
    protected int remainingCount;
    @XmlElement(required = true)
    protected StreamPosition newStartPosition;
    @XmlElementRef(name = "leadChangeRecordList", type = JAXBElement.class, required = false)
    protected JAXBElement<ArrayOfLeadChangeRecord> leadChangeRecordList;

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
     * Obtient la valeur de la propriété newStartPosition.
     * 
     * @return
     *     possible object is
     *     {@link StreamPosition }
     *     
     */
    public StreamPosition getNewStartPosition() {
        return newStartPosition;
    }

    /**
     * Définit la valeur de la propriété newStartPosition.
     * 
     * @param value
     *     allowed object is
     *     {@link StreamPosition }
     *     
     */
    public void setNewStartPosition(StreamPosition value) {
        this.newStartPosition = value;
    }

    /**
     * Obtient la valeur de la propriété leadChangeRecordList.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfLeadChangeRecord }{@code >}
     *     
     */
    public JAXBElement<ArrayOfLeadChangeRecord> getLeadChangeRecordList() {
        return leadChangeRecordList;
    }

    /**
     * Définit la valeur de la propriété leadChangeRecordList.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfLeadChangeRecord }{@code >}
     *     
     */
    public void setLeadChangeRecordList(JAXBElement<ArrayOfLeadChangeRecord> value) {
        this.leadChangeRecordList = value;
    }

}
