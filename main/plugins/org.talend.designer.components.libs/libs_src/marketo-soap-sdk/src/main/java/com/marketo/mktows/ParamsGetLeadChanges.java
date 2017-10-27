
package com.marketo.mktows;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour ParamsGetLeadChanges complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ParamsGetLeadChanges">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="startPosition" type="{http://www.marketo.com/mktows/}StreamPosition"/>
 *         &lt;element name="activityFilter" type="{http://www.marketo.com/mktows/}ActivityTypeFilter" minOccurs="0"/>
 *         &lt;element name="activityNameFilter" type="{http://www.marketo.com/mktows/}ArrayOfString" minOccurs="0"/>
 *         &lt;element name="batchSize" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="leadSelector" type="{http://www.marketo.com/mktows/}LeadSelector" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ParamsGetLeadChanges", propOrder = {
    "startPosition",
    "activityFilter",
    "activityNameFilter",
    "batchSize",
    "leadSelector"
})
@XmlRootElement(name = "paramsGetLeadChanges")
public class ParamsGetLeadChanges {

    @XmlElement(required = true)
    protected StreamPosition startPosition;
    @XmlElementRef(name = "activityFilter", type = JAXBElement.class, required = false)
    protected JAXBElement<ActivityTypeFilter> activityFilter;
    @XmlElementRef(name = "activityNameFilter", type = JAXBElement.class, required = false)
    protected JAXBElement<ArrayOfString> activityNameFilter;
    @XmlElementRef(name = "batchSize", type = JAXBElement.class, required = false)
    protected JAXBElement<Integer> batchSize;
    protected LeadSelector leadSelector;

    /**
     * Obtient la valeur de la propriété startPosition.
     * 
     * @return
     *     possible object is
     *     {@link StreamPosition }
     *     
     */
    public StreamPosition getStartPosition() {
        return startPosition;
    }

    /**
     * Définit la valeur de la propriété startPosition.
     * 
     * @param value
     *     allowed object is
     *     {@link StreamPosition }
     *     
     */
    public void setStartPosition(StreamPosition value) {
        this.startPosition = value;
    }

    /**
     * Obtient la valeur de la propriété activityFilter.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ActivityTypeFilter }{@code >}
     *     
     */
    public JAXBElement<ActivityTypeFilter> getActivityFilter() {
        return activityFilter;
    }

    /**
     * Définit la valeur de la propriété activityFilter.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ActivityTypeFilter }{@code >}
     *     
     */
    public void setActivityFilter(JAXBElement<ActivityTypeFilter> value) {
        this.activityFilter = value;
    }

    /**
     * Obtient la valeur de la propriété activityNameFilter.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfString }{@code >}
     *     
     */
    public JAXBElement<ArrayOfString> getActivityNameFilter() {
        return activityNameFilter;
    }

    /**
     * Définit la valeur de la propriété activityNameFilter.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link ArrayOfString }{@code >}
     *     
     */
    public void setActivityNameFilter(JAXBElement<ArrayOfString> value) {
        this.activityNameFilter = value;
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
     * Obtient la valeur de la propriété leadSelector.
     * 
     * @return
     *     possible object is
     *     {@link LeadSelector }
     *     
     */
    public LeadSelector getLeadSelector() {
        return leadSelector;
    }

    /**
     * Définit la valeur de la propriété leadSelector.
     * 
     * @param value
     *     allowed object is
     *     {@link LeadSelector }
     *     
     */
    public void setLeadSelector(LeadSelector value) {
        this.leadSelector = value;
    }

}
