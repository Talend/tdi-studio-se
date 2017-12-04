
package com.marketo.mktows;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour StaticListSelector complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="StaticListSelector">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.marketo.com/mktows/}LeadSelector">
 *       &lt;sequence>
 *         &lt;element name="staticListName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="staticListId" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StaticListSelector", propOrder = {
    "staticListName",
    "staticListId"
})
public class StaticListSelector
    extends LeadSelector
{

    @XmlElementRef(name = "staticListName", type = JAXBElement.class, required = false)
    protected JAXBElement<String> staticListName;
    @XmlElementRef(name = "staticListId", type = JAXBElement.class, required = false)
    protected JAXBElement<Integer> staticListId;

    /**
     * Obtient la valeur de la propriété staticListName.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getStaticListName() {
        return staticListName;
    }

    /**
     * Définit la valeur de la propriété staticListName.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setStaticListName(JAXBElement<String> value) {
        this.staticListName = value;
    }

    /**
     * Obtient la valeur de la propriété staticListId.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public JAXBElement<Integer> getStaticListId() {
        return staticListId;
    }

    /**
     * Définit la valeur de la propriété staticListId.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public void setStaticListId(JAXBElement<Integer> value) {
        this.staticListId = value;
    }

}
