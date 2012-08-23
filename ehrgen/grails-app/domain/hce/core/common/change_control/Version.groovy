package hce.core.common.change_control

import support.identification.ObjectRef
import hce.core.common.archetyped.Locatable
import support.identification.ObjectVersionID // Todavia no se usa para nada, se usaria cuando se implemente el 'change control'
import data_types.quantity.date_time.DvDateTime
import hce.core.common.generic.AuditDetails
import hce.core.common.generic.*
import data_types.basic.DataValue

// TEST: para serializar DataValue a string y ahorrar joins
import com.thoughtworks.xstream.XStream

class Version {
   
    // FIXME: Por ahora y para simplificar, los lifecycleStates los defino aca, deberian estar en la terminologia openehr.
    // Estos corresponden con los estados Nuevo registro, Registro completo y Registro firmado de la maquina de estados
    // del registro clinico.
    static String STATE_INCOMPLETE = 'ehr.lifecycle.incomplete'
    static String STATE_COMPLETE = 'ehr.lifecycle.complete'
    static String STATE_SIGNED = 'ehr.lifecycle.signed'

    // FIXME: Este atributo es en realidad un atributo de AuditDetails.
    // Esta clase Version tiene una relacion a un AuditDetails que no esta 
    // implementada pero es necesario el atributo de la fecha para saber
    // cuando fue creada la nueva version, y asi saber cual es la ultima.
    DvDateTime timeCommited
    PartyProxy committer

    // FIXME: Por ahora se seguira actualizando el atributo timeCommited, pero luego
    // seria bueno dejarlo de utilizar y quitarlo ya que AuditDetails tiene un atributo
    // con el mismo proposito: time_committed.
    AuditDetails commit_audit
    
    String signature
    ObjectRef contribution
    
    // En la definicion es DvCodedText, la simplifico.
    // Los valores posibles son los de la terminologica openehr de lifecycleState
    /*
     * <group name="version lifecycle state">
		<concept id="532" rubric="complete"/>
		<concept id="553" rubric="incomplete"/>
		<concept id="523" rubric="deleted"/>
	   </group>
     */
    String lifecycleState = STATE_INCOMPLETE
    
    String canonicalForm // serializacion a string de data
    
    // Puede ser uno de FOLDER, COMPOSITION o PARTY
    // Por ahora solo se usa para composition
    Locatable data
    
    // No se va a usar todavia, se usara cuando se implemente el control de cambios.
    ObjectVersionID uid
    
    int numeroVers
    String nombreArchCDA
    
    
    String codedTimeCommited
    String codedContribution
    String codedUid
    static transients = ['timeCommited', 'contribution', 'uid']
    
    def beforeInsert() {
       // Para generar XML en una sola linea sin pretty print: http://stackoverflow.com/questions/894625/how-to-disable-pretty-printingwhite-space-newline-in-xstream
       // Interesante: http://www.xml.com/pub/a/2001/06/20/databases.html
       XStream xstream = new XStream()
       xstream.omitField(DataValue.class, "errors");
       codedTimeCommited = xstream.toXML(timeCommited)
       codedContribution = xstream.toXML(contribution)
       codedUid = xstream.toXML(uid)
    }
    def beforeUpdate() {
       XStream xstream = new XStream()
       xstream.omitField(DataValue.class, "errors");
       codedTimeCommited = xstream.toXML(timeCommited)
       codedContribution = xstream.toXML(contribution)
       codedUid = xstream.toXML(uid)
    }
    // Al reves
    def afterLoad() {
       XStream xstream = new XStream()
       if (codedTimeCommited) timeCommited = xstream.fromXML(codedTimeCommited)
       if (codedContribution) contribution = xstream.fromXML(codedContribution)
       if (codedUid) uid = xstream.fromXML(codedUid)
    }

    static constraints = {
        //uid(nullable:true)
        codedTimeCommited(nullable:true, maxSize:4096) // para que valide
        codedContribution(nullable:true, maxSize:4096)
        codedUid(nullable:true, maxSize:4096)
        
        canonicalForm(nullable:true)
        signature(nullable:true)
        //contribution(nullable:true)
        commit_audit(nullable:true)
        nombreArchCDA(nullable:true)
        data(nullable:true)
        committer(nullable:true)
    }
    static mapping = {
        //timeCommited cascade: "save-update"
        //table 'versn' // en pg version es una palabra clave
        commit_audit cascade: "save-update"
        data cascade: "save-update"
        committer cascade: "save-update"
        data column: "version_data" // en pg version es una palabra clave
    }

    def getNumVersion(){
        return numeroVers
    }
}