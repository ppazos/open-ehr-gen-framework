package hce.core.composition.content.entry

import hce.core.datastructure.itemstructure.ItemStructure

// TEST: para serializar DataValue a string y ahorrar joins
import com.thoughtworks.xstream.XStream
import data_types.basic.DataValue

class Evaluation extends CareEntry{

    ItemStructure data;
    
    static mapping = {
        data column: "evaluation_data"
        data cascade: "save-update"
    }

    static constraints = {
        data (nullable:false)
    }
    
    // Nuevo para calcular codedValue
    def beforeInsert() {
       // Para generar XML en una sola linea sin pretty print: http://stackoverflow.com/questions/894625/how-to-disable-pretty-printingwhite-space-newline-in-xstream
       // Interesante: http://www.xml.com/pub/a/2001/06/20/databases.html
       XStream xstream = new XStream()
       xstream.omitField(DataValue.class, "errors");
       codedName = xstream.toXML(name) // atributo de locatable
    }
    def beforeUpdate() {
       XStream xstream = new XStream()
       xstream.omitField(DataValue.class, "errors");
       codedName = xstream.toXML(name) // atributo de locatable
    }
    // Al reves
    def afterLoad() {
       XStream xstream = new XStream()
       if (codedName) name = xstream.fromXML(codedName)
    }
}