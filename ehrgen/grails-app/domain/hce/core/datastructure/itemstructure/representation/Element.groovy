package hce.core.datastructure.itemstructure.representation

import hce.core.common.archetyped.Pathable;
import data_types.basic.*;
import data_types.text.*;

// TEST: para serializar DataValue a string y ahorrar joins
import com.thoughtworks.xstream.XStream

class Element extends Item {

    // FIXME: por ahora no soportamos null_flavor, lo podemos sacar.
    DvCodedText null_flavor // flavour of null value, e.g. indeterminate, not asked etc 
    String codedNullFlavor
    
    // TEST
    // Prueba de guardar cualquier DataValue como un String XML
    DataValue value // Esta queda igual que antes
    String codedValue // Nuevo: donde guardar el string
    static transients = ['value', 'null_flavor'] // Nuevo: no quiero que se guarde value, quiero guardar codedValue
    
    // Nuevo para calcular codedValue
    def beforeInsert() {
       // Para generar XML en una sola linea sin pretty print: http://stackoverflow.com/questions/894625/how-to-disable-pretty-printingwhite-space-newline-in-xstream
       // Interesante: http://www.xml.com/pub/a/2001/06/20/databases.html
       XStream xstream = new XStream()
       xstream.omitField(DataValue.class, "errors");
       codedValue = xstream.toXML(value)
       codedNullFlavor = xstream.toXML(null_flavor)
       codedName = xstream.toXML(name) // atributo de locatable
    }
    def beforeUpdate() {
       XStream xstream = new XStream()
       xstream.omitField(DataValue.class, "errors");
       codedValue = xstream.toXML(value)
       codedNullFlavor = xstream.toXML(null_flavor)
       codedName = xstream.toXML(name) // atributo de locatable
    }
    // Al reves
    def afterLoad() {
       XStream xstream = new XStream()
       if (codedValue) value = xstream.fromXML(codedValue)
       if (codedNullFlavor) null_flavor = xstream.fromXML(codedNullFlavor)
       if (codedName) name = xstream.fromXML(codedName)
    }
 

    static mapping = {
        //null_flavor cascade: "save-update"
        
        // TEST: value no se salva
        //value cascade: "save-update"
        codedValue column: "element_coded_value"
    }

    static constraints = {
       // TEST: la restriccion deberia ser sobre codedValue 
       value (nullable: false)
       // FIXME: debe tener value o nullflavor
       codedValue (maxSize: 10485760, nullable: true) // 1024*1024*10 // FIXME: si subo imagenes esto se puede quedar corto!
       codedNullFlavor (maxSize: 4096, nullable: true)
    }
    
    /*
    static transients = ['padre']
    Pathable getPadre()
    {
       if (!this.rmParentId) return null
       return Pathable.get(this.rmParentId)
    }
    */
}