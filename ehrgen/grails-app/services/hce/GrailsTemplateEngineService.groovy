package hce

import javax.servlet.http.HttpServletRequest
import org.codehaus.groovy.grails.web.servlet.DefaultGrailsApplicationAttributes
import org.codehaus.groovy.grails.web.pages.GroovyPagesTemplateEngine
import org.springframework.web.context.request.RequestContextHolder
import org.codehaus.groovy.grails.web.context.ServletContextHolder
import org.springframework.web.context.support.WebApplicationContextUtils
import org.codehaus.groovy.grails.plugins.PluginManagerHolder
import org.codehaus.groovy.grails.commons.GrailsResourceUtils
import org.springframework.web.servlet.LocaleResolver
import org.springframework.web.servlet.support.RequestContextUtils as RCU

class GrailsTemplateEngineService {

   String PS = System.getProperty("file.separator")

   static transactional = false

   GroovyPagesTemplateEngine groovyPagesTemplateEngine

   //static PATH_TO_VIEWS = "/WEB-INF/grails-app/views"
   static PATH_TO_VIEWS = "" // La path viene entera desde afuera...

   public String renderView(templateName, model, pluginName = null)
   {
      if(!groovyPagesTemplateEngine) throw new IllegalStateException("Property [groovyPagesTemplateEngine] must be set!")
      assert templateName

      def engine = groovyPagesTemplateEngine
      def requestAttributes = RequestContextHolder.getRequestAttributes()
      boolean unbindRequest = false

      def servletContext
      
      
      // outside of an executing request, establish a mock version
      if(!requestAttributes)
      {
         /* dice que servletContext es null...
         servletContext = ServletContextHolder.servletContext //getServletContext()
         def applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext)
         requestAttributes = grails.util.GrailsWebUtil.bindMockWebRequest(applicationContext)
         unbindRequest = true
        */
         
         def grailsApp = org.codehaus.groovy.grails.commons.ApplicationHolder.application
         
         //println "grailsApp: "+ grailsApp
         // grailsApp: org.codehaus.groovy.grails.commons.DefaultGrailsApplication@525d3

         def applicationContext = grailsApp.mainContext
         
         //println applicationContext.class
         //class org.codehaus.groovy.grails.commons.spring.GrailsWebApplicationContext
         
         requestAttributes = grails.util.GrailsWebUtil.bindMockWebRequest(applicationContext)
         unbindRequest = true
         
         /*
         class org.apache.catalina.core.ApplicationContextFacade
         class org.codehaus.groovy.grails.commons.spring.GrailsWebApplicationContext
         class org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
         class org.springframework.mock.web.MockHttpServletRequest
         class org.springframework.mock.web.MockServletContext
         
         println servletContext.class
         println applicationContext.class
         println requestAttributes.class
         println requestAttributes.request.class
         println requestAttributes.request.servletContext.class
         */
      }
      else
      {
         servletContext = requestAttributes.request.servletContext
      }
      
      
      def request = requestAttributes.request // org.springframework.mock.web.MockHttpServletRequest
      
      
      // En el contexto de un request, permite guardar el locale actual para reestablecerlo
      // al terminar de generar la GUI para que no quede cambiado para el siguiente request.
      // El locale se cambia para poder generar la GUI para distintos locales.
      //
      def currentLocale
      
      // Donde se setea el locale en el contexto de un request y donde se reestablece el actual.
      def localeResolver
      
      // Si es mock (no esta en el contexto de un request, ej. ejecucion desde el bootstrap)
      if (request instanceof org.springframework.mock.web.MockHttpServletRequest)
      {
         // http://code.google.com/p/open-ehr-gen-framework/issues/detail?id=62
         request.addPreferredLocale( model.locale )
      }
      else
      {
         // Nuevo: soporte de generacion de gui en el contexto de un request (por el usuario)
         
         // Como en LocaleFilters
         localeResolver = RCU.getLocaleResolver(request) // LocaleResolver http://static.springsource.org/spring/docs/2.0.x/api/org/springframework/web/servlet/support/RequestContextUtils.html
         
         // Guarda el locale actual
         currentLocale = RCU.getLocale(request)
         
         // Cambia el locale actual para generar la GUI
         localeResolver.setLocale(request, requestAttributes.response, model.locale)
      }

      
      //def grailsAttributes = new DefaultGrailsApplicationAttributes(servletContext)
      
      //println "path 0: " + templateName
      
      // See if the application has the view for it
      def uri = resolveViewUri(templateName, request)

      // guiGen\create\_generarCreate.gsp
      //println "URI: $uri"
      
      // URL [file:C:/dev/projects/ehrgen-1.3.7/grails-app/views/guiGen/create/_generarCreate.gsp]
      def r = engine.getResourceForUri(uri) 

      //println "R: $r"
      
      // Try plugin view if not found in application
      if ((!r || !r.exists()) && pluginName)
      {
         // Caution, this uses views/ always, whereas our app view resolution uses the PATH_TO_MAILVIEWS which may in future be orthogonal!
         def plugin = PluginManagerHolder.pluginManager.getGrailsPlugin(pluginName)
         String pathToView
         if (plugin)
         {
            pathToView = '/plugins/'+plugin.name+'-'+plugin.version+'/'+GrailsResourceUtils.GRAILS_APP_DIR+'/views'+templateName
         }

         // Este codigo me manda la path adentro del WEB_INF, cosa que no deberia pasar porque el template para generar la vista esta en views/guiGen
         if (pathToView != null)
         {
            uri = GrailsResourceUtils.WEB_INF +pathToView +templateName+".gsp";
            r = engine.getResourceForUri(uri)
         }
      }
      
      def t = engine.createTemplate( r )
      def out = new StringWriter();
      def originalOut = requestAttributes.getOut()
      requestAttributes.setOut(out)
      try
      {
         if(model instanceof Map)
         {
            t.make( model ).writeTo(out)
         }
         else
         {
            t.make().writeTo(out)
         }
      }
      finally
      {
         requestAttributes.setOut(originalOut)
         if(unbindRequest)
         {
            RequestContextHolder.setRequestAttributes(null)
         }
         
         // Reestablece locale actual si estoy en el contexto de un request
         if (localeResolver)
         {
            localeResolver.setLocale(request, requestAttributes.response, currentLocale)
         }
      }

      return out.toString();
   }

   protected String resolveViewUri(String viewName, HttpServletRequest request)
   {
      StringBuffer buf = new StringBuffer(PATH_TO_VIEWS);

      if(viewName.startsWith(PS))
      {
         String tmp = viewName.substring(1,viewName.length());
         if(tmp.indexOf(PS) > -1)
         {
            buf.append(PS);
            buf.append(tmp.substring(0,tmp.lastIndexOf(PS)));
            buf.append(PS);
            buf.append(tmp.substring(tmp.lastIndexOf(PS) + 1,tmp.length()));
         }
         else
         {
            buf.append(PS);
            buf.append(viewName.substring(1,viewName.length()));
         }
      }
      else
      {
         /*
         if (!request) throw new IllegalArgumentException(
            "View cannot be loaded from relative view paths where there is no current HTTP request")
         
         // Para la generacion desde bootstrap, getControllerUri es /
         // Entonces genera: /\guiGen\edit\_generarEdit.gsp
         def grailsAttributes = new DefaultGrailsApplicationAttributes(request.servletContext);
         buf.append(grailsAttributes.getControllerUri(request))
            .append(PS)
            .append(viewName);
         */
         // Supongo que viene la ruta correcta y no la proceso
         buf.append(viewName)
      }
      String path = buf.append(".gsp").toString();
      
      // Debe comenzar con guiGen que es donde estan los gsps para generar las GUIs
      // Por ejemplo si estoy llamando a generar gui desde el controller domain,
      // la path va a ser /domain/guiGen\create\_generarCreate.gsp
      while (!path.startsWith('guiGen'))
      {
         //println "path: $path"
         path = path.substring( path.indexOf(PS)+1, path.length())
      }
      
      //println "path N: $path"
      
      return path
   }
}