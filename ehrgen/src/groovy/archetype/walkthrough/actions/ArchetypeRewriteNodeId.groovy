package archetype.walkthrough.actions;

import org.openehr.am.archetype.constraintmodel.ConstraintRef
import org.openehr.am.openehrprofile.datatypes.quantity.CDvOrdinal
import org.openehr.am.openehrprofile.datatypes.text.CCodePhrase

public class ArchetypeRewriteNodeId extends AbstractAction {

   static String node_id_sep = "_"
   
   @Override
   public void execute(Map params)
   {
      //println "ID Rewrite " + params.keySet()
      
      // Este no es el raiz del plano porque se procesa cada arquetipo
      // por separado, root es el arquetipo actual.
      //
      def root = params.walk.root
      def archetype = params.archetype
      
      
      //println "ArchetypeRewriteNodeId: root " + root.archetypeId.value
      
      
      // -------------------------------------------------------------
      // Genera nuevos codigos at para el arquetipo plano
      def codes = archetype.walkthrough.codes.ArchetypeCodes.instance
      
      /* Seteo el flatArchetypeId por afuera
      if (!codes.hasFlatArchetypeId())
      {
         codes.setFlatArchetypeId(root.archetypeId.value)
      }
      // -------------------------------------------------------------
      */
      
      
      // Procesa solo si el nodo actual no es del arquetipo raiz
      // Esto se hace afuera
      //if (root == archetype) return
      
      def node = params.node
      
      //println "setNodeId: "+ archetype.archetypeId.value + "::" + node.getNodeId()
      //println "setNodeId: "+ archetype.archetypeId.value + "::" + node.nodeID
      //println node.class
      
      // En algunos arquetipos de ACTION, faltan los nodeID de ISM_TRANSITION y son CComplexObject
      // Esta condicion debe satisfacerse si el nodo tiene hermanos, sino tiene hermanos, puede tener nodeID vacio.
      //if (!node.nodeID) throw new Exception("Required nodeID missing on node " + archetype.archetypeId.value + "::" + node.path())
      
      /*
      //if (node.path == "/")
      if (node.isRoot())
      {
         node.setNodeId(archetype.archetypeId.value)
      }
      else if (node.nodeID)
      {
         node.setNodeId( this.newCode(archetype.archetypeId.value, node.getNodeId()) ) //archetype.archetypeId.value + node_id_sep + node.getNodeId())
      }
      */
      if (node.nodeID)
      {
         print "0) Sobreescribe nodeID para: " + archetype.archetypeId.value +" "+ node.getNodeId()
         node.setNodeId( codes.transformCode(archetype.archetypeId.value, node.getNodeId()) )
         println " a " + node.getNodeId()
      }
      else
      {
         // el nodo no tiene nodeID
         
         // Verifico por codigos de restricciones CDomainType
         if (node instanceof CCodePhrase && node.codeList && node.codeList.size() > 0)
         {
            //println "CCodePhrase *** " + node.codeList
            
            // Cambia solo si la terminologia es "local"
            //println node.terminologyId //  + " " + node.terminologyId.class // TerminologyID
            if (node.terminologyId.name == "local")
            {
               //println node
               
               def codesToChange = node.codeList
               node.codeList = []
               codesToChange.each { code ->
                  
                  //println code.class // String
                  //node.codeList << archetype.archetypeId.value + node_id_sep + code
                  //node.codeList << this.newCode(archetype.archetypeId.value, code)
                  node.codeList << codes.transformCode(archetype.archetypeId.value, code)
               }
            }
            else
            {
               println "1) No sobreescribe para terminologia " + node.terminologyId.name
            }
         }
         else if (node instanceof CDvOrdinal)
         {
            // TODO:
            //println "CDvOrdinal: " + node
            throw new Exception("TODO: rewrite codes on CDvOrdinal")
         }
         else if (node instanceof ConstraintRef) // ConstraintRef.reference es un codigo acNNNN
         {
            //println "ref: " + node
            //node.reference = archetype.archetypeId.value + node_id_sep + node.reference
            //node.reference = this.newCode(archetype.archetypeId.value, node.reference)
            node.reference = codes.transformCode(archetype.archetypeId.value, node.reference)
         }
         else
         {
            //println "2) No sobreescribe nodeID para " + node
         }
      }
      
      //println node
      //println node.nodeID
   }
   
   /*
   private String newCode(String archetypeId, String oldCode)
   {
      return archetypeId + node_id_sep + oldCode
   }
   */
}