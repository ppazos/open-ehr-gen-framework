// Place your Spring DSL code here
//import org.springframework.web.servlet.i18n.SessionLocaleResolver

beans = {
   
   /* ninguno parece funcionar para tener un locale definido en el arranque.
   // http://grails.1312388.n4.nabble.com/Defining-default-language-td1366297.html
   // http://static.springsource.org/spring/docs/1.2.x/api/org/springframework/web/servlet/i18n/SessionLocaleResolver.html
   localeResolver(SessionLocaleResolver) {
      //Locale.setDefault(new Locale('es'))
      SessionLocaleResolver.setDefault(new Locale('es'))
   }
   */
   /*
   localeResolver(org.springframework.web.servlet.i18n.FixedLocaleResolver) {
      defaultLocale = new Locale('es')
   }
   */
}