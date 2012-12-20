//import i18n.I18nService

import org.springframework.web.servlet.LocaleResolver
import org.springframework.web.servlet.support.RequestContextUtils as RCU
import org.codehaus.groovy.grails.commons.GrailsClassUtils as GCU

import org.springframework.util.StringUtils // Para poder parsear strings y obtener locales
                                            // Locale locale = StringUtils.parseLocaleString( it );

class LocaleFilters {

//  def i18nService

   def filters = {

      // put a locale by default. 
      setLocale(controller:'*', action:'*')
      {
         before = {
            if (params.sessionLang)
            {
               // to be used from login screens (temporary)
               try 
               {
                  // Codigo de Staffmember.changeLocale
                  log.debug("sessionLang parameter:"+params.sessionLang)
                  
                  // El locale de java tiene formato aa_AA_aaaa
                  def localeParts = params.sessionLang.split('_') // lang, country, variant
                  
                  //Locale locale = new Locale(params.sessionLang)
                  Locale locale
                  switch (localeParts.length)
                  {
                     case 1:
                       locale = new Locale(localeParts[0])
                     break;
                     case 2:
                        locale = new Locale(localeParts[0], localeParts[1])
                     break;
                     case 3:
                        locale = new Locale(localeParts[0], localeParts[1], localeParts[2])
                     break;
                     // Otro caso termina en el locale por defecto.
                  }
                  
                  println "locale set to: " + locale.toString()
                  
                  //i18nService.setLocale(locale, request, response, session) // sets the session as well
                  if (request)
                  {
                     LocaleResolver localeResolver = RCU.getLocaleResolver(request);
                     localeResolver.setLocale(request, response, locale);
                  }
                  log.debug("session.locale: " + locale.toString())
                  session.locale = locale 
               }
               catch (Exception e)
               {
                  //log.error("Error changing locale to:"+params.lang)
                  log.debug("Exception:"+e)
                  println "Exception:"+e
               }
            }
            else if (session.user && session.user.selectedLocale)
            {
               session.locale = session.user.selectedLocale
            }
    
            // if still not set, set it by force
            if (!session.locale)
            {
              log.debug("session.locale not set. Forcing to spanish")
              def locale = new Locale('es')
              
              //session.locale = new Locale('en');
              //i18nService.setLocale(new Locale('en'), request, response, session) // sets the session as well
              
              if (request)
              {
                 LocaleResolver localeResolver = RCU.getLocaleResolver(request);
                 localeResolver?.setLocale(request, response, locale);
              }
              
              log.debug("session.locale: " + locale.toString())
              session.locale = locale 
              
            }
         }
      }
   }
}