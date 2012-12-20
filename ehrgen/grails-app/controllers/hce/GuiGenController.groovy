/**
import hce.core.composition.content.ContentItem;
 * Controller para pruebas de generacion de vistas.
 */
package hce

import org.codehaus.groovy.grails.commons.ApplicationHolder

import se.acode.openehr.parser.*
import org.openehr.am.archetype.Archetype
import org.openehr.am.archetype.constraintmodel.ArchetypeConstraint
import org.openehr.am.openehrprofile.datatypes.text.CCodePhrase

import hce.HceService

import domain.Domain
import archetype.ArchetypeManager
import hce.ArchetypeService
import templates.TemplateManager
import templates.* // Template

import events.*

import com.thoughtworks.xstream.XStream

import hce.core.composition.*

import hce.core.common.archetyped.Locatable

//TEST BINDER
import binding.BindingAOMRM

import cache.PathValores

// Automatic marshalling of XML and JSON
import grails.converters.*

import util.FieldNames
import gui.GuiManager

import domain.Domain
import workflow.WorkFlow
import workflow.Stage

/**
 * @author Pablo Pazos Gutierrez (pablo.swp@gmail.com)
 */
class GuiGenController {

   def archetypeService
   def hceService
   
   
   // ============================= GESTION DE GUIS generadas ======================================
   // TEST
   def fieldPathlist = {
      
      def fn = FieldNames.getInstance()
      return [mapping: fn.getMapping()]
   }
   
   /* TODO:
    * quiero listar las guis generadas
    * ver el html generados
    * ver solo las de show, solo las de edit o solo las de create
    * ponerles algunos datos autogenerados para ver algo en las insterfaces
    * ponerles algo de estilo para verlas como se verian en el registro clinico
    * volver a generar todas, o solo las vistas de algun template, o solo las de algun locale
    * 
    * 
    */
   // ============================= GESTION DE GUIS generadas ======================================
   
   
   def index = {
      redirect(action:'listarTemplates')
      return
   }
   
   /*
   // Esta accion es de prueba, la lista de templates para completar se hace desde trauma.registroClinico,
   // que carga los templates que se llenan (no las pruebas) y su orden para armar la pantalla 5.1.
   def listarTemplates = {

      // FIXME: Ahora listo los templates asi nomas, puede haber subdirectorios.
      // FIXME: esto a config
      def path = "templates/hce/trauma" // FIXME: sacar dominio actual de la config
      def p = ~/.*\.xml/
      def templatesNames = []
      new File( path ).eachFileMatch(p)
      { f ->
         templatesNames << f.name - ".xml" // Solo el id, no necesito la extension del archivo
      }
       
      render(view:"listarTemplates", model:[templateNames: templatesNames])
   }
   */
   
   /*
   // in: templateId
   def generarTemplate = {
      
      EventManager.getInstance().handle("pre_gui_gen", params)

      / *
      // Test para prevenir cacheo
      //NO HACE NADA....
      response.setHeader("Cache-Control", "no-cache, must-revalidate")
      //response.setHeader("Cache-Control", "no-Store")
      response.setHeader("Pragma", "no-cache")
      response.setHeader("Expires", "0")
      * /

      // DEBE haber un episodio seleccionado para poder asociar el registro clinico.
      if (!session.traumaContext?.episodioId)
      {
         flash.message = 'trauma.list.error.noEpisodeSelected'
         redirect(controller:'records', action:'list')
         return
      }
   
      // TODO: verificar que el estado del registro es 'incomplete', de lo contrario no puedo editarlo.
   
      def templateId = params.templateId // es el nombre del archivo

      // Model: Paciente del episodio seleccionado
      def composition = Composition.get( session.traumaContext.episodioId )

      // FIXME: esta tira una except si hay mas de un pac con el mismo id, hacer catch
      def patient = hceService.getPatientFromComposition( composition )

      // FIXME: verificar si ya se hizo registro para este template, y si se hizo ir a show.
      // def item = hceService.getCompositionContentItemForTemplate(composition, attrs.templateId)
      // def ContentItem getCompositionContentItemForTemplate( Composition composition, String templateId )
      
      def item = hceService.getCompositionContentItemForTemplate(composition, templateId)
      if (item)
      {
         //flash.message = 'trauma.list.error.registryAlreadyDone'
         redirect( controller:'guiGen', action:'generarShow', id: item.id,
                   params: ['flash.message': 'trauma.list.error.registryAlreadyDone'] )
         return
      }

      def sections = this.getSections()
      def subsections = this.getSubsections(templateId.split("-")[0]) // this.getSubsections('EVALUACION_PRIMARIA')
      
      // Genera GUI solo si el registro esta abierto
      if ( hceService.isIncompleteComposition( composition ) )
      {
         //println "subSectionPrefix: " + subSectionPrefix
         //println "subsections: " + subsections
   
         Template template = TemplateManager.getInstance().getTemplate( templateId )
         
         def fullUri = grailsAttributes.getViewUri('../hce/'+templateId, request)
         
         //println fullUri
         //println grailsAttributes.pagesTemplateEngine.getResourceForUri(fullUri)
   
         def resource = grailsAttributes.pagesTemplateEngine.getResourceForUri(fullUri)
         if ( resource && resource.file && resource.exists() )
         {
            println 'vista estatica'
            
            EventManager.getInstance().handle("post_gui_gen", params)
            
            render( view: '../hce/'+templateId,
                  model: [
                        patient: patient,
                        template: template,
                        sections: sections,
                        subsections: subsections,
                        episodeId: session.traumaContext?.episodioId,
                        userId: session.traumaContext.userId,
                        allSubsections: this.getDomainTemplates()
                       ] )
            return
         }
         else
         {
            println 'vista dinamica'
   
            EventManager.getInstance().handle("post_gui_gen", params)
   
            // OK
            //XStream xstream = new XStream()
            //println xstream.toXML(template) + "\n\n"
            //println "templateID: " + template.id
            
            // TODO: buscar si existe la vista para el template, y si no existe ir a la generacion dinamica.
            
            return [archetypeService: archetypeService,
                  template: template,
                  patient: patient,
                  sections: sections,
                  subsections: subsections,
                  episodeId: session.traumaContext?.episodioId,
                  userId: session.traumaContext.userId,
                  allSubsections:  this.getDomainTemplates()
                 ]
         }
      }
      else
      {
         flash.message = "registroClinico.warning.noHayRegistroParaLaSeccion"
         redirect( controller: 'records', action: 'show', id: session.traumaContext?.episodioId)
         return
      }
      
   } // generateTemplate
   */
   
   /**
    * Prueba con la vista cacheada.
    * FIXME: cambiar nombre a create.
    */
   def generarTemplate = {

      // DEBE haber un episodio seleccionado para poder asociar el registro clinico.
      if (!session.traumaContext?.episodioId)
      {
         flash.message = 'trauma.list.error.noEpisodeSelected'
         redirect(controller:'records', action:'list')
         return
      }

      /**
       * FIXME: el id del template no deberia depender del nombre de la seccion/etapa
       *        donde se va a usar, ej. dos dominios pueden usar el mismo template en
       *        etapas que se llaman distinto.
       *        Ej. teniendo una etapa llamada "primer etapa" y un template llamado
       *        "INGRESO-triage.v1" busca cargar el template "primer etapa-triage.v1".
       *        Antes INGRESO era el nombre de la seccion/etapa y estaba duro en el
       *        id del template, pero reduce reusabilidad del template.
       */
      
      def templateId = params.templateId // es el nombre del archivo
      Template template = TemplateManager.getInstance().getTemplate( templateId )
      
      // Si no hay template, es porque metio un valor a mano en el templateId
      // Debe regregas al show del registro actual y seleccionar una seccion dek registro (cada seccion tiene un templateId)
      if (!template)
      {
         // FIXME: el id del temaplate es de uso interno, no se lo deberia mostrar al usuario. Queda asi para debuggear.
         flash.message = "Seccion de registro no encontrada " + params.templateId
         redirect(controller:'records', action:'show', id:session.traumaContext?.episodioId)
         return
      }
      

      // Model: Paciente del episodio seleccionado
      def composition = Composition.get( session.traumaContext.episodioId )

      // FIXME: esta tira una except si hay mas de un pac con el mismo id, hacer catch
      def patient = hceService.getPatientFromComposition( composition )

      def sections = util.TemplateUtils.getSections(session)
      def subsections = util.TemplateUtils.getSubsections(templateId.split("-")[0], session) // this.getSubsections('EVALUACION_PRIMARIA')
      
     
     // Igual que Records.show
     // Necesario para verificar permisos sobre el menu
     def completeSections = [:] // secciones con sus templates
     def domainTemplates = util.TemplateUtils.getDomainTemplates(session)
     
     domainTemplates.keySet().each { sectionPrefix ->
        domainTemplates."$sectionPrefix".each { section ->
         
           if (!completeSections[sectionPrefix]) completeSections[sectionPrefix] = []
         
           // Tiro la lista de esto para cada "section prefix" que son los templates
           // de las subsecciones de la seccion principal.
           //println sectionPrefix + "-" + section
           completeSections[sectionPrefix] << sectionPrefix + "-" + section
        }
     }
     
     
      // Genera GUI solo si el registro esta abierto
      if ( hceService.isIncompleteComposition( composition ) )
      {
         //println "subSectionPrefix: " + subSectionPrefix
         //println "subsections: " + subsections
         
         // TODO:
         // Es mas rapido que la generacion y estoy levantando de disco!
         // Si cacheo en memoria es incluso mas rapido! tengo que hacer un handler!!
         //
         GuiManager guiManager = GuiManager.getInstance()
         if (guiManager.exists(templateId, 'create', session.locale.toString()))
         {
            println 'vista generada'
            
            render( view: 'create/generarCreate',
                    model: [
                     patient: patient,
                     template: template,
                     sections: sections,
                     subsections: subsections,
                     episodeId: session.traumaContext?.episodioId,
                     // userId: session.traumaContext.userId, // no se usa
                     allSubsections: util.TemplateUtils.getDomainTemplates(session),
                     form: guiManager.get(templateId, 'create', session.locale.toString()),
                     completeSections: completeSections,
					 domain: Domain.get(session.traumaContext.domainId)
                    ] )
            return
         }
         else // Si no existe la vista generada, deberia ser una vista estatica
         {
            println 'vista estatica'
            
            render( view: '../hce/'+templateId,
                    model: [
                     patient: patient,
                     template: template,
                     sections: sections,
                     subsections: subsections,
                     episodeId: session.traumaContext?.episodioId,
                     //userId: session.traumaContext.userId,
                     allSubsections: util.TemplateUtils.getDomainTemplates(session),
                     completeSections: completeSections,
					 domain: Domain.get(session.traumaContext.domainId)
                    ] )
            return
         }
         //   println 'dice que no es un recurso valido: '+ path
      }
      else
      {
         flash.message = "registroClinico.warning.noHayRegistroParaLaSeccion"
         redirect( controller: 'records', action: 'show', id: session.traumaContext?.episodioId)
         return
      }
      
   } // generateTemplate2
   
   /**
    * Prueba con vista cacheada.
    */
   def generarShow = {
      
      // DEBE haber un episodio seleccionado para poder asociar el registro clinico.
      if (!session.traumaContext?.episodioId)
      {
         flash.message = 'trauma.list.error.noEpisodeSelected'
         redirect(controller:'records', action:'list')
         return
      }

      // ========================================
      // ContentItem
      def rmNode = Locatable.get(params.id)
      PathValores pv = PathValores.findByItem(rmNode)
      // TODO: quiero el PathValues asociado a este rmNode
      // FIXME: if !params.id
      // FIXME: if !rmNode (creo que esto seria chequear si existe el registro para el template y la composition, si no es asi, tambien lo deberia verificar)
      // ========================================
      
      
      // El templateId se saca del objeto del RM guardado, asi si varia la version del template,
      // siempre voy a poder mostrar el contenido guardado con versiones anteriores del mismo.
      def templateId = rmNode.archetypeDetails.templateId
      
      // Model: Paciente del episodio seleccionado
      def composition = Composition.get( session.traumaContext.episodioId )

      // FIXME: esta tira una except si hay mas de un pac con el mismo id, hacer catch
      def patient = hceService.getPatientFromComposition( composition )

      def sections = util.TemplateUtils.getSections(session)
      def subsections = util.TemplateUtils.getSubsections(templateId.split("-")[0], session) // this.getSubsections('EVALUACION_PRIMARIA')
      
     // Igual que Records.show
     // Necesario para verificar permisos sobre el menu
     def completeSections = [:] // secciones con sus templates
     def domainTemplates = util.TemplateUtils.getDomainTemplates(session)
     
     domainTemplates.keySet().each { sectionPrefix ->
        domainTemplates."$sectionPrefix".each { section ->
         
           if (!completeSections[sectionPrefix]) completeSections[sectionPrefix] = []
         
           // Tiro la lista de esto para cada "section prefix" que son los templates
           // de las subsecciones de la seccion principal.
           //println sectionPrefix + "-" + section
           completeSections[sectionPrefix] << sectionPrefix + "-" + section
        }
     }
     
     
      Template template = TemplateManager.getInstance().getTemplate( templateId )
      
      
      // TODO:
      // Es mas rapido que la generacion y estoy levantando de disco!
      // Si cacheo en memoria es incluso mas rapido! tengo que hacer un handler!!
      //def path = './grails-app/views/genViews/'+templateId+'_show.htm'
      //def f = new File(path)
      //println f.getText()
      GuiManager guiManager = GuiManager.getInstance()
      
      //println "-----------------------------------------"
      //println guiManager.get(templateId, "show") // OK
      //println "pathValores: " + pv.params
      
      // ===============================================================================
      // FIXED: el problema se resolvio en AjaxApiController.save porque para los coded
      // text faltaba guardar el nombre como se guarda en GuiGenController.save: codigo||nombre
      //
      // Para los textos codificados tengo que pedir los valores
      // http://code.google.com/p/open-ehr-gen-framework/issues/detail?id=50
      /*
      ArchetypeManager am = ArchetypeManager.getInstance()
      FieldNames fields = FieldNames.getInstance()
      binding.CtrlTerminologia terms = binding.CtrlTerminologia.getInstance()
      
      String fullPath
      Archetype archetype
      ArchetypeConstraint n
      
      // Para cada codigo encontrado, su nombre
      def names = [:]
      
      pv.params.each { entry ->
         
         // entry.key es fieldName
         fullPath = fields.getPath(entry.key)
         println 'fullPath: '+ fullPath
         
         // Si el campo es una parte de una fecha, no va a tener path
         if (!fullPath) return
         
         //def n = am.getArchetypeNode(fullPath) // No puedo usar esto porque tambien necesito el arquetipo para pedirle el termino a CtrlTerminologia...
         int i = fullPath.indexOf('/')
         String archetypeId = fullPath.substring(0, i)
         String path = fullPath.substring(i)
         
         archetype = am.getArchetype(archetypeId)
         n = archetype?.node(path)
         //println n
         

         // Veo los nombres de los codigos para este nodo del arquetipo si es CCodePhrase
         if (n instanceof CCodePhrase)
         {
            // Ojo este es: org.openehr.rm.support.identification.TerminologyID
            // Y necesito: support.identification.TErminologyID que es mi implementacion!
            //println n.terminologyId
            
            // Quiero siempre una lista de codigos
            def codes = []
            
            if (entry.value instanceof String)
            {
               codes << entry.value
            }
            else
            {
               codes = entry.value
            }
            
            codes.each{ code ->
               names[code] = terms.getTermino(
                   support.identification.TerminologyID.create(n.terminologyId.name, n.terminologyId.version),
                   code,
                   archetype,
                   session.locale)
            }
         }
      }
      */
      
      if (params.mode=='edit')
      {
         render(view:'edit/generarEdit', model:
         [
           patient:   patient,
           template:  template,
           //templateId: templateId,
           sections:  sections,
           subsections: subsections,
           completeSections: completeSections,
           allSubsections: util.TemplateUtils.getDomainTemplates(session),
           episodeId: session.traumaContext?.episodioId,
           
           // content es para el generateShow, para generateEdit se usa form
           //content: guiManager.get(templateId, "edit", session.locale.toString()),
           data: pv.params as JSON,
           domain: Domain.get(session.traumaContext.domainId),
           id: params.id,
           
           // Para el edit, copiado del edit de correccion de errores de validacion en el save
           form: guiManager.get(templateId, "edit", session.locale.toString()),
           errors: [] as JSON,   // No hay errores, estoy editando algo ya validado y guardado
           errors2: [] as JSON,  // No hay errores, estoy editando algo ya validado y guardado
           
           /*
           rmNode:    rmNode,
           index:     hceService.getRMRootsIndex(template, rmNode),
           episodeId: session.traumaContext?.episodioId, // necesario para el layout
           patient:   patient,
           userId:    session.traumaContext.userId,
           */
         ])
         return
      }
      
      //if ( f.exists() )
      //{
         //println 'vista cacheada!'
         render( view: 'show/generarShow', model:
         [
            patient:    patient,
            template:   template,
            templateId: templateId, // FIXME: este incluye la version y el id del template no, deberian ser iguales.
            sections: sections,
            subsections: subsections,
            completeSections: completeSections,
            //userId: session.traumaContext.userId,
            allSubsections: util.TemplateUtils.getDomainTemplates(session),
            episodeId: session.traumaContext?.episodioId,
            //content: f.getText(),
            content: guiManager.get(templateId, "show", session.locale.toString()),
            data: pv.params as JSON,
            domain: Domain.get(session.traumaContext.domainId),
            id: params.id
         ])
         return
      //}
      //else
      //   println 'dice que no es un recurso valido: '+ path
      
   } // generarShow2
   
   
   /**
    * prueba de salvar desde generarTemplate2, usando FieldNames para obtener las paths.
    * FIXME: deberia estar en recordController.
    */
   def save = {
      
      if (!session.traumaContext?.episodioId)
      {
         flash.message = 'trauma.list.error.noEpisodeSelected'
         redirect(controller:'records', action:'list')
         return
      }
      
      // Episodio seleccionado para el cual se está registrando
      Composition comp = Composition.get(session.traumaContext.episodioId)

      // TODO: verificar que el estado del registro es 'incomplete', de lo contrario no puedo editarlo.


      //EventManager.getInstance().handle("pre_save", [composition:comp])


      // Verifico si el registro para el template de entrada ya se hizo,
      // en ese caso se está haciendo un edit de un registro previo,
      // tengo que buscar el registro previo, sacarlo del episodio y
      // meter el nuevo registro.
      // TODO: Aquí es donde entra en juego el tema del versionado, si
      //      ya pasaron 24 hs desde el inicio del episodio y se quiere
      //      editar, se deberia versionar, guardando la versión actual
      //      del registro y guardando luego la nueva como nueva version.

      def item = hceService.getCompositionContentItemForTemplate( comp, params.templateId )
      if (item)
      {
         // Si es el save de un edit, borra el registro anterior y sustituye por el nuevo.
         if (params.mode == 'edit')
         {
            //println "Esta seccion o entrada ya estaba registrada, se procede a eliminar el registro actual y sustituirlo por el nuevo ingreso..."
            
            //XStream xstream = new XStream()
            //println "COMPONENT ANTES"
            //println xstream.toXML(comp)
            
            // Elimina el cache de valores que tiene una referencia al
            // item sino salta una restricion de integridad del SQL.
            def cache = PathValores.findByItem(item)
            cache.delete(flush:true)
            
            
            comp.removeFromContent(item)
   
            // FIXME: borra la raiz pero no borra los subitems, por ejemplo si es section, la borra pero no sus entries.
            // Se podria hacer que las entries pertenezcan a las sections, asi se elimina en cascada.
            // Tambien hay que ver que se eliminen los objetos mas bajo nivel.
            /**
             * http://grails.org/doc/latest/ref/Domain%20Classes/delete.html
             * By default Grails will use transactional write behind to perform the delete,
             * if you want to perform the delete in place then you can use the 'flush' argument.
             */
            item.delete(flush:true) // FIXME: delete no es en cascada si no se pone belongsTo en las clases hijas.
   
            //println "COMPONENT DESPUES"
            //println xstream.toXML(comp)
         }
         else // Si no es save de edit, esta tratando de salvar de nuevo algo que ya habia salvado.
         {
            println "Registro ya realizado, se va a show para y no se vuelve a guardar"
            // Muestro el registro ya ingresado previamente
            //flash.message = 'trauma.list.error.registryAlreadyDone'
            redirect( controller:'guiGen', action:'generarShow', id: item.id,
                      params: ['flash.message': 'trauma.list.error.registryAlreadyDone'] )
            return
         }
      }

      // Deberian venir todas las paths definidas en el template que use
      // para crear la vista, y complementarse con las paths que se definieron
      // en el template que no se mostraran en la vista, por ejemplo, para
      // casos de valores asumidos, donde el usuario no puede ingresar otra
      // cosa que lo que dice el template.
      // Los valores asumidos del template pueden derivarse de los arquetipos,
      // pero seran muy pocos arquetipos los que definan estos valores, porque
      // deben ser de uso general.
      
      // TODO: ver que transformaciones hay definidas para los paths->data
      // submiteados y transformar los datos que tengan transformaciones
      // definidas y darle a Lea todo listo para guardar
      
      // TODO: las transformaciones de unidades deberian hacerse por el Measurement Service

      // Veo si vienen archivos
      def files = request.getFileMap()
      
      //println "VIENEN ARCHIVOS: " + files
      //println "+++++++++++++++++++++++++++++++++++++++++++++++++++"

      
      def template = TemplateManager.getInstance().getTemplate( params.templateId )
      
      // Si no hay template, es porque metio un valor a mano en el templateId
      // Debe regregas al show del registro actual y seleccionar una seccion dek registro (cada seccion tiene un templateId)
      if (!template)
      {
         // FIXME: el id del temaplate es de uso interno, no se lo deberia mostrar al usuario. Queda asi para debuggear.
         flash.message = "Seccion de registro no encontrada " + params.templateId
         redirect(controller:'records', action:'show', id:session.traumaContext?.episodioId)
         return
      }
      
      def transformations = template.getTransformations()

      //println "+++++++++++++++++++++++++++++++++++++++++++++++++++"
      //println "Hay " + transformations.size() + " transformaciones"
      
      transformations.each { transform ->
      
         //println "Transform: " + transform.operation + " " + transform.operand
      
         // archetypeId+path
         if ( params.containsKey( transform.owner.owner.id + transform.path) )
         {
            // Solo puede transformar un number
            // Number no tiene funcion para pasar de un string a number
            // Uso Float para todo // FIXME: puede dar problemas si el valor no es float en el arquetipo.
            
            try // por si el numero no viene tengo un error, deberia meter un error en errors2 del binder si es que el valor era obligatorio: DE ESO SE ENCARGA EL BINDER!
            {
               def oldValue = Float.valueOf( params.(transform.owner.owner.id+transform.path) )
               def newValue = transform.process( oldValue )
               
               // FIXME: en la desconversion para el show, si ahora ingreso 10 se guarda 0.16666666666,
               //      en la inversa, 0.1666666666 da 9.99996, asi que en la desconversion
               //      deberia intentar hacer redondeo para mostrar.
               //      Preguntar en la lista de OpenEHR si alguien supo resolver esto de una forma que no sea
               //      cambiando el tipo del Quantity por 2 campos individuales en un cluster.
               
               println "Old: " + oldValue + ", New: " + newValue
               
               params.(transform.path) = newValue
            }
            catch (Exception e)
            {
               println "WARNING: Se intenta transformar un valor con mal formato: '" + params.(transform.owner.owner.id+transform.path) + "'"
            }
         }
         else
         {
            println "Transform: Params no tiene la path: " + transform.owner.owner.id + transform.path
         }
      }
      //println "+++++++++++++++++++++++++++++++++++++++++++++++++++"
      
      // params es GrailsParameterMap y bind espera LinkedHashMap
      /*
      def pathValue = [:]
      
      params.each{
         //println "   (=) " + params[it.key].getClass().getSimpleName()
         //println "   (+) " + it.value.getClass().getSimpleName()
         //println "   it: " + it
         // no deja pasar los parametros que se separan con puntos, los "railsified params".
         if (it.value instanceof String || it.value.getClass().isArray())
           pathValue[it.key] = it.value
      }
      */
      
      // VERIFY:
      // La forma Groovy de hacerlo, ademas le agrego que filtre los valores
      // vacios, porque se esta bindeando para strings vacios, y cuando se
      // hace la validacion de ocurrencias deberia saltar que falta un valor
      // sin necesidad de pasarle todas las path con valor vacio.
      // FIXME: aunque no pase los valores vacios, igual llega hasta el ultimo
      // nodo para bindear, porque recorre el arquetipo. En realidad deberia
      // parar de bindear cuando me doy cuenta que viola una restriccion,
      // si sigo para abajo ya se que va a seguir violando.
      // Y si quiero crear una estructura para ponerle errores, puedo hacerlo
      // solo con el arquetipo y sin bindear, es como activar errores para
      // todas la restricciones del arquetipo para una cierta subestructura.
      // Pero, si tengo una estructura opcional y no viene valor, no deberia
      // seguir bindeando, porque si la estructura es opcional, todas las
      // subestructuras lo seran.
      
      // Parametros que vienen siempre (aparte de los valores a bindear):
      // - templateId
      // - controller
      // - action
      // - doit

      // FIXME: deberia saber si no viene ningun dato, pero no sacar paths sin datos si viene algun dato,
      //        porque sacar paths afecta el binder, porque no valida cosas que deberia validar.
      
      // FIX 2: no filtro los vacios, los dejo pasar si vienen. Pero si no viene nada, ni siquiera llamo al binder.
      boolean vienenDatos = false
      Map pathValue2 = [:]
      pathValue2 += params // no filtro los vacios, los dejo pasar si vienen.
      for (def e in pathValue2)
      {
         // Si el valor es un string y no es vacio, o
         // si es un array, con mas de un elemento, y ninguno es vacio,
         //   vienenDatos = true
         if ( e.key.startsWith('field_') &&
              (
                ( e.value instanceof String && e.value != '' ) ||
                ( e.value.getClass().isArray() &&
                  e.value.length > 0 &&
                  (e.value.findAll{ v -> v != '' && v != null }.size() > 0)
                )
              )
            )
         {
            vienenDatos = true
            break
         }
         else if (files.size() > 0) // Tambien si viene algun archivo, marco que vienen datos
         {
            vienenDatos = true
            break
         }
      }
      
      Map pathValue2Filtered = [:] // igual a pathValue2 pero saca items vacios, es para guardar en paramsCache para hacer el show desde JS.
      pathValue2.each{ e->
      
         if ( e.key.startsWith('field_') )
         {
            if (e.value instanceof String && e.value != '')
            {
               pathValue2Filtered << e
            }
            else if ( e.value.getClass().isArray() && e.value.length > 0 )
            {
               // No puedo sacar todos los vacios de los arrays asi nomas
               // tengo que sacar los vacios desde el ultimo lugar hasta que
               // encuentre un valor no vacio, esto es porque si tengo un
               // cluster con 2 elements, y el cluster viene 2 veces, y tengo:
               //  - cluster
               //    - element1 (valor1)
               //    - element2 ('')
               //  - clustar
               //    - element1 ('')
               //    - element2 (valor2)
               //
               // Ese caso viene de la web como:
               //  - element1 = [valor1, '']
               //  - element2 = ['', valor2]
               //
               // Y si saco los valores vacios asi nomas queda:
               //  - element1 = [valor1]
               //  - element2 = [valor2]
               //
               // Lo que da a una estructura asi (estructura erronea):
               //  - cluster
               //    - element1 (valor1)
               //    - element2 (valor2)
               //
               // Lo que tengo que hacer es tener:
               //  - element1 = [valor1] // saco el vacio empezando desde el final
               //  - element2 = ['', valor2] // no saco el vacio si no esta al final
               
               // itero del ultimo al primero
               boolean ponerValor = false // Es true cuando encuentro el primer valor desde el final
               List valores = []
               for (int i in e.value.length-1..0)
               {
                  if (e.value[i] != '') // El primer valor no vacio, prende la bandera.
                  {
                     ponerValor = true
                  }
                  
                  if (ponerValor)
                  {
                     valores[i] = e.value[i]
                  }
               }
               
               // Guardo un array, podria guardar la lista directamente...
               pathValue2Filtered[e.key] = valores.toArray(new String[valores.size()])
            }
         }
      }
      
      println ""
      println "pathValue2: "+ pathValue2
      println "pathValue2Filtered: "+ pathValue2Filtered
      
      /*
      Map pathValue2 = params.findAll { e ->
         
         ( e.value != '' && e.value instanceof String ) ||
         ( e.value.getClass().isArray() &&
           e.value.length > 0 &&
          (e.value.findAll{ v -> v != '' && v != null }.size() > 0) ) // El array podria contener valores pero ser todos vacios...
          
         //it.value != '' && ( it.value.getClass().isArray() && it.value.length > 0)
      }
      */
      //println "   -=-=- PATH VALUE: " + pathValue
   
      // Pongo archivos (puede no venir ninguno)
      pathValue2 += files
   
      //println "== ------------------------------------------"
      //println "== pathValue2: "+ pathValue2
      
      
      // FIXME: esto lo podria hacer en el loop previo sobre params para optimizar
      // Resolucion de fieldNames a path
      List resolverDates = []
      FieldNames fields = FieldNames.getInstance()
      Map pathValue = [:]
      pathValue2.each { entry ->
         // entry.key es fieldName
         
         // Si el valor es date.struct, deberia tomar las demas partes de la date
         // para pasarlos con una path de mentira para cada parte:
         // path_year, path_month, day, hour, minute
         // Asi es como la toma el bindDV_DATE_TIME
         // Otra es hacer el bind aca y pasar una date, ahi me ahorro algo de procesamiento en el binder.
         
         String path = fields.getPath(entry.key)
         
         if (entry.value == 'date.struct')
         {
            // Para resolverlo en otro loop abajo
            resolverDates << entry.key // field_xxx
            
            /*
            // entry.key es field_xxx
            // y busco field_xxx_year, field_xxx_month, field_xxx_day, ...
            // no puedo hacer un each sobre otro each del mismo map
            def partesFecha = pathValue2.findAll{ e2 -> e2.key.startsWith(entry.key) }
            partesFecha.each{ p ->
               // pathValue[path]_year
               // _year = field_xxx_year - field_xxx
               pathValue[path+(p.key-entry.key)] = entry.value
            }
            */
         }
         
         if (path) pathValue[path] = entry.value
      }
      
      resolverDates.each { fieldName ->
         
         def partesFecha = pathValue2.findAll{ e2 -> e2.key.startsWith(fieldName) }
         partesFecha.each{ p ->
            // pathValue[path]_year
            // _year = field_xxx_year - field_xxx
            pathValue[fields.getPath(fieldName)+(p.key-fieldName)] = pathValue2[p.key]
         }
      }
      
      //println "mapping: " + fields.getMapping()
      //println "inverso: " + fields.getInverseMapping()
      //println "##############################################################"
      //println "pathValue: " + pathValue
      //println ""
      
      if (!vienenDatos)
      {
         // volver a la pagina y pedirle que ingrese algun dato
         //println "EL RESULTADO DEL BINDEO ES NULL"
         GuiManager guiManager = GuiManager.getInstance()
         
         flash.message = "Ingrese algun dato antes de guardar"
         
         render(view: 'create/generarCreate',
                model: [
                  template: template,
                  form: guiManager.get(params.templateId, "create", session.locale.toString()),
                  episodeId: session.traumaContext?.episodioId, // necesario para el layout
                  //userId: session.traumaContext.userId,
                  subsections: util.TemplateUtils.getSubsections(params.templateId.split("-")[0], session),
                  allSubsections: util.TemplateUtils.getDomainTemplates(session),
				      domain: Domain.get(session.traumaContext.domainId)
                ]
               )
         return
      }
      
      // ==========================================================
      // Bind
      BindingAOMRM bindingAOMRM = new BindingAOMRM(session)
      def rmobj = bindingAOMRM.bind(pathValue, params.templateId)
      
      
      //println "Params: " + params.toString() + "\n"
      //println "Path value: " + pathValue + "\n"
      
      /*
      XStream xstream = new XStream()
      xstream.omitField(Locatable.class, "errors");
      xstream.omitField(data_types.basic.DataValue.class, "errors");
      println "-- rmobj se guardo en RM Bindeado para template.xml"
      File file = new File("RM Bindeado para template.xml")
      file << xstream.toXML(rmobj)
      */
      
      if (!rmobj)
      {
         // volver a la pagina y pedirle que ingrese algun dato
         //println "EL RESULTADO DEL BINDEO ES NULL"
         GuiManager guiManager = GuiManager.getInstance()
         
         flash.message = "Ingrese algun dato antes de guardar" // FIXME: I18N
         
         render(view: 'create/generarCreate',
                model: [
                  template: template,
                  form: guiManager.get(params.templateId, "create", session.locale.toString()),
                  episodeId: session.traumaContext?.episodioId, // necesario para el layout
                  //userId: session.traumaContext.userId,
                  subsections: util.TemplateUtils.getSubsections(params.templateId.split("-")[0], session),
                  allSubsections: util.TemplateUtils.getDomainTemplates(session),
				      domain: Domain.get(session.traumaContext.domainId)
                ]
               )
         return
      }
      
      // Llega aca si el binder retorna algo distinto de null
      
      //println "hasErrors: " + bindingAOMRM.hasErrors()
      
      // Creo objeto con los valores submiteados.
      // Solo se guarda si rmobj valida ok.
      PathValores paramsCache = new PathValores(params: pathValue2Filtered, item: rmobj)
      
      // primero veo si se detectaron errores en el binder, sino, trato de salvar.
      // si no se detectaron errores en el binder, deberia salvar ok,
      // pero dejo el chequeo de salvar para detectar errores de programacion en el binder (faltas de chequeos de errores).
      // flush para que el save se haga de inmediato
      //
      // VER esto por la config: http://code.google.com/p/open-ehr-gen-framework/issues/detail?id=61
      // 1. si no tiene errores de bind, se fija si tiene que salvar
      //   1.1. si no tiene que salvar, tampoco va a tener errores de save, asi que no entra. OK
      //   1.2. si tiene que salvar y el save tiene errores entra. OK
      // 2. si tiene errores de bind, entra. OK
      if (bindingAOMRM.hasErrors() ||
          (ApplicationHolder.application.config.hce.clinical_record.save_rm_structure && !rmobj.save(flush: true)) )
      {
         if (bindingAOMRM.hasErrors())
            println "Hay errors en el binder (bandera errors en true)"
         else
            println "Errores en rmobj: " + rmobj.errors  
         
         //EventManager.getInstance().handle("post_save_error", [composition:comp])

         flash.message = 'Hay errores en los datos, por favor verifíquelos' // TODO: i18n

         // ==============
         // FIXME: para el edit necesito una estructura auxiliar con todos los errores en el RM.
         //        tengo que guardar los errores para cada path, convertir las paths a fields, y pasar todo a la GUI con JSON.
         //        esta estructura se la tengo que pedir al binder.
         // ==============
         
         // Transformo las paths de los errores, a nombres de campos (donde se tiene que mostrar el error)
         def errors = [:]
         for (def entry in bindingAOMRM.errors) // path => lista de errors del gorm
         {
            // Intento resolver el caso de que tengo 2 errores para 2 nodos uno clonado del otro,
            // que si uso un map field error, pierdo el error para el segundo nodo.
            //if (!errors[fields.getField(entry.key)]) errors[fields.getField(entry.key)] = []
            //errors[fields.getField(entry.key)] << entry.value
            
            // deberia pedir el field con archetype.archetypeId.value +_refPath+ entry.key
            errors[fields.getField(entry.key)] = entry.value // field => lista de errors
         }
         
         // En error no puedo hacer redirect porque pierdo los valores y los errores (a no ser que los guarde)
         GuiManager guiManager = GuiManager.getInstance()
         render(view: 'edit/generarEdit',
                model: [
                  rmNode: rmobj, // si no pudo guardar no puedo hacer get a la base...
                  index: bindingAOMRM.getRMRootsIndex(),
                  template: template,
                  //mode: 'edit',
                  data: paramsCache.params as JSON,
                  form: guiManager.get(params.templateId, "edit", session.locale.toString()),
                  errors: errors as JSON,
                  errors2: bindingAOMRM.errors as JSON,
                  episodeId: session.traumaContext?.episodioId, // necesario para el layout
                  subsections: util.TemplateUtils.getSubsections(params.templateId.split("-")[0], session),
                  allSubsections: util.TemplateUtils.getDomainTemplates(session),
				      domain: Domain.get(session.traumaContext.domainId)
                ]
               )
         return
      }
      

      // Llega aca si no hubieron errores de binder y si pudo salvar bien
      
      println "SALVADO ENTRY O SECTION OK"
         
      // ========================================================================
      // Guarda el los valores submiteados para poder generar las vistas mas
      // rapido (cache) sin necesidad de recorrer toda la estructura del RM.
      //PathValores paramsCache = new PathValores(params: pathValue, item: rmobj)
      
      // TODO: verificar que guarda correctamente y hacer log si no guarda.
      paramsCache.save()
      
      
      // Se linkea las Entry y Section bindeadas a la Composition Correspondiente
      comp.addToContent(rmobj)
      if (!comp.save())
      {
         println "ERROR AL SALVAR COMPOSITION"
         // TODO
         // Todas las salvadas que se hacen ahí deberían ser
         // parte de una misma transaccion y si algo falla,
         // volver todo para atrás, ir a la página y decirle
         // que intente submitear de nuevo, mostrándole la
         // pantalla con los valores que acaba de ingresar,
         
         // FIXME: si falla el salvado, tengo que hacer un log interno, el usuario
         // deberia ser notificado de que hubo un error, pero que los datos que
         // envio se guardaron. Los datos se guardaron porque se guarda paramsCache,
         // pero falta asegurar que se podran asociar a la composition correcta.
         return
      }
      
      // Llega aca si guardo correctamente el registro bindeado en la composition
      
      println "SALVADA COMPOSITION OK y muestra show"
   
      // Hay un handler que se encarga de verificar si se dan
      // las condiciones de cierre del registro.
      //EventManager.getInstance().handle("post_save_ok", [composition:comp])
   
      // Redirige a show para mostrar el registro ingresado.
      // Siempre hay que redirigir para que no haga resubmit si hacer reload de la pagina
      redirect(action: 'generarShow', params: [id:rmobj.id])
      return
   }
   
   
   /**
    * 
    */
   def listarArquetipos = {
   
      println "===" + archetypeService + "==="
         
      // Test archetype manager
      //def manager = ArchetypeManager.getInstance()
      //manager.loadAll()
      //println manager
      // /Test archetype manager
     
      // FIXME: cambie la estructura de arquetipos a la que usa openEHR, asi que habria que listar subdirectorios.
      //def path = "archetypes/ehr/entry/evaluation"
      //def path = "archetypes/ehr/entry/action"
      //def path = "archetypes/ehr/cluster"
      def path = "archetypes/ehr/section"
      def p = ~/.*\.adl/
      def arqsNames = []
      new File( path ).eachFileMatch(p)
      { f ->
        arqsNames << f.name - ".adl" // Solo el id, no necesito la extension del archivo
      }
      
      render(view:"listarArquetipos", model:[arqNames:arqsNames])
   }
   
   /**
    * Prueba para poder ver los arquetipos que estan cargados en cache.
    */
   def loadedArchetypes = {
      
      def manager = ArchetypeManager.getInstance()
      render( manager.toString() )
   }
   
   /**
    * in: archetypeId
    */
   def generar = {
         
      //println "===" + archetypeService + "==="
   
      // FIXME: ahora es con arquetipos para probar, pero
      //      deberia ser con templates!
      def archetypeId = params.archetypeId
      
      log.debug("generar: " + params.archetypeId)
      
      // Test archetype manager
      def manager = ArchetypeManager.getInstance()
      Archetype archetype = manager.getArchetype( archetypeId )
      // /Test archetype manager
      
      
      render(view:"generar", model:[archetype:archetype, archetypeService:archetypeService])
   }
   
   /*
   def save = {
      
      //println "params: " + params
      
      // DEBE haber un episodio seleccionado para poder asociar el registro clinico.
      // Se necesita para mostrar el layout con el resumen del episodio arriba.
      if (!session.traumaContext?.episodioId)
      {
         flash.message = 'trauma.list.error.noEpisodeSelected'
         redirect(controller:'records', action:'list')
         return
      }
      
      // Episodio seleccionado para el cual se está registrando
      Composition comp = Composition.get(session.traumaContext.episodioId)

      // TODO: verificar que el estado del registro es 'incomplete', de lo contrario no puedo editarlo.


      EventManager.getInstance().handle("pre_save", [composition:comp])


      // Verifico si el registro para el template de entrada ya se hizo,
      // en ese caso se está haciendo un edit de un registro previo,
      // tengo que buscar el registro previo, sacarlo del episodio y
      // meter el nuevo registro.
      // TODO: Aquí es donde entra en juego el tema del versionado, si
      //      ya pasaron 24 hs desde el inicio del episodio y se quiere
      //      editar, se deberia versionar, guardando la versión actual
      //      del registro y guardando luego la nueva como nueva version.

      def item = hceService.getCompositionContentItemForTemplate( comp, params.templateId )
      if (item)
      {
         // Si es el save de un edit, borra el registro anterior y sustituye por el nuevo.
         if (params.mode == 'edit')
         {
            println "Esta seccion o entrada ya estaba registrada, se procede a eliminar el registro actual y sustituirlo por el nuevo ingreso..."
            
            //XStream xstream = new XStream()
            //println "COMPONENT ANTES"
            //println xstream.toXML(comp)
            
            comp.removeFromContent(item)
   
            // FIXME: borra la raiz pero no borra los subitems, por ejemplo si es section, la borra pero no sus entries.
            // Se podria hacer que las entries pertenezcan a las sections, asi se elimina en cascada.
            // Tambien hay que ver que se eliminen los objetos mas bajo nivel.
            / **
             * http://grails.org/doc/latest/ref/Domain%20Classes/delete.html
             * By default Grails will use transactional write behind to perform the delete,
             * if you want to perform the delete in place then you can use the 'flush' argument.
             * /
            item.delete(flush:true) // FIXME: delete no es en cascada si no se pone belongsTo en las clases hijas.
   
            //println "COMPONENT DESPUES"
            //println xstream.toXML(comp)
         }
         else // Si no es save de edit, esta tratando de salvar de nuevo algo que ya habia salvado.
         {
            println "Registro ya realizado, se va a show para y no se vuelve a guardar"
            // Muestro el registro ya ingresado previamente
            //flash.message = 'trauma.list.error.registryAlreadyDone'
            redirect( controller:'guiGen', action:'generarShow', id: item.id,
                      params: ['flash.message': 'trauma.list.error.registryAlreadyDone'] )
            return
         }
      }

      // Deberian venir todas las paths definidas en el template que use
      // para crear la vista, y complementarse con las paths que se definieron
      // en el template que no se mostraran en la vista, por ejemplo, para
      // casos de valores asumidos, donde el usuario no puede ingresar otra
      // cosa que lo que dice el template.
      // Los valores asumidos del template pueden derivarse de los arquetipos,
      // pero seran muy pocos arquetipos los que definan estos valores, porque
      // deben ser de uso general.
      
      // TODO: ver que transformaciones hay definidas para los paths->data 
      // submiteados y transformar los datos que tengan transformaciones
      // definidas y darle a Lea todo listo para guardar
      
      // TODO: las transformaciones de unidades deberian hacerse por el Measurement Service

      // Veo si vienen archivos
      def files = request.getFileMap()
      
      //println "VIENEN ARCHIVOS: " + files
      //println "+++++++++++++++++++++++++++++++++++++++++++++++++++"

      
      def template = TemplateManager.getInstance().getTemplate( params.templateId )
      def transformations = template.getTransformations()

      println "+++++++++++++++++++++++++++++++++++++++++++++++++++"
      println "Hay " + transformations.size() + " transformaciones" 
      
      transformations.each { transform ->
      
         //println "Transform: " + transform.operation + " " + transform.operand
      
         // archetypeId+path
         if ( params.containsKey( transform.owner.owner.id + transform.path) )
         {
            // Solo puede transformar un number
            // Number no tiene funcion para pasar de un string a number
            // Uso Float para todo // FIXME: puede dar problemas si el valor no es float en el arquetipo.
            
            try // por si el numero no viene tengo un error, deberia meter un error en errors2 del binder si es que el valor era obligatorio: DE ESO SE ENCARGA EL BINDER!
            {
               def oldValue = Float.valueOf( params.(transform.owner.owner.id+transform.path) )
               def newValue = transform.process( oldValue )
               
               // FIXME: en la desconversion para el show, si ahora ingreso 10 se guarda 0.16666666666,
               //      en la inversa, 0.1666666666 da 9.99996, asi que en la desconversion
               //      deberia intentar hacer redondeo para mostrar.
               //      Preguntar en la lista de OpenEHR si alguien supo resolver esto de una forma que no sea
               //      cambiando el tipo del Quantity por 2 campos individuales en un cluster.
               
               println "Old: " + oldValue + ", New: " + newValue
               
               params.(transform.path) = newValue
            }
            catch (Exception e)
            {
               println "WARNING: Se intenta transformar un valor con mal formato: '" + params.(transform.owner.owner.id+transform.path) + "'"
            }
         }
         else
         {
            println "Transform: Params no tiene la path: " + transform.owner.owner.id + transform.path
         }
      }
      println "+++++++++++++++++++++++++++++++++++++++++++++++++++"
         
      //render(view:'showParams')
      
      // params es GrailsParameterMap y bind espera LinkedHashMap
      / *
      def pathValue = [:]
      
      params.each{
         //println "   (=) " + params[it.key].getClass().getSimpleName()
         //println "   (+) " + it.value.getClass().getSimpleName()
         //println "   it: " + it
         // no deja pasar los parametros que se separan con puntos, los "railsified params".
         if (it.value instanceof String || it.value.getClass().isArray())
           pathValue[it.key] = it.value
      }
      * /
      
      // VERIFY:
      // La forma Groovy de hacerlo, ademas le agrego que filtre los valores
      // vacios, porque se esta bindeando para strings vacios, y cuando se
      // hace la validacion de ocurrencias deberia saltar que falta un valor
      // sin necesidad de pasarle todas las path con valor vacio.
      // FIXME: aunque no pase los valores vacios, igual llega hasta el ultimo
      // nodo para bindear, porque recorre el arquetipo. En realidad deberia
      // parar de bindear cuando me doy cuenta que viola una restriccion,
      // si sigo para abajo ya se que va a seguir violando.
      // Y si quiero crear una estructura para ponerle errores, puedo hacerlo
      // solo con el arquetipo y sin bindear, es como activar errores para
      // todas la restricciones del arquetipo para una cierta subestructura.
      // Pero, si tengo una estructura opcional y no viene valor, no deberia
      // seguir bindeando, porque si la estructura es opcional, todas las
      // subestructuras lo seran.
      Map pathValue = params.findAll {
         it.value != '' && ( it.value instanceof String || it.value.getClass().isArray() )
      }
      //println "   -=-=- PATH VALUE: " + pathValue
   
      // Pongo archivos (puede no venir ninguno)
      pathValue += files
   
      
      // Bind
      BindingAOMRM bindingAOMRM = new BindingAOMRM(session)
      def rmobj = bindingAOMRM.bind(pathValue, params.templateId)
      
      //println "Params: " + params.toString() + "\n"
      //println "Path value: " + pathValue + "\n"
      
      //XStream xstream = new XStream()
      //println xstream.toXML(rmobj)
      //File file = new File("log.xml")
      //file << xstream.toXML(rmobj)
   
      if (rmobj)
      {
         //if (!rmobj.save() || bindingAOMRM.getErrors().hasErrors() || bindingAOMRM.hasErrors() )
         // flush para que el save se haga de inmediato
         // FIXME: bindingAOMRM.getErrors() no se esta usando
         if (!rmobj.save(flush: true) || bindingAOMRM.hasErrors() ) 
         {
            // No se usa mas
            //if (bindingAOMRM.getErrors().hasErrors())
            //   println "Hay errors en los errors del binder: " + bindingAOMRM.getErrors()
            if (bindingAOMRM.hasErrors())
               println "Hay errors en el binder (bandera errors en true)"
            else
               println "Errores en rmobj: " + rmobj.errors
         
            //println "ERROR AL SALVAR: ---> " + rmobj.errors // Los errores pueden estar en uno de los objetos del arbol
            //println "TheErrors: " + bindingAOMRM.getErrors() + "\n\n"
            // TIENE QUE VOLVER Al CREATE con los errores y valores ya ingresados.
            // No puedo hacer redirect porque pierdo los valores y los errores.

            def sections = this.getSections()
            def subsections = this.getSubsections(params.templateId.split("-")[0]) // this.getSubsections('EVALUACION_PRIMARIA')
            
            EventManager.getInstance().handle("post_save_error", [composition:comp])

            flash.message = 'Hay errores en los datos, por favor verifíquelos'

            render(view: 'generarEdit',
                  model: [ rmNode: rmobj, // si no pudo guardar no puedo hacer get a la base...
                     index: bindingAOMRM.getRMRootsIndex(),
                     template: template,
                     mode: 'edit',
                     //errors: bindingAOMRM.getErrors(), // FIXME: esto creo que ya no se usa...
                     episodeId: session.traumaContext?.episodioId, // necesario para el layout
                     userId: session.traumaContext.userId,
                     subsections: subsections,
                     allSubsections: this.getDomainTemplates() 
                     //grailsApplication.config.hce.emergencia.sections.trauma // Mapa nombre seccion -> lista de subsecciones
                  ]
                 )
            return
         }
         else
         {
            println "SALVADO ENTRY O SECTION OK"
            
            
            // ========================================================================
            // Guarda el los valores submiteados para poder generar las vistas mas
            // rapido (cache) sin necesidad de recorrer toda la estructura del RM.
            PathValores paramsCache = new PathValores(params: pathValue, item: rmobj)
            paramsCache.save()
            
            
            // Se linkea las Entry y Section bindeadas a la Composition Correspondiente
            comp.addToContent(rmobj)
            if (!comp.save())
            {
               println "ERROR AL SALVAR COMPOSITION"
               // TODO
               // Todas las salvadas que se hacen ahí deberían ser
               // parte de una misma transaccion y si algo falla,
               // volver todo para atrás, ir a la página y decirle
               // que intente submitear de nuevo, mostrándole la
               // pantalla con los valores que acaba de ingresar,
            }
            else
            {
               println "SALVADA COMPOSITION OK"
            
               // Hay un handler que se encarga de verificar si se dan
               // las condiciones de cierre del registro.
               EventManager.getInstance().handle("post_save_ok", [composition:comp])
            
               // Redirige a show para mostrar el registro ingresado.
               redirect(action: 'generarShow', params: [id:rmobj.id])
               return
            }
         }
      }
      else
      {
         // volver a la pagina y pedirle que ingrese algun dato
         println "EL RESULTADO DEL BINDEO ES NULL"
      }
   
      // FIXME: aqui no deberia llegar
      XStream xstream = new XStream()
      render( text: xstream.toXML(rmobj), contentType:'text/xml' )
   } // save
   */
   
   /**
   * genera sho o edit, depende del 'mode'
   * in: id identificador del locatable
   */
   /*
   def generarShow = {
      
      println "----------------------------------------------------"
      println "GENERAR SHOW"
      println ""

      // DEBE haber un episodio seleccionado para poder asociar el registro clinico.
      // Se necesita para mostrar el layout con el resumen del episodio arriba.
      if (!session.traumaContext?.episodioId)
      {
         flash.message = 'trauma.list.error.noEpisodeSelected'
         redirect(controller:'records', action:'list')
         return
      }
      
      // ContentItem
      def rmNode = Locatable.get(params.id)
      //println "TemplateID : "+ rmNode.archetypeDetails.templateId

      // FIXME: if !params.id
      // FIXME: if !rmNode (creo que esto seria chequear si existe el registro para el template y la composition, si no es asi, tambien lo deberia verificar)

      def composition = Composition.get( session.traumaContext?.episodioId )

      // FIXME: esta tira una except si hay mas de un pac con el mismo id, hacer catch
      def patient = hceService.getPatientFromComposition( composition )


//println "Template: " + template
//XStream xstream = new XStream()
//println xstream.toXML(template)
//println xstream.toXML(template.getArchetypesByZone('content'))
//println xstream.toXML( hceService.getRMRootsIndex(template, rmNode) )
//println "============================================================"

      / *
      // Secciones predefinidas para seleccionar registro clinico
      // Es necesario para mostrar el menu
      def sections = []
      grailsApplication.config.hce.emergencia.sections.trauma.keySet().each { sectionPrefix ->
         sections << sectionPrefix
      }

      // Subsections de la section seleccionada
      def subsections = []
      def subSectionPrefix = rmNode.archetypeDetails.templateId.split("-")[0]
      grailsApplication.config.hce.emergencia.sections.trauma."$subSectionPrefix".each { subsection ->
         subsections << subSectionPrefix + "-" + subsection
      }
      * /
      def sections = this.getSections()
      def subsections = this.getSubsections(rmNode.archetypeDetails.templateId.split("-")[0]) // this.getSubsections('EVALUACION_PRIMARIA')
      
      / * Prueba de generar el HTML de una entrada y guardarlo en una variable.
      // IDEA PARA METER los datos en formato HTML en el CDA...
      def a = g.render(template:'generarShow', model:
         [
         rmNode:   rmNode,
         template:  template,
         index:    hceService.getRMRootsIndex(template, rmNode),
         episodeId: session.traumaContext?.episodioId, // necesario para el layout
         mode: ((params.mode)?params.mode:'show'),
         userId: session.traumaContext.userId,
         sections: sections,
         subsections: subsections,
         allSubsections: grailsApplication.config.hce.emergencia.sections.trauma // Mapa nombre seccion -> lista de subsecciones
         ]
      )
      println a
      * /
      
      // Template del ContentItem del que se quiere hacer show
      def template = TemplateManager.getInstance().getTemplate( rmNode.archetypeDetails.templateId )

      // Ver si el template es de una pagina estatica, el edit no es autogenerado,
      // pero el show si es autogenerado.
      String pathToView = '../hce/'+template.id
      def fullUri = grailsAttributes.getViewUri(pathToView, request)
      def resource = grailsAttributes.pagesTemplateEngine.getResourceForUri(fullUri)
      if ( resource && resource.file && resource.exists() && params.mode == 'edit')
      {
         println 'vista estatica'
         
         EventManager.getInstance().handle("post_gui_gen", params)
         
         render( view: pathToView, // create y edit son la misma pagina, pero podrian ser distintas
               model: [
               rmNode: rmNode,
               patient: patient,
               template: template,
               sections: sections,
               subsections: subsections,
               episodeId: session.traumaContext?.episodioId,
               mode: params.mode,
               userId: session.traumaContext.userId,
               allSubsections: this.getDomainTemplates()
               ] )
         return
      }
      else
      {
         println 'vista dinamica'
      
         // El template lo puedo sacar del objeto Locatable del RM.
         // TODO: lo que no tengo es el indice de arquetipo y estructura del RM
         //      que me dice para un estructura bindeada, donde estan las raices
         //      de las estructuras que se bindean por cada slot.
         //      Es lo que obtengo de bindingAOMRM.getRMRootsIndex() pero aca
         //      no tengo la instancia de bindindAOMRM que usé para bindear.
         //      Lo que se podría hacer es guardarla en la base, referenciando 
         //      como raíz al locatable que obtengo acá, así se cómo obtenerlo.
         
         if (params.mode=='edit')
         {
            render(view:'generarEdit', model:
            [
              rmNode:    rmNode,
              template:  template,
              index:     hceService.getRMRootsIndex(template, rmNode),
              episodeId: session.traumaContext?.episodioId, // necesario para el layout
              patient:   patient,
              userId:    session.traumaContext.userId,
              sections:  sections,
              subsections: subsections,
              allSubsections: this.getDomainTemplates(),
              mode:      'edit'
            ])
            return
         }
         
         return [
           rmNode:   rmNode,
           template:  template,
           index:    hceService.getRMRootsIndex(template, rmNode),
           episodeId: session.traumaContext?.episodioId, // necesario para el layout
           patient:patient,
           userId: session.traumaContext.userId,
           sections: sections,
           subsections: subsections,
           allSubsections: this.getDomainTemplates(),
           mode:      'show'
         ]
      }
   } // generarShow
   */
   
   def showRecord = {
      
      // DEBE haber un episodio seleccionado para poder asociar el registro clinico.
      // Se necesita para mostrar el layout con el resumen del episodio arriba.
      if (!session.traumaContext?.episodioId)
      {
         flash.message = 'trauma.list.error.noEpisodeSelected'
         redirect(controller:'records', action:'list')
         return
      }
      
      def composition = Locatable.get(session.traumaContext?.episodioId)
      if (!composition)
      {
         flash.message = 'trauma.list.error.noEpisodeSelected' // FIXME: el error es otro...
         redirect(controller:'records', action:'list')
         return
      }
      
      // FIXME: esta tira una except si hay mas de un pac con el mismo id, hacer catch
      def patient = hceService.getPatientFromComposition( composition )

      
      // NECESARIO PARA EL MENU
      def sections = util.TemplateUtils.getSections(session)
      //def subsections = this.getSubsections(templateId.split("-")[0]) // this.getSubsections('EVALUACION_PRIMARIA')
      
      // Igual que Records.show
      // Necesario para verificar permisos sobre el menu
      def completeSections = [:] // secciones con sus templates
      def domainTemplates = util.TemplateUtils.getDomainTemplates(session)
     
      domainTemplates.keySet().each { sectionPrefix ->
         domainTemplates."$sectionPrefix".each { section ->
         
            if (!completeSections[sectionPrefix]) completeSections[sectionPrefix] = []
         
            // Tiro la lista de esto para cada "section prefix" que son los templates
            // de las subsecciones de la seccion principal.
            //println sectionPrefix + "-" + section
            completeSections[sectionPrefix] << sectionPrefix + "-" + section
         }
      }
      
      return [composition: composition,
              // userId: session.traumaContext.userId,
              patient: patient,
              episodeId: session.traumaContext?.episodioId,
              sections: sections, // necesario para el menu
              allSubsections: util.TemplateUtils.getDomainTemplates(session),
              completeSections: completeSections
             ]
   }
}