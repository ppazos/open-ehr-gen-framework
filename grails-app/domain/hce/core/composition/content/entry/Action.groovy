package hce.core.composition.content.entry

import hce.core.datastructure.itemstructure.ItemStructure
import data_types.quantity.date_time.DvDateTime

// TEST: para serializar DataValue a string y ahorrar joins
import com.thoughtworks.xstream.XStream
import data_types.basic.DataValue

class Action extends CareEntry {

    DvDateTime time
    ItemStructure description
    //ISMTransition ismTransition
    //InstructionDetails instructionDetails

    String codedTime // Nuevo: donde guardar el string
    static transients = ['time'] // Nuevo: no quiero que se guarde value, quiero guardar codedValue
    
    // Nuevo para calcular codedValue
    def beforeInsert() {
       // Para generar XML en una sola linea sin pretty print: http://stackoverflow.com/questions/894625/how-to-disable-pretty-printingwhite-space-newline-in-xstream
       // Interesante: http://www.xml.com/pub/a/2001/06/20/databases.html
       XStream xstream = new XStream()
       xstream.omitField(DataValue.class, "errors");
       codedTime = xstream.toXML(time)
       codedName = xstream.toXML(name) // atributo de locatable
    }
    def beforeUpdate() {
       XStream xstream = new XStream()
       xstream.omitField(DataValue.class, "errors");
       codedTime = xstream.toXML(time)
       codedName = xstream.toXML(name) // atributo de locatable
    }
    // Al reves
    def afterLoad() {
       XStream xstream = new XStream()
       xstream.omitField(DataValue.class, "errors");
       if (codedTime) time = xstream.fromXML(codedTime)
       if (codedName) name = xstream.fromXML(codedName)
    }
    
    static mapping = {
        //time cascade: "save-update"
        description cascade: "save-update"
        description column: "action_description_id"
    }

    static constraints = {
        //time (nullable: false)
        description (nullable: false)
        //ismTransition (nullable: false)
        codedTime (nullable: true, maxSize:4096)
    }
}
