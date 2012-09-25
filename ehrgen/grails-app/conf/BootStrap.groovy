
import javax.management.relation.RoleInfo

import demographic.*
import demographic.contact.*
import demographic.party.*
import demographic.identity.*
import demographic.role.*
import support.identification.*
import authorization.*
import hce.core.common.change_control.Version
import hce.HceService
import tablasMaestras.*

// TEST Folder
import hce.core.common.directory.Folder
import data_types.text.*
import hce.core.common.archetyped.Archetyped
import org.springframework.web.context.support.WebApplicationContextUtils
//import org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib // Para usar g.message

// Crear compositions de prueba
import converters.DateConverter
import data_types.quantity.date_time.*

import templates.TemplateManager
import templates.tom.* // Template

// Para hacer un MOCK de una sesion para setear el locale temporalmente, para generar las pantallas correctamente.
import org.springframework.web.context.request.RequestContextHolder
import org.codehaus.groovy.grails.web.context.ServletContextHolder
import org.springframework.web.context.support.WebApplicationContextUtils
import org.springframework.web.servlet.support.RequestContextUtils as RCU

import gui.GuiManager

class BootStrap {

    def hceService
    def guiCachingService
    
    // Reference to Grails application. Lo inyecta.
    def grailsApplication
    
    def init = { servletContext ->
     
        println ""
        println "======= +++++++++ ======="
        println "======= Bootstrap ======="
        println "======= +++++++++ ======="
        println ""
        
        
        println "- correccion de la TimeZone a la de Montevideo/Buenos Aires"
        // Correccion de reloj segun uso horario
        // http://groovy.codehaus.org/JN0545-Dates
        // Esto si lo corrige!!!!
        //TimeZone.'default' = TimeZone.getTimeZone('GMT-03:00') //set the default time zone
        TimeZone.'default' = TimeZone.getTimeZone('America/Montevideo') // Con este considera daylight savings (cambios de +/- una hora por anio)
        
        
        // Parece que no puedo definir el locale sin estar en una sesion de verdad
        // Una SOLUCION podria ser que no se genere la GUI desde el bootstrap, sino que se genere desde una GUI de administracion.
        // ===================================================
        // MOCK del request temporal para usar el locale seleccionado que puede ser distinto al del sistema.
        //servletContext  = ServletContextHolder.getServletContext()
        /*
        def applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext)
        def requestAttributes = grails.util.GrailsWebUtil.bindMockWebRequest(applicationContext)
        
        // requestAttributes: ServletWebRequest: uri=;client=127.0.0.1
        println "requestAttributes: " + requestAttributes
        
        // no puedo llamarlo sin request
        println RCU.getLocale(requestAttributes.request) // en
        
        Locale.setDefault(new Locale('es'))
        println RCU.getLocale(requestAttributes.request) // en
        
        // getLocaleResolver tira null
        RCU.getLocaleResolver(requestAttributes.request).setLocale(requestAttributes.request, requestAttributes.response, new Locale('es'))
        */
        //
        // ===================================================
        
        
        // TEST Folder
        //def g = new org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib()
        def appContext = WebApplicationContextUtils.getWebApplicationContext( servletContext )
        def messageSource = appContext.getBean( 'messageSource' )
        
        def folder
        def domains = grailsApplication.config.domains

        if (Folder.count() == 0) // Si no se crearon los folders...
        {
           domains.each { domain ->
              
              folder = new Folder(
                 //name: new DvText(value: g.message(code: domain)),
                 //name: new DvText(value: messageSource.getMessage(domain, new Object[2], new Locale('es'))),
                 // para verificar seguridad necesito tambien el codigo en el folder.
                 name: new DvCodedText(
                   value: messageSource.getMessage(domain, new Object[2], new Locale('es')), // FIXME: I18N
                   definingCode: new CodePhrase(
                     codeString: domain,
                     terminologyId: TerminologyID.create('ehrgen', null)
                   )
                 ),
                 path: domain,
                 archetypeNodeId: "at0001",         // FIXME: Inventado
                 archetypeDetails: new Archetyped(  // FIXME: Inventado
                   archetypeId: 'ehr.domain',
                   templateId: 'ehr.domain',
                   rmVersion: '1.0.2' // FIXME: deberia ser variable global de config
                 )
              )
              
              // FIXME: no esta salvando...
              // TODO: setear atributos de Locatable
              
              if (!folder.save())
              {
                 println folder.errors
                 //println folder.name.errors
                 println folder.archetypeDetails.errors
              }
              
              // =====================================================================================
              // Crea registro de prueba para cada dominio
              def startDate = DateConverter.toIso8601ExtendedDateTimeFormat( new Date() )
              def composition = hceService.createComposition( startDate, "bla bla bla" )
              
              // Set parent
              //Folder domain = Folder.findByPath( session.traumaContext.domainPath )
              composition.padre = folder           
   
              if (!composition.save())
              {
                  println "Error: " + composition.errors
              }
              
              // Crea la version inicial
              def version = new Version(
                data: composition,
                timeCommited: new DvDateTime(
                  value: DateConverter.toIso8601ExtendedDateTimeFormat( new Date() )
                )
              )
              
              if (!version.save())
              {
                  println "ERROR: " + version.errors
              }
              // =====================================================================================
           }
        }
        // /TEST Folder
        
     
        println " - START: Carga catalogos maestros"
        
        // saco para acelerar la carga
        
        println "   - CIE 10..."
        if (Cie10Trauma.count() == 0)
        {
           def codigos = Cie10Trauma.getCodigos()
           codigos.each { codigo ->
              if (!codigo.save()) println codigo.errors
           }
        }
        else
        {
           println "      ya estan cargados"
        }
        
        
        println "   - OpenEHR Concepts..."
        if (OpenEHRConcept.count() == 0)
        {
           def oehr_concepts = OpenEHRConcept.getConcepts()
           oehr_concepts.each { concept ->
              if (!concept.save()) println concept.errors
           }
        }
        else
        {
           println "      ya estan cargados"
        }
        
        println "   - Tipos de identificadores..."
        if (TipoIdentificador.count() == 0)
        {
           def identificadores = TipoIdentificador.getTipos()
           identificadores.each { id ->
              if (!id.save()) println id.errors
           }
        }
        else
        {
           println "      ya estan cargados"
        }
        
        if (MotivoConsulta.count() == 0)
        {
           println "   - Motivos de consulta (idem tipos de evento)..."
           def eventos = MotivoConsulta.getTipos()
           eventos.each { evento ->
              if (!evento.save()) println evento.errors
           }
        }
        else
        {
           println "      ya estan cargados"
        }
        
        if (EmergenciaMovil.count() == 0)
        {
           println "   - Empresas emergencia movil..."
           def emergencias = EmergenciaMovil.getEmergencias()
           emergencias.each { emergencia ->
              if (!emergencia.save()) println emergencia.errors
           }
        }
        else
        {
           println "      ya estan cargados"
        }
        
        if (DepartamentoUY.count() == 0)
        {
           println "   - Departamentos UY..."
           def departamentos = DepartamentoUY.getDepartamentos()
           departamentos.each { dpto ->
              if (!dpto.save()) println dpto.errors
           }
        }
        else
        {
           println "      ya estan cargados"
        }
        
        println " - END: Carga tablas maestras"
        
        // TODO: no crear si ya existen
        
        // ----------------------------------------------------------------------------
        
        println " - Creacion de pacientes de prueba"
        
        
        //
        Permit.createDefault() // controler/action
        //
        DomainPermit.createDefault() // domain/templateId
        //
        
        // ROLES: se crea una instancia por cada rol existente.
        // Luego el admin puede crear otros roles y asignar permisos.
        // TODO: crear un usuario para el rol GODLIKE
        def rGodLike = new Role(type: Role.GODLIKE)
        if (!rGodLike.save()) println rGodLike.errors
        
        def rAdmin = new Role(type: Role.ADMIN)
        if (!rAdmin.save()) println rAdmin.errors
        
        def rPaciente = new Role(type: Role.PACIENTE)
        if (!rPaciente.save()) println rPaciente.errors
        
        def rMedico = new Role(type: Role.MEDICO)
        if (!rMedico.save()) println rMedico.errors
        
        def rEnfermeria = new Role(type: Role.ENFERMERIA)
        if (!rEnfermeria.save()) println rEnfermeria.errors
        
        def rAdministrativo = new Role(type: Role.ADMINISTRATIVO)
        if (!rAdministrativo.save()) println rAdministrativo.errors

        
        // Los roleValidity se guardan al guardar las personas
        
        
        def paciente = new Person(primerNombre:'Pablo', primerApellido:'Pazos')
        
        paciente.addToIds( new UIDBasedID(value:'2.16.840.1.113883.2.14.2.1::1234567') )
        paciente.addToIds( new UIDBasedID(value:'2.16.840.1.113883.2.14.1.1.1.3.1.5.1::6677') )
        paciente.fechaNacimiento = new Date(81, 9, 24) // 24/10/1981
        paciente.type = "Persona" // FIXME: el type no se setea solo con el nombre de la clase? (Person)
        paciente.sexo = "M"
        def validityPac1 = new RoleValidity(performer: paciente, role: rPaciente)
        paciente.addToRoles(validityPac1)
        if (!paciente.save()) println paciente.errors
        
        def pac2 = new Person(primerNombre:'Leandro', primerApellido:'Carrasco')
        pac2.addToIds( new UIDBasedID(value:'2.16.840.1.113883.2.14.2.4::2345678') )
        pac2.addToIds( new UIDBasedID(value:'2.16.840.1.113883.2.14.1.1.1.3.1.5.1::3366') )
        pac2.fechaNacimiento = new Date(82, 10, 25)
        pac2.type = "Persona"
        pac2.sexo = "M"
        def validityPac2 = new RoleValidity(performer: pac2, role: rPaciente)
        pac2.addToRoles(validityPac2)
        if (!pac2.save()) println pac2.errors
        
        def persona3 = new Person(primerNombre:'Marta', primerApellido:'Doctora')
        persona3.addToIds( new UIDBasedID(value:'2.16.840.1.113883.4.330.858::6667778') )
        //persona3.addToIds( new UIDBasedID(value:'2.16.840.1.113883.2.14.1.1.1.3.1.5.1::444') ) // este es un id de paciente, si fuera paciente tambien deberia asociarle el rol
        persona3.fechaNacimiento = new Date(83, 11, 26)
        persona3.type = "Persona"
        persona3.sexo = "F"
        def validityMed1 = new RoleValidity(performer: persona3, role: rMedico)
        persona3.addToRoles(validityMed1)
        if (!persona3.save()) println persona3.errors
        
        def persona4 = new Person(primerNombre:'Pablo', primerApellido:'Cardozo')
        persona4.addToIds( new UIDBasedID(value:'2.16.840.1.113883.2.14.2.1::1234888') )
        persona4.addToIds( new UIDBasedID(value:'2.16.840.1.113883.2.14.1.1.1.3.1.5.1::44556') )
        persona4.fechaNacimiento = new Date(85, 9, 24) // 24/10/1981
        persona4.type = "Persona"
        persona4.sexo = "M"
        def validityPac3 = new RoleValidity(performer: persona4, role: rPaciente)
        persona4.addToRoles(validityPac3)
        if (!persona4.save()) println persona4.errors
        
        def persona5 = new Person(primerNombre:'Marcos', primerApellido:'Carisma')
        persona5.addToIds( new UIDBasedID(value:'2.16.840.1.113883.4.330.858::45687543') )
        persona5.addToIds( new UIDBasedID(value:'2.16.840.1.113883.2.14.1.1.1.3.1.5.1::2233445') )
        persona5.fechaNacimiento = new Date(80, 11, 26)
        persona5.type = "Persona"
        persona5.sexo = "M"
        def validityPac4 = new RoleValidity(performer: persona5, role: rPaciente)
        persona5.addToRoles(validityPac4)
        if (!persona5.save()) println persona5.errors
        
        // Paciente con estudios imagenologicos en el CCServer local
        def persona6 = new Person(primerNombre:'CT', primerApellido:'Mister')
        persona6.addToIds( new UIDBasedID(value:'2.16.840.1.113883.4.330.666::2178309') ) // id en el CCServer
        persona6.type = "Persona"
        def validityPac5 = new RoleValidity(performer: persona6, role: rPaciente)
        persona6.addToRoles(validityPac5)
        if (!persona6.save()) println persona6.errors
        
        def persona_administrativo = new Person(primerNombre:'Charles', primerApellido:'Administrativo')
        persona_administrativo.addToIds( new UIDBasedID(value:'2.16.840.1.113883.2.14.2.1::3334442') )
        persona_administrativo.type = "Persona"
        persona_administrativo.sexo = "M"
        def validityPac9 = new RoleValidity(performer: persona_administrativo, role: rAdministrativo)
        persona_administrativo.addToRoles(validityPac9)
        if (!persona_administrativo.save()) println persona_administrativo.errors
        
        def persona_enfermera = new Person(primerNombre:'Juana', primerApellido:'Enfermera')
        persona_enfermera.addToIds( new UIDBasedID(value:'2.16.840.1.113883.2.14.2.1::9876456') )
        persona_enfermera.type = "Persona"
        persona_enfermera.sexo = "F"
        def validityPac7 = new RoleValidity(performer: persona_enfermera, role: rEnfermeria)
        persona_enfermera.addToRoles(validityPac7)
        if (!persona_enfermera.save()) println persona_enfermera.errors
        
        def persona_admin = new Person(primerNombre:'The', primerApellido:'Admin')
        persona_admin.addToIds( new UIDBasedID(value:'2.16.840.1.113883.2.14.2.1::98607521') )
        persona_admin.type = "Persona"
        persona_admin.sexo = "M"
        def validityPac8 = new RoleValidity(performer: persona_admin, role: rAdmin)
        persona_admin.addToRoles(validityPac8)
        if (!persona_admin.save()) println persona_admin.errors
        
        
        // =============================================================
        // ASIGNACION DE PERMISOS POR DEFECTO
        
        // ROL MEDICO, ACCESO A TODOS LOS DOMINIOS y todos los templates
        // -------------------------------------------------------------
        DomainPermit.findAllByTemplateId("*").each {
        
           rMedico.addToDomainPermits(it)
        }
        
        rMedico.save()
        //
        // =============================================================
      
        // ====================================================================================
        // LOGINS
        //
        // Login para el medico   
        def login = new LoginAuth(user:'user', pass:'pass', person: persona3)
        if (!login.save()) println login.errors
        
        // Login para el adminsitrativo
        def login_adm = new LoginAuth(user:'adm', pass:'1234', person: persona_administrativo)
        if (!login_adm.save()) println login_adm.errors
        
        def login_enf = new LoginAuth(user:'enf', pass:'1111', person: persona_enfermera)
        if (!login_enf.save()) println login_enf.errors
        
        def login_admin = new LoginAuth(user:'admin', pass:'admin', person: persona_admin)
        if (!login_admin.save()) println login_admin.errors
        
        // /Creacion de pacientes
        // ====================================================================================

        
        // ====================================================================================
        // Caching de formularios de ingreso de datos para todos los templates
        
        // Problema: la generacion de formularios depende del locale (los templates llaman a session.locale.language)
        //  - opcion 1: que se fije un locale en la config y se genere todo para ese
        //  - opcion 2: generar vistas para todos los locales que haya en la config
        
        // no parece funcar si le paso un session de mentira...
        //def session = [locale:[language:"es"]]
        
        //List domains = grailsApplication.config.domains // Ya esta definida mas arriba
        GuiManager guiManager = GuiManager.getInstance()
        Map domainTemplates
        
        String PS = System.getProperty("file.separator")
        
        domains.each { domain ->
           
           //println "Domain: $domain"
           domainTemplates = grailsApplication.config.templates2."$domain"
           domainTemplates.each{ entry ->
              
              //println " - "+ entry.key + ":"+entry.value // EVALUACION_PRIMARIA:[via_aerea, columna_vertebral, ...]
              
              String templateId
              String form
              File archivo
              entry.value.each { subsection -> // via_aerea
                 
                 templateId = entry.key + "-" + subsection // 'EVALUACION_PRIMARIA-via_aerea.v1'
                 
                 println "templateId: " + templateId
                 //println "split . " + templateId.split('\\.')
                 
                 
                 // Path independiente del OS
                 // http://code.google.com/p/open-ehr-gen-framework/issues/detail?id=54
                 // '.\\grails-app\\views\\hce\\'+templateId+'.gsp'
                 //
                 String pathToStaticViews = '.'+ PS +'grails-app'+ PS +'views'+ PS +'hce'+ PS + templateId +'.gsp'
                 //pathToStaticViews = pathToStaticViews.replaceAll("\\\\", "\\\\\\\\")
                 
                 
                 // Path independiente del OS
                 // http://code.google.com/p/open-ehr-gen-framework/issues/detail?id=54
                 // '.\\templates\\hce'
                 //
                 String pathToTemplates = '.'+ PS +'templates'+ PS +'hce'
                 
                 String pathToGuiGenCreate   = 'guiGen'+ PS +'create'+ PS +'_generarCreate' // dentro del directorio /grails-app/views al template _generarCreate.gsp
                 String pathToGuiGenShow     = 'guiGen'+ PS +'show'  + PS +'_generarShow'
                 String pathToGuiGenEdit     = 'guiGen'+ PS +'edit'  + PS +'_generarEdit'
                 String pathToGeneratedViews = '.'+ PS +'grails-app'+ PS +'views'+ PS +'genViews'+ PS
                 
                 
                 // FIXME: deberia generar para todas las versiones del template,
                 //        o sea: EVALUACION_PRIMARIA-via_aerea.vX, para toda X.
                 // Para ver todas las versiones del template, tengo que ir a buscar al disco.

                 // Para todas las versiones del template
                 String templatePrefix = templateId.split('\\.')[0] // 'EVALUACION_PRIMARIA-via_aerea', El nombre DEBE tener .vX
                 
                 
                 
                 // Si no existe la vista estatica, genero create, show y edit.
                 // Para la estatica solo genero el show.
                 //
                 //if (!new File('.\\grails-app\\views\\hce\\'+templateId+'.gsp').exists())
                 if (!new File(pathToStaticViews).exists())
                 {
                    // http://pleac.sourceforge.net/pleac_groovy/directories.html
                    //new File('.\\templates\\hce').eachFileMatch(~(templatePrefix+'\\.v\\d+\\.xml')) { f ->
                    new File(pathToTemplates).eachFileMatch(~(templatePrefix+'\\.v\\d+\\.xml')) { f ->
                       
                       // Template id del versionado.
                       String templateIdV = f.name - '.xml'
                       
                       //if (f.isFile()) println f.canonicalPath
                       
                       // FIXME: al template le falta la version en el modelo. Por ahora esta solo en el nombre del archivo.
                       Template template = TemplateManager.getInstance().getTemplate( templateIdV )
                      
                       // Se genera cada vista para cada locale disponible
                       grailsApplication.config.langs.eachWithIndex { lang, i ->
                       
                          // FIX: http://code.google.com/p/open-ehr-gen-framework/issues/detail?id=62
                          //Locale.setDefault( grailsApplication.config.locales[i] ) // No funciona
                          // DatePIcker usa: new DateFormatSymbols(RCU.getLocale(request))
                          
                          //println 'lang: '+ lang
                          //println 'locale: '+ grailsApplication.config.locales[i].toString()
                          //println 'template: '+ templateIdV
                          
                          //form = guiCachingService.template2String('guiGen\\create\\_generarCreate', [template:template, lang:lang, locale:grailsApplication.config.locales[i]]) // FIXME: hacerlo para todos los locales
                          form = guiCachingService.template2String(pathToGuiGenCreate, [template:template, lang:lang, locale:grailsApplication.config.locales[i]]) // FIXME: hacerlo para todos los locales
                          form = form.replace('x</textarea>', '</textarea>') // reemplaza todo, pero sin usar regex
                          //archivo = new File(".\\grails-app\\views\\genViews\\" + templateIdV + "_create_"+ lang +".htm")
                          archivo = new File(pathToGeneratedViews + templateIdV + "_create_"+ lang +".htm")
                          archivo.write(form)
                          guiManager.add(templateIdV, "create", form)
                          
                          // idem para el show
                          //form = guiCachingService.template2String('guiGen\\show\\_generarShow', [template:template, lang:lang, locale:grailsApplication.config.locales[i]])
                          form = guiCachingService.template2String(pathToGuiGenShow, [template:template, lang:lang, locale:grailsApplication.config.locales[i]])
                          
                          // http://code.google.com/p/open-ehr-gen-framework/issues/detail?id=59
                          //form = form.replaceAll('<label class="(.*?)"(/s)/>', '<label class="$1"> </label>')
                          form = form.replaceAll('<label class="(.*?)"(\\s*?)/>', '<label class="$1"> </label>')
                          //archivo = new File(".\\grails-app\\views\\genViews\\" + templateIdV + "_show_"+ lang +".htm")
                          archivo = new File(pathToGeneratedViews + templateIdV + "_show_"+ lang +".htm")
                          archivo.write(form)
                          guiManager.add(templateIdV, "show", form)
                          
                          // idem para edit
                          //form = guiCachingService.template2String('guiGen\\edit\\_generarEdit', [template:template, lang:lang, locale:grailsApplication.config.locales[i]])
                          form = guiCachingService.template2String(pathToGuiGenEdit, [template:template, lang:lang, locale:grailsApplication.config.locales[i]])
                          form = form.replace('x</textarea>', '</textarea>') // reemplaza todo, pero sin usar regex
                          //archivo = new File(".\\grails-app\\views\\genViews\\" + templateIdV + "_edit_"+ lang +".htm")
                          archivo = new File(pathToGeneratedViews + templateIdV + "_edit_"+ lang +".htm")
                          archivo.write(form)
                          guiManager.add(templateIdV, "edit", form)
                       }
                    }
                    
                    
                    /* Generacion sin considerar version del template, genera solo para el que esta configurado.
                     * 
                    Template template = TemplateManager.getInstance().getTemplate( templateId )
                    //form = guiCachingService.template2String('.\\grails-app\\views\\guiGen\\_generarCreate.gsp', [template:template])
                    form = guiCachingService.template2String('guiGen\\create\\_generarCreate', [template:template, lang:'es']) // FIXME: hacerlo para todos los locales
                    form = form.replace('x</textarea>', '</textarea>')
                    def archivo = new File(".\\grails-app\\views\\genViews\\" + templateId + "_create.htm")
                    archivo.write(form);
                    
                    // No hago cache para ver los tiempos de carga de disco
                    guiManager.add(templateId, "create", form);
                    
                    // idem para el show
                    form = guiCachingService.template2String('guiGen\\show\\_generarShow', [template:template, lang:'es'])
                    
                    archivo = new File(".\\grails-app\\views\\genViews\\" + templateId + "_show.htm")
                    archivo.write(form);
                    
                    guiManager.add(templateId, "show", form);
                    
                    
                    // idem para edit
                    form = guiCachingService.template2String('guiGen\\edit\\_generarEdit', [template:template, lang:'es'])
                    form = form.replace('x</textarea>', '</textarea>')
                    archivo = new File(".\\grails-app\\views\\genViews\\" + templateId + "_edit.htm")
                    archivo.write(form);
                    
                    guiManager.add(templateId, "edit", form);
                    
                    */
                 }
                 else // para la vista estatica genero el show igual
                 {
                    // http://pleac.sourceforge.net/pleac_groovy/directories.html
                    //new File('.\\templates\\hce').eachFileMatch(~(templatePrefix+'\\.v\\d+\\.xml')) { f ->
                    new File(pathToTemplates).eachFileMatch(~(templatePrefix+'\\.v\\d+\\.xml')) { f ->
                       
                       // Template id del versionado.
                       String templateIdV = f.name - '.xml'
                       
                       //if (f.isFile()) println f.canonicalPath
                       
                       // FIXME: al template le falta la version en el modelo. Por ahora esta solo en el nombre del archivo.
                       Template template = TemplateManager.getInstance().getTemplate( templateIdV )
                      
                       int i = 0
                       
                       // Se genera cada vista para cada locale disponible
                       grailsApplication.config.langs.each { lang ->
                       
                          // idem para el show
                          //form = guiCachingService.template2String('guiGen\\show\\_generarShow', [template:template, lang:lang, locale:grailsApplication.config.locales[i]]) // FIXME: i18n
                          form = guiCachingService.template2String(pathToGuiGenShow, [template:template, lang:lang, locale:grailsApplication.config.locales[i]])
                          form = form.replaceAll('<label class="(.*?)"(\\s*?)/>', '<label class="$1"> </label>')
                          //archivo = new File(".\\grails-app\\views\\genViews\\" + templateIdV + "_show_"+ lang +".htm")
                          archivo = new File(pathToGeneratedViews + templateIdV + "_show_"+ lang +".htm")
                          archivo.write(form)
                          guiManager.add(templateIdV, "show", form)
                          
                          i++
                       }
                    }
                 }
              }
           }
        }
        
        
        
        
        /* saco episodio de prueba para no generar problemas...
        
        println " - Creacion de episodio de prueba"
        
        def composition = hceService.createComposition( '2010-01-08 01:23:32', 'El paciente ingresa con dolor en el tobillo' )

        // Agrego el autor a la composición
        //def arrayIds = persona3.ids.toArray()
        //hceService.setCompositionComposer(composition, arrayIds[0].getRoot(), arrayIds[0].getExtension())

        def uidAutor = new UIDBasedID(value:'2.16.840.1.113883.2.14.1.1.1.3.1.5.1::444')
        hceService.setCompositionComposer(composition, uidAutor.getRoot(), uidAutor.getExtension())

        if (!composition.save())
        {
            println "Error: " + composition.errors
        }
        
        // Crea la version inicial
        def version = new Version(
          data: composition,
          timeCommited: new DvDateTime(
            value: '2010-01-08 01:23:32'
          )
        )
        
        if (!version.save())
        {
            println "ERROR: " + version.errors
        }
        
        // /Creacion de episodio
        */
        
        println ""
        println "======= +++++++++ ======="
        println "======= /Bootstrap ======="
        println "======= +++++++++ ======="
        println ""
        
     }
     def destroy = {
     }
} 