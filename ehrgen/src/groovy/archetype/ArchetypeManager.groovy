/*
Copyright 2013 CaboLabs.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

This software was developed by Pablo Pazos at CaboLabs.com

This software uses the openEHR Java Ref Impl developed by Rong Chen
http://www.openehr.org/wiki/display/projects/Java+Project+Download

This software uses MySQL Connector for Java developed by Oracle
http://dev.mysql.com/downloads/connector/j/

This software uses PostgreSQL JDBC Connector developed by Posrgresql.org
http://jdbc.postgresql.org/

This software uses XStream library developed by Jörg Schaible
http://xstream.codehaus.org/
*/
/**
 * Clase SINGLETON para el manejo de arquetipos y medio de acceso al repositorio de arquetipos local.
 * Tiene un cache que carga los arquetipos presentes en el repostorio local a medida que estos son utilizados.
 */
package archetype

import org.openehr.am.archetype.Archetype
import org.openehr.rm.support.identification.ArchetypeID

// http://www.openehr.org/wiki/display/projects/Java+ADL+Parser+Guide
import se.acode.openehr.parser.*

import org.apache.log4j.Logger
import java.util.regex.Pattern

import org.openehr.am.archetype.constraintmodel.ArchetypeConstraint
import org.codehaus.groovy.grails.commons.ApplicationHolder

import archetype.ArchetypeIndex
import archetype.walkthrough.*
import archetype.walkthrough.actions.SlotResolution

/**
 * @author Pablo Pazos Gutierrez (pablo.pazos@cabolabs.com)
 * @version 1.0
 */
class ArchetypeManager {

   private Logger log = Logger.getLogger(getClass()) 
   
   // Ruta independiente del SO
   // http://code.google.com/p/open-ehr-gen-framework/issues/detail?id=54
   private static String PS = System.getProperty("file.separator")
   
   /**
    * Directorio donde estan los arquetipos.
    * FIXME: deberia ser un parametro de la aplicacion en un .properties
    */
   //private String archetypeRepositoryPath = "archetypes"+ PS +"ehr"
    
   // Cache: archetypeId => Archetype
   private static Map<String, Archetype> cache = [:]
    
   // archetypeId => timestamp de cuando fue usado por ultima vez.
   // Sirve para saber si un arquetipo no fue utilizado por mucho tiempo, y bajarlo del cache par optimizar espacio en memoria.
   private static Map<String, Date> timestamps = [:]
    
   // SINGLETON: FIXME usar singleton de groovy
   private static ArchetypeManager instance = null
    
   private ArchetypeManager() {}
    
   public static ArchetypeManager getInstance()
   {
      if (!instance) instance = new ArchetypeManager()
      return instance
   }
    
   /**
    * Carga todos los arquetipos presentes en el repositorio de arquetipos.
    * No importa si algunos arquetipos ya estan en el cache, carga todo y acutaliza timestamps.
    */
   public void loadAll()
   {
      //def path = ApplicationHolder.application.config.hce.archetype_repo
      loadAllRecursive( ApplicationHolder.application.config.hce.archetype_repo )
   }
    
   private loadAllRecursive( String path )
   {
      println "loadAllRecursive: " + path
      def root = new File( path )

      // FIXME: deberia filtrar solo archivos adl
      // eachFile tambien recorre subdirectorios!!! para eso se usa FILES
      root.eachFileMatch (groovy.io.FileType.FILES, ~/.*\.adl/) { f ->

         //println "LOAD: [" + f.name + "]"

         // PARSEAR ARQUETIPO
         ADLParser parser = null;
         try
         {
            parser = new ADLParser( f )
         }
         catch (IOException e)
         {
            println "PROBLEMA AL CREAR EL PARSER: " + e.message
         }
         
         Archetype archetype = null
         try
         {
            archetype = parser.archetype()
         }
         catch (Exception e)
         {
            println e.message
         }
         // /PARSEAR ARQUETIPO
             
         if (archetype)
         {
            log.debug("Cargado el arquetipo: " + f.name + " de " + root.path)
            cache[archetype.archetypeId.value] = archetype
            timestamps[archetype.archetypeId.value] = new Date()
         }
         else
         {
            log.error("No se pudo cargar el arquetipo: " + f.name + " de:\n\t " + root.path)
         }
      }
        
      // Recursiva por directorios
      root.eachDir { d ->
         loadAllRecursive( d.path )
      }
   }
   
   /**
    * Carga el arquetipo con id archetypeId
    * @param archetypeId
    * @return el arquetipo cargado o null si no lo encuentra
    */
   public Archetype getArchetype( String archetypeId )
   {
      // FIXME: PROBLEMAS DE CARGAR EL ARQUETIPO: openEHR-EHR-COMPOSITION.prescription.v1
       
      // Si no esta cargado, lo intenta cargar
      if (!this.cache[archetypeId])
      {
         //println "No se encuentra el arquetipo " + archetypeId + ", se intenta cargarlo"
         def id = new ArchetypeID( archetypeId ) // a partir del ID saco la ruta que tengo que cargar
         def type = id.rmEntity // cluster, entry, composition, etc...
           
         // FIXME: ojo que si es un subtipo la ruta no es directa (action esta en /ehr/entry/action no es /ehr/action!)
           
         // archetypes/ehr/type/archId.adl
         //println "Carga desde: " + ApplicationHolder.application.config.hce.archetype_repo+ PS +getTypePath(type)+ PS +archetypeId+".adl"
         //def adlFile = new File( ApplicationHolder.application.config.hce.archetype_repo+ PS +getTypePath(type)+ PS +archetypeId+".adl" )
         println "Carga desde: " + ApplicationHolder.application.config.hce.archetype_repo + getTypePath(type) + PS + archetypeId +".adl"
         def adlFile = new File( ApplicationHolder.application.config.hce.archetype_repo + getTypePath(type) + PS + archetypeId +".adl" )
           
         // PARSEAR ARQUETIPO
         ADLParser parser = null
         try { parser = new ADLParser( adlFile ) }
         catch (IOException e) { print e.message }
           
         Archetype archetype = null;
         try { archetype = parser.archetype() }
         catch (Exception e) { print e.message }
         // /PARSEAR ARQUETIPO
               
         if (archetype)
         {
            log.debug("Cargado el arquetipo: " + adlFile.name + " de " + adlFile.path)
            cache[archetype.archetypeId.value] = archetype
            timestamps[archetype.archetypeId.value] = new Date()
         }
         else
         {
            log.error("No se pudo cargar el arquetipo: " + adlFile.name + " de " + adlFile.path)
         }
      }
      else
      {
         //println "Carga $archetypeId desde cache"
         this.timestamps[archetypeId] = new Date() // actualizo timestamp
      }
       
      return this.cache[archetypeId]
   }
   
   public String getTypePath( String type )
   {
      type = type.toLowerCase()
      switch (type)
      {  // FIXME: cluster y element deberian estar en /item/cluster o /item/element
         case 'cluster':
         case 'composition':
         case 'element':
         case 'section':
         case 'structure':
            return type
         break
         case 'item_tree':
         case 'item_single':
         case 'item_list':
         case 'item_table':
            return 'structure'
         break
         case 'action':
         case 'evaluation':
         case 'instruction':
         case 'observation':
         case 'admin_entry':
            return 'entry'+ PS + type
         break
         default:
            throw new Exception('Tipo no conocido ['+ type +'], se espera uno de: cluster, composition, element, section, item_tree, item_single, item_list, item_table, action, observation, instruction, evaluation, admin_entry' )
      }
   }
   
   /**
    * Obtiene un arquetipo por si tipo y expresion regular.
    * FIXME: type puede venir en mayusculas o en minusculas, y lo necesito en minusculas.
    * FIXME: el resultado de esta operacion deberia ser una lista de arquetipos!
    */
   public Archetype getArchetype( String type, String idMatchingKey )
   {
      println "=== getArchetype( "+type+", "+idMatchingKey+" ) ==="
      //type = type.toLowerCase()
       
      // FIXME: no uso el type porque para guardar los arquetipos no lo uso,
      //        seria una optimizacion para buscar.
      Archetype archetype = null
      def p = Pattern.compile( ".*"+idMatchingKey+".*\\.adl" ) // agrego .adl porque si hay .adls de ADL1.5 en el dir, intenta cargarlo.
       
      // Busca en los arquetipos cargados:
      def iter = this.cache.keySet().iterator()
      def archetypeId
      while( iter.hasNext() )
      {
         archetypeId = iter.next()
         if ( p.matcher(archetypeId).matches() )
         {
            println "   Encuentra arquetipo en cache: " + archetypeId
            this.timestamps[archetypeId] = new Date()
            return this.cache[archetypeId]
         }
         //else println "NO ES"
      }
   
      // TODO: Si no esta, tendria que ir a cargarlo... ahi uso type.
      println "   No se encuentra el arquetipo que corresponda con " + idMatchingKey + ", se intenta cargarlo"
       
      // FIXME: ojo que si es un subtipo la ruta no es directa (action esta en /ehr/entry/action no es /ehr/action!)
       
      // archetypes/ehr/type/archId.adl
      // Abre el directorio donde supuestamente esta el arquetipo
      def root = new File( ApplicationHolder.application.config.hce.archetype_repo + getTypePath(type) )
       
       
      // FIXME: varios pueden matchear!
      def adlFile = null
      root.eachFile { f ->
       
         if ( p.matcher(f.name).matches() )
         {
            adlFile = f
         }
      }
       
      if (!adlFile) println "   ERROR: No se encuentra el archivo que matchee con " + "[.*"+type+".*"+idMatchingKey+".*]" + " desde " + root.path
      else
      {
         println "   Carga desde: " + adlFile.path

         // PARSEAR ARQUETIPO
         ADLParser parser = null;
         try { parser = new ADLParser( adlFile ) }
         catch (IOException e) { print e.message }
         
         try { archetype = parser.archetype() }
         catch (Exception e) { print e.message }
         // /PARSEAR ARQUETIPO
               
         if (archetype)
         {
            println "   Cargado el arquetipo: " + adlFile.name + " de " + adlFile.path
            cache[archetype.archetypeId.value] = archetype
            timestamps[archetype.archetypeId.value] = new Date()
         }
         else
         {
            println "   ERROR: No se pudo cargar el arquetipo: " + adlFile.name + " de " + adlFile.path
         }
      }
       
      return archetype
   }
   
   // FIXME: deberia buscar primero en el cache, luego en disco?
   // Correccion para devolver muchos
   public List<Archetype> getArchetypes( String type, String idMatchingKey )
   {
       //println "=== getArchetypes( "+type+", "+idMatchingKey+" ) ==="
       //type = type.toLowerCase()
       
       def archetypes = []
       
       // FIXME: no uso el type porque para guardar los arquetipos no lo uso,
       //        seria una optimizacion para buscar.
       //Archetype archetype = null
       
       // Puede ser tan complicada como:
       // openEHR-EHR-ITEM_TREE\.medication\.v1|openEHR-EHR-ITEM_TREE\.medication-formulation\.v1|openEHR-EHR-ITEM_TREE\.medication-vaccine\.v1
       def p = Pattern.compile( ".*"+idMatchingKey+".*\\.adl" ) // agrego .adl porque si hay .adls de ADL1.5 en el dir, intenta cargarlo.
       
       /* Saco busqueda en cache porque cuando son varios es mas compleja:
        *  1. buscar en cache y ver todos los que matchean
        *  2. ver los archivos que matchean la regex pero que no esten ya en lo que se cargo en el cache, y cargarlos desde los archivos
        *  3. tal vez esta deberia solo buscar en cache y no cargar de disco
       // Busca en los arquetipos cargados:
       def iter = this.cache.keySet().iterator()
       def archetypeId
       while( iter.hasNext() )
       {
           archetypeId = iter.next()
           if ( p.matcher(archetypeId).matches() )
           {
               println "   Encuentra arquetipo en cache: " + archetypeId
               this.timestamps[archetypeId] = new Date()
               return this.cache[archetypeId]
           }
           //else println "NO ES"
       }
       
       */
   
       // TODO: Si no esta, tendria que ir a cargarlo... ahi uso type.
       //println "   No se encuentra el arquetipo que corresponda con " + idMatchingKey + ", se intenta cargarlo"
       
       // FIXME: ojo que si es un subtipo la ruta no es directa (action esta en /ehr/entry/action no es /ehr/action!)
       
       // archetypes/ehr/type/archId.adl
       def root = new File( ApplicationHolder.application.config.hce.archetype_repo + PS + getTypePath(type) ) // Abre el directorio donde supuestamente esta el arquetipo
       
       // FIXME: varios pueden matchear!
       def adlFiles = []
       //root.eachFile { f ->
       root.eachFileMatch(p) { f ->
          //println f.name
          //println f.name - '.adl' archetypes\adl_source\structure\openEHR-EHR-ITEM_TREE.medication-formulation.v1.adl - .adl
          //if ( p.matcher(f.name - '.adl').matches() )
          //{
             adlFiles << f
          //}
       }
       
       //println "adlFiles: "+ adlFiles
       
       if (adlFiles.size() == 0) println "   ERROR: No se encuentra el archivo que matchee con " + "[.*"+idMatchingKey+".*]" + " desde " + root.path
       else
       {
          def archetype = null
          adlFiles.each { adlFile ->
              
              //println "   Carga desde: " + adlFile.path
   
              // PARSEAR ARQUETIPO
              ADLParser parser = null;
              try { parser = new ADLParser( adlFile ) }
              catch (IOException e) { print e.message }
              
              //Archetype archetype = null;
              try { archetype = parser.archetype() }
              catch (Exception e) { print e.message }
              // /PARSEAR ARQUETIPO
                  
              if (archetype)
              {
                 //println "   Cargado el arquetipo: " + adlFile.name + " de " + adlFile.path
                 
                 // Saco el cacheo
                 //cache[archetype.archetypeId.value] = archetype
                 //timestamps[archetype.archetypeId.value] = new Date()
                 
                 archetypes << archetype
              }
              else
              {
                 //println "   ERROR: No se pudo cargar el arquetipo: " + adlFile.name + " de " + adlFile.path
              }
          }
       }
       
       return archetypes
   }
   
   
   /**
    * Carga indices para el archetypeId, o si es null carga
    * para todos los arquetipos en el repo local.
    */
   public boolean createArchetypeIndexes(String rootArchId = null)
   {
      def index
      def slot_index
      def walk
      def result
      def archetypes
      
      // Retorno true sino hubo error
      boolean ok = true
      boolean loopOk = true // para detectar errores en cada loop, se reinicia en cada vuelta
      
      if (!rootArchId) archetypes = this.getLoadedArchetypes() // Map archetypeId -> archetype
      else archetypes = [(rootArchId): this.getArchetype(rootArchId)]
      
      archetypes.each { archetypeId, archetype ->

         //println " createIndexes archetypeId: "+ archetypeId
         
         // http://code.google.com/p/open-ehr-gen-framework/issues/detail?id=100
         // Garantiza que hay un index por arquetipo
         index = ArchetypeIndex.findByArchetypeId(archetypeId)
      
         if (!index)
         {
            index = new ArchetypeIndex(
               archetypeId: archetypeId,
               type: archetype.archetypeId.rmEntity.toLowerCase()
            )
         }
         
         //println " createIndexes: type "+ archetype.archetypeId.rmEntity.toLowerCase()
         
         
         // Busca slots usando el archetype walkthrough
         walk = new ArchetypeWalkthrough()
         walk.walthroughInit(archetype)
         walk.walthroughStart(
            [
               (walk.EVENT_SLOT): [new SlotResolution()]
            ],
            new WalkthroughResult())

         result = walk.walthroughResult()

         //println result as grails.converters.XML
         //println result.loadedArchetypes
         //println result.references [archId::path : [archId,...]] // Slots en el arquetipo raiz con los ids referenciados en cada path
         //println result.cache
         
         result.loadedArchetypes.each { archId, arch ->
         
            if (archId != archetypeId)
            {
               // http://code.google.com/p/open-ehr-gen-framework/issues/detail?id=100
               // Garantiza que hay un index por arquetipo
               slot_index = ArchetypeIndex.findByArchetypeId(archId)
               
               if(!slot_index)
               {
                  slot_index = new ArchetypeIndex(
                     archetypeId: archId,
                     type: arch.archetypeId.rmEntity.toLowerCase()
                  )
                  
                  //println " createIndexes: slot type "+ arch.archetypeId.rmEntity.toLowerCase()
               }
               
               // Verifica que el slot_index sea de un tipo soportado (entry o section)
               // Ver ArchetypeIndex.constraints.type.inList
               if (slot_index.validate())
               {
                  index.addToSlots( slot_index )
               }
               else
               {
                  loopOk = false
                  println "No se pudo agregar el slot a $archId por no ser SECTION o ENTRY, es "+ slot_index.type
               }
            }
         }
         
         // Guardo solo si valida, ej. no guarda indices de tipos structure o item,
         // y si no dio un error previamente por tener un slot a un arquetipo de tipo no soportado.
         if (index.validate() && loopOk)
         {
            index.save()
         }
         else
         {
            //println " errs: "+ index.errors
            loopOk = false
            println "No se puede crear el indice para el arquetipo " + index.archetypeId + " porque el tipo " +
                    index.type + " no es soportado (solo se soportan SECTION o ENTRY) o porque contiene algun slot a un tipo no soportado "+
                    index.errors.getFieldValue("type")
            
            /*
            println index.errors
            Field error in object 'archetype.ArchetypeIndex' on field 'type': rejected value [item_tree]; codes [archetype.ArchetypeIndex.type
            .inList.error.archetype.ArchetypeIndex.type,archetype.ArchetypeIndex.type.inList.error.type,archetype.ArchetypeIndex.type.inList.e
            rror.java.lang.String,archetype.ArchetypeIndex.type.inList.error,archetypeIndex.type.inList.error.archetype.ArchetypeIndex.type,ar
            chetypeIndex.type.inList.error.type,archetypeIndex.type.inList.error.java.lang.String,archetypeIndex.type.inList.error,archetype.A
            rchetypeIndex.type.not.inList.archetype.ArchetypeIndex.type,archetype.ArchetypeIndex.type.not.inList.type,archetype.ArchetypeIndex
            .type.not.inList.java.lang.String,archetype.ArchetypeIndex.type.not.inList,archetypeIndex.type.not.inList.archetype.ArchetypeIndex
            .type,archetypeIndex.type.not.inList.type,archetypeIndex.type.not.inList.java.lang.String,archetypeIndex.type.not.inList,not.inLis
            t.archetype.ArchetypeIndex.type,not.inList.type,not.inList.java.lang.String,not.inList]; arguments [type,class archetype.Archetype
            Index,item_tree,[section, action, evaluation, instruction, observation, admin_entry]]; default message [La propiedad [{0}] de la c
            lase [{1}] con valor [{2}] no esta contenido dentro de la lista [{3}]]
            */
         }
         
         if (!loopOk) ok = false
         
         loopOk = true // resetea el ok del loop
      }
      
      return ok
      
   } // createArchetypeIndexes
   
   /**
    * Da de baja el indice del arquteipo archetypeId o si archetypeId es null, de todos los arquetipos.
    * Para cada indice, da de baja los indices de slot referenciados.
    * @return
    */
   public void deleteArchetypeIndexes(String archetypeId = null)
   {
      // Elimina el indice para un arquetipo y los slots referenciados
      // sino estan referenciados desde otro index.
      if (archetypeId)
      {
         def index = ArchetypeIndex.findByArchetypeId(archetypeId)
         
         // Se fija si los slots del index son referenciados desde otro index
         // FIXME: esto se puede hacer con delete-orphan en el mapping ... PROBAR!
         index.slots.each { slot ->
            
            // si hay algun index que apunte al slot, no lo elimino
            def count = ArchetypeIndex.withCriteria {
               projections {
                  count('id')
               }
               slots {
                  idEq(slot.id)
               }
            }
            if (count == 0)
            {
               slot.delete()
            }
         }
         
         index.delete()
         
         return
      }

      
      // Elimina todos los indices, por ejemplo para reindizar.
      def indexes = ArchetypeIndex.list()
      
      indexes.each { index ->
         
         index.delete()
      }
   }
   
   /**
    * La path incluye el id del arquetipo:
    * @param fullPath openEHR-EHR-OBSERVATION.diagnosticos.v1/data[at0001]/events[at0002]/data[at0003]/items[at0004]/value/defining_code
    * @return
    */
   public ArchetypeConstraint getArchetypeNode(String fullPath)
   {
      int i = fullPath.indexOf('/')
      String archetypeId = fullPath.substring(0, i)
      String path = fullPath.substring(i)
      
      Archetype a = this.getArchetype(archetypeId)
      return a?.node(path)
   }
   
   public String toString()
   {
      return this.cache.keySet().toString()
   }
   
   public Map getLoadedArchetypes()
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
   
   public void unload(String archetypeId)
   {
      this.cache.remove(archetypeId)
      this.timestamps.remove(archetypeId)
   }
   
   /**
    * Busca ids de arquetipos que contengan el substring.
    * @param substring
    * @return
    */
   public List<String> findArchetypeIds(String substring)
   {
      //println "=== fingArchetypeIds( "+substring+" ) ==="
      
      def ret = []

      // Transformo el substring en una regex
      def pattern = Pattern.compile( ".*"+substring+".*", Pattern.CASE_INSENSITIVE )
      def dir = new File( ApplicationHolder.application.config.hce.archetype_repo )
 
      fingArchetypeIdsRecursive(pattern, dir, ret)
      
      return ret
   }
    
   /**
    * 
    * @param pattern
    * @param path
    * @param ret in/out
    * @return
    */
   private fingArchetypeIdsRecursive(Pattern pattern, File currentDir, List ret)
   {
      //println "fingArchetypeIdsRecursive: " + currentDir.path
      
      //dir.eachFileMatch(~/foo\d\.txt/) { f ->
      // FILES para que no ponga directorios
      currentDir.eachFileMatch(groovy.io.FileType.FILES, pattern) { f ->
      
         ret << f.name - ".adl"
      }
      
      // Recursiva por directorios
      currentDir.eachDir { d ->
         fingArchetypeIdsRecursive( pattern, d, ret )
      }
   }
}