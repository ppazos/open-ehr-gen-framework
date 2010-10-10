
import hce.core.common.change_control.Version
import hce.core.composition.* // Composition y EventContext
import hce.core.data_types.quantity.date_time.*
import converters.DateConverter
import demographic.role.Role
import hce.core.data_types.encapsulated.DvMultimedia
import org.codehaus.groovy.grails.commons.ApplicationHolder
import cda.*
import util.*

/**
 * @author Pablo Pazos Gutierrez (pablo.swp@gmail.com)
 *
 */
class RecordsController {

    def demographicService
    def authorizationService
    def hceService
    
    // FIXME: tambien esta implementadas en GuiGenController
    
    /**
     * Devuelve un Map con los templates configurados para el dominio actual.
     *
     * this.getDomainTemplates()
     *
     * @return Map
     */
    private Map getDomainTemplates()
    {
        def routes = grailsApplication.config.domain.split('/') // [hce, trauma]
        def domainTemplates = grailsApplication.config.templates
        routes.each{
            domainTemplates = domainTemplates[it]
        }
        //println domainTemplates
        
        return domainTemplates
    }
    
    /**
     * Devuelve todos los prefijos de identificadores de templates del domino actual.
     * @return
     */
    private List getSections()
    {
        def sections = []
        this.getDomainTemplates().keySet().each {
            sections << it
        }
        
        return sections
    }
    
    /**
     * Obtiene las subsecciones de una seccion dada.
     *
     * this.getSubsections('EVALUACION_PRIMARIA')
     *
     * @param section es el prefijo del id de un template
     * @return List
     */
    private List getSubsections( String section )
    {
        // Lista de ids de templates
        def subsections = []

        this.getDomainTemplates()."$section".each { subsection ->
           subsections << section + "-" + subsection
        }
        
        return subsections
    }
    
    
    def index = {

       redirect(action:'list')
    }
    
    // TODO: vista
    // Pantalla 2.1- Escritorio Medico-Administrativo
    def list = {
        
        // TODO: poner el usuario en sesion cuando halla login.
        // FIXME: esto deberia hacerse con filters
        if (!session.traumaContext)
        {
        /* ahora debe pasar por login primero!
           session.traumaContext = new HCESession(
                                 userId: 1234 // FIXME: depende del login
                               )
        */
        }
        else { // deselecciona el episodio que este seleccionado
            session.traumaContext.episodioId = null
        }
    
        def compos = Composition.list()
        return [compositions: compos,
                userId: session.traumaContext.userId ]
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
            //def composition = hceService.createComposition( params.startDate, params.otherContext )
           
            // TODO: verificar si se crea para un paciente:
            // - buscarlo por id en el servicio demografico
            // - asociarlo como subject de la composition
            // - guardar todo
            
            // FIXME: si hay un paciente seleccionado no deberia venir en params,
            //        deberia estar en HCESession.
            if (params.root && params.extension) // si viene el id del paciente
            {
                println "Se crea un episodio para el paciente seleccionado"
                def partySelf = hceService.createPatientPartysSelf(params.root, params.extension)
                def participation = hceService.createParticipationToPerformer( partySelf )
                composition.context.addToParticipations( participation )
            }
            
            //XStream xstream = new XStream()
            //render( text: xstream.toXML(composition), contentType:'text/xml' )
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
            
            redirect(action:'list')
            return
        }
    }
    
    def show = {
       
       // TODO: poner en sesion el episodio que se esta viendo
       println "Show: " + params
       
       
       // Si expira la sesion tengo que volver al listado para crearla de nuevo
       // FIXME: esto deberia estar en un pre-filter
       if (!session.traumaContext)
       {
           redirect(action:'list')
           return
       }
       
       // Actualizacion de contexto, esta seleccionado un unico episodio
       session.traumaContext.episodioId = Integer.parseInt(params.id)
       
       
       def composition = Composition.get( params.id )
       
       // FIXME:
       // La primera vez que se muestra luego de seleccionar un paciente, esto da null.
       // Ver si es un tema de la carga lazy de las participations y si se resuelve con carga eager.
       // FIXME: esta tira una except si hay mas de un pac con el mismo id, hacer catch
       def patient = hceService.getPatientFromComposition( composition )


       // NECESARIO PARA EL MENU
       def sections = this.getSections()
       //def subsections = this.getSubsections(templateId.split("-")[0]) // this.getSubsections('EVALUACION_PRIMARIA')
       

       // patient puede ser null si todavia no se selecciono un paciente para el episodio,
       // p.e. si la atencion es de urgencia, se atiente primero y luego se identifica al paciente.
       return [composition: composition,
               patient: patient,
               episodeId: session.traumaContext?.episodioId,
               userId: session.traumaContext.userId,
               sections: sections, // necesario para el menu
               allSubsections: this.getDomainTemplates() 
               //grailsApplication.config.hce.emergencia.sections.trauma // necesario para el menu
              ]
    }
    
    
    // TODO: vista listando links a templates segun config.
    // Pantalla 5.1- Registro Clinico
    def registroClinico = {
            
       //println grailsApplication.config.hce.emergencia.sections.getClass() // ConfigObject extends LinkedHashMap
        
       // FIXME: desde que esta el filter del login esto no es necesario.
       // DEBE haber un episodio seleccionado para poder asociar el registro clinico.
       if (!session.traumaContext?.episodioId)
       {
           flash.message = 'trauma.list.error.noEpisodeSelected'
           redirect(controller:'trauma', action:'list')
           return
       }
       
       /*
       def sections = [:]
       grailsApplication.config.hce.emergencia.sections.trauma.keySet().each { sectionPrefix ->
           grailsApplication.config.hce.emergencia.sections.trauma."$sectionPrefix".each { section ->
            
               if (!sections[sectionPrefix]) sections[sectionPrefix] = []
             
               // Tiro la lista de esto para cada "section prefix" que son los templates
               // de las subsecciones de la seccion principal.
               //println sectionPrefix + "-" + section
               sections[sectionPrefix] << sectionPrefix + "-" + section
           }
       }
       */
       def domainTemplates = this.getDomainTemplates()
       
       def sections = [:]
       domainTemplates.keySet().each { sectionPrefix ->
           domainTemplates."$sectionPrefix".each { section ->
            
               if (!sections[sectionPrefix]) sections[sectionPrefix] = []
             
               // Tiro la lista de esto para cada "section prefix" que son los templates
               // de las subsecciones de la seccion principal.
               //println sectionPrefix + "-" + section
               sections[sectionPrefix] << sectionPrefix + "-" + section
           }
       }
       
       //def subsections = this.getSubsections(templateId.split("-")[0]) // this.getSubsections('EVALUACION_PRIMARIA')
       
       
       def composition = Composition.get( session.traumaContext?.episodioId )

// FIXME: esta tira una except si hay mas de un pac con el mismo id, hacer catch
       def patient = hceService.getPatientFromComposition( composition )
         
       return [sections: sections,
               composition: composition,
               episodeId: session.traumaContext?.episodioId,
               patient:patient,
               userId: session.traumaContext.userId]
    }
    
    def registroClinico2 = {
    
        def section = params.section
        //def firstSubSection = grailsApplication.config.hce.emergencia.sections.trauma."$section"[0]
        
        def subsections = this.getSubsections(section) // this.getSubsections('EVALUACION_PRIMARIA')
        def firstSubSection = subsections[0]
        
        //println "section: " + section
        //println "firstSubSection: " + firstSubSection
        
        def composition = Composition.get( session.traumaContext?.episodioId )

// FIXME: esta tira una except si hay mas de un pac con el mismo id, hacer catch
        def patient = hceService.getPatientFromComposition( composition )

        // FIXME: mismo codigo que en GuiGen generarTemplate
        if ( hceService.isIncompleteComposition( composition ) )
        {
	        //g.hasContentItemForTemplate( episodeId: session.traumaContext?.episodioId, templateId: section+'-'+firstSubSection)
            def item = hceService.getCompositionContentItemForTemplate(composition, section+'-'+firstSubSection)
            //if (item)
            //{
	            //println "==========-------------============"
	            //println "aha: " + it
	            //println "==========-------------============"
                
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
	                         params: [templateId: firstSubSection, //section+'-'+firstSubSection,
	                                  episodeId: session.traumaContext?.episodioId,
                                      patient:patient,
	                                  userId: session.traumaContext.userId,
                                      id: item.id])
                    return
	            }
	            else
	            {
	                redirect(controller: 'guiGen',
    		                 action: 'generarTemplate',
    		                 params: [templateId: firstSubSection, //section+'-'+firstSubSection,
            		                  episodeId: session.traumaContext?.episodioId,
                                      patient:patient,
            		                  userId: session.traumaContext.userId])
                    return
	            }
            //}
        }
        else
        {
            flash.message = "registroClinico.warning.noHayRegistroParaLaSeccion"
            redirect( action: 'show', id: session.traumaContext?.episodioId)
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
     * in: id episode id
     */
    def signRecord = {
        
        def composition = Composition.get( params.id )

        if (!composition)
        {
            redirect(action:'list')
            return
        }
        
// FIXME: esta tira una except si hay mas de un pac con el mismo id, hacer catch
        def patient = hceService.getPatientFromComposition( composition )


		// Es necesario para mostrar el menu
        def sections = this.getSections()
        def subsections = [] // No hay porque estoy firmando el registro


        flash.message = null
        flash.error = null
        
        if (params.doit)
        {
            if (composition.composer)
            {
                flash.error = "trauma.sign.registryAlreadySigned"
                return [episodeId: session.traumaContext?.episodioId,
                        userId: session.traumaContext.userId,
                        composition: composition,
                        patient: patient,
		                sections: sections,
		                subsections: subsections,
		                allSubsections: this.getDomainTemplates()
                        //grailsApplication.config.hce.emergencia.sections.trauma
                        ]
            }
            
            def auth = authorizationService.getLogin(params.user, params.pass)
            if (!auth)
            {
                // TODO: i18n
                flash.error = "Firma erronea, verifique sus datos"
                return [episodeId: session.traumaContext?.episodioId,
                        userId: session.traumaContext.userId,
                        composition: composition,
                        patient: patient,
		                sections: sections,
		                subsections: subsections,
		                allSubsections: this.getDomainTemplates() 
                        //grailsApplication.config.hce.emergencia.sections.trauma
                        ]
            }
            
            // Verificacion del rol, debe ser medico
            // Este problema puede pasar si estoy logueado como medico pero firmo con datos de un adminsitrativo.
            // TODO: un posible tema a ver es que pasa si la persona firmante no es la persona
            //       que esta logueada, puede pasar y no necesariamente es un problema.
            def roles = Role.withCriteria {
                eq('performer', auth.person)
            }
            
            def roleKeys = roles.type
            if ( !roleKeys.contains(Role.MEDICO) )
            {
                flash.error = "Firma erronea, la persona firmante no es medico"
                return [episodeId: session.traumaContext?.episodioId,
                        userId: session.traumaContext.userId,
                        composition: composition,
                        patient: patient,
		                sections: sections,
		                subsections: subsections,
		                allSubsections: this.getDomainTemplates()
                        //grailsApplication.config.hce.emergencia.sections.trauma
                        ]
            }
                
            
            def person = auth.person
            def id = person.ids[0] // FIXME: ver si tiene ID, DEBERIA TENER UN ID SIEMPRE, es un medico!

            if (!hceService.setCompositionComposer(composition, id.root, id.extension))
            {
                // TODO: i18n
                flash.error = "Ocurrio un error al intentar firmar el registro clinico, intente de nuevo"
                return [episodeId: session.traumaContext?.episodioId,
                        userId: session.traumaContext.userId,
                        composition: composition,
                        patient: patient,
		                sections: sections,
		                subsections: subsections,
		                allSubsections: this.getDomainTemplates()
                        //grailsApplication.config.hce.emergencia.sections.trauma
                        ]
            }

            // Cambia el estado del regsitro en su VERSION
            def version = Version.findByData( composition )
            version.lifecycleState = Version.STATE_SIGNED
            version.save()

            flash.message = "Registro firmado correctamente"
            return [episodeId: session.traumaContext?.episodioId,
                    userId: session.traumaContext.userId,
                    composition: composition,
                    patient: patient,
		            sections: sections,
		            subsections: subsections,
		            allSubsections: this.getDomainTemplates()
                    //grailsApplication.config.hce.emergencia.sections.trauma
                    ]
        }
        
        return [episodeId: session.traumaContext?.episodioId,
                userId: session.traumaContext.userId,
                composition: composition,
                patient: patient,
		        sections: sections,
		        subsections: subsections,
		        allSubsections: this.getDomainTemplates() 
                //grailsApplication.config.hce.emergencia.sections.trauma
                ]
    }

    //-------------------------------------------------------------------------------------------------------------
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
            def sections = this.getSections()
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
                    return [episodeId: session.traumaContext?.episodioId,
                            userId: session.traumaContext.userId,
                            composition: composition,
                            patient: patient,
                            sections: sections,
                            subsections: subsections,
                            allSubsections: this.getDomainTemplates() 
                            //grailsApplication.config.hce.emergencia.sections.trauma
                            ]
                }

                // Verificacion del rol, debe ser medico
                // Este problema puede pasar si estoy logueado como medico pero firmo con datos de un adminsitrativo.
                // TODO: un posible tema a ver es que pasa si la persona firmante no es la persona
                //       que esta logueada, puede pasar y no necesariamente es un problema.
                def roles = Role.withCriteria {
                    eq('performer', auth.person)
                }

                def roleKeys = roles.type
                if ( !roleKeys.contains(Role.MEDICO) )
                {
                    flash.error = "Firma erronea, la persona firmante no es medico"
                    return [episodeId: session.traumaContext?.episodioId,
                            userId: session.traumaContext.userId,
                            composition: composition,
                            patient: patient,
                            sections: sections,
                            subsections: subsections,
                            allSubsections: this.getDomainTemplates() 
                            //grailsApplication.config.hce.emergencia.sections.trauma
                            ]
                }


                def person = auth.person
                def id = person.ids[0] // FIXME: ver si tiene ID, DEBERIA TENER UN ID SIEMPRE, es un medico!

                if (!hceService.setVersionCommitter(version, id.root, id.extension))
                {
                    // TODO: i18n
                    flash.error = "Ocurrio un error al intentar firmar el registro clinico, intente de nuevo"
                    return [episodeId: session.traumaContext?.episodioId,
                            userId: session.traumaContext.userId,
                            composition: composition,
                            patient: patient,
                            sections: sections,
                            subsections: subsections,
                            allSubsections: this.getDomainTemplates() 
                            //grailsApplication.config.hce.emergencia.sections.trauma
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
                hceService.eliminarMovimientoComposition(composition)

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

                if(new_version.save())
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
                
                return [episodeId: session.traumaContext?.episodioId,
                        userId: session.traumaContext.userId,
                        composition: composition,
                        patient: patient,
                        sections: sections,
                        subsections: subsections,
                        allSubsections: this.getDomainTemplates()
                        //grailsApplication.config.hce.emergencia.sections.trauma
                        ]
            }

            return [composition: composition,
                    patient: patient,
                    episodeId: session.traumaContext?.episodioId,
                    userId: session.traumaContext.userId,
                    sections: sections, // necesario para el menu
                    subsections: subsections, // necesario para el menu
                    allSubsections: this.getDomainTemplates()
                    //grailsApplication.config.hce.emergencia.sections.trauma // necesario para el menu
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
