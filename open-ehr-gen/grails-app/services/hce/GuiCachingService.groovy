package hce

import org.codehaus.groovy.grails.commons.ApplicationHolder;

/**
 * Servicio auxiliar para generar vistas para los templates y guardarlas en strings.
 * 
 * @author pab
 * @link http://grails.1312388.n4.nabble.com/Render-a-view-to-String-td2255598.html
 */
class GuiCachingService {

   def groovyPagesTemplateEngine;
   def grailsTemplateEngineService;
   //def ctx = ApplicationHolder.getApplication().getMainContext();
   
   // view is a path relative to the grails-app/views/ directory
   String view2String(view, model)
   {
      // Get a template of the view gsp
      def tmpl = groovyPagesTemplateEngine.createTemplate(view);
      
      //def path = ApplicationHolder.getApplication().getParentContext().getServletContext().getRealPath(".");
      //println "path $path"
      /*
      if (!tmpl)
      {
         println "A"
         tmpl = groovyPagesTemplateEngine.createTemplate('.\\guiGen\\_generarCreate.gsp');
      }
      if (!tmpl)
      {
         println "B"
         tmpl = groovyPagesTemplateEngine.createTemplate('.\\views\\guiGen\\_generarCreate.gsp');
      }
      if (!tmpl)
      {
         println "C"
         tmpl = groovyPagesTemplateEngine.createTemplate('.\\grails-app\\views\\guiGen\\_generarCreate.gsp');
      }
      
      if (!tmpl)
      {
         println "D"
         tmpl = groovyPagesTemplateEngine.createTemplate(path+'/guiGen/_generarCreate');
      }
      if (!tmpl)
      {
         println "E"
         tmpl = groovyPagesTemplateEngine.createTemplate(path+'/views/guiGen/_generarCreate');
      }
      if (!tmpl)
      {
         println "F"
         tmpl = groovyPagesTemplateEngine.createTemplate(path+'/grails-app/views/guiGen/_generarCreate');
      }
      
      if (!tmpl)
      {
         println "G"
         tmpl = groovyPagesTemplateEngine.createTemplate(path+'guiGen/_generarCreate.gsp');
      }
      if (!tmpl)
      {
         println "H"
         tmpl = groovyPagesTemplateEngine.createTemplate(path+'views/guiGen/_generarCreate.gsp');
      }
      if (!tmpl)
      {
         println "I"
         tmpl = groovyPagesTemplateEngine.createTemplate(path+'grails-app/views/guiGen/_generarCreate.gsp');
      }
   */
      // Provide a place to store the processed gsp
      def out = new StringWriter();
        
      // Process the gsp with the given model and write to the StringWriter
      tmpl.make(model).writeTo(out);
        
      return out.toString();
   }
   
   String template2String(template, model)
   {
      //def tagLib = ctx.getBean('org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib')
      //String out = tagLib.render(template:template, model: model)
      //return out
      
      //No signature of method: GrailsTemplateEngineService.renderWithTemplateEngine()
      //return grailsTemplateEngineService.renderWithTemplateEngine(template, model)
      
      //return grailsTemplateEngineService.renderView(template, model)
      
      String xml = grailsTemplateEngineService.renderView(template, model)
      
      // Para resolver:
      // org.xml.sax.SAXParseException: The entity "aacute" was referenced, but not declared
      List remover = ['\\&aacute;', '\\&eacute;', '\\&iacute;', '\\&oacute;', '\\&uacute;', '\\&ntilde;']
      List reemplazo = ['á', 'é', 'í', 'ó', 'ú', 'ñ']
      
      for (int i in 0..remover.size()-1)
      {
         xml = xml.replaceAll(remover[i], reemplazo[i])
      }
      
      // Intento de preety print del xml
      //String xml = output.toString()
      def node = new XmlParser().parseText(xml);
      return groovy.xml.XmlUtil.serialize( node )
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