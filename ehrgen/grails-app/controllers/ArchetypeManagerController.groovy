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

This software uses XStream library developed by J√∂rg Schaible
http://xstream.codehaus.org/
*/
import org.openehr.am.archetype.Archetype
import support.identification.TerminologyID
import archetype.ArchetypeManager
import binding.CtrlTerminologia
import org.codehaus.groovy.grails.commons.ApplicationHolder
import grails.converters.JSON
import se.acode.openehr.parser.ADLParser
import archetype.ArchetypeIndex
import demographic.role.Role

class ArchetypeManagerController {

   def manager = archetype.ArchetypeManager.getInstance()

   /**
    * Lista arquetipos cargados en el cache con su ultima fecha de uso.
    */
   def list = {
      
      def archetypes = manager.getLoadedArchetypes()
      def actualizaciones = manager.getLastUse()

      return [archetypeMap: archetypes, lastUseList: actualizaciones]
   }
   
   /**
    * Carga todos los arquetipos en memoria desde el repo local.
    */
   def loadAll = {
      // TODO: crear archetypeIndex sino existe en la db
      manager.loadAll()
      redirect(action:'list')
   }
   
   /**
    * Vacia el cache de arquetipos en memoria.
    */
   def unloadAll = {

      manager.unloadAll()
      redirect(action:'list')
   }
   
   /**
    * Sube un archivo ADL al repositorio local en el file system.
    */
   def uploadArchetype = {
   
      if (params.doit)
      {
         // TODO: verificar permisos de escritura en el directorio destino
         // TODO: verificar que no haya un arquetipo con el mismo id en el repo,
         //       si lo hay preguntar si lo quiere sobre escribir (esto implica
         //       que no deberian haber registros para el arquetipo anterior,
         //       lo puedo dejar en DEV pero no deberia dejarlo en PROD, ahi deberia versionar)
         // TODO: verificar que el ADL se parsea ok
         // TODO: mover el archivo del repo temporal al repo de arquetipos de la aplicacion
         // TODO: log de la subida y resultado del procesamiento (me sirve para listar, ver que errores se dieron y corregirlos)
      
         // =====================================================================================
         // 1. Intenta guardar el archivo subido en el directorio temporal
         
         // CommonsMultipartFile
         def upAdlFile = request.getFile('adl')
         
         //println upAdlFile.getContentType()       // application/octet-stream
         //println upAdlFile.getName()              // adl (nombre del campo del form)
         //println upAdlFile.getOriginalFilename()  // openEHR-EHR-....adl
         //println upAdlFile.getSize()              // 1234
         
         if (upAdlFile.isEmpty())
         {
            flash.message = "El contenido del archivo es vacio, intente subirlo de nuevo"
            return
         }
         
         def tmp_destination = ApplicationHolder.application.config.hce.uploaded_archetypes_repo
         
         //def sctx = org.codehaus.groovy.grails.web.context.ServletContextHolder.servletContext
         //def storagePath = sctx.getRealPath( tmp_destination )
         //
         // el getRealPath me da adentro de web-app ... pruebo usar solo el uploaded_archetypes_repo
         // (en dev funciona pero en prod no se...)
         
         def storagePath = tmp_destination
         def storagePathDir = new File(storagePath)
         if (!storagePathDir.exists())
         {
            throw new Exception("El directorio $storagePath no existe")
         }
         
         def storageFilePath = storagePath + upAdlFile.getOriginalFilename()
         def adlFile = new File(storageFilePath)
         upAdlFile.transferTo( adlFile )
         
         
         // =====================================================================================
         // 2. Intenta parsear el ADL desde el directorio temporal
         
         ADLParser parser = null;
         try { parser = new ADLParser( adlFile ) }
         catch (IOException e)
         {
            flash.message = "No se pudo abrir el archivo ADL "+ e.message
            println e.message
            return
         }
           
         Archetype archetype = null
         try { archetype = parser.archetype() }
         catch (Exception e)
         {
            flash.message = "El ADL no es valido "+ e.message
            println e.message
            return
         }
         
         if (!archetype)
         {
            flash.message = "No se pudo abrir el archivo ADL"
            println e.message
            return
         }
         
         
         // =====================================================================================
         // 3. Mueve el adl desde el directorio temporal al repo local de arquetipos
         
         def type = archetype.archetypeId.rmEntity.toLowerCase()
         def path = manager.getTypePath( type ) // path destino definitivo del arquetipo (repo local)
         def repo_path = ApplicationHolder.application.config.hce.archetype_repo + path + System.getProperty("file.separator")
         
         //println "repo_path "+ repo_path + adlFile.getName()
         
         def adlFileInRepo = new File(repo_path + adlFile.getName())
         
         
         // TODO: dar la opcion de sobre escribir si existe y avisarle si se sobreescribiÛ alg˙n arquetipo en el flash.message
         // Para que la sobreescritura funcione se deben regenerar las UIs para los templates que referencian al arquetipo
         // En DEV esto no serÌa problema para probar cosas, pero en PROD NO SE PUEDE generar una GUI para un arquetipo distinto pero con el mismo id porque no se puede garantizar la compatibilidad de los datos ya registrados, entonces SIEMPRE se deben cargar nuevas versiones de los arquetipos.
         // La otra forma es que la responsabilidad sea del adminsitrador, y que este borre el arquetipo existente antes de cargar otro con el mismo ID y Èl mismo lance el proceso de generaciÛn de GUI.
         if (adlFileInRepo.exists())
         {
            flash.message = "Ya existe el arquetipo en el repositorio, intente cargar el nuevo arquetipo con un numero de version distinto al existente: "+ archetype.archetypeId.value
            return
         }

         if (!adlFile.renameTo( adlFileInRepo )) // Mueve el archivo
         {
            flash.message = "No se pudo guardar el archivo ADL en el repositorio local, verifique que tiene permisos de escritura"
            
            // Intenta borrar el archivo del dir temporal porque
            // no se pudo hacer el move.
            adlFile.delete()
            
            return
         }
         
         
         // =====================================================================================
         // 4. Cachea el nuevo arquetipo en memoria del manager (ya cachea el createArchetypeIndexes)
         //manager.getArchetype( archetype.archetypeId.value )
         
         
         // Si alguna de las tareas de abajo falla, se debe borrar el archivo del repo
         // para permitirle al usuario subirlo de nuevo, sino se queda en un loop por
         // no poder subir el mismo adl o lo debe eliminar a mano, lo que lleva mas tiempo.
         
         
         // =====================================================================================
         // 5. Crea indices de las partes del arquetipo y los guarda en la DB
         if (!manager.createArchetypeIndexes( archetype.archetypeId.value ))
         {
            flash.message = "Ocurrio un error al crear los indices para el arquetipo, verifique que el mismo es de clase SECTION o ENTRY y que no contienen slots a arquetipos de otras clases como ITEM_STRUCTURE o CLUSTER"
            
            println adlFileInRepo.canonicalPath + ".delete()"
            
            // Intenta borrar el archivo adl del repo para dejar cargarlo de nuevo
            adlFileInRepo.delete()
            
            // Quita del cache la referencia al arquetipo que se creÛ en
            // createArchetypeIndexes para el arquetipo que no se pudo cargar. 
            manager.unload(archetype.archetypeId.value)
            
            return
         }
         
         
         // ============================================================================
         // https://code.google.com/p/open-ehr-gen-framework/issues/detail?id=106
         // TEST: ver si viene un arquetipo de instruction o que contenga una instruction
         def index = ArchetypeIndex.findByArchetypeId(archetype.archetypeId.value)
         def hayInstruction = false
         if (index.type == 'instruction')
         {
            hayInstruction = true
         }
         else if ( index.slots.find{ it.type == 'instruction' } != null )
         {
            hayInstruction = true
         }
         
         
         // Deberia mostrar una segunda pantalla para seleccionar los roles que veran las instrucciones creadas con este arquetipo.
         // Los roles los guardo mismo en el archetype index, igual como se guardan los roles en el workflow.
         if (hayInstruction)
         {
            flash.message = "Archivo adl guardado en $repo_path"
            redirect(action:'selectInstructionRoles', id:index.id)
            return
         }
         // ============================================================================
         
         
         flash.message = "Archivo adl guardado en $repo_path"
         return
      }
   } // uploadArchetype
   
   
   /**
    * Selecciona que roles pueden ver las instrucciones creadas con un arquetipo de instruction.
    * Viene de uploadArchetype cuando se sube un arquetipo que contenga una instruction.
    *
    * @param id identificador del ArchetypeIndex de la instruction
    */
   def selectInstructionRoles = {
   
      if (!params.id)
      {
         println "id de index es obligatorio"
      }
      
      def index = ArchetypeIndex.get(params.id)
      
      if (params.doit)
      {
         // Con el toList parece que no da concurrent modification en la coleccion
         // y hace bien el remove de todos los elementos.
         index.instructionRoles.toList().each { role ->
            
            index.removeFromInstructionRoles(role)
         }
         
         // Agrega roles seleccionados
         def roleIds = params.list('roleId')
         roleIds.each { id ->

            index.addToInstructionRoles( Role.get(id) )
         }
         
         if (!index.save())
         {
            println index.errors
         }
         
         render "Roles establecidos para la instrucction " + index.archetypeId
         return
      }
      
      return [index: index, roles:Role.list()]
   }
   
   /**
     * Consultas semanticas:
     * 
     * 1. Ingreso:
     *  - http://localhost:8080/ehr/archetypeManager/query
     * 
     * 2. Selecciona concepto:
     *  - http://localhost:8080/ehr/archetypeManager/query?archetypeId=openEHR-EHR-EVALUATION.triage_trauma.v2&conceptName=Triage+de+trauma
     * 
     * 3. Selecciona paths: (POST)
     *  - params:
           [ paths: [/data[at0001]/items[at0002], /data[at0001]/items[at0008]],
             fromDate:, archetypeId:openEHR-EHR-EVALUATION.triage_trauma.v2,
             doit:Seleccionar paths,
             conceptName:Triage de trauma,
             _paths:[, , , , ],
             action:query,
             controller:archetypeManager ]
     *
     * 4. Agregacion para los tipos soportados (DvOrdinal, CodePhrase) (estos definen listas de valores por eso es facil agrupar)
     *  - Para agrupar fechas y numeros (DvCount) es necesario definir rangos (hay que dejar que el usuario los defina)
     *  - Para DvQuantity tambien hay que definir rangos para la magnitud, pero hay que asegurar que la unidad es la misma.
     *  - Para agrupar texto libre se podrian definir terminos a buscar en el texto, y si esta en el texto se cuenta 1 (hay que dejar que el usuario ponga la lista de valores a buscar)
     *  - TODO: los boolean son faciles de agrupar tambien como DvOrdinal y CodePhrase.
     *  - Para DvCodedText (CodePhrase) donde la restriccion no es list sino que es un bind con una terminologia se
     *    puede hacer una agregacion igual, pero los valores posibles son muchos mas, lo optimo seria agrupar por
     *    los valores en la db y no por los valores en la terminologia (voy a tener muchos ceros). Para saber los
     *    valores posibles es hacer una query a la db con distinct, incluso con eso ya puedo hacer el count agrupando (group by).
     *  - otros: DvIdentifier, DvURI, DvProportion
     */
    
    /**
     * Test de la idea de buscar datos en el registro del paciente, basado en arquetipos y paths.
     */
    def query = {
       
       println params
       
       /**
        * Ideas:
        * - Pueden haber distintas versiones del mismo arquetipo, dar 2 opciones:
        *   - seleccionar el concepto, para cualquier version
        *   - seleccionar el concepto y una version particular de arquetipo 
        */
       
       // Map id arquetipo -> nombre concepto
       def clinicalConcepts = [:]
       
       // Arquetipo seleccionado desde la lista de conceptos
       def archetype
       
       // Lista de elementos del RM al filtrar por arquetipo y path
       def data
       
       // parentCompositionId -> [Pathable]
       def dataByComposition = [:]
       
       // Arquetipos
       def archs = archetype.ArchetypeManager.getInstance().getLoadedArchetypes()
       
       // Lista de paths seleccionadas dentro de un arqutetipo
       List paths
       
       
       // El primer paso es cuando entra y se le muestran los conceptos, ahi debera seleccionar uno
       if (!params.archetypeId)
       {
          // Nombre de concepto de cada arquetipo en la KB (id at0000)
          def terms = binding.CtrlTerminologia.getInstance()
          def terminologyId = TerminologyID.create('local', null) // Para sacar el nombre del concepto de la ontologia del arquetipo
          
          // archetypeId => archetype
          archs.each { entry ->
             
             archetype = entry.value
             
             def conceptName = terms.getTermino(terminologyId, 'at0000', archetype, session.locale) // at0000 es el id de la raiz del arquetipo
             
             // archetypeId => conceptName
             clinicalConcepts[entry.key] = conceptName
          }
          
          archetype = null // Se reutiliza esta variable aca para la recorrida, la tengo que borrar para no generar problemas luego.
       }
       else
       {
          /**
          *  Si ya selecciono el concepto, le muestro las paths en el arquetipo (las paths van a
          *  depender de la version del arquetipo! (ahora considero que se consulta por arquetipos
          *  separados, no por concepto clinico)
          */
         
          archetype = archs[params.archetypeId]
         
          // Para mostrar el concepto seleccionado
          clinicalConcepts[params.archetypeId] = params.conceptName
          
          /**
           * Idea: poder seleccionar mas de una path
           */
          if (params.paths) // Puede ser un string o un array
          {
             // Selecciono una path de un arquetipo, busco los datos con ese filtro
             
             // TODO: deberia filtrarse para el paciente actual, si es que hay uno seleccionado,
             // sino no tienen mucho sentido las consultas porque se necesitaria una funcion de agrecacion.
             
             // TODO: Tambien filtrar por fecha del registro contenedor inicio/fin
             //       en lugar de tener estos datos en todas las clases del RM para filtrar facil,
             //       podria tener una relacion fisica con el contenedor ppal, la composition.
             //       Y usar estas queries de hibernate que navegan relaciones para hacer el filtro
             //       sobre los valores de fecha de la composition. Si necesitaria que el valor en
             //       la composition sea plano, no un datatype, asi la consulta por fecha es simple.
             
             //println params
             
             // TODO: data deberia ser MAP<parentCompositionId, List<Pathable>>
             // asi por cada composition tengo todos los elementos de esa composition seleccionados por su path para ser mostrados
             // la composition le da marco temporal a esos datos, asi que es mas facils si quiero filtrar por fechas
             // el problema es que sin tener la relacion fisica entre Pathable y Composition, no puedo meter el filtro
             // por fecha en la query, y si pongo esa relacion me da un error de backref al salvar la section porque
             // la compo tiene un hasmany a section y section tien el parendCompositionId por estar declarado en pathable,
             // el problema que es si composition tambien es pathable, el parentComposition en pathable modela un posible
             // link entre composition y composition tambien, no se si eso puede causar problemas.
             
             
             // Haciendo que siempre reciba una lista aunque haya una sola path:
             // http://grails.org/doc/latest/guide/single.html#6.1.12%20Simple%20Type%20Converters
             paths = params.list('paths')
             
//             println paths
             
//             if (params.paths.getClass().isArray())
//             {
                data = []
                def data1
//                params.paths.each{ pth ->
                paths.each{ pth ->
                   
                   // FIXME: cuando no se use mas datatypes en XML, aqui no se usar√° m√°s el XStream
                   // Necesario para comparar el dato codificado como XML en la base
                   /*
                   def startTimeFilter = new data_types.quantity.date_time.DvDateTime(value: ((params.fromDate)?params.fromDate:'1900-01-01 00:00:00') )
                   com.thoughtworks.xstream.XStream xstream = new com.thoughtworks.xstream.XStream()
                   xstream.omitField(data_types.basic.DataValue.class, "errors");
                   def codedStartTimeFilter = xstream.toXML(startTimeFilter)
                   */
                   
                   // Si viene la fecha, se usa el mismo formato que se utilizo en la vista para parsear el string
                   def startTimeFilter = ((params.fromDate) ? Date.parse(ApplicationHolder.application.config.app.l10n.date_format, params.fromDate) : new Date(0, 0, 1)) // 1900/01/01
      
                   
                   //println pth
                   //println codedStartTimeFilter
                   
                   //data1 = hce.core.common.archetyped.Locatable.findAll( "FROM Locatable p WHERE p.archetypeDetails.archetypeId = ? AND p.path = ?", [params.archetypeId, pth, codedStartTimeFilter] )
                   
                   // El filtro funciona ok, el problema es que el restulado es una lista de pares ELEMENT/COMPOSITION,
                   // porque hace el producto cartesiando entre los Locatables y la Composition, y en realidad la composition la quiero solo para filtrar,
                   // no para el restulado final, talvez tengo que hacer una consulta anidada para composition.
                   //data1 = hce.core.common.archetyped.Locatable.findAll( "FROM Locatable p, Composition c WHERE p.archetypeDetails.archetypeId = ? AND p.path = ? AND c.context.codedStartTime < ?", [params.archetypeId, pth, codedStartTimeFilter] )
                   
                   // OK! usando nested query: http://www.felixgers.de/teaching/sql/sql_nested_queries.html
                   // Falta que pueda ingresar las fechas como filtro en la gui.
                   //data1 = hce.core.common.archetyped.Locatable.findAll( "FROM Locatable p WHERE p.archetypeDetails.archetypeId = ? AND p.path = ? AND EXISTS( SELECT c.id FROM Composition c WHERE c.id = p.parentCompositionId AND c.context.codedStartTime > ?)",
                   //                                                      [params.archetypeId, pth, codedStartTimeFilter] )
                   data1 = hce.core.common.archetyped.Locatable.findAll( "FROM Locatable p WHERE p.archetypeDetails.archetypeId = ? AND p.path = ? AND EXISTS( SELECT c.id FROM Composition c WHERE c.id = p.parentCompositionId AND c.startTime > ?)",
                                                                         [params.archetypeId, pth, startTimeFilter] )
                   
                   
                   /* Hasta cierto punto la consulta podria hacerse con withCriteria, no se como hacer la consulta anidada con el EXISTS para filtrar por fecha de la composition!
                   def test = hce.core.common.archetyped.Locatable.withCriteria {
                      
                      archetypeDetails {
                         eq('archetypeId', params.archetypeId)
                      }
                      eq('path', pth)
                   }
                   */
                   
                   //println data1
                   
                   // Aplano cada consulta parcial en la solucion final
                   // TODO: asociar los datos que son para las mismas parentCompositons
                   
                   data1.each {
                      
                      data << it
                      
                      
                      // TODO: faltaria el filtro por paciente, para asociar las distintas compositions
                      // Asocia cada instancia de Pathable a la composition a la que pertenece,
                      // asi si tengo varias paths, puedo mostrar asociados los objetos de la misma composition.
                      if (!dataByComposition[it.parentCompositionId]) dataByComposition[it.parentCompositionId] = []
                      dataByComposition[it.parentCompositionId] << it
                   }
                }
                
                //println "data: " + data // Elements
                //println "dataByComposition: " + dataByComposition // Composition -> Elements
                
//             }
//             else
//             {
//                // Necesario para comparar el dato codificado como XML en la base
//                def startTimeFilter = new data_types.quantity.date_time.DvDateTime(value: ((params.fromDate)?params.fromDate:'1900-01-01 00:00:00') )
//                com.thoughtworks.xstream.XStream xstream = new com.thoughtworks.xstream.XStream()
//                xstream.omitField(data_types.basic.DataValue.class, "errors");
//                def codedStartTimeFilter = xstream.toXML(startTimeFilter)
//                
//                // Esta consulta se podria hacer igual con withCriteria
//                data = hce.core.common.archetyped.Locatable.findAll( "FROM Locatable p WHERE p.archetypeDetails.archetypeId = ? AND p.path = ? AND EXISTS( SELECT c.id FROM Composition c WHERE c.id = p.parentCompositionId AND c.context.codedStartTime > ?)", [params.archetypeId, params.paths, codedStartTimeFilter] )
//                
//                data.each {
//                  if (!dataByComposition[it.parentCompositionId]) dataByComposition[it.parentCompositionId] = []
//                  dataByComposition[it.parentCompositionId] << it
//                }
//             }
             
             //println "dataByComposition:"
             //println dataByComposition
             
          }
       }
       
       return [clinicalConcepts: clinicalConcepts, archetype: archetype, paths: paths, data: data, dataByComposition: dataByComposition]
       
    } // query
    
    /**
     * 
     * @param data lista de elements con value DvQuantity
     * @param agg tipo de agrgacion a realizar
     * @param aggregator map de salida con el restulado
     * @param names nombres correspondientes a cada agregacion en aggregator
     */
    private void aggregateDvQuantity(List data, String agg,
                                     Archetype archetype, Object constraint, CtrlTerminologia terms,
                                     Map aggregator, Map names)
    {
	   // Promedio
       if (data.size() > 0)
       {
          // Hay un solo agregador de promedio (los agregadores de los demas tipos
          // lo que hacen es contar ocurrencias de un valor, eso para DvQuantity no
          // tiene mucho sentido).
          aggregator['promedio'] = 0
        
          // Calculo de agregacion AVG en magnitude (sin considerar units distintas!!!)
          // data es List<Element>
          data.each {
          
             //println it.value.magnitude
             aggregator['promedio'] += it.value.magnitude
          }
        
          aggregator['promedio'] = aggregator['promedio'] / data.size()
        
          // labels (hay solo una porque es un promedio)
          names['promedio'] = 'promedio' // TODO: I18N
       }
    }
    
	
	/**
	 * Agregacion del tipo basico dentro de DvCount.
	 */
	private void aggregateInteger(List data, String agg,
                                     Archetype archetype, Object constraint, CtrlTerminologia terms,
                                     Map aggregator, Map names)
    {
	   // Promedio
       if (data.size() > 0)
       {
          // Hay un solo agregador de promedio (los agregadores de los demas tipos
          // lo que hacen es contar ocurrencias de un valor, eso para DvQuantity no
          // tiene mucho sentido).
          aggregator['promedio'] = 0
        
          // Calculo de agregacion AVG en magnitude (sin considerar units distintas!!!)
          // data es List<Element>
          data.each {
          
             //println it.value.magnitude // it.value ~ DvCount, it.value.magnitude ~ Integer
             aggregator['promedio'] += it.value.magnitude
          }
        
          aggregator['promedio'] = aggregator['promedio'] / data.size()
        
          // labels (hay solo una porque es un promedio)
          names['promedio'] = 'promedio' // TODO: I18N
       }
	}
	
	
    private void aggregateDvOrdinal(List data, String agg,
                                    Archetype archetype, Object constraint, CtrlTerminologia terms,
                                    Map aggregator, Map names)
    {
       //println 'DvOrdinal: ' + constraint.list // constraint.list lista de org.openehr.am.openehrprofile.datatypes.quantity.Ordinal
       
       // Inicializacion de valores de agregacion
       // Ordinal.symbol es CodePhrase, y .codeString tengo el codigo de la restriccion.
       constraint.list.each {
          
          aggregator[it.symbol.codeString] = 0
       }
       
       // Calculo de agregacion
       data.each {
          
          //println it.value // DvOrdinal, .symbol es DvCodedText, .definingCode es CodePhrase
          aggregator[it.value.symbol.definingCode.codeString] ++
       }
	   
	   def terminologyId = TerminologyID.create('local', null) // Para sacar el nombre del concepto de la ontologia del arquetipo
       
       // labels
       aggregator.each {
          
          names[it.key] = terms.getTermino(terminologyId, it.key, archetype, session.locale) // at0000 es el id de la raiz del arquetipo
       }
    }
    
	
    private void aggregateCodePhrase(List data, String agg,
                                    Archetype archetype, Object constraint, CtrlTerminologia terms,
                                    Map aggregator, Map names)
    {
       //println 'CodePhrase: ' + constraint.codeList
       
       // Inicializacion de valores de agregacion
       constraint.codeList.each{
          
          aggregator[it] = 0
       }

       // Calculo de agregacion
       data.each {
          
         println it.value.definingCode.codeString
         
         aggregator[it.value.definingCode.codeString] ++
       }
       
	   def terminologyId = TerminologyID.create('local', null) // Para sacar el nombre del concepto de la ontologia del arquetipo
	   
       // labels
       aggregator.each {
          
          names[it.key] = terms.getTermino(terminologyId, it.key, archetype, session.locale) // at0000 es el id de la raiz del arquetipo
       }
    }
    
    
    private void aggregateDvBoolean(List data, String agg,
                                    Archetype archetype, Object constraint, CtrlTerminologia terms,
                                    Map aggregator, Map names)
    {
       // Inicializacion de valores de agregacion
       aggregator['true'] = 0
       aggregator['false'] = 0
       //aggregator['null'] = 0 // No responde ***
       
       // Calculo de agregacion
       data.each {
         
         // it Element, it.value DvBoolean, it.value.value Boolean
         //println it.value.value
         aggregator[it.value.value.toString()] ++
       }
       
       // labels
       names['true'] = 'Si'
       names['false'] = 'No'
       //names['null'] = 'NR' ***
    }
    
    
    private void aggregateDV_TEXT(List data, String agg,
                                  Archetype archetype, Object constraint, CtrlTerminologia terms,
                                  Map aggregator, Map names)
    {
       def values = params.aggKeys.split(',')
       
       //println values
       
       values.each{ aggValue ->
         
         aggregator[aggValue] = 0
         names[aggValue] = aggValue // Uso el valor del usuario como label
       }
       
       //println aggregator
       
       // La agregacios es viendo por si el aggValue esta incluido en el DvText, si esta cuenta 1.
       
       // Calculo de agregacion
       data.each {
         
          // it Element, it.value DvText, it.value.value String
          //println it.value.value

          values.each() { search ->
             
             // TODO: case insensitive
             // TODO: trim de espacios en search
             if (it.value.value.contains(search))
             {
                println "match "+ it.value.value +" contains "+ search
                aggregator[search] ++
             }
          }
       }
    }
    
    
   /**
    * archetypeId
    * chart_path del ELEMENT :: y del CDvOrdinal o CCodePhrase donde estan los valores para clasificar
    * TODO: dateFrom
    */
   def aggregate = {
       
      println params
      
      // Arquetipo seleccionado desde la lista de conceptos
      def archetype = archetype.ArchetypeManager.getInstance().getArchetype(params.archetypeId)
       
      // Lista de elementos del RM al filtrar por arquetipo y path
      def data
       
       
      def elemPath = params.chart_path.split("::")[0]
      def constraintPath = params.chart_path.split("::")[1]
       
       /*
      def startTimeFilter = new data_types.quantity.date_time.DvDateTime(value: ((params.fromDate)?params.fromDate:'1900-01-01 00:00:00') )
      com.thoughtworks.xstream.XStream xstream = new com.thoughtworks.xstream.XStream()
      xstream.omitField(data_types.basic.DataValue.class, "errors");
      def codedStartTimeFilter = xstream.toXML(startTimeFilter)
      */
      
      // Si viene la fecha, se usa el mismo formato que se utilizo en la vista para parsear el string
      def startTimeFilter = ((params.fromDate) ? Date.parse(ApplicationHolder.application.config.app.l10n.date_format, params.fromDate) : new Date(0, 0, 1)) // 1900/01/01
      
      
      // Esta consulta se podria hacer igual con withCriteria
      //data = hce.core.common.archetyped.Locatable.findAll( "FROM Locatable p WHERE p.archetypeDetails.archetypeId = ? AND p.path = ? AND EXISTS( SELECT c.id FROM Composition c WHERE c.id = p.parentCompositionId AND c.context.codedStartTime > ?)", [params.archetypeId, elemPath, codedStartTimeFilter] )
      data = hce.core.common.archetyped.Locatable.findAll("FROM Locatable p WHERE p.archetypeDetails.archetypeId = ? AND p.path = ? AND EXISTS( SELECT c.id FROM Composition c WHERE c.id = p.parentCompositionId AND c.startTime > ?)", [params.archetypeId, elemPath, startTimeFilter])
       

      // Valores para clasificar
      def constraint = archetype.node(constraintPath)
       
      // Codigos de agregacion -> valor agregado (counter)
      def aggregator = [:]
       
      // Codigos de agregacion -> nombres correspondientes (label para mostrar)
      def names = [:]
      def terms = CtrlTerminologia.getInstance()
      
       
       
      // ============================================================================================
      // TODO: para cada tipo deberia haber agregadores distintos que se puedan seleccionar.
      //       ej. DvCount puedo querer SUM o AVG o MAX o MIN
      // ============================================================================================
      
      
      String agg_ftn = "aggregate" + constraint.rmTypeName
      
      // TODO: distintas funciones de agregacion por tipo de dato
      "$agg_ftn"(data, null, archetype, constraint, terms, aggregator, names)
      
      // FIXME:
      // DvQuantity tiene el problema de que depende de las unidades,
      // ej. hacer promedios de magnitudes con distintas unidades da cualquier cosa.
      // eso ahora no lo considero.
      /*
      if (constraint.rmTypeName == 'DvQuantity')
      {
         // list de org.openehr.am.openehrprofile.datatypes.quantity.CDvQuantityItem
         // Puedo usar las constraints para hacer promedios solo en la misma unidad
         //println 'DvQuantity: ' + constraint.list
       
         if (data.size() > 0)
         {
            // Hay un solo agregador de promedio (los agregadores de los demas tipos
            // lo que hacen es contar ocurrencias de un valor, eso para DvQuantity no
            // tiene mucho sentido).
            aggregator['promedio'] = 0
          
            // Calculo de agregacion AVG en magnitude (sin considerar units distintas!!!)
            // data es List<Element>
            data.each {
            
               //println it.value.magnitude
               aggregator['promedio'] += it.value.magnitude
            }
          
            aggregator['promedio'] = aggregator['promedio'] / data.size()
          
            // labels (hay solo una porque es un promedio)
            names['promedio'] = 'promedio' // TODO: I18N
         }
      }
      */
      
      /*
      if (constraint.rmTypeName == 'DvOrdinal')
      {
         println 'DvOrdinal: ' + constraint.list // constraint.list lista de org.openehr.am.openehrprofile.datatypes.quantity.Ordinal
         
         // Inicializacion de valores de agregacion
         // Ordinal.symbol es CodePhrase, y .codeString tengo el codigo de la restriccion.
         constraint.list.each {
            
            aggregator[it.symbol.codeString] = 0
         }
         
         // Calculo de agregacion
         data.each {
            
           println it.value // DvOrdinal, .symbol es DvCodedText, .definingCode es CodePhrase
           
           aggregator[it.value.symbol.definingCode.codeString] ++
         }
         
         // labels
         aggregator.each {
            
            names[it.key] = terms.getTermino(terminologyId, it.key, archetype, session.locale) // at0000 es el id de la raiz del arquetipo
         }
      }
      */
      /*
      if (constraint.rmTypeName == 'CodePhrase')
      {
         println 'CodePhrase: ' + constraint.codeList
         
         // Inicializacion de valores de agregacion
         constraint.codeList.each{
            
            aggregator[it] = 0
         }

         // Calculo de agregacion
         data.each {
            
           println it.value.definingCode.codeString
           
           aggregator[it.value.definingCode.codeString] ++
         }
         
         // labels
         aggregator.each {
            
            names[it.key] = terms.getTermino(terminologyId, it.key, archetype, session.locale) // at0000 es el id de la raiz del arquetipo
         }
      }
      */
      /*
      if (constraint.rmTypeName == 'DvBoolean')
      {
          // ***
          // El caso de null no es necesario porque cuando es null no se crea el Element, asi que no hay Element para verificar su valor null aqui...
          
          // Inicializacion de valores de agregacion
          aggregator['true'] = 0
          aggregator['false'] = 0
          //aggregator['null'] = 0 // No responde ***
          
          // Calculo de agregacion
          data.each {
            
            // it Element, it.value DvBoolean, it.value.value Boolean
            //println it.value.value
            aggregator[it.value.value.toString()] ++
          }
          
          // labels
          names['true'] = 'Si'
          names['false'] = 'No'
          //names['null'] = 'NR' ***
      }
      */
      /*
      if (constraint.rmTypeName == 'DV_TEXT')
      {
          //println 'DV_TEXT'
          //println params.aggKeys // valores de agregacion puestos por el usuario, separados por coma
          def values = params.aggKeys.split(',')
          
          //println values
          
          values.each{ aggValue ->
            
            aggregator[aggValue] = 0
            names[aggValue] = aggValue // Uso el valor del usuario como label
          }
          
          //println aggregator
          
          // La agregacios es viendo por si el aggValue esta incluido en el DvText, si esta cuenta 1. 
          
          // Calculo de agregacion
          data.each {
            
             // it Element, it.value DvText, it.value.value String
             //println it.value.value

             values.each() { search ->
                
                // TODO: case insensitive
                // TODO: trim de espacios en search
                if (it.value.value.contains(search))
                {
                   println "match "+ it.value.value +" contains "+ search
                   aggregator[search] ++
                }
             }
          }
          
          //println aggregator
       }
       */
       
       //println data
       //println aggregator // code -> cantidad (el codigo deberia usarlo para obtener el nombre segun el servicio terminologico y el locale seleccionado
       //println names
       
       def r = [ names: names, aggregator: aggregator ]
       
       render r as JSON
   }
}