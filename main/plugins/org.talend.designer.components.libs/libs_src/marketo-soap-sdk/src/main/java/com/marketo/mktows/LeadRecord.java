
package com.marketo.mktows;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour LeadRecord complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="LeadRecord">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Id" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="Email" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ForeignSysPersonId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ForeignSysType" type="{http://www.marketo.com/mktows/}ForeignSysType" minOccurs="0"/>
 *         &lt;element name="leadAttributeList" type="{http://www.marketo.com/mktows/}ArrayOfAttribute" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LeadRecord", propOrder = {
    "id",
    "email",
    "foreignSysPersonId",
    "foreignSysType",
    "leadAttributeList"
})
public class LeadRecord {

    @XmlElementRef(name = "Id", type = JAXBElement.class, required = false)
    protected JAXBElement<Integer> id;
    @XmlElementRef(name = "Email", type = JAXBElement.class, required = false)
    protected JAXBElement<String> email;
    @XmlElementRef(name = "ForeignSysPersonId", type = JAXBElement.class, required = false)
    protected JAXBElement<String> foreignSysPersonId;
    @XmlElementRef(name = "ForeignSysType", type = JAXBElement.class, required = false)
    protected JAXBElement<ForeignSysType> foreignSysType;
    @XmlElementRef(name = "leadAttributeList", type = JAXBElement.class, required = false)
    protected JAXBElement<ArrayOfAttribute> leadAttributeList;

    /**
     * Obtient la valeur de la propriété id.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public JAXBElement<Integer> getId() {
        return id;
    }

    /**
     * Définit la valeur de la propriété id.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public void setId(JAXBElement<Integer> value) {
        this.id = value;
    }

    /**
     * Obtient la valeur de la propriété email.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getEmail() {
        return email;
    }

    /**
     * Définit la valeur de la propriété email.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setEmail(JAXBElement<String> value) {
        this.email = value;
    }

    /**
     * Obtient la valeur de la propriété foreignSysPersonId.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getForeignSysPersonId() {
        return foreignSysPersonId;
    }

    /**
     * Définit la valeur de la propriété foreignSysPersonId.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setForeignSysPersonId(JAXBElement<String> value) {
        this.foreignSysPersonId = value;
    }

    /**
     * Obtient la valeur de la propriété foreignSysType.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ForeignSysType }{@code >}
     *     
     */
    public JAXBElement<ForeignSysType> getForeignSysType() {
        return foreignSysType;
    }

    /**
     * Définit la valeur de la propriété foreignSysType.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ForeignSysType }{@code >}
     *     
     */
    public void setForeignSysType(JAXBElement<ForeignSysType> value) {
        this.foreignSysType = value;
    }

    /**
     * Obtient la valeur de la propriété leadAttributeList.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfAttribute }{@code >}
     *     
     */
    public JAXBElement<ArrayOfAttribute> getLeadAttributeList() {
        return leadAttributeList;
    }

    /**
     * Définit la valeur de la propriété leadAttributeList.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfAttribute }{@code >}
     *     
     */
    public void setLeadAttributeList(JAXBElement<ArrayOfAttribute> value) {
        this.leadAttributeList = value;
    }

}
