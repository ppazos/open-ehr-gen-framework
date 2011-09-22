package hce.core.composition.content.entry

import hce.core.composition.content.ContentItem
import data_types.text.*

// TEST: para serializar DataValue a string y ahorrar joins
import com.thoughtworks.xstream.XStream

class Entry extends ContentItem {

    CodePhrase language
    CodePhrase encoding
    String codedLanguage
    String codedEncoding
    
    static transients = ['language', 'encoding']
    
    // FIXME: los atributos coded de Entry deben mapearse por las subclases de entry
    
    // Nuevo para calcular codedValue
    def beforeInsert() {
       // Para generar XML en una sola linea sin pretty print: http://stackoverflow.com/questions/894625/how-to-disable-pretty-printingwhite-space-newline-in-xstream
       // Interesante: http://www.xml.com/pub/a/2001/06/20/databases.html
       XStream xstream = new XStream()
       codedLanguage = xstream.toXML(language)
       codedEncoding = xstream.toXML(encoding)
    }
    def beforeUpdate() {
       XStream xstream = new XStream()
       codedLanguage = xstream.toXML(language)
       codedEncoding = xstream.toXML(encoding)
    }
    // Al reves
    def afterLoad() {
       XStream xstream = new XStream()
       if (codedLanguage) language = xstream.fromXML(codedLanguage)
       if (codedEncoding) encoding = xstream.fromXML(codedEncoding)
    }
    
    //PartyProxy subject;
    //PartyProxy provider;
    //ObjectRef workflowId;
    //static hasMany = [otherParticipations:Participation]

    static mapping = {
        //language cascade: "save-update"
        //encoding cascade: "save-update"
       
        // Intento por excepcion que tira al hacer run-app
        // Repeated column in mapping for entity: hce.core.composition.content.entry.Entry 
        // column: parent_id (should be mapped with insert="false" update="false")
        //parent column:"parent_id", insert:"false", update:"false"
    }

    static constraints = {
        //language (nullable: false)
        //encoding (nullable: false)
        //subject (nullable: false)
       codedLanguage (nullable: true)
        
        // No se porque cuando valida tiene estos en null,
        // pero luego los asigna y guarda bien.
        codedEncoding(nullable: true)
    }
}