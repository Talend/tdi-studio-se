
package com.marketo.mktows;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour LeadActivityList complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="LeadActivityList">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="returnCount" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="remainingCount" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="newStartPosition" type="{http://www.marketo.com/mktows/}StreamPosition"/>
 *         &lt;element name="activityRecordList" type="{http://www.marketo.com/mktows/}ArrayOfActivityRecord" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LeadActivityList", propOrder = {
    "returnCount",
    "remainingCount",
    "newStartPosition",
    "activityRecordList"
})
public class LeadActivityList {

    protected int returnCount;
    protected int remainingCount;
    @XmlElement(required = true)
    protected StreamPosition newStartPosition;
    @XmlElementRef(name = "activityRecordList", type = JAXBElement.class, required = false)
    protected JAXBElement<ArrayOfActivityRecord> activityRecordList;

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
     * Obtient la valeur de la propriété activityRecordList.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfActivityRecord }{@code >}
     *     
     */
    public JAXBElement<ArrayOfActivityRecord> getActivityRecordList() {
        return activityRecordList;
    }

    /**
     * Définit la valeur de la propriété activityRecordList.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfActivityRecord }{@code >}
     *     
     */
    public void setActivityRecordList(JAXBElement<ArrayOfActivityRecord> value) {
        this.activityRecordList = value;
    }

}
