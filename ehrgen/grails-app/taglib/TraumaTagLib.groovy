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
import hce.core.composition.Composition
import hce.HceService 
import hce.CustomQueriesService
import authorization.LoginAuth
import demographic.DemographicService
//import demographic.identity.PersonName
import demographic.party.Person
import hce.core.common.change_control.Version
import demographic.role.*
import java.text.DecimalFormat;
import hce.core.common.generic.PartyIdentified // composer
import support.identification.*
import templates.Template

/**
 * @author Pablo Pazos Gutierrez (pablo.swp@gmail.com)
 */
class TraumaTagLib {
    
    def hceService
    def customQueriesService
    def demographicService
    
    /**
     * Necesaria para mostrar datos del paciente de una composition en las vistas.
     * composition Composition
     */
    def showPatientFromComposition = { attrs, body ->
        
       // TODO: esto deberia hacerse en un template y de aqui renderearlo nomas.
       
       def composition = attrs.composition
       if (!composition)
          throw new Exception("No se encuentra el episodio con id " + attrs.episodeId + " @TraumaTagLib")
       
       def person = hceService.getPatientFromComposition(composition)
       
       if (person)
          out << render(template:'../demographic/Person', model:[person:person])
    }
    
    def showCompositionComposer =  { attrs, body ->
        
       // TODO: esto deberia hacerse en un template y de aqui renderearlo nomas.
       
       def composition = attrs.composition
       if (!composition)
          throw new Exception("No se encuentra el episodio con id " + attrs.episodeId + " @TraumaTagLib")
       
       def partyIdentified = hceService.getCompositionComposer(composition)
       if (partyIdentified)
       {
          //out << '['+partyIdentified.externalRef.objectId.value+']' // externalRef es partyRef
          //out << partyIdentified.name // vacio
          
          // Mismo codigo que en hceService.getPatientFromComposition, que
          // resuelve la Person contra el servicio demografico a partir de su id.
          def person

          // TODO: probar esta funcion en el Maciel
          // FIXME: probar si creo 2 episodios para la misma persona a ver si no obtiene 2 pacientes...
          // List<Person> findPersonById( UIDBasedID id )
             def persons = demographicService.findPersonById( partyIdentified.externalRef.objectId )
  
          //println "hceService.getPatientFromComposition findPersonById id: " + patientProxy.externalRef.objectId.value
          //println "hceService.getPatientFromComposition findPersonById: " + patients
          
          if (persons.size() == 0)
          {
              // no hago nada, el patient es null...
          }
          else if (persons.size() == 1)
          {
              person = persons[0]
          }
          else
          {
              // TODO: en teoria no deberia pasar pero en ningun lugar hay una restriccion explicita de no tener 2 pacientes con un id comun, hay que probar.
              println persons
              throw new Exception('Se encuentran: ' + persons.size() + ' pacientes a partir del ID: '+ partyIdentified.externalRef.objectId )
          }
          
          // TODO: verificar que patientProxy.externalRef.objectId no tiene clase javassist
          if (!partyIdentified.externalRef.objectId instanceof UIDBasedID)
             throw new Exception('Se espera un UIDBasedID y el tipo del id es: ' + partyIdentified.externalRef.objectId.getClass())

          
          if (person)
          {
             // No es necesario mostrar identificadores ni sexo para el medico
             //out << render(template:'../demographic/Person', model:[person:person])
             
             // FIXME: el composer podria ser enfermera o tecnico y no corresponde Dr. o Dra.
             //        deberia depender de si tiene el rol medico u otro rol.
             out << ((person.sexo == 'M') ? 'Dr. ' : 'Dra. ')
             out << person.primerNombre +' '+ person.primerApellido
          }
       }
    }
    
    
    // Viene archetypeId o (rmtype y idMarchingKey)
    // in: episodeId
    def resumenEpisodio = { attrs, body ->
        
       // TODO: esto deberia hacerse en un template y de aqui renderearlo nomas.
       
       def composition = Composition.get( attrs.episodeId )
       if (!composition)
          throw new Exception("No se encuentra el episodio con id " + attrs.episodeId + " @TraumaTagLib 1")

        
        /*
        //-----------------------------------------------------------------------
        // Datos para mostrar RTS y RTSp
        //-----------------------------------------------------------------------
        def FR = customQueriesService.getFrecuenciaRespiratoriaEpisodio(composition)
        def PAS = customQueriesService.getPresionArterialSistolicaEpisodio(composition)
        def GCS = customQueriesService.getGlasgowComaScaleEpisodio(composition)
        def RTS = customQueriesService.getRTSEpisodio(composition)
        def RTSp = customQueriesService.getRTSpEpisodio(composition) 
  
        def resultRTS
        def resultRTSp
        if (RTS != null){ 
            DecimalFormat formateador = new DecimalFormat("####.##")
            resultRTS = message(code:'trauma.list.label.RTS') + ': ' + RTS.intValue() + ' (FR = ' + FR.doubleValue() + ', PAS = ' + PAS.doubleValue() + ', GCS = ' + GCS.doubleValue() + ')'
            resultRTSp = message(code:'trauma.list.label.RTSp') + ': ' + formateador.format(RTSp.doubleValue())
        }
        else{
            resultRTS = message(code:'trauma.list.label.FaltanDatosParaCalcularRTS')
            resultRTSp = message(code:'trauma.list.label.FaltanDatosParaCalcularRTSp')
        }
        */

        //-----------------------------------------------------------------------
        //-----------------------------------------------------------------------

        out << '<div id="resumen_episodio">'
        
        out <<   '<div><h2>' + message(code:'records.label.resumenEpisodio') + '</h2></div>'
        
        out <<   '<div style="width:90%; float: left;">' // comienzo-fin-observaciones-responsable
        out <<     '<div style="padding: 4px;">'
        out <<       '<span>' +  message(code:'records.list.label.startTime') +' / '+ message(code:'records.list.label.endTime') +': </span>'
        //out <<       '<span>' + g.format( date: composition.context.startTime?.toDate() ) + ' / '
        //out <<                  g.format( date: composition.context.endTime?.toDate() ) + '</span>'
        out <<       '<span>' + g.format( date: composition.startTime ) + ' / '
        out <<                  g.format( date: composition.endTime ) + '</span>'
        out <<     '</div>'

        //out << '</div>' // /comienzo-fin
        
        //out << '<div>' // observaciones-responsable
        out <<     '<div style="padding: 4px;">'
        out <<       '<span>'+ message(code:'records.list.label.observations') + '</span>: ' + composition.context.otherContext.item.value.value
        out <<     '</div>'
        
        
        // RESPONSABLE
        // Si han firmado, mostrar el responsable de la atencion.
        if (composition.composer)
        {
            def composer = PartyIdentified.get( composition.composer.id )
            //println "-------------------------------------"
            //println composer.externalRef
           
            def persons = demographicService.findPersonById( composition.composer.externalRef.objectId )
            def responsable
            if (persons.size() == 0)
            {
                // no hago nada, no se encuentra el responsable, aca hay un error de consistencia de datos en el sistema!
            }
            else if (persons.size() == 1)
            {
                responsable = persons[0]
            }
            else
            {
                // TODO: en teoria no deberia pasar pero en ningun lugar hay una restriccion explicita de no tener 2 pacientes con un id comun, hay que probar.
                //println persons
                throw new Exception('Se encuentran: ' + persons.size() + ' personas a partir del ID: '+ composition.composer.externalRef.objectId )
            }
            
            
            // TODO: mostrar un identificador
            //def nombres = responsable.identities.find{ it.purpose == 'PersonName' }
            
            out << '<div style="padding: 4px;">'
            out << message(code:'trauma.list.label.composer') + ': ' +
                   responsable.primerNombre + ' ' +
                   ((responsable.segundoNombre) ? (responsable.segundoNombre + ' ') : '') +
                   responsable.primerApellido + ' ' +
                   ((responsable.segundoApellido) ? responsable.segundoApellido : '')
            out << '</div>'
        }
        // /RESPONSABLE
        
        out <<   '</div>' // /comienzo-fin-observaciones-responsable
        
        /*
        // Separacion entre (Comienxo-Final o Triage) y RTS-RTSp
        out <<   '<div style="float: left; border-style: hidden; border-color: teal; border-width: 2px">'
        out <<     '&nbsp;&nbsp;&nbsp;'
        out <<   '</div>'
        // RTS y RTSp 
        out <<   '<div style="float:left;background-color:#AAAAFF;">'
        out <<     '<div style="border-style:solid;border-color:#000;border-width:1px;text-align: left; padding: 4px; padding-left: 10px; padding-right: 10px;">'
        out <<       resultRTS 
        out <<     '</div>'
        out <<     '<div style="border-style:solid;border-color:#000;border-width:1px; border-top:0px; text-align: left; padding: 4px; padding-left: 10px; padding-right: 10px;">'
        out <<       resultRTSp
        out <<     '</div>'
        out <<   '</div>'
        out <<   '<div style="clear:both;"></div>'
        out << '</div>'
        out << '<div>&nbsp;</div>'
        */

        // FIXME: sacarlo a una taglib que se filtre por dominio y templateId
        // o sea, se muestran todas las taglibs si se esta en el dominio o dominio y templateId actual.
        // asi puedo implementar
        //-----------------------------------------------------------------------
        // Datos para mostrar Triage
        //-----------------------------------------------------------------------
        def color = triageColor( composition )
        // Para mostrar el color del triage si es que ya fue registrado.
        if (color)
        {
            // Separacion entre Comienxo-Final y Triage
            //out << '<div style="float: left;">&nbsp;&nbsp;&nbsp;</div>'
            // Triage
            out << '<div style="float: left; border-style: solid; border-color: #000; border-width: 1px; text-align: center; padding: 16px; padding-left: 20px; padding-right: 20px; background-color:'+color+';">T</div>'
            
            //out << '<div style="float: left;">&nbsp;&nbsp;&nbsp;</div>'
        }
        
        out << '</div>' // resumen_episodio
    }
    
    /**
     * TagLib condicional, si el component tiene el item para el template, ertorna el body.
     * in: templateId 'EVALUACION_PRIMARIA-via_aerea.v1' (la verificacion deberia hacerse sin considerar la version, para poder mostrar registros viejos hechos con templates en otras versiones).
     * in: episodeId (composition.id)
     */
    def hasContentItemForTemplate = { attrs, body ->
        
        def composition = Composition.get( attrs.episodeId )
        if (!composition)
            throw new Exception("No se encuentra el episodio con id " + attrs.episodeId + " @TraumaTagLib 2")
        
        if (!attrs.templateId)
            throw new Exception("El templateId es obligatorio @TraumaTagLib 2")
        
        def item = hceService.getCompositionContentItemForTemplate(composition, attrs.templateId)

        // Mando un boolean como var para que en la vista pueda discutir si hay o no un item en la composition.
        //out << body(item!=null)
        
        out << body( hasItem:(item!=null), itemId:item?.id, item:item)
    }
    
    /**
     * in: stage
     * in: episodeId (composition.id)
     */
    def hasContentItemForStage  = { attrs, body ->
    
       def composition = Composition.get( attrs.episodeId )
        if (!composition)
            throw new Exception("No se encuentra el episodio con id " + attrs.episodeId + " @TraumaTagLib 2")
        
        if (!attrs.stage)
            throw new Exception("La stage es obligatoria @TraumaTagLib 2")
        
        def item
        for (Template template: attrs.stage.recordDefinitions)
        {
           item = hceService.getCompositionContentItemForTemplate(composition, template.templateId)
           if (item) break
        }

        out << body( hasItem:(item!=null), itemId:item?.id, item:item)
    }
    
    /**
     * Imprime el body si la composition actual tiene paciente asignado
     */
    def compositionHasPatient = { attrs, body ->
        
        def composition = Composition.get( attrs.episodeId )
        if (!composition) return
        
        // FIXME: esta tira una except si hay mas de un pac con el mismo id, hacer catch
        def patient = hceService.getPatientFromComposition( composition )
        if ( patient )
        {
            out << body(patient:patient)
        }
    }
    def compositionHasNoPatient = { attrs, body ->
        
        def composition = Composition.get( attrs.episodeId )
        if (!composition) return
        
        // FIXME: esta tira una except si hay mas de un pac con el mismo id, hacer catch
        if ( !hceService.getPatientFromComposition( composition ) )
        {
            out << body()
        }
    }
    
    
    def isIncompleteRecord = { attrs, body ->
        
        def composition = Composition.get( attrs.episodeId )
        def version = Version.findByData( composition )
        out << body( answer: (version.lifecycleState == Version.STATE_INCOMPLETE) )
    }
    
    /**
     * Verifica la condicion para poder firmar el registro.
     * La condicion es que el registro este completo, esta condicion 
     * se da cuando se mueve al paciente y hay un paciente seleccionado
     * para la composition. Dicha condicion es verificada desde el
     * event handler VerificarCondicionCierre.
     */
    /* Desde que el cerrado y firmado del registro lo hace explicitamente el medico,
     * esta tag no tiene sentido. Ver: http://code.google.com/p/open-ehr-gen-framework/issues/detail?id=9
    def canSignRecord = { attrs, body ->
        
        def composition = Composition.get( attrs.episodeId )
        def version = Version.findByData( composition )
        //def item = hceService.getCompositionContentItemForTemplate( composition, "COMUNES-movimiento_paciente" )
        //if (item)
        if (version.lifecycleState == Version.STATE_COMPLETE)
            out << body()
    }
    */
    def isNotSignedRecord = { attrs, body ->
       
       def composition = Composition.get( attrs.episodeId )
       if (!composition.composer)
           out << body()
    }
    
    def isSignedRecord = { attrs, body ->
        
        def composition = Composition.get( attrs.episodeId )
        if (composition.composer)
            out << body()
    }
    
    def reabrirEpisodio = { attrs, body ->
        def composition = Composition.get( attrs.episodeId )
        def version = Version.findByData( composition )
        if (version.lifecycleState == Version.STATE_SIGNED){
            out << body()
        }
    }

    def stateForComposition = { attrs, body ->
        
        def composition = Composition.get( attrs.episodeId )
        def version = Version.findByData( composition )
        
        //println "=========================================================="
        //println "Lifecycle state: " + version.lifecycleState.getClass()
        // BUG de Grails 1.2: aunque retorno string, a la vista llega org.codehaus.groovy.grails.web.util.StreamCharBuffer
        out << version.lifecycleState
    }
    
    
    /**
     * Para mostrar un color en la web respecto al codigo del triage.
     */
    private String triageColor( Composition composition )
    {
        def triageCode = customQueriesService.getTriageClasification( composition )

        if (triageCode)
        {
            switch(triageCode)
            {
                // Los codigos estan en el arquetipo de triage de trauma.
                case 'at0003':
                    return '#55cc55'
                break;
                case 'at0004':
                    return '#dddd55'
                break;
                case 'at0005':
                    return '#ff5555'
                break;
                case 'at0006':
                    return'#cccccc'
                break;
                case 'at0007':
                    return '#333333'
                break;
            }
        }
    }
    
    /**
     * Muestra el body si puede mostrar el registro clinico.
     * En si solo los medicos y enfermeras pueden acceder al registro clinico,
     * otros roles como los administrativos no podran accederlo.
     */
    def canFillClinicalRecord = { attrs, body ->

        def login = LoginAuth.get( session.ehrSession.userId )
        
        // FIXME: no puedo poner domain objects en session: http://grails.1312388.n4.nabble.com/Best-way-to-cache-some-domain-objects-in-a-user-session-td3820978.html
        //def login = session.ehrSession.login

        // Roles de la persona
        /*
        def roles = Role.withCriteria {
            eq('performer', login.person)
        }
        */
        /*
        def validity = RoleValidity.withCriteria {
           eq('performer', login.person)
        }
        */
        
        //def roleKeys = roles.type
        def roleKeys = []
        
        //println "login: " + login
        //println "person: " + login.person
        //println "roleValidity: " + login.person.roles // [RoleValidity]
        //println "role: " + login.person.roles.role // da: demographic.party.Actor.roles (debe ser algo interno de grails)
        login.person.roles.each { roleValidity ->
           
           // FIXME: esto deberia hacerse dinamico y a nivel de controller y accion, no fijo y a nivel general de 'registro clinico'
           if ( [Role.MEDICO, Role.ENFERMERIA].contains( roleValidity.role.type ) )
           {
              out << body()
              return
           }
        }
        
        /*
        if ( roleKeys.intersect([Role.MEDICO,Role.ENFERMERIA]).size() > 0 )
            out << body()
        */
    }
    
    def langSelector = { attrs, body ->
        
        int i = 0
        grailsApplication.config.langs.each {
           
            out << body(localeString:it, locale:grailsApplication.config.locales[i])
            i++ 
        }
    }
    
    def canEditPatient = { attrs, body ->
        
        // Puedo editar si:
        //   1. le faltan datos
        //   2. la base es local
        if ( !demographicService.personHasAllData( attrs.patient ) && grailsApplication.config.hce.patient_administration.serviceType.local )
            out << body()
    }
}
