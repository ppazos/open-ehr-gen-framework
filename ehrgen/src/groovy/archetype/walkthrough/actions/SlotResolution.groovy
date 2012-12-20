package archetype.walkthrough.actions

import archetype.ArchetypeManager

public class SlotResolution extends AbstractAction {

   @Override
   public void execute(Map params)
   {
      //println "slot resolution: " + params
      
      // in: archetype, archetypeSlot
      // agrega al resultado los arquetipos referenciados
      // le deberia decir a walkthrough que haga la recorrida
      // en esos nuevos arquetipos tambien (eso puede depender
      // de una bandera "recursive" al inicio de la recorrida)
      
      def slot = params.node
      def result = params.result
      
      slot.includes.each{ assertion ->
         
         //println assertion.expression // ExpressionBinaryOperator
         //println assertion.expression.rightOperand // ExpressionLeaf
         //println 'item: ' + assertion.expression.rightOperand.item // CString
         //println 'pattern: ' + assertion.expression.rightOperand.item.pattern // ExpressionLeaf
         

         // Carga arquetipos referenciados
         def pattern = assertion.expression.rightOperand.item.pattern
         
         // Para evitar loops:
         // Si pattern es .*, solo cargar si el arquetipo no esta ya cargado
         // Lo voy a verificar siempre, independientemente de la patterns

         // Carga todos los que matchean con pattern
         def loader = ArchetypeManager.getInstance()
         
         //println "SlotResolution: " + slot.rmTypeName + " " + pattern
         
         def archetypes = loader.getArchetypes(slot.rmTypeName, pattern)
         archetypes.each { ref_archetype ->
            
            //println "WaLk ($pattern)" + ref_archetype.archetypeId.value
            
            
            // Verifica que ya no se haya cargado para evitar loops infinitos
            if (result.loadedArchetypes[ref_archetype.archetypeId.value]) return
            
            
            // ----------------------------------------------------------------------
            // Agrega al result los arquetipos cargados
            result.loadedArchetypes[ref_archetype.archetypeId.value] = ref_archetype
            
            
            //result.references[params.archetype.archetypeId.value +"::"+ slot.path()] = ref_archetype.archetypeId.value
            def completePath = params.archetype.archetypeId.value +"::"+ slot.path()
            if (!result.references[completePath]) result.references[completePath] = []
            result.references[completePath] << ref_archetype.archetypeId.value
            
            
            //println " - arquetipo referenciado : " + ref_archetype.archetypeId.value
            
            
            // - Mete las definiciones del arquetipo
            //c.getParent().children.add(archetype.definition)
            // TODOS los nodos del arquetipo deberian anteponer la c.path a su path asi quedan bien las paths en el arquetipo flatten
            // Esto no es necesario porque cuando escriba el ADL plano las paths se vuelven a recalcular cuando lo parseo.
            // Lo que tengo que poner es el id del arquetipo como dijo Thomas.
//            parent.children.add(ref_archetype.definition)
            
//            ref_archetype.definition.setNodeId( ref_archetype.archetypeId.value )


            // Recorrida recursiva para cargar las referencias de los arquetipos hijos
            params.walk.wt(ref_archetype.definition, null, ref_archetype)
         }
      }
   }
}