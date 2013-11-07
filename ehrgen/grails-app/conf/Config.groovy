// locations to search for config files that get merged into the main config
// config files can either be Java properties files or ConfigSlurper scripts


// Ruta independiente del SO
// http://code.google.com/p/open-ehr-gen-framework/issues/detail?id=54
String PS = System.getProperty("file.separator")


// idiomas disponibles
langs = ['es','en'] //,'es_AR'] // ISO 639-1 Code
locales = [new Locale('es'), new Locale('en')] //, new Locale('es', 'AR')]
default_locale_string = 'es'

// donde se buscan con templates en disco, corresponde al domino de la HCE.
// El framework soporta multiples dominios ofreciendo una pantalla
// con todos los dominios disponibles al usuario (segun su perfil) luedo de
//  que se loguea.

// TODO: la gestion de dominios se deberia poder hacer desde el area de
//       configuracion, agregando nuevos dominios, y asociando multiples
//       templates a cada dominio. ESTO AHORA SE HACE DE FORMA DURA AQUI.

// Nuevo para organizar los registros por domain
// ver http://code.google.com/p/open-ehr-gen-framework/issues/detail?id=12
/*
domains = [
           '/domain.prehospitalario_same_uy',
           '/domain.prehospitalario',
           '/domain.emergencia',
           '/domain.trauma',
           '/domain.ambulatorio',
           '/domain.internacion_domiciliaria',
           '/domain.internacion_sala',
           '/domain.internacion_ci',
           '/domain.internacion_cti',
           '/domain.tests'
           ]
*/
// Ahora se usa solo nomnbre de dominio, TODO: i18n
domains = [
  'Prehospitalario SAME-UY',
  'Prehospitalario',
  'Emergencia',
  'Emergencia de Trauma',
  //'Ambulatorio',
  //'Internacion domiciliaria',
  //'Internacion en sala',
  'Tests'
]

// FIXME: no deberia ir a buscar los templates a distintos directorios,
//        distintos dominios pueden compartir templates.
//domain = 'hce/trauma'
//domain = 'hce/emergencia'

// Configuracion nueva, para usar con dominios
// Se va a cargar desde el bootstrap en el modelo de templates
// persistentes y no se va a usar directamente esta estructura.
templates2 {
   //path = 'hce' // Path en disco de los templates, no debe empezar ni terminar en / porque TemplateManager poner las /
   
   // =======================================================
   // dominio -> etapa -> template
   
   // Configuracion de templates por dominio,
   // cada dominio tiene un registro distinto
   // formado por multiples templates
   'Prehospitalario SAME-UY' {
      PREHOSPITALARIO = ['same_uy.v1', 'same_uy_ubicacion.v1']
   }
   'Emergencia de Trauma' {
      // en la composition se listan las sections y subsections, si tiene una sola es que no hay subsecciones.
      // con estos nombres se arman los nombres de los templates a pedir para cada registro.
      INGRESO = ['triage.v1'] //,'test_body_weight'] //, 'test_a1_a2', 'test_cluster', 'test_dates']
      ADMISION = ['prehospitalario.v1', 'contexto_del_evento.v1']
      ANAMNESIS = ['resumen_clinico.v1']
      EVALUACION_PRIMARIA = [
                             'via_aerea.v1',
                             'columna_vertebral.v1',
                             'ventilacion.v1',
                             'estado_circulatorio.v1',
                             'disfuncion_neurologica.v1'
                            ]
      PARACLINICA = ['pedido_imagenes.v1', 'pedido_laboratorio.v1']
      EVALUACION_SECUNDARIA = ['exposicion_corporal_total.v1']
      DIAGNOSTICO = ['diagnosticos.v1']
      // decisiones terapeuticas evolutivas, ISS
      COMUNES = ['movimiento_paciente.v1']
   }
   'Emergencia' {
      ACCIONES = ['adm_sust.v1']
      DIAGNOSTICO = ['diagnosticos.v1']
   }
   'Tests' {
      /*
      TEST = [
              //'cluster_obligatorio.v1',
              'cluster_obligatorio.v2',
              'cluster_obligatorio_multiple.v1',
              'cluster_obligatorio_multiple_struct.v1',
              'cluster_oblig_multiple_element_oblig.v1',
              'blood_pressure.v3', // No tiene INTERVAL_EVENT
              'blood_pressure_liotta.v1' // Tiene INTERVAL_EVENT
             ]
      DEMO = ['demo.v1']
      */
      SINERGIS = ['problemas.v1', 'derivacion.v1', 'resumen.v1', 'fallecimiento.v1', 'test_sts.v1']
   }
}


hce {
    patient_administration {
        serviceType {
            local = true // busqueda de pacientes es local a no ser que diga lo contrario
        }
    }
    clinical_record {
         save_rm_structure = true // http://code.google.com/p/open-ehr-gen-framework/issues/detail?id=61
    }
    close_record_job_on = false
    
    template_repo = 'templates'+ PS   // luego se pone la path del tipo y el templateId
    archetype_repo = 'archetypes'+ PS +'ehr'+ PS // luego se pone la path del tipo y el archetypeId
    uploaded_archetypes_repo = 'archetypes'+ PS +'uploaded'+ PS // directorio temporal donde se suben los arquetipos desde la web
    
    // Generalizar la vista de búsqueda de términos codificados
    // https://code.google.com/p/open-ehr-gen-framework/issues/detail?id=23
    terminologyServicesMapping = [
      'openEHR-EHR-OBSERVATION.test_servicios_terminologicos.v1_ac0001': util.terminology.CIE10LocalAccess,
      'openEHR-EHR-OBSERVATION.test_servicios_terminologicos.v1_ac0002': util.terminology.SnomedITServerAccess
    ]
}

openEHR.RMVersion = '1.0.2'

hce.rutaDirCDAs = '.'+ PS + 'CDAs'

// grails.config.locations = [ "classpath:${appName}-config.properties",
//                             "classpath:${appName}-config.groovy",
//                             "file:${userHome}/.grails/${appName}-config.properties",
//                             "file:${userHome}/.grails/${appName}-config.groovy"]

// if(System.properties["${appName}.config.location"]) {
//    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
// }
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [ html: ['text/html','application/xhtml+xml'],
                      xml: ['text/xml', 'application/xml'],
                      text: 'text/plain',
                      js: 'text/javascript',
                      rss: 'application/rss+xml',
                      atom: 'application/atom+xml',
                      css: 'text/css',
                      csv: 'text/csv',
                      all: '*/*',
                      json: ['application/json','text/json'],
                      form: 'application/x-www-form-urlencoded',
                      multipartForm: 'multipart/form-data'
                    ]
// The default codec used to encode data with ${}
grails.views.default.codec="none" // none, html, base64
grails.views.gsp.encoding="ISO-8859-1" //"UTF-8"
grails.converters.encoding="ISO-8859-1" //"UTF-8"

// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true

// set per-environment serverURL stem for creating absolute links
environments {
    production {
        grails.serverURL = "http://www.changeme.com"
    }
    development {
        grails.serverURL = "http://localhost:8080/${appName}"
    }
    test {
        grails.serverURL = "http://localhost:8080/${appName}"
    }

}

// log4j configuration
log4j = {
    // Example of changing the log pattern for the default console
    // appender:
    //
    //appenders {
    //    console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
    //}
//   appenders {
//      file name:'file', file:'hibernate.log'
//   }
   
    info 'com.linkedin.grails'

    error  'org.codehaus.groovy.grails.web.servlet',  //  controllers
	       'org.codehaus.groovy.grails.web.pages', //  GSP
	       'org.codehaus.groovy.grails.web.sitemesh', //  layouts
	       'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
	       'org.codehaus.groovy.grails.web.mapping', // URL mapping
	       'org.codehaus.groovy.grails.commons', // core / classloading
	       'org.codehaus.groovy.grails.plugins', // plugins
	       'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
	       'org.springframework',
	       'org.hibernate'

    warn   'org.mortbay.log'
    
    //info   'org.hibernate'
    //debug   file:'org.hibernate'
}


app {
   l10n { // localization
      
      // general
      decimal_symbol = ',' // separa numero enteros de la fraccion decimal
      decimal_digits = 2   // digitos luego de decimal_symbol
      digit_grouping = '.' // agrupador de a3 digitos para escribir numeros grandes ej. 1.000
      display_leading_zeros = true // ej. si es false, 0,7 se escribe ,7
      
      // formatos de fechas
      // ==================
      //  - ref: http://docs.oracle.com/javase/6/docs/api/java/text/SimpleDateFormat.html
      //
      // h hora 1-12
      // H hora 0-23
      // a marcador AM/PM
      // m minutos
      // S milisegundos
      // Z zona horaria (RFC 822)
      
      // formatos para procesamiento de fechas
      // incluye fraccion (debe estar separado con el decimal_symbol) y zona horaria
      datetime_format = "yyyyMMdd'T'HHmmss,SSSSZ" 
      date_format = "yyyyMMdd"
      time_format = "HHmmss"
      
      // formatos para mostrar las fechas al usuario
      display_datetime_format = "yyyy/MM/dd HH:mm:ss (Z)" 
      display_date_format = "yyyy/MM/dd"
      display_time_format = "HH:mm:ss"
      
      db_datetime_format = "yyyy-MM-dd HH:mm:ss" // mysql no soporta fragment o timezone, otros dbms si
      db_date_format = "yyyy-MM-dd"
      db_time_format = "HH:mm:ss"
   }
}