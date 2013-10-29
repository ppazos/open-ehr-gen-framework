package binding

import org.springframework.validation.FieldError
//import org.springframework.validation.BeanPropertyBindingResult

//import org.springframework.web.servlet.LocaleResolver
//import org.springframework.web.servlet.support.RequestContextUtils as RCU

import hce.core.datastructure.itemstructure.representation.*
import data_types.basic.*
import data_types.encapsulated.*
import data_types.quantity.*
import data_types.quantity.date_time.*
import data_types.text.*
import data_types.uri.*
import support.identification.*
import hce.core.datastructure.itemstructure.*
import hce.core.datastructure.history.*
import hce.core.composition.*
import hce.core.composition.content.*
import hce.core.composition.content.entry.*
import hce.core.composition.content.navigation.*
import hce.core.common.archetyped.*

import org.openehr.am.archetype.Archetype
import org.openehr.am.openehrprofile.datatypes.quantity.*
import org.openehr.am.openehrprofile.datatypes.text.CCodePhrase
import org.openehr.am.openehrprofile.datatypes.quantity.Ordinal
import org.openehr.am.archetype.constraintmodel.*
//import org.openehr.am.archetype.constraintmodel.CObject

import org.codehaus.groovy.grails.commons.ApplicationHolder

import java.util.Set
import java.net.URI
import com.thoughtworks.xstream.XStream

import converters.DateConverter // Para formatos de fechas

/**
 * @author Leandro Carrasco, Pablo Pazos Gutierrez<pablo.swp@gmail.com>
 */
class FactoryObjectRM {

   def session
   
   // Agrego la composition para poder asociarsela a todos los objetos
   // del RM bindeados, esto sirve para hacer las busquedas semanticas
   // y relacionar objetos del RM devueltos como resultado entre si,
   // cuando pertenecen a la misma ocmposition.
   //def composition
   
   def FactoryObjectRM(Object session)
   {
      this.session = session
      // Guardar la composicion en los objetos del RM me da un error en backred entre Composition y Section.
      //this.composition = Composition.get(session.ehrSession.episodioId)
   }

   /**
   * Operación que encapsula el seteo de los atrinutos archetypeNodeId, name y
   * archetypeDetails del los Objetos del RM creados.
   * FIXME: en lugar de pasarle el archNodeIde psarle el CObject y con su path,
   *      del archetype saco el archNodeId: arquetipo.node(cco.path()).nodeID
   * FIXME: tambien esta operacion deberia setear el locatable.path
   */
   def completarLocatable(Locatable locatable, String archNodeId, Archetype archetype, String tempId)
   {
      //println "ENTRANDO A COMPLETAR LOCATABLE"

      
      // Es para asociar nodos del RM en la busqueda semantica
      // Guardar la composicion en los objetos del RM me da un error en backred entre Composition y Section.
      // Voy a guardar solo el id de la composition, eso es suficiente.
      //locatable.parentComposition = this.composition
      locatable.parentCompositionId = session.ehrSession.episodioId
       
      // Saqué rm_version porque siempre va a ser 1.0.2
      //String rmV = ApplicationHolder.application.config.openEHR.RMVersion
      String archetypeId = archetype.archetypeId.value
      Archetyped archDetails = new Archetyped(archetypeId: archetypeId, templateId: tempId) //, rmVersion: rmV)

      // FIXME: para esto se puede usar CtrlTerminologia
      String lang = 'es' // Lenguaje por defecto, FIXME: sacar de config.
      if (this.session)
      {
         if (session.locale)
         {
            // FIXME: deberia escalar en locale como ArchetypeTagLib.findTerm
            lang = session.locale.getLanguage()
         }
      }
      
      String nameN
      if (archNodeId) // FIXME: todos los nodos tienen nodeID, es al pedo chequear.
      {
         // FIXME: sacar el idioma del locale seleccionado, si no, no va a encontrar
         // la definicion del termino.
         // FIXME: ver que se pone si lo que retorna termDefinition es null y 
         // no se puede hacer getItems. Esto pasa cuando hay un nodo CodedText 
         // que referencia a una terminologia externa mediante ConstraintRef.
         //nameN = archetype.ontology.termDefinition("es", archNodeId)?.getItems()?.text
         
         /*
         def term = archetype.ontology.termDefinition(lang, archNodeId)
         if (!term)
         {
            nameN = 'Termino no encontrado en el arquetipo ['+archetypeId+'], '+
                  'para el nodo ['+archNodeId+'], y el lang ['+lang+']'
         }
         else
         {
            nameN = term.getItems().text // TODO: verificar si getItems da un solo texto!
         }
         */
         
         nameN = CtrlTerminologia.getInstance().getTermino(TerminologyID.create('local', null), archNodeId, archetype, session.locale) // Ya escala, si no encuentra devuelve un texto
      }
      else
      {
         // FIXME
         nameN = 'Termino no encontrado en el arquetipo ['+archetypeId+'], '+
               'para el nodo ['+archNodeId+']'
      }
      DvText nameNode = new DvText(value: nameN)

      locatable.archetypeNodeId = archNodeId
      locatable.name = nameNode
      locatable.archetypeDetails = archDetails
   }

   /**
    * Operación que encapsula el seteo de los atrinutos encoding, language
    * del los Objetos del Entry creados.
    */
   def completarEntry (Entry e)
   {
      TerminologyID tidE = TerminologyID.create("TODO_E", null) // TODO, Obtenerlo de algún lado
      TerminologyID tidL = TerminologyID.create("TODO_L", null) // TODO, Obtenerlo de algún lado

      e.encoding = new CodePhrase(codeString: "TODO", terminologyId: tidE)
      e.language = new CodePhrase(codeString: "TODO", terminologyId: tidL)
   }

   /**
   * Operación que copia los valores de todos los atributos de rmObjectRootSec
   * a rmObject. Esta operación se utiliza en la operacion completarRMOAS de la clase
   * BindingAOMRM para armar el arbol del RM completo para un template.
   */
   def clonarRMO(Locatable rmObject, Locatable rmObjectRootSec)
   {
      // PAB: con groovy podes pedir los atributos como una lista, iterarlos e irlos seteando por su nombre (te evita hacer el switch).
      //     creo que haciendo rmObject.class.fields te da todos los campos, hay que probar.
      rmObject.archetypeNodeId = rmObjectRootSec.archetypeNodeId
      rmObject.name = rmObjectRootSec.name
      rmObject.archetypeDetails = rmObjectRootSec.archetypeDetails
      Class tipoRM = rmObjectRootSec.getClass()
      switch(tipoRM) {
         case Cluster:
            rmObject.items = rmObjectRootSec.items
            break;
         case Element:
            rmObject.null_flavor = rmObjectRootSec.null_flavor
            rmObject.value = rmObjectRootSec.value
            break;
         case ItemList:
            rmObject.items = rmObjectRootSec.items
            break;
         case ItemSingle:
            rmObject.item = rmObjectRootSec.item
            break;
         case ItemTable:
            rmObject.rows = rmObjectRootSec.rows
            break;
         case ItemTree:
            rmObject.items = rmObjectRootSec.items
            break;
         case CareEntry:
            break;
         case Action:
            rmObject.time = rmObjectRootSec.time
            rmObject.description = rmObjectRootSec.description
            break;
         case Observation:
            rmObject.data = rmObjectRootSec.data
            break;
         case Instruction:
            rmObject.narrative = rmObjectRootSec.narrative
            rmObject.expiryTime = rmObjectRootSec.expiryTime
            rmObject.wfDefinition = rmObjectRootSec.wfDefinition
            rmObject.activities = rmObjectRootSec.activities
            break;
         case Evaluation:
            rmObject.data = rmObjectRootSec.data
            break;
         case Section:
            rmObject.items = rmObjectRootSec.items
            break;
         case Composition:
            rmObject.context = rmObjectRootSec.context
            rmObject.category = rmObjectRootSec.category
            rmObject.territory = rmObjectRootSec.territory
            rmObject.language = rmObjectRootSec.language
            rmObject.content = rmObjectRootSec.content
            break;
         default:
            println "tipoRM no considerado: " + tipoRM.ToString()
            break;
      }
   }

   //--------------------------------------------------------------------------

   /**
   * Operación que crea un objeto del RM "vacio". Utilizada para
   * bindear ArchetypeSlots.
   *
   * FIXME: Locatable es un super-tipo, nunca es utilizado directamente.
   *      ¿desde donde se llama a este metodo? desde BindingAOMRM.bindArchetypeSlot()
   *      pero no se si es correcto llamar solo para crear una instancia vacia...
   *      
   * TODO: necesito el arquetipo que se usa para crear este locatable... para setearle su
   *      archetypeDetails aqui, asi luego puedo saber con que arquetipo se arquetipo y
   *      que nodo se usa para hacerlo.
   */
   def createLOCATABLE(String tipoRM, String archNodeId, Archetype arquetipo, String tempId, CObject co)
   {
      //println "|||||||||||||||||||||||||||||||||||||||||||||||||"
      //println "|||| CREATE LOCATABLE type: " + tipoRM + " |||||"
      //println "|||||||||||||||||||||||||||||||||||||||||||||||||"
      Locatable rmObject
      switch(tipoRM)
      {
         case "CLUSTER":
            rmObject = new Cluster()
         break
         case "ELEMENT":
            rmObject = new Element()
         break
         case "ITEM_LIST":
            rmObject = new ItemList()
            break;
         case "ITEM_SINGLE":
            rmObject = new ItemSingle()
            break;
         case "ITEM_TABLE":
            rmObject = new ItemTable()
         break
         case "ITEM_TREE":
            rmObject = new ItemTree()
         break
         case "CARE_ENTRY":
            rmObject = new CareEntry()
         break
         case "ACTION":
            rmObject = new Action()
         break
         case "OBSERVATION":
            rmObject = new Observation()
         break
         case "INSTRUCTION":
            rmObject = new Instruction()
         break
         case "EVALUATION":
            rmObject = new Evaluation()
         break
         case "ADMIN_ENTRY":
            rmObject = new AdminEntry()
         break
         case "SECTION":
            rmObject = new Section()
         break
         case "COMPOSITION":
            rmObject = new Composition()
         break
         default:
            println "tipoRM no considerado: " + tipoRM
         break
      }

      completarLocatable(rmObject, archNodeId, arquetipo, tempId)

      return rmObject
   }

   // -----------------------------
   // data_structure.representation
   // -----------------------------

   def createSECTION(List<Object> listaListRMO, Archetype arquetipo, String archNodeId, String tempId, CObject co)
   {
      Section sec = new Section()
      List<Object> listaItems = listaListRMO[0]
      
      /*
      println "--"
      println "--"
      println "-- createSECTION listListRMO: " + listaListRMO
      println "--"
      println "--"
      */

      if (listaItems.size() == 0) return null

      listaItems.each{ item ->
      
         if (item)
            sec.addToItems(item)
      }

      completarLocatable(sec, archNodeId, arquetipo, tempId)
      return sec
   }
   
   

   def createITEM_TREE(List<Object> listaListRMO, Archetype arquetipo, String archNodeId, String tempId, CObject co)
   {
      // FIXME: si no vienen items, retornar null
      ItemTree tree = new ItemTree()
      List<Object> listaItems = listaListRMO[0]
   
      //println "--------------------------------------------------------"
      //println "---------- ITEM TREE items: " + listaListRMO
      //println "--------------------------------------------------------"
      
      // Dejo bindear para mostrar errores del GORM
      // Si no tengo items, no tengo arbol
      //if (listaItems.size()==0) return null
   
      listaItems.each { item ->
         if (item)
         {
            tree.addToItems(item)
         }
      }
      
      // TODO: validar las ocurrencias de cada nodo hijo
      // 1. fijarse en el arquetipo, CObject los hijos de mi CAttribute items
      // 2. para cada uno,
      //   2.1. pedir su ocurrencia
      //   2.2. pido los items correspondientes a tree.items
      //   2.3. veo si la cantidad de items que hay en tree.items para ese CObject satisface la ocurrencia minima
      //   2.4. veo si la cantidad de items que hay en tree.items para ese CObject satisface la ocurrencia maxima
      //
      // Los errores de validacion se ponen en el tree para el atributo items.
      
      // Validate de cardinalidad
      //      
      // Quiero la restriccion para el atributo items del tree
      def multipleAttr = co.attributes.find{ it.rmAttributeName == "items" }
      if (multipleAttr.cardinality.interval.lower > tree.items.size())
      {
         tree.errors.rejectValue("items", "ITEM_TREE.error.cardinality")
      }
      
      // TODO: validar el maximo

      completarLocatable(tree, archNodeId, arquetipo, tempId)
      return tree
   }

   def createITEM_SINGLE(List<Object> listaListRMO, Archetype arquetipo, String archNodeId, String tempId, CObject co)
   {
      //println "== ITEM SINGLE"
      //println "==== listaListRMO: " + listaListRMO
      
      List<Object> listaItems = listaListRMO[0]
      ItemSingle single = new ItemSingle(item: listaItems[0])
      
      // Validate de cardinalidad
      // Esto no lo puedo poner en las constraints porque depende del arquetipo (COject)
      
      if ( co.attributes[0].getChildren()[0].occurrences.lower == 1 && !single.item )
      {
         single.errors.rejectValue("item", "ITEM_SINGLE.error.occurrences")
      }
      
      completarLocatable(single, archNodeId, arquetipo, tempId)
      return single
   }

   def createITEM_LIST(List<Object> listaListRMO, Archetype arquetipo, String archNodeId, String tempId, CObject co)
   {
      //println "---------- ITEM LIST items: " + listaListRMO
      
      List<Object> listaItems = listaListRMO[0]
      
      ItemList list = new ItemList()
      listaItems.each { item ->
         if (item)
         {
            if (item instanceof List) // FIXME: porque para tree no se verifica que venga una lista de listas
            {
               item.each { subItem ->
                  if (subItem) list.addToItems(subItem)
               }
            }
            else
            {
               list.addToItems(item)
            }
         }
      }
      
      // TODO: validar las ocurrencias de cada nodo hijo
      // 1. fijarse en el arquetipo, CObject los hijos de mi CAttribute items
      // 2. para cada uno,
      //   2.1. pedir su ocurrencia
      //   2.2. pido los items correspondientes a list.items
      //   2.3. veo si la cantidad de items que hay en list.items para ese CObject satisface la ocurrencia minima
      //   2.4. veo si la cantidad de items que hay en list.items para ese CObject satisface la ocurrencia maxima
      //
      // Los errores de validacion se ponen en el list para el atributo items.
      
      // Validate de cardinalidad
      //
      // Quiero la restriccion para el atributo items de la lista
      def multipleAttr = co.attributes.find{ it.rmAttributeName == "items" }
      if (multipleAttr.cardinality.interval.lower > list.items.size())
      {
         list.errors.rejectValue("items", "ITEM_LIST.error.cardinality")
      }

      completarLocatable(list, archNodeId, arquetipo, tempId)
      return list
   }

   def createITEM_TABLE(List<Object> listaListRMO, Archetype arquetipo, String archNodeId, String tempId, CObject co)
   {
      ItemTable table = new ItemTable()
      List<Object> listaItems = listaListRMO[0]

      listaItems.each { item ->
         if (item)
         {
            table.addToRows(item)
         }
      }
      
      // TODO: validar las ocurrencias de cada nodo hijo
      // 1. fijarse en el arquetipo, CObject los hijos de mi CAttribute items
      // 2. para cada uno,
      //   2.1. pedir su ocurrencia
      //   2.2. pido los items correspondientes a table.rows
      //   2.3. veo si la cantidad de items que hay en table.rows para ese CObject satisface la ocurrencia minima
      //   2.4. veo si la cantidad de items que hay en table.rows para ese CObject satisface la ocurrencia maxima
      //
      // Los errores de validacion se ponen en la table para el atributo rows.
      
      
      // Validate de cardinalidad
      //
      // Quiero la restriccion para el atributo rows de la table
      def multipleAttr = co.attributes.find{ it.rmAttributeName == "rows" }
      if (multipleAttr.cardinality.interval.lower > table.items.size())
      {
         table.errors.rejectValue("items", "ITEM_TABLE.error.cardinality")
      }
      
      completarLocatable(table, archNodeId, arquetipo, tempId)
      return table
   }
   
   
   // =================================================================================
   // Soportar POINT e INTERVAL EVENT
   // https://code.google.com/p/open-ehr-gen-framework/issues/detail?id=53
   
   def createEVENT(List<Object> listaListRMO, Archetype arquetipo, String archNodeId, String tempId, CObject co)
   {
      //println "== createEVENT "+ listaListRMO
      
      Event e // Event representa PointEvent porque PointEvent no agrega mas informacion a la clase Event
      List<Object> listaItems = listaListRMO[0]
      if (listaItems.size() == 1)
      {
         e = new Event() // FIXME: deberia ser POINT_EVENT, EVENT es abstracta
         e.data = listaItems[0] // ItemStructure
         
         // TODO: tambien esta el e.state ItemStructre (ver Blod Pressure que lo usa)
         
         // FIXME: OJO con el formato! para crear la composition estoy usando iso8601ExtendedDateTimeFromParams
         // que deja yyyy-mm-dd, y esta de hl7 es yyyymmdd !!!!
         e.time = new DvDateTime(value: DateConverter.toHL7DateFormat(new Date()) )
         completarLocatable(e, archNodeId, arquetipo, tempId)
      }
      
      //println "==== return event: " + e
      //println "=================================================="
      
      return e
   }
   
   // Si en el arquetipo aparece EVENT que es abstracta, el evento por defecto es POINT_EVENT
   // FIXME: hay que implementar la clase POINT_EVENT en el RM
   def createPOINT_EVENT(List<Object> listaListRMO, Archetype arquetipo, String archNodeId, String tempId, CObject co)
   {
      return createEVENT(listaListRMO, arquetipo, archNodeId, tempId, co)
   }
   
   // La dejo asi por ahora para que no tire excepcion de que falta el metodo cuando un arquetipo tiene un interval event
   // FIXME: hay que implementar la clase INTERVAL_EVENT en el RM
   def createINTERVAL_EVENT(List<Object> listaListRMO, Archetype arquetipo, String archNodeId, String tempId, CObject co)
   {
      //println "createINTERVAL_EVENT " + listaListRMO
      
      // createINTERVAL_EVENT
      // [[],
      //  [data_types.quantity.date_time.DvDuration@1986321],
      //  [ItemTree-> [at0003] name: *blood pressure(en)],
      //  [ItemTree-> [at0007] name: *state structure(en)]
      // ]
      //
      // el elemento vacio no se que es, puede ser el interval event.mathFunction?
      // la duration es el blood pressure.interval event.width
      // 0003 es el blood pressure.data (referencia desde el event de 24h al point event)
      // 0007 es el blood pressure.state (referencia desde el event de 24h al point event)
      // falta el interval event.sample count
      
      //return createEVENT(listaListRMO, arquetipo, archNodeId, tempId, co)
      Event e
      List<Object> listaItems = listaListRMO[0]
      //if (listaItems.size() == 1)
      //{
         e = new IntervalEvent()
         
         e.mathFunction = listaItems[0] // DvCodedText
         e.width = listaItems[1] //DvDuration
         e.data = listaItems[2] // ItemStructure
         //e.state = listaItems[3] // ItemStructure TODO: no esta implementado el binding de Event.state
         
         // TODO: tambien esta el e.state ItemStructre (ver Blod Pressure que lo usa)
         
         // FIXME: OJO con el formato! para crear la composition estoy usando iso8601ExtendedDateTimeFromParams
         // que deja yyyy-mm-dd, y esta de hl7 es yyyymmdd !!!!
         e.time = new DvDateTime(value: DateConverter.toHL7DateFormat(new Date()) )
         completarLocatable(e, archNodeId, arquetipo, tempId)
      //}
      
      //println "==== return event: " + e
      //println "=================================================="
      
      return e
   }
   
   // ==========================================================================================
   // ==========================================================================================
   

   def createHISTORY(List<Object> listaListRMO, Archetype arquetipo, String archNodeId, String tempId, CObject co)
   {
      //println "===================================="
      //println "createHISTORY " + listaListRMO
      
      History h = new History()
      List<Object> listaEvents = listaListRMO[0]
      
      // Si no tiene eventos, no tengo history
      if (listaEvents.size() == 0) return null
      
      //imprimirObjetoXML(listaEvents)
      
      // Puede venir mas de un evento...
      //if (listaEvents.size() == 1)
      //{
         listaEvents.each { event ->

            if (event) h.addToEvents(event)
         }
   
         // FIXME: OJO con el formato! para crear la composition estoy usando iso8601ExtendedDateTimeFromParams
         // que deja yyyy-mm-dd, y esta de hl7 es yyyymmdd !!!!
         h.origin = new DvDateTime(value: DateConverter.toHL7DateFormat(new Date()) ) //new DvDateTime(value: "20091121")
         completarLocatable(h, archNodeId, arquetipo, tempId)
      //}
      
      // TODO: validar las ocurrencias de cada nodo hijo
      // 1. fijarse en el arquetipo, CObject los hijos de mi CAttribute items
      // 2. para cada uno,
      //   2.1. pedir su ocurrencia
      //   2.2. pido los items correspondientes a h.events
      //   2.3. veo si la cantidad de items que hay en h.events para ese CObject satisface la ocurrencia minima
      //   2.4. veo si la cantidad de items que hay en h.events para ese CObject satisface la ocurrencia maxima
      //
      // Los errores de validacion se ponen en el h para el atributo events.
      
      // TODO: verificar que no haya otro caso.

      return h
   }

   def createACTIVITY(List<Object> listaListRMO, Archetype arquetipo, String archNodeId, String tempId, CObject co)
   {
      // [[], [ItemTree-> [at0003] name: Arbol]]
      //println "createACTIVITY: " + listaListRMO
      
      /*
      // Quiero ver donde esta el arction_archetype_id
      // Cuidado, puede no estar el atributo, esos arquetipos no se deberian dejar subir!
      println ""
      println co.attributes.find{ it.rmAttributeName == 'action_archetype_id' } // CSingleAttribute
      println co.attributes.find{ it.rmAttributeName == 'action_archetype_id' }.children[0] // CPrimitiveObject
      println co.attributes.find{ it.rmAttributeName == 'action_archetype_id' }.children[0].item // CString
      println co.attributes.find{ it.rmAttributeName == 'action_archetype_id' }.children[0].item.pattern // String
      println ""
      imprimirObjetoXML( co.attributes.find{ it.rmAttributeName == 'action_archetype_id' } )
      println ""
      */
      /*
      <org.openehr.am.archetype.constraintmodel.CSingleAttribute>
        <anyAllowed>false</anyAllowed>
        <path>/activities[at0002]/action_archetype_id</path>
        <rmAttributeName>action_archetype_id</rmAttributeName>
        <existence>REQUIRED</existence>
        <children>
          <org.openehr.am.archetype.constraintmodel.CPrimitiveObject>
            <anyAllowed>false</anyAllowed>
            <path>/activities[at0002]/action_archetype_id</path>
            <rmTypeName>String</rmTypeName>
            <occurrences>
              <lower class="int">1</lower>
              <upper class="int">1</upper>
              <lowerIncluded>true</lowerIncluded>
              <upperIncluded>true</upperIncluded>
            </occurrences>
            <item class="org.openehr.am.archetype.constraintmodel.primitive.CString">
              <pattern>openEHR-EHR-ACTION\.prueba</pattern>
            </item>
          </org.openehr.am.archetype.constraintmodel.CPrimitiveObject>
        </children>
      </org.openehr.am.archetype.constraintmodel.CSingleAttribute>
      */
      
      Activity a = new Activity()
      //List<Object> listaItems = listaListRMO[0]
      
      // Deben venir 2 valores para atributos de ACTIVITY:
      //   action_archerype_id y description
      //   action_archerype_id viene vacio porque no es un valor de input, se saca del arquetipo
      //     esto garantiza que siempre hay un valor en el arquetipo: http://code.google.com/p/open-ehr-gen-framework/issues/detail?id=112
      //
      //   FIXME: hay que ver de donde sacar el valor para el tercer atributo: timing
      if (listaListRMO.size() == 2)
      {
         a.description = listaListRMO[1][0] // ItemTree // [[], [ItemTree-> [at0003] name: Arbol]]
         
         a.timing = new DvParsable(value: "value", formalism: "formalism") // FIXME: de donde sacar los valores?
         
         //a.action_archetype_id = arquetipo.archetypeId.value // FIXME: action_archetype_id es una regex y es de arquetipos de ACTION, aca setea el propio arquetipo...
         //                         attribute CSingleAttribute > children[0] CPrimitiveObject > item CString > pattern String
         a.action_archetype_id = co.attributes.find{ it.rmAttributeName == 'action_archetype_id' }.children[0].item.pattern
         
         completarLocatable(a, archNodeId, arquetipo, tempId)
      }
      else
      {
         throw new Exception("Verifique que el arquetipo "+ arquetipo.archetypeId.value +" especifica el atributo ACTIVITY.action_archetype_id")
      }

      return a
   }

   
   //----------------------------------------------------------------------
   // ENTRY
   //----------------------------------------------------------------------

   def createOBSERVATION(List<Object> listaListRMO, Archetype arquetipo, String archNodeId, String tempId, CObject co)
   {
      Observation o
      List<Object> listaItems = listaListRMO[0]
      
      /*
      println "--"
      println "--"
      println "-- createOBSERVATION listListRMO: " + listaListRMO
      println "--"
      println "--"
      */
      
      if (listaItems.size() == 1)
      {
         o = new Observation()
         o.data = listaItems[0]
         completarLocatable(o, archNodeId, arquetipo, tempId)
         completarEntry(o)
      }

      // TODO: no puede haber un caso donde vengan 2 elementos,
      // y si lo hay cae aca y deberia tirar except o algo...
      
      return o
   }
   
   def createEVALUATION(List<Object> listaListRMO, Archetype arquetipo, String archNodeId, String tempId, CObject co)
   {
      //println "== createEVALUATION"
      //println "==== listaList: " + listaListRMO
      //println "============================================="
      
      Evaluation e
      List<Object> listaItems = listaListRMO[0]

      // Si viene algo es un item_structure
      if (listaItems.size() == 1)
      {
         e = new Evaluation()
         e.data = listaItems[0]
         completarLocatable(e, archNodeId, arquetipo, tempId)
         completarEntry(e)
      }
      
      // TODO: No deberia haber otro caso, no esta de mas chequear
      
      return e
   }

   def createINSTRUCTION(List<Object> listaListRMO, Archetype arquetipo, String archNodeId, String tempId, CObject co)
   {
      println "== createINSTRUCTION " + listaListRMO + " size: "+ listaListRMO.size()
      
      // En listaListRMO viene:
      // - Siempre: un DvText que es el narrative
      // - Opcional: lista de Activities
      
      Instruction instruction = new Instruction()
      
      //println "~+~+~+~+~+~+~+~+~+~+~+~+~+~+~+~+~+~+~+~+~+~+~+~+~~+~+~+~+~"
      //println "~+~+~+~+~+~+~+~+~+~+~+~+~+~+~+~+~+~+~+~+~+~+~+~+~~+~+~+~+~"
      //imprimirObjetoXML(listaListRMO)
      //println "~+~+~+~+~+~+~+~+~+~+~+~+~+~+~+~+~+~+~+~+~+~+~+~+~~+~+~+~+~"
      //println "~+~+~+~+~+~+~+~+~+~+~+~+~+~+~+~+~+~+~+~+~+~+~+~+~~+~+~+~+~"
      
      /*
      == createINSTRUCTION [
        [Activity-> [at0001] name: Request], 
        [ItemTree-> [at0008] name: Tree], 
        [DvText dfh dfhd dfgh ff]
      ]
      
      == createINSTRUCTION [
        [Activity-> [at0001] name: Current Activity], 
        [DvText fhfghfhfhfhfghf], 
        [DvText fhfghfhfhfhfghf]]
      */
      
      DvText narrative
      List activities = []
      ItemStructure protocol // puede no venir
      
      if (listaListRMO.size()==1) // viene solo el narrative
      {
         if (listaListRMO[0][0] instanceof DvText)
            narrative = listaListRMO[0][0] // el narrative es el primer elemento de la primer lista
         else
            throw new Exception("Se esperaba un DvText y se obtuvo un " + listaListRMO[0][0].getClass() + " revisar el arquetipo de la Instruction porque seguramente no se definio el nodo narrative que es obligatorio, ver: " + arquetipo.archetypeId.value)
         
      }
      else // viene narrative y activities
      {
         listaListRMO.each { dataList ->
         
            if (dataList[0] instanceof DvText)
            {
               narrative = dataList[0]
            }
            else if (dataList[0] instanceof Activity)
            {
               activities = dataList
            }
            else if (dataList[0] instanceof ItemStructure)
            {
               protocol = dataList[0]
            }
            else
            {
               throw Exception("El item "+ dataList[0] +" no es ItemStructure, DvText o Activity")
            }
         }
         
         /*
         if (listaListRMO[0] instanceof List) // el primer elemento son las activities
         {
            activities = listaListRMO[0]
            narrative  = listaListRMO[1][0] // el narrative es el primer elemento de la segunda lista
         }
         else // el primer elemento el el narrative
         {
            activities = listaListRMO[1]
            narrative  = listaListRMO[0][0] // el narrative es el primer elemento de la primer lista
         }
         */
      }
      // No tengo otro caso posible
      
      
      instruction.narrative = narrative
      instruction.protocol  = protocol
      
      activities.each{ activity ->
      
         if (activity) instruction.addToActivities(activity)
      }

      completarLocatable(instruction, archNodeId, arquetipo, tempId)
      completarEntry(instruction)
      
      return instruction
   }

   def createACTION(List<Object> listaListRMO, Archetype arquetipo, String archNodeId, String tempId, CObject co)
   {
      //println "== createACTION"
      //println "==== listaListRMO: "+ listaListRMO
      
      Action a = new Action()
      List<Object> listaItems = listaListRMO[0]
      
      //if (listaItems.size()==0) return null
      
      //println listaItems + " sz:"+listaItems.size() + " class:"+listaItems.getClass().getSimpleName()
      //println "==============================================="
      
      if (listaItems.size() == 1)
      {
         a.description = listaItems[0] // Se sabe que description es oblig.
         
         // FIXME: mal fecha
         //a.time = new DvDateTime(value: "20091121")
         // FIXME: OJO con el formato! para crear la composition estoy usando iso8601ExtendedDateTimeFromParams
         // que deja yyyy-mm-dd, y esta de hl7 es yyyymmdd !!!!
         a.time = new DvDateTime(value: DateConverter.toHL7DateFormat(new Date()) )
      }

      completarLocatable(a, archNodeId, arquetipo, tempId)
      completarEntry(a)
      return a
   }

   def createADMIN_ENTRY(List<Object> listaListRMO, Archetype arquetipo, String archNodeId, String tempId, CObject co)
   {
      //println "=== ADMIN_ENTRY ==="
      //println "=== listaListRMO: " + listaListRMO
      AdminEntry ae = new AdminEntry()
      List<Object> listaItems = listaListRMO[0]
      if (listaItems.size() == 1)
      {
         ae.data = listaItems[0]
      }

      completarLocatable(ae, archNodeId, arquetipo, tempId)
      completarEntry(ae)
      return ae
   }

// FIXME: no se le esta seteando la path al CLUSTER!
   def createCLUSTER(List<Object> listaItems, Archetype arquetipo, String archNodeId, String tempId, CObject co)
   {
      //println "========== createCLUSTER " + listaItems
      //println "??????????????? listaItems: " + listaItems
      //println "???????????????????????????????????????????????????"
      
      Cluster cluster = new Cluster()
      listaItems.each{ item ->
      
         if ( item ) cluster.addToItems(item)
      }
      
      // TODO: validar las ocurrencias de cada nodo hijo
      // 1. fijarse en el arquetipo, CObject los hijos de mi CAttribute items
      // 2. para cada uno,
      //   2.1. pedir su ocurrencia
      //   2.2. pido los items correspondientes a cluster.items
      //   2.3. veo si la cantidad de items que hay en cluster.items para ese CObject satisface la ocurrencia minima
      //   2.4. veo si la cantidad de items que hay en cluster.items para ese CObject satisface la ocurrencia maxima
      //
      // Los errores de validacion se ponen en el custer para el atributo items.
      
      // Validate de cardinalidad
      //
      // Quiero la restriccion para el atributo items del cluster
      def multipleAttr = co.attributes.find{ it.rmAttributeName == "items" }
      if (multipleAttr.cardinality.interval.lower > cluster.items.size())
      {
         cluster.errors.rejectValue("items", "CLUSTER.error.cardinality")
      }
      
      // TODO: verificar cardinalidad maxima

      completarLocatable(cluster, archNodeId, arquetipo, tempId)
      return cluster
   }


   def createELEMENT(DataValue value, Archetype arquetipo, String archNodeId, String tempId, CObject co)
   {
      // PAB:
      // Saque un TRY que era al pedo porque queria tirar una except y tiraba para cartchear
      // y tirar de nuevo, la tiro directamente.
      
      Element e = new Element(path: co.path())
      
      // FIXME: si no tiene value, deberia tener nullFlavor
      // Lo que me importa es que si el element es obligatorio, que tenga value no null_flavour.
      /*
      if (!value)
      {
         e.null_flavor = new DvCodedText(definingCode: new CodePhrase(codeString: "TODO"), value: "TODO" )
      }
      else
      */
         e.value = value
      
      // FIXME: no se le esta seteando la path al ELEMENT!, se deberia hacer en completarLocatable!
      
      completarLocatable(e, archNodeId, arquetipo, tempId)
      return e
   }

   
   // -----------------------------
   // data_types.basic
   // -----------------------------
   
   // DV_IDENTIFIER
   def createDV_IDENTIFIER(pathValor, Archetype arquetipo, String archNodeId, String tempId, CObject co)
   {
      // TODO
      return new DvIdentifier(assigner: "assigner", code: "code", issuer: "issuer", type: " type")
   }

   // DV_STATE
   def createDV_STATE(pathValor, Archetype arquetipo, String archNodeId, String tempId, CObject co)
   {
      // TODO
      return new DvState(terminal: false, new DvCodedText(definingCode: new CodePhrase(codeString: "TODO"), value: "TODO" ))
   }

   // -----------------------------
   // data_types.encapsulated
   // -----------------------------

   // DV_ENCAPSULATED
   // TODO: ?
   
   /**
    * org.springframework.web.multipart.MultipartFile es el tipo de los archivos que se suben desde la web.
    */
   def createDV_MULTIMEDIA (
         org.springframework.web.multipart.MultipartFile file,
         Archetype arquetipo,
         String archNodeId,
         String tempId, CObject co)
   {
      //println "createDV_MULTIMEDIA"
      //println "--- file: " + file
      //println "======================================================="
      
      def mm = new DvMultimedia()
      
      mm.data = new byte[file.size]
      file.inputStream.read( mm.data ) // file -> mm.data
      
      // FIXME: ver los tipos en la terminologia openehr...
      mm.mediaType = new CodePhrase(
                   codeString: file.contentType,
                   terminologyId: TerminologyID.create('openehr', null)
                  )
      
      // FIXME: ver si el alternate text esta bien usado
      mm.alternateText = file.originalFilename
      mm.size = file.size // tamanio en bytes

      return mm
   }

   // DV_PARSABLE
   def createDV_PARSABLE(pathValor, Archetype arquetipo, String archNodeId, String tempId, CObject co)
   {
      // TODO
      return new DvParsable(value: "TODO", formalism: "TODO")
   }

   // -----------------------------
   // data_types.quantyty
   // -----------------------------

   //DV_ABSOLUTE_QUANTITY
   // TODO: ?
   
   //DV_AMOUNT
   // TODO: ?

   // DV_COUNT
   //def createDV_COUNT(List<Object> listaListRMO, Archetype arquetipo, String archNodeId, String tempId)
   // El valor es string y lo tengo que pasar a INT, ahi puedo tirar except por si no tiene formato correcto.
   // El value viene bindeado de createInteger
   //def createDV_COUNT(Integer value, Archetype arquetipo, String archNodeId, String tempId)
   // pruebo pasarle string para que valide en GORM, el deberia decir si es null o si esta mal formado al pasar a int.
   def createDV_COUNT(String value, Archetype arquetipo, String archNodeId, String tempId, CObject co)
   {
      //println "----- createDV_COUNT value: " + value
      
      // prueba: para que si no hay valor ni siquiera siga y si el ELEMENT era
      //       obligatorio, deberia saltar el error al verificar ocurrencias.
      /* ABC*
       * Si retorno null pasa esto>
       * Caused by: java.lang.NullPointerException: Cannot get property 'errors' on null object
       *    at binding.BindingAOMRM.bindDV_COUNT(BindingAOMRM.groovy:2367)
       */
      if (!value) return null
      
      // Reescribo string vacio a null, para que de el error correcto en el GORM
      def dvcount = new DvCount()
      Integer magnitude
      try
      {
         //println "count value: " + value
         magnitude = Integer.parseInt(value)
         dvcount.magnitude = magnitude
         
         
         // FIXME: verificar que el validate no sobreescriba el error de validacion de pasarle in string no numerico en el value
         

         // verifyDvCountRange
         def cattr = co.attributes.find{it.rmAttributeName=='magnitude'}
         if ( cattr )
         {
           // FIXME: implementar la validacion en el validate de la clase.
           def interval = cattr.children[0].item.interval
           if ( interval )
           {
             if (interval.lower != null && dvcount.magnitude < interval.lower)
             {
               dvcount.errors.rejectValue('magnitude', 'error.range.min')
             }
             if (interval.upper != null && dvcount.magnitude > interval.upper)
             {
               dvcount.errors.rejectValue('magnitude', 'error.range.max')
             }
           }
         }
         // /verifyDvCountRange
      }
      catch (Exception e)
      {
         //println "count error: " + e.getMessage()
         
         // El dvcount no va a tener el error porque salta la excepcion antes del validate.
         //if ( dvcount.errors.getFieldError('magnitude') )
         //{
            // http://static.springsource.org/spring/docs/2.5.x/api/index.html?org/springframework/validation/FieldError.html
            //dvcount.errors = new BeanPropertyBindingResult(dvcount, 'DvCount') // Borro el error actual y meto uno con el rejectedValue correcto.
            
            // FieldError(String objectName, String field, Object rejectedValue, boolean bindingFailure, String[] codes, Object[] arguments, String defaultMessage)
            /*
            dvcount.errors.addError(
               new FieldError('DvCount', 'magnitude', value, false,
                  ["typeMismatch.java.lang.Integer"] as String[], null, null
               )
            )
            */
            dvcount.errors.rejectValue('magnitude', "typeMismatch.java.lang.Integer")
            
            //println "count error en magnitude: typeMismatch.java.lang.Integer"
         //}
      }
      
      // Es necesario validar aca por el ticket #23: http://code.google.com/p/open-ehr-sa/issues/detail?id=23
      dvcount.validate()
      
      return dvcount
   }
   
   
   // Quiero que valide el GORM por eso le paso un string en el value.
   // Value sera true, false, '' o null.
   def createDV_BOOLEAN(String value, Archetype arquetipo, String archNodeId, String tempId, CObject co)
   {
      // Reescribo string vacio a null, para que de el error correcto en el GORM
      Boolean bool = null
      DvBoolean ret = new DvBoolean()
      //println "createDV_BOOLEAN value: " + value
      if (value)
      {
         // si viene label.boolean.true o label.boolean.false
         bool = 'label.boolean.true' == value;
         ret.value = bool
         
         /* si viene string true/false
         try
         {
           bool = Boolean.valueOf(value)
           ret.value = bool
         }
         catch (Exception e)
         {
           ret.errors.addError(
             new FieldError('DvBoolean', 'value', value, false,
                ["typeMismatch.java.lang.Boolean"] as String[], null, null
             )
           )
         }
         */
      }
      
      // FIXME: no valida los que son null, pero si el element que lo contiene no es obligatorio, no deberia tirar error
      // Tiene que hacerse haya o no value
      //if (!ret.validate()) println "No valida DvBoolean value:" + value
      
      ret.validate()
      
      return ret
   }
   
   /**
    * FIXME:
    * Esta mal el nombre del tipo en el archetype parser, en lugar de Boolean pone
    * DvBoolean. Ver que para Integer pone Integer no DvInteger. Ver que de esto
    * hubo una discusion en la mail list hace un tiempo. 
    */
   //def createDvBoolean(String value, Archetype arquetipo, String archNodeId, String tempId)
   //{
   //   return Boolean.parseBoolean(value)
   //}

   // DV_INTERVAL
   def createDV_INTERVAL(pathValor, Archetype arquetipo, String archNodeId, String tempId, CObject co)
   {
      // TODO
      return new DvInterval()
   }

   //DV_ORDERED
   // TODO: ?

   //DV_ORDINAL
   // Se usa createDvOrdinal

   // DV_PROPORTION
   def createDV_PROPORTION(pathValor, Archetype arquetipo, String archNodeId, String tempId, CObject co)
   {
      // TODO
      return new DvProportion() // Da error en versiones > Grails 1.1.1
   }

   //DV_QUANTIFIED
   // TODO: ?

   // PROPORTION_KIND
   def createPROPORTION_KIND(pathValor, Archetype arquetipo, String archNodeId, String tempId, CObject co)
   {
      return ProportionKind.RATIO
   }

   // REFERENCE_RANGE
   def createREFERENCE_RANGE(pathValor, Archetype arquetipo, String archNodeId, String tempId, CObject co)
   {
      // TODO
      ReferenceRange rr = new ReferenceRange(range: new DvInterval())
      return rr
   }

   // -----------------------------
   // data_types.quantity.date_tyme
   // -----------------------------

   // DV_DATE
   def createDV_DATE(String year, String month, String day, Archetype arquetipo, String archNodeId, String tempId, CObject co)
   {
      //println "ENTRO DV_DATE"
      String fechaISO8601 = crearFechaISO8601(year, month, day, "", "", "")
      //println "---->" + fechaISO8601
      return new DvDate(value: fechaISO8601)
   }

   // DV_DATE_TIME
   def createDV_DATE_TIME(String year, String month, String day, String hour, String minute, String seg, Archetype arquetipo, String archNodeId, String tempId, CObject co)
   {
      //println "ENTRO DV_DATE_TIME"
      String fechaISO8601 = crearFechaISO8601(year, month, day, hour, minute, seg)
      //println "---->" + fechaISO8601
      return new DvDateTime(value: fechaISO8601)
   }

   /*def createDV_DATE_TIME(List<Object> listaListRMO, Archetype arquetipo, String archNodeId, String tempId){

      if (listaListRMO.flatten() == []){
         return null
      }
      try{
         if (listaListRMO[0].size() == 1){
            // Creo un string con formato ISO 8601
            String fechaISO8601 = (String)(listaListRMO[0][0])
            return new DvDateTime(value: fechaISO8601)
         }
         throw new Exception()
      }
      catch (Exception exc){
         throw new Exception("createDV_DATE_TIME con listaListRMO: Colección de listaListRMO no contiene un solo elemento (que debería ser string).")
      }
   }*/

   // DV_DURATION
   def createDV_DURATION(int years, int months, int days, int hours, int minutes, int seconds,
                         Archetype arquetipo, String archNodeId, String tempId, CObject co)
   {
      //println "FactoryObjectRM.createDV_DURATION" 
   
      String value = "P"
      
      if (years)   value += years + "Y"
      if (months)  value += months + "M"
      if (days)    value += days + "D"
      
      if (hours || minutes || seconds) value += "Ts"
      
      if (hours)   value += hours + "H"
      if (minutes) value += minutes + "M"
      if (seconds) value += seconds + "S"
      
      DvDuration d = new DvDuration(value: value)
      
      return d
   }

   //DV_INTERVAL

   //DV_TEMPORAL

   // DV_TIME
   def createDV_TIME(String hour, String minute, String seg, Archetype arquetipo, String archNodeId, String tempId, CObject co)
   {
      //println "ENTRO DV_TIME"
      String fechaISO8601 = crearFechaISO8601("", "", "", hour, minute, seg)
      //println "---->" + fechaISO8601
      return new DvTime(value: fechaISO8601)

      /*
      // Encuentro los valores en la coleccion de path valor
      String hour = pathValor.find{it.key.endsWith("hour")}?.value
      String minute = pathValor.find{it.key.endsWith("minute")}?.value
      String seg = pathValor.find{it.key.endsWith("seg")}?.value

      if ((month != null) && (day != null) && (hour != null)){
         // Creo un string con formato ISO 8601
         String fechaISO8601 = crearFechaISO8601("", "", "", hour, minute, seg)
         //return new DvDateTime(value: fechaISO8601)
         return new DvTime(value: fechaISO8601)
      }
      else{
         return null
         //throw new Exception("createDV_DATE_TIME: Colección de pathValor no tiene path a 'year' o 'month' o 'day' u 'hour'.")
      }
      */
   }

   /*def createDV_TIME(List<Object> listaListRMO, Archetype arquetipo, String archNodeId, String tempId){
      // TODO

      if (listaListRMO.flatten() == []){
         return null
      }
      try{
         if (listaListRMO[0].size() == 1){
            // Creo un string con formato ISO 8601
            String fechaISO8601 = (String)(listaListRMO[0][0])
            return new DvTime(value: fechaISO8601)
         }
         throw new Exception()
      }
      catch (Exception exc){
         throw new Exception("createDV_DATE_TIME con listaListRMO: Colección de listaListRMO no contiene un solo elemento (que debería ser string).")
      }
   }*/

   // -----------------------------
   // data_types.text
   // -----------------------------

   //CODE_PHRASE
   /**
    * TODO: verificar si esta se usa. Creo que solo se usa createCodePhrase
    * @param pathValor
    * @param arquetipo
    * @param archNodeId
    * @param tempId
    * @param co
    * @return
    */
   /*
   def createCODE_PHRASE(pathValor, Archetype arquetipo, String archNodeId, String tempId, CObject co)
   {
      if (pathValor.size() == 0)
      {
         return null
      }
      if (pathValor.size() == 1)
      {
         String defCode = pathValor.find{it.key.endsWith("defining_code")}?.value
         String[] defCodes = defCode.split(",") // ???
         
         println "createCODE_PHRASE: defCode="+defCode
         println ""

         // Si tiene mas de un valor para la path, retorno una lista de CodePhrase
         if (defCodes.size() > 1)
         {
            LinkedList<CodePhrase> listaCodePhrase = new LinkedList<CodePhrase>()
            defCodes.each { dc ->
               listaCodePhrase.add(createCodePhrase(ccp, dc.replace(' ',''), arquetipo, archNodeId, tempId))
            }

            return listaCodePhrase
         }
         
         String dc = pathValor.find{it.key.endsWith("defining_code")}?.value
         if (dc)
         {
            return createCodePhrase(ccp, dc, arquetipo, archNodeId, tempId)
         }
         else
         {
            return null
         }
      }
      else
      {
         throw new Exception("bindCCodePhrase: Colección de pathValor no tiene 1 elemento.")
      }
   }
   */

   def createDV_CODED_TEXT(CodePhrase definingCode, Archetype arquetipo, String archNodeId, String tempId, CObject co)
   {
      //println "createDV_CODED_TEXT: defCode.cs="+definingCode.codeString
      //println ""

      // Voy a buscar el termino por el codigo
      Locale locale = this.session.locale
      String val = null
      
      if (definingCode) val = CtrlTerminologia.getInstance().getTermino(definingCode.terminologyId, definingCode.codeString, arquetipo, locale)

      // language y encoding para el codedText
      // TODO: deberia salir de la config
      // FIXME: ni siquiera se deberian guardar porque van a ser los mismos para todos los registros de estasinstancia del sistema.
      
      DvCodedText codedText = new DvCodedText(
         definingCode: definingCode,
         value: val,
         language: new CodePhrase(
           codeString: locale.toString(), // p.e. es-UY
           terminologyId: TerminologyID.create('ISO_639-1', null)
         ),
         encoding: new CodePhrase(
           codeString: 'UTF-8',
           terminologyId: TerminologyID.create('IANA_character-sets', null)
         )
      )
      
      // Valida para verificar errores
      codedText.validate()
      
      return codedText
   }

   def createDV_PARAGRAPH(pathValor, Archetype arquetipo, String archNodeId, String tempId, CObject co)
   {
      // TODO
      return new DvParagraph()
   }

   //DV_TEXT
   def createDV_TEXT(String value, Archetype arquetipo, String archNodeId, String tempId, CObject co)
   {
      // Reescribo string vacio a null, para que de el error correcto en el GORM
      if (!value) value = null
      
      // FIXME: encodear value en UTF-8 para que concuerde con text.encoding
      def text = new DvText(value: value)
      
      // TODO: sacar language de config
      // FIXME: ni siquiera se deberian guardar porque van a ser los mismos para todos los registros de estasinstancia del sistema.
      text.language = new CodePhrase(
         codeString: 'es-UY',
         terminologyId: TerminologyID.create('ISO_639-1', null)
      )
      text.encoding = new CodePhrase(
         codeString: 'UTF-8',
         terminologyId: TerminologyID.create('IANA_character-sets', null)
      )
      
      text.validate()
      return text
   }


   //TERM_MAPPING
   def createTERM_MAPPING(pathValor, Archetype arquetipo, String archNodeId, String tempId, CObject co)
   {
      // TODO
      TerminologyID tid = TerminologyID.create("TODO", null) // Obtenerlo de algún lado
      CodePhrase cph =  new CodePhrase(codeString: "TODO", terminologyId: tid)

      return new TermMapping(match: TermMapping.UNKNOWN, target: cph)
   }

   // -----------------------------
   // data_types.time_spesification
   // -----------------------------
   // TODO?

   // -----------------------------
   // data_types.uri
   // -----------------------------

   // DV_EHR_URI
   def createDV_EHR_URI(pathValor, Archetype arquetipo, String archNodeId, String tempId, CObject co)
   {
      // TODO
      return new DvEHRURI(value: new URI("ehr.TODO"))
   }

   def createDV_URI(pathValor, Archetype arquetipo, String archNodeId, String tempId, CObject co)
   {
      // TODO
      return new DvURI(value: new URI("TODO"))
   }


   //-----------------------------------
   // C_DOMAIN_TYPE
   //-----------------------------------

   def createCodePhrase(CCodePhrase ccp, String cs, Archetype arquetipo, String archNodeId, String tempId, CObject co)
   {
      //println "createCodePhrase: cs="+ cs // at0009||Coloca
      //println ""
      
      // TEST con el texto y el codigo viniendo desde la web separeado por ||
      String[] parts = cs.split(/\|\|/) // code||texto
      //println "cs0: "+ parts[0] +" cs1: "+ parts[1]
      def tid = ccp.getTerminologyId() // TerminologyID del java-ref-impl, lo uso para crear un TerminologyID del GORM.
      return new CodePhrase(codeString: parts[0],
                      terminologyId: TerminologyID.create(tid.name(), tid.versionID()))
   }

   // Pruebo pasarle en magnitude el valor que recibo de la web para que el GORM valide y reporte errores.
   def createDvQuantity(String mag, String units, Archetype arquetipo, String archNodeId, String tempId, CObject co)
   {
      //println "createDvQuantity: mag " + mag + " units " + units

      /**
       * Si hay una sola opcion de unidades en la lista de restricciones, en la vista siempre
       * viene esa unidad como ingresada por el usuario.
       * En ese caso, si no ingreso nada, me marca el error, pero si el ELEMENT es opcional,
       * no deberia haber error. Ademas el usuario no sabe que se esta enviando la unidad
       * porque en realidad no se deberia enviar nada porque el usuario no ingreso nada.
       * Entonces, trato ese caso como si no se hubiera mandado nada y retorno null.
       */
      // Si hay una sola opcion para las unidades y no se ingresa magnitud:
      if (co.list.units.size() == 1 && mag == '') return null
      
      
      // ABC*
      // prueba para retornar null de createDvQuantity
      //if (mag!='0' && mag!='0.0' && mag!='0,0' && !mag && !units) return null // agrego el mag != 0 porque !0 da true y 0 es un valor posible para mag
      if ((mag == '' || mag == null) && (units == '' || units == null)) return null

      
      def q = new DvQuantity(units: units)
     
      //println "---------------------------------------> Entrando a createDvQuantity(double mag, String un)"
      // Reescribo string vacio a null, para que de el error correcto en el GORM
      Double magnitude = null
      if (mag)
      {
         // Nuevo por pruebas de salvar serializado
         try
         {
           magnitude = Double.valueOf(mag)
           q.magnitude = magnitude
           
           
           // Validacion de rango de magnitude
           // Verificar: co deberia ser CDvQuantity
           def cQuantityItem = co.list?.find { it.units == q.units }
           
           /*
           println "co.list: "+ co.list // [org.openehr.am.openehrprofile.datatypes.quantity.CDvQuantityItem@b9a0a9a8]
           println "co.list.units: "+ co.list.units
           println "q.units: "+ q.units
           println "cQuantityItem: "+ cQuantityItem // null
           */
           
           // FIXME: no me esta mostrando el error por max en la pagina para el template
           //      EVALUACION_PRIMARIA-ventilacion, se genera ok en errors de js y el
           //      nombre del container del campo con error esta ok> debe haber algo
           //      mal en el js.
           if (cQuantityItem && cQuantityItem.magnitude)
           {
              //println "cQuantityItem.magnitude: "+ cQuantityItem.magnitude
             
              // Si hay lower y la magnitude es menor que el lower, error por rango.
              if ( cQuantityItem.magnitude.lower != null && q.magnitude < cQuantityItem.magnitude.lower )
              {
                 q.errors.rejectValue('magnitude', 'error.range.min')
              }
              // Si hay uper y la magnitude es mayor que el upper, erro por rango
              else if ( cQuantityItem.magnitude.upper != null && q.magnitude > cQuantityItem.magnitude.upper )
              {
                 q.errors.rejectValue('magnitude', 'error.range.max')
              }
           }
           
           //println "errors1: "+ q.errors
         }
         catch (Exception)
         {
           // Con rejectValue no tengo el valor del string que causa el problema
           q.errors.rejectValue('magnitude', "typeMismatch.java.lang.Double")
           /*
           q.errors.addError(
             new FieldError('DvQuantity', 'magnitude', mag, false,
                ["typeMismatch.java.lang.Double"] as String[], null, null
             )
           )
           */
           
           // Quiero validar las units aunque haya fallado la magnitude
           q.magnitude = 0 // Le pongo cero para que no falle que no tiene magnitude, porque el error de magnitude ya lo verifique.
           
           //println "errors2: "+ q.errors
         }
      }
      q.validate()
      
      //println "errors3: "+ q.errors
      
      return q
   }

   // PAB: que es 's' ?
   // 's' es el codeString que se selecciona en la web como valor.
   def createDvOrdinal(CDvOrdinal cdvo, String s, Archetype arquetipo, String archNodeId, String tempId, CObject co)
   {
      //println "createDvordinal: s="+ s
      //println "parts="+ parts[0] + ", " + parts[1]
      
      def ord = null
      
      if (!s)
      {
         ord = new DvOrdinal(symbol: null, value: null)
      }
      else
      {
         String[] parts = s.split(/\|\|/) // code||value
         
         /*
         Set<Ordinal> setOrdinal = cdvo.getList()
         //Ordinal ordin = setOrdinal.find{it.getSymbol().codeString.endsWith(s)}
         Ordinal ordin = setOrdinal.find{it.getSymbol().codeString == s} // la condicion es que sea el mismo codigo.
         */
         
         Ordinal ordin = cdvo.getList().find{it.getSymbol().codeString == parts[0]} // la condicion es que sea el mismo codigo.
         TerminologyID ti = TerminologyID.create(ordin.getSymbol().terminologyId.name, null)
         CodePhrase cp = new CodePhrase(codeString: parts[0], terminologyId: ti)
         
         String value = CtrlTerminologia.getInstance().getTermino(cp.terminologyId, cp.codeString, arquetipo, this.session.locale)
        
         ord = new DvOrdinal(
            symbol:  new DvCodedText(value: value, definingCode: cp),
            value: ordin.value
         )
      }
      
      ord.validate()
      
      return ord
   }

   void imprimirObjetoXML(Object o)
   {
      println "-----------------................"
      XStream xstream = new XStream();
      String xml = xstream.toXML(o);
      println xml
      println "-----------------................."
   }

   //-----------------------------------
   // Funciones auxiliares
   //-----------------------------------

   /*
    * @author Leandro Carrasco
    *
    * FIXME: o usamos o metodos estaticos o el singleton para no tener 2 criterios distintos para hacer la misma cosa.
    */
   static String crearFechaISO8601(String anio, String mes, String dia, String hora, String min, String seg)
   {
      // FIXME: if (!mes) en lugar de if ((mes != null) && (mes != ""))
      if ((mes != null) && (mes != "") && (mes.length() == 1) && (Integer.parseInt(mes) < 10)) mes = "0" + mes
      if ((dia != null) && (dia != "") && (dia.length() == 1) && (Integer.parseInt(dia) < 10)) dia = "0" + dia
      if ((hora != null) && (hora != "") && (hora.length() == 1) && (Integer.parseInt(hora) < 10)) hora = "0" + hora
      if ((min != null) && (min != "") && (min.length() == 1) && (Integer.parseInt(min) < 10)) min = "0" + min
      if ((seg != null) && (seg != "") && (seg.length() == 1) && (Integer.parseInt(seg) < 10)) seg = "0" + seg

      if ((dia == null) || (dia == ""))
         dia = "00"
      if ((mes == null) || (mes  == ""))
         mes = "00"
      if ((min == null) || (min == ""))
         min = "00"
      if ((seg == null) || (seg == ""))
         seg = "00"

      if ((hora == null) || (hora == "")) // Date
         return anio +"-"+ mes +"-"+ dia
      else if ((anio == null) || (anio == "")) // Time
         return hora + ":" + min + ":" + seg
      else // DateTime
         return anio + "-" + mes + "-" + dia + " " + hora + ":" + min + ":" + seg // FIXME Este no es el formato ISO8601, corregirlo aqui y en el resto de la aplicación
   }
}

