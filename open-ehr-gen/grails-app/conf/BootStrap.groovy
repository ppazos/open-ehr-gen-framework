
import demographic.*
import demographic.contact.*
import demographic.party.*
import demographic.identity.*
import demographic.role.*
import hce.core.support.identification.*
import authorization.*
import hce.core.common.change_control.Version
import hce.HceService
import tablasMaestras.*
import hce.core.data_types.quantity.date_time.*

// TEST Folder
import hce.core.common.directory.Folder
import hce.core.data_types.text.*
import hce.core.common.archetyped.Archetyped
import org.springframework.web.context.support.WebApplicationContextUtils
//import org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib // Para usar g.message

class BootStrap {

    def hceService
    
    // Reference to Grails application. Lo inyecta.
    def grailsApplication
    
    def init = { servletContext ->
     
        println ""
        println "======= +++++++++ ======="
        println "======= Bootstrap ======="
        println "======= +++++++++ ======="
        println ""
        
        // TEST Folder
        //def g = new org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib()
        def appContext = WebApplicationContextUtils.getWebApplicationContext( servletContext )
        def messageSource = appContext.getBean( 'messageSource' )
        
        def folder
        def domains = grailsApplication.config.domains
        domains.each { domain ->
           
           folder = new Folder(
              //name: new DvText(value: g.message(code: domain)),
              name: new DvText(value: messageSource.getMessage(domain, new Object[2], new Locale('es'))),
              path: domain,
              archetypeNodeId: "at0001",         // Inventado
              archetypeDetails: new Archetyped(  // Inventado
                archetypeId: 'ehr.domain',
                templateId: 'ehr.domain',
                rmVersion: '1.0.2'
              )
           )
           
           // FIXME: no esta salvando...
           // TODO: setear atributos de Locatable
           
           if (!folder.save())
           {
              println folder.errors
              println folder.name.errors
              println folder.archetypeDetails.errors
           }
        }
        // /TEST Folder
        
        // Correccion de reloj segun uso horario
        // http://groovy.codehaus.org/JN0545-Dates
        // Esto si lo corrige!!!!
        TimeZone.'default'= TimeZone.getTimeZone('GMT-03:00') //set the default time zone
        
     
        println " - START: Carga tablas maestras"
        
        // saco para acelerar la carga
        
        println "   - CIE 10..."
        def codigos = Cie10Trauma.getCodigos()
        codigos.each { codigo ->
           if (!codigo.save()) println codigo.errors
        }
        
        
        println "   - OpenEHR Concepts..."
        def oehr_concepts = OpenEHRConcept.getConcepts()
        oehr_concepts.each { concept ->
           if (!concept.save()) println concept.errors
        }
        
        println "   - Tipos de identificadores..."
        def identificadores = TipoIdentificador.getTipos()
        identificadores.each { id ->
           if (!id.save()) println id.errors
        }
        
        println "   - Motivos de consulta (idem tipos de evento)..."
        def eventos = MotivoConsulta.getTipos()
        eventos.each { evento ->
           if (!evento.save()) println evento.errors
        }
        
        println "   - Empresas emergencia movil..."
        def emergencias = EmergenciaMovil.getEmergencias()
        emergencias.each { emergencia ->
           if (!emergencia.save()) println emergencia.errors
        }
        
        println "   - Departamentos UY..."
        def departamentos = DepartamentoUY.getDepartamentos()
        departamentos.each { dpto ->
           if (!dpto.save()) println dpto.errors
        }
        
        println " - END: Carga tablas maestras"
        
        // TODO: no crear si ya existen
        
        // ----------------------------------------------------------------------------
        
        println " - Creacion de pacientes de prueba"
        
        def paciente = new Person()
        //paciente.addToIds( new UIDBasedID(root:'2.16.840.1.113883.2.14.2.1', value:'1234567') )
        //paciente.addToIds( new UIDBasedID(root:'2.16.840.1.113883.2.14.1.1.1.3.1.5.1', value:'6677') )
        paciente.addToIds( new UIDBasedID(value:'2.16.840.1.113883.2.14.2.1::1234567') )
        paciente.addToIds( new UIDBasedID(value:'2.16.840.1.113883.2.14.1.1.1.3.1.5.1::6677') )
        paciente.addToIdentities( new PersonName(primerNombre:'Pablo', primerApellido:'Pazos') )
        paciente.fechaNacimiento = new Date(81, 9, 24) // 24/10/1981
        paciente.type = "Persona" // FIXME: el type no se setea solo con el nombre de la clase? (Person)
        paciente.sexo = "M"
        if (!paciente.save()) println paciente.errors
        
        def pac2 = new Person()
        //pac2.addToIds( new UIDBasedID(root:'2.16.840.1.113883.2.14.2.4', value:'2345678') )
        //pac2.addToIds( new UIDBasedID(root:'2.16.840.1.113883.2.14.1.1.1.3.1.5.1', value:'3366') )
        pac2.addToIds( new UIDBasedID(value:'2.16.840.1.113883.2.14.2.4::2345678') )
        pac2.addToIds( new UIDBasedID(value:'2.16.840.1.113883.2.14.1.1.1.3.1.5.1::3366') )
        pac2.addToIdentities( new PersonName(primerNombre:'Leandro', primerApellido:'Carrasco') )
        pac2.fechaNacimiento = new Date(82, 10, 25)
        pac2.type = "Persona"
        pac2.sexo = "M"
        if (!pac2.save()) println pac2.errors
        
        def persona3 = new Person()
        //pac3.addToIds( new UIDBasedID(root:'2.16.840.1.113883.4.330.858', value:'6667778') )
        //pac3.addToIds( new UIDBasedID(root:'2.16.840.1.113883.2.14.1.1.1.3.1.5.1', value:'444') )
        persona3.addToIds( new UIDBasedID(value:'2.16.840.1.113883.4.330.858::6667778') )
        persona3.addToIds( new UIDBasedID(value:'2.16.840.1.113883.2.14.1.1.1.3.1.5.1::444') )
        persona3.addToIdentities( new PersonName(primerNombre:'Marta', primerApellido:'Stewart') )
        persona3.fechaNacimiento = new Date(83, 11, 26)
        persona3.type = "Persona"
        persona3.sexo = "F"
        if (!persona3.save()) println persona3.errors
        
        def persona4 = new Person()
        persona4.addToIds( new UIDBasedID(value:'2.16.840.1.113883.2.14.2.1::1234888') )
        persona4.addToIds( new UIDBasedID(value:'2.16.840.1.113883.2.14.1.1.1.3.1.5.1::44556') )
        persona4.addToIdentities( new PersonName(primerNombre:'Pablo', primerApellido:'Cardozo') )
        persona4.fechaNacimiento = new Date(85, 9, 24) // 24/10/1981
        persona4.type = "Persona"
        persona4.sexo = "M"
        if (!persona4.save()) println persona4.errors
        
        def persona5 = new Person()
        persona5.addToIds( new UIDBasedID(value:'2.16.840.1.113883.4.330.858::45687543') )
        persona5.addToIds( new UIDBasedID(value:'2.16.840.1.113883.2.14.1.1.1.3.1.5.1::2233445') )
        persona5.addToIdentities( new PersonName(primerNombre:'Marcos', primerApellido:'Carisma') )
        persona5.fechaNacimiento = new Date(80, 11, 26)
        persona5.type = "Persona"
        persona5.sexo = "M"
        if (!persona5.save()) println persona5.errors
        
        // Paciente con estudios imagenologicos en el CCServer local
        def persona6 = new Person()
        persona6.addToIds( new UIDBasedID(value:'2.16.840.1.113883.4.330.666::2178309') ) // id en el CCServer
        persona6.addToIdentities( new PersonName(primerNombre:'CT', primerApellido:'Mister') )
        persona6.type = "Persona"        
        if (!persona6.save()) println persona6.errors
        
        def persona_administrativo = new Person()
        persona_administrativo.addToIds( new UIDBasedID(value:'2.16.840.1.113883.2.14.2.1::3334442') )
        persona_administrativo.addToIdentities( new PersonName(primerNombre:'John', primerApellido:'Doe') )
        persona_administrativo.type = "Persona"        
        if (!persona_administrativo.save()) println persona_administrativo.errors
        
        // ROLES
        def role1 = new Role(timeValidityFrom:new Date(), type:Role.PACIENTE, performer:paciente)
        if (!role1.save()) println role1.errors
            
        def role2 = new Role(timeValidityFrom:new Date(), type:Role.PACIENTE, performer:pac2)
        if (!role2.save()) println role2.errors
            
        def role3 = new Role(timeValidityFrom:new Date(), type:Role.PACIENTE, performer:persona4)
        if (!role3.save()) println role3.errors
            
        def role4 = new Role(timeValidityFrom:new Date(), type:Role.PACIENTE, performer:persona5)
        if (!role4.save()) println role4.errors
            
        def role5 = new Role(timeValidityFrom:new Date(), type:Role.PACIENTE, performer:persona6)
        if (!role5.save()) println role5.errors
        
        // Medico
        def role6 = new Role(timeValidityFrom:new Date(), type:Role.MEDICO, performer:persona3) 
        if (!role6.save()) println role6.errors
        
        // Administrativo
        def role_adm = new Role(timeValidityFrom:new Date(), type:Role.ADMINISTRATIVO, performer:persona_administrativo) 
        if (!role_adm.save()) println role_adm.errors
        
        
        // LOGINS
        
        // Login para el medico   
        def login = new LoginAuth(user:'user', pass:'pass', person:persona3)
        if (!login.save())  println login.errors
        
        // Login para el adminsitrativo
        def login_adm = new LoginAuth(user:'adm', pass:'1234', person:persona_administrativo)
        if (!login_adm.save())  println login_adm.errors
        
        // /Creacion de pacientes
        
        
        /* saco episodio de prueba para no generar problemas...
        
        println " - Creacion de episodio de prueba"
        
        def composition = hceService.createComposition( '2010-01-08 01:23:32', 'El paciente ingresa con dolor en el tobillo' )

        // Agrego el autor a la composición
        //def arrayIds = persona3.ids.toArray()
        //hceService.setCompositionComposer(composition, arrayIds[0].getRoot(), arrayIds[0].getExtension())

        def uidAutor = new UIDBasedID(value:'2.16.840.1.113883.2.14.1.1.1.3.1.5.1::444')
        hceService.setCompositionComposer(composition, uidAutor.getRoot(), uidAutor.getExtension())

        if (!composition.save())
        {
            println "Error: " + composition.errors
        }
        
        // Crea la version inicial
        def version = new Version(
          data: composition,
          timeCommited: new DvDateTime(
            value: '2010-01-08 01:23:32'
          )
        )
        
        if (!version.save())
        {
            println "ERROR: " + version.errors
        }
        
        // /Creacion de episodio
        */
        
        println ""
        println "======= +++++++++ ======="
        println "======= /Bootstrap ======="
        println "======= +++++++++ ======="
        println ""
        
     }
     def destroy = {
     }
} 