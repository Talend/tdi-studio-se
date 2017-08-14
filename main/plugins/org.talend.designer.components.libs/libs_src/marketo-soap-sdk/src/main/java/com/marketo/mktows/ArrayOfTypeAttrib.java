
package com.marketo.mktows;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour ArrayOfTypeAttrib complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfTypeAttrib">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="typeAttrib" type="{http://www.marketo.com/mktows/}TypeAttrib" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfTypeAttrib", propOrder = {
    "typeAttribs"
})
public class ArrayOfTypeAttrib {

    @XmlElement(name = "typeAttrib")
    protected List<TypeAttrib> typeAttribs;

    /**
     * Gets the value of the typeAttribs property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the typeAttribs property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTypeAttribs().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TypeAttrib }
     * 
     * 
     */
    public List<TypeAttrib> getTypeAttribs() {
        if (typeAttribs == null) {
            typeAttribs = new ArrayList<TypeAttrib>();
        }
        return this.typeAttribs;
    }

}
