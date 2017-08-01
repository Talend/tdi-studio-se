
package com.marketo.mktows;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour ParamsGetCustomObjects complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ParamsGetCustomObjects">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="objTypeName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="streamPosition" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="batchSize" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="customObjKeyList" type="{http://www.marketo.com/mktows/}ArrayOfAttribute" minOccurs="0"/>
 *         &lt;element name="includeAttributes" type="{http://www.marketo.com/mktows/}ArrayOfString"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ParamsGetCustomObjects", propOrder = {
    "objTypeName",
    "streamPosition",
    "batchSize",
    "customObjKeyList",
    "includeAttributes"
})
@XmlRootElement(name = "paramsGetCustomObjects")
public class ParamsGetCustomObjects {

    @XmlElement(required = true)
    protected String objTypeName;
    @XmlElementRef(name = "streamPosition", type = JAXBElement.class, required = false)
    protected JAXBElement<String> streamPosition;
    @XmlElementRef(name = "batchSize", type = JAXBElement.class, required = false)
    protected JAXBElement<Integer> batchSize;
    @XmlElementRef(name = "customObjKeyList", type = JAXBElement.class, required = false)
    protected JAXBElement<ArrayOfAttribute> customObjKeyList;
    @XmlElement(required = true)
    protected ArrayOfString includeAttributes;

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
     * Obtient la valeur de la propriété streamPosition.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getStreamPosition() {
        return streamPosition;
    }

    /**
     * Définit la valeur de la propriété streamPosition.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setStreamPosition(JAXBElement<String> value) {
        this.streamPosition = value;
    }

    /**
     * Obtient la valeur de la propriété batchSize.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public JAXBElement<Integer> getBatchSize() {
        return batchSize;
    }

    /**
     * Définit la valeur de la propriété batchSize.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public void setBatchSize(JAXBElement<Integer> value) {
        this.batchSize = value;
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
     * Obtient la valeur de la propriété includeAttributes.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfString }
     *     
     */
    public ArrayOfString getIncludeAttributes() {
        return includeAttributes;
    }

    /**
     * Définit la valeur de la propriété includeAttributes.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfString }
     *     
     */
    public void setIncludeAttributes(ArrayOfString value) {
        this.includeAttributes = value;
    }

}
