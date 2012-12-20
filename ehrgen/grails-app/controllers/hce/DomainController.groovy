package hce;

//import hce.core.common.directory.Folder
import domain.Domain
import workflow.WorkFlow
import workflow.Stage
import templates.Template
import templates.ArchetypeTypeEnum
import archetype.ArchetypeIndex

import data_types.text.*
import hce.core.common.archetyped.Archetyped
import support.identification.*
import hce.HceService
import authorization.DomainPermit
import demographic.role.Role
import demographic.role.RoleValidity


class DomainController {

   def hceService
   def guiCachingService // para generar gui de templates creados desde la gui
   
   def index = {
      redirect(action: 'list')
   }
   
   /**
    * Lista dominios existentes.
    * TODO: filtrar dominios por permisos del usuario.
    */
   def list = {
      
      if (session.ehrSession)
      {
         // Si hay un dominio seleccionado, se deselecciona
         session.ehrSession.domainId = null
         session.ehrSession.workflowId = null
      }

      // Lista de folders creados a partir de los codigos de dominios de la configuracion      
      def domains = Domain.list()
     
     
      //println session.ehrSession
     
      // Roles del usuario logueado para seleccionar workflow
      def roleValidities = RoleValidity.withCriteria {
        performer {
          eq('id', session.ehrSession.getLoggedPerson().id) // EHRSession tiene el id del LoginAuth no de la Person
        }
      }
      
      def roles = roleValidities.role
     
      //println roleValidities
      //println roles
      
      return [domains: domains, roles: roles]
   }
   
   /**
    * @params id identificador del domain en la DB.
   */
   def selectDomain = {
     
     println "selectDomain " + params
     
     if (!params.id)
     {
        flash.message = "Debe seleccionar un dominio"
        redirect(action:'list')
        return
     }
     
     def domain = Domain.get(params.id)
     
      if (!hceService.domainHasTemplates(domain))
      {
         flash.message = "El dominio aún no tiene registros definidos"
         redirect(action: "list")
         return
      }
      
      if (!session.ehrSession)
      {
         throw new Exception("Debe haber un contexto en session")
      }
      
      session.ehrSession.domainId = domain.id
        
      // Seleccion del workflow segun el rol:
      //  - Define el proceso (serie de templates) que le voy a mostrar en el menu
      //  - El dominio tiene varios workflows, cada uno es visto por un rol
      //  - Cada workflow tiene sus propios templates
      //def role = Role.get(params.roleId) // Probar si funciona buscar por roleId
        
      // Dentro de un dominio, un rol que esta en un wf no esta en otro,
      // por eso siempre tira como resultado solo UN wf.
      
      // Lista con un resultado
      def workflows = WorkFlow.withCriteria {
         eq('owner', domain)
         forRoles {
          idEq(Long.valueOf(params.roleId))
         }
      }
        
      println "workflows: "+ workflows
        
      session.ehrSession.workflowId = workflows[0].id
      
      
      redirect(controller: 'records', action: 'list')
   }
   
   /**
    * Crea un nuevo dominio desde la UI.
    * 
    * @param value nombre del dominio
    * @param codeString codigo del dominio (usado para verificar autorizacion)
    */
   def create = {
      
      if (params.doit)
      {
        def domain
       
        // Ya hay un dominio con el mismo nombre o codigo?
       // TODO: tengo que hacer la consulta por campos en el
         //         nombre que estan codificados como XML
       
       /*
         domain = new Folder(
            name: new DvCodedText(
            value: params.value, // FIXME: I18N
            definingCode: new CodePhrase(
              codeString: params.codeString,
              terminologyId: TerminologyID.create('local', null)
            )
         ),
         path: '/domain.user_created.'+params.codeString,    // FIXME: para que se usa?
         archetypeNodeId: "at0001",        // FIXME: Inventado. Consultar si sirve de algo arquetipar un Folder... (no tiene estructura)
         archetypeDetails: new Archetyped( // FIXME: Inventado
            archetypeId: 'ehr.domain',
            templateId: 'ehr.domain',
            rmVersion: '1.0.2' // FIXME: deberia ser variable global de config
          )
         )
       */
       domain = new Domain(
          name: params.value
       )
       
       if (!domain.save())
       {
          println folder.errors
          //println folder.name.errors
          //println folder.archetypeDetails.errors
       }
       
       
       // =============================================================
       // Permisos para el nuevo dominio
       //
       // Permiso * para todos los templates del dominio
       // (ahora el dominio NO tiene templates, recien se crea)
       def perm = new DomainPermit(domain: domain.id) // Los permisos se verifican contra el codeString
       perm.save()
       
       // El rol medico tiene acceso a todos los dominios
       def role = Role.findByType(Role.MEDICO)
       role.addToDomainPermits(perm)
       role.save()
       // ==============================================================
       
       
       flash.message = 'Nuevo dominio '+ params.value +' creado con éxito'
       redirect(action:'list')
       return
      }
   }
   
   /**
    * Edita un dominio existente. Solo se pueden editar los dominios
    * creados con "create" y no los creados desde el bootstrap.
   *
   * @param id identificador del folder en la base de datos
   * @param value nombre del dominio
    * @param codeString codigo del dominio (usado para verificar autorizacion)
    */
   def edit = {
      
     if (!params.id)
     {
        flash.message = 'Debe seleccionar algún dominio para editar'
       redirect(action:'list')
       return
     }
     
     def domain = Domain.get(params.id)
     
     if (!domain)
     {
        flash.message = 'El dominio que desea editar no existe'
        redirect(action:'list')
        return
     }
     
     if (params.doit)
     {
        println params
     
        if (params.value)
          domain.name = params.value
      
        //if (params.codeString)
          //domain.name.definingCode.codeString = params.codeString
          
        if (!domain.save())
        {
          println domain.errors
          //println folder.name.errors
          //println folder.archetypeDetails.errors
        }
       
        flash.message = 'Dominio '+ domain.name +' actualizado con éxito'
        redirect(action:'list')
        return
     }
     
     return [domain: domain]
   }
   
   /**
    * Crea un worklow para un dominio determinado.
    *
    * @param id identificador del dominio.
    * @param roleId lista de identificadores de roles que pueden ejecutar el workflow.
    */
   def createWorkflow = {
   
      def domain = Domain.get(params.id)
      
      // Roles para seleccionar
      def roles = Role.list()
      
      def currentRoles = domain.workflows.forRoles // [[Role]]
      
      // Solo los roles que no han sido asignados a otros workflows
      // RNE: La interseccion de roles de dos workflows del mismo domain debe ser vacia.
      roles = roles - currentRoles.flatten()
      
      if (params.doit)
      {
         def wf = new WorkFlow(owner:domain)
         
         def roleIds = params.list('roleId')
         def role
         roleIds.each {
         
            role = Role.get(it)
            wf.addToForRoles(role)
         }
         
         domain.addToWorkflows( wf )
         
         if (!domain.save())
         {
            println domain.errors
            println wf.errors
         }
         
         // Muestra texto en la modal
         render "Flujo de trabajo creado con exito"
      }
      
      return [domain: domain, roles: roles]
   }
   
   /**
    * Crea una stage para un workflow de un dominio
    *
    * @param workflowId identificador del workflow
    * @param name nombre de la stage a crear
    * @param templateId lista de ids de templates que definen los registros de la stage
    */
   def createStage = {
   
      def wf = WorkFlow.get(params.workflowId)
      
      // Para elegir que registros van en esta stage
      def templates = Template.list()
      
      if (params.doit)
      {
         def stage = new Stage(name: params.name)
         
         def templateIds = params.list('templateId')
         def template
         templateIds.each {
         
            template = Template.get(it)
            
            stage.addToRecordDefinitions( template )
         }
         
         wf.addToStages( stage )
         
         if (!wf.save())
         {
            println wf.errors
         }
         
         // Muestra texto en la modal
         render "Stages creada con exito"
      }
      
      return [workflow:wf, templates:templates]
   }
   
   /**
    * Crea un template desde la gui y lo guarda en la db.
    * TODO: serializar a XML para guardar en disco.
    *
    * @param templateId
    * @param name
    * @param archetypeId
    */
   def createTemplate = {
   
      // TODO: falta crear las referencias a los slots
      def archetypes = ArchetypeIndex.list()
   
      if (params.doit)
      {
         def template = new templates.Template(
            templateId: 'EHRGen-EHR-'+ params.templateId, // TODO: soporte para DEMOGRAPHIC
            name: params.name
         )
         
         // Included archetypes para slots
         def idx = ArchetypeIndex.findByArchetypeId(params.archetypeId)
        
        
         // No necesito pedir el arquetipo para tener su archId, creo uno y sacarle el rmEntity...
         //def archetype = archetype.ArchetypeManager.getInstance().getArchetype(params.archetypeId)
         //def type = ArchetypeTypeEnum.fromValue( archetype.archetypeId.rmEntity.toLowerCase() )
         
         def archetypeId = new org.openehr.rm.support.identification.ArchetypeID(params.archetypeId)
         def type = ArchetypeTypeEnum.fromValue( archetypeId.rmEntity.toLowerCase() )
        
         
         // Para SECTION, no se muestra, la estructura esta en los arquetipos referenciados por slots
         def pageZone = "content"
         if (type == ArchetypeTypeEnum.SECTION)
         {
            pageZone = "none"
            
            // Si es una seccion, el index debe tener algun slot
            if (idx.slots.size() == 0)
            {
               flash.message = "El arquetipo elegido ("+ params.archetypeId +") es una seccion cuyos slots no han podido ser resueltos, por favor incluya los arquetipos referenciados en el repositorio"
               return [archetypes: archetypes]
            }
         }
         
         
         def aref = new templates.ArchetypeReference(
            refId: params.archetypeId,
            type: type,
            pageZone: pageZone
            
            // includeAll
            // fields
         )
         
         template.rootArchetype = aref
         
         
         // Included archetypes para slots
         idx.slots.each { slot_index ->
         
            archetypeId = new org.openehr.rm.support.identification.ArchetypeID(slot_index.archetypeId)
            type = ArchetypeTypeEnum.fromValue( archetypeId.rmEntity.toLowerCase() )
            
            aref = new templates.ArchetypeReference(
               refId: slot_index.archetypeId,
               type: type
               
               // pageZone content por defecto // TODO: dejar especificar desde la gui
               // includeAll
               // fields
            )
            
            template.addToIncludedArchetypes( aref )
         }
         
         
         if (!template.save())
         {
            println template.errors
            flash.message = "Ocurrió un error, por favor ingrese el identificador, el nombre y seleccione un arquetipo"
            return [archetypes: archetypes]
         }
         
         // TODO: serializar a XML
         
         // Genera GUI para el nuevo template
         guiCachingService.generateGUI([template])
         
         // Agrega el template al cache
         // Permite tener toda la estructura del template en memoria y
         // no tener que cargarlo en cada request desde la base
         templates.TemplateManager.getInstance().cacheTemplate(template)
         
         
         flash.message = "Se ha creado el template con éxito"
         redirect(action:'list')
         return
      }
      
      return [archetypes: archetypes]
   }
}