/**
 * @author Pablo Pazos Gutierrez (pablo.swp@gmail.com)
 *
 */

import demographic.party.*
import demographic.identity.PersonName
import demographic.role.*
import hce.core.support.identification.UIDBasedID

import hce.HceService

import tablasMaestras.TipoIdentificador

import hce.core.composition.*

import com.thoughtworks.xstream.XStream

import converters.DateConverter

// TEST
import demographic.PixPdqDemographicAccess

import org.codehaus.groovy.grails.commons.ApplicationHolder

// Para manejar eventos
import events.*

import util.RandomGenerator

class DemographicController{

    def hceService
    def demographicService
    
    def index = {
        // Por defecto es la busqueda de pacientes
       redirect(action:'admisionPaciente')
    }
    
    /**
     * Comienzo de la admision del paciente (proceso de identificacion
     * y seleccion del paciente). Es la pantalla de ingreso de criterio de busqueda.
     */
    def admisionPaciente = {
        // TODO: me deberia venir un id de episodio para el cual
        // quiero seleccionar un paciente.
        def tiposIds = TipoIdentificador.list()
        return [tiposIds: tiposIds]
    }
    
    /**
     * Busqueda de candidatos.
     */
    def findPatient = {
        
        println "PARAMS: " + params + "\n"

    //    def pixpdq = new PixPdqDemographicAccess()
        //pixpdq.findIdsById( new UIDBasedID(value:params.identificador) )
        //pixpdq.findPersonById( new UIDBasedID(value:params.identificador) )
        
        //def person = new Person()
        //person.properties = params
        //bindData(person, params, 'person')
        //println "Person: " + person
        
        //if (!params.identificador)
        /*
        if (!params.('person.ids[0].value'))
        {
            flash.message = "Identificador requerido"
            redirect(action:'admisionPaciente')
            return
        }
        */
        
        // TODO: aca va la consulta PIX al maciel.
        // Deberia hacerse como un strategy, definiendo una interfaz comun,
        // tanto para la entrada como para la salida.
        // Cual estrategia se elige, deberia sacarse de la config.
        // En la config dice que IMP se usa.
    
        
        def id = null
        if (params.identificador)
            id = new UIDBasedID(value:params.root+'::'+params.identificador)
        
        // TODO: usar rango de fechas... si viene solo una se usa esa como bd.
        
        // Para la fecha no funciona el bindData, lo hago a mano
        def bd = DateConverter.dateFromParams( params, 'fechaNacimiento_' ) // Si no vienen todos los datos, que sea null
        
        /*
        if (params.useBirthDate)
        {
            //String fecha = params.fechaNacimiento_day+'-'+params.fechaNacimiento_month+'-'+params.fechaNacimiento_year
            //java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MM-yyyy")
            //bd = sdf.parse(fecha)
            
            bd = DateConverter.dateFromParams( params, 'fechaNacimiento_' )
            //println "Date: " + bd
        }
        */

        // TODO: si no hay datos para los nombres, no crear pn.
        def pn = null
        if (params.'personName.primerNombre'  || params.'personName.segundoNombre'||
            params.'personName.primerApellido' || params.'personName.segundoApellido')
        {
            pn = new PersonName()
            bindData(pn, params, 'personName')
            //println "Person Name: " + pn
        }
        
        /*
        // TODO: todavia no uso el sexo
        def candidatos2 = demographicService.findByPersonData(pn, bd, null)
        println "CANDIDATOS2 : " + candidatos2
        */
        
        println "======================================="
        println "busca por: "
        println "   PN: "+ pn
        println "   BD: "+ bd
        println "======================================="
        
        //findByPersonDataAndIdAndRole( PersonName n, Date bithdate, String sex, UIDBasedID id, String roleType )
        //def candidatos = demographicService.findByPersonDataAndId(pn, bd, null, id)
        
        def candidatos = []
        try // La comunicacion puede tirar timeout
        {
            // busco en la base local
            candidatos = demographicService.findByPersonDataAndIdAndRole(
                    pn,
                    bd,
                    null,
                    id,
                    Role.PACIENTE )
    
            // Si el IMP es remoto, salvo los resultados localmente como un cache para facilitar el pedido
            // y mostrado de los datos de los pacientes seleccionados.
            // TODO: vaciar el cache despues de cierto tiempo, y si el paciente de un episodio no esta en
            //       el repositorio local y el IMP es remoto, pedirlo de nuevo con PDQ y cachearlo.
            //       El borrado deberia hacerse con cuidado de no eliminar personal del hospital, el cual
            //       se guarda en la misma tabla que los pacientes y tiene logins asignados.
            //       
            if (!ApplicationHolder.application.config.hce.patient_administration.serviceType.local)
            {
                def candidatosCache = candidatos // Los que se trageron del IMP remoto
                candidatos = [] // Los que se cargan del cache, incluyendo los que se trageron y se guardaron ahora en cache.
                
                candidatosCache.each{ per ->
                    
                    // Ver si esta en cache
                    
                    def perid = per.ids.toArray()[0]
                    def cache = Person.withCriteria {
                        ids {
                            eq('value', perid.value)
                        }
                    }
                    
                    // Ya esta en cache
                    if (cache.size()>0)
                    {
                        // Toma la entrada del cache para no ingresar 2 veces el mismo paciente en el cache
                        per = cache[0]
                    }
                    else // No esta
                    {
                        // FIXME: falta asignar rol paciente.
                        if ( !per.save(flush:true) )
                        {
                            println "Error al salvar persona en cache: " + per.errors
                        }
                        
                        def role = new Role(timeValidityFrom:new Date(), type:Role.PACIENTE, performer:per)
                        if (!role.save())
                        {
                            println "Error al salvar rol en cache: " + role.errors
                        }
                    }
                    
                    // Todos los pacientes resultados de la busqueda, los que ya
                    // estaban en cache y los que se guardaron ahora.
                    candidatos << per
                }
            }
        }
        catch (Exception e)
        {
            flash.message = "Ocurrio un error en la comunicacion, intente de nuevo"
            println "Ocurrio un error en la comunicacion " + e.getMessage()
        }
        
        // ==================================================================
        // TEST
        //pixpdq.findByPersonData(pn, bd, "M")
    //    def result = pixpdq.findByPersonData(pn, bd, null)
    //    XStream xstream = new XStream()
    //    println xstream.toXML(result) + "\n\n"
        // /TEST
        // ==================================================================
        
        
        // OJO! los candidatos de pix y pdq no tienen ID!!!!!
        // lo puse para hacer el OR entre cand 1 y cand 2
//        def candidatos = candidatos1.plus( candidatos2 ) //.unique{ it.id }
        
        render(view:'listaCandidatos', model:[candidatos:candidatos])
        
    } // findPatient
    
    /**
     * Selecciona a un paciente en el sistema para ser atendido (identificacion positiva).
     * PRE: el paciente debe tener por lo menos 1 id.
     */
    def seleccionarPaciente = {
        
        // FIXME: esta hecho en base al id en la base, que pasa cuando la
        // seleccion se hace sobre un paciente en un IMP remoto y no esta en la base?
        
        // Guardo los resultados de consultar el IMP remoto en la base como cache.
        def persona = Person.get(params.id)
        
        // =====================================================================
        // 1) Si no hay un episodio seleccionado, muestro la patalla de show del
        // paciente que tiene un boton "crear episodio" para abrir un episodio
        // para ese paciente, es la apertura desde admision.
        
        // =====================================================================
        // 2) Si hay un episodio seleccionado, entonces admision o el medico esta
        // seleccionando un paciente para ese episodio. Es un paciente existente
        // o uno ingresado en el momento. Previo a asignar a esta persona al
        // episodio se debe verificar que no se tenga ya una persona seleccionada
        // (o simplemente pongo la que me digan y que ellos corrijan).
        // Cada correccion debe tener un log de quien lo hizo.
        // Vuelve a la pantalla principal del episodio seleccionado (show).

        if (!session.traumaContext?.episodioId) // caso 1)
        {
            println "No hay epidosio seleccionado"
            
            if (persona.ids.size() == 0) // Debe tener un id!
            {
                redirect( action : 'findPatient',
                          params : ['flash.message': 'El paciente seleccionado no tiene identificadores, debe tener por lo menos uno.'] )
                return
            }
            
            def ids = persona.ids.toArray()
            render( view:'show', model: [ persona: persona, root: ids[0].root, extension: ids[0].extension ])
        }
        else // caso 2)
        {
            println "Hay un episodio seleccionado"
            
            // Pide el episodio a la base para agregarle la participation del paciente
            def composition = Composition.get( session.traumaContext.episodioId )
            
            // PRE: el episodio no deberia tener un paciente asignado.
            // FIXME: esta tira una except si hay mas de un pac con el mismo id, hacer catch
            if ( hceService.getPatientFromComposition( composition ) )
            {
                flash.message = 'trauma.show.feedback.patientAlreadySelectedForThisEpisode'
                redirect( controller:'records', action:'show',
                          params: [id: session.traumaContext.episodioId, 'flash.message': 'trauma.show.feedback.patientAlreadySelectedForThisEpisode'] )
                return
            }
            
            //println "IDS: " + persona.ids
            
            // Crea la participacion del paciente para la compostion del episodio
            if (persona.ids.size() == 0) // Debe tener un id!
            {
                redirect( controller:'records', action:'show',
                          params: [id: session.traumaContext.episodioId, 'flash.message': 'El paciente seleccionado no tiene identificadores, debe tener por lo menos uno.'] )
                return
            }
            
            // Crea un PartyRef desde la composition hacia la persona usando una copia del id de la persona,
            // esto crea otra instancia de ObjectID con el valor igual al id de la persona.
            def ids = persona.ids.toArray()
            def partySelf = hceService.createPatientPartysSelf( ids[0].root, ids[0].extension )
            def participation = hceService.createParticipationToPerformer( partySelf )

            // TODO: agregar un participation a la composition deberia hacerse tambien en HceService.
            composition.context.addToParticipations( participation )
            
            // Si no le pongo flush:true parece que demora un poco mas en guardar el partyself y
            // vuelve a la pagina rapido y muestra que el episodio no tiene paciente.
            if (!composition.save(flush:true))
            {
                println "ERROR compo: " + composition.errors
            }
            
            
            // Ejecuta eventos cuando el paciente seleccionado con exito.
            EventManager.getInstance().handle("post_seleccionar_paciente_ok", [composition:composition, persona:persona])
            
            
            //println "ERROR participation: " + participation.errors
            //println "ERROR partySelf: " + partySelf.errors
            
            // FIXME: cuando selecciona un paciente y vuelve al show del episodio,
            //        no se ve el paciente, si se hace reload de la pagina, se ve el paciente...
            //        puede ser un tema de aca (hay que hacer flush de la session o algo),
            //        o es un tema de carga lazy en el records.show para las participations del
            //        episodio.
            
            redirect( controller:'records', action:'show',
                      params: [id: session.traumaContext.episodioId] )
        }
        
        
        
        // Mientras rederea aprovecho para lanzar el evento
        EventManager.getInstance().handle("paciente_seleccionado",
          [
            patient: persona,
            episodeId: session.traumaContext?.episodioId // puede ser null
          ]
        )
        
        //render('Selecciona paciente: ' + persona)
    }
    
    /**
     * Agrega un nuevo paciente cuando el paciente a atender no esta en el sistema.
     */
    def agregarPaciente = {
        
        //println params
        
        // FIXME: si viene el id, verificar que no hay otro paciente con ese id, si lo hay, no dejar dar de alta, decirle que ya existe.
        
        // FIXME:  <%-- Solo se puede agregar un nuevo paciente si el repositorio es local --%>
        //  <g:if test="${ApplicationHolder.application.config.hce.patient_administration.serviceType.local}">
        
        if (params.doit)
        {
            /*
             * Account.withTransaction { status ->
                   def source = Account.get(params.from)
                   def dest = Account.get(params.to)
                   def amount = params.amount.toInteger()
                   if(source.active)
                   {
                     source.balance -= amount
                     if(dest.active) { dest.amount += amount }
                     else { status.setRollbackOnly() }
                 }
               }
             */
            
            // Veo si viene extension y root o si root es autogenerado
            def id = null
            if (params.root == TipoIdentificador.AUTOGENERADO)
            {
                // Verificar si este ID existe, para no repetir
                def extension = RandomGenerator.generateDigitString(8)
                id = UIDBasedID.create(params.root, extension)
                
                // Se deberia hacer con doWhile para no repetir el codigo pero groovy no tiene doWhile
                while ( UIDBasedID.findByValue(id.value) )
                {
                    extension = RandomGenerator.generateDigitString(8)
                    id = UIDBasedID.create(params.root, extension)
                }
            }
            else
            {
                if (params.extension && params.root)
                {
                    id = UIDBasedID.create(params.root, params.extension) // TODO: if !hasExtension => error
                    
                    // FIXME: verificar que no hay otro paciente con el mismo id
                    println "===================================================="
                    println "Busco por id para ver si existe: " + id.value
                    def existId = UIDBasedID.findByValue(id.value)
                    if (existId)
                    {
                        println "Ya existe!"
                        flash.message = "Ya existe la persona con id: " + id.value + ", verifique el id ingresado o vuelva a buscar la persona"
                        def tiposIds = TipoIdentificador.list()
                        return [tiposIds: tiposIds]
                    }
                    else
                       println "No existe!"
                }
                else
                {
                    // Vuelve a la pagina
                    flash.message = "identificador obligatorio, si no lo tiene seleccione 'Autogenerado' en el tipo de identificador"
                    def tiposIds = TipoIdentificador.list()
                    return [tiposIds: tiposIds]
                }
            }
            
            def person = new Person( params ) // sexo, fechaNac (no mas)
            
            def bd = DateConverter.dateFromParams( params, 'fechaNacimiento_' )
            person.setFechaNacimiento( bd )

            person.addToIds( id )
            
            def name = new PersonName(params)
            person.addToIdentities( name )
            
            if (!person.save()) println person.errors
            
            
            def role = new Role(timeValidityFrom: new Date(), type: "paciente", performer: person)
            if (!role.save()) println role.errors
            
            redirect(action:'seleccionarPaciente', id:person.id)
            return
        }
        
        // creacion de un nuevo paciente
        def tiposIds = TipoIdentificador.list()
        return [tiposIds: tiposIds]
    }
    
    /**
     * Editar un paciente con datos incompletos.
     * in: id
     */
    def edit = {
        
        println params
        
        // Si no viene el id, vuelvo a un punto seguro.
        if (!params.id)
        {
            if (session.traumaContext.episodioId)
                redirect(controller:'records', action:'show', id:session.traumaContext.episodioId)
            else
                redirect(controller:'records', action:'list')
            return
        }
        
        def patient = Person.get( params.id )
        def pn = patient.identities.find{ it.purpose == 'PersonName' }
        def tiposIds = TipoIdentificador.list()

        if (params.doit)
        {
            patient.setProperties( params )
            //pn.setProperties( params )
            
            println "PN:: " + pn
            
            // borra el viejo
            patient.removeFromIdentities(pn)
            pn.delete()

            // crea el nuevo
            pn = new PersonName(params)
            patient.addToIdentities( pn )


            /*
            def person = new Person( params ) // sexo, fechaNac (no mas)
            
            def bd = DateConverter.dateFromParams( params, 'fechaNacimiento_' )
            person.setFechaNacimiento( bd )
            
            person.addToIds( id )
            */
            
            if (!patient.save(flush:true))
            {
                println patient.errors
                return [patient:patient, pn:pn, tiposIds:tiposIds]
            }
            
            if (session.traumaContext.episodioId)
                redirect(controller:'records', action:'show', id:session.traumaContext.episodioId)
            else
                redirect(controller:'records', action:'list')
            return
        }
        
        // muestra pagina de edit
        return [patient:patient, pn:pn, tiposIds:tiposIds]
    }
}
