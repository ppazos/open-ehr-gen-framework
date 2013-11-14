package hce

import org.codehaus.groovy.grails.commons.ApplicationHolder;

/**
 * Servicio auxiliar para generar vistas para los templates y guardarlas en strings.
 * 
 * @author pab
 * @link http://grails.1312388.n4.nabble.com/Render-a-view-to-String-td2255598.html
 */
class GuiCachingService {

   //def groovyPagesTemplateEngine
   def grailsTemplateEngineService // FIXME: poner las operaciones de este servicio dentro de esta clase
   //def ctx = ApplicationHolder.getApplication().getMainContext()
   
   /**
    * Genera todos los HTML los templates dados.
    * Este codigo antes estaba en el bootstrap.
    */
   def generateGUI(List templates)
   {
      // Se debe hacer un templateManager.loadAll() antes!!!
      
      def guiManager = gui.GuiManager.getInstance()
      String PS = System.getProperty("file.separator")
      
      String form
      File archivo
      String pathToStaticViews
        
      // dentro del directorio /grails-app/views al template _generarCreate.gsp
      // Si le pongo el . antes al pathToGuiGenXXXX genera path dentro de WEB-INF!:
      // grails-app\views\WEB-INF\grails-app\views\grails-app\views\guiGen\create\_generarCreate.gsp
      String pathToGuiGenCreate   = 'guiGen'+ PS +'create'+ PS +'_generarCreate' // dentro del directorio /grails-app/views al template _generarCreate.gsp
      String pathToGuiGenShow     = 'guiGen'+ PS +'show'  + PS +'_generarShow'
      String pathToGuiGenEdit     = 'guiGen'+ PS +'edit'  + PS +'_generarEdit'
      String pathToGeneratedViews = '.'+ PS +'grails-app'+ PS +'views'+ PS +'genViews'+ PS
      
        
      templates.each { tpl ->
        
         //println "GUIGEN TEMPLATE: " + tpl.templateId
           
         pathToStaticViews = '.'+ PS +'grails-app'+ PS +'views'+ PS +'hce'+ PS + tpl.templateId +'.gsp'
         
         
         // Si no existe la vista estatica, genero create, show y edit.
         // Si existe, solo genero show
         if (!new File(pathToStaticViews).exists())
         {
            //println "No existe vista estatica: $pathToStaticViews"
         
            // Se genera cada vista para cada locale disponible
            ApplicationHolder.application.config.langs.eachWithIndex { lang, i ->
              
                // FIX: http://code.google.com/p/open-ehr-gen-framework/issues/detail?id=62
               //Locale.setDefault( grailsApplication.config.locales[i] ) // No funciona
               // DatePIcker usa: new DateFormatSymbols(RCU.getLocale(request))
                 
               //println 'lang: '+ lang
               //println 'locale: '+ grailsApplication.config.locales[i].toString()
               
               // create
               //form = guiCachingService.template2String('guiGen\\create\\_generarCreate', [template:tpl, lang:lang, locale:grailsApplication.config.locales[i]]) // FIXME: hacerlo para todos los locales
               form = this.template2String(pathToGuiGenCreate, [template:tpl, lang:lang, locale:ApplicationHolder.application.config.locales[i]]) // FIXME: hacerlo para todos los locales
               form = form.replace('x</textarea>', '</textarea>') // reemplaza todo, pero sin usar regex
               //archivo = new File(".\\grails-app\\views\\genViews\\" + tpl.templateId + "_create_"+ lang +".htm")
               archivo = new File(pathToGeneratedViews + tpl.templateId + "_create_"+ lang +".htm")
               archivo.write(form)
               guiManager.add(tpl.templateId, "create", form)
                 
               // show
               //form = guiCachingService.template2String('guiGen\\show\\_generarShow', [template:tpl, lang:lang, locale:grailsApplication.config.locales[i]])
               form = this.template2String(pathToGuiGenShow, [template:tpl, lang:lang, locale:ApplicationHolder.application.config.locales[i]])
                 
               // http://code.google.com/p/open-ehr-gen-framework/issues/detail?id=59
               //form = form.replaceAll('<label class="(.*?)"(/s)/>', '<label class="$1"> </label>')
               form = form.replaceAll('<label class="(.*?)"(\\s*?)/>', '<label class="$1"> </label>')
               //archivo = new File(".\\grails-app\\views\\genViews\\" + tpl.templateId + "_show_"+ lang +".htm")
               archivo = new File(pathToGeneratedViews + tpl.templateId + "_show_"+ lang +".htm")
               archivo.write(form)
               guiManager.add(tpl.templateId, "show", form)
                 
               // edit
               //form = guiCachingService.template2String('guiGen\\edit\\_generarEdit', [template:tpl, lang:lang, locale:grailsApplication.config.locales[i]])
               form = this.template2String(pathToGuiGenEdit, [template:tpl, lang:lang, locale:ApplicationHolder.application.config.locales[i]])
               form = form.replace('x</textarea>', '</textarea>') // reemplaza todo, pero sin usar regex
               //archivo = new File(".\\grails-app\\views\\genViews\\" + tpl.templateId + "_edit_"+ lang +".htm")
               archivo = new File(pathToGeneratedViews + tpl.templateId + "_edit_"+ lang +".htm")
               archivo.write(form)
               guiManager.add(tpl.templateId, "edit", form)
            }
         }
         else // Genera solo show
         {
            println "Existe vista estatica: $pathToStaticViews"
            
            // Se genera cada vista para cada locale disponible
            ApplicationHolder.application.config.langs.eachWithIndex { lang, i ->
              
               // idem para el show
               //form = guiCachingService.template2String('guiGen\\show\\_generarShow', [template:tpl, lang:lang, locale:grailsApplication.config.locales[i]]) // FIXME: i18n
               form = this.template2String(pathToGuiGenShow, [template:tpl, lang:lang, locale:ApplicationHolder.application.config.locales[i]])
               form = form.replaceAll('<label class="(.*?)"(\\s*?)/>', '<label class="$1"> </label>')
               //archivo = new File(".\\grails-app\\views\\genViews\\" + tpl.templateId + "_show_"+ lang +".htm")
               archivo = new File(pathToGeneratedViews + tpl.templateId + "_show_"+ lang +".htm")
               archivo.write(form)
               guiManager.add(tpl.templateId, "show", form)
            }
         }
      }
   }
   
   /**
    * @param pathToGspTemplate
    * @param model [template, lang, locale]
    */
   String template2String(pathToGspTemplate, model)
   {
      //def tagLib = ctx.getBean('org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib')
      //String out = tagLib.render(template:pathToGspTemplate, model: model)
      //return out
      
      //No signature of method: GrailsTemplateEngineService.renderWithTemplateEngine()
      //return grailsTemplateEngineService.renderWithTemplateEngine(pathToGspTemplate, model)
      
      //return grailsTemplateEngineService.renderView(pathToGspTemplate, model)
      
      String xml = grailsTemplateEngineService.renderView(pathToGspTemplate, model)
      
      // Para resolver:
      // org.xml.sax.SAXParseException: The entity "aacute" was referenced, but not declared
      List remover = ['\\&aacute;', '\\&eacute;', '\\&iacute;', '\\&oacute;', '\\&uacute;', '\\&ntilde;', '&']
      List reemplazo = ['á', 'é', 'í', 'ó', 'ú', 'ñ', '&amp;']
      
      for (int i in 0..remover.size()-1)
      {
         xml = xml.replaceAll(remover[i], reemplazo[i])
      }
      
      // Intento de preety print del xml
      //String xml = output.toString()
      def node
      def ret
      try
      {
         node = new XmlParser().parseText(xml)
         ret = groovy.xml.XmlUtil.serialize( node )
      }
      catch (Exception e)
      {
         println e.message
         println xml.replaceAll("\n", "")
      }
      return ret
   }
   
   /*
   String template2String2(templatePath, model)
   {
      File file = new File(templatePath)
      String templateText = file.content
      
      def output = new StringWriter()
      def tmpl = groovyPagesTemplateEngine.createTemplate(templateText, 'sample')
      tmpl.make(model).writeTo(output)
      //render output.toString()
      
      
      // Intento de preety print del xml
      String xml = output.toString()
      def node = new XmlParser().parseText(xml);
      
      // no funka
      //def stringWriter = new StringWriter()
      //new XmlNodePrinter(new PrintWriter(stringWriter)).print(node)
      //return stringWriter.toString()
      
      // otro intento
      // no funk
      //return new groovy.xml.StreamingMarkupBuilder().bindNode(node).toString()
      
      // otro tampoco
      return groovy.xml.XmlUtil.serialize( node )
   }
   */
}