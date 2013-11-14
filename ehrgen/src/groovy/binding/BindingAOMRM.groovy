/*
 * Binder de objetos del RM a partir del template, arquetipos y datos submiteados de la web.
 */
package binding

import org.codehaus.groovy.grails.commons.ApplicationHolder

import org.openehr.am.archetype.Archetype
import org.openehr.am.archetype.constraintmodel.ArchetypeConstraint
import org.openehr.am.archetype.assertion.Assertion
import org.openehr.am.archetype.constraintmodel.*
import org.openehr.am.archetype.constraintmodel.primitive.*
import org.openehr.am.openehrprofile.datatypes.quantity.*
import org.openehr.am.openehrprofile.datatypes.text.CCodePhrase
import hce.core.datastructure.itemstructure.representation.*

import archetype.ArchetypeManager
import templates.TemplateManager

import data_types.quantity.date_time.*
import data_types.quantity.*
import data_types.basic.*
import data_types.text.*
//import hce.core.RMObject
import hce.core.common.archetyped.*
import support.identification.*

import com.thoughtworks.xstream.XStream

/**
 * @author Leandro Carrasco, Pablo Pazos Gutierrez
 */
class BindingAOMRM {

    // Inicializo HashMap en donde almaceno los objetos bindeados del RM.
    // Mapa ArqId->Locatable, donde el locatable se coresponde con el nodo raiz del arquetipo.
    //LinkedHashMap<String, Locatable> mapaRMO = new LinkedHashMap<String, Locatable>()
    def mapaRMO = [:]
    
    // Estructura que mapea, nombre de arquetipo, con objeto Locatable
    // en donde se instancia el bindeo para ese arquetipo
    // Inicializo lista de referencias a ArchetypeSlot
    // LinkedHashMap<String, Locatable> mapaRefArchSlot = new LinkedHashMap<String, Locatable>() 
    
    // Todas estas tienen la misma clave
    def tempSlotRefs = [:] // slotNodePath => arquetipo referenciado
    def tempSlotRefsSlots = [:] // similar a al anterior pero para la path guardo el slot
    def tempSlotResfsAttrs = [:] // similar al anterior, es para resolver que no tengo el nombre del atributo al tener el slot (porque slot.getParent() retorna null, es un bug y ya lo reporte en la ML)
    
    // Resultado final del procesamiento de los 3 mappings anteriores.
    def slotRefs = []
    
    
    // Prueba para usar como indice de las estructuras del RM mapeadas con cada arquetipo referenciado.
    def idx = [:]
    
    
    // TODO: esto lo podria tener chacheado desde que arranca la aplicacion/
    def getRMRootsIndex()
    {
        this.slotRefs.each { slotRef ->
            
            def bindedRef = this.mapaRMO[slotRef.getRefArqId()]
            idx[slotRef.getRefArqId()] = bindedRef
        }
        
        // FIXME: el raiz tambien hay que meterlo.
        // ESto lo pone bind con la estructura final, yo no tengo el nombre del arquetipo aca.
        //idx[] = resultBindeo // OJO! ya tiene los demas objetos enganchados!
        
        return this.idx
    }
    
    // Almacena el resultado del bindeo
    // FIXME: no se si es necesaria, el resultado se retorna del metodo bind.
    Locatable resultBindeo
    
    
    // VALIDACION ==========================================================
    //
    // Este mapa de errores por path se necesita para el edit con la generacion actual de gui
    def errors = [:]
    
    // La idea es: el bind de los nodos multiples setean esto al numero de nodo actual,
    // y si ocurre un error, este numero se incluye en el error, y sirve para ubicar el
    // error en el nodo multiple correspondiente, sino, no se sabe donde ubicarlo
    // (todos los campos de nodos multiples se llaman igual).
    int currentIndex = 0
    
    // Si la validacion en el save del GORM no detecta errores, aunque yo haya metido un reject
    // (me pasa en el bool de 'hay alteraciones' en la evaluacion de la via aerea), al terminar
    // como no detecta errores va a show en lugar de a edit. Con esta bandera logro prenderla
    // cuando detecto el error de null en el DataValue y lo pongo en el Element, y puedo ir a
    // edit como deberia.
    boolean hasErrors = false
    def hasErrors()
    {
        return this.hasErrors   
    }
    
    /**
     * Sirve para que el binder vaya metiendo errores en errors a medida que los va encontrando
     * @param path
     * @param errors http://static.springsource.org/spring/docs/3.0.x/javadoc-api/org/springframework/validation/Errors.html
     */
    private void addErrors( String path, Object errors )
    {
       this.hasErrors = true
       
       // Una misma path podria tener varios errores,
       // esto pasa en caso de nodos multiples clonados.
       if (!this.errors[path]) this.errors[path] = [:] // index -> errors
       
       // Agrega los errors (tipo de errores del GORM)
       this.errors[path][currentIndex] = errors
       
       // Intento diferenciar usando currentIndex
       //this.errors[path+'__'+currentIndex] << errors
    }
    //
    // /VALIDACION ==========================================================
    
    
    // PAB: me quedo con la instancia global de FactoryObjectRM para no pedirla cada vez.
    // TODO: que no sea singleton y que le pueda pasar un locale para obtener los textos de las ontologias de los coded_text.
    def rmFactory
    
    /**
     * Constructor al que se le pasa la session para acceder al locale seleccionado.
     * El locale se usa para seleccionar los textos correctos de las ontologias, para
     * los coded text. Si no se tiene session, se usa el idioma por defecto.
     * @param session
     */
    def BindingAOMRM(Object session)
    {
        this.rmFactory = new FactoryObjectRM(session)
    }

    /**
     * Metodo auxiliar para obtener la path a un nodo con el id del arquetipo al
     * que pertenece (igual a como vienen las paths con sus valores de la GUI).
     */
    def fullPath( Archetype a, ArchetypeConstraint c )
    {
        return a.archetypeId.value + c.path()
    }
    
    /**
     * Retorna el ultimo bindeo realizado con el objeto
     * PAB: el objeto se retorna, creo que no es necesario este metodo.
     *
     * @return Ultimo bindeo
     */
    Locatable getResultBindeo()
    {
        return resultBindeo
    }
    
    /**
     * Retorna el numero de valores no vacios de la lista.
     * Se considera vacio un string vacio o un null.
     * Se usa para evaluar si hay un error por la cardinalidad de las ocurrencias, 
     * comparando con la cantidad de valores no nulos que vienen desde la web, este
     * metodo da este ultimo numero.
     */
    int countValues( List list )
    {
        int count = 0
        list.each {
           if (it) count++
        }
        return count
    }
    
    /**
     * Retorna true si el nombre rmTypeName es de un DataValue.
     * Para tipos DomainType como DvCodedText o DvQuantity retorna false.
     */
    // FIXME: pasa que para el rmTypName DV_CODED_TEXT el tipo de la restriccion es CComplexObject,
    //        en lugar de ser CDomainType como muestra la especificacion del AOM.
    //        entonces trato a DV_CODED_TEXT como esta CComplexObject y luego veo
    //        de preguntar en la comunidad a ver porque es que no viene CDomainType.
    //
    // El tema creo que viene porque el CComplexObject que restringe el DV_CODED_TEXT, restringe su atributo defining_code,
    // y para hacer esa restriccion de atributos, necesita ser CComplexObject para tener CAttributes asociados.
    //
    //        POR AHORA AGREGO DV_CODED_TEXT como un tipo comun en isDataValue no un DomainType.
    // 
    //        Esto agrega la necesidad de tener un metodo bindDV_CODED_TEXT
    def isDataValue( String rmTypeName )
    {
        // TODO: agregar tipos faltantes
        return ['DV_BOOLEAN',
                'DV_MULTIMEDIA',
                'DV_COUNT',
                'DV_TEXT',
                'DV_CODED_TEXT', // LO AGREGO ACA DE FORMA TEMPORARIA hasra resolver porque no es un DomainType en la instancia del AOM.
                'DV_DATE',
                'DV_DATE_TIME',
                'DV_TIME',
                'DV_DURATION'
                ].contains(rmTypeName)
    }
    
    // FIXME: pasa que para el rmTypName DV_CODED_TEXT el tipo de la restriccion es CComplexObject,
    //        en lugar de ser CDomainType como muestra la especificacion del AOM.
    //        entonces trato a DV_CODED_TEXT como esta CComplexObject y luego veo
    //        de preguntar en la comunidad a ver porque es que no viene CDomainType.
    //
    //        POR AHORA AGREGO DV_CODED_TEXT como un tipo comun no un DomainType.
    //
    def isDomainType( String rmTypeName )
    {
        return [//'DV_CODED_TEXT',
                'DV_QUANTITY',
                'DV_ORDINAL'
                ].contains(rmTypeName)
    }
    
    def isItemStructure( String rmTypeName )
    {
        return ['ITEM_TREE',
                'ITEM_LIST',
                'ITEM_SINGLE',
                'ITEM_TABLE',
                'ITEM_STRUCTURE' // no debe caer aca porque es abstracta...
                ].contains(rmTypeName)
    }
    
    def isItem( String rmTypeName )
    {
        return ['CLUSTER',
                'ELEMENT',
                'ITEM' // no deberia caer aca porque es abstracta
                ].contains(rmTypeName)
    }
    
    /**
     * Para saber si un tipo es contenedor, entonces valida las occurrences de sus hijos
     * y valida la cardinalidad de su atributo que es una coleccion de hijos.
     * 
     * @param rmTypeName
     * @return
     */
    def isContainer( String rmTypeName )
    {
        return ['ITEM_TREE',
                'ITEM_LIST',
                'ITEM_SINGLE',
                'ITEM_TABLE',
                'CLUSTER'
                ].contains(rmTypeName)
    }

    
    /**
     * Retorna un mapeo con nombre de arquetipo, arquetipo, para todos
     * los arquetipos incluidos en el template con id templateId
     *
     * @param templateId, nombre del template para el cual se obtendra el mapeo
     * @return mapeo nombre arquetipo, arquetipo para los arquetipos incluidos en el template
     */
    LinkedHashMap<String, Archetype> getArchetypeIDList(String templateId)
    {
       def template = TemplateManager.getInstance().getTemplate(templateId)
    
       def manager = ArchetypeManager.getInstance()

       def archetypeList = [:] // archId => archetype
       template.getArchetypeIDs().each { archId ->
           archetypeList[archId] = manager.getArchetype(archId)
       }
    
       return archetypeList
    }

    
    /**
     * Retorna el id del arquetipo raiz del template de Id templateId
     *
     * @param templateId, nombre del template para el cual se obtendra el id del arquetipo raiz
     * @return id del arquetipo raiz del template de Id templateId
     */
    String getIdArchetypeRoot(String templateId)
    {
        def template = TemplateManager.getInstance().getTemplate(templateId)
        return template.getRootArchetypeID()
    }


    /**
     * Metodo que realiza el bind entre los datos llenados por el usuario 
     * (definidos por los arquetipos) y objetos del modelo de referencia
     *
     * El resultado del bindeo puede obtenerse con la operación getResultBindeo() y
     * los errores surgidos en el mismo con la operación getErrores()
     *
     * @param pathValor, es un mapeo entre las diferentes path de los arquetipos y
     * los valores ingresados por el usuario
     * @param templateId, es el id del template sobre el cual se realizara el bindeo
     */
    def bind(LinkedHashMap<String, Object> pathsValor, String templateId)
    {
        //println "== bind " + pathsValor
        //println "   = pathsValor: " + pathsValor
        //println "   = templateId: " + templateId
        //println "======================================================="
        
        LinkedHashMap<String, Archetype> arquetipos = getArchetypeIDList(templateId)
        
        // FIXME: para los arquetipos que interesa el bindeo van a ser los que tengan elements, que es donde se ponen los valores.
        //        las estructuras de DataStructure para arriba pueden ser generadas a partir del arquetipo sin necesidad de los valores submiteados, por lo que pueden estar cacheadas para cada arquetipo.
        //        y luego le enchufo las estructuras de clusters y elements que si tienen valores a bindear.
        
        // Se podria aumentar la performance, prosesando en paralelo los arquetipos. Para esto primero cargamos un mapeo <idArquetipo, lista de path>
        arquetipos.each{ entry ->
        
            String arqId = entry.key
            
            //println "=== bind: arqId: " + arqId

            // Me quedo con los path que corresponden al arquetipo que se esta procesando
            def keys = pathsValor.keySet().findAll{ it.startsWith(arqId) }
            LinkedHashMap<String, Object> pathValorArquetipo = pathsValor.subMap(keys)

            // Quito el id del Arquetipo de los Path
            LinkedHashMap<String, Object> pathValorSinIdArq = [:]
            pathValorArquetipo.keySet().each{
                pathValorSinIdArq.put(it[arqId.length() .. (it.length() - 1)], pathValorArquetipo[it])
            }

            // PAB: poda previa, si no tengo pathValor para el arq, ni siquiera llamo al bindArq
            // Si hago esta poda me quedo sin los arquetipos de SECTION que tienen solo SLOTS
            // y no tienen ningun pathValor
            
            //println "========================================="
            //println "bind Entry: " + entry

            // PAB: (***) rmObject puede ser null si no vienen valores para bindear...
            def rmObject = bindArquetipo(entry.value, pathValorSinIdArq, templateId)

            //println "||>>>||||>>>>>>>||>>>> Salida bindArquetipo: " + rmObject
            
            //completarLocatable(rmObject, entry.value.node("/").nodeID, entry.value, templateId)
            
            // (***)
            if (rmObject) // puede ser una lista!, en a1_a2 la raiz es Cluster y bindCluster tira una lista!
            {
                if (rmObject instanceof List)
                {
                    if (rmObject[0]) // Si la lista no es vacia (ver quien mete un elemento nulo y corregirlo para que no meta nada en la lista)
                    {
                        // Aunque tire una lista, la raiz siempre va a ser una, por lo que la lista tiene solo un elemento!
                        //println "FIXME: tira una lista"
                        //println "FIXME: tira una lista"
                        //println "FIXME: tira una lista"
                        rmObject[0].archetypeNodeId = arqId
                        this.mapaRMO[arqId] = rmObject[0]
                    }
                }
                else
                {
                    // El archetypeNodeId es el archetypeId porque es nodo raiz (ref: common_im.pdf, Pagina 14)
                    rmObject.archetypeNodeId = arqId

                    // Guardo en un mapeo para armar el objeto RM final en base a los ArchetypeSlots
                    //this.mapaRMO.put(arqId, rmObject)
                    this.mapaRMO[arqId] = rmObject
                }
            }
        }

        // SOLUCION INICIAL DE SLOT
        /*
        println "**************************"
        println "**** COMPLETAR SLOTS *****"
        println "mapaRefArchSlot: " + this.mapaRefArchSlot // FIXME: me dice que esto es vacio...
        println "**************************"
        */
        
        // FIXME: mapaRMO podria ser global
        resultBindeo = completarRMOAS(templateId)

        // Resultado va al indice de raices de RM por su respectivo arquetipo
        idx[ getIdArchetypeRoot(templateId) ] = resultBindeo

        // PAB
        return resultBindeo
    }

    /**
     * Metodo que une las estructuras armadas para cada archetypeSlot, presente en los
     * arquetipos que se usan para el binder, en una estructura del RM.
     * 
     * Recorro estructura global que se fue armando a medida que se bindeaba y se encontraba
     * un ArchetypeSlot, en esta estructura se mapea, id del arquetipo con un objeto del RM vacio (que pertenece
     * a un arbol padre de objetos del RM bindeados, mapaRefArchSlot). Para cada elemento de esta estructura, busco
     * el subarbol correspondiente en el mapeo mapaRMO (mapea id de arquetipo con subarbol correspondiente
     * al arquetipo). Luego copio la informacion de este ultimo objeto del RM, en el objeto del RM que se
     * encuentra referenciado en la estructura mapaRefArchSlot.
     *
     * @param templateId: Nombre del Template, de donde se obtiene el arquetipo raiz
     * @param mapaRMO: Estructura que mapea id de arquetipo con subarbol del RM bindeado
     * @return Estructura completa del RM bindeada para el template
     */
    def completarRMOAS( String templateId )
    {
        //println "==== completarRMOAS: "
        //println "     = mapaRMO: " + this.mapaRMO
        //println "     = slotRefs: " + this.slotRefs // global
        //println "     = tempSlotRefsSlots: " + this.tempSlotRefsSlots // global
        //println "=========================================="
        
        // forma inicial de hacerlo
        String idArqRoot = getIdArchetypeRoot(templateId)
        def rmObjectRoot = this.mapaRMO[idArqRoot] // Quito el objeto raiz (sera el objeto que retorne)
        //this.mapaRMO.remove(idArqRoot)

        this.slotRefs.each { slotRef ->

            def owner = slotRef.owner // A quien le tengo que enchufar el objeto referenciado.

            //slorRef.reference // refArq
            //slotRef.ownner    // objeto a setear la referencia
            def bindedRef = this.mapaRMO[slotRef.getRefArqId()] // TODO: verificar que da algo != null

            // TODO: ver que atributo era el de la referencia
            //       sacar el objeto vacio que esta ahora en ese atributo con el refArqId en su archetypeDetails
            //       setear o agregar el bindedRef
            // tengo que ver si el atributo es simple o multiple, para hacer setXXX o addToXXX

            // Obtengo el slot
            def slot = slotRef.ownerSlot // FIX: al problema de tener mal las paths en el adl parser

            // BUG: getParent() da null para los ArchetypeSlots,
            // es un bug y ya lo reporte en la ML, lo soluciono encajando otro map global con el attr...
            //def parentAttr = slot.getParent()
            def parentAttr = this.tempSlotResfsAttrs[slotRef.pathToSlot]
            def attr = parentAttr.rmAttributeName
            
            //println "|||||| ATTR: " + attr
            
            // Es un atributo multiple o simple?
            if ( parentAttr instanceof CMultipleAttribute )
            {
                //println "|||||| ES MULTIPLE: " + slotRef.pathToSlot
                
                // 1. Remover de la coleccion (es un attr multiple entonces es una coleccion) el elemento
                //    vacio bindeado en owner que tenga el mismo archetypeId que la referencia.
                // 2. Agregar a la coleccion la referencia bindeada con toda la informacion.
                
                
                // Todos los objetos dummies de owner.$att tienen su archetypDetails porque
                // se los setea el rmFactory.completarLocatable.
                def removeObject = owner."$attr".find{ it.archetypeDetails.archetypeId == slotRef.getRefArqId() }
                
                if (!removeObject) throw new Exception( "No encuentra al objeto vacio en el owner... con: " + slotRef.getRefArqId() )
                
                owner."$attr".remove(removeObject)
                
                // WARNING: si no vinieron valores, bindedRef puede ser null. Si meto null en
                //          la coleccion la linea de arriba de def removeObject = ow... falla 
                //          al pedir it.archDetails porque it es null :D
                if (bindedRef)
                    owner."$attr".add( bindedRef )
            }
            else // Asumo que si cae aca es CSingleAttribute, no hay otra opcion.
            {
                //println "|||||| ES SIMPLE: " + slotRef.pathToSlot
                
                // 1. Setear el atributo con el bindedRef, asi sobreescribe el objeto vacio que
                //    estaba en ese lugar.
                
                // WARNING: bindedRef puede ser null, no pasa nada porque setea null y elimina el objeto vacio que estaba antes.
                owner."$attr" = bindedRef
            }
        } // completarRMOAS
        
        //println "|||||||||||||||||||||||||||||||||||||||||||||||||||"

        // FUTURE: el resultado del bind podria no ser un unico objeto raiz,
        //         podrian ser objetos primos, al mismo nivel pero de distintos
        //         arquetipos, entonces el bind devolveria una lista de elementos.
        return rmObjectRoot
        
    } // completarRMOAS


    /**
     * Retorna el bindeo para el arquetipo dado, tomando en cuenta el mapeo pathValorArquetipo
     *
     * @param arquetipo, es el arquetipo sobre el cual se realizara el bindeo
     * @param pathValorArquetipo, mapeo de path y valores ingresados por el usuario, los cuales
     * seran bindeados a objetos del RM segun las path
     * @param tempId, id del template sobre el que se realiza el bindeo
     *
     * @return Objeto del RM correspondiente al bindeo
     */
    def bindArquetipo(Archetype arquetipo, LinkedHashMap<String, Object> pathValorArquetipo, String tempId)
    {
        //println "==== bindArquetipo " + arquetipo.archetypeId.value
        //println "==== bindArquetipo " + pathValorArquetipo
        //println "========================================================"
        
        CComplexObject cco = arquetipo.getDefinition()
        def bindedObject = bindCComplexObject(cco, pathValorArquetipo, arquetipo, tempId)
        if (bindedObject)
        {
            if (!(bindedObject instanceof List))
            {
                bindedObject.path = cco.path()
            }
            else if (bindedObject[0] != null)
            {
                bindedObject[0].path = cco.path()
            }
        }
        return bindedObject
    }

    /**
     * Punto de entrada del bindeo. Llama al metodo de bindeo correspondiente al
     * tipo de objeto que sea el parametro de entrada co.
     *
     * @return objeto del RM bindeado
     */
    def bindCObject(CObject co, LinkedHashMap<String, Object> pathValorCObject, Archetype arquetipo, String tempId)
    {
        //println "========== bindCObject"
       
        // Obtengo el nombre de la clase (nombre sin el paquete), para luego llamar al metodo correspondiente
        String nombreClase = co.getClass().getSimpleName()
        String bindMethod = 'bind' + nombreClase
        
        // FIXME: pasa que para el rmTypName DV_CODED_TEXT el tipo de la restriccion es CComplexObject,
        //        en lugar de ser CDomainType como muestra la especificacion del AOM.
        //        entonces trato a DV_CODED_TEXT como esta CComplexObject y luego veo
        //        de preguntar en la comunidad a ver porque es que no viene CDomainType.
        //
        // El tema es que la restriccion para DV_CODED_TEXT restringe su atributo defining_code mediante CAttribute,
        // y es el CCOmplexObject el que tienen CAttributes relacionados.
        //        
        //println "bindCObject = bindMethod: " + bindMethod + " rmTypeName: " + co.rmTypeName
        
        
        //println "==== bindCObject"
        //println "   = bindMethod: " + bindMethod
        //println "   = typeName: " + co.rmTypeName
        //println "   = co: " + co
        //println "======================================================="
        
        
        //println "========== bindCObject -> " + bindMethod
        
        // Llamara a bindCComplexObject, bindCPrimitiveObject, bindCDomainType, bindArchetypeSlot, bindArchetypeInternalRef
        def bindedObjs = this."$bindMethod"(co, pathValorCObject, arquetipo, tempId)

        /*
         * No signature of method: binding.BindingAOMRM.bindConstraintRef() is applicable for argument types: (org.openehr.am.archetype.constraintmodel.ConstraintRef, java.util.LinkedHashMap, org.openehr.am.archetype.Archetype, java.lang.String) values: [org.openehr.am.archetype.constraintmodel.ConstraintRef@1922007[ reference=ac0001 rmTypeName=CodePhrase occurrences=org.openehr.rm.support.basic.Interval@74ac5f[lower=1,lowerIncluded=true,upper=1,upperIncluded=true] nodeID=<null>  parent=<null>  anyAllowed=false path=/data[at0001]/items[at0002]/value/defining_code ]
         */
        
        //println "==== Setear las paths a los objetos del RM bindeados ..."

        // FIXME: la path se la deberia poner la factory
        if (bindedObjs instanceof List)
        {
            //println "==== bindCObject######## ES UNA LIsTA @@@@@@@@@@"
            bindedObjs.each {
                if (it) // no deberia haber nulos...
                {
                    if (it instanceof Locatable)
                    {
                        it.path = co.path()
                    }
                }
            }
        }
        else
        {
            //println "==== bindCObject######## ES UN OBJETO SIMPLE @@@@@@@@@@"
            if (bindedObjs instanceof Locatable)
            {
                bindedObjs.path = co.path()
            }
        }
        
        return bindedObjs
    }

    /**
     * ownerArchetype arquetipo donde esta el nodo cco
     * cco es el nodo que se usa para bindear rmObject.
     * rmObject es el objeto ya bindeado a partir de cco, es el objeto que quiero ver si es duenio de algun slot que se haya encontrado.
     */
    def setSlotRefs(Archetype ownerArchetype, CComplexObject cco, Locatable rmObject)
    {
        //println "<<<<<<|o|>>>>>> setSlotRefs"
        
        // Quiero ver que hijos slot encuentro y saber si este nodo es el padre.
        // Me fijo si se bindeo algun slot para este nodo.
        // Supongo que el unico que puede tener hijos slots es un CComplexObject, si no es asi deberia hacer esto en otros metodos.
        def deleteSlots = [] // Slots a remover porque ya los proceso
        this.tempSlotRefs.keySet().each { refPath ->
        
            // Si la referencia parte del nodo cco, cco es su duenio
            if ( refPath.startsWith( cco.path() ) )
            {
                //println "<<<<<<<<< ENCUENTRO SLOT >>>>>>>>>>>>>"
                //println "<<<<<<<<< refPath: " + refPath
                //println "<<<<<<<<< ownerPath: " + cco.path()
                //println "<<<<<<<<< reference: " + this.tempSlotRefs[refPath].archetypeId.value
                //println ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
                deleteSlots << refPath // Marco para elminar path procesada
                this.slotRefs << new SlotRef(
                               reference: this.tempSlotRefs[refPath], // arquetipo que referencia
                               owner: rmObject,
                               pathToSlot: refPath,
                               ownerArchetype: ownerArchetype,
                               ownerSlot: this.tempSlotRefsSlots[refPath]
                            )
                //println ">>>>>>>> archetype.pathNodeMap: " + ownerArchetype.pathNodeMap
            }
        }
        
        // Elimina referencias usadas
        deleteSlots.each { path ->
            this.tempSlotRefs.remove(path)
            this.tempSlotRefsSlots.remove(path)
            //this.tempSlotResfsAttrs.remove(path) // no se debe eliminar el objeto porque no se pone en SlotRef, se deja global hasta que se usa...
        }
        
    } // setSlotRefs
    
    /**
     * PAB: me parece que a veces devuelve un unico elemento y a veces una lista,
     *      es mejor tener consistencia en el tipo, por ejemplo devolver siempre
     *      una lista.
     *
     * Bindeo para objetos (nodo del arquetipo) instancias de CComplexObject. Crea el objeto del RM
     * correspondiente (segun lo indicado por la propiedad rmTypeName del objeto) tomando en cuenta
     * los datos del mapeo pathValor.
     *
     * Si el CComplexObject tiene atributos, el metodo bindea todos sus atributos y crea el
     * objeto del RM tomando en cuenta todos los Objetos del RM correspondientes a los atributos
     *
     * @return objeto del RM bindeado
     */
    def bindCComplexObject(CComplexObject cco, LinkedHashMap<String, Object> pathValor, Archetype arquetipo, String tempId)
    {
        //println "====== bindCComplexObject " + cco.rmTypeName
        //println "====== bindCComplexObject " + pathValor        
       
//         println "==== bindCComplexObject"
        //println "   = pathValor: " + pathValor
        //println "   = attributes: " + cco.getAttributes()
        //println "   = typeName: " + cco.rmTypeName
        // println "======================================================="
        
        def rmTypeName = cco.rmTypeName
        
        // PAB:
        // FIXME: Ojo a veces es un objeto y a veces una lista, deberia se siempre una lista.
        def rmObject = null
        
        
        // PODA:
        // Si soy un nodo opcional y no tengo valores para bindear,
        // no sigo bindeando y genero una estructura valida.
        int min = cco.occurrences.getLower()
        if (min == 0 && pathValor.size() == 0)
        {
            //println "====== bindCComplexObject === PODA ==="
            return rmObject
        }
        
        
        // Bindeado de tipos que no quiero que bindee automaticamente sus atributos,
        // esto corta la recursion y bindea los tipos simples directamente.
        // Para: DV_BOOLEAN, DV_MULTIMEDIA, DV_COUNT, DV_TEXT.
        // No para ORDINAL, QUANTITY, CODED_TEXT que son DomainType.
        if ( this.isDataValue( rmTypeName ) )
        {
            //println "====== bindCComplexObject -> " + "bind" + rmTypeName
           
            def method = "bind" + rmTypeName
            rmObject = this."$method"(cco, pathValor, arquetipo, tempId) // es una lista de DataValue, podria ser vacia!
            return rmObject // es una lista SIEMPRE!
        }
        
        /**
         * Los unicos bindXXX que devuelven listas son los de CLUSTER y ELEMENT.
         */

        // Para CLUSTER se tienen que resolver casos especiales como cuando es multiple
        // la ocurrencia del CLUSTER, se deben manejar los valores multiples distinto a
        // como se manmejan para ELEMENT.
        if (rmTypeName == "CLUSTER")
        {
            //println "====== bindCComplexObject -> bindCLUSTER"
           
            // es una lista de clusters siempre
            // adentro hace la recorrida de sus attributes 
            return bindCLUSTER(cco, pathValor, arquetipo, tempId)
        }
        
        if (rmTypeName == "ELEMENT") // El element tambien resuelve multiples ocurrencias
        {
           //println "====== bindCComplexObject -> bindELEMENT"
           
           // FIXME:
           // Aca deberia venir el proceso de los rmObjects bindeados para cada atributo.
           // Si es multiple, para cada atributo voy a tener varios rmObjs.
           // Para crear ELEMENTS, le deberia mandar de a uno los rmObjs, en este caso el
           // atributo es uno solo: value.
           
           // lista de elementos bindeados
           // adentro hace la recorrida de sus attributes
           return bindELEMENT( cco, pathValor, arquetipo, tempId)
        }
        
        
        // Si no es DataValue o Cluster o Element, usa directamente los metodos de factory...
        // FIXME: el bind de todos los objetos deberia ser consistente, la unica diferencia es
        // cuando hay que manejar multiplicidad y nodos estructurados, luego para elementos
        // simples, hay que verificar las mismas restricciones de ocurrencia, cardinalidad
        // y las restricciones de cada DataValue particular que haya en el arquetipo.
        // ESTO SE TRADUCE A QUE (si es posible) NO DEBERIAN HABER if POR EL rmTypeName y el codigo deberia ser
        // el mismo, hasta que no se deban generar nodos multiples o bindear nodos estructurados.
        
        String factoryRMMethod = 'create' + rmTypeName
        
        //println ""
        //println "FactoryRMMethod: "+ factoryRMMethod
        //println ""
        
        // Tipos que llegan aca:
        // SECTION
        //
        // INSTRUCTION
        // ACTIVITY
        // ITEM_TREE
        // DV_CODED_TEXT
        //
        // OBSERVATION
        // HISTORY
        // EVENT
        // ITEM_TREE, ITEM_SINGLE
        //
        // ACTION
        // EVALUATION
        
        //println "bindCComplexObject ==== el tipo es: " + cco.rmTypeName
        
        // FIXME: para procesar los atributos se deberia ir a otro metodo
        // PAB:
        // Me parece que aca hay un error, pregunta por si attributes no es null para saber
        // si son atributos simples o complejos?
        // Siempre que tenga atributos, tanto simples como complejos, la lista de atributos
        // no sera null, si esto esta mal, la solucion es preguntar por el tipo de los atributos
        // (CSingleAttribute o CMultipleAttribute)
        if (cco.getAttributes()) // con atributos
        {
            //println "-------CA>>>> " + cco.rmTypeName
            
            // Lista de lista de objetos bindeados para cada atributo del CCObject
            List<Object> listaListRMO = []

            cco.getAttributes().each { cattr ->
            
                // Me quedo con los path que corresponde al atributo que se esta procesando
                LinkedHashMap<String, Object> pathValorAtribute = pathValor.findAll{it.key.startsWith(cattr.path())}
                def listRMO
                
                // TODO:
                // Si no tengo valores para ese atributo ya se que va a retornar null,
                // se podria verificar si hay valores antes de hacer la llamada y si no
                // hay, retornar null directamente. Habria que hacer la verificacion de
                // errores por ocurrencia igual.
                
                // Abajo aun deberia verificar, si es que mande a bindear, y si esperaba un
                // minimo de objetos para el nodo del arquetipo, y obtengo menos objetos,
                // debe dar un error de que hay menos objetos de los esperados.
                
                // TEST: puse lo de SECTION para probar los slots, las SECTIONs no tienen pathsValor
                //       asi que no sale nada del bind y esta poda mata que siga bindeando los slots
                //       de la SECTION... si es SECTION quiero que entre...

                // PAB: si es un atributo multiple bindea segun los miembros,
                //      por ejemplo, si es 'items' de un ITEM_TREE, aca devuelve
                //      los CLUSTERs y ELEMENTs que van adentro de 'items'.
                
                listRMO = bindAttribute(cattr, pathValorAtribute, arquetipo, tempId)
                listaListRMO.add(listRMO)
            }
            
            println ""
            println "listaListRMO: "+ listaListRMO
            println ""
            
            // ==============================================================
            // El atributo INSTRUCTION.narrative no esta en el arquetipo, debo crear la path a mano para que bindee.
            // http://code.google.com/p/open-ehr-gen-framework/issues/detail?id=110
            if (cco.rmTypeName == 'INSTRUCTION')
            {
               //println "factoryRMMethod: " + factoryRMMethod // createINSTRUCTION
               // La instruction se crea en la llamada de abajo al metodo factoryRMMethod,
               // tengo que pasarle el valor de /narrative en el listaListRMO
               
               // narrative es el primer elemento de la segunda lista
               // la primera lista es de activities bindeadas
               // FIXME: esta path es si el arquetipo es de instruction y la instruction
               //        no esta directamente en un arquetipo de section o composition (plano)
               if (listaListRMO.size() == 0)
               {
                  listaListRMO << [] // no se bindearon activities
               }
               listaListRMO << [ new DvText(value: pathValor['/narrative']) ] // createINSTRUCTION espera DvText
            }
            // ==============================================================
            

             // FIXME: todo este codigo lo dejo para otros casos, no se si para CLUSTERS hay que hacer algo
             //        parecido que para ELEMENT, DV_COUNT o DV_BOOLEAN que se hace arriba.
             
             // PAB:
             // TODO: para otros tipos como CLUSTER o ITEM_SINGLE creo que se deberia hacer el mismo chequeo...
             // Si es ELEMENT, si listaListRMO == [[]] quiere decir que el value del ELEMENT es null
             
             
             // FIXME: verificacion de ocurrencias.
             // Para probar el binder con SLOTS no quiero que chequee nulos para SECTIONS
             // Para los ItemStruture no quiero chequeo de ocurrencias, es solo para nodos CLSUTER Y ELEMENT.
             // Si es item tree o itemlist podria verificar la cardinalidad de los Items, pero si habia restricciones sobre los items,
             // esos errores ya se van a mostrar.
             // La verificacion se hace en el ELEMENT.
             

             // Creo Objeto del modelo de referencia correspondiente (con los objetos del RM
             // devueltos por el bindAttribute en la iteracion por su lista de atributos)
             // PAB: ojo! $factoryRMMethod aca recibe como 1er parametro una lista de objetos RM,
             //      y en la llamada de la rama ELSE de este IF recibe el mapa de path/valor,
             //      puede llevar a problemas usar un parametro comodin.
             //      Se solucionaria si aca se recorre la lista y se llama con cada objeto.
             // FIXME: primer parametro...
             
            
             //println "====== bindCComplexObject -> "+ factoryRMMethod + " (1)"
             
             
             // Ya valida, entonces solo verifico si hay errores
             rmObject = rmFactory."$factoryRMMethod"(listaListRMO, arquetipo, arquetipo.node(cco.path()).nodeID, tempId, cco)

             // FIXME: a veces rmObject es una lista y a veces un Locatable
             // FIXED: el unico metodo de factory que tiraba list era createDV_CODED_TEXT
             if (rmObject instanceof Locatable && rmObject.hasErrors())
             {
                //println "bindCComplexObject rmObject tiene errores: " + rmObject.getErrors()
                
                // FIXME: la path completa seria archetypeId+refPath+path
                //        falta calcular refPath como esta calculada en las vistas
                //this.errors[arquetipo.archetypeId.value + cco.path()] = rmObject.errors
                //this.hasErrors = true
                this.addErrors(arquetipo.archetypeId.value + cco.path(), rmObject.errors)
             }

             // TEST
             // Corregido: ya no devuelve listas
             // if (rmObject instanceof List) println factoryRMMethod + " retorna una lista de " + rmObject.size() + " elementos"
             
             // Verifico slots hijos de este nodo
             // setSlotRefs(CComplexObject cco, Locatable rmObject)
             
// Ahora no devuelve mas una lista
//            if (rmObject instanceof LinkedList)
//            {
//                rmObject.each { obj ->
//                 
//                     if (obj instanceof Locatable) // Puede salir un DvValue
//                        setSlotRefs( arquetipo, cco, obj )
//                }
//            }
//            else
//            {
                 if (rmObject instanceof Locatable) // Puede salir un DvValue
                     setSlotRefs( arquetipo, cco, rmObject )
//            }
        }
        else // Sin Atributos
        {
            // Cae aca cuando es DV_TEXT, DV_COUNT, ...

            //println "-------SA>>>> " + cco.rmTypeName

            // FIXME: ver como determina el nodeId en el otro caso, creo que es distinto...
            String archNodeId = ""
            if (arquetipo.node(cco.path()).nodeID)
            {
                archNodeId = arquetipo.node(cco.path()).nodeID
            }
            
            // PARA UN CDvQuantity creo que vendrian 2 paths...
            if (pathValor.size() == 1)
            {
                def result = []
                
                // FIXME: con que termine depende del tipo que bindeo, es el nombre del atributo, no siempre es 'value'.
                //def values = pathValor.find{it.key.endsWith("value")}?.value
                def values = pathValor.find{ true }.value // Obtiene los valores de la unica entry del map
                if (values.getClass().isArray()) // Valores multiples
                {
                    def valuesList = values as List
                    
                    // FIXME: los valores pueden estar pero ser strings vacios... es lo mismo que si no estan!!!!
                    // FIXED: countValues cuenta los valores no nulos.
                    // (AAA) verificacion de errores de ocurrencias para cuando hay multiples valores
                    
                    //println "@@@@ @@@@@ @@@@"
                    //println "@@@@ FIXME @@@@"
                    //println "     VERIFICACION DE ERRORES DE OCURRENCIAS PARA EL CASO ''Sin Atributos''"
                    //println "@@@@ FIXME @@@@"
                    //println "@@@@ @@@@@ @@@@"
                    /*
                    if ( countValues(valuesList)<min) // hay menos elementos de los que se esperaban.
                    {
                        errors.add(arquetipo.archetypeId.value,
                                cco, //cco.path(),
                                Errors.ERROR_OCCURRENCES) // TODO: error por tipo rm
                    }
                    */
                    
                    valuesList.each { value ->
                    
                        //println " //////////////////////////////// VALUE 1: '" + value +"'" 

                        if (value)
                        {
                            //println "====== bindCComplexObject -> "+ factoryRMMethod + " (2)"
                           
                            // FIXME: si da except arriba no deberia invocar este metodo!
                            // Necesitaria que lo invoque para crear el objeto en el que hago rejectValue para poder usar los errors de grails...
                            result << rmFactory."$factoryRMMethod"(value, arquetipo, archNodeId, tempId, cco)
                        }
                    }
                }
                else // Valor simple
                {
                    //println " //////////////////////////////// VALUE 2: '" + values + "'" 
                    def value = values
                    
                    if (!value) // Si es vacio o null
                    {
                        //println "@@@@ @@@@@ @@@@"
                        //println "@@@@ FIXME @@@@"
                        //println "     VERIFICACION DE ERRORES DE OCURRENCIAS PARA EL CASO ''Valor Unico''"
                        //println "@@@@ FIXME @@@@"
                        //println "@@@@ @@@@@ @@@@"
//                      if (min > 0)
//                        {
//                            errors.add(arquetipo.archetypeId.value,
//                                    cco, //cco.path(),
//                                    Errors.ERROR_OCCURRENCES) // TODO: error por tipo rm
//                        }
                    }
                    else
                    {
                        //println "====== bindCComplexObject -> "+ factoryRMMethod + " (3)"
                       
                        // FIXME: si da except arriba no deberia invocar este metodo!
                        result << rmFactory."$factoryRMMethod"(value, arquetipo, archNodeId, tempId, cco)
                    }
                }

                return result // lista de objetos simples rm
            }
            else
            {
                // TODO: verificar cuando cae aca, si es que cae (deberia ser una fecha o un quantity
                //       sin definicion de restricciones para sus atributos).
                //println "== Path Valor tiene mas de una key: " + pathValor
                //println "==============================================================================="
            }
        }

        return rmObject
        
    } // bindCComplexObject

    
    /**
     * Bindeo para objetos (atributos de CComplexObject) instancias de Attribute. Crea objetos del RM
     * correspondiente a cada children del atributo tomando en cuenta el mapeo pathValorAttribute.
     *
     * @return objetos del RM bindeados
     */
    def bindAttribute(CAttribute cattr, LinkedHashMap<String, Object> pathValorAttribute, Archetype arquetipo, String tempId)
    {
        //println "======== bindAttribute "+ cattr.rmAttributeName +" "+ pathValorAttribute
        
        //println "==== bindAttribute"
        //println "   = name: " + cattr.rmAttributeName
        //println "=========================================="
        // PAB: el atributo depende del rm_type_name del CObject que se esta bindeando.
        
        // cattr.rmAttributeName sera "items" , "value" o "magnitude", etc (sera
        // un multiple attribute o un single attribute)

        // Creo la lista de items que voy a retornar
        List<Object> listaRMObject = []

        // FIXME: hacer una para multiple attribute y otra para single.
        // Lo bueno es que para single, el template deberia decir cual de los children usar,
        // porque son alternativas, entonces se tendria una sola restriccion children para bidear.
        // Para que esto funcione, deberia tener la estructura completa del arquetipo, segun como
        // fue definido su uso en el template, incluso si son multiples arquetipos, se
        // deberian tener resueltos los slots.
        
        // Recorro los CObjects del CAttribute
        cattr.getChildren().each { co ->
        
            // Me quedo con los path que corresponde al children (CObject) que se esta procesando
            LinkedHashMap<String, Object> pathValorCObject = pathValorAttribute.findAll{it.key.startsWith(co.path())}
            
             //println "======== bindAttribute -> bindCObject"
             //println "  bindAttribute children "+ co.rmTypeName + " pathValor: "+ pathValorCObject
             def rmObject = bindCObject(co, pathValorCObject, arquetipo, tempId)
 
             //------------------------------------
             // Agregado para multiples ocurrencias
             //------------------------------------
             
             // FIXME: mas facil si siempre devuelve una lista, cuando es un solo elemento es una lista con ese elemento.
             if (rmObject)
             {
                 if (rmObject instanceof List)
                 {
                     //println "  bindAttribute rmObject es una lista de "+ rmObject.size() + " elementos " + rmObject
                     rmObject.each { rmo ->
                     
                         if (rmo) listaRMObject.add(rmo)
                     }
                 }
                 else
                 {
                     listaRMObject.add(rmObject)
                 }
             }
        }
        
        //println "  bindAttribute listaRMObject es una lista de "+ listaRMObject.size() + " elementos " + listaRMObject
              
        // Necesario para el bind de slots
        // Soluciona el problema de que no puedo obtener el parent
        // attribute de los archetypeSlots porque getParent retorna
        // null, y es necesario para saber el nombre del atributo que
        // tiene el o los slots.
        // El proceso es similar al metodo setSlotRefs()
        //  - ver si alguna referencia a un slot fue encontrada en algun hijo
        //  - si se encuentra, guardar la referenca a este atributo en una estructura global
        
        this.tempSlotRefs.keySet().each { refPath ->
        
            // Si la referencia parte del nodo cattr, cattr es su duenio
            if ( refPath.startsWith( cattr.path() ) )
            {
                //println "<<<<<<<<< ENCUENTRO SLOT >>>>>>>>>>>>>"
                //println "<<<<<<<<< refPath: " + refPath
                //println "<<<<<<<<< ownerPath: " + cattr.path()
                //println "<<<<<<<<< reference: " + this.tempSlotRefs[refPath].archetypeId.value
                //println ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
                
                this.tempSlotResfsAttrs[refPath] = cattr
            }
        }
        
        // Retorno la lista con los objetos del RM creado, que podra contener
        // uno elemento en caso de ser cattr.rmAttributeName = "value" (por ejemplo)
        // y mas de un elemento en caso de que cattr.rmAttributeName = "item"
        return listaRMObject
        
    } // bindAttribute


    /**
     * Bindeo para objetos instancias de CPrimitiveObject. Crea un objeto primitivo del RM.
     *
     * @return objeto del RM bindeado
     * PAB: este metodo no hace nada de valor solo llama al otro, llamar al otro directamente o poner el codigo del otro metodo aca.
     */
    def bindCPrimitiveObject(CPrimitiveObject cpo, LinkedHashMap<String, Object> pathValor, arquetipo, tempId)
    {
        println "==== bindCPrimitiveObject "+ pathValor
        //println "   = pathValor: " + pathValor
        //println "   = rmType: " + cpo.rmTypeName
        //println "=========================================="
        
        // PAB:
        // Mismo chequeo que en bindCComplexObject por el min de las occurrences del nodo
        // del arquetipo viendo si el nodo es obligatorio y no viene ningun valor.
        
        // FIXME:
        // El min creo que no se deberia verificar para CPrimitiveObject, si no para el
        // CComplexObject ELEMENT duenio de este primitivo, y es ahi donde se deberian
        // crear las multiples ocurrencias tambien.
        //
        // TODO: Min aqui creo que siempre da 0, verificar.
        int min = cpo.occurrences.getLower()
               
        // PAB:
        // Si no tengo valores retorno null, y verifico si el nodo es obligatorio.
        if (pathValor.size()==0)
        {
            //println "@@@@ @@@@@ @@@@"
            //println "@@@@ FIXME @@@@"
            //println "     VERIFICACION DE ERRORES DE OCURRENCIAS PARA EL ''PrimitiveObject''"
            //println "@@@@ FIXME @@@@"
            //println "@@@@ @@@@@ @@@@"
//            if (min>0)
//            {
//                errors.add(arquetipo.archetypeId.value,
//                        cpo, //cpo.path(),
//                        Errors.ERROR_OCCURRENCES) // TODO: error por tipo rm
//            }
            
            // FIXME: deberia tirar igual el objeto con valor null y dejar que valide el GORM.
            return null
        }
        
        // En lugar de ir a CPrimitive llamo directo al metodo que crea instancias
        //return bindCPrimitive(cpo.item, pathValor, arquetipo, tempId) // TODO item puede ser null
        
        String factoryRMMethod = 'create' + cpo.rmTypeName
        
        if (pathValor.size() == 1)
        {
            def result = []
            
            // FIXME: con que termine depende del tipo que bindeo, es el nombre del atributo, no siempre es 'value'
            // Lo correcto es pedir por la path de este nodo...
            //def values = pathValor.find{it.key.endsWith("value")}?.value
            
            // Pido la unica entrada del mapa
            def values = pathValor.find{ true }.value
            if (values.getClass().isArray()) // Valores multiples
            {
                def valuesList = values as List
                
                // FIXME: corregir chequeo de errores de ocurrencia para multiples valores.
                // FIXME: los valores pueden estar pero ser strings vacios... es lo mismo que si no estan!!!!
                // FIXED: countValues cuenta los valores no nulos.
                // (AAA) verificacion de errores de ocurrencias para cuando hay multiples valores
                
                // VERIFICACION DE ERRORES DE OCURRENCIAS PARA EL PrimitiveObject CASO ''Valores multiples''
//                if ( countValues(valuesList)<min) // hay menos elementos de los que se esperaban.
//                {
//                    errors.add(arquetipo.archetypeId.value,
//                            cpo, //cpo.path(),
//                            Errors.ERROR_OCCURRENCES) // TODO: error por tipo rm
//                }
                
                valuesList.each { value ->
                
                    //println " //////////////////////////////// VALUE 1: '" + value +"'" 
                    
                    // Corregido como se verifican errores de ocurrencias para multiples valores.
                    if (value)
                       result << rmFactory."$factoryRMMethod"(value, arquetipo, cpo.nodeID, tempId, cpo)
                }
            }
            else // Valor simple
            {
                //println " //////////////////////////////// VALUE 2: '" + values + "'" 
                
                if (!values) // Si es vacio o null
                {
                    //println "@@@@ @@@@@ @@@@"
                    //println "@@@@ FIXME @@@@"
                    //println "     VERIFICACION DE ERRORES DE OCURRENCIAS PARA EL PrimitiveObject CASO ''Valores simples''"
                    //println "@@@@ FIXME @@@@"
                    //println "@@@@ @@@@@ @@@@"
//                    if (min > 0)
//                    {
//                        errors.add(arquetipo.archetypeId.value,
//                                cpo, //cpo.path(),
//                                Errors.ERROR_OCCURRENCES) // TODO: error por tipo rm
//                    }
                }
                else
                {
                    // TODO: No deberia tirar exception (antes habia try/catch), deberia validar
                    // el GORM y meter el error en la instancia, VERIFICAR que sea asi!
                    result << rmFactory."$factoryRMMethod"(values, arquetipo, cpo.nodeID, tempId, cpo)
                }
            }
            
            return result // lista de objetos simples rm
        }
        
        // FIXME: Si es quantity no vendria a CPrimitiveObject, seria un DomainType.
        // Si pathValor tiene 2 o mas entradas (caso posible en dates y quantity)
        //throw new Exception("pathValor tiene 2 o mas entradas para este nodo")
        
        // Viejo bindCPrimitive
        String bindMethod = 'bind' + cpo.item.getClass().getSimpleName()
        
        //println "==== bindCPrimitive"
        //println "  == Llama a: " + factoryRMMethod
        //println "  == Type: " + cpo.item.getType()
        //println "===================================="
        
        return this."$bindMethod"(cpo.item, pathValor, arquetipo, tempId)
        
    } // bindCPrimitiveObject


    /**
     * PAB:
     * Nunca se va a invocara este metodo porque CDomainType es abstracta,
     * entonces en bindCObject ningun nodo de entrada es CDomainType, pero
     * si puede ser una de sus subclases concretas.
     * 
     * Bindeo para objetos instancias de CDomainType. Crea un objeto del RM.
     *
     * Llama a la operacion de bindeo correspondiente a la clase concreto de cdt.
     *
     * @return objeto del RM bindeado
     */
    def bindCDomainType(CDomainType cdt, LinkedHashMap<String, Object> pathValorCDomainType, Archetype arquetipo, String tempId)
    {
        //println "bindCDomainType " + cdt.getClass().getSimpleName()
        
        String nombreClase = cdt.getClass().getSimpleName()
        String bindMethod = 'bind' + nombreClase
        //println "nombreClase CDomainType: " + nombreClase
        def rmObject = this."$bindMethod"(cdt, pathValorCDomainType, arquetipo, tempId)
        return rmObject
    }
    
    /**
     * ConstraintRef se usa cuando hay una referencia ed un coded_text a una terminologia externa,
     * como por ejemplo en el caso del arquetipo openEHR-EHR-EVALUATION.alert.v1.adl
     * @param cref
     * @param pathValor
     * @param arquetipo
     * @param tempId
     * @return
     */
    def bindConstraintRef(ConstraintRef cref, LinkedHashMap<String, Object> pathValor, Archetype arquetipo, String tempId)
    {
        /*
        
        Esto bindea para estas retricciones del DV_CODED_TEXT.defining_code:
        
          - definingCode.terminologyId debe venir en el dato ingresado en la UI para bindear, viene:
            - terminologyId::code||text
        
        DV_CODED_TEXT matches {
          defining_code matches {[ac0001]}		-- Nueva restricción
        }
        
        
        Mientras bindCCodePhrase bindea para retricciones del DV_CODED_TEXT.defining_code:
        
          - ver que especifica terminologyId (local), y NO viene desde la UI para bindear, viene:
            - code||text
        
        DV_CODED_TEXT matches {
            defining_code matches {
               [local::
               at0007, 	-- aaa
               at0008]	-- bbb
            }
         }
        
        */
        /*
        println "= = = = = = = = = = = = = = = = = = = = = = = = = = = ="
        println "BindingAOMRM: Metodo bindConstraintRef no implementado"
        println cref
        println cref.path()
        println pathValor
        println pathValor.find{it.key == cref.path()}?.value
        org.codehaus.groovy.runtime.StackTraceUtils.sanitize(new Exception()).printStackTrace(System.out)
        println "= = = = = = = = = = = = = = = = = = = = = = = = = = = ="
        */
        
        // Unico caso por ahora es que la constraintRef acNNNN este dentro
        // de un DV_CODED_TEXT para su CodePhrase.
        if (cref.rmTypeName == 'CodePhrase')
        {
           // idem bindCCodePhrase
           if (pathValor.size() == 0)
           {
               return null
           }
           
           // Size puede ser 1 pero puedo tener multiples valores....
           if (pathValor.size() == 1)
           {
               def result = []
               def values = pathValor.find{it.key == cref.path()}?.value
               if (values.getClass().isArray()) // Valores multiples
               {
                   (values as List).each { value ->
                   
                       // FIXME: creo que deberia tirar el objeto con valor null para que valide el
                       //        GORM y luego el ELEMENT.
                       if (value)
                           result << rmFactory.createCodePhrase(cref, value, arquetipo, cref.nodeID, tempId, cref)
                   }
               }
               else // Valor simple
               {
                   // FIXME: creo que deberia tirar el objeto con valor null para que valide el
                   //        GORM y luego el ELEMENT.
                   if (values)
                       result << rmFactory.createCodePhrase(cref, values, arquetipo, cref.nodeID, tempId, cref)
               }
               
               return result
           }

           throw new Exception("bindConstraintRef: Colección de pathValor tiene mas de una path.")
        }
        
        return null
    }

    /**
     * Bindeo para objetos instancias de ArchetypeSlot. Agrega un elemento
     * al mapeo mapaRefArchSlot que mapea id de arquetipo con objeto del RM
     * en donde deberia bindearse el arquetipo.
     *
     * Luego de finalizado el bindeo del template, la operacion completarRMOAS
     * se encarga de armar el arbol del RM completo para el template tomando en cuenta
     * el mapeo mapaRefArchSlot.
     */
    def bindArchetypeSlot(ArchetypeSlot cdt, LinkedHashMap<String, Object> pathValorArchetypeSlot, Archetype arquetipo, String tempId)
    {
        //println "==== bindArchetypeSlot"
        
        //String archetypeRefId
        String type
        String idMatchingKey
        
        def cantBind = false
        
        Set<Assertion> conjAssertion = cdt.includes
        conjAssertion.each{eachAssertion ->

            def item = eachAssertion.expression.rightOperand.item
            // Construyo id de arquetipo
            if (item.type == "String")
            {
                idMatchingKey = ((CString)(item)).pattern
                
                println ""
                println "idMatchingKey: "+ idMatchingKey
                println ""
                
                // Sino se definen que arquetipos se pueden cargar, no se puede bindear.
                cantBind = (idMatchingKey == '.*')
                
                //  CLUSTER\.level_of_exertion(-[a-zA-Z0-9_]+)*\.v1
                //println "  xxx> "+ (((CString)(item)).pattern - "openEHR-EHR-")
                //println "  zzz> "+ (((CString)(item)).pattern - "openEHR-EHR-").replace("\\", "").split("\\.")
                
                type = (((CString)(item)).pattern - "openEHR-EHR-").replace("\\", "").split("\\.")[0]
                // Me quedo con el tipo del arquetipo. (openEHR-EHR-OBSERVATION.prueba6.v1 -> OBSERVATION)
                // siendo ((CString)(item)).pattern igual a openEHR-EHR-OBSERVATION.prueba6.v1
                // Le quito el comienzo: "openEHR-EHR-"
                // Luego me queda OBSERVATION.prueba6.v1
                // .replace("\\", "") porque hay caracteres escapados, el . esta como \.
                // A esto le hago un split por el "." y me quedo con el primer string: OBSERVATION
            }
        }
        
        if (cantBind)
        {
           log.error 'idMatchingKey es la regex .*, se necesita una regex especifica para saber que arquetipos cargar'
           return null
        }

        // FIXME: Que pasa si no encuentra type y matchingKey?
        
        println "==== Slot Type: " + type
        //println "==== IdMatchingKey: " + idMatchingKey
        //println "==============================================="
        //println ""
        
        // Otra opcion para lo anterior
        //    String expReg = asser.expression.rightOperand.item.pattern
        //    def manager = ArchetypeManager.getInstance()
        //    def refArchetype = manager.getArchetype(expReg) // FIXME

        // Obtengo el arquetipo referenciado
        def manager = ArchetypeManager.getInstance()
        
        // FIXME: podria obtener varios arquetipos.
        def arqRef = manager.getArchetype(type, idMatchingKey) // Obtengo el arquetipo que machea con idMatchingKey siendo de tipo type

        
        // FIXME: que pasa si no encuentra arqRef?
        // Sino hay arqRef es porque no hay un arquetipo en el repo de arquetipos que matchee
        // con la referencia, entonces no se puede crear un objeto para ese arquetipo
        if (!arqRef) return null
        
        
        // Obtengo el rmTypeName de la raiz del arquetipo
        CComplexObject cco = arqRef.getDefinition()
        String tipoRM = cco.rmTypeName

        //println "==== tipoRM: " + tipoRM
        
        // Creo un objeto del RM del tipo indicado por rmTypeName
        // FIXME: para que se crea un objeto vacio? no alcanza con guardar la path? o path y tipo?
        def rmObject = rmFactory.createLOCATABLE(tipoRM, cco.nodeID, arqRef, tempId, cco) // Ahora le paso los datos para que pueda completar el locatable con los archetypeDetails, los datos son del arquetipo referenciado, no del que estoy recorriendo.

        //println "==== guardo slot a: " + rmObject
        //println "==== desde el slot: " + cdt.path()
        
        //this.mapaRefArchSlot.put(arqRef.archetypeId.value, rmObject)

// SOLUCION INICIAL DE SLOT
//        this.mapaRefArchSlot[arqRef.archetypeId.value] = rmObject
        
        // temporal para que el nodo padre CComplexObject reclame el slot como suyo
        // FIXME: si es un atributo multiple, una unica path seria la misma para varios elementos,
        //        por lo que el segundo elemento que tenga misma cdt.path sobreescribe el primero.
        //
        // BUG: Parece ser un bug del adl parser que para el SECTION.items tieme path /items,
        //      y para cada children de ese atributo tiene la misma path /items, en lugar de
        //      /items[slotNodeID]. Esto pasa en el caso de la SECTION con slots, hay que probar
        //      otros casos.
        //      Hago un fix rapido agregando el slotNodeId a la path como clave del map tempSlotRefs.
        //
        //this.tempSlotRefs[ cdt.path() ] = arqRef // Como deberia ser si el slot tiviera bien la path.
        this.tempSlotRefs[ cdt.path() + "["+ cdt.nodeID +"]" ] = arqRef // FIX de Pablo.
        // Tambien necesito el slot para pedirle el parent attr, asi que hago otra estructura para poder verlo.
        this.tempSlotRefsSlots[ cdt.path() + "["+ cdt.nodeID +"]" ] = cdt // FIX de Pablo.
        
        // WARNING! (***)
        // ESTA ES LA CLAVE DEL ENGANCHE DE LAS ESTRUCTURAS REFERENCIADAS CON SLOTS.
        // Al retornar el objeto vacio, se lo setea a la section que tenia
        // el slot, y como mapaRefArchSlot tiene un puntero a este, luego 
        // cuando se clona el objeto referenciado en este objeto en el 
        // mapaRefArchSlot desde completarRMOAS, la referencia desde el 
        // slot se actualiza y el objeto sale completo.
        return rmObject
        
    } // bindArchetypeSlot

    
    /**
     * Bindeo para objetos instancias de ArchetypeInternalRef. 
     * TODO: explicar que hace y como.
     */
    def bindArchetypeInternalRef(ArchetypeInternalRef air, LinkedHashMap<String, Object> pathValorInternalRef, Archetype arquetipo, String tempId)
    {
        LinkedHashMap<String, Object> pathValorIR = pathValorInternalRef.findAll{it.key.startsWith(air.path())}

        // Quito de las path la parte correspondiente a la path al internal ref
        LinkedHashMap<String, Object> pathValor = new LinkedHashMap<String, Object>()
        pathValorIR.each{it ->
            // Si la path es una path que referencia al target path la incluyo
            if (it.key.contains(air.targetPath)){
                pathValor.put(it.key.replace(air.path(), ""), it.value)
            }
        }

        def co = arquetipo.node(air.targetPath)
        def rmObject = bindCObject(co, pathValor, arquetipo, tempId)

        //println "---bindArchetypeInternalRef: rmObject: " + rmObject
        
        // PAB:
        // Si bindCObject devuelve una lista, completar locatable espera un objeto locatable
        // asi que va a tirar una except...
        // completarLocatable(Locatable locatable, String archNodeId, Archetype archetype, String tempId)
//        FactoryObjectRM.getInstance().completarLocatable(rmObject, arquetipo.node(air.targetPath).nodeID , arquetipo, tempId)

        return rmObject
        
    } // bindArchetypeInternalRef

    
    //-----------------------------------------------------------------------------
    // CPrimitive
/* a estas ya no se llama.
    def bindCBoolean(CBoolean cb, LinkedHashMap<String, Object> pathValorCBoolean, Archetype arquetipo, String tempId){

        if (pathValorCBoolean.size() == 0){
            return null // No hay valores para este 
        }
        if (pathValorCBoolean.size() == 1){
            //return new DvBoolean(value: Boolean.parseBoolean(pathValorCBoolean.find{true}.value))
            return new Boolean(Boolean.parseBoolean(pathValorCBoolean.find{true}.value))
        }
        
        throw new Exception("bindCBoolean: Colección de pathValor tiene mas de un elemento.")
    }

    def bindCDate(CDate cd, LinkedHashMap<String, Object> pathValorCDate, Archetype arquetipo, String tempId)
    {
        // Encuentro los valores en la coleccion de path valor
        String year = pathValorCDate.find{it.key.endsWith("year")}?.value
        String month = pathValorCDate.find{it.key.endsWith("month")}?.value
        String day = pathValorCDate.find{it.key.endsWith("day")}?.value

        if ((year != null) && (month != null) && (day != null))
        {
            // Creo un string con formato ISO 8601
            String fechaISO8601 = FactoryObjectRM.crearFechaISO8601(year, month, day, "", "", "")
            //return new DvDate(value: fechaISO8601)
            return fechaISO8601
        }
        else
        {
            return null
            //throw new Exception("bindCDate: Colección de pathValor no tiene path a 'year' o 'month' o 'day'.")
        }
    }

    def bindCDateTime(CDateTime cdt, LinkedHashMap<String, Object> pathValorCDateTime, Archetype arquetipo, String tempId){
        // Encuentro los valores en la coleccion de path valor
        String year = pathValorCDateTime.find{it.key.endsWith("year")}?.value
        String month = pathValorCDateTime.find{it.key.endsWith("month")}?.value
        String day = pathValorCDateTime.find{it.key.endsWith("day")}?.value
        String hour = pathValorCDateTime.find{it.key.endsWith("hour")}?.value
        String minute = pathValorCDateTime.find{it.key.endsWith("minute")}?.value
        String seg = pathValorCDateTime.find{it.key.endsWith("seg")}?.value

        if ((year != null) && (month != null) && (day != null) && (hour != null)){
            // Creo un string con formato ISO 8601
            String fechaISO8601 = FactoryObjectRM.crearFechaISO8601(year, month, day, hour, minute, seg)
            return fechaISO8601
        }
        else{
            return null
            //throw new Exception("bindCDateTime: Colección de pathValor no tiene path a 'year' o 'month' o 'day' u 'hour'.")
        }

    }

    def bindCDuration(CDuration cd, LinkedHashMap<String, Object> pathValorCDuration, Archetype arquetipo, String tempId)
    {
        // Encuentro los valores correspondientes al dateTime inicial en la coleccion de path valor
        String yearIni = pathValorCDuration.find{it.key.endsWith("initialValue_year")}?.value
        String monthIni = pathValorCDuration.find{it.key.endsWith("initialValue_month")}?.value
        String dayIni = pathValorCDuration.find{it.key.endsWith("initialValue_day")}?.value
        String hourIni = pathValorCDuration.find{it.key.endsWith("initialValue_hour")}?.value
        String minuteIni = pathValorCDuration.find{it.key.endsWith("initialValue_minute")}?.value
        String segIni = pathValorCDuration.find{it.key.endsWith("initialValue_seg")}?.value

        // Encuentro los valores correspondientes al dateTime final en la coleccion de path valor
        String yearFin = pathValorCDuration.find{it.key.endsWith("finalValue_year")}?.value
        String monthFin = pathValorCDuration.find{it.key.endsWith("finalValue_month")}?.value
        String dayFin = pathValorCDuration.find{it.key.endsWith("finalValue_day")}?.value
        String hourFin = pathValorCDuration.find{it.key.endsWith("finalValue_hour")}?.value
        String minuteFin = pathValorCDuration.find{it.key.endsWith("finalValue_minute")}?.value
        String segFin = pathValorCDuration.find{it.key.endsWith("finalValue_seg")}?.value

        if ((yearIni != null) && (yearFin != null)){
            // Creo un string con formato ISO 8601
            String fechaISO8601Ini = FactoryObjectRM.crearFechaISO8601(yearIni, monthIni, dayIni, hourIni, minuteIni, segIni)
            String fechaISO8601Fin = FactoryObjectRM.crearFechaISO8601(yearFin, monthFin, dayFin, hourFin, minuteFin, segFin)
            return fechaISO8601Ini + "/" + fechaISO8601Fin
        }
        else{
            return null
            //throw new Exception("bindCDuration: Colección de pathValor no tiene path a 'inicialValue_year' o 'finalValue_month' o 'day' u 'hour'.")
        }
        
        //TODO
    }

    //-----------------------------------------------------------------------------
    // PAB: este es mas del factoryRM que del binder.
    def bindCInteger(CInteger ci, LinkedHashMap<String, Object> pathValor, Archetype arquetipo, String tempId)
    {
        println "==== bindCInteger"
        println "  == pathValor: "+ pathValor
        println "===================================="
        
        // No hay valores para este nodo 
        if (pathValor.size() == 0) return null 
        
        // FIXME: Puede haber una path pero multiples valores...
        if (pathValor.size() == 1)
        {
            // Si viene un valor pero no es un entero valido
            try
            {
                return new Integer(Integer.parseInt(pathValor.find{true}.value)) // find(true) retorna el primer elemento de la coleccion (en este caso deberia haber un solo elemento)
            }
            catch (Exception e)
            {
                // ERROR: no se pudo parsear el valor
                // TODO: podria preguntar por el tipo de la except... igual se que la except es por el mal parseo... java.lang.NumberFormatException
                
                errors.add(arquetipo.archetypeId.value,
                            ci, //ci.path(),
                            Errors.ERROR_BAD_FORMAT) // TODO: error por tipo rm
                
                return null
            }
        }

        // Para los CPrimitives no puede llegar un valor multiple, el multiple debe ser en el nivel mas bajo un ELEMENT
        throw new Exception("bindCInteger: Colección de pathValor tiene mas de un elemento.")
        
    } // bindCInteger

    def bindCReal(CReal cr, LinkedHashMap<String, Object> pathValorCReal, Archetype arquetipo, String tempId)
    {
        throw new Exception("bindCReal: Esta funcion no se deberia llamar ya que no deberian haber valores reales fuera de un quantity.")
    }

    def bindCString(CString cs, LinkedHashMap<String, Object> pathValorCString, Archetype arquetipo, String tempId)
    {
        println "==== bindCString"
        println "   = pathValor: " + pathValorCString
        println "   = tipos de valores: "
        pathValorCString.each {
            print it.value.getClass().toString() + ", "
        }
        println ""
        println "==========================================="
        
        if (pathValorCString.size() == 0)
        {
            return null // No hay valores para este nodo
        }
        if (pathValorCString.size() == 1)
        {
            //return new DvText(value: pathValorCString.find{true}.value)
            return new String(pathValorCString.find{true}.value)
        }
        
        throw new Exception("bindCString: Colección de pathValor tiene mas de un elemento.")
    }

    def bindCTime(CTime ct, LinkedHashMap<String, Object> pathValorCTime, Archetype arquetipo, String tempId)
    {
        // Encuentro los valores en la coleccion de path valor
        String hour = pathValorCTime.find{it.key.endsWith("hour")}?.value
        String minute = pathValorCTime.find{it.key.endsWith("minute")}?.value
        String seg = pathValorCTime.find{it.key.endsWith("seg")}?.value

        if (hour != null){
            // Creo un string con formato ISO 8601
            // FIXME: o usamos o metodos estaticos o el singleton para no tener 2 criterios distintos para hacer la misma cosa.
            String fechaISO8601 = FactoryObjectRM.crearFechaISO8601("", "", "", hour, minute, seg)
            return fechaISO8601
        }
        else{
            return null
            //throw new Exception("bindCTime: Colección de pathValor no tiene path a 'hora'.")
        }
    }
*/
    
    //-----------------------------------------------------------------------------
    // CDomainType
    // Todos los bindeos de los tres tipos de CDomineType tienen el control de obligatoriedad
    //
    // PAB: ahora tira una lista de Ordinals con mas de un elemento si vienen valores multiples.
    //
    def bindCDvOrdinal(CDvOrdinal cdvo, LinkedHashMap<String, Object> pathValorCDvOrdinal, Archetype arquetipo, String tempId)
    {
        def result = []
        
        //println "==== bindCDvOrdinal"
        //println "   = pathValor: " + pathValorCDvOrdinal
        //println "   = tipos de valores: "
        //pathValorCDvOrdinal.each {
        //    print it.value.getClass().toString() + ", "
        //}
        //println ""
        //println "==========================================="

        if (pathValorCDvOrdinal.size() == 0)
        {
            // La verificacion de ocurrencias la debe hacer el element.
            
            // FIXME:
            // Los CDvQuantity no tienen nodeID, llegan hasta el ELEMENT que los contiene.
            // Esto pasa con todos los nodos simples, en estos casos no es necesario pasar xx.nodeID a la factory.
            //println "xxxxxxxxxx"
            //imprimirObjetoXML(cdvo)
            //println cdvo.nodeID
            //println arquetipo.node(cdvo.path()).nodeID
            //println "yyyyyyyyyy"
            
            // FIX: para qe valide el triage, que si no meto valor, no muestra error
            //      esto es analogo a lo que se hace en bindDV_TEXT
            //return null
            
            def rmObj = rmFactory.createDvOrdinal(cdvo, null, arquetipo, cdvo.nodeID, tempId, cdvo)
            
            // FIXME: esto deberia estar en factory!
            rmObj.errors.rejectValue('value', 'error.empty') // errorCode
            // TODO: verificar si alguien verrifica este error, sino marcarlo aca.
            
            result << rmObj
            
            return result
        }
        
        // Size puede ser 1 pero puedo tener multiples valores....
        if (pathValorCDvOrdinal.size() == 1)
        {
            // ERROR: en a1_a2 me da un array no un string aca porque hay un CDvOrdinal multiple.
            //String v = pathValorCDvOrdinal.find{it.key.endsWith("value")}?.value
            
            //def values = pathValorCDvOrdinal.find{it.key.endsWith("value")}?.value // la path de CDvOrdinal termina con /value
            // Busqueda mas sencilla, porque si estoy en este metodo es que la path me corresponde, no es necesario verificar que
            // termina con 'value' (a no ser que quien me llame no haga la verificacion! => TODO: verificar esto).
            def values = pathValorCDvOrdinal.find{true}?.value // la path de CDvOrdinal termina con /value
            if (values.getClass().isArray()) // Valores multiples
            {
                (values as List).each { value ->
                
                    //println " //////////////////////////////// VALUE 1: '" + value +"'" 
                
                    // FIXME: corregir chequeo de ocurrencias para multiples valores como esta en bindCComplexObject caso SA.
                    
                    if (!value) // Si es vacio o null
                    {
                        // FIXME: como hay multiples valores para la path, si el valor es requerido para cada instancia,
                        //        este error deberia aparecer una vez por cada uno de los valores que vinieron vacios,
                        //        de este modo un error sobreescribe el otro y si hay varios solo se registra uno y no
                        //        se puede distinguir cual. Podria hacerse en la vuelta a guigen, viendo si es que hay
                        //        multiples valores y un solo error, verificar cada valor vacio en params, para la path,
                        //        y mostrar el error para cada valor vacio.
                        //        Otra forma seria aqui registrar el error y el indice del valor para el cual dio error,
                        //        asi si vienen 5 valores y 2 son vacios, registro que el 2 y el 4 son los que estan vacios.
                        /* El ERROR DE OCURRENCIAS LO DEBERIA VERIFICAR EL ELEMENT donde se meten los valores bindeados aca.
                        if (min > 0)
                        {
                            errors.add(arquetipo.archetypeId.value,
                                    cdvo, //cdvo.path(),
                                    Errors.ERROR_OCCURRENCES) // TODO: error por tipo rm
                        }
                        */
                    }
                    else
                        result << rmFactory.createDvOrdinal(cdvo, value, arquetipo, arquetipo.node(cdvo.path()).nodeID, tempId, cdvo)
                }
            }
            else // Valor simple
            {
                //println " //////////////////////////////// VALUE 2: '" + values + "'" 
                
                if (!values) // Si es vacio o null
                {
                    /* TODO: Los errores de ocurrencias los deberia validar el ELEMENT
                    if (min > 0)
                    {
                        errors.add(arquetipo.archetypeId.value,
                                cdvo, //cdvo.path(),
                                Errors.ERROR_OCCURRENCES) // TODO: error por tipo rm
                    }
                    */
                }
                else
                    result << rmFactory.createDvOrdinal(cdvo, values, arquetipo, arquetipo.node(cdvo.path()).nodeID, tempId, cdvo)
            }

            return result
        }

        throw new Exception("bindCDvOrdinal: Colección de pathValor tiene mas de una path.")
        
    } // bindCDvOrdinal

    def bindCDvQuantity(CDvQuantity cdvc, LinkedHashMap<String, Object> pathValor, Archetype arquetipo, String tempId)
    {
        //println "== bindCDvQuantity"
        //println "==== pathValor: " + pathValor

        def result = []

        // Valores potencialmente multiples
        def magnitudes
        def unidades
        def pathMagnitudes
        def pathUnidades
        
        // FIXME: arreglar este codigo que se repiten muchas lineas, tratar de reutilizar codigo.
        
        if (pathValor.size()==0)
        {
            // Creo objeto vacio para que valide el GORM
            result << rmFactory.createDvQuantity(null, null, arquetipo, cdvc.nodeID, tempId, cdvc)
        }
        else if ((pathValor.size() == 1) || (pathValor.size() == 2))
        {
           // Vienen magnitudes o unidades, tengo que ver cual
           magnitudes = pathValor.find{it.key.endsWith("magnitude")}?.value
           pathMagnitudes = pathValor.find{it.key.endsWith("magnitude")}?.key
           unidades = pathValor.find{it.key.endsWith("units")}?.value
           pathUnidades = pathValor.find{it.key.endsWith("units")}?.key
           
           // Si solo vienen magnitudes, tengo que sacar las unidades del arquetipo.
           // O lo que es lo mismo: si no vienen unidades...
           if (!unidades)
           {
               // LEA: Un quantity siempre debe tener unidad, pero se puede definir un quantity sin unidad.
               // PAB: Tomamos como convencion que siempre definimos en el arquetipo la unidad.
               if (cdvc.list)
               {
                   if (cdvc.list.size() == 1) // Obtengo la unica unidad del arquetipo
                       unidades = cdvc.list[0].units
                   else
                   {
                      // Hay muchas unidades definidas en el arquetipo, no se cual elegir...
                      // Le pongo null y que valide luego...
                      unidades = null
                   }
               }
               else
               {
                   // FIXME: siempre debe haber unidad definida en el arquetipo
                   throw new Exception("No se definio la unidad de CDvQuantity "+ cdvc.path() +" en arquetipo: " + arquetipo.archetypeId.value)
               }
           }
           
           /* REESCRIBO EL CODIGO DE ABAJO!
            // Si viene una sola path (puede ser units o magnitude) y pueden ser muchos valores 
            if (pathValor.size() == 1) // Si el Quantity tiene una sola unidad, esta no viene pathValorCDvQuantity, y hay que obtenerla de la definicion del arquetipo
            {
                // Vienen magnitudes o unidades, tengo que ver cual
                / *
                magnitudes = pathValor.find{it.key.endsWith("magnitude")}?.value
                pathMagnitudes = pathValor.find{it.key.endsWith("magnitude")}?.key
                unidades = pathValor.find{it.key.endsWith("units")}?.value
                pathUnidades = pathValor.find{it.key.endsWith("units")}?.key
                * /
                
                // Si vienne solo unidades, el GORM se encarga de validar y va a dar que falta el
                // valor para la magnitud de las quantities bindeadas.
                
                // Si solo vienen magnitudes, tengo que sacar las unidades del arquetipo.
                if (magnitudes)
                {
                    List<CDvQuantityItem> listQI = cdvc.list // Saca las unidades del arquetipo, solo sirve si hay una unica unidad...
                    
                    // LEA: Un quantity siempre debe tener unidad, pero se puede definir un quantity sin unidad.
                    // PAB: Tomamos como convencion que siempre definimos en el arquetipo la unidad.
                    //
                    if (listQI)
                    { 
                        if (listQI.size()==1) // Obtengo la unica unidad del arquetipo
                            unidades = listQI[0].units
                        else
                        {
                           // FIXME: aca deberia poner un error en el dvquantity para el campo units
                           unidades = null // Le pongo null y que valide luego
                           //throw new Exception("No viene la unidad de la web y el arquetipo "+ arquetipo.archetypeId.value +" define muchas unidades para CDvQuantity: "+ cdvc.path())
                        }
                    }
                    else
                    {
                        // FIXME: siempre debe haber unidad definida en el arquetipo
                        throw new Exception("No se definio la unidad de CDvQuantity "+ cdvc.path() +" en arquetipo: " + arquetipo.archetypeId.value)
                    }
                }
            }
            else if (pathValor.size() == 2)
            {
                / *
                magnitudes = pathValor.find{it.key.endsWith("magnitude")}?.value
                pathMagnitudes = pathValor.find{it.key.endsWith("magnitude")}?.key
                unidades = pathValor.find{it.key.endsWith("units")}?.value
                pathUnidades = pathValor.find{it.key.endsWith("units")}?.key
                * /
            }
            */
            
            // dejo que pase aunque no tenga valores para que el GORM valide
            if (magnitudes && unidades)
            {
                // Si hay valores multiples
                if (magnitudes?.getClass().isArray() && unidades?.getClass().isArray())
                {
                    //println "???????????? MAGNITUDES y UNIDADES MULTIPLES"
                    
                    def index = 0 // Para iterar por los valores multiples
                    def termino = false
                    def magnitud = null
                    def unidad = null
                    while (!termino)
                    {
                        // FIXME: porque itera por pathValor si no uso entry y uso magnitures y unidades?
                        // ADemas itero por pathValor y el valro que asigna siempre es el mismo! porque depende de index no de pathValor!!
                        //pathValor.each { entry ->
                            
                            if (index < magnitudes.length) // ambos arrays deberian tener el mismo largo
                            {
                                magnitud = magnitudes[index]
                                unidad = unidades[index]
                            }
                        //}
                        
                        // Si no tengo mas valores para bindear => termino...
                        termino = (!magnitud && !unidad)
                        
                        if (!termino) // en la ultima vuelta me aseguro de no bindear nada...
                        {
                            //////////////////////////////////////////////////////////////////////////////
                            // FIXME: se deberia marcar el error de nulo solo si el dato es obligatorio.
                            // Ahora lo que pasa es que si no viene valor, lo toma como un error de formato
                            // porque lo que viene en realidad es un string.
                            // Funcionaria convertir los strings vacios en nulls!
                            
                            // dejo que valide el GORM
                            // Pruebo pasarle el string para que la validacion de grails haga el trabajo
                            // de chequear y reportar el error, si es que hay.
                            def rmObj = rmFactory.createDvQuantity(magnitud, unidad, arquetipo, cdvc.nodeID, tempId, cdvc)

                            // ABC*
                            // prueba para retornar null de createDvQuantity
                            if (rmObj)
                            {
                                // VERIFICACION DE ERRORES DE RANGO.
                                // Si el objeto se crea correctamente, verifico la restriccion de rango del arquetipo, si es que la hay.
                                if (rmObj.errors.hasErrors())
                                {
                                   //println "bindCDvQuantity hasErrors 1"
                                   //this.hasErrors = true
                                   
                                   // FIXME: si el ELEMENT es opcional, y el error es de incompleto, no deberia dejar el error en errors.
                                   //        si es incompleto porque vino la unidad y no la magnitud, y hay una sola unidad, viene la unidad
                                   //        porque se pone como hidden en la vista, no deberia mostrar un error por no poner la magnitud,
                                   //        porque tampoco pone la unidad, se pone sola.
                                   // FIXME: la path completa seria archetypeId+refPath+path
                                   //        falta calcular refPath como esta calculada en las vistas
                                   //this.errors[arquetipo.archetypeId.value + cdvc.path()] = rmObj.errors
                                   this.addErrors(arquetipo.archetypeId.value + cdvc.path(), rmObj.errors)
                                }
                                result << rmObj
                            }

                            index++
                        }
                        
                        magnitud = null
                        unidad = null
                        
                    } // while ! termino
                }
                else if (magnitudes?.getClass().isArray() && unidades instanceof String) // Si hay varias magnitudes pero una sola unidad.
                {
                    //println "???????????? MAGNITUDES MULTIPLES"
                            
                    def unidad = unidades // es un valor simple
                    // Este loop es como el anterior de while !termino
                    (magnitudes as List).each { magnitud ->
                        
                        // dejo que valide el GORM
                        // Pruebo pasarle el string para que la validacion de grails haga el trabajo
                        // de chequear y reportar el error, si es que hay.
                        def rmObj = rmFactory.createDvQuantity(magnitud, unidad, arquetipo, cdvc.nodeID, tempId, cdvc)
                        
                        // ABC*
                        // prueba para retornar null de createDvQuantity
                        if (rmObj)
                        {
                            // VERIFICACION DE ERRORES DE RANGO.
                            // Si el objeto se crea correctamente, verifico la restriccion de rango del arquetipo, si es que la hay.
                            if (rmObj.errors.hasErrors())
                            {
                                // println "bindCDvQuantity hasErrors 2"
                                //this.hasErrors = true
                                
                                // FIXME: si el ELEMENT es opcional, y el error es de incompleto, no deberia dejar el error en errors.
                                //        si es incompleto porque vino la unidad y no la magnitud, y hay una sola unidad, viene la unidad
                                //        porque se pone como hidden en la vista, no deberia mostrar un error por no poner la magnitud,
                                //        porque tampoco pone la unidad, se pone sola.
                                // FIXME: la path completa seria archetypeId+refPath+path
                                //        falta calcular refPath como esta calculada en las vistas
                                //this.errors[arquetipo.archetypeId.value + cdvc.path()] = rmObj.errors
                                this.addErrors(arquetipo.archetypeId.value + cdvc.path(), rmObj.errors)
                            }
                            result << rmObj
                        }
                    }
                }
                else if (magnitudes instanceof String && unidades instanceof String) // Si ambos son simples
                {
                    //println "???????????? AMBOS VALORES SIMPLES"
                    def magnitud = magnitudes
                    def unidad = unidades
                    
                    // dejo que valide el GORM
                    // Pruebo pasarle el string para que la validacion de grails haga el trabajo
                    // de chequear y reportar el error, si es que hay.
                    def rmObj = rmFactory.createDvQuantity(magnitud, unidad, arquetipo, cdvc.nodeID, tempId, cdvc)

                    // ABC*
                    // prueba para retornar null de createDvQuantity
                    if (rmObj)
                    {
                        // VERIFICACION DE ERRORES DE RANGO.
                        // Si el objeto se crea correctamente, verifico la restriccion de rango del arquetipo, si es que la hay.
                        if (rmObj.errors.hasErrors())
                        {
                            //println "bindCDvQuantity hasErrors 3"
                            //this.hasErrors = true
                            // FIXME: si el ELEMENT es opcional, y el error es de incompleto, no deberia dejar el error en errors.
                            //        si es incompleto porque vino la unidad y no la magnitud, y hay una sola unidad, viene la unidad
                            //        porque se pone como hidden en la vista, no deberia mostrar un error por no poner la magnitud,
                            //        porque tampoco pone la unidad, se pone sola.
                            // FIXME: la path completa seria archetypeId+refPath+path
                            //        falta calcular refPath como esta calculada en las vistas
                            //this.errors[arquetipo.archetypeId.value + cdvc.path()] = rmObj.errors
                            this.addErrors(arquetipo.archetypeId.value + cdvc.path(), rmObj.errors)
                        }
                        result << rmObj
                    }
                }
                else
                {
                   println "bindCDvQuantity: caigo en el caso imposible: mag="+magnitudes+", units="+units
                }
                // No hay otro caso posible
            }
            else // alguno de mag o unit es vacio, creo el elemento igual para que valide el GORM. 
            {
                // FIXME: esto no funciona si uno es null y otro un array!!!!
                def rmObj = rmFactory.createDvQuantity(magnitudes, unidades, arquetipo, cdvc.nodeID, tempId, cdvc)
                if (rmObj)
                {
                   if (rmObj.errors.hasErrors())
                   {
                      //println "bindCDvQuantity hasErrors 3"
                      //this.hasErrors = true
                      
                      // FIXME: si el ELEMENT es opcional, y el error es de incompleto, no deberia dejar el error en errors.
                      //        si es incompleto porque vino la unidad y no la magnitud, y hay una sola unidad, viene la unidad
                      //        porque se pone como hidden en la vista, no deberia mostrar un error por no poner la magnitud,
                      //        porque tampoco pone la unidad, se pone sola.
                      // FIXME: la path completa seria archetypeId+refPath+path
                      //        falta calcular refPath como esta calculada en las vistas
                      //this.errors[arquetipo.archetypeId.value + cdvc.path()] = rmObj.errors
                      this.addErrors(arquetipo.archetypeId.value + cdvc.path(), rmObj.errors)
                   }
                   result << rmObj
                }
            }
        }
        else
        {
            // FIXME: no tirar excepcion, devolver null y hacer un log a disco o mandar por mail.
            throw new Exception("Error en bindCDvQuantity, hay mas de 2 paths para este nodo... hay: " + pathValor.size())
        }
        
        return result
        
    } // bindCDvQuantity

    def bindCCodePhrase(CCodePhrase ccp, LinkedHashMap<String, Object> pathValorCCodePhrase, Archetype arquetipo, String tempId)
    {
        println "==== bindCCodePhrase "+ pathValorCCodePhrase
        //println "   = pathValor: " + pathValorCCodePhrase
        //println "   = tipos de valores: "
        //pathValorCCodePhrase.each {
        //    print it.value.getClass().toString() + ", "
        //}
        //println ""
        //println "==========================================="
        
        //def min = ccp.occurrences.getLower()

        // FIXME: verificar ocurrencias como en CComplexObject.
        if (pathValorCCodePhrase.size() == 0)
        {
            /* Los errores de ocurrencias los deberia validar el element
            if (min > 0)
            {
                errors.add(arquetipo.archetypeId.value,
                            ccp, //ccp.path(),
                            Errors.ERROR_OCCURRENCES) // TODO: error por tipo rm
            }
            */
            // FIXME: creo que deberia tirar el objeto con valor null para que valide el
            //        GORM y luego el ELEMENT.
            return null
        }
        
//      Size puede ser 1 pero puedo tener multiples valores....
        if (pathValorCCodePhrase.size() == 1)
        {
            def result = []
            def values = pathValorCCodePhrase.find{it.key.endsWith("defining_code")}?.value
            if (values.getClass().isArray()) // Valores multiples
            {
                (values as List).each { value ->
                
                    //println " //////////////////////////////// VALUE multiple: '" + value +"'" 
                    
                    // FIXME: creo que deberia tirar el objeto con valor null para que valide el
                    //        GORM y luego el ELEMENT.
                    if (value)
                        result << rmFactory.createCodePhrase(ccp, value, arquetipo, ccp.nodeID, tempId, ccp)
                }
            }
            else // Valor simple
            {
                //println " //////////////////////////////// VALUE simple: '" + values + "'" 
                
                // FIXME: creo que deberia tirar el objeto con valor null para que valide el
                //        GORM y luego el ELEMENT.
                if (values)
                    result << rmFactory.createCodePhrase(ccp, values, arquetipo, ccp.nodeID, tempId, ccp)
            }
            
            return result
        }

        throw new Exception("bindCCodePhrase: Colección de pathValor tiene mas de una path.")
        
    } // bindCCodePhrase


    /**
     * Operaion auxiliar para testing.
     * @param o objeto a mostrar en XML.
     */
    void imprimirObjetoXML(Object o)
    {
        println "-----------------"
        XStream xstream = new XStream()
        println xstream.toXML(o)
        println "-----------------"
    }
    
    // =============================================================
    // ===================== Bindeo de representation.Item =========
    // =============================================================
    def bindCLUSTER(CComplexObject cco, LinkedHashMap<String, Object> pathValor, Archetype arquetipo, String tempId)
    {
        //println "== bindCLUSTER"
        //println "==== pathValor: " + pathValor
        //println "============================================================="
        def result = [] // lista de clusters
        
        // Si pueden haber muchas ocurrencias del cluster
        if (cco.occurrences.isUpperUnbounded() || cco.occurrences.getUpper() > 1)
        {
            // Si viene un solo valor para cada path es como el caso en que el cluster no tiene ocurrencias multiples
            
            if (pathValor.every{it.value instanceof String}) // Todos los valores son simples, copio el codigo de abajo...
            {
                //println "MULTIPLES OCURRENCIAS, PERO NO HAY VALORES MULTIPLES"
               
                // TODO: verificar que hay algun valor para las paths a bindear, si no hay valores, no deberia bindear
                if (pathValor.find{it.value != ''})
                    result << bindSingleCluster(cco, pathValor, arquetipo, tempId)               
            }
            else // Hay por lo menos una path que tiene multiples valores
            {
                //println "MULTIPLES OCURRENCIAS, Y HAY VALORES MULTIPLES"
                
                // El criterio es "mandar" de a UN valor de las path multiples para crear UN Cluster para
                // cada "mandada", pero si el valor de una path es simple, solo se "manda" para el primer
                // cluster o sea:
                // [p1 => v1, p2 => [v21, v22], p3 => [v31, v32, v33] ]
                // Para crear el primer cluster uso subPathValor:  [p1=>v1, p2=>v21, p3=>v31]
                // Para crear el segundo cluster uso subPathValor: [p2=>v22, p3=>v32]
                // Para crear el tercer cluster uso subPathValor:  [p3=>v33]
                
                // WARNING: ESTO NO SOPORTA CLUSTERs multiples y ELEMENTs multiples, porque los elements
                //          serian siempre bindeados simples (se pasa de a un valor para cada path).
                //          Para soportar esto, las paths deberian venir diferenciadas para cada CLUSTER,
                //          por ejemplo poniendo un _1, _2, etc para cada repeticios del cluster.
                
                def index = 0 // Para iterar por los valores multiples
                def termino = false
                while (!termino)
                {
                    // ==================================================
                    // Indice para poner errores 
                    currentIndex = index
                    // ==================================================
                   
                    def subPathValor = [:]
                    
                    // armo el subPathValor
                    pathValor.each { entry ->
                        
                        if (entry.value.getClass().isArray()) // valores multiples
                        {
                            if (index < entry.value.length)
                                subPathValor[entry.key] = entry.value[index] // path => valor simple
                        }
                        else // supongo que el valor es un string, TODO: ver si existe algun caso que caiga aca!
                        {
                            // TODO: verificar si esto puede ser un problema: como se que el objeto simple
                            //       se corresponde con el primer cluster? no se... y si los valores del
                            //       cluster tienen una relacion semantica, aca estoy metiendo la pata.
                            if (index == 0) // solo se pone en el primer cluster
                            {
                                subPathValor[entry.key] = entry.value // path => valor simple
                            }
                        }
                    }
                    
                    // Si no tengo mas valores para bindear, termino...
                    termino = subPathValor.size() == 0
                    
                    if (!termino) // en la ultima vuelta me aseguro de no bindear nada...
                    {
                        // TODO: verificar que hay algun valor para las paths a bindear, si no hay valores, no deberia bindear
                        if (subPathValor.find{it.value != ''})
                            result << bindSingleCluster(cco, subPathValor, arquetipo, tempId)
                    
                        index++
                    }
                    
                } // while ! termino
                
                // ==================================================
                // Indice para poner errores
                currentIndex = 0
                // ==================================================
                
            } // multiples valores
        }
        else // Cluster no tiene ocurrencias multiples
        {
            //println "OCURRENCIA SIMPLE"
           
            // TODO: verificar que hay algun valor para las paths a bindear, si no hay valores, no deberia bindear
            if (pathValor.find{it.value != ''})
                result << bindSingleCluster(cco, pathValor, arquetipo, tempId)
        }
        
        //println "???????????????????????????????????? RETURN bindCLUSTER"
        
        return result
        
    } // bindCLUSTER
    
    // Metodo auxiliar para bindCLUSTER, devuelve un CLUSTER con sus ELEMENTs.
    /**
     * 
     * @param cco
     * @param pathValor
     * @param arquetipo
     * @param tempId
     * @param index indice del cluster a bindear si se estan bindeando multiples clusters.
     * @return
     */
    def bindSingleCluster(CComplexObject cco, LinkedHashMap<String, Object> pathValor, Archetype arquetipo, String tempId)
    {
        def cluster = null
        
        // Lista de lista de objetos bindeados para cada atributo del CCObject
        /*
        List<Object> listaListRMO = []

        assert cco.getAttributes().size() == 1 // Un Cluster debe tener un CAttribute para su atributo items
        
        // El unico atributo que espero que tenga es 'items'
        cco.getAttributes().each { cattr ->
        
            // Me quedo con los path que corresponde al atributo que se esta procesando
            LinkedHashMap<String, Object> pathValorAtribute = pathValor.findAll{it.key.startsWith(cattr.path())}
            
            // Es innecesario hacer el filtro porque ya se que lo que viene es todo para el atributo items del cluster...
            assert pathValorAtribute == pathValor
            
            def listRMO = bindAttribute(cattr, pathValorAtribute, arquetipo, tempId)
            listaListRMO.add(listRMO)
        }
        */
        
        // Simplificacion por conocer la estructura interna de un CLUSTER
        // 1. No hago loop each porque se que tengo un solo CAttribute (cco.getAttributes()[0])
        // 2. No hago filtro de pathsValor porque se que todas las paths son para el atributo items del cluster
        //
        // TODO: se puede hacer lo mismo con los demas tipos!
        //
        def listaItems = bindAttribute(cco.getAttributes()[0], pathValor, arquetipo, tempId)
        
        
        
        // FIXME: no se valida la cardinalidad de los items del CLUSTER, deberia hacerse igual que se hace para ITEM_TREE
        //if (listaListRMO.size()==1) // Ha bindeado 'items'
        //{
        //    def listaItems = listaListRMO[0] // Puede ser una lista de objetos
            if (countValues(listaItems) > 0)
            {
                //println "|||||||||||=========>>>>>>> (List<Object>?) tipo listaItems: " + listaItems.getClass()
                //createCLUSTER(List<Object> listaItems, Archetype arquetipo, String archNodeId, String tempId)
                cluster = rmFactory.createCLUSTER(listaItems, arquetipo, cco.nodeID, tempId, cco)
            }
            else // (+++)
            {
               //println "bindSingleCluster: no hay items para bindear"
               if ( cco.occurrences.getLower() > 0 )
               {
                  //println "bindSingleCluster 1: bindeo el cluser sin items porque tiene ocurrencia > 0"
                  cluster = rmFactory.createCLUSTER([], arquetipo, cco.nodeID, tempId, cco)
               }
            }
        //}
        //else // (+++)
        //{
        //   if ( cco.occurrences.getLower() > 0 )
        //   {
        //      //println "bindSingleCluster 2: bindeo el cluser sin items porque tiene ocurrencia > 0"
        //      cluster = rmFactory.createCLUSTER([], arquetipo, cco.nodeID, tempId, cco)
        //   }
        //}
        
        // Cluster nunca puede tener 2 atributos bindeados
        
        return cluster
    }
    
    
    def bindELEMENT(CComplexObject cco, LinkedHashMap<String, Object> pathValor, Archetype arquetipo, String tempId)
    {
       //println '=============================================='
       //println 'bindELEMENT pathValor: ' + pathValor
       
              
       // Un Element debe tener un CAttribute para su atributo value
       // Es innecesario hacer el filtro porque ya se que lo que viene es todo para el atributo value del Element...
       
       
       // Simplificacion por conocer la estructura interna de un ELEMENT
       // 1. No hago loop each porque se que tengo un solo CAttribute (cco.getAttributes()[0])
       // 2. No hago filtro de pathsValor porque se que todas las paths son para el atributo items del cluster
       //
       def rmObjectsForValue = bindAttribute(cco.getAttributes()[0], pathValor, arquetipo, tempId)
       
       
       // Lista para retornar
       def elements = []

       
        //println "bondElement: rmobjectForValue="+rmObjectsForValue
        
        // FIXME:
        // HICE UN CAMBIO EN FACTORY: si no se tienen todos los valores para crear el value,
        // se devuelve null, entonces deberia verificar si la cantidad de elementos que me
        // devuelve es menor que el occurrences.lower del element que estoy bindeando.
        // Ahora lo que hace es buscar errores de null en los values, pero por el cambio: NO HAY VALUES!
        // Con esto creo que anda ok:
        if ( rmObjectsForValue.size() < cco.occurrences.getLower() )
        {
           //println "- hay menos valores que la menor ocurrencia permitida"
           
           // Bindeo tantos elements como se esperaban, pero para los que no tengan valor, pongo null,
           // entonces se genera un error para el element.value (que es lo que quiero).
           // FIXME: Tambien se podria generar un error en el padre que diga que se esperaban X
           // elements y se tienen Y, es un error mas amigable al usuario.
           def element
           for (def i in 0..cco.occurrences.getLower()-1)
           {
              // ==================================================
              // Indice para poner errores
              currentIndex = i
              // ==================================================
              
              // rmObjectsForValue[i] puede ser null
              element = rmFactory.createELEMENT(rmObjectsForValue[i], arquetipo, arquetipo.node(cco.path()).nodeID, tempId, cco)
              if (!element.validate()) // valida que tenga value, no las ocurrencias del propio element
              {
                 //this.hasErrors = true
                 
                 //println "errors: " + element.errors
                 
                 // El i es para saber para cual de los nodos multiples dio el error
                 // FIXME: la path completa seria archetypeId+refPath+path
                 //        falta calcular refPath como esta calculada en las vistas
                 // FIXME: poner el i solo si el nodo puede ser multiple!
                 //this.errors[arquetipo.archetypeId.value + cco.path()+'_'+i] = bindedElem.errors
                 //this.errors[arquetipo.archetypeId.value + cco.path()] = element.errors
                 this.addErrors(arquetipo.archetypeId.value + cco.path(), element.errors)
              }
              elements << element
           }
           
           // ==================================================
           // Indice para poner errores
           currentIndex = 0
           // ==================================================
           
           return elements
        }
        
        /* FIXME:
         * El error de ocurrencia se debe verificar en el ELEMENT, pero deben ponerse
         * las paths dependiendo del tipo de su value. Por ejemplo si es unQuantity,
         * hay que ver si se ponen errores para las 2 paths, y el element debe saber
         * los nombres de los atributos sabiendo el tipo del value.
         * Esto se resolveria llamando a un metodo que se encargue de averiguar
         * los nombres de lo atributos, asi aqui veo so el element tiene las ocurrencias
         * correctas y se que  es porque faltan valores para paths de sus atributos
         */
        /**
         * LA CONDICION CORRECTA SERIA, SI LA CANTIDAD DE LOS VALORES QUE
         * VIENEN BINDEADOS PARA LOS ELEMS QUE TIENEN ERROR DE VALOR NULO
         * SON MENOS QUE LOS QUE EXIGE EL ELEM.
         * Porque aunque el valor no sea nulo, el param que le pasé para
         * crearlo pudo haber sido y el GORM valida metiendo error de nulo.
         * El error deberia mostrarse una sola vez, esto lo logor mostrando
         * el error para la path y sacandolo del map de errores.
         * Se chequea aca: *****
         */
         // La verificacion de errores la hace el GORM, aqui veo si hay errores en los
         // datavalues y los traslado al ELEMENT si corresponde.
        
        // Cantidad de errores por null?
        def nullErrorCount = 0
        rmObjectsForValue.each { bindedElementValue ->
            
            //println "xxx Tipo: " + bindedElementValue.getClass().getSimpleName()

            // FIXME: faltan datatypes para ver el atributo obligatorio segun el RM
            // Si es boolean, el atributo es value, asi que no habria que verificar si es DvBoolean. 
            def attribute = "value"
            if (["DvQuantity","DvCount"].contains( bindedElementValue.getClass().getSimpleName() ))
                attribute = "magnitude"
            else if (["DvCodedText"].contains( bindedElementValue.getClass().getSimpleName() ))
                attribute = "definingCode"
            
            //println "Clase 1: " + bindedElementValue.getClass().getSimpleName()
            //println "Errores: " + bindedElementValue.errors
            
            //println "Tiene error: " + bindedElementValue.errors.getFieldError(attribute)
            //println "Rejected value: " + bindedElementValue.errors.getFieldError(attribute).rejectedValue
            
            //println "xxx Attribute: " + attribute
            // Si hay un error y el valor rejected fue null...
            if (bindedElementValue.errors.getFieldError(attribute) &&
                bindedElementValue.errors.getFieldError(attribute).rejectedValue == null)
            {
               nullErrorCount++
            }
        }
        
        //println "Element Null ERROR Count :" + nullErrorCount
        //println "rmObjectsForValue.size :" + rmObjectsForValue.size()
        //println "==== rmObjectsForValue: " + rmObjectsForValue
        
        rmObjectsForValue.eachWithIndex { bindedElementValue, i ->

           
           // ==================================================
           // Indice para poner errores
           currentIndex = i
           // ==================================================
           
           
            // *****
            //println bindedElementValue.errors // TODO> ver cuantos errores son por nulos...
            
            // FIXME: el valor puede ser null y el Element puede ser obligatorio!
            if (bindedElementValue)
            {
                //println "==== bindedElementValue: " + bindedElementValue.getClass()
                
                def bindedElem = rmFactory.createELEMENT(bindedElementValue, arquetipo, arquetipo.node(cco.path()).nodeID, tempId, cco)

                //println "====>>>> bindedElem: " + bindedElem

                // Por [555]
                // Indica si debe poner al element en el resultado.
                def bindElement = true
                    
                //println "xxx Comparacion: " + (rmObjectsForValue.size()-nullErrorCount) + " y " + cco.occurrences.getLower()

                
                // FIXME: Esta verificacion se hace considerando el size de la coleccion por la que estoy iterando,
                //        esto se deberia verificar por fuera de la iteracion.
                //        Ademas pone un error para todos los ELEMENTS que se hayan bindeado, no solamente en los
                //        que les falta el value.
                //        Por otro lado, la validacion sobre las ocurrencias del ELEMENT deberia ponerse en el
                //        contenedor, no en cada ELEMENT, porque cada instancia de ELEMENT ve su ocurrencia (1)
                //        no la ocurrencia de todos sus hermanos, quien es responsable por las ocurrencias de
                //        todos los ELEMENTs es el padre (CLUSTER o un ITEM_STRUCTURE).
                //        
                //        El padre verificaria algo asi:
                //
                //        PARA OCURRENCIAS DE UN NODO HIJO:
                //         - Un CLUSTER/ITEM_STRUCT verifica que todos sus items tengan su ocurrencia minima cumplida
                //         - Un CLUSTER/ITEM_STRUCT verifica que todos sus items tengan su ocurrencia maxima cumplida 
                //
                //        PARA CARDINALIDAD DE ATRIBUTOS COLECCION:
                //         - Un CLUSTER/ITEM_STRUCT verifica que la cantidad de items que tiene cumple con la cardinalidad minima
                //         - Un CLUSTER/ITEM_STRUCT verifica que la cantidad de items que tiene cumple con la cardinalidad maxima
                //
                //        Los errores de ocurrencias y de cardinalidad, van en el contenedor, no en los hijos! 
                
                // TODO: esta validacion deberia hacerse en el Factory.
                
                // Si hay menos valores OK de null de los necesarios por ocurrencias, error de ocurrencias!
                if ( rmObjectsForValue.size()-nullErrorCount < cco.occurrences.getLower() )
                {
                    //println "ooooo pongo error a: " + cco.path()
                    //println "bindELEMENT hasErrors 1 " + cco.path()
                    
                    // FIXME: en realidad cuando no tengo element.value no es error de ocurrencias,
                    // es error del RM de que value no puede ser null.
                    // El error de ocurrencias deberia ir en el padre del element, que diga que se
                    // esperaban X elements y se tienen Y.
                   
                    // es value porque estoy en ELEMENT
                    bindedElem.errors.rejectValue("value", Errors.ERROR_OCCURRENCES)

                    //this.hasErrors = true
                    
                    // El i es para saber para cual de los nodos multiples dio el error
                    // FIXME: la path completa seria archetypeId+refPath+path
                    //        falta calcular refPath como esta calculada en las vistas
                    // FIXME: poner el i solo si el nodo puede ser multiple!
                    //this.errors[arquetipo.archetypeId.value + cco.path()+'_'+i] = bindedElem.errors
                    //this.errors[arquetipo.archetypeId.value + cco.path()] = bindedElem.errors
                    this.addErrors(arquetipo.archetypeId.value + cco.path(), bindedElem.errors)
                    
                    //println "xxx pongo el error en el ELEMENT: " + cco.path()

                    // Saco el error del element.value
                    bindedElem.value.errors = null // Saco errores de formato que pueda tener el hijo value, asi muestro solo errores en el element.

                    nullErrorCount = 0 // Asi el error solo va en el primer ELEMENT, asi se muestra el error una sola vez!
                }
                else if (bindedElem.value.errors.hasErrors() && cco.occurrences.getLower()==0) // Si tengo un error (TODO: deberia ser un error de null) en el element.value pero el element es opcional, el element queda en null y no se agrega a la lista bindeada.
                {
                    // FIXME: faltan datatypes para ver el atributo obligatorio segun el RM
                    def attribute = "value"
                    if (["DvQuantity","DvCount"].contains( bindedElementValue.getClass().getSimpleName() ))
                        attribute = "magnitude"
                    else if (["DvCodedText"].contains( bindedElementValue.getClass().getSimpleName() ))
                        attribute = "definingCode"
                    
                    //println "Clase 2: " + bindedElementValue.getClass().getSimpleName()
                    //println "== Rejected attribute: " + attribute
                    //println "== Tiene error en el attr: " + bindedElem.value.errors.hasFieldErrors(attribute)
                    //println "== Rejected value: " + bindedElem.value.errors.getFieldError(attribute)?.rejectedValue
                    
                    // Le digo que no bindee el ELEMENT solo si el valor rechazado es null,
                    // si el valor es por ejemplo un string mal formado que se necesita pasar
                    // a numerico, el ELEMENT se tiene que bindear para cargar el error para ser
                    // mostrado en la web.
                    if ( bindedElem.value.errors.hasFieldErrors(attribute) &&
                         bindedElem.value.errors.getFieldError(attribute)?.rejectedValue == null )
                    {
                        /** [555]
                         * *****************************************************************************************
                         * *****************************************************************************************
                         * Si bindea un datavalue con un valor nulo, va a dar error de validacion en el DV,
                         * pero si el ELEMENT donde va ese DV es opcional, ese ELEMENT deberia ser null adentro
                         * de su estructura contenedora, de esa forma no bindea un element opcional con un
                         * valor null en su datavalue.value, o sea el valor simple adentro del datavalue en
                         * ELEMENT.value.
                         * *****************************************************************************************
                         * *****************************************************************************************
                         */
                        bindElement = false
                        //println "----=== LE DICE QUE NO META AL ELEMENT EN LA LIsTA DE ELEMENTS!"
                    }
                }

                //println "bindELEMENT Element errors: " + bindedElem.errors
                
                if (bindElement) elements << bindedElem
                
            } // if (bindedElementValue)
        } // rmObjectsForValue.each
        
        
        // ==================================================
        // Indice para poner errores
        currentIndex = 0
        // ==================================================
        
        
        return elements // lista de elementos
    }
    
    // =============================================================
    // ===================== Bindeo de DataValues ==================
    // =============================================================
    def bindDV_MULTIMEDIA(CComplexObject cco, LinkedHashMap<String, Object> pathValor, Archetype arquetipo, String tempId)
    {
        //println "== bindDV_MULTIMEDIA"
        //println "==== pathValor: " + pathValor
        //println "=============================================="
        
        def result = []
        
        if (pathValor.size()==0) // Si no hay path ni valores
        {
            return result
        }
        if (pathValor.size()==1) // Si viene una path (serian solo archivos)
        {
            def key = pathValor.find{true}.key // agarro la unica entrada y pido su clave
            if (!pathValor[key]) // Si vino la path pero el valor es vacio
            {
                return result
            }
            
            // pueden haber muchos archivos
            if (pathValor[key].getClass().isArray()) // hay varios archivos
            {
                (pathValor[key] as List).each { file ->
                    if (!file.isEmpty())
                        result << rmFactory.createDV_MULTIMEDIA( file, arquetipo, cco.nodeID, tempId, cco)
                }
            }
            else // hay un solo archivo
            {
                def file = pathValor[key]
                if (!file.isEmpty())
                    result << rmFactory.createDV_MULTIMEDIA( file, arquetipo, cco.nodeID, tempId, cco)
            }
            
            return result
        }
        // por ahora no hay un caso donde venga archivo y otro dato.
        throw new Exception("Hay mas de una path para bindDV_MULTIMEDIA, hay: " + pathValor.size())
        
    } // bindDV_MULTIMEDIA
    
    def bindDV_BOOLEAN(CComplexObject cco, LinkedHashMap<String, Object> pathValor, Archetype arquetipo, String tempId)
    {
        //println "== bindDV_BOOLEAN"
        //println "==== pathValor: " + pathValor
        //println "=============================================="
        
        def result = []
        
        if (pathValor.size()==0) // Si no hay path ni valores
        {
            //println "==== NO HAY VALORES"
            // quiero que valide el GORM
            result << rmFactory.createDV_BOOLEAN( null, arquetipo, cco.nodeID, tempId, cco)
            return result
        }
        if (pathValor.size()==1) // Si viene una path
        {
            //println "==== HAY VALORES"
            def key = pathValor.find{true}.key // agarro la unica entrada y pido su clave
            
            // Los errores de ocurrencias las debe verificar el ELEMENT al que pertenece este DataValue.
            
            // pueden haber muchos valores
            if (pathValor[key].getClass().isArray()) // hay varios valores
            {
                //println "==== MUCHOS VALORES"
                (pathValor[key] as List).each { value ->
                
                    // El valor es string, es exacto lo que viene de la web
                    // No chequeo errores, quiero que valide el GORM.
                    result << rmFactory.createDV_BOOLEAN(value, arquetipo, cco.nodeID, tempId, cco)
                }
            }
            else // hay un solo valor
            {
                //println "==== UN SOLO VALOR"
                
                // No chequeo errores, quiero que valide el GORM
                def value = pathValor[key] // el valor es string, es exacto lo que viene de la web
                result << rmFactory.createDV_BOOLEAN( value, arquetipo, cco.nodeID, tempId, cco)
            }
            
            return result // puede ser vacia
        }
        // por ahora no hay un caso donde venga el valor bool y otro dato.
        throw new Exception("Hay mas de una path para bindDV_BOOLEAN, hay: " + pathValor.size())
        
    } // bindDV_BOOLEAN
    
    def bindDV_TEXT(CComplexObject cco, LinkedHashMap<String, Object> pathValor, Archetype arquetipo, String tempId)
    {
        //println "== bindDV_TEXT"
        //println "==== pathValor: " + pathValor
        //println "=============================================="
        
        def result = []
        def rmObj

        if (pathValor.size()==0) // Si no hay path ni valores
        {
            //println "---- NO HAY VALORES "
            // Meto un objeto vacio para que valide el GORM y muestre errores en la web
            rmObj = rmFactory.createDV_TEXT( null, arquetipo, cco.nodeID, tempId, cco)
            rmObj.errors.rejectValue('value', 'error.empty') // errorCode
            
            // TODO: verificar que alguien verifica este error
            
            result << rmObj
            
            // Marco que hubo error para que el controller mande a edit
            //this.hasErrors = true
            
            //println "---- RESULT: " + result

            return result
        }
        if (pathValor.size()==1) // Si viene una path
        {
            def key = pathValor.find{true}.key // agarro la unica entrada y pido su clave
            
            // Los errores de ocurrencias las debe verificar el ELEMENT al que pertenece este DataValue.
            
            // pueden haber muchos valores
            if (pathValor[key].getClass().isArray()) // hay varios valores
            {
                (pathValor[key] as List).each { value ->
                
                    // el valor es string, es exacto lo que viene de la web
                    // Dejo pasar valores vacios y no chequeo errores, dejo que valide el GORM.
                    rmObj = rmFactory.createDV_TEXT( value, arquetipo, cco.nodeID, tempId, cco)

                    if (rmObj.errors.hasErrors())
                    {
                        // Marco que hubo error para que el controller mande a edit
                        //this.hasErrors = true
                        // TODO: verificar que alguien verifica este error
                    }

                    result << rmObj
                }
            }
            else // hay un solo valor
            {
                def value = pathValor[key] // el valor es string, es exacto lo que viene de la web
                
                // Si no hay valor, lo dejo pasar igual para que cree el DV y lo valide el GORM.
                // Dejo pasar valores vacios y no chequeo errores, dejo que valide el GORM.
                rmObj = rmFactory.createDV_TEXT( value, arquetipo, cco.nodeID, tempId, cco)

                if (rmObj.errors.hasErrors())
                {
                    // Marco que hubo error para que el controller mande a edit
                    //this.hasErrors = true
                    // TODO: verificar que alguien verifica este error
                }
                
                result << rmObj
            }
            
            return result // puede ser vacia
        }
        // por ahora no hay un caso donde venga el valor y otro dato.
        throw new Exception("Hay mas de una path para bindDV_TEXT, hay: " + pathValor.size())
    
    } // bindDV_TEXT

    
    // FIXME: pasa que para el rmTypName DV_CODED_TEXT el tipo de la restriccion es CComplexObject,
    //        en lugar de ser CDomainType como muestra la especificacion del AOM.
    //        entonces trato a DV_CODED_TEXT como esta CComplexObject y luego veo
    //        de preguntar en la comunidad a ver porque es que no viene CDomainType.
    //
    //        POR AHORA AGREGO DV_CODED_TEXT como un tipo comun en isDataValue no un DomainType.
    //
    //        Esto agrega la necesidad de tener un metodo bindDV_CODED_TEXT
    def bindDV_CODED_TEXT(CComplexObject cco, LinkedHashMap<String, Object> pathValor, Archetype arquetipo, String tempId)
    {
        // Los valores en pathValor vienen con datos para DvCodedText pero tambien para DvCodedText.definition_code
        // ej. code||text si la restriccion es CCodePhrase
        // ej. terminologyId::code||text si la restriccion es ConstraintRef
        
        //println "== bindDV_CODED_TEXT pathValor: " + pathValor
        
        // Hay que bindear los definingCode por el CCodePhrase que tiene el attributes del cco
        List<Object> definingCodes = []
        
        // Restrivccion sobre el definingCode del DvCodedText
        //println "----------- attributes: " + cco.getAttributes().rmAttributeName
        CAttribute cattr = cco.getAttributes().find{it.rmAttributeName=='defining_code'}
        definingCodes = bindAttribute(cattr, pathValor, arquetipo, tempId) // List<CodePhrase>
        /*
        cco.getAttributes().each { cattr ->
        
            // Me quedo con los path que corresponde al atributo que se esta procesando
            LinkedHashMap<String, Object> pathValorAtribute = pathValor.findAll{it.key.startsWith(cattr.path())}
            def listRMO = bindAttribute(cattr, pathValorAtribute, arquetipo, tempId)
            definingCodes.add(listRMO)
        }
        */
        
        //println "== bindDV_CODED_TEXT definingCodes: " + definingCodes
        
        def result = []
        def rmObj
        def text
        def valueListOrSingleValue
        definingCodes.eachWithIndex { dcode, i -> // uso el i para saber que texto de pathValor corresponde con que defininCode (porque los definingCode fueron procesados en el mismo orden!)
           
            // [0] valores para la unica path que recibo
            // [1] valor i code||text o terminologyId::code||text, quiero el text para DvCodedText.value
            
            /*
            println 'entry '+ pathValor.find { true } // 
            println 'value list '+ pathValor.find { true }.value // 
            println 'value i '+ pathValor.find { true }.value[i] // 
            println pathValor.find { true }.value[i].split(/\|\|/) // 
            println pathValor.find { true }.value[i].split(/\|\|/)[1] // 
            */
            
            // Si viene un solo valor, es un string, sino una lista
            valueListOrSingleValue = pathValor.find { true }.value
            
            if (valueListOrSingleValue instanceof String) text = valueListOrSingleValue.split(/\|\|/)[1]
            else text = pathValor.find { true }.value[i].split(/\|\|/)[1]
            
            
            // el valor es string, es exacto lo que viene de la web
            // Dejo pasar valores vacios y no chequeo errores, dejo que valide el GORM.
            rmObj = rmFactory.createDV_CODED_TEXT(text, dcode, arquetipo, cco.nodeID, tempId, cco)

            if (rmObj.errors.hasErrors())
            {
                // Marco que hubo error para que el controller mande a edit
                //println "bindDV_CODED_TEXT hasErrors "
                //this.hasErrors = true
                
                // FIXME: la path completa seria archetypeId+refPath+path
                //        falta calcular refPath como esta calculada en las vistas
                //this.errors[arquetipo.archetypeId.value + cco.path()] = rmObj.errors
                this.addErrors(arquetipo.archetypeId.value + cco.path(), rmObj.errors)
            }

            result << rmObj
        }
        
        return result
    
    } // bindDV_CODED_TEXT
    

    def bindDV_COUNT(CComplexObject cco, LinkedHashMap<String, Object> pathValor, Archetype arquetipo, String tempId)
    {
        //println "== bindDV_COUNT"
        //println "==== pathValor: " + pathValor
        //imprimirObjetoXML(cco)
        //println "=============================================="

        def result = []

        if (pathValor.size()==0) // Si no hay path ni valores
        {
            //println "==== DICE QUE NO HAY VALORES"
            def rmObj = rmFactory.createDV_COUNT(null, arquetipo, cco.nodeID, tempId, cco)

            // ABC*
            // prueba para retornar null de createDV_COUNT
            if (rmObj)
            {
                if (rmObj.errors.hasErrors())
                {
                    //println "bindDV_COUNT hasErrors 1"
                    //this.hasErrors = true
                    
                    // FIXME: la path completa seria archetypeId+refPath+path
                    //        falta calcular refPath como esta calculada en las vistas
                    //this.errors[arquetipo.archetypeId.value + cco.path()] = rmObj.errors
                    //println "==== EL ERROR QUE DA ES: " + rmObj.errors
                    
                    this.addErrors(arquetipo.archetypeId.value + cco.path(), rmObj.errors)
                }
                
                result << rmObj
            }
            
            return result
        }
        else if (pathValor.size()==1) // Si viene una path
        {
            //println "==== DICE QUE HAY VALORES"
            
            def key = pathValor.find{true}.key // agarro la unica entrada y pido su clave

            // Los errores de ocurrencias las debe verificar el ELEMENT al que pertenece este DataValue.

            // pueden haber muchos archivos
            if (pathValor[key].getClass().isArray()) // hay varios archivos
            {
                (pathValor[key] as List).each { value ->
                    
                    // Pruebo crear el obj aunque el valor sea incorrecto, asi pongo el error adentro.
                    def rmObj = rmFactory.createDV_COUNT( value, arquetipo, cco.nodeID, tempId, cco)

                    // ABC*
                    // prueba para retornar null de createDV_COUNT
                    if (rmObj)
                    {
                        // Veo error de rangos
                        if (rmObj.errors.hasErrors())
                        {
                            //println "==== EL ERROR QUE DA ES: " + rmObj.errors
                            //println "bindDV_COUNT hasErrors 2"
                            //this.hasErrors = true
                            
                            // FIXME: la path completa seria archetypeId+refPath+path
                            //        falta calcular refPath como esta calculada en las vistas
                            //this.errors[arquetipo.archetypeId.value + cco.path()] = rmObj.errors
                            this.addErrors(arquetipo.archetypeId.value + cco.path(), rmObj.errors)
                        }
    
                        result << rmObj
                    }
                }
            }
            else // hay un solo valor
            {
                def value = pathValor[key] // el valor es string, es exacto lo que viene de la web

                // Pruebo crear el obj aunque el valor sea incorrecto, asi pongo el error adentro.
                def rmObj = rmFactory.createDV_COUNT( value, arquetipo, cco.nodeID, tempId, cco )

               // ABC*
               // prueba para retornar null de createDV_COUNT
               if (rmObj)
               {
                   // Verifico error de rangos
                   if (rmObj.errors.hasErrors())
                   {
                       //println "==== EL ERROR QUE DA ES: " + rmObj.errors
                       //println "bindDV_COUNT hasErrors 3"
                       //this.hasErrors = true
                       
                       // FIXME: la path completa seria archetypeId+refPath+path
                       //        falta calcular refPath como esta calculada en las vistas
                       //this.errors[arquetipo.archetypeId.value + cco.path()] = rmObj.errors
                       this.addErrors(arquetipo.archetypeId.value + cco.path(), rmObj.errors)
                   }
   
                   result << rmObj
               }
            }
            
            return result // puede ser vacia
        }
        
        // FIXME: no tirar excepcion, devolver null y hacer un log a disco o mandar por mail.
        // por ahora no hay un caso donde venga el valor y otro dato.
        throw new Exception("Hay mas de una path para bindDV_COUNT, hay: " + pathValor.size())

    } // bindDV_COUNT

    def bindDV_DATE(CComplexObject cco, LinkedHashMap<String, Object> pathValor, Archetype arquetipo, String tempId)
    {
        //println "== bindDV_DATE"
        //println "==== pathValor: " + pathValor
        //println "=============================================="

        def result = []

        if (pathValor.size()==0) // Si no hay path ni valores
        {
            return result
        }
        // FIXME: implementar la validacion en el validate de la clase.
        if (pathValor.size() >= 3) // Si viene una path
        {
            String year = pathValor.find{it.key.endsWith("year")}?.value
            String month = pathValor.find{it.key.endsWith("month")}?.value
            String day = pathValor.find{it.key.endsWith("day")}?.value

            if ((year != null) && (month != null) && (day != null))
            {
                try
                {
                    // TODO: que estas transformaciones las haga el propio constructor
                    //       del DV_DATE, que si se le pasa strings los trata de convertir
                    //       a int y valida e inyecta los errores en el objeto del GORM.
                    int y = Integer.parseInt(year)
                    int m = Integer.parseInt(month)
                    int d = Integer.parseInt(day)

                    result << rmFactory.createDV_DATE(year, month, day, arquetipo, cco.nodeID, tempId, cco)
                }
                catch(Exception e)
                {
                    // FIXME: si hay un error aca, no deberia meterse en errors2 porque no se usa mas.
                    /*
                    def path = pathValor.keySet().toArray()[0]
                    def fullPath = arquetipo.archetypeId.value + path // No puede llamar a fullPath() porque ci no es ArchetypeConstraint!
                    if ( !errors2[fullPath] ) errors2[fullPath] = [] // Creo lista de errores para esta path si no estaba creada
                    errors2[fullPath] << ERROR_BAD_FORMAT
                   */
                }
            }

            return result // puede ser vacia
        }
        // por ahora no hay un caso donde venga el valor y otro dato.
        throw new Exception("No hay 3 path para bindDV_DATE, hay: " + pathValor.size())

    } // bindDV_DATE


   def bindDV_DATE_TIME(CComplexObject cco, LinkedHashMap<String, Object> pathValor, Archetype arquetipo, String tempId)
   {
      //println "== bindDV_DATE_TIME"
      //println "==== pathValor: " + pathValor
      //println "=============================================="

      def result = []

      if (pathValor.size()==0) // Si no hay path ni valores
      {
         return result
      }
      // FIXME: implementar la validacion en el validate de la clase.
      if (pathValor.size() >= 4) // Si viene una path
      {
         String year = pathValor.find{it.key.endsWith("year")}?.value
         String month = pathValor.find{it.key.endsWith("month")}?.value
         String day = pathValor.find{it.key.endsWith("day")}?.value
         String hour = pathValor.find{it.key.endsWith("hour")}?.value
         String minute = pathValor.find{it.key.endsWith("minute")}?.value
         String seg = pathValor.find{it.key.endsWith("seg")}?.value // FIXME: no vienen los segundos, pero si vinieran seguro que no termina con "seg"


         if ((year != null) && (month != null) && (day != null) && (hour != null))
         {
             try
             {
                 // TODO: que estas transformaciones las haga el propio constructor
                 //       del DV_DATE, que si se le pasa strings los trata de convertir
                 //       a int y valida e inyecta los errores en el objeto del GORM.
                 int y = Integer.parseInt(year)
                 int m = Integer.parseInt(month)
                 int d = Integer.parseInt(day)
                 int h = Integer.parseInt(hour)
                 int min = Integer.parseInt(minute)

                 if ((seg != null) && (seg != "")){
                     int s = Integer.parseInt(seg)
                 }

                 result << rmFactory.createDV_DATE_TIME(year, month, day, hour, minute, seg, arquetipo, cco.nodeID, tempId, cco)
             }
             catch(Exception e)
             {
                 // FIXME: no se usa mas poner errores en errors2
//                   def path = pathValor.keySet().toArray()[0]
//                   def fullPath = arquetipo.archetypeId.value + path // No puede llamar a fullPath() porque ci no es ArchetypeConstraint!
//                   if ( !errors2[fullPath] ) errors2[fullPath] = [] // Creo lista de errores para esta path si no estaba creada
//                   errors2[fullPath] << ERROR_BAD_FORMAT
             }
         }

         return result // puede ser vacia
      }
      // por ahora no hay un caso donde venga el valor y otro dato.
      throw new Exception("No hay al menos 4 path para bindDV_DATE_TIME, hay: " + pathValor.size() )

   } // bindDV_DATE_TIME

    def bindDV_TIME(CComplexObject cco, LinkedHashMap<String, Object> pathValor, Archetype arquetipo, String tempId)
    {
        //println "== bindDV_TIME"
        //println "==== pathValor: " + pathValor
        //println "=============================================="

        def result = []

        if (pathValor.size()==0) // Si no hay path ni valores
        {
            return result
        }

        if (pathValor.size() >= 3) // Si viene una path
        {
            // FIXME: implementar la validacion en el validate de la clase.
            String hour = pathValor.find{it.key.endsWith("hour")}?.value
            String minute = pathValor.find{it.key.endsWith("minute")}?.value
            String seg = pathValor.find{it.key.endsWith("seg")}?.value

            if ((month != null) && (day != null) && (hour != null))
            {
                try
                {
                    // TODO: que estas transformaciones las haga el propio constructor
                    //       del DV_DATE, que si se le pasa strings los trata de convertir
                    //       a int y valida e inyecta los errores en el objeto del GORM.
                    int h = Integer.parseInt(hour)
                    int m = Integer.parseInt(minute)
                    int s = Integer.parseInt(seg)

                    result << rmFactory.createDV_DATE_TIME(hour, minute, seg, arquetipo, cco.nodeID, tempId, cco)
                }
                catch(Exception e)
                {
                    // FIXME: no se usa mas errors2 para poner errores
//                   def path = pathValor.keySet().toArray()[0]
//                   def fullPath = arquetipo.archetypeId.value + path // No puede llamar a fullPath() porque ci no es ArchetypeConstraint!
//                   if ( !errors2[fullPath] ) errors2[fullPath] = [] // Creo lista de errores para esta path si no estaba creada
//                   errors2[fullPath] << ERROR_BAD_FORMAT
                }
            }

            return result // puede ser vacia
        }
        // por ahora no hay un caso donde venga el valor y otro dato.
        throw new Exception("No hay 3 path para bindDV_DATE_TIME, hay: " + pathValor.size())

    } // bindDV_TIME
    
    
   def bindDV_DURATION(CComplexObject cco, LinkedHashMap<String, Object> pathValor, Archetype arquetipo, String tempId)
   {
      //println "== bindDV_DURATION"
      //println "==== pathValor: " + pathValor
      //println "=============================================="

      def result = []

      if (pathValor.size()==0) // Si no hay path ni valores
      {
         return result
      }

      String years   = pathValor.find{it.key.endsWith("years")}?.value
      String months  = pathValor.find{it.key.endsWith("months")}?.value
      String days    = pathValor.find{it.key.endsWith("days")}?.value
      String hours   = pathValor.find{it.key.endsWith("hours")}?.value
      String minutes = pathValor.find{it.key.endsWith("minutes")}?.value
      String seconds = pathValor.find{it.key.endsWith("seconds")}?.value

      int iyears   = (years) ? Integer.parseInt(years) : null
      int imonths  = (months) ? Integer.parseInt(months) : null
      int idays    = (days) ? Integer.parseInt(days) : null
      int ihours   = (hours) ? Integer.parseInt(hours) : null
      int iminutes = (minutes) ? Integer.parseInt(minutes) : null
      int iseconds = (seconds) ? Integer.parseInt(seconds) : null

      result << rmFactory.createDV_DURATION(iyears, imonths, idays, ihours, iminutes, iseconds, arquetipo, cco.nodeID, tempId, cco)

      return result // puede ser vacia
        
   } // bindDV_DURATION
}