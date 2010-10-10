// locations to search for config files that get merged into the main config
// config files can either be Java properties files or ConfigSlurper scripts

// idiomas disponibles
langs = ['es','en','pt'] // ISO 639-1 Code

// donde se buscan con templates en disco, corresponde al domino de la HCE.
// TODO: el framework podria soportar multiples dominios ofreciendo una pantalla
//       con todos los dominios disponibles al usuario (segun su perfil) luedo de
//       que se loguea.
domain = 'hce/trauma'
//domain = 'hce/emergencia'

templates {
    hce {
        trauma {
            // en la composition se listan las sections y subsections, si tiene una sola es que no hay subsecciones.
            // con estos nombres se arman los nombres de los templates a pedir para cada registro.
            INGRESO = ['triage'] //,'test_body_weight'] //, 'test_a1_a2', 'test_cluster', 'test_dates']
            ADMISION = ['prehospitalario', 'contexto_del_evento']
            ANAMNESIS = ['resumen_clinico']
            EVALUACION_PRIMARIA = [
                                   'via_aerea',
                                   'columna_vertebral',
                                   'ventilacion',
                                   'estado_circulatorio',
                                   'disfuncion_neurologica'
                                  ]
            PARACLINICA = ['pedido_imagenes', 'pedido_laboratorio']
            EVALUACION_SECUNDARIA = ['exposicion_corporal_total']
            DIAGNOSTICO = ['diagnosticos']
            // decisiones terapeuticas evolutivas, ISS
            COMUNES = ['movimiento_paciente']
        }
        emergencia {
            ACCIONES = ['adm_sust']
            DIAGNOSTICO = ['diagnosticos']
        }
        ambulatorio {
            
        }
        quirurgica {
            
        }
    }
}

hce {
    patient_administration {
        serviceType {
            local = true // busqueda de pacientes es local a no ser que diga lo contrario
        }
    }
}

openEHR.RMVersion = '1.0.2'

// Ruta a directorio en donde se almacenan los CDAs generados

hce.rutaDirCDAs = ".\\CDAs"

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
}


     