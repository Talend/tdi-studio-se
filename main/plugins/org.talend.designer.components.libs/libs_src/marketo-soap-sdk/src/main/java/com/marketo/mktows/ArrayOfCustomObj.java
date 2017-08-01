
package com.marketo.mktows;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour ArrayOfCustomObj complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfCustomObj">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="customObj" type="{http://www.marketo.com/mktows/}CustomObj" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfCustomObj", propOrder = {
    "customObjs"
})
public class ArrayOfCustomObj {

    @XmlElement(name = "customObj")
    protected List<CustomObj> customObjs;

    /**
     * Gets the value of the customObjs property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the customObjs property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCustomObjs().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CustomObj }
     * 
     * 
     */
    public List<CustomObj> getCustomObjs() {
        if (customObjs == null) {
            customObjs = new ArrayList<CustomObj>();
        }
        return this.customObjs;
    }

}
