package hce.core.composition

import hce.core.common.archetyped.Locatable
import data_types.text.*
import hce.core.composition.content.*
import hce.core.common.generic.*
import java.util.List

// TEST: para serializar DataValue a string y ahorrar joins
import com.thoughtworks.xstream.XStream
import data_types.basic.DataValue

class Composition extends Locatable {

    /**
     * The composer is the person who was primarily responsible for the content
     * of the Composition. This is the identifier that should appear on the screen.
     * It could be a junior doctor who did all the work, even if not legally
     * responsible, or it could be a nurse,
     */
    PartyProxy composer
    EventContext context
    DvCodedText category
    CodePhrase territory
    CodePhrase language
	
    List content // Para que guarden en orden
    static hasMany = [content:ContentItem]

    String codedCategory
    String codedTerritory
    String codedLanguage
    static transients = ['category', 'territory', 'language']
    
    // Nuevo para calcular codedValue
    def beforeInsert() {
       // Para generar XML en una sola linea sin pretty print: http://stackoverflow.com/questions/894625/how-to-disable-pretty-printingwhite-space-newline-in-xstream
       // Interesante: http://www.xml.com/pub/a/2001/06/20/databases.html
       XStream xstream = new XStream()
       xstream.omitField(DataValue.class, "errors");
       codedCategory = xstream.toXML(category)
       codedTerritory = xstream.toXML(territory)
       codedLanguage = xstream.toXML(language)
       codedName = xstream.toXML(name) // atributo de locatable
       //println "Compo.codedLanguage: " + codedLanguage
    }
    def beforeUpdate() {
       XStream xstream = new XStream()
       xstream.omitField(DataValue.class, "errors");
       codedCategory = xstream.toXML(category)
       codedTerritory = xstream.toXML(territory)
       codedLanguage = xstream.toXML(language)
       codedName = xstream.toXML(name) // atributo de locatable
       //println "Compo.codedLanguage: " + codedLanguage
    }
    // Al reves
    def afterLoad() {
       XStream xstream = new XStream()
       if (codedCategory) category = xstream.fromXML(codedCategory)
       if (codedTerritory) territory = xstream.fromXML(codedTerritory)
       if (codedLanguage) language = xstream.fromXML(codedLanguage)
       if (codedName) name = xstream.fromXML(codedName) // atributo de locatable
    }
    
    static mapping = {
        composer cascade: "save-update"
        context cascade: "save-update"
        //category cascade: "save-update"
        //territory cascade: "save-update"
        //language cascade: "save-update"
        content cascade: "save-update"
        content column:'composition_content'
        //table 'composition'
    }

    static constraints = {
        //category (nullable: false)
        //territory (nullable: false)
        //language (nullable: false)
       codedCategory (nullable: true, maxSize:4096) // para que pase validacion
       codedTerritory (nullable: true, maxSize:4096)
       codedLanguage (nullable: true, maxSize:4096)

        //composer (nullable: false)
        composer (nullable: true) // permito que sea null para poder guardar la composition sin composer, pero al final deberia tener uno!
        context (nullable:true)
    }
}