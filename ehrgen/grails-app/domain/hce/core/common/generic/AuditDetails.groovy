package hce.core.common.generic

import data_types.quantity.date_time.*
import data_types.text.*

// TEST: para serializar DataValue a string y ahorrar joins
import com.thoughtworks.xstream.XStream
import data_types.basic.DataValue

class AuditDetails {

    // FIXME: Por ahora, para simplificar, los change_type los definimos aquí, deberían estar en la terminologia openehr.
    // Estos corresponden con los cambios por Correccion de Datos de Pacinete, Cambiod de Pacinete y Reapertura Episodio respectivamente.
    // del registro clinico.
    static String CHANGE_TYPE_CORRECCION_PACIENTE = 'ehr.version.change_type.correccionPacinete'
    static String CHANGE_TYPE_CAMBIO_PACIENTE = 'ehr.version.change_type.cambioPacinete'
    static String CHANGE_TYPE_REAPERTURA_EPISODIO = 'ehr.version.change_type.reaperturaEpisodio'

    String system_id            // Identity of the system where the change was committed. Ideally this is a machine- and human-processable identifier, but it may not be.
    PartyProxy committer        // Identity and optional reference into identity management service, of user who committed the item.
    DvDateTime time_committed   // Time of committal of the item.
    DvText description          // Reason for committal.
    //DvCodedText change_type   // Type of change. Coded using the openEHR Terminology “audit change type” group.
    String change_type          // FIXME: Representaremos al tipo de cambio (change_Type) con un String.
                                // Por Ahora manejamos los tipos de cambio definido como constantes:
                                // CHANGE_TYPE_CORRECCION_PACIENTE
                                // CHANGE_TYPE_CAMBIO_PACIENTE
                                // CHANGE_TYPE_REAPERTURA_EPISODIO
    
    String codedTimeCommited
    String codedDescription
    static transients = ['time_committed', 'description']
    
    
    // Nuevo para calcular codedValue
    def beforeInsert() {
       // Para generar XML en una sola linea sin pretty print: http://stackoverflow.com/questions/894625/how-to-disable-pretty-printingwhite-space-newline-in-xstream
       // Interesante: http://www.xml.com/pub/a/2001/06/20/databases.html
       XStream xstream = new XStream()
       xstream.omitField(DataValue.class, "errors");
       codedTimeCommited = xstream.toXML(time_committed)
       codedDescription = xstream.toXML(description)
    }
    def beforeUpdate() {
       XStream xstream = new XStream()
       xstream.omitField(DataValue.class, "errors");
       codedTimeCommited = xstream.toXML(time_committed)
       codedDescription = xstream.toXML(description)
    }
    // Al reves
    def afterLoad() {
       XStream xstream = new XStream()
       if (codedTimeCommited) time_committed = xstream.fromXML(codedTimeCommited)
       if (codedDescription) description = xstream.fromXML(codedDescription)
    }
    
    static mapping = {
        committer : "save-update"
        //time_committed: "save-update"
        //description : "save-update"
    }

    static constraints = {
        system_id (nullable: false, blank:false)
        //committer  (nullable: false)
        //time_committed (nullable: false)
    }
}