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
import hce.core.common.change_control.Version
import hce.core.composition.* // Composition y EventContext
import data_types.quantity.date_time.*
import converters.DateConverter
import demographic.role.Role
import demographic.role.RoleValidity
import data_types.encapsulated.DvMultimedia
import org.codehaus.groovy.grails.commons.ApplicationHolder
import cda.*
import util.*

//import hce.core.common.directory.Folder
import domain.Domain
import support.identification.ObjectID
import support.identification.ObjectRef

import workflow.WorkFlow

import templates.*

/**
 * @author Pablo Pazos Gutierrez (pablo.pazos@cabolabs.com)
 */
class RecordsController {

   def demographicService
   def authorizationService
   def hceService
   
   // Para generar GUI en recordAction
   def guiCachingService
   
   def config = ApplicationHolder.application.config
   
   def index = {

      redirect(action:'list')
   }
   
   // ===========================================================
   // SOPORTE INICIAL DE ORDENES
   //
   /**
    * Lista instrucciones para alguno de los roles del usuario logueado, dentro del dominio seleccionado.
    * Esta accion por ahora se llama desde la vista de list con "include".
    */
   def listInstructions = {
      
      // FIXME: a la consulta le falta verificar si las instrucciones que lista son para
      //      alguno de mis roles y puede mostrar instrucciones que no sean para mi.
      
      
      // Obtener los InstExec mas nueva para la misma Instruction
      // ESTO NO SE PUEDE HACER CON withCriteria!!!
      // > InstExec, que no hay otro con mismo instructionId y mayor dateCreated (obtengo el que tiene mayor dateCreated)
      def instructionExecs = workflow.InstructionExecution.findAll( 
       "FROM InstructionExecution ie " +
       "WHERE ie.domainId = ? AND " +
       "     NOT EXISTS( " +
       "      SELECT ie2.id " +
       "      FROM InstructionExecution ie2 " +
       "      WHERE ie2.instructionId = ie.instructionId AND ie2.dateCreated > ie.dateCreated " +
       "     )",
       [session.ehrSession.domainId]
      )
      
      println instructionExecs
      println instructionExecs.activity
      
      // TODOs:
      // - crear metodo en InstrExec para obtener la Instruction y la Activity
      // - obtener los arquetipos de accion para cada actividad en ejecucion,
      //   para ver que careflow_steps se pueden ejecutar desde el estado actual de la activity.
   
   
      // Con cada ACTIVITY.action_archetype_id, le pido al ArchetypeManager los arquetipos que matcheen
      // 
      
      // ------------------------------------------------------
      // Prueba por el "Problema (*)" de mas abajo.
      // Definicion de la ISM (estado actual y estados a los que se puede trascender)
      def ism = [
       'planned':   ['planned','cancelled','active','completed','postponed','scheduled'],
       'scheduled': ['scheduled','active','postponed','cancelled'],
       'active':   ['active','aborted','suspended','completed','expired'],
       'completed': [],
       'postponed': ['postponed','planned','scheduled','expired'],
       'cancelled': [],
       'suspended': ['suspended','active','expired','aborted'],
       'aborted':   [],
       'expired':   ['completed','aborted','cancelled']
      ]
      // En el arquetipo de action estan los codigos, no los nombres.
      def state_codes = [
       'planned':   '526',
       'scheduled': '529',
       'active':    '245',
       'completed': '532',
       'postponed': '527',
       'cancelled': '528',
       'suspended': '530',
       'aborted':   '531',
       'expired':   '533'
      ]
      // ------------------------------------------------------
      
      def activityActions = [:]
      //def transitions = [:]
      def activity
      def archetypes
      def attr
      def possibleStates
      /*
      Lo que en realidad quiero son>
      - los careflow steps de cada arquetipo de actios referenciado desde la activity
      - filtrados por sus estados asociados
      - donde alguno de esos estados debe ser un estado siguiente al estado actual
        - (haber una transicion desde el estado actual a uno de los codigos en current_state.codeList())
      */
      instructionExecs.each { instExec ->
      
        activity = instExec.activity
      
        // 2)
        // Map <instExec.id , Maps<archId, List<careflow step>>>
        activityActions[instExec.id] = []
      
        // Deberia haber por lo menos uno que matchee
        // http://code.google.com/p/open-ehr-gen-framework/issues/detail?id=107
        archetypes = archetype.ArchetypeManager.getInstance().getArchetypes('action', activity.action_archetype_id)

        
        // Estados a los que puedo ir desde el estado actual
        possibleStates = ism[instExec.state]
        println "Desde "+ instExec.state +" puedo ir a los estados " + possibleStates
        
        // 2)
        // Lista de careflow steps que se pueden ejecutar del arquetipo de action,
        // para el estado actual de la actiity.
        // carefow steps los values.
        def arch_careflow_steps
          
        // Test para obtener la ISM Transition correcta
        // ism_transition es un atributo de Action
        archetypes.each { arch ->
        
          // 2)
          arch_careflow_steps = []
        
        
          // ACTION<CCO> -> ism_transition<ATTR> -> ISM_TRANSITION<CCO> -> (careflow_step, current_state)
          // Problema (*): sabiendo el estado actual, obtener cuales ISM_TRANSITIONs puedo hacer
          // (ISM_T.current_state son los estados siguientes, no el estado actual)
          // Necesito la ISM definida para que dado el estado actual, me diga a que
          // estados puedo ir, y obtener del arquetipo las ISM_T de esos estados a los que puedo ir.
          attr = arch.definition.attributes.find{ cattr -> cattr.rmAttributeName == "ism_transition"}
          
          println attr.rmAttributeName
          println "Tiene " + attr.children.size() + " ISM_TRANSITIONs"
          
          def pathToCurrentState
          def pathToCareflowStep // CStep que ejecuto para ir al currentState del mismo nodo del arquetipo
          attr.children.eachWithIndex { ism_transition, i ->
          
            //println ism_transition.nodeID
            
            // El estado siguiente
            pathToCurrentState = "/ism_transition[$ism_transition.nodeID]/current_state/defining_code"
            pathToCareflowStep = "/ism_transition[$ism_transition.nodeID]/careflow_step/defining_code"
            
            
            // Codigos de los estados en la terminologia de openehr que corresponden a un careflow_step
            // Tengo que saber cuales estados en la ism corresponden a cuales codigos ...
            //println ""
            //println "current state: "+ arch.node( pathToCurrentState ).getCodeList()
            //println "careflow step: "+ arch.node( pathToCareflowStep ).getCodeList()
            
            
            def csteps_codes
            
            // Para cada estado a los que puedo ir desde el estado actual
            possibleStates.each { state ->
            
              /*
              // Si el estado al que puedo ir 'staet' desde el estado actual
              // esta en la lista de current state del nodo actual del arquetipos
              if ( arch.node( pathToCurrentState ).getCodeList().contains( state_codes[state] ) )
              {
                // Agrega el estado siguiente a la lista de estados posibles a los que ir.
                if (!transitions[instExec.id])
                {
                  transitions[instExec.id] = []
                }
                transitions[instExec.id] << state_codes[state] // codigo en la terminologia de openehr
              }
              */
              
              // 2) Selecciono careflow step si:
              //   del estado actual puedo ir a alguno de los estados
              //   "current state" asociados al careflow step.
              if ( arch.node( pathToCurrentState ).getCodeList().contains( state_codes[state] ) )
              {
                csteps_codes = arch.node( pathToCareflowStep ).getCodeList() // deberia teber un codigo solo...
                
                println "csteps_codes: "+ csteps_codes
                
                arch_careflow_steps << csteps_codes[0]
              }
            }
            
            /* varios codigos para un careflow_step, son strings
            arch.node( pathToCurrentState ).getCodeList().each {
            
             println it +" "+ it.class
            }
            */
            
            //println i + ") " + ism_transition.attributes.find{ it.rmAttributeName == 'current_state' }
            
          } // each ism_transition
          
          
          
          // TODO: ver cuales de los estados posibles estan definidos en el atributo del arquetipo
          //      solo puedo ejecutar acciones para esos estados (no para todos los posibles)
          //
          // Esto no evita que pueda ejecutar 2 veces el mismo careflow step porque corresponde
          // con distintos estados (esto lo pregunte en el CKM y no tuve respuesta definitiva).
          
          // 2)
          activityActions[instExec.id] << [
            archetype: arch,
            careflow_steps: arch_careflow_steps // map<archId, List<careflow step>>
          ]
          
        } // each archetype
      }
   
      //println "transitions: " + transitions
   
      render(template: 'listInstructions',
           model: [instructionExecs: instructionExecs,
                 activityActions: activityActions])
                 /* transitions: transitions */
   } // listInstructions
   
   
   /**
    * Registra una ACTION para una ACTIVITY en una nueva COMPOSITION.
    * 1. Se muestra GUI para ingresar datos para la ACTION seleccionada (archetypeId)
    *   1.1. Debe generar GUI dinamicamente o en Bootstrap generar GUIs especificas
    *       para el ingreso de acciones referenciadas desde arquetipos de INSTRUCTION.
    *   1.2. Para generar GUI necesito un template, lo puedo armar dinamicamente por ahora...
    * 2. Ingresa datos, valida, guarda, en una NUEVA COMPOSITION
    * 3. Se muestran los datos ingresados y puede editar.
    * 4. Firma para cerrar.
    *
    * @param archetypeId identificador del arquetipo de accion
    * @param instructionId identificador del InstructionExecution
    * @param careflowStep codigo del careflow_step dentro del arquetipo de ACTION que se quiere ejecutar
    */
   def recordAction = {
   
      def instExec = workflow.InstructionExecution.get(params.instructionId)
   
      // 1. Generar GUI para la accion
      
      // template dinamico con ref al archetypeId para generar la GUI
      // FIXME: todas las acciones usaran el mismo template ¿porque no persistirlo?
      // ademas en los datos de archetyped se tendria el mismo templateId
      // habria que pregutar primero si ya no existe el template para el mismo
      // archId en el root
      def dinTemplate = new Template(
       templateId: (java.util.UUID.randomUUID() as String),
       name: 'template dinamico por recordAction',
       rootArchetype: new ArchetypeReference(
         refId: params.archetypeId,
         type:ArchetypeTypeEnum.ACTION))
      
      // genera GUI
      guiCachingService.generateGUI( [dinTemplate] )
      
      // muestra gui
      def view = '/guiGen/create/generarCreate' // dinamica
      def form = gui.GuiManager.getInstance().get(dinTemplate.templateId, 'create', session.locale.toString())
      
      
      // ============================================================
      // Crea una composition para meterle la action
      //def startDate = converters.DateConverter.toIso8601ExtendedDateTimeFormat( new Date() )
      //def composition = hceService.createComposition( startDate, "cumple una orden", null )
      def composition = hceService.createComposition( new Date(), "cumple una orden", null )
      
      if (!composition.save())
      {
        println composition.errors
      }
      
      session.ehrSession.episodioId = composition.id
      // ============================================================
      
      render( view: view,
            model: [
              //patient: patient,
              template: dinTemplate,
              //sections: sections,
              episodeId: composition.id, //session.ehrSession?.episodioId,
              form: form,
              domain: Domain.get(session.ehrSession.domainId),
              //workflow: workflow,
              //stage: stage
            ])
       return
   
   } // recordAction
   
   // /SOPORTE INICIAL DE ORDENES
   // ===========================================================
   
   
   // TODO: vista
   // Pantalla 2.1- Escritorio Medico-Administrativo
   /**
    * @param anyDomain si hay un paciente en session y viene anyDomain, el listado muestra registros de cualquier dominio.
    * @param offset desfazaje para paginacion
    * @param max cantidad de registros por pagina de la paginacion (TODO: este param no se esta pasando...)
    */
   def list = {
      
      //println session.ehrSession.patientId
      
      // FIXME: esto deberia hacerse con filters?
      if (!session.ehrSession || !session.ehrSession.domainId) // puede pasar si caduca la session
      {
        // TODO: i18n
        flash.message = "Ha caducado la sesion, por favor ingrese de nuevo"
        redirect(controller:'domain', action:'list')
        return
      }
      
      // TODO: configurable
      if (!params.max) params.max = '20' // De la web siempre viene como String
      
      // ==========================================================================
      // TODO: filtrar registros por dominio (session.ehrSession.domainId)
      //println "dominio: " + session.ehrSession.domainId
      def domain = Domain.get( session.ehrSession.domainId )
       
      // domain.compositions equivale a folder.items
      //println "domain compositions: " + domain.compositions
      
      
      // Filtro por fechas
      // TODO: para poder filtrar, Composition deberia tener startDate y endDate
      //       de EventContext y como Date en lugar de DvDateTime.
      Date qFromDate
      Date qToDate
      if (params.fromDate) qFromDate = Date.parse(config.app.l10n.date_format, params.fromDate)
      if (params.toDate) qToDate = Date.parse(config.app.l10n.date_format, params.toDate)
      
      
      // FIXME: si no coincide ningun criterio, devuelve todas las compos.
      // esto se resuelve teniendo la referencia inversa desde las compos
      // al parent Folder.
      def compos = Composition.withCriteria {
         
         if (qFromDate)
         {
            ge('startTime', qFromDate) // startTime >= fromDate
         }
         if (qToDate)
         {
            le('startTime', qToDate) // startTime <= toDate
         }
         
         // TODO: filtrar registros por paciente, si hay un paciente en session.ehrSession.patientId
         // Ver issue #22
         /*
          def partySelf = hceService.createPatientPartysSelf(params.root, params.extension)
          def participation = hceService.createParticipationToPerformer( partySelf )
          composition.context.addToParticipations( participation )
         */
         // Si hay paciente seleccionado
         if (session.ehrSession.patientId)
         {
            def patient = demographic.party.Person.get(session.ehrSession.patientId)
            
            def xstream = new com.thoughtworks.xstream.XStream()
            def codedExternalRef = xstream.toXML(
              new support.identification.PartyRef(
                namespace: "demographic", // FIXME: ver valores correctos
                type: "PERSON", // FIXME: ver valores correctos
                objectId: support.identification.UIDBasedID.create(patient.ids[0].root, patient.ids[0].extension)
              )
            )
            
            //println codedExternalRef
            /*
            <support.identification.PartyRef>
             <namespace>demographic</namespace>
             <type>PERSON</type>
             <objectId class="support.identification.UIDBasedID">
               <value>2.16.840.1.113883.2.14.2.1::1234567</value>
             </objectId>
            </support.identification.PartyRef>
            */
            
            //println patient + " " + session.ehrSession.patientId
            
            // context.participations<List<Participation>>.performer<PartySelf>
            context {
              participations {
                performer {
                  //eq('root', patient.ids[0].root)
                  //eq('extension', patient.ids[0].extension)
                  eq('codedExternalRef', codedExternalRef)
                }
              }
            }
         }
         
         // La lista de items podria ser larguisima,
         // una solucion mas performante es que cada
         // composition tenga como parent al folder
         // domain. 'parent' es un atributo de Locatable (creo)
         
         // Inlist implementado mas o menos
         /*
         or {
            domain.items.each{ objref ->
              // Supongo que objref.type == 'COMPOSITION', y que objref.namespace=='local'
              // podria agregar un chequeo por las dudas.
              eq('id', Long.parseLong(objref.objectId.value))
              
              println "ref compo id: " + objref.objectId.value
            }
         }
         */
         
         // SI no es un listado de cualquier dominio
         if (!params.anyDomain)
         {
            // Uso la referencia desde los hijos al padre, asi me ahorro el loop
            eq('rmParentId', domain.id)
         }
         
         // paginacion
         maxResults(Integer.parseInt(params.max))
         
         if (params.offset)
           firstResult( Integer.parseInt(params.offset) )
         
         order("id", "desc") // se que el id es incremental
         
         
         cache(true)
      }
     
      // para paginacion
      def total = Composition.withCriteria {
         
        projections {
         count('id')
        }
        
        if (qFromDate)
        {
           ge('startTime', qFromDate) // startTime >= fromDate
        }
        if (qToDate)
        {
           le('startTime', qToDate) // startTime <= toDate
        }
         
        // Si hay paciente seleccionado
        if (session.ehrSession.patientId)
        {
            def patient = demographic.party.Person.get(session.ehrSession.patientId)
            
            def xstream = new com.thoughtworks.xstream.XStream()
            
            def codedExternalRef = xstream.toXML(
              new support.identification.PartyRef(
                namespace: "demographic", // FIXME: ver valores correctos
                type: "PERSON", // FIXME: ver valores correctos
                objectId: support.identification.UIDBasedID.create(patient.ids[0].root, patient.ids[0].extension)
              )
            )
            
            // context.participations<List<Participation>>.performer<PartySelf>
            context {
              participations {
                performer {
                  //eq('root', patient.ids[0].root)
                  //eq('extension', patient.ids[0].extension)
                  eq('codedExternalRef', codedExternalRef)
                }
              }
            }
        }
        
        // Si no es un listado de cualquier dominio
        if (!params.anyDomain)
        {
           // Uso la referencia desde los hijos al padre, asi me ahorro el loop
           eq('rmParentId', domain.id)
        }
        
        cache(true)
      }
      // ==========================================================================
      
      
      // deselecciona el episodio y el paciente que este seleccionado
      session.ehrSession.episodioId = null
      
      
      // Antes se devolvian todas las compositions, ahora se filtra por dominio.
      return [ compositions: compos, domain: domain, total: total[0] ]
   }
   
   
   // Pantalla 3.2- Crear Episodio
   // Puede venir un patientId si creo el episodio para un paciente desde admision.
   def create = {
   
      //println "Create: " + params
      if (params.doit)
      {
         //def startDate = DateConverter.iso8601ExtendedDateTimeFromParams( params, 'startDate_' )
         def startDate = new Date()
         //println "Startdate: " + startDate
         
         def composition = hceService.createComposition( startDate, params.otherContext, session.ehrSession.workflowId )
         
         // TODO: verificar si se crea para un paciente:
         // - buscarlo por id en el servicio demografico
         // - asociarlo como subject de la composition
         // - guardar todo
         
         // FIXME: si hay un paciente seleccionado no deberia venir en params,
         //      deberia estar en EHRSession.
         if (params.root && params.extension) // si viene el id del paciente desde la seccion demografica
         {
            // FIXME: hacer como en el caso de abajo: si se selecciona un paciente en el
            // demografico, poner su id en session.
            
            //println "Se crea un registro para el paciente seleccionado"
            def partySelf = hceService.createPatientPartysSelf(params.root, params.extension)
            def participation = hceService.createParticipationToPerformer( partySelf )
            composition.context.addToParticipations( participation )
         }
         else if (session.ehrSession.patientId) // si viene el id del paciente desde la lista de admisiones (en el listado de dominios)
         {
            def patient = demographic.party.Person.get(session.ehrSession.patientId)

            def partySelf = hceService.createPatientPartysSelf(patient.ids[0].root, patient.ids[0].extension)
            def participation = hceService.createParticipationToPerformer( partySelf )
            composition.context.addToParticipations( participation )
         }
         
         
         // Set parent
         def domain = Domain.get( session.ehrSession.domainId )
         composition.padre = domain
         
         
         //XStream xstream = new XStream()
         //render( text: xstream.toXML(composition), contentType:'text/xml' )
         if (!composition.save())
         {
            // FIXME: haldlear el error si ocurre!, darle un mensaje lindo al usuario, etc.
            println "Error: " + composition.errors
         }
         
         // ------------------------------------------------------------------
         //
         // TODO: poner la composition dentro del folder del dominio actual
         //
         /*
         ObjectRef ref = new ObjectRef(
            namespace: 'local', // porque se usa el id local en la base para la composition
            type: 'COMPOSITION',
            objectId: new ObjectID( // FIXME: ObjectID en el RM es abstracta, ver si otra subclase encaja mejor o pedir que se relaje el modelo para usar directamente ObjectID.
              value: composition.id.toString() // El value es de tipo string
            )
         )
         
         domain.addToItems( ref )
         if (!domain.save())
         {
            // TODO: handlear el error
            println "Error al guardar domain folder: " + domain.errors
         }
         */
         // ------------------------------------------------------------------
         
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
         
         // Pablo: antes volvia al listado.
         // Queda mas agil que vaya derecho al show luego de crear, asi empieza a registrar.
         redirect(action:'show', id:composition.id)
         return
      }
      
      render(template:"create")
   }
   
   def show = {
      
      // TODO: poner en sesion el episodio que se esta viendo
      //println "Show: " + params
      
      
      // Si expira la sesion tengo que volver al listado para crearla de nuevo
      // FIXME: esto deberia estar en un pre-filter
      if (!session.ehrSession)
      {
        redirect(action:'list')
        return
      }
      
      def composition = Composition.get( params.id )
      
      
      // Actualizacion de contexto, esta seleccionado un unico episodio
      session.ehrSession.episodioId = composition.id
      
      
      
      
      // FIXME:
      // La primera vez que se muestra luego de seleccionar un paciente, esto da null.
      // Ver si es un tema de la carga lazy de las participations y si se resuelve con carga eager.
      // FIXME: esta tira una except si hay mas de un pac con el mismo id, hacer catch
      def patient = hceService.getPatientFromComposition( composition )

      //println "Patient from composition: " + patient

      // NECESARIO PARA EL MENU
      // no se usa mas para el menu, se usa solo workflow
      //def sections
      //def workflow
      
      // La composition a mostrar puede ser de un dominio/workflow diferente al seleccionado
      if (composition.workflowId == session.ehrSession.workflowId)
      {
         // Stage names para el workflow actual...
         //sections = util.TemplateUtils.getSections(session)
         
         //workflow = WorkFlow.get( session.ehrSession.workflowId )
      }
      else
      {
         //println "getSections para composition!"
         // Stage names para el workflow de la composition que no es el actual...
         // FIXME: esto sirve para ambos casos, asi que el if no seria necesario
         //sections = util.TemplateUtils.getSections(composition)
         
         //workflow = WorkFlow.get( composition.workflowId )
         
         // Pongo en sesion el domain y workflow de la compostion que quiero ver porque
         // toda la generacion de gui depende de esos datos, este es el caso en que
         // quiero ver un registro pasado que su wf es distinto al wf seleccionado en sesion.
         session.ehrSession.workflowId = composition.workflowId
         
         // FIXME: Seleccionar un dominio distinto al actual puede generar confusion si se
         //        estaba en otro dominio y se vuelve para atras al listado de registros del
         //        dominio porque se van a ver otros registros...
         session.ehrSession.domainId = composition.padre.id
      }
      

      // patient puede ser null si todavia no se selecciono un paciente para el episodio,
      // p.e. si la atencion es de urgencia, se atiente primero y luego se identifica al paciente.
      return [composition: composition,
            patient: patient,
            //sections: sections, // no se usa mas para el menu, se usa solo workflow
            allSubsections: util.TemplateUtils.getDomainTemplates(session),
            workflow: WorkFlow.get( session.ehrSession.workflowId ) // Nuevo en lugar de completeSections
           ]
   } // show
   
   
   // TODO: vista listando links a templates segun config.
   // Pantalla 5.1- Registro Clinico
   def registroClinico2 = {
   
      println "------- registroClinico2 ---------------"
      if (!session.ehrSession?.episodioId)
      {
         flash.message = 'trauma.list.error.noEpisodeSelected'
         redirect(action:'list')
         return
      }
      
      def section = params.section // nombre de la etapa
      //def wf = WorkFlow.get( session.ehrSession.workflowId )
      //def stg = wf.stages.find{ it.name == section }
      
      
      def subsections = util.TemplateUtils.getSubsections(section, session) // this.getSubsections('EVALUACION_PRIMARIA')
	   
      println "subsections: " + subsections // [INGRESO-triage.v1]
     
	   // FIXME: mostrar la primer seccion para la que tenga permisos, sino tiene permisos para
	   //      la primer seccion, tiene permisos para otra dentro de la misma etapa asistencial.
	   
	   def firstSubSection // = subsections[0]
	   
	   for (String templateId : subsections)
	   {
	     g.hasDomainPermit(domain:Domain.get(session.ehrSession.domainId), templateId:templateId) {
			  println "tiene permisos para $templateId"
			  
			  firstSubSection = templateId
			  return
		  }
		  g.dontHasDomainPermit() // para apagar las flags de hasDomainPermit
		  
		  if (firstSubSection) break
	   }
	   
	   // FIXME: sino hay firstSubSections, no tiene permisos (en la gui se verifica
	   //      que no lo deje pasar si no tiene permisos, pero es una medida precautoria)
	   
	   
      //println "section: " + section
      //println "firstSubSection: " + firstSubSection
      
      def composition = Composition.get( session.ehrSession?.episodioId )

      // FIXME: esta tira una except si hay mas de un pac con el mismo id, hacer catch
      //def patient = hceService.getPatientFromComposition( composition )

      // FIXME: mismo codigo que en GuiGen generarTemplate
      if ( hceService.isIncompleteComposition( composition ) )
      {
         //g.hasContentItemForTemplate( episodeId: session.ehrSession?.episodioId, templateId: section+'-'+firstSubSection)
         def item = hceService.getCompositionContentItemForTemplate(composition, section+'-'+firstSubSection)
         
         // FIXME:
         // Esto ya se chequea en la vista, es mas simple chequearlo aca y que
         // la vista si tiene que generar how o generar template siempre llame
         // a registoClinico2.
         // Se fija si el episodio tiene o no registro para el template dado.
         //if (it.hasItem)
         if (item)
         {
            redirect(controller: 'guiGen',
                  action: 'generarShow',
                  params: [templateId: firstSubSection, id: item.id])
            return
         }
         else
         {
            println "registroClinico2: redirect a generarTemplate: templateId=$firstSubSection"
            // Muestra create
            redirect(controller: 'guiGen',
                  action: 'generarTemplate',
                  params: [templateId: firstSubSection])
            return
         }
      }
      else
      {
         flash.message = "registroClinico.warning.noHayRegistroParaLaSeccion"
         redirect( action: 'show', id: session.ehrSession?.episodioId)
         return
      }
   }
   
   /**
    * Acion auxiliar para mostrar imagenes guardas en DvMultimedia en la web.
    */
   def fetch_mm = {
      
      response.setHeader("Cache-Control", "no-store")
      response.setHeader("Pragma", "no-cache")
      response.setDateHeader("Expires", 0)
        
      def image = DvMultimedia.get( params.id )
         
      if (image)
      {
         response.setContentType(image.mediaType.codeString)
         response.getOutputStream().write(image.data) // con byte[]   
         
         response.outputStream.flush()
         response.outputStream.close()
      }

      return null
   }
   
   /**
    * Firma y cierra el registro (antes firmar y cerrar eran procesos separados: http://code.google.com/p/open-ehr-gen-framework/issues/detail?id=9).
    * in: id episode id
    */
   def signRecord = {
      
      // FIXME: se tiene el id en session.ehrSession?.episodioId
      def composition = Composition.get( params.id )

      if (!composition)
      {
         flash.message = 'trauma.list.error.noEpisodeSelected'
         redirect(action:'list')
         return
      }
      
      // FIXME: esta tira una except si hay mas de un pac con el mismo id, hacer catch
      def patient = hceService.getPatientFromComposition( composition )


      flash.message = null
      flash.error = null
      
      // Para retornarle a la vista
      def model = [
                composition: composition,
                patient: patient,
                sections: util.TemplateUtils.getSections(session), // Es necesario para mostrar el menu
                subsections: [], // No hay porque estoy firmando el registro
                allSubsections: util.TemplateUtils.getDomainTemplates(session),
                workflow: WorkFlow.get(session.ehrSession.workflowId) // nuevo
               ]
      
      
      // FIXME: cerrar y firmar deberian estar dentro de la misma transaccion y asegurar de que si fallo algo, el registro
      //      NO quede cerrado y no firmado, o abierto y firmado.
      if (params.doit)
      {
         if (!patient)
         {
            flash.error = "trauma.sign.noPatientSelected"
            return model
         }
         
         if (composition.composer)
         {
            flash.error = "trauma.sign.registryAlreadySigned"
            return model
         }
         
         def auth = authorizationService.getLogin(params.user, params.pass)
         if (!auth)
         {
            flash.error = "trauma.sign.wrongSignature"
            return model
         }
         
         // Verificacion del rol, debe ser medico
         // Este problema puede pasar si estoy logueado como medico pero firmo con datos de un adminsitrativo.
         // TODO: un posible tema a ver es que pasa si la persona firmante no es la persona
         //      que esta logueada, puede pasar y no necesariamente es un problema.
         //def roles = Role.withCriteria {
         def validities = RoleValidity.withCriteria {
            eq('performer', auth.person)
         }
         
         //def roleKeys = roles.type
         def roleKeys = validities.role.type
         
         println "----------------------"
         println "roleKeys: " + roleKeys
         println "----------------------"
         
         if ( !roleKeys.contains(Role.MEDICO) )
         {
            flash.error = "trauma.sign.wrongSigningRole"
            return model
         }
            
         
         def person = auth.person
         def id = person.ids[0] // FIXME: ver si tiene ID, DEBERIA TENER UN ID SIEMPRE, es un medico!

         
         // Cierra el registro
         //if ( !hceService.closeComposition(composition, DateConverter.toIso8601ExtendedDateTimeFormat(new Date())) )
         if ( !hceService.closeComposition(composition, new Date()) )
         {
            flash.error = "trauma.sign.closeInternalError"
            return model
         }
         
         // TODO:
         // Guardar digesto del registro para detectar alteraciones posteriores
         // Usar clave privada del medico para encriptar el digesto, y asi firmar el registro.
         //   Luego con su clave publica se podra decifrar el digesto y compararlo con el digesto original.
         //   Con esto se garantiza autoria, pero se necesita algun tipo de gestor de claves para mantener la publica y permitir que el medico ingrese la privada (que no se puede mantener en el sistema).
         
         //println "id medico: " + id + " " + id.root + " " + id.extension
         
         // Firma el registro 
         if (!hceService.setCompositionComposer(composition, id.root, id.extension))
         {
            flash.error = "trauma.sign.signInternalError"
            return model
         }

         // Cambia el estado del regsitro en su VERSION
         def version = Version.findByData( composition )
         version.lifecycleState = Version.STATE_SIGNED
         version.save()

         flash.message = "trauma.sign.recordCorrectlySigned"
      }
      
      return model
   }

   //-------------------------------------------------------------------------------------------------------------
   // Pantalla - Reapertura de registro
   def reopenRecord = {

      def composition = Composition.get( params.id )

      if (!composition)
      {
         redirect(action:'list')
         return
      }

      def version = Version.findByData( composition ) // Ojo. findByData retorna una coleccion. Como hay una sola version con esa composition retorna una instancia (porque al crear una nueva version, pongo null en el atributo data de la version)
      if (version.lifecycleState == Version.STATE_SIGNED)
      {
         // -----------------------------------------------------------------
         // FIXME: esta tira una except si hay mas de un pac con el mismo id, hacer catch
         def patient = hceService.getPatientFromComposition( composition )
         def sections = util.TemplateUtils.getSections(session)
         def subsections = [] // No hay porque estoy firmando el registro

         //------------------------------------------------------------------
         //------------------------------------------------------------------

         flash.message = null
         flash.error = null

         if (params.doit)
         {

            def auth = authorizationService.getLogin(params.user, params.pass)
            if (!auth)
            {
               // TODO: i18n
               flash.error = "Firma erronea, verifique sus datos"
               return [
                     composition: composition,
                     patient: patient,
                     sections: sections,
                     subsections: subsections,
                     allSubsections: util.TemplateUtils.getDomainTemplates(session),
                     workflow: WorkFlow.get( session.ehrSession.workflowId )
                     ]
            }

            // Verificacion del rol, debe ser medico
            // Este problema puede pasar si estoy logueado como medico pero firmo con datos de un adminsitrativo.
            // TODO: un posible tema a ver es que pasa si la persona firmante no es la persona
            //      que esta logueada, puede pasar y no necesariamente es un problema.
            //def roles = Role.withCriteria {
            def validities = RoleValidity.withCriteria {
               eq('performer', auth.person)
            }
            
            //def roleKeys = roles.type
            def roleKeys = validities.role.type
            if ( !roleKeys.contains(Role.MEDICO) )
            {
               flash.error = "Firma erronea, la persona firmante no es medico"
               return [
                     composition: composition,
                     patient: patient,
                     sections: sections,
                     subsections: subsections,
                     allSubsections: util.TemplateUtils.getDomainTemplates(session),
                     workflow: WorkFlow.get( session.ehrSession.workflowId )
                     ]
            }


            def person = auth.person
            def id = person.ids[0] // FIXME: ver si tiene ID, DEBERIA TENER UN ID SIEMPRE, es un medico!

            if (!hceService.setVersionCommitter(version, id.root, id.extension))
            {
               // TODO: i18n
               flash.error = "Ocurrio un error al intentar firmar el registro clinico, intente de nuevo"
               return [
                     composition: composition,
                     patient: patient,
                     sections: sections,
                     subsections: subsections,
                     allSubsections: util.TemplateUtils.getDomainTemplates(session),
                     workflow: WorkFlow.get( session.ehrSession.workflowId )
                     ]
            }

            // Cambia el estado del regsitro en su VERSION
            //def version = Version.findByData( composition )
            //version.lifecycleState = Version.STATE_SIGNED
            //version.save()


            // Creo CDA si no existe
            def archivoCDA = new File(ApplicationHolder.application.config.hce.rutaDirCDAs + '\\' + version.nombreArchCDA)
            if (!archivoCDA.exists())
            {
               def cdaMan = new ManagerCDA()
               int idEpisodio = Integer.parseInt(params.id)
               cdaMan.createFileCDA(idEpisodio)
            }

            // Creo una copia de la composition
            ////def new_composition = new Composition(archetypeNodeId: composition.archetypeNodeId,
            ////                             name: composition.name,
            ////                             archetypeDetails: composition.archetypeDetails,
            ////                             path: composition.path,
            ////                             composer: null,
            ////                             context: composition.context,
            ////                             category: composition.category,
            ////                             territory: composition.territory,
            ////                             language: composition.language)
            ////composition.content.each{e ->
            ////   new_composition.addToContent(e)
            ////}

            // Elimino movimiento y firma de la composition (de la copia)
            def composerAux = composition.composer
            def contentAux = composition.content
            composition.composer = null
            
            // Creo nueva versión (con motivo, firma, nombre Arch CDA, composition)
            def new_version = new Version(
              //data: composition,
              //timeCommited: new DvDateTime(
              //  value: DateConverter.toIso8601ExtendedDateTimeFormat( new Date() )
              //),
              //lifecycleState: Version.STATE_INCOMPLETE,
              //numeroVers: version.getNumVersion() + 1
            )

            new_version.data = composition
            new_version.timeCommited = new DvDateTime(value: DateConverter.toIso8601ExtendedDateTimeFormat(new Date()))
            new_version.lifecycleState = Version.STATE_INCOMPLETE
            new_version.numeroVers = version.getNumVersion() + 1
            
            //println "XXXXXXXXXXXXXX------>>>> V0:" + version.getNumVersion()
            //println "XXXXXXXXXXXXXX------>>>> V1:" + new_version.getNumVersion()

            if (new_version.save())
            {
               version.data = null
               if (version.save())
               {
                  flash.message = "Reapertura firmada correctamente"
               }
               else
               {
                  composition.composer = composerAux
                  composition.content = contentAux
                  version.data = composition;
                  flash.error = "Ocurrio un error al intentar firmar el registro clinico, intente de nuevo"
               }
            }
            else
            {
               flash.error = "Ocurrio un error al intentar firmar el registro clinico, intente de nuevo"
            }
            
            return [
                  composition: composition,
                  patient: patient,
                  sections: sections,
                  subsections: subsections,
                  allSubsections: util.TemplateUtils.getDomainTemplates(session),
                  workflow: WorkFlow.get( session.ehrSession.workflowId )
                  ]
         }

         return [composition: composition,
               patient: patient,
               sections: sections, // necesario para el menu
               subsections: subsections, // necesario para el menu
               allSubsections: util.TemplateUtils.getDomainTemplates(session),
               workflow: WorkFlow.get( session.ehrSession.workflowId )
               ]
      }
      else
      {
         // Vuelvo a la Pagina de Selección Episodio
         redirect(action:'list')
         return
      }
   }
}