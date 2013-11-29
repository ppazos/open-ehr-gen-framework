grails.project.dependency.resolution = {
   // inherit Grails' default dependencies
   inherits("global") {
      // specify dependency exclusions here; for example, uncomment this to disable ehcache:
      // excludes 'ehcache'
   }
   
   log "error" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
   checksums true // Whether to verify checksums on resolve
    
   repositories {
      //inherits true // Whether to inherit repository definitions from plugins
      
      grailsPlugins()
      grailsHome()
      grailsCentral()

      mavenLocal()
      mavenCentral()
       
      mavenRepo "http://repo.grails.org"
   }
   dependencies {
   }
   plugins {
   
   }
}