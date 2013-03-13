/*
Copyright 2013 CaboLabs.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

This software was developed by Pablo Pazos at CaboLabs.com

This software uses the openEHR Java Ref Impl developed by Rong Chen
http://www.openehr.org/wiki/display/projects/Java+Project+Download

This software uses MySQL Connector for Java developed by Oracle
http://dev.mysql.com/downloads/connector/j/

This software uses PostgreSQL JDBC Connector developed by Posrgresql.org
http://jdbc.postgresql.org/

This software uses XStream library developed by Jörg Schaible
http://xstream.codehaus.org/
*/
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
import domain.Admission
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

//import archetype.ArchetypeIndex
import archetype.ArchetypeManager
//import archetype.walkthrough.*
//import archetype.walkthrough.actions.SlotResolution

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
      
      // Para crear los ArchetypeIndex necesito todos los arquetipos cargados
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
      
      
      // ====================================================================================
      // Crea dominios
      //
      def config_domains = grailsApplication.config.domains

      println " - Creacion de dominios"
      
      if (Domain.count() == 0) // Si no se crearon los folders...
      {
         config_domains.each { config_domain ->
            
            println "   - $config_domain"
            
            def domain = new Domain(
               name: config_domain,
               userDefined: true
            )
                 
            if (!domain.save(flush:true))
            {
               println domain.errors
            }
         }
      }
      //
      // /Crea dominios
      // ====================================================================================
      
      
      // TODO: no crear si ya existen
      // ---------------------------  PERSONAS Y ROLES  ----------------------
      println " - Creacion de personas de prueba"
        
      //
      //Permit.createDefault() // controler/action
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
        
      // 24/10/1981
      def paciente = createPerson('Pablo','Pazos',
                                  '2.16.840.1.113883.2.14.2.1::1234567',
                                  '2.16.840.1.113883.2.14.1.1.1.3.1.5.1::6677',
                                  new Date(81, 9, 24), 'M', rPaciente)
        
      def pac2 = createPerson('Leandro','Carrasco',
                              '2.16.840.1.113883.2.14.2.1::2345678',
                              '2.16.840.1.113883.2.14.1.1.1.3.1.5.1::3366',
                              new Date(82, 10, 25), 'M', rPaciente)
      // 24/10/1985
      def persona4 = createPerson('Pablo','Cardozo',
                                  '2.16.840.1.113883.2.14.2.1::1234888',
                                  '2.16.840.1.113883.2.14.1.1.1.3.1.5.1::44556',
                                  new Date(85, 9, 24), 'M', rPaciente)
        
      def persona5 = createPerson('Marcos','Carisma',
                                  '2.16.840.1.113883.2.14.2.1::45687543',
                                  '2.16.840.1.113883.2.14.1.1.1.3.1.5.1::2233445',
                                  new Date(80, 11, 26), 'M', rPaciente)
        
      // Paciente con estudios imagenologicos en el CCServer local
      // id en el CCServer
      def persona6 = createPerson('CT','Mister',
                                  '2.16.840.1.113883.4.330.666::2178309',
                                  null,
                                  null, 'M', rPaciente)
        
      // ex persona3
      def doctor1 = createPerson('Marta','Doctora',
                                 '2.16.840.1.113883.4.330.858::6667778',
                                 null,
                                 new Date(83, 11, 26), 'F', rMedico)

      def persona_administrativo = createPerson('Charles','Administrativo',
                                    '2.16.840.1.113883.2.14.2.1::3334442',
                                    null,
                                    null, 'M', rAdministrativo)

      def persona_enfermera = createPerson('Juana','Enfermera',
                                    '2.16.840.1.113883.2.14.2.1::9876456',
                                    null,
                                    null, 'F', rEnfermeria)
        
      def persona_admin = createPerson('Joe','Admin',
                                    '2.16.840.1.113883.2.14.2.1::98607521',
                                    null,
                                    null, 'M', rAdmin)

        
        
      // =============================================================
      // CREA ADMISION DE LOS PACIENTES AL DOMINIO DE TRAUMA
      def admissions = []
      def trauma_domain = Domain.findByName('Emergencia de Trauma') // CUIDADO: nombre en Config.groovy puede cambiar!
        
      // Personas con rol paciente
      def rvs = RoleValidity.withCriteria {
        role {
          eq('type', Role.PACIENTE)
        }
      }
        
      // Para cada persona con rol paciente
      rvs.performer.each { person ->
        
         admissions << new Admission(
           patientId: person.id,
           physicianId: doctor1.id,
           domainId: trauma_domain.id
         )
      }
        
      // Guarda admisiones
      admissions.each { admission ->
         if (!admission.save()) println admission.errors
      }
        
      //
      // =============================================================
        
      // =============================================================
      // ASIGNACION DE PERMISOS POR DEFECTO
      // ROL MEDICO, ACCESO A TODOS LOS DOMINIOS y todos los templates
      // -------------------------------------------------------------
      DomainPermit.findAllByTemplateId("*").each {
        
         rMedico.addToDomainPermits(it)
      }
      rMedico.save()
      
      // ====================================================================================
      // LOGINS
      //
      // Login para el medico   
      def login = new LoginAuth(user:'med', pass:'med', person: doctor1)
      if (!login.save()) println login.errors
      
      // Login para el administrativo
      def login_adm = new LoginAuth(user:'adm', pass:'adm', person: persona_administrativo)
      if (!login_adm.save()) println login_adm.errors
      
      def login_enf = new LoginAuth(user:'enf', pass:'enf', person: persona_enfermera)
      if (!login_enf.save()) println login_enf.errors
      
      def login_admin = new LoginAuth(user:'admin', pass:'admin', person: persona_admin)
      if (!login_admin.save()) println login_admin.errors
        
      // /Creacion de personas
      // ====================================================================================
      
      // =====================================================================
      // Crea workflows para cada domain
      
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
      
      // Stage ->* Templates
      Map domainTemplates
      
      Domain.list().each { domain ->
         
         // Por defecto todo domain tiene un workflow y el
         //medico tiene acceso a ese workflow en todos los domains
         workflow = new WorkFlow(
            forRoles: [rMedico], // Cuidado, este es el rol medico de UN usuario, si se crea mas de un usuario medico aca, se deberian poner todos los roles medicos de cada usuario (el rol es por instancia!)
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
      } // each domain
        
      // /Crea workflows para cada domain
      // ====================================================================================
      
      // ====================================================================================
      // Crea compositions
      //

      def partySelf
      def participation
      def startDate
      def composition
      
      Domain.list().each { domain ->

         // =====================================================================================
         // Crea N registros por dominio, la mitad los asigna a un paciente
              
         (1..30).each { i ->
                 
            //println " - Crea COMPOSITION $i"
              
            // Crea registro de prueba para cada dominio
            //startDate = DateConverter.toIso8601ExtendedDateTimeFormat( new Date() )
            
            // TODO: usar textos medicos para la descripcion, ver lista de evoluciones
            //composition = hceService.createComposition( startDate, "bla bla bla", domain.workflows[0].id )
            composition = hceService.createComposition( new Date(), "bla bla bla", domain.workflows[0].id )
            
            // ============================================================================
            // TODO: no se usa la referencia desde el domain a la composition? VERIFICAR.
            // ============================================================================
              
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
            
            
            // TODO: crear un generador de contenido basado en los arquetipos de los templates
            //       del wf que se usa para crear la composition, asi las compositions cerradas
            //       tienen algo de informacion.
            //       deberia crear path/values basados en las restricciones del arquetipo
            //       y enviarselas al binder.
            
            
            // Se pone aca porque necesita que la composition este guardada, tambien necesita la version...
            // Los registros creados aqui estan cerrados
            // idem records.signRecord
            // login es la Authorization del medico
            def person = login.person
            def id = person.ids[0]
            //if ( !hceService.closeComposition(composition, DateConverter.toIso8601ExtendedDateTimeFormat(new Date())) )
            if ( !hceService.closeComposition(composition, new Date()) )
            {
               println "error: no se pudo cerrar la composition"
            }
            if (!hceService.setCompositionComposer(composition, id.root, id.extension))
            {
               println "error: no se pudo setear el composer"
            }
            //def version = Version.findByData( composition )
            version.lifecycleState = Version.STATE_SIGNED
            version.save()
         
              
            // La mitad de los registros los asigna a un paciente
            if (i % 2 == 0)
            {
               // paciente es una Person
               partySelf = hceService.createPatientPartysSelf(paciente.ids[0].root, paciente.ids[0].extension)
               participation = hceService.createParticipationToPerformer( partySelf )
               composition.context.addToParticipations( participation )
            }
              
            
            // /Crear registro
         }
      }
      // /Dominios y compositions por defecto
        
     
        println " - START: Carga catalogos maestros"
        
        // saco para acelerar la carga
        /*
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
        */
        
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
        


        
        
        
        
        
        /* saco episodio de prueba para no generar problemas...
        
        println " - Creacion de episodio de prueba"
        
        def composition = hceService.createComposition( '2010-01-08 01:23:32', 'El paciente ingresa con dolor en el tobillo' )

        // Agrego el autor a la composición
        //def arrayIds = doctor1.ids.toArray()
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
        archetypeManager.createArchetypeIndexes()
        
        println ""
        println "======= +++++++++ ======="
        println "======= /Bootstrap ======="
        println "======= +++++++++ ======="
        println ""
        
   }
   def destroy = {
   }
   
   def createPerson(String pn, String pa, String id1, String id2, Date dob, String sexo, Role role)
   {
      def p = new Person(primerNombre:pn, primerApellido:pa)
      p.addToIds( new UIDBasedID(value:id1) )
      
      if (id2) p.addToIds( new UIDBasedID(value:id2) )
      
      p.type = "Persona" // Por defecto se setea Person ...
      p.sexo = sexo
      
      if (dob) p.fechaNacimiento = dob
      
      def roleValidity = new RoleValidity(performer: p, role: role)
      p.addToRoles(roleValidity)
      
      if (!p.save()) println p.errors
      
      return p
   }
   
   /**
    * Los indices a arquetipos en la db se usan para editar templates desde la gui.
    * Luego se usaran para otras tareas de gestion de arquetipos desde la gui.
    */
   /*
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
   */
} 