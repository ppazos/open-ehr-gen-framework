package archetype.walkthrough.actions;

import java.util.Map
import archetype.ArchetypeManager

public class SlotFlattener extends AbstractAction {

   @Override
   public void execute(Map params)
   {
      // result tiene
      //  arquetipos cargados en loadedArchetypes
      //  referencias archid::path -> lista de ref archids
      
      //def slot = params.node
      
      def result = params.result
      
      
      // Copia de la raiz que se esta aplanando
      // para usarlo como arquetipo aplanado.
      //def root = params.walk.root.copy()
      def archetypes = result.loadedArchetypes // Todos los arquetipos cargados
      
      
      // TODO: para todos los arquetipos que no son root, cambiar los nodeId por archId.nodeId
      // TODO: tambien cambiar los ids en la ontologia
      // TODO: tambien cambiar los ids en los constraint refs (DvCodedText, DvOrdinal, y otros que usen restricciones terminologicas acNNNN)
      
      
      // Se resuelven los slots a las definiciones de los arquetipos referenciados
      def archIdPath
      def archId
      def path
      def parentPath
      def archetype
      def parent_cobj
      def slot
      result.references.each { archIdRefs ->
         
         archIdPath = archIdRefs.key.split("::") // archId::path (slot path)
         archId = archIdPath[0]
         
         // path los slots (la path puede hacer referencia a mas de un slot)
         // TODO: esto es un bug en java ref impl, los nodos deberian diferenciarse por node ID
         //       tambien puede ser un bug en el ADL.
         //       el bug en la ref impl es no chequearlo
         path = archIdPath[1]
         
         
         parentPath = this.parentPath(path) // path al atributo que contiene el slot
        
         
         // Arquetipo que contiene el slot
         archetype = archetypes[archId]
         slot = archetype.node(path)
         
         // En algunos arquetipos se definen slots sin nodeID, eso viola una regla
         // de validacion que el parser deja pasar.
         if (!slot.nodeID) throw new Exception("The slot should have a nodeID, fix it in the ADL: " + archetype.archetypeId.value)
         
         
         // -----------------------------------------------------------
         // FIXME: el nodeID del slot debe quitarse de la ontologia
         //        para todos los idiomas en los que aparezca.
         if (!result.cache['slotNodeIds'])
         {
            result.cache['slotNodeIds'] = []
         }
         result.cache['slotNodeIds'] << slot.nodeID
         // -----------------------------------------------------------
         
         
         // Quita el slot del CAttribute.children
         
         /*
          * Hay un bug en la java ref impl:
          * un slot tiene la misma path que su parent CAttribute, por ejemplo:
          *  / es CComplexObject
          *  /content es un CAttribute
          *  /content es un Slot dentro del CAttribute
          *  
          *  y cuando se hace archetype.node(/content)
          *  devuelve el slot, no el attribute.
          *  
          *  A la path del slot le falta el nodeId.
          *  
          *  Entonces para pedi el attribute parent del slot,
          *  tengo que ir al CObject padre del attribute, ver que
          *  coincida la path, y a ese le saco el slot y le pongo
          *  el CComplexObject de la definicion del arquetipo
          *  apuntado por el slot.
          * 
         parent = archetype.node(parentPath) // Cuidado: este no es attribute es CObject
         
         println "Slot Flattener: "
         println "Slot: $path " + slot.class
         println "Parent: $parentPath " + parent.class
         parent.attributes.each { attr ->
            println " - " + attr.path()
            attr.children.each { cob ->
               println "  * " + cob.path()
            }
         }
         
         parent.children.remove(slot)
         */
         
         // Cuidado: este no es attribute es CObject, para obtener el attribute
         // hay que sacar el ultimo nodeID de la path al slot
         // "openEHR-EHR-ACTION.medication.v1::/description[at0017]"
         //
         // Parent es CObject
         parent_cobj = archetype.node(parentPath)
         
         // Quiero el attr hijo de parent_cobj talque su path sea padre de la path del slot
         // o sea que el attr es el padre directo del slot.
         def parent_attr = parent_cobj.attributes.find{ path.startsWith(it.path()) }
         

         // Quita el slot del attr padre
         parent_attr.children.remove(slot)


         // Agrega referencias directas al attr padre
         // Para todas las referencias desde ese slot
         // Las referencias son las que matchearon con la regex del slot
         archIdRefs.value.each { refArchId ->
            
            // Se agrega la definicion del arquetipo referenciado a donde estaba el slot
            parent_attr.children.add(archetypes[refArchId].definition)
         }
      }
   }
   
   private String parentPath (String path)
   {
      /*
      def ss = [
         "openEHR-EHR-INSTRUCTION.medication.v1::/activities[at0001]/description",
         "openEHR-EHR-INSTRUCTION.medication.v1::/activities[at0001]",
         "openEHR-EHR-INSTRUCTION.medication.v1::/activities[at0001]/description[at0002]/value/magnitude",
       ]
       
       ss.each { s ->
         def f = s.reverse() - ~/^(.*?\/)/
         println f.reverse()
       }
       
//       openEHR-EHR-INSTRUCTION.medication.v1::/activities[at0001]
//       openEHR-EHR-INSTRUCTION.medication.v1::
//       openEHR-EHR-INSTRUCTION.medication.v1::/activities[at0001]/description[at0002]/value

      */
      
      def f = path.reverse() - ~/^(.*?\/)/
      
      // Si queda vacia el parent es el root
      if (!f) return f = "/"
      
      return f.reverse()
   }
}