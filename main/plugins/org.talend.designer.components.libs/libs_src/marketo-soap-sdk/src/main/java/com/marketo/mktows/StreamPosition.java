
package com.marketo.mktows;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Classe Java pour StreamPosition complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="StreamPosition">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="latestCreatedAt" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="oldestCreatedAt" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="activityCreatedAt" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="offset" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StreamPosition", propOrder = {
    "latestCreatedAt",
    "oldestCreatedAt",
    "activityCreatedAt",
    "offset"
})
public class StreamPosition {

    @XmlElementRef(name = "latestCreatedAt", type = JAXBElement.class, required = false)
    protected JAXBElement<XMLGregorianCalendar> latestCreatedAt;
    @XmlElementRef(name = "oldestCreatedAt", type = JAXBElement.class, required = false)
    protected JAXBElement<XMLGregorianCalendar> oldestCreatedAt;
    @XmlElementRef(name = "activityCreatedAt", type = JAXBElement.class, required = false)
    protected JAXBElement<XMLGregorianCalendar> activityCreatedAt;
    @XmlElementRef(name = "offset", type = JAXBElement.class, required = false)
    protected JAXBElement<String> offset;

    /**
     * Obtient la valeur de la propriété latestCreatedAt.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}
     *     
     */
    public JAXBElement<XMLGregorianCalendar> getLatestCreatedAt() {
        return latestCreatedAt;
    }

    /**
     * Définit la valeur de la propriété latestCreatedAt.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}
     *     
     */
    public void setLatestCreatedAt(JAXBElement<XMLGregorianCalendar> value) {
        this.latestCreatedAt = value;
    }

    /**
     * Obtient la valeur de la propriété oldestCreatedAt.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}
     *     
     */
    public JAXBElement<XMLGregorianCalendar> getOldestCreatedAt() {
        return oldestCreatedAt;
    }

    /**
     * Définit la valeur de la propriété oldestCreatedAt.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}
     *     
     */
    public void setOldestCreatedAt(JAXBElement<XMLGregorianCalendar> value) {
        this.oldestCreatedAt = value;
    }

    /**
     * Obtient la valeur de la propriété activityCreatedAt.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}
     *     
     */
    public JAXBElement<XMLGregorianCalendar> getActivityCreatedAt() {
        return activityCreatedAt;
    }

    /**
     * Définit la valeur de la propriété activityCreatedAt.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}
     *     
     */
    public void setActivityCreatedAt(JAXBElement<XMLGregorianCalendar> value) {
        this.activityCreatedAt = value;
    }

    /**
     * Obtient la valeur de la propriété offset.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getOffset() {
        return offset;
    }

    /**
     * Définit la valeur de la propriété offset.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setOffset(JAXBElement<String> value) {
        this.offset = value;
    }

}
