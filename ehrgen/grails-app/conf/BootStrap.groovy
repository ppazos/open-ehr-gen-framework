
import javax.management.relation.RoleInfo

import demographic.*
import demographic.contact.*
import demographic.party.*
import demographic.identity.*
import demographic.role.*
import support.identification.*
import authorization.*
import hce.core.common.change_control.Version
import hce.HceService
import tablasMaestras.*

//import hce.core.common.directory.Folder
import domain.Domain
import workflow.WorkFlow
import workflow.Stage
import data_types.text.*
import hce.core.common.archetyped.Archetyped
import org.springframework.web.context.support.WebApplicationContextUtils
//import org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib // Para usar g.message

// Crear compositions de prueba
import converters.DateConverter
import data_types.quantity.date_time.*

// Template
import templates.*
import templates.constraints.*
import templates.controls.*

import archetype.ArchetypeIndex
import archetype.ArchetypeManager
import archetype.walkthrough.*
import archetype.walkthrough.actions.SlotResolution

// Para hacer un MOCK de una sesion para setear el locale temporalmente, para generar las pantallas correctamente.
import org.springframework.web.context.request.RequestContextHolder
import org.codehaus.groovy.grails.web.context.ServletContextHolder
import org.springframework.web.context.support.WebApplicationContextUtils
import org.springframework.web.servlet.support.RequestContextUtils as RCU

import gui.GuiManager

import com.thoughtworks.xstream.XStream

class BootStrap {

   def hceService
   def guiCachingService
   
   // Reference to Grails application. Lo inyecta.
   def grailsApplication
    
   def init = { servletContext ->
     
      println ""
      println "======= +++++++++ ======="
      println "======= Bootstrap ======="
      println "======= +++++++++ ======="
      println ""
        
        
      println "- correccion de la TimeZone a la de Montevideo/Buenos Aires"
      // Correccion de reloj segun uso horario
      // http://groovy.codehaus.org/JN0545-Dates
      // Esto si lo corrige!!!!
      //TimeZone.'default' = TimeZone.getTimeZone('GMT-03:00') //set the default time zone
      TimeZone.'default' = TimeZone.getTimeZone('America/Montevideo') // Con este considera daylight savings (cambios de +/- una hora por anio)
        
        
        
      def templateManager = TemplateManager.getInstance()
      def archetypeManager = ArchetypeManager.getInstance()
      
      // Para crear los ARchetypeIndex necesito todos los arquetipos cargados
      archetypeManager.loadAll()
        
        
        // Parece que no puedo definir el locale sin estar en una sesion de verdad
        // Una SOLUCION podria ser que no se genere la GUI desde el bootstrap, sino que se genere desde una GUI de administracion.
        // ===================================================
        // MOCK del request temporal para usar el locale seleccionado que puede ser distinto al del sistema.
        //servletContext  = ServletContextHolder.getServletContext()
        /*
        def applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext)
        def requestAttributes = grails.util.GrailsWebUtil.bindMockWebRequest(applicationContext)
        
        // requestAttributes: ServletWebRequest: uri=;client=127.0.0.1
        println "requestAttributes: " + requestAttributes
        
        // no puedo llamarlo sin request
        println RCU.getLocale(requestAttributes.request) // en
        
        Locale.setDefault(new Locale('es'))
        println RCU.getLocale(requestAttributes.request) // en
        
        // getLocaleResolver tira null
        RCU.getLocaleResolver(requestAttributes.request).setLocale(requestAttributes.request, requestAttributes.response, new Locale('es'))
        */
        //
        // ===================================================
        
        // ==============
        // FIXME: aqui se crean folders por defecto, pero se debe permitir
        //        crear folders desde pantallas de config del framework.
        //        para los folders creados desde UI el nombre va a ser
        //        ingresado pero tambien se necesita ingresar un codigo
        //        unico para el dominio porque se usa para verificar autorizacion.
        // --------------
        
		
        // Dominios y compositions por defecto
        //def g = new org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib()
        //def appContext = WebApplicationContextUtils.getWebApplicationContext( servletContext )
        //def messageSource = appContext.getBean( 'messageSource' )

        def config_domains = grailsApplication.config.domains

        if (Domain.count() == 0) // Si no se crearon los folders...
        {
           config_domains.each { config_domain ->
              
              def domain = new Domain(
                 name: config_domain,
                 userDefined: true
              )
              
              /*
                 folder = new Folder(
                    // para verificar seguridad necesito tambien el codigo en el folder.
                    name: new DvCodedText(
                      value: messageSource.getMessage(domain, new Object[2], new Locale('es')), // FIXME: I18N
                      definingCode: new CodePhrase(
                        codeString: domain,
                        terminologyId: TerminologyID.create('ehrgen', null) // Deberia haber alguna terminologia identificado con ehrgen en el CtrlTerminologia al cual pueda pedir codigos y este codigo este incluido.
                      )
                    ),
                    path: domain,
                    archetypeNodeId: "at0001",        // FIXME: Inventado. Consultar si sirve de algo arquetipar un Folder... (no tiene estructura)
                    archetypeDetails: new Archetyped( // FIXME: Inventado
                      archetypeId: 'ehr.domain',
                      templateId: 'ehr.domain',
                      rmVersion: '1.0.2' // FIXME: deberia ser variable global de config
                    )
                 )
              */
                 
              if (!domain.save(flush:true))
              {
                 println domain.errors
              }
                 
              // =====================================================================================
              // Crea registro de prueba para cada dominio
              def startDate = DateConverter.toIso8601ExtendedDateTimeFormat( new Date() )
              def composition = hceService.createComposition( startDate, "bla bla bla" )
                 
              // ============================================================================
              // FIXME: no se usa la referencia desde el domain a la composition? VERIFICAR.
              // ============================================================================
              
              // Set parent
              composition.padre = domain           
      
              if (!composition.save())
              {
                  println "Error: " + composition.errors
              }
                 
              // Crea la version inicial
              def version = new Version(
                data: composition,
                timeCommited: new DvDateTime(
                  value: DateConverter.toIso8601ExtendedDateTimeFormat( new Date() )
                )
              )
                 
              if (!version.save())
              {
                     println "ERROR: " + version.errors
              }
              // =====================================================================================
           }
        }
        // /Dominios y compositions por defecto
        
     
        println " - START: Carga catalogos maestros"
        
        // saco para acelerar la carga
        
        println "   - CIE 10..."
        if (Cie10Trauma.count() == 0)
        {
           def codigos = Cie10Trauma.getCodigos()
           codigos.each { codigo ->
              if (!codigo.save()) println codigo.errors
           }
        }
        else
        {
           println "      ya estan cargados"
        }
        
        
        println "   - OpenEHR Concepts..."
        if (OpenEHRConcept.count() == 0)
        {
           def oehr_concepts = OpenEHRConcept.getConcepts()
           oehr_concepts.each { concept ->
              if (!concept.save()) println concept.errors
           }
        }
        else
        {
           println "      ya estan cargados"
        }
        
        println "   - Tipos de identificadores..."
        if (TipoIdentificador.count() == 0)
        {
           def identificadores = TipoIdentificador.getTipos()
           identificadores.each { id ->
              if (!id.save()) println id.errors
           }
        }
        else
        {
           println "      ya estan cargados"
        }
        
        if (MotivoConsulta.count() == 0)
        {
           println "   - Motivos de consulta (idem tipos de evento)..."
           def eventos = MotivoConsulta.getTipos()
           eventos.each { evento ->
              if (!evento.save()) println evento.errors
           }
        }
        else
        {
           println "      ya estan cargados"
        }
        
        /*
		  // TODO: Se usa?
        if (EmergenciaMovil.count() == 0)
        {
           println "   - Empresas emergencia movil..."
           def emergencias = EmergenciaMovil.getEmergencias()
           emergencias.each { emergencia ->
              if (!emergencia.save()) println emergencia.errors
           }
        }
        else
        {
           println "      ya estan cargados"
        }
        */
        
        if (DepartamentoUY.count() == 0)
        {
           println "   - Departamentos UY..."
           def departamentos = DepartamentoUY.getDepartamentos()
           departamentos.each { dpto ->
              if (!dpto.save()) println dpto.errors
           }
        }
        else
        {
           println "      ya estan cargados"
        }
        
        println " - END: Carga tablas maestras"
        
        // TODO: no crear si ya existen
        
        // ----------------------------------------------------------------------------
        
        println " - Creacion de personas de prueba"
        
        
        //
        Permit.createDefault() // controler/action
        //
        DomainPermit.createDefault() // domain/templateId
        //
        
        // ROLES: se crea una instancia por cada rol existente.
        // Luego el admin puede crear otros roles y asignar permisos.
        // TODO: crear un usuario para el rol GODLIKE
        def rGodLike = new Role(type: Role.GODLIKE)
        if (!rGodLike.save()) println rGodLike.errors
        
        def rAdmin = new Role(type: Role.ADMIN)
        if (!rAdmin.save()) println rAdmin.errors
        
        def rPaciente = new Role(type: Role.PACIENTE)
        if (!rPaciente.save()) println rPaciente.errors
        
        def rMedico = new Role(type: Role.MEDICO)
        if (!rMedico.save()) println rMedico.errors
        
        def rEnfermeria = new Role(type: Role.ENFERMERIA)
        if (!rEnfermeria.save()) println rEnfermeria.errors
        
        def rAdministrativo = new Role(type: Role.ADMINISTRATIVO)
        if (!rAdministrativo.save()) println rAdministrativo.errors

        
        // Los roleValidity se guardan al guardar las personas
        
        
        def paciente = new Person(primerNombre:'Pablo', primerApellido:'Pazos')
        
        paciente.addToIds( new UIDBasedID(value:'2.16.840.1.113883.2.14.2.1::1234567') )
        paciente.addToIds( new UIDBasedID(value:'2.16.840.1.113883.2.14.1.1.1.3.1.5.1::6677') )
        paciente.fechaNacimiento = new Date(81, 9, 24) // 24/10/1981
        paciente.type = "Persona" // FIXME: el type no se setea solo con el nombre de la clase? (Person)
        paciente.sexo = "M"
        def validityPac1 = new RoleValidity(performer: paciente, role: rPaciente)
        paciente.addToRoles(validityPac1)
        if (!paciente.save()) println paciente.errors
        
        def pac2 = new Person(primerNombre:'Leandro', primerApellido:'Carrasco')
        pac2.addToIds( new UIDBasedID(value:'2.16.840.1.113883.2.14.2.4::2345678') )
        pac2.addToIds( new UIDBasedID(value:'2.16.840.1.113883.2.14.1.1.1.3.1.5.1::3366') )
        pac2.fechaNacimiento = new Date(82, 10, 25)
        pac2.type = "Persona"
        pac2.sexo = "M"
        def validityPac2 = new RoleValidity(performer: pac2, role: rPaciente)
        pac2.addToRoles(validityPac2)
        if (!pac2.save()) println pac2.errors
        
        def persona3 = new Person(primerNombre:'Marta', primerApellido:'Doctora')
        persona3.addToIds( new UIDBasedID(value:'2.16.840.1.113883.4.330.858::6667778') )
        //persona3.addToIds( new UIDBasedID(value:'2.16.840.1.113883.2.14.1.1.1.3.1.5.1::444') ) // este es un id de paciente, si fuera paciente tambien deberia asociarle el rol
        persona3.fechaNacimiento = new Date(83, 11, 26)
        persona3.type = "Persona"
        persona3.sexo = "F"
        def validityMed1 = new RoleValidity(performer: persona3, role: rMedico)
        persona3.addToRoles(validityMed1)
        if (!persona3.save()) println persona3.errors
        
        def persona4 = new Person(primerNombre:'Pablo', primerApellido:'Cardozo')
        persona4.addToIds( new UIDBasedID(value:'2.16.840.1.113883.2.14.2.1::1234888') )
        persona4.addToIds( new UIDBasedID(value:'2.16.840.1.113883.2.14.1.1.1.3.1.5.1::44556') )
        persona4.fechaNacimiento = new Date(85, 9, 24) // 24/10/1981
        persona4.type = "Persona"
        persona4.sexo = "M"
        def validityPac3 = new RoleValidity(performer: persona4, role: rPaciente)
        persona4.addToRoles(validityPac3)
        if (!persona4.save()) println persona4.errors
        
        def persona5 = new Person(primerNombre:'Marcos', primerApellido:'Carisma')
        persona5.addToIds( new UIDBasedID(value:'2.16.840.1.113883.4.330.858::45687543') )
        persona5.addToIds( new UIDBasedID(value:'2.16.840.1.113883.2.14.1.1.1.3.1.5.1::2233445') )
        persona5.fechaNacimiento = new Date(80, 11, 26)
        persona5.type = "Persona"
        persona5.sexo = "M"
        def validityPac4 = new RoleValidity(performer: persona5, role: rPaciente)
        persona5.addToRoles(validityPac4)
        if (!persona5.save()) println persona5.errors
        
        // Paciente con estudios imagenologicos en el CCServer local
        def persona6 = new Person(primerNombre:'CT', primerApellido:'Mister')
        persona6.addToIds( new UIDBasedID(value:'2.16.840.1.113883.4.330.666::2178309') ) // id en el CCServer
        persona6.type = "Persona"
        def validityPac5 = new RoleValidity(performer: persona6, role: rPaciente)
        persona6.addToRoles(validityPac5)
        if (!persona6.save()) println persona6.errors
        
        def persona_administrativo = new Person(primerNombre:'Charles', primerApellido:'Administrativo')
        persona_administrativo.addToIds( new UIDBasedID(value:'2.16.840.1.113883.2.14.2.1::3334442') )
        persona_administrativo.type = "Persona"
        persona_administrativo.sexo = "M"
        def validityPac9 = new RoleValidity(performer: persona_administrativo, role: rAdministrativo)
        persona_administrativo.addToRoles(validityPac9)
        if (!persona_administrativo.save()) println persona_administrativo.errors
        
        def persona_enfermera = new Person(primerNombre:'Juana', primerApellido:'Enfermera')
        persona_enfermera.addToIds( new UIDBasedID(value:'2.16.840.1.113883.2.14.2.1::9876456') )
        persona_enfermera.type = "Persona"
        persona_enfermera.sexo = "F"
        def validityPac7 = new RoleValidity(performer: persona_enfermera, role: rEnfermeria)
        persona_enfermera.addToRoles(validityPac7)
        if (!persona_enfermera.save()) println persona_enfermera.errors
        
        def persona_admin = new Person(primerNombre:'The', primerApellido:'Admin')
        persona_admin.addToIds( new UIDBasedID(value:'2.16.840.1.113883.2.14.2.1::98607521') )
        persona_admin.type = "Persona"
        persona_admin.sexo = "M"
        def validityPac8 = new RoleValidity(performer: persona_admin, role: rAdmin)
        persona_admin.addToRoles(validityPac8)
        if (!persona_admin.save()) println persona_admin.errors
        
        
        // =============================================================
        // ASIGNACION DE PERMISOS POR DEFECTO
        
        // ROL MEDICO, ACCESO A TODOS LOS DOMINIOS y todos los templates
        // -------------------------------------------------------------
        DomainPermit.findAllByTemplateId("*").each {
        
           rMedico.addToDomainPermits(it)
        }
        
        rMedico.save()
        //
        // =============================================================
      
        // ====================================================================================
        // LOGINS
        //
        // Login para el medico   
        def login = new LoginAuth(user:'user', pass:'pass', person: persona3)
        if (!login.save()) println login.errors
        
        // Login para el adminsitrativo
        def login_adm = new LoginAuth(user:'adm', pass:'1234', person: persona_administrativo)
        if (!login_adm.save()) println login_adm.errors
        
        def login_enf = new LoginAuth(user:'enf', pass:'1111', person: persona_enfermera)
        if (!login_enf.save()) println login_enf.errors
        
        def login_admin = new LoginAuth(user:'admin', pass:'admin', person: persona_admin)
        if (!login_admin.save()) println login_admin.errors
        
        // /Creacion de personas
        // ====================================================================================

        
        //List domains = grailsApplication.config.domains // Ya esta definida mas arriba
        //String PS = System.getProperty("file.separator")
        
        
        // Arma workflows para cada domain
        Template template
        WorkFlow workflow
        Stage stage
        
        // Auxiliares para la generacion del HTML
        String templateId
        
        
        
        // Carga del repo todos los templates
        // Se usan para generar todas las guis de todos los dominios
        templateManager.loadAll()
        
        // ====================================================================
        // Generacion de gui
        guiCachingService.generateGUI( templateManager.getLoadedTemplates().values() as List )
        //
        // ====================================================================
        
        
        // Stage ->* Templates
        Map domainTemplates
        
        Domain.list().each { domain ->
           
           // Por defecto todo domain tiene un workflow y el
           //medico tiene acceso a ese workflow en todos los domains
           workflow = new WorkFlow(
              forRoles: [rMedico],
              owner: domain
           )
           
           // Agrego el workflow al domain
           domain.addToWorkflows( workflow )
           
           // Falta agregar las stages al workflow
           // y los templates a cada stage
           
           
           
           //println "Domain: $domain"
           domainTemplates = grailsApplication.config.templates2."$domain.name"
           
           println "Domain: "+ domain.name
           //println "domainTemplates: " + domainTemplates
           
           domainTemplates.each{ entry ->
              
              // entry.key es stage.name
              //println " - "+ entry.key
              //println " - entry.value: "+ entry.value // ['prehospitalario.v1', 'contexto_del_evento.v1']
              
              // Crea stages en el workflow por defecto del domain
              stage = new Stage(
                 owner: workflow,
                 name: entry.key // EVALUACION_PRIMARIA
              )
              // Agrego la stage al workflow
              workflow.addToStages( stage )
              
              // Falta agregar los templates de cada stage


              // Cada template dentro de una stage
              entry.value.each { subsection -> // via_aerea

                 templateId = "EHRGen-EHR-" + subsection // 'EHRGen-EHR-via_aerea.v1'
                 //println "templateId: " + templateId

                 template = templateManager.getTemplate( templateId )
                
                 if (!template)
                 {
                    println "ERROR: Verifique que el template $templateId esta en el repositorio"
                    return
                 }
                
                 // TEST
                 //def xstream = new XStream()
                 //def tlog = new File('template.log')
                 //tlog.append( xstream.toXML(template) + "\n\n" )
                 
                 
                 // Agrega el template a la stage actual del workflow
                 stage.addToRecordDefinitions( template )
              }
           }
           
           
           // Guarda en cascada workflow, stages y templates del domain
           if (!domain.save())
           {
              println domain.errors
              domain.workflows.each { wf ->
                 println wf.errors
                 wf.stages.each { stg ->
                    println stg.errors
                    stg.recordDefinitions.each { tpl ->
                       println tpl.errors
                    }
                 }
              }
           }
        }
        
        
        
        
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
        
        println "Create Archetype Indexes"
        createArchetypeIndexes()
        
        println ""
        println "======= +++++++++ ======="
        println "======= /Bootstrap ======="
        println "======= +++++++++ ======="
        println ""
        
   }
   def destroy = {
   }
   
   /**
    * Los indices a arquetipos en la db se usan para editar templates desde la gui.
    * Luego se usaran para otras tareas de gestion de arquetipos desde la gui.
    */
   def createArchetypeIndexes()
   {
      def index
      def slot_index
      def walk
      def result
      def man = ArchetypeManager.getInstance()
      def archetypes = man.getLoadedArchetypes() // Map archetypeId -> archetype
      archetypes.each { archetypeId, archetype ->

         // http://code.google.com/p/open-ehr-gen-framework/issues/detail?id=100
         // Garantiza que hay un index por arquetipo
         index = ArchetypeIndex.findByArchetypeId(archetypeId)
      
         if (!index)
         {
            index = new ArchetypeIndex(
               archetypeId: archetypeId,
               type: archetype.archetypeId.rmEntity.toLowerCase()
            )
         }
         
         
         // Busca slots usando el archetype walkthrough
         walk = new ArchetypeWalkthrough()
         walk.walthroughInit(archetype)
         walk.walthroughStart(
            [
               (walk.EVENT_SLOT): [new SlotResolution()]
            ],
            new WalkthroughResult())

         result = walk.walthroughResult()

         //println result as grails.converters.XML
         //println result.loadedArchetypes
         //println result.references [archId::path : [archId,...]] // Slots en el arquetipo raiz con los ids referenciados en cada path
         //println result.cache
         
         result.loadedArchetypes.each { archId, arch ->
         
            if (archId != archetypeId)
            {
               // http://code.google.com/p/open-ehr-gen-framework/issues/detail?id=100
               // Garantiza que hay un index por arquetipo
               slot_index = ArchetypeIndex.findByArchetypeId(archId)
               
               if(!slot_index)
               {
                  slot_index = new ArchetypeIndex(
                     archetypeId: archId,
                     type: arch.archetypeId.rmEntity.toLowerCase()
                  )
               }
               
               index.addToSlots( slot_index )
            }
         }
         
         // Guardo solo si valida, ej. no guarda indices de tipos structure o item
         if (index.validate())
         {
            index.save()
         }
         else
         {
            //println index.errors
         }
      }
   }
} 