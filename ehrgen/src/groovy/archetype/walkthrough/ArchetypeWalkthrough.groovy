package archetype.walkthrough;

import archetype.walkthrough.actions.AbstractAction;

import org.openehr.am.archetype.Archetype
import org.openehr.am.archetype.constraintmodel.ArchetypeInternalRef
import org.openehr.am.archetype.constraintmodel.ArchetypeSlot;
import org.openehr.am.archetype.constraintmodel.CAttribute;
import org.openehr.am.archetype.constraintmodel.CComplexObject;
import org.openehr.am.archetype.constraintmodel.CDomainType;
import org.openehr.am.archetype.constraintmodel.CPrimitiveObject;
import org.openehr.am.archetype.constraintmodel.ConstraintRef;
import org.openehr.am.archetype.constraintmodel.primitive.CPrimitive
import org.openehr.am.archetype.constraintmodel.primitive.CString
import org.openehr.am.archetype.ontology.ArchetypeOntology
import org.openehr.am.archetype.ontology.ArchetypeTerm
import org.openehr.am.archetype.ontology.OntologyBinding
import org.openehr.am.archetype.ontology.OntologyBindingItem
import org.openehr.am.archetype.ontology.OntologyDefinitions
import org.openehr.am.archetype.ontology.TermBindingItem
import org.openehr.am.openehrprofile.datatypes.quantity.CDvOrdinal

public class ArchetypeWalkthrough {

   /**
    * Eventos que se disparan cuando se encuentran distintos tipos de nodos.
    * TODO: Deberian ser un enum.
    */
   static String EVENT_COBJECT = "co"
   static String EVENT_CCOMPLEXOBJECT = "cco"
   static String EVENT_CDOMAIN = "cdo"
   static String EVENT_CPRIMITIVE = "cpr"
   static String EVENT_SLOT = "sl"
   static String EVENT_CATTRIBUTE = "cat"
   static String EVENT_CREF = "ref"
   static String EVENT_BEFORE = "bef" // Antes de comenzar la recorrida ejecuta este
   
   
   static String EVENT_ONT_ARCHETYPE_TERM = "ont_at"
   static String EVENT_ONT_BINDING_ITEM = "ont_bit"
   
   
   // Map event->lista de acciones que se ejecutan cuando se encuentra cada nodo
   Map observers = [:]
   
   
   /**
    * Registra una accion a un evento.
    */
   public void observe(String event, AbstractAction action)
   {
      if (!observers[event]) observers[event] = []
      observers[event] << action
   }
   
   
   /**
    * Arquetipo por el que se empieza la recorrida.
    */
   Archetype root
   
   /**
    * Objeto donde que las acciones van modificando y que se devuelve al terminar la recorrida.
    */
   WalkthroughResult result
   
   
   public void walthroughInit(Archetype root)
   {
      this.root = root
   }
   
   /**
    * actions contiene las acciones a registrar a cada evento
    * result es lo que hay que devolver
    */
   public void walthroughStart(Map actions, WalkthroughResult result)
   {
      this.observers = actions // ya viene con la misma estructura
      this.result = result
      
      this.result.loadedArchetypes[this.root.archetypeId.value] = this.root
      
      
      // No se le pasa arquetipo o nodo porque no tiene sentido para este evento
      this.observers[EVENT_BEFORE].each { _actions ->
         _actions.each { action ->
            action.execute([result:this.result, walk:this]) // result es in/out
         }
      }
      
      // walkthrough definition
      wt(this.root.definition, null, this.root)
      
      // walkthrough ontology
      // TODO: puede ser otro metodo
      wt(this.root.ontology, null, this.root)
   }
   
   public WalkthroughResult walthroughResult()
   {
      return this.result
   }
   
   
   // Recorrida por ontology
   
   // Comienza recorrida
   private void wt(ArchetypeOntology ontology, Object parent, Archetype archetype)
   {
      wtdefs(ontology.termDefinitionsList, archetype)
      wtdefs(ontology.constraintDefinitionsList, archetype)
      wtbinds(ontology.termBindingList, archetype)
      wtbinds(ontology.constraintBindingList, archetype)
   }
   
   // term definitions
   // constraint definitions
   private void wtdefs(List<OntologyDefinitions> definitions, Archetype archetype)
   {
      definitions.each{ // OntologyDefinitions
         wto(it, archetype)
      }
   }
   
   // term binding
   // constraint binding
   private void wtbinds(List<OntologyBinding> bindings, Archetype archetype)
   {
      bindings.each{ // OntologyBinding
         wto(it, archetype)
      }
   }
   
   private void wto(OntologyDefinitions defs, Archetype archetype)
   {
      //defs.language
      defs.definitions.each { // ArchetypeTerm
         wto(it, archetype)
      }
   }
   private void wto(OntologyBinding binding, Archetype archetype)
   {
      //binding.terminology
      binding.bindingList.each { // OntologyBindingItem
         wto(it, archetype)
      }
   }
   
   private void wto(ArchetypeTerm term, Archetype archetype)
   {
      //term.code         << el codigo que hay que cambiar en el rewrite de codigos
      //term.description
      //term.text
      this.observers[EVENT_ONT_ARCHETYPE_TERM].each { actions ->
         actions.each { action ->
            action.execute([archetype:archetype, node:term, result:this.result, walk:this]) // result es in/out
         }
      }
   }
   
   // Esta no creo que se llame, seguro se llama a la de su hija TermBindingItem
   private void wto(OntologyBindingItem item, Archetype archetype)
   {
      //item.code
      
      this.observers[EVENT_ONT_BINDING_ITEM].each { actions ->
         actions.each { action ->
            action.execute([archetype:archetype, node:item, result:this.result, walk:this]) // result es in/out
         }
      }
   }
   
   // Subclase de OntologyBindingItem
   private void wto(TermBindingItem item, Archetype archetype)
   {
      //item.code  // String
      //item.terms // List<String>
      
      // esta es subclase, por eso el evento es el mismo
      this.observers[EVENT_ONT_BINDING_ITEM].each { actions ->
         actions.each { action ->
            action.execute([archetype:archetype, node:item, result:this.result, walk:this]) // result es in/out
         }
      }
   }
   
   // /Recorrida por ontology
   // =================================================================================
   
   // =================================================================================
   // Inicio de la recorrida definition
   private void wt(CComplexObject c, Object parent, Archetype archetype)
   {
      //if (c.rmTypeName == "DV_CODED_TEXT")
      //   println "DV_CODED_TEXT " + c
//      if (c.rmTypeName == "ISM_TRANSITION")
//      {
//         println c
//         println ""
//      }
      
      this.observers[EVENT_CCOMPLEXOBJECT].each { actions ->
         actions.each { action ->
            action.execute([archetype:archetype, node:c, result:this.result, walk:this]) // result es in/out
         }
      }
      
      //println "CComplexObject"
      // List<CAttributes>
      c.attributes.each{ attr -> wt(attr, c, archetype) }
   }
   // Para CMultipleAttribute y CSingleAttribute
   private void wt(CAttribute c, Object parent, Archetype archetype)
   {
      //println "CAttribute " + c.getClass().getSimpleName()
      
      this.observers[EVENT_CATTRIBUTE].each { actions ->
         actions.each { action ->
            action.execute([archetype:archetype, node:c, result:this.result, walk:this]) // result es in/out
         }
      }
      
      // List<CObject>
      //c.children.each { co -> findSlots(co, c) } // sin contiene slots, estoy modificando el c.children por el que estoy iterando y tira una except
      
      // Coleccion aparte para poder iterar y no modificar la coleccion por la que itero
      def loopPorAfuera = []
      c.children.each { co -> loopPorAfuera << co }
      loopPorAfuera.each { co ->
         wt(co, c, archetype)
      }
   }
   
   // Muestra el slot encontrado y carga arquetipos referenciados
   private void wt(ArchetypeSlot c, Object parent, Archetype archetype)
   {
      // nodeId dice que es null para slot
      //println "Slot>> " + c
      //println "Slot>> " + c.rmTypeName  // + ' ' + c.nodeId // + ' ' + c.includes
      
      this.observers[EVENT_SLOT].each { actions ->
         actions.each { action ->
            action.execute([archetype:archetype, node:c, result:this.result, walk:this]) // result es in/out
         }
      }
   }
   
   private void wt(ArchetypeInternalRef c, Object parent, Archetype archetype)
   {
      // nodeId dice que es null para slot
      //println "Slot>> " + c
      
      //println "InternalRef>> " + c.rmTypeName + " " + parent //archetype.archetypeId.value // + ' ' + c.nodeId // + ' ' + c.includes
      
      // FIXME: la path del InternalRef se debe sobreescribir con la path en el arquetipo plano y con los codeIds reescritos.
      // data matches {
      //   use_node ITEM_TREE /data[at0001]/events[at0006]/data[at0003]   -- /data[history]/events[any event]/data[blood pressure]

      
      /*
      this.observers[EVENT_SLOT].each { actions ->
         actions.each { action ->
            action.execute([archetype:archetype, node:c, result:this.result, walk:this]) // result es in/out
         }
      }
      */
   }
   
   // No hacen nada porque no tiene hijos que puedan ser slots, solo el CComplexObject puede tener hijos slots
   private void wt(CPrimitiveObject c, Object parent, Archetype archetype)
   {
      //println "CPrimitiveObject"
      this.observers[EVENT_CPRIMITIVE].each { actions ->
         actions.each { action ->
            action.execute([archetype:archetype, node:c, result:this.result, walk:this]) // result es in/out
         }
      }
      
      wt(c.item, c, archetype)
   }
   
   private void wt(CPrimitive c, Object parent, Archetype archetype)
   {
//      if (c instanceof CString)
//         println "wt primitive " + c.list
      // TODO
   }
   
   private void wt(CDomainType c, Object parent, Archetype archetype) // CCodePhrase, ...
   {
      //println "CDomainType " + c.getClass().getSimpleName()
      // CDvState, CCodePhrase, CDvOrdinal, CDvQuantity
      
      //println c
      //if (c instanceof CDvOrdinal) println c
      
      this.observers[EVENT_CDOMAIN].each { actions ->
         actions.each { action ->
            action.execute([archetype:archetype, node:c, result:this.result, walk:this]) // result es in/out
         }
      }
   }
   private void wt(ConstraintRef c, Object parent, Archetype archetype) // CCodePhrase, ...
   {
      //println "CDomainType " + c.getClass().getSimpleName()
      
      this.observers[EVENT_CREF].each { actions ->
         actions.each { action ->
            action.execute([archetype:archetype, node:c, result:this.result, walk:this]) // result es in/out
         }
      }
   }
}