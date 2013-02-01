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
/**
 * @todo implementar el EventContext.otherContext<ItemStructure>
 * @todo implementar el Composition.composer<PartyProxy>
 */
package hce

import org.codehaus.groovy.grails.commons.ApplicationHolder

import hce.core.composition.* // Composition y EventContext
import data_types.text.* // DvText, DvCodedText, ...
import data_types.quantity.date_time.*
import hce.core.composition.content.navigation.Section

// other context
import hce.core.datastructure.itemstructure.ItemSingle
import hce.core.datastructure.itemstructure.representation.Element

import hce.core.common.archetyped.Archetyped

// Crear party proxy para el paciente
import hce.core.common.generic.*
import support.identification.*

import demographic.party.Person

import domain.Domain
import workflow.WorkFlow
import workflow.Stage
import templates.Template
import hce.core.common.archetyped.Locatable
import hce.core.composition.content.ContentItem
import hce.core.common.change_control.Version

import com.thoughtworks.xstream.XStream
import util.*
import hce.core.composition.content.entry.*

/**
 * @author Pablo Pazos Gutierrez (pablo.swp@gmail.com)
 *
 * El implements es necesarioa para acceder al servicio desde
 * clases del src/groovy segun: http://www.grails.org/Services
 */
class HceService implements serviceinterfaces.HceServiceInterface  {

    def demographicService
    
    /**
     * Crea la composition para el formulario de trauma.
     * No usa arquetipos.
     * @param workflowId identificador del wf donde se creó la composition.
     */
    //def Composition createComposition( String _startTime, String _otherContext, Long workflowId )
    def Composition createComposition( Date _startTime, String _otherContext, Long workflowId )
    {
        // TODO: falta implementacion Coposition.otherContext

        def compo = new Composition(path:"/", workflowId:workflowId, startTime: _startTime) // El formulario de trauma!
        
        // ========================================================
        // Contenido de la HCE, se setea a medida que se va ingresando.
        //compo.addToContent( getSection() )
        // ========================================================
            
        // ========================================================
        // TODO: falta el "composer" usando party proxy
        // Es el medico responsable de la atencion del paciente
        // ========================================================
        
        // FIXME: estos valores deben ser de configuracion de la aplicacion
        //        y ser utilizados solo para comunicacion de datos al exterior
        //        no para ser guardados en la DB.
        
        // Sacado de openehr terms en codeset languages
        compo.language = new CodePhrase(
           codeString: 'es-uy',
           terminologyId: TerminologyID.create('ISO_639-1', null)
        )
        // sacado de openehr terms en codeset countries
        compo.territory = new CodePhrase(
           codeString: 'UY',
           terminologyId: TerminologyID.create('ISO_3166-1', null)
        )
        
        // el registro de trauma es de tipo eventual, definido en el grupo
        // de conceptos "composition category" de la terminologia openehr.
        compo.category = new DvCodedText(
           value: "event",
           definingCode: new CodePhrase(
              codeString: '433',
              terminologyId: TerminologyID.create('openehr', null)
           )
        )
        
        // Se usa para marcar el inicio y fin de la atencion de trauma.
        // Tambien tiene "participations" que sirven para decir quien ingreso los datos.
        // TODO: al context le faltan participations y otherContext, tambien falta healthCareFacility pero no creo que lo usemos.
        compo.context = new EventContext(
        
          path: "/context",
          
          //startTime: new DvDateTime(value:_startTime),
          //startTime: RMExamples.getDvDateTime1('2009-11-23 23:14:00'), // Se tendria que poner al iniciar un nuevo episodio
          //endTime: RMExamples.getDvDateTime1('2009-11-24 06:37:00'), // Se tendira que poner al cerra el episodio
          //location: 'cama 5', // es el "point of care" con granularidad maxima, por ejemplo "cama 5", es opcional y no lo vamos a usar
           
          // setting es el servicio donde se da el cuidado, el valor se saca de la terminologia openehr
          setting: new DvCodedText(
            value: "emergency care", // TODO: traducir // FIXME: ahora dependen del dominio
            definingCode: new CodePhrase(
              codeString: '227',
              terminologyId: TerminologyID.create('openehr', null)
            ) // code phrase
          ), // setting
             
          // ==================================================
          // otherContext
          otherContext: new ItemSingle(
            path: "/context/otherContext",
            item: new Element(
              path: "/context/otherContext/item",
              value: new DvText(
                value: _otherContext,
                language: new CodePhrase(
                  codeString: 'es-uy',
                  terminologyId: TerminologyID.create('ISO_639-1', null)
                ),
                encoding: new CodePhrase(
                  codeString: 'UTF-8',
                  terminologyId: TerminologyID.create('IANA_character-sets', null)
                )
              ), // dvtext
              archetypeNodeId: "trauma.composition.otherContext.item",
              archetypeDetails: new Archetyped(
                archetypeId: 'composition.otherContext.Element.item',
                templateId: 'composition.otherContext.template.item'
              ),
              name: new DvText(
                value: 'hcet.otherContext.item'
              )
            ), // element
            archetypeNodeId: "trauma.composition.otherContext",
            archetypeDetails: new Archetyped(
              archetypeId: 'composition.otherContext.Element',
              templateId: 'composition.otherContext.template'
            ),
            name: new DvText(
              value: 'hcet.otherContext'
            )
          ), // other context, item single
          archetypeNodeId: "trauma.composition.context",
          archetypeDetails: new Archetyped(
            archetypeId: 'composition.context.EventContext',
            templateId: 'composition.context.template'
          ),
          name: new DvText(
            value: 'hcet.context'
          )
        ) // event context
        
        
        // Datos de Locatable
        compo.archetypeNodeId = "trauma.composition" // FIXME: no tengo arquetipados los compos
        compo.archetypeDetails = new Archetyped(
          archetypeId: 'trauma.composition',
          templateId: 'trauma.template'
        )
        compo.name = new DvText(
          value: 'hcet' // FIXME: depende del dominio
        )
        
        return compo
        
    } // createComposition
    
    /**
     * Cierra el registro, es una accion en la maquina de estados del registro.
     */
    //def boolean closeComposition( Composition composition, String endTime )
    def boolean closeComposition( Composition composition, Date endTime )
    {
        if (!endTime)
        {
           //def now = new Date()
           //endTime = DateConverter.toIso8601ExtendedDateTimeFormat( now )
           endTime = new Date()
        }
        
        // TODO: cambiar el estado creando un VERSION
        // FIXME: Se cambia afuera en un event handler, creo que se deberia
        // hacer todo aqui y llamar a esta desde el event handler...
        def version = Version.findByData( composition )
        version.lifecycleState = Version.STATE_COMPLETE
        version.save()
        
        //composition.context.endTime = new DvDateTime(value:endTime)
        composition.endTime = endTime
        
        // true o false
        return composition.save()
    }
    
    def boolean isIncompleteComposition( Composition composition )
    {
        def version = Version.findByData( composition )
        return version.lifecycleState == Version.STATE_INCOMPLETE
    }
    
    // FIXME: es la misma condicion que canSignRecord taglib
    def boolean isCompleteComposition( Composition composition )
    {
        def version = Version.findByData( composition )
        return version.lifecycleState == Version.STATE_COMPLETE
    }
    
    /**
     * Crea un PartySelf de un paciente a partir de su Id.
     */
    def PartySelf createPatientPartysSelf( String root, String extension )
    {
       /*
        * El modelo es:
        * PartySelf(hereda de PartyProxy)
        * PartySelf.externalRef<PartyRef>.objectId<UIDBasedID>(root, extension)
        */
       def patient = new PartySelf (
           externalRef: new PartyRef (
               namespace: "demographic", // FIXME: ver valores correctos
               type: "PERSON", // FIXME: ver valores correctos
               objectId: UIDBasedID.create(root, extension)
           )
       )
       
       return patient
    }
    
    /**
     * Le pone la referencia al responsable del registro.
     * @param composition
     * @param root
     * @param extension
     * @return boolean
     */
    def boolean setCompositionComposer( Composition composition, String root, String extension )
    {
        def composer = new PartyIdentified (
           externalRef: new PartyRef (
               namespace: "demographic", // FIXME: ver valores correctos
               type: "PERSON", // FIXME: ver valores correctos
               objectId: UIDBasedID.create(root, extension)
           )
        )
        composition.composer = composer
        
        return composition.save()
    }
    
    /**
     * Retorna el PartyIdentified correspondiente al composer de la composition. (es el responsable de la atencion)
     * 
     * @param composition
     * @return
     */
    def PartyIdentified getCompositionComposer( Composition composition )
    {
        return composition.composer
    }

    /**
     * Se usa en RecordsController.reopenRecord
     * Le pone la referencia al responsable de la creación de una versión.
     * @param composition
     * @param root
     * @param extension
     * @return boolean
     */
    def boolean setVersionCommitter( Version version, String root, String extension )
    {
        def composer = new PartyIdentified (
           externalRef: new PartyRef (
               namespace: "demographic", // FIXME: ver valores correctos
               type: "PERSON", // FIXME: ver valores correctos
               objectId: UIDBasedID.create(root, extension)
           )
        )
        version.committer = composer

        return version.save()
    }

    /**
     * Crea una participacion con el performer dado.
     */
    def Participation createParticipationToPerformer( PartyProxy _performer )
    {
        /*
         * TODO: todos los atributos son obligatorios!!!! function para el paciente???
         * DvText function <concept id="253" rubric="unknown"/>
         * DvCodedText mode <concept id="219" rubric="physically present"/>
         */
        def participation = new Participation(
          performer: _performer,
          function: new DvText(value:'subject of care'), // texto libre, no lo codifico.
          mode: new DvCodedText(
            value: 'physically present',
            definingCode: new CodePhrase(
              codeString: '219',
              terminologyId: TerminologyID.create('openehr', null)
            )
          )
        )
        
        return participation
    }
    
    
    /**
     * Obtiene el paciente PartySelf de una participacion en la composition si es que lo tiene.
     * Como las referencias a los pacientes son TODAS externas mediante PartyRef, sirve para 
     * obtener el ID de referencia al paciente en el IMP, ese ID es el que esta guardado en la composition.
     * pre: composition != null 
     */
    def PartySelf getPartySelfFromComposition( Composition composition )
    {
        // Si la composition tiene un paciente asignado (tiene un ID en
        // context.participations.PartySelf), con ese ID le pido al 
        // servicio demografico que me de la info del paciente.
        def participations = composition.context.participations
        def patientParticipation = null
        if ( participations.size() > 0 )
        {
           //println "hceService.getPartySelfFromComposition participations.size(): " + participations.size()
           patientParticipation = participations.find{ it.function.value == 'subject of care' } // FIXME: no usar 'subject of care', usar algo tipo el rol...
        }
        
        if (patientParticipation)
        {
           //println "hceService.getPartySelfFromComposition patientParticipation: " + patientParticipation
           //println "hceService.getPartySelfFromComposition patientParticipation.performer: " + patientParticipation.performer
           //println "hceService.getPartySelfFromComposition patientParticipation.performer.getClass(): " + patientParticipation.performer.getClass()
           //if (patientParticipation.performer instanceof PartySelf) // Dice que la clase es: hce.core.common.generic.PartyProxy_$$_javassist_49
           if (patientParticipation.performer.getClassName() == 'PartySelf')
           {
              //println "hceService.getPartySelfFromComposition return PartySelf"
              //return patientParticipation.performer // Error por javassist en lugar de PartySelf
              // Se recarga de la base la misma clase que ya tengo, esto resuelve el problema del javassist
              // o sea que patientParticipation.performer es lo mismo que el PartySelf que retorno aqui.
              return PartySelf.get(patientParticipation.performer.id)
           }
        }
        
        //println "hceService.getPartySelfFromComposition null"
        
        // No se encuentra el PartySelf de la composition
        return null
    }
    
    /**
     * Obtiene la Person, paciente de la composition, si
     * es que la composition tiene un paciente ya seteado.
     * Retorna null si el paciente no fue seteado.
     */
    def Person getPatientFromComposition( Composition composition )
    {
        def patient = null
        
        def patientProxy = this.getPartySelfFromComposition( composition ) // usa operacion local

        //println "hceService.getPatientFromComposition patientProxy: " + patientProxy
        
/** FIXME: me dio error al buscar un paciente teniendo la conf del IMP remoto del Maciel...
 * Caused by: java.lang.reflect.UndeclaredThrowableException
at hce.HceService$$EnhancerByCGLIB$$a9b08e53.getPatientFromComposition(<generated>)
at serviceinterfaces.HceServiceInterface$getPatientFromComposition.call(Unknown Source)
at events.handlers.VerificarCondicionCierreEventHandler.handle(script1269617715484.groovy:41)
at events.handlers.VerificarCondicionCierreEventHandler$handle.call(Unknown Source)
at events.EventManager$_handle_closure1.doCall(EventManager.groovy:47)
at events.EventManager.handle(EventManager.groovy:46)
at events.EventManager$handle.call(Unknown Source)
at DemographicController$_closure4.doCall(DemographicController.groovy:223)
at DemographicController$_closure4.doCall(DemographicController.groovy)
Caused by: java.lang.Exception: Se encuentran: 0 pacientes a partir del ID: UIDBasedID-> 2.16.840.1.113883.2.14.2.1::5021690
at hce.HceService.getPatientFromComposition(HceService.groovy:338)
at hce.HceService$$FastClassByCGLIB$$a9659a4f.invoke(<generated>)
at net.sf.cglib.proxy.MethodProxy.invoke(MethodProxy.java:149)
... 9 more
 */

        if (patientProxy)
        {  
           // TODO: probar esta funcion en el Maciel
           // FIXME: probar si creo 2 episodios para la misma persona a ver si no obtiene 2 pacientes...
           // List<Person> findPersonById( UIDBasedID id )
           def patients = demographicService.findPersonById( patientProxy.externalRef.objectId )

           //println "hceService.getPatientFromComposition findPersonById id: " + patientProxy.externalRef.objectId.value
           //println "hceService.getPatientFromComposition findPersonById: " + patients
           
           if (patients.size() == 0)
           {
               // no hago nada, el patient es null...
           }
           else if (patients.size() == 1) 
           {
               patient = patients[0]
           }
           else
           {
               // TODO: en teoria no deberia pasar pero en ningun lugar hay una restriccion explicita de no tener 2 pacientes con un id comun, hay que probar.
               println patients
               throw new Exception('Se encuentran: ' + patients.size() + ' pacientes a partir del ID: '+ patientProxy.externalRef.objectId )
           }
           
           // TODO: verificar que patientProxy.externalRef.objectId no tiene clase javassist
           if (!patientProxy.externalRef.objectId instanceof UIDBasedID)
              throw new Exception('Se espera un UIDBasedID y el tipo del id es: ' + patientProxy.externalRef.objectId.getClass())
           
        }
        
        return patient // null si no lo encuentra
    }
    
    /**
     * TODO: en el futuro el paciente podra tener varias compositions abieras
     *       en la misma sesion clinica, lo que importa es la sesion, no los documentos.
     * Devuelve la composition que no tenga fecha de finalizacion
     * para la persona que participe como paciente.
     */
    def Composition getActiveCompositionForPatient( Person person )
    {
        // Busca un PartySelf (un paciente) por cada id de la persona,
        // ese es el criterio de comparacion de personas por ahora.
        def iter = person.ids.iterator()
        def personId
        while (iter.hasNext())
        {
            personId = iter.next()
            
            // PUeden ser varios partySelfs, dependiendo si la persona fue ingresada mas de una vez.
            def partySelfs = PartySelf.withCriteria {
                externalRef { // PartSelf>PartyRef
                    objectId { // PartyRef>ObjectId
                       eq('value',personId.value)
                    }
                }
            }
            
            // Busca el contexto con la participacion del partySelf que no tenga fecha de fin (el espisodio esta activo)
            def contexts = EventContext.withCriteria {
                isNull('endTime') // episodio activo
                participations {
                    performer {
                        or {
                           partySelfs.id.each { pSelfId -> // pueden ser varios pSelf
                              eq('id', pSelfId)
                           }
                        }
                    }
                }
            }
            
            // Deberia haber 0..1 episodios activos para el paciente
            // FIXME: si hay un episodioa activo para un paciente no se puede crear otro...
            if (contexts.size()==1)
            {
                // Devuelve el episodio para el contexto
                return Composition.findByContext(contexts[0])
            }
        }
        
        // No se encuentra el episodio, puede ser que todavia no se haya creado,
        // pero si se haya identificado al paciente.
        return null
    }
    
    /**
     * Devuelve un map<ArchetypeID,Locatable>, con las referencias de los
     * arquetipos, de un template, a los respectivos nodos del RM.
     * Se usa para que el show y el edit sepan donde mostrar los objetos en
     * la web.
     * Este metodo seria mas un RM_UTILS que un HCEService.
     * @param template
     * @param rmNode
     * @return map<ArchetypeID,Locatable> // cuidado que es el ArchetypeID de la Java Ref Impl, no el de nuestro modelo.
     */
    def Map getRMRootsIndex( Template template, Locatable rmNode)
    {
        def idx = [:]
        def archIds = template.getArchetypeIDs()
        
        archIds.each { archId ->
            
            idx[archId] = findRootNodeForArchId( rmNode, archId )    
        }
        
        return idx
    }
    
    /**
     * Auxiliar para getRMRootsIndex, obtiene el nodo raiz de la estructura
     * RM rmNode tal que su archetypeDetails.archetypeId concuerda con el archId pasado.
     * @param rmNode
     * @param archId
     * @return
     */
    def Locatable findRootNodeForArchId ( Locatable rmNode, String archId )
    {
        if (rmNode.archetypeDetails.archetypeId == archId)
           return rmNode
        
        // llamadas recursivas
        
        // FIXME:
        // solo van por sections y entries porque son los slots que hoy manejamos,
        // si quisieramos soporte para indexar una estructura completa deberiamos
        // llegar a los ELEMENTS. Con este codigo recorre todas las sections y sus
        // hijos directos, pero no recorre mas abajo.
           
        // TODO: verificar que la clase de rmNode no es javassist
        if ( rmNode instanceof Section )
        {
            def iter = rmNode.items.iterator()
            def item
            def foundRmNode
            while (iter.hasNext())
            {
                item = iter.next()
                foundRmNode = findRootNodeForArchId( item, archId )
                if (foundRmNode) return foundRmNode
            }
        }
        
        // Si no se encuentra
        return null
    }
    
    /**
     * Esta operacion sirve para saber si ya hay un registro para una seccion o entry en la
     * composition que se le pasa. Esto es porque cada registro individual es un template y
     * cada template genera un contentItem en la composition del episodio actual.
     * Esta operacion no serviria en el caso general donde un template puede determinar una
     * composition o puede determinar elementos de mas bajo nivel que las sections y entries,
     * por ejemplo cuando hay templates anidados.
     * 
     * @param composition
     * @param templateId 'EVALUACION_PRIMARIA-via_aerea.v1' la comparacion no deberia considerar la version para poder mostrar registros anteriores hechos con versiones anteriores del mismo template.
     * @return
     */
    def ContentItem getCompositionContentItemForTemplate( Composition composition, String templateId )
    {
        def iter = composition.content.iterator()
        def item
        def found
        while (iter.hasNext())
        {
            item = iter.next()
            
            //println item.archetypeDetails.templateId +"=?"+ templateId
            //println item.archetypeDetails.templateId.split('\\.')[0] +"=?"+ templateId.split('\\.')[0]
            
            //if (item.archetypeDetails.templateId == templateId)
            if (item.archetypeDetails.templateId.split('\\.')[0] == templateId.split('\\.')[0]) // le saca el .vX y compara, asi no considera la version
            {
               found = item
               break
            }
        }

        return found
    }


    
    // FIXME:
    // Esto no es mas necesario en la reapertura, porque cerrar el registro ya
    // no implica que se movio al paciente.
    /*
    def eliminarMovimientoComposition(Composition composition){

        def listaEntry = composition.content
        composition.content = null
        listaEntry.each{e ->
            if (e.archetypeDetails.archetypeId != "openEHR-EHR-INSTRUCTION.movimientos.v1"){
                composition.addToContent(e)
            }
        }

        ////def entryMovimiento = getCompositionContentItemForTemplate(composition, "openEHR-EHR-INSTRUCTION.movimientos.v1")
        ////composition.content.remove(entryMovimiento)

        println "==============================================================222"
        println "==============================================================>>>>>>>>>>>>>>>>>>>>>>>>"
        def new_composition2 = new Composition()
        RMLoader.recorrerComposition(composition, new_composition2)
        println imprimirObjetoXML(new_composition2)
        println "==============================================================>>>>>>>>>>>>>>>>>>>>>>>>"
        println "==============================================================222"
    }
    */
    
    
    /**
     * Sirve para saber si un determinado dominio tiene templates definidos,
     * o sea si se puede registrar informacion clinica para ese dominio.
     */
    boolean domainHasTemplates( Domain domain )
    {
       //return (ApplicationHolder.application.config.templates2."${domainPath}".size() > 0)
	   
	    // Sino tiene algun workflow, no puede tener templates
	    if (domain.workflows.size() == 0) return false
	   
	    // Todos los workflows con stages
	    def wfs = domain.workflows.findAll{ it.stages.size() > 0 }

	    // Si ningun workflow tiene stages, no puede tener templates
	    if (!wfs) return false
	   
	    // Dentro de los wfs se fija si alguna stage tiene algun template
	    // TODO: verificar si solo con esto evito todos los if de arriba
       def wf = wfs.find{ wf ->
	     wf.stages.find{ stg -> 
		   stg.recordDefinitions.size() > 0
		 }
	   }
	   
	   return wf != null
    }
    
    
    void imprimirObjetoXML(Object o){
        println "-----------------"
        XStream xstream = new XStream();
        String xml = xstream.toXML(o);
        println xml
        println "-----------------"
    }
}