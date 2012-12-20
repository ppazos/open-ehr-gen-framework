package archetype.walkthrough.actions;

import org.openehr.am.archetype.Archetype
import org.openehr.am.archetype.constraintmodel.ConstraintRef
import org.openehr.am.archetype.ontology.ArchetypeTerm
import org.openehr.am.archetype.ontology.TermBindingItem
import org.openehr.am.openehrprofile.datatypes.quantity.CDvOrdinal
import org.openehr.am.openehrprofile.datatypes.text.CCodePhrase

public class RewriteOntologyCodes extends AbstractAction {

   static String node_id_sep = "_"
   
   
   // Para sobreescribir codigos
   def codes = archetype.walkthrough.codes.ArchetypeCodes.instance
   
   
   @Override
   public void execute(Map params)
   {
      //println "ID Rewrite " + params.keySet()
      
      //def root = params.walk.root
      def archetype = params.archetype
      def node = params.node
      
      // Solo si no es root (se chequea afuera)
      rewriteCodes(node, archetype)
      
      /*
      // ConstraintRef.reference es un codigo acNNNN
      if (node instanceof ConstraintRef)
      {
         //println "ref: " + node
         node.reference = archetype.archetypeId.value + "::" + node.reference
      }
      */
   }
   
   private void rewriteCodes(TermBindingItem item, Archetype archetype)
   {
      //println "rewrite item: " + item + " " + item.code
      /*
       * rewrite item: org.openehr.am.archetype.ontology.TermBindingItem@1c8b24d[
       *   terms=[[SNOMED-CT(2003)::163020007]]
       *   code=at0000
       * ]
       */
      //item.code = archetype.archetypeId.value + node_id_sep + item.code
      
      // FIXME: groovy.lang.ReadOnlyPropertyException: Cannot set readonly property: code for class: org.openehr.am.archetype.ontology.TermBindingItem
      //item.code = codes.getMappedCode(archetype.archetypeId.value, item.code)
   }
   
   private void rewriteCodes(ArchetypeTerm term, Archetype archetype)
   {
      //println "rewrite term: " + term.code // puede ser at o ac
      // rewrite term: ac0000 Any term that 'is_a' form of medication
      //println "rewrite term: " + term.code + " " + term.text
      //term.code = archetype.archetypeId.value + node_id_sep + term.code
      term.code = codes.getMappedCode(archetype.archetypeId.value, term.code)
   }
}