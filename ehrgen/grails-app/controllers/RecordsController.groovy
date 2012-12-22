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

/**
 * @author Pablo Pazos Gutierrez (pablo.pazos@cabolabs.com)
 */
class RecordsController {

    def demographicService
    def authorizationService
    def hceService
    
    def index = {

       redirect(action:'list')
    }
    
    
    // TODO: vista
    // Pantalla 2.1- Escritorio Medico-Administrativo
    def list = {
        
       def compos = []
       
       // FIXME: esto deberia hacerse con filters?
       if (!session.ehrSession || !session.ehrSession.domainId) // puede pasar si caduca la session
       {
          // TODO: flash.message
          redirect(controller:'domain', action:'list')
          return
       }
         
       // ==========================================================================
       // TODO: filtrar registros por dominio (session.ehrSession.domainId)
       println "dominio: " + session.ehrSession.domainId
       def domain = Domain.get( session.ehrSession.domainId )
         
       
       // domain.compositions equivale a folder.items
       //println "domain compositions: " + domain.compositions
         
       // FIXME: si no coincide ningun criterio, devuelve todas las compos.
       // esto se resuelve teniendo la referencia inversa desde las compos
       // al parent Folder.
       compos = Composition.withCriteria {
            
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
            
            // Uso la referencia desde los hijos al padre, asi me ahorro el loop
            eq('rmParentId', domain.id)
            
            // TODO: paginacion
            // TODO: orden por fecha descendente
            // TODO: poner cantidad en config
            maxResults(15)
            
            if (params.offset)
              firstResult( Integer.parseInt(params.offset) )
            
            order("id", "desc") // 
            //order("context.startTime.value", "desc") // no funca
       }
         
       // ==========================================================================
         
       // TODO: filtrar registros por paciente, si hay un paciente en session.ehrSession.patientId
       
       // deselecciona el episodio y el paciente que este seleccionado
       session.ehrSession.episodioId = null
       //session.ehrSession.patient = null // FIXME: todavia no lo puedo hacer porque no puedo poner domain objects en session
       
       
       // Antes se devolvian todas las compositions, ahora se filtra por dominio.
       return [compositions: compos,
               //userId: session.ehrSession.userId, // no se usa
               domain: domain ]
    }
    
    
    // Pantalla 3.2- Crear Episodio
    // Puede venir un patientId si creo el episodio para un paciente desde admision.
    def create = {
   
        println "Create: " + params
        if (params.doit)
        {
            def startDate = DateConverter.iso8601ExtendedDateTimeFromParams( params, 'startDate_' )
            println "Startdate: " + startDate
            
            def composition = hceService.createComposition( startDate, params.otherContext )
           
            // TODO: verificar si se crea para un paciente:
            // - buscarlo por id en el servicio demografico
            // - asociarlo como subject de la composition
            // - guardar todo
            
            // FIXME: si hay un paciente seleccionado no deberia venir en params,
            //        deberia estar en EHRSession.
            if (params.root && params.extension) // si viene el id del paciente
            {
                println "Se crea un registro para el paciente seleccionado"
                def partySelf = hceService.createPatientPartysSelf(params.root, params.extension)
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
       
       // Actualizacion de contexto, esta seleccionado un unico episodio
       session.ehrSession.episodioId = Integer.parseInt(params.id)
       
       
       def composition = Composition.get( params.id )
       
       // FIXME:
       // La primera vez que se muestra luego de seleccionar un paciente, esto da null.
       // Ver si es un tema de la carga lazy de las participations y si se resuelve con carga eager.
       // FIXME: esta tira una except si hay mas de un pac con el mismo id, hacer catch
       def patient = hceService.getPatientFromComposition( composition )

       //println "Patient from composition: " + patient

       // NECESARIO PARA EL MENU
       // Stage names para el workflow actual...
       def sections = util.TemplateUtils.getSections(session)
       

       // patient puede ser null si todavia no se selecciono un paciente para el episodio,
       // p.e. si la atencion es de urgencia, se atiente primero y luego se identifica al paciente.
       return [composition: composition,
               patient: patient,
               sections: sections, // necesario para el menu
               allSubsections: util.TemplateUtils.getDomainTemplates(session),
               workflow: WorkFlow.get( session.ehrSession.workflowId ) // Nuevo en lugar de completeSections
              ]
    }
    
    
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
	   //        la primer seccion, tiene permisos para otra dentro de la misma etapa asistencial.
	   
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
	   //        que no lo deje pasar si no tiene permisos, pero es una medida precautoria)
	   
	   
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


        // Es necesario para mostrar el menu
        def sections = util.TemplateUtils.getSections(session)
        def subsections = [] // No hay porque estoy firmando el registro


        flash.message = null
        flash.error = null
        
        // Para retornarle a la vista
        def model = [//episodeId: session.ehrSession?.episodioId,
                     //userId: session.ehrSession.userId, // no se usa
                     composition: composition,
                     patient: patient,
                     sections: sections,
                     subsections: subsections,
                     allSubsections: util.TemplateUtils.getDomainTemplates(session)
                    ]
        
        
        // FIXME: cerrar y firmar deberian estar dentro de la misma transaccion y asegurar de que si fallo algo, el registro
        //        NO quede cerrado y no firmado, o abierto y firmado.
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
            //       que esta logueada, puede pasar y no necesariamente es un problema.
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
            if ( !hceService.closeComposition(composition, DateConverter.toIso8601ExtendedDateTimeFormat(new Date())) )
            {
               flash.error = "trauma.sign.closeInternalError"
               return model
            }
           
            // TODO:
            // Guardar digesto del registro para detectar alteraciones posteriores
            // Usar clave privada del medico para encriptar el digesto, y asi firmar el registro.
            //   Luego con su clave publica se podra decifrar el digesto y compararlo con el digesto original.
            //   Con esto se garantiza autoria, pero se necesita algun tipo de gestor de claves para mantener la publica y permitir que el medico ingrese la privada (que no se puede mantener en el sistema).
            
            println "id medico: " + id + " " + id.root + " " + id.extension
            
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
            return model
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
            
            /*
            def sections = [] // NECESARIO PARA EL MENU
            def subsections = [] // No hay porque estoy firmando el registro
            grailsApplication.config.hce.emergencia.sections.trauma.keySet().each { sectionPrefix ->
                sections << sectionPrefix
            }
            */
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
                    return [//episodeId: session.ehrSession?.episodioId,
                            //userId: session.ehrSession.userId, // no se usa
                            composition: composition,
                            patient: patient,
                            sections: sections,
                            subsections: subsections,
                            allSubsections: util.TemplateUtils.getDomainTemplates(session)
                           ]
                }

                // Verificacion del rol, debe ser medico
                // Este problema puede pasar si estoy logueado como medico pero firmo con datos de un adminsitrativo.
                // TODO: un posible tema a ver es que pasa si la persona firmante no es la persona
                //       que esta logueada, puede pasar y no necesariamente es un problema.
                //def roles = Role.withCriteria {
                def validities = RoleValidity.withCriteria {
                   eq('performer', auth.person)
                }
               
                //def roleKeys = roles.type
                def roleKeys = validities.role.type
                if ( !roleKeys.contains(Role.MEDICO) )
                {
                    flash.error = "Firma erronea, la persona firmante no es medico"
                    return [//episodeId: session.ehrSession?.episodioId,
                            //userId: session.ehrSession.userId, // no se usa
                            composition: composition,
                            patient: patient,
                            sections: sections,
                            subsections: subsections,
                            allSubsections: util.TemplateUtils.getDomainTemplates(session)
                           ]
                }


                def person = auth.person
                def id = person.ids[0] // FIXME: ver si tiene ID, DEBERIA TENER UN ID SIEMPRE, es un medico!

                if (!hceService.setVersionCommitter(version, id.root, id.extension))
                {
                    // TODO: i18n
                    flash.error = "Ocurrio un error al intentar firmar el registro clinico, intente de nuevo"
                    return [//episodeId: session.ehrSession?.episodioId,
                            //userId: session.ehrSession.userId, // no se usa
                            composition: composition,
                            patient: patient,
                            sections: sections,
                            subsections: subsections,
                            allSubsections: util.TemplateUtils.getDomainTemplates(session)
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
                ////                                      name: composition.name,
                ////                                      archetypeDetails: composition.archetypeDetails,
                ////                                      path: composition.path,
                ////                                      composer: null,
                ////                                      context: composition.context,
                ////                                      category: composition.category,
                ////                                      territory: composition.territory,
                ////                                      language: composition.language)
                ////composition.content.each{e ->
                ////    new_composition.addToContent(e)
                ////}
                //RMLoader.recorrerComposition(composition, new_composition)

                // Elimino movimiento y firma de la composition (de la copia)
                def composerAux = composition.composer
                def contentAux = composition.content
                composition.composer = null
                
                // Esto no es mas necesario en la reapertura, porque cerrar el registro ya
                // no implica que se movio al paciente.
                //hceService.eliminarMovimientoComposition(composition)

                
                //composition.save()

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
                println "XXXXXXXXXXXXXX------>>>> V0:" + version.getNumVersion()
                println "XXXXXXXXXXXXXX------>>>> V1:" + new_version.getNumVersion()

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
                
                return [//episodeId: session.ehrSession?.episodioId,
                        //userId: session.ehrSession.userId, // no se usa
                        composition: composition,
                        patient: patient,
                        sections: sections,
                        subsections: subsections,
                        allSubsections: util.TemplateUtils.getDomainTemplates(session)
                       ]
            }

            return [composition: composition,
                    patient: patient,
                    //episodeId: session.ehrSession?.episodioId,
                    //userId: session.ehrSession.userId, // no se usa
                    sections: sections, // necesario para el menu
                    subsections: subsections, // necesario para el menu
                    allSubsections: util.TemplateUtils.getDomainTemplates(session)
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