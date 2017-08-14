
package com.marketo.mktows;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour ParamsGetLeadActivity complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ParamsGetLeadActivity">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="leadKey" type="{http://www.marketo.com/mktows/}LeadKey"/>
 *         &lt;element name="activityFilter" type="{http://www.marketo.com/mktows/}ActivityTypeFilter" minOccurs="0"/>
 *         &lt;element name="startPosition" type="{http://www.marketo.com/mktows/}StreamPosition" minOccurs="0"/>
 *         &lt;element name="batchSize" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ParamsGetLeadActivity", propOrder = {
    "leadKey",
    "activityFilter",
    "startPosition",
    "batchSize"
})
@XmlRootElement(name = "paramsGetLeadActivity")
public class ParamsGetLeadActivity {

    @XmlElement(required = true)
    protected LeadKey leadKey;
    @XmlElementRef(name = "activityFilter", type = JAXBElement.class, required = false)
    protected JAXBElement<ActivityTypeFilter> activityFilter;
    @XmlElementRef(name = "startPosition", type = JAXBElement.class, required = false)
    protected JAXBElement<StreamPosition> startPosition;
    @XmlElementRef(name = "batchSize", type = JAXBElement.class, required = false)
    protected JAXBElement<Integer> batchSize;

    /**
     * Obtient la valeur de la propriété leadKey.
     * 
     * @return
     *     possible object is
     *     {@link LeadKey }
     *     
     */
    public LeadKey getLeadKey() {
        return leadKey;
    }

    /**
     * Définit la valeur de la propriété leadKey.
     * 
     * @param value
     *     allowed object is
     *     {@link LeadKey }
     *     
     */
    public void setLeadKey(LeadKey value) {
        this.leadKey = value;
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
     * Obtient la valeur de la propriété startPosition.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link StreamPosition }{@code >}
     *     
     */
    public JAXBElement<StreamPosition> getStartPosition() {
        return startPosition;
    }

    /**
     * Définit la valeur de la propriété startPosition.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link StreamPosition }{@code >}
     *     
     */
    public void setStartPosition(JAXBElement<StreamPosition> value) {
        this.startPosition = value;
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

}
