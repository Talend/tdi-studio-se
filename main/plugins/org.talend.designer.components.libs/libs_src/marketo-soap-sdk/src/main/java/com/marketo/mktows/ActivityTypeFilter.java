
package com.marketo.mktows;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour ActivityTypeFilter complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ActivityTypeFilter">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="includeTypes" type="{http://www.marketo.com/mktows/}ArrayOfActivityType" minOccurs="0"/>
 *         &lt;element name="excludeTypes" type="{http://www.marketo.com/mktows/}ArrayOfActivityType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ActivityTypeFilter", propOrder = {
    "includeTypes",
    "excludeTypes"
})
public class ActivityTypeFilter {

    protected ArrayOfActivityType includeTypes;
    protected ArrayOfActivityType excludeTypes;

    /**
     * Obtient la valeur de la propriété includeTypes.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfActivityType }
     *     
     */
    public ArrayOfActivityType getIncludeTypes() {
        return includeTypes;
    }

    /**
     * Définit la valeur de la propriété includeTypes.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfActivityType }
     *     
     */
    public void setIncludeTypes(ArrayOfActivityType value) {
        this.includeTypes = value;
    }

    /**
     * Obtient la valeur de la propriété excludeTypes.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfActivityType }
     *     
     */
    public ArrayOfActivityType getExcludeTypes() {
        return excludeTypes;
    }

    /**
     * Définit la valeur de la propriété excludeTypes.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfActivityType }
     *     
     */
    public void setExcludeTypes(ArrayOfActivityType value) {
        this.excludeTypes = value;
    }

}
