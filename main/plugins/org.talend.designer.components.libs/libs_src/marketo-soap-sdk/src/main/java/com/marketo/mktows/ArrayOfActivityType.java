
package com.marketo.mktows;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour ArrayOfActivityType complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfActivityType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="activityType" type="{http://www.marketo.com/mktows/}ActivityType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfActivityType", propOrder = {
    "activityTypes"
})
public class ArrayOfActivityType {

    @XmlElement(name = "activityType")
    @XmlSchemaType(name = "string")
    protected List<ActivityType> activityTypes;

    /**
     * Gets the value of the activityTypes property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the activityTypes property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getActivityTypes().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ActivityType }
     * 
     * 
     */
    public List<ActivityType> getActivityTypes() {
        if (activityTypes == null) {
            activityTypes = new ArrayList<ActivityType>();
        }
        return this.activityTypes;
    }

}
