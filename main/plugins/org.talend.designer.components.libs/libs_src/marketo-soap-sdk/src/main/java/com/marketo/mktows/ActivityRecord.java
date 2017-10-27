
package com.marketo.mktows;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Classe Java pour ActivityRecord complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ActivityRecord">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="marketoGUID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="activityDateTime" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="activityType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="mktgAssetName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="activityAttributes" type="{http://www.marketo.com/mktows/}ArrayOfAttribute" minOccurs="0"/>
 *         &lt;element name="campaign" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="personName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="mktPersonId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="foreignSysId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="orgName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="foreignSysOrgId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ActivityRecord", propOrder = {
    "id",
    "marketoGUID",
    "activityDateTime",
    "activityType",
    "mktgAssetName",
    "activityAttributes",
    "campaign",
    "personName",
    "mktPersonId",
    "foreignSysId",
    "orgName",
    "foreignSysOrgId"
})
public class ActivityRecord {

    @XmlElementRef(name = "id", type = JAXBElement.class, required = false)
    protected JAXBElement<Long> id;
    @XmlElement(required = true)
    protected String marketoGUID;
    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar activityDateTime;
    @XmlElement(required = true)
    protected String activityType;
    protected String mktgAssetName;
    @XmlElementRef(name = "activityAttributes", type = JAXBElement.class, required = false)
    protected JAXBElement<ArrayOfAttribute> activityAttributes;
    @XmlElementRef(name = "campaign", type = JAXBElement.class, required = false)
    protected JAXBElement<String> campaign;
    @XmlElementRef(name = "personName", type = JAXBElement.class, required = false)
    protected JAXBElement<String> personName;
    @XmlElement(required = true)
    protected String mktPersonId;
    @XmlElementRef(name = "foreignSysId", type = JAXBElement.class, required = false)
    protected JAXBElement<String> foreignSysId;
    @XmlElementRef(name = "orgName", type = JAXBElement.class, required = false)
    protected JAXBElement<String> orgName;
    @XmlElementRef(name = "foreignSysOrgId", type = JAXBElement.class, required = false)
    protected JAXBElement<String> foreignSysOrgId;

    /**
     * Obtient la valeur de la propriété id.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Long }{@code >}
     *     
     */
    public JAXBElement<Long> getId() {
        return id;
    }

    /**
     * Définit la valeur de la propriété id.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Long }{@code >}
     *     
     */
    public void setId(JAXBElement<Long> value) {
        this.id = value;
    }

    /**
     * Obtient la valeur de la propriété marketoGUID.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMarketoGUID() {
        return marketoGUID;
    }

    /**
     * Définit la valeur de la propriété marketoGUID.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMarketoGUID(String value) {
        this.marketoGUID = value;
    }

    /**
     * Obtient la valeur de la propriété activityDateTime.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getActivityDateTime() {
        return activityDateTime;
    }

    /**
     * Définit la valeur de la propriété activityDateTime.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setActivityDateTime(XMLGregorianCalendar value) {
        this.activityDateTime = value;
    }

    /**
     * Obtient la valeur de la propriété activityType.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getActivityType() {
        return activityType;
    }

    /**
     * Définit la valeur de la propriété activityType.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setActivityType(String value) {
        this.activityType = value;
    }

    /**
     * Obtient la valeur de la propriété mktgAssetName.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMktgAssetName() {
        return mktgAssetName;
    }

    /**
     * Définit la valeur de la propriété mktgAssetName.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMktgAssetName(String value) {
        this.mktgAssetName = value;
    }

    /**
     * Obtient la valeur de la propriété activityAttributes.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfAttribute }{@code >}
     *     
     */
    public JAXBElement<ArrayOfAttribute> getActivityAttributes() {
        return activityAttributes;
    }

    /**
     * Définit la valeur de la propriété activityAttributes.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfAttribute }{@code >}
     *     
     */
    public void setActivityAttributes(JAXBElement<ArrayOfAttribute> value) {
        this.activityAttributes = value;
    }

    /**
     * Obtient la valeur de la propriété campaign.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getCampaign() {
        return campaign;
    }

    /**
     * Définit la valeur de la propriété campaign.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setCampaign(JAXBElement<String> value) {
        this.campaign = value;
    }

    /**
     * Obtient la valeur de la propriété personName.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getPersonName() {
        return personName;
    }

    /**
     * Définit la valeur de la propriété personName.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setPersonName(JAXBElement<String> value) {
        this.personName = value;
    }

    /**
     * Obtient la valeur de la propriété mktPersonId.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMktPersonId() {
        return mktPersonId;
    }

    /**
     * Définit la valeur de la propriété mktPersonId.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMktPersonId(String value) {
        this.mktPersonId = value;
    }

    /**
     * Obtient la valeur de la propriété foreignSysId.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getForeignSysId() {
        return foreignSysId;
    }

    /**
     * Définit la valeur de la propriété foreignSysId.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setForeignSysId(JAXBElement<String> value) {
        this.foreignSysId = value;
    }

    /**
     * Obtient la valeur de la propriété orgName.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getOrgName() {
        return orgName;
    }

    /**
     * Définit la valeur de la propriété orgName.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setOrgName(JAXBElement<String> value) {
        this.orgName = value;
    }

    /**
     * Obtient la valeur de la propriété foreignSysOrgId.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getForeignSysOrgId() {
        return foreignSysOrgId;
    }

    /**
     * Définit la valeur de la propriété foreignSysOrgId.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setForeignSysOrgId(JAXBElement<String> value) {
        this.foreignSysOrgId = value;
    }

}
