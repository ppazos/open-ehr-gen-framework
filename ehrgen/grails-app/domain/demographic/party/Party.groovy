package demographic.party

import support.identification.*
import demographic.*
import demographic.contact.Contact
import demographic.identity.*

// TEST: para serializar DataValue a string y ahorrar joins
import com.thoughtworks.xstream.XStream

/**
 * @author Pablo Pazos Gutierrez (pablo.swp@gmail.com)
 */
class Party {

    // Tipo del party como "persona", "organización", etc.
    // Nombre del rol, por ejemplo, "medico general", "enfermera",
    // "ciudadano privado". Tomado del atributo de nombre heredado.
    String type = this.getClass().getSimpleName()


    // Las listas de datatypes y support tambien deben codificarse
    List<UIDBasedID> ids
    String codedIds
    
    def Party()
    {
       ids = []
    }
    def addToIds(UIDBasedID id)
    {
       ids.add(id)
    }
    
    // Nuevo para calcular codedValue
    def beforeInsert() {
       // Para generar XML en una sola linea sin pretty print: http://stackoverflow.com/questions/894625/how-to-disable-pretty-printingwhite-space-newline-in-xstream
       // Interesante: http://www.xml.com/pub/a/2001/06/20/databases.html
       XStream xstream = new XStream()
       xstream.omitField(ids.class, "errors");
       codedIds = xstream.toXML(ids)
    }
    def beforeUpdate() {
       XStream xstream = new XStream()
       xstream.omitField(ids.class, "errors");
       codedIds = xstream.toXML(ids)
    }
    // Al reves
    def afterLoad() {
       XStream xstream = new XStream()
       if (codedIds) ids = xstream.fromXML(codedIds)
    }
    
    static constraints = {
       codedIds(nullable:true, maxSize:4096) // Nullable para que pase el validador
    }
    
    // relationships: Relaciones de la cual este party es source
    // ids: identificadores del party (ObjectID es II en HL7) -- este campo iria en el campo details del modelo de OpenEHR.
    // contacts: medios de contacto 
    // identities: informacion para identificar al party
    static hasMany = [
                       relationships : PartyRelationship,
                       //ids: UIDBasedID, // FIXME: esto tiene que ser HierObjectId
                       contacts: Contact,
                       //identities: PartyIdentity // Esto se usaba para los nombres y apellidos de las personas y organizaciones,
                                                   // para simplificar pongo estos atributos en Person y Organization.
                     ]
    
    // Como PartyRel tiene 2 Party, tengo que decirle
    // con cual de esos se mapea la relacion 1-N 'relationships'
    // de Party en PartyRel: es el source.
    static mappedBy = [relationships:'source']
    static mapping = {
        ids cascade: "save-update"
        //identities cascade: "save-update"
        contacts cascade: "save-update"
    }   
}