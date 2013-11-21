package templates

import templates.*
import templates.constraints.*
import templates.controls.*

import org.codehaus.groovy.grails.commons.ApplicationHolder

/**
 * @author Pablo Pazos Gutierrez (pablo.pazos@cabolabs.com)
 */
class TemplateManager {
    
	// Ruta independiente del SO
    // http://code.google.com/p/open-ehr-gen-framework/issues/detail?id=54
    String PS = System.getProperty("file.separator")
	
    // FIXME> puede ser necesario sincronizar las estructuras porque son compartidas...
    // Cache: templateId => template
    private static Map<String, Template> cache = [:]
     
    // templateId => timestamp de cuando fue usado por ultima vez.
    // Sirve para saber si un template no fue utilizado por mucho tiempo, y bajarlo del cache par optimizar espacio en memoria.
    private static Map<String, Date> timestamps = [:]
     
    // SINGLETON
    private static TemplateManager instance = null
     
    private TemplateManager() {}
     
    public static TemplateManager getInstance()
    {
       if (!instance) instance = new TemplateManager()
       return instance
    }

    private Template parseTemplate( File templateFile )
    {
        //String fileContents = ""
        //templateFile.eachLine{line-> fileContents += line}   // Contenido del archivo a String
        //def xmlTemplate = new XmlSlurper().parseText( fileContents ) // XML parseado
        
        def xmlTemplate = new XmlSlurper().parseText( templateFile.getText() ) // XML parseado
        
        Template template = new Template()
        
        // El id incluye la version, ej. PARACLINICA-pedido_imagenes.v1
        template.templateId = xmlTemplate.id.text()
        template.name = xmlTemplate.name.text()
        
        template.rootArchetype = new ArchetypeReference()
        
        // <archetype type="cluster" id="openEHR-EHR-CLUSTER.a1.v1" includeAll="false">
        template.rootArchetype.type       = ArchetypeTypeEnum.fromValue( xmlTemplate.root_archetype.archetype.'@type'.text() )
        template.rootArchetype.refId      = xmlTemplate.root_archetype.archetype.'@id'.text()
        template.rootArchetype.includeAll = (xmlTemplate.root_archetype.archetype.'@includeAll'.text() == "true")
        template.rootArchetype.owner = template
        
        // TEST
        //println "-----"
        //println "rootArchetype: " + template.templateId + " " + template.rootArchetype.type
        //println "-----"
        
        def pageZone = xmlTemplate.root_archetype.archetype.'@pageZone'.text()
        if (pageZone) template.rootArchetype.pageZone = pageZone
        
        // FIXME: chequear si includeAll es false, debe tener algun nodo
        //        y si includeAll es true, no debe tener ningun nodo.
        
        // <field path="/items[at0001]/value/magnitude" />
        // <field path="/items[at0002]/value" />
        // <field path="/items[at0003]/value" />
        template.rootArchetype.fields = [] // List<ArchetypeField>
        
//        println "Root fields"
        xmlTemplate.root_archetype.archetype.field.each
        { xmlField ->

          def field = new ArchetypeField( path: xmlField.'@path'.text(),
                                          owner: template.rootArchetype,
                                          fieldConstraints: parseFieldConstraints( xmlField ),
                                          controls: parseFieldControls( xmlField )
                                        )
          
//          println "Controls: " + field.controls

          // Asociaciones constraint->field
          field.fieldConstraints.each { constraint ->
             constraint.owner = field
          }
          
          template.rootArchetype.fields << field
        }
//        println "/Root fields"
        
        
        template.includedArchetypes = [] // List<ArchetypeReference>
        
        
        xmlTemplate.included_archetypes.archetype.each
        { xmlArchetypeNode ->
        
           def ref = new ArchetypeReference()
           
           // <archetype type="cluster" id="openEHR-EHR-CLUSTER.a1.v1" includeAll="false">
           ref.type       = ArchetypeTypeEnum.fromValue( xmlArchetypeNode.'@type'.text() )
           ref.refId      = xmlArchetypeNode.'@id'.text()
           ref.includeAll = (xmlArchetypeNode.'@includeAll'.text() == "true")
           ref.owner      = template
           
           pageZone = xmlArchetypeNode.'@pageZone'.text()
           if (pageZone) ref.pageZone = pageZone
           
           // FIXME: chequear si includeAll es false, debe tener algun nodo
           //        y si includeAll es true, no debe tener ningun nodo.
           
           // <field path="/items[at0001]/value/magnitude" />
           // <field path="/items[at0002]/value" />
           // <field path="/items[at0003]/value" />
           ref.fields = [] // List<ArchetypeField>
           
//           println "Ref arch fields"
           xmlArchetypeNode.field.each
           { xmlField ->

             def field = new ArchetypeField( path: xmlField.'@path'.text(),
                                             owner: ref,
                                             fieldConstraints: parseFieldConstraints( xmlField ),
                                             controls: parseFieldControls( xmlField )
                                           )
//             println "Controls: " + field.controls

             // Asociaciones constraint->field
             field.fieldConstraints.each { constraint ->
          
                constraint.owner = field
             }

             ref.fields << field
           }
//           println "/Ref arch fields"
           
           template.includedArchetypes << ref
        }
        
        return template
    }
    
    def parseFieldConstraints( xml_field )
    {
        def constraints = []
        xml_field.transform?.each { xml_transform ->
           constraints << new Transform(
                                path: xml_transform.'@path'.text(),
                                operation: xml_transform.'@operation'.text(),
                                operand: xml_transform.'@operand'.text() )
        }
        xml_field.overwrite?.each { xml_transform ->
           constraints << new Overwrite(
                path: xml_transform.'@path'.text(),
                with: xml_transform.'@with'.text() )
        }
        return constraints
    }
    
   def parseFieldControls( xml_field )
   {
      def controls = []
      xml_field.control?.each { xml_control ->
         controls << new Control(
                           path: xml_control.'@path'.text(),
                           type: xml_control.'@type'.text() )
      }
      return controls
   }
    
   public Map getLoadedTemplates()
   {
      return this.cache
   }
    
   public Map getLastUse()
   {
      return this.timestamps
   }
    
   public void unloadAll()
   {
      // FIXME: debe estar sincronizada
      this.cache.clear()
      this.timestamps.clear()
   }
   
   public void unload( String templateId )
   {
      // FIXME: debe estar sincronizada
      this.cache.remove(templateId)
      this.timestamps.remove(templateId)
   }
   
   /**
    * Fuerza el cacheo de un template desde afuera.
    */
   public void cacheTemplate(Template template)
   {
      cache[template.templateId] = template
      timestamps[template.templateId] = new Date()
   }
   
   public Template getTemplate( String templateId )
   {
      //println "getTemplate: " + templateId
       
      // Si no esta cargado, lo intenta cargar
      if (!this.cache[templateId])
      {
         //println "No se encuentra el template " + templateId + ", se intenta cargarlo"
         
         //def id = new templateID( templateId ) // a partir del ID saco la ruta que tengo que cargar
         //def type = id.rmEntity // cluster, entry, composition, etc...
         
         // Nuevo!
         //println "  Intenta cargar archivo: templates" + PS + ApplicationHolder.application.config.templates2.path + PS + templateId +".xml"
         //File templateFile = new File( "templates"+ PS + ApplicationHolder.application.config.templates2.path + PS + templateId +".xml" )
         //File templateFile = new File( ApplicationHolder.application.config.hce.template_repo + getTypePath(type) + PS + templateId +".xml" )
         
         Template template
         
         // Se busca la path usando el templateId porque no se tiene el type del template.
         String templatePath = this.findTemplatePath(templateId)
         if (!templatePath)
         {
            // Verifica si el template esta en la base de datos pero no ha sido serializada a XML
            template = Template.findByTemplateId( templateId )
            if (template)
            {
               println "    Cargado el template: $templateId desde la base de datos"
               cache[templateId] = template
               timestamps[templateId] = new Date()
               
               // Ver si esto se hace en DomainController.createTemplate o aca
               // TODO: serializar a XML
               // TODO: generar GUI
               
               return template
            }
            else
            {
               println "  No se la path del template: $templateId ni tampoco esta en la base de datos"
               return null
            }
         }
         
         //println " -> hay que buscar el template $templateId en: " + templatePath
         
         
         File templateFile = new File( templatePath )
         
         /* templatePath sino es null existe siempre porque se crea consultando el filesystem
         if (!templateFile.exists())
         {
            //println "  No se encuentra el archivo: templates" + PS + ApplicationHolder.application.config.templates2.path + PS + templateId +".xml"
            println "  No se encuentra el archivo: " + ApplicationHolder.application.config.hce.template_repo + getTypePath(type) + PS + templateId +".xml"
            return null
         }
         */
         
         // Verifica que hay permisos de lectura
         if (!templateFile.canRead())
         {
            throw new Exception("No se puede leer " + templateFile.getCanonicalPath())
         }
         
         
         template = parseTemplate( templateFile )

         if (template)
         {
            println "    Cargado el template: " + templateFile.name + " de " + templateFile.path
            cache[templateId] = template
            timestamps[templateId] = new Date()
         }
         else
         {
            println "    No se pudo cargar el template $templateId desde " + templateFile.path
         }
      }
      else
      {
         println "Template: ${templateId} esta en cache << " + this.cache[templateId] + " >>"
         this.timestamps[templateId] = new Date() // actualizo timestamp
      }
        
      return this.cache[templateId]
   }
   
   /**
    * Serializa un template a XML y lo guarda en el repo local de templates.
   */
   public void saveTemplateToRepo( Template template )
   {
      String path = ApplicationHolder.application.config.hce.template_repo
      path += getTypePath( template.rootArchetype.type.toString() )
     
      println " - save template to: " + path
     
      def writer = new StringWriter()
      def xml = new groovy.xml.MarkupBuilder(writer)
     
      xml.template() {
         id(template.templateId + ".v1")
         name(template.name)
         root_archetype {
            archetype(type: template.rootArchetype.type.toString(),
                      id: template.rootArchetype.refId,
                      pageZone: template.rootArchetype.pageZone,
                      includeAll: template.rootArchetype.includeAll) {
               // TODO: serializar referencias a fields
            }
         }
         if (template.includedArchetypes?.size() > 0) // Puede no tener includedArchetypes
         {
            included_archetypes {
               template.includedArchetypes.each { archRef ->
                  archetype(type: archRef.type.toString(),
                            id: archRef.refId,
                            pageZone: archRef.pageZone,
                            includeAll: archRef.includeAll) {
                    // TODO: serializar referencias a fields
                  }
               }
            }
         }
      }
	  
	  println " - Template guardado en repo local: " + path + PS + template.templateId + ".xml"
	  File file = new File(path + PS + template.templateId + ".xml")
	  file << writer.toString()
	  
   }
   
   
   private String getTypePath( String type )
   {
      type = type.toLowerCase()
      switch (type)
      {
         case 'composition':
         case 'section':
            return type
         break
         case 'action':
         case 'evaluation':
         case 'instruction':
         case 'observation':
         case 'admin_entry':
            return 'entry'+ PS + type
         break
         default:
            throw new Exception('Tipo no conocido ['+ type +'], se espera uno de: composition, section, action, observation, instruction, evaluation, admin_entry' )
      }
   }
   
   /**
    * Carga todos los teplates del repo local.
    * Incluso distintas versiones del mismo template (necesario para mostrar registros viejos).
    */
   public void loadAll()
   {
      loadAllRecursive( ApplicationHolder.application.config.hce.template_repo )
   }
   
   /**
    * Carga templates recursivamente segun la path en el filesystem.
    */
   private loadAllRecursive( String path )
   {
      //println " --- loadAllRecursive: " + path
      def root = new File( path )
      
      Template template
      
      // Filtra solo archivos xml
      // eachFile tambien recorre subdirectorios!!! para eso se usa FILES
      root.eachFileMatch (groovy.io.FileType.FILES, ~/.*\.xml/) { f ->
      
         //println " ---- template file: " + f.name // EHRGen-EHR-adm_sust.v1.xml
         //println " ---- template path: " + f.path // templates\entry\action\EHRGen-EHR-adm_sust.v1.xml
         
         
         // Carga de disco, parsea y cachea
         // No es necesario tomar el template que devuelve, luego se
         // pueden pedir todos los templates usando getLoadedTemplates()
         //templateId = f.name - '.xml'
         //getTemplate(templateId)
         
         template = parseTemplate( f ) // TODO: probar si tira excepcion cuando lee un XML que no es un template
         if (template)
         {
            //println " ---- Cargado el template: " + f.path
            cache[template.templateId] = template
            timestamps[template.templateId] = new Date()
         }
         else
         {
            //println " ---- No se pudo cargar el template: " + f.path
         }
      }
      
      // Recursiva por directorios
      root.eachDir { d ->
         loadAllRecursive( d.path )
      }
   }
   
   /**
    * Ubica la path en el repo sabiendo su templateId,
    * buscando recursivamente por la estructura de directorios.
    */
   public String findTemplatePath(String templateId)
   {
      return findTemplatePathRecursive( templateId, ApplicationHolder.application.config.hce.template_repo )
   }
   
   private String findTemplatePathRecursive( String templateId, String path )
   {
      // FIXME: utilizar loops for
      //if (f.isFile()) println f.canonicalPath
      String fpath
      
      def root = new File( path )
      root.eachFile { f ->
      
         //println " > " + f.path
      
         if (templateId == (f.name - '.xml'))
         {
            println "encuentra $templateId en "+ f.path
            fpath = f.path
            // Aunque se ponga return, sigue con el each loop
         }
      }
      
      if (fpath) return fpath
      
      // Recursiva por directorios
      def dpath
      root.eachDir { d ->
         dpath = findTemplatePathRecursive( templateId, d.path )
         if (dpath) fpath = dpath // Aunque se ponga return, sigue con el each loop
      }
      
      if (fpath) return fpath
   }
}