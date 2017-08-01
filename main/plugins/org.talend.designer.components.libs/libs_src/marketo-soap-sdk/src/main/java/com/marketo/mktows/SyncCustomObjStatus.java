
package com.marketo.mktows;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour SyncCustomObjStatus complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="SyncCustomObjStatus">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="objTypeName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="customObjKeyList" type="{http://www.marketo.com/mktows/}ArrayOfAttribute" minOccurs="0"/>
 *         &lt;element name="status" type="{http://www.marketo.com/mktows/}SyncStatusEnum"/>
 *         &lt;element name="error" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SyncCustomObjStatus", propOrder = {
    "objTypeName",
    "customObjKeyList",
    "status",
    "error"
})
public class SyncCustomObjStatus {

    @XmlElement(required = true)
    protected String objTypeName;
    @XmlElementRef(name = "customObjKeyList", type = JAXBElement.class, required = false)
    protected JAXBElement<ArrayOfAttribute> customObjKeyList;
    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected SyncStatusEnum status;
    @XmlElementRef(name = "error", type = JAXBElement.class, required = false)
    protected JAXBElement<String> error;

    /**
     * Obtient la valeur de la propriété objTypeName.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getObjTypeName() {
        return objTypeName;
    }

    /**
     * Définit la valeur de la propriété objTypeName.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setObjTypeName(String value) {
        this.objTypeName = value;
    }

    /**
     * Obtient la valeur de la propriété customObjKeyList.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfAttribute }{@code >}
     *     
     */
    public JAXBElement<ArrayOfAttribute> getCustomObjKeyList() {
        return customObjKeyList;
    }

    /**
     * Définit la valeur de la propriété customObjKeyList.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfAttribute }{@code >}
     *     
     */
    public void setCustomObjKeyList(JAXBElement<ArrayOfAttribute> value) {
        this.customObjKeyList = value;
    }

    /**
     * Obtient la valeur de la propriété status.
     * 
     * @return
     *     possible object is
     *     {@link SyncStatusEnum }
     *     
     */
    public SyncStatusEnum getStatus() {
        return status;
    }

    /**
     * Définit la valeur de la propriété status.
     * 
     * @param value
     *     allowed object is
     *     {@link SyncStatusEnum }
     *     
     */
    public void setStatus(SyncStatusEnum value) {
        this.status = value;
    }

    /**
     * Obtient la valeur de la propriété error.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getError() {
        return error;
    }

    /**
     * Définit la valeur de la propriété error.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setError(JAXBElement<String> value) {
        this.error = value;
    }

}
