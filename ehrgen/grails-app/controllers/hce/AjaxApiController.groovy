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
package hce

import tablasMaestras.Cie10Trauma

import grails.converters.* // as JSON
import com.thoughtworks.xstream.XStream

// TEST BINDER
import binding.BindingAOMRM

import domain.Domain
import hce.core.composition.* // Composition y EventContext

import hce.HceService

import templates.TemplateManager

// Automatic marshalling of XML and JSON
import grails.converters.*
import cache.PathValores
import util.FieldNames

import domain.Domain
import workflow.WorkFlow
import workflow.Stage

/**
 * @author Pablo Pazos Gutierrez (pablo.pazos@cabolabs.com)
 *
 * FIXME: usar templateId en lugar de nombre de vista harcoded.
 * FIXME: usar nombres de arquetipos referenciados por el templateId en lugar de tenerlos harcodeados.
 * FIXME: usar el bind y validador generico en lugar de procesar los datos mano.
 */
class AjaxApiController {

   def hceService
   
   
   // TEST de PathValores 2 JSON
   def pathValores = {
      
      //render PathValores.list(params) as JSON
      //render PathValores.get(1).params as JSON
      render PathValores.list(params).params as JSON
   }

   
   
   // Prueba
   def diagnosticos = {
      println "DIAGNOSTICOS ==============="
      println ""
      println ""
      render(view:'../hce/EHRGen-EHR-diagnosticos.v1')
   }
   
   /**
    * Devuelve una lista de codigos CIE10 basandose en la entrada de texto.
    * La web se encarga de por lo menos enviar algunos caracteres minimos
    * para realizar una busqueda de valor, no estaria de mas que aqui
    * tambien se verifique que vienen por lo minimo X caracteres con X configurable.
    * 
    * @param String text
    */
   def findCIE10 = {
      
      def partes = params.text.split(" ") // saco palabras por espacios
      
      println partes
      
      def _codigos = Cie10Trauma.withCriteria {
         //like('nombre', '%'+ params.text +'%') // Ok si uso el texto completo
         and {
            partes.each { parte ->
               like('nombre', '%'+ parte +'%')
            }
         }
      }
      
      // TODO: hacerlo JSON creo que hay un JSON builder
      
      // Lo hago derecho HTML para probar nomas.
      /*
      def html = '<table>'
      _codigos.each { codigo ->
         
         / *
         html += '<tr><td>' +
               it.nombre.replaceAll(params.text, '<b>'+params.text+'</b>') + // bien para marcar con negrita el texto completo
               '</td></tr>' // TODO: highlight de params.text!
         * /
         
         html += '<tr><td>'
         
         //println "Codigo: " + codigo.nombre
         
         def nombre = codigo.nombre
         partes.each { parte ->
            //println "Parte: " + parte
            nombre = nombre.replaceAll(parte, '<b>'+parte+'</b>')
            println nombre
         }
         
         
         html += nombre
         html += '</td></tr>'
      }
      html += '</table>'
      
      render( html )
      */
      
      
      render(builder:'json') {
        codigos {
         _codigos.each { _codigo ->
         
           // FIXME: hacer el highlight del lado del cliente con js, asi tengo el nombre sin tags html para procesar en la vista
           // a negrita los textos de entrada en el texto de salida
           /*
           def _nombre = _codigo.nombre
           partes.each { parte ->
           
            // El texto en la base esta en upper
            
            _nombre = _nombre.replaceAll(parte.toUpperCase(), '<b class="highlight">'+parte.toUpperCase()+'</b>')
           }
           */
           //println "nombre: "+_nombre
         
           codigo (
            id: _codigo.id,
            grupo: _codigo.grupo,
            subgrupo: _codigo.subgrupo,
            codigo: _codigo.codigo,
            
            
            nombre: _codigo.nombre
            //nombre: _nombre
           )
         }
        }
      }
      
      //render _codigos as JSON // manda class, deleted y demas

   } // findCIE10
   
   
   // No se si va aca, capaz en un controller que se encargue de recibir las salvadas.
   /**
    * @params templateId
    */
   def saveDiagnostico = {
      
      println "AjaxApi saveDiagnostico params: " + params
      
      def cie10ids = request.getParameterValues("codes") as List // 845||OTROS TRAUMATISMOS Y LOS NO ESPECIFICADOS DE LA CABEZA
      //List everyDays = java.util.Arrays.asList( everyDaysArray );
      
      def pathValor = [:]
      
      def codePath = 'openEHR-EHR-OBSERVATION.diagnosticos.v1/data[at0001]/events[at0002]/data[at0003]/items[at0004]/value/defining_code'
      def descPath = 'openEHR-EHR-OBSERVATION.diagnosticos.v1/data[at0001]/events[at0002]/data[at0003]/items[at0005]/value'
      
      //XStream xstream = new XStream()
      
      // Para poner todos los codigos que vengan, puede no venir ninguno
      pathValor[ codePath ] = []
      
      cie10ids.each { id_name ->
      
         // http://code.google.com/p/open-ehr-gen-framework/issues/detail?id=50
         // Viene con || el nombre para poder mostrar el nombre del codedtext en el show, no solo el cie10
         def partes = id_name.split("\\|\\|")
         
         //println id_name
         //println partes
         
         def code = Cie10Trauma.get( partes[0] )
         
         //println xstream.toXML(code)

         // le meto cie10 al principio para saber de cual terminologica tiene que sacar el texto y para que pueda crear la terminology Id del definingcode del CodedText.
         // FIXME: si el seleccionado es un subgrupo, no tiene codigo!
         //pathValor['openEHR-EHR-OBSERVATION.diagnosticos.v1/data[at0001]/events[at0002]/data[at0003]/items[at0004]/value/defining_code'] << 'cie10::'+code.codigo
         //pathValor[ codePath ] << ((code.codigo)?code.codigo:code.subgrupo) // si es un subgrupo, el codigo es el del subgrupo!
         
         // http://code.google.com/p/open-ehr-gen-framework/issues/detail?id=50
         // le pongo el nombre del coded text
         pathValor[ codePath ] << ((code.codigo)?code.codigo:code.subgrupo) + '||' + partes[1]
      }
      
      //println "PathValor: " + pathValor
      
      // Necesito que sea array porque es el tipo que usa java
      // para poner los valores multiples cuando se submitean
      pathValor[ codePath ] = pathValor[ codePath ].toArray()
      pathValor[ descPath ] = params.descripcion
      
      //println "AjaxApi saveDiagnostico pathValor: " + pathValor
      
      // Armo path -> valor para binder! (a mano)
      // openEHR-EHR-ACTION.columna_vertebral.v1/description[at0001]/items[at0002]/value/defining_code
      // openEHR-EHR-OBSERVATION.diagnosticos.v1/data[at0001]/events[at0002]/data[at0003]/items[at0004]/value/defining_code
      
      BindingAOMRM bindingAOMRM = new BindingAOMRM( session )
      def rmobj = bindingAOMRM.bind(pathValor, params.templateId)

      // Se necesita para generar la vista edit
      def template = TemplateManager.getInstance().getTemplate( params.templateId )
      
      // FIXME: hacer flujo de guardar y volver al registro clinico.
      //println pathValor.toString()
      //println xstream.toXML(rmobj)
      
      Composition comp = Composition.get(session.ehrSession.episodioId)

      // Idem al chequeo de GuiGenController.save, si ya existe el ContentItem y
      // es mode=edit, ser borra el viejo y se guarda el nuevo CI.
      // Si mode!=edit, el registro ya esta hecho, no se puede volver a hacer.
      def item = hceService.getCompositionContentItemForTemplate( comp, params.templateId )
      if (item)
      {
         // Si es el save de un edit, borra el registro anterior y sustituye por el nuevo.
         if (params.mode == 'edit')
         {
            // Elimina el cache de valores que tiene una referencia al
            // item sino salta una restricion de integridad del SQL.
            def cache = PathValores.findByItem(item)
            cache.delete(flush:true)
         
            comp.removeFromContent(item)
            item.delete(flush:true) // FIXME: delete no es en cascada si no se pone belongsTo en las clases hijas.
         }
         else // Si no es save de edit, esta tratando de salvar de nuevo algo que ya habia salvado.
         {
            println "Registro ya realizado, se va a show para y no se vuelve a guardar"
            redirect( controller:'guiGen', action:'generarShow', id: item.id,
                    params: ['flash.message': 'trauma.list.error.registryAlreadyDone'] )
            return
         }
      }
      
      if (rmobj)
      {
         if (!rmobj.save(flush:true) || bindingAOMRM.hasErrors() )
         {
            println "ERROR AL SALVAR: ---> " + rmobj.errors
            println "TheErrors: " + bindingAOMRM.getErrors() + "\n\n"
            // TIENE QUE VOLVER Al CREATE con los errores y valores ya ingresados.
            // No puedo hacer redirect porque pierdo los valores y los errores.
            
            // ==============================================================================
            // Model: Paciente del episodio seleccionado
            def composition = Composition.get( session.ehrSession.episodioId )

            // FIXME: esta tira una except si hay mas de un pac con el mismo id, hacer catch
            def patient = hceService.getPatientFromComposition( composition )

            // ==============================================================================

            // Templates de la stage actual
            def workflow = WorkFlow.get(session.ehrSession.workflowId)
            def stage = workflow.getStage(template)
            def subsections = stage.recordDefinitions.templateId

            render( view: '/hce/EHRGen-EHR-diagnosticos.v1',
                  model: [
                     patient: patient,
                     template: template,
                     sections: util.TemplateUtils.getSections(session),
                     subsections: subsections,
                     // Params para edit
                     rmNode: rmobj, // si no pudo guardar no puedo hacer get a la base...
                     mode: 'edit', // necesario por ahora...
                     index: bindingAOMRM.getRMRootsIndex(),
                     errors: bindingAOMRM.getErrors(),
                     allSubsections: util.TemplateUtils.getDomainTemplates(session),
                     workflow: workflow // nuevo
                  ] )
            return
         }
         else
         {
            println "SALVADO ENTRY O SECTION OK"

            
            // ========================================================================
            FieldNames fields = FieldNames.getInstance()
            def fieldValor = [:] // En el cache se tiene que guardar el field no la path
            String field
            pathValor.each{
               
               field = fields.getField(it.key) // obtengo el field por la path
               fieldValor[field] = it.value
            }
            
            PathValores paramsCache = new PathValores(params: fieldValor, item: rmobj)
            
            // Guarda el los valores submiteados para poder generar las vistas mas
            // rapido (cache) sin necesidad de recorrer toda la estructura del RM.
            //PathValores paramsCache = new PathValores(params: pathValue, item: rmobj)
            
            // TODO: verificar que guarda correctamente y hacer log si no guarda.
            if (!paramsCache.save()) println paramsCache.errors
            // ========================================================================
            
            
            // Se linkea las Entry y Section bindeadas a la Composition Correspondiente
            comp.addToContent(rmobj)
            
            if (!comp.save())
            {
               println "ERROR AL SALVAR COMPOSITION"
               // TODO
               // Todas las salvadas que se hacen ahí deberían ser
               //  parte de una misma transaccion y si algo falla,
               //  volver todo para atrás, ir a la página y decirle
               //  que intente submitear de nuevo, mostrándole la
               //  pantalla con los valores que acaba de ingresar,
            }
            else
            {
               println "SALVADA COMPOSITION OK"
                redirect(controller: 'guiGen',
                       action: 'generarShow',
                       params: [id: rmobj.id])
               return
            }
         }
      }
      else
      {
         // volver a la pagina y pedirle que ingrese algun dato
         println "EL RESULTADO DEL BINDEO ES NULL"
      }
      
      // FIXME: esto nunca debe ser ejecutado
      render( text: xstream.toXML(rmobj), contentType:'text/xml' )
      
   } // save diagsnostico
}