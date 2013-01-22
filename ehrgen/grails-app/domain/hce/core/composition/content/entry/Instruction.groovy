package hce.core.composition.content.entry

import java.util.List

import data_types.text.DvText
import data_types.quantity.date_time.DvDateTime
import data_types.encapsulated.DvParsable

// TEST: para serializar DataValue a string y ahorrar joins
import com.thoughtworks.xstream.XStream
import data_types.basic.DataValue

class Instruction extends CareEntry {

    DvText narrative
    DvDateTime expiryTime
    DvParsable wfDefinition
    
    String codedNarrative
    String codedExpirityTime
    String codedWfDefinition
    
    static transients = ['narrative', 'expiryTime', 'wfDefinition']
    
    // Nuevo para calcular codedValue
    def beforeInsert() {
       // Para generar XML en una sola linea sin pretty print: http://stackoverflow.com/questions/894625/how-to-disable-pretty-printingwhite-space-newline-in-xstream
       // Interesante: http://www.xml.com/pub/a/2001/06/20/databases.html
       XStream xstream = new XStream()
       xstream.omitField(DataValue.class, "errors");
       codedNarrative = xstream.toXML(narrative)
       codedExpirityTime = xstream.toXML(expiryTime)
       codedWfDefinition = xstream.toXML(wfDefinition)
       codedName = xstream.toXML(name) // atributo de locatable
    }
    def beforeUpdate() {
       XStream xstream = new XStream()
       xstream.omitField(DataValue.class, "errors");
       codedNarrative = xstream.toXML(narrative)
       codedExpirityTime = xstream.toXML(expiryTime)
       codedWfDefinition = xstream.toXML(wfDefinition)
       codedName = xstream.toXML(name) // atributo de locatable
    }
    // Al reves
    def afterLoad() {
       XStream xstream = new XStream()
       if (codedNarrative) narrative = xstream.fromXML(codedNarrative)
       if (codedExpirityTime) expiryTime = xstream.fromXML(codedExpirityTime)
       if (codedWfDefinition) wfDefinition = xstream.fromXML(codedWfDefinition)
       if (codedName) name = xstream.fromXML(codedName)
    }
	
	 List activities // Para que guarden en orden
    static hasMany = [activities:Activity]

    static mapping = {
        //narrative cascade: "save-update"
        //expiryTime cascade: "save-update"
        //wfDefinition cascade: "save-update"
        activities cascade: "save-update"
    }

    static constraints = {
        //narrative (nullable: false)
        // FIXME: deberia hacer el chequeo a mano porque me deja pasar si el narrative es null
        codedNarrative (nullable: true, maxSize:4096) // para que valide, pero no deberia ser nulo
        codedExpirityTime (nullable: true)
        codedWfDefinition (nullable: true)
    }
}