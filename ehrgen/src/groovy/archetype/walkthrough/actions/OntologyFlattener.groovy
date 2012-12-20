package archetype.walkthrough.actions;

import java.util.List;
import java.util.Map

import org.openehr.am.archetype.Archetype;
import org.openehr.am.archetype.ontology.ArchetypeTerm
import org.openehr.am.archetype.ontology.OntologyBinding
import org.openehr.am.archetype.ontology.OntologyDefinitions
import archetype.ArchetypeManager

public class OntologyFlattener extends AbstractAction {

   def params
   
   @Override
   public void execute(Map params)
   {
      // Viene result y walk
      this.params = params
      
      def result = params.result
      def root = params.walk.root
      def archetypes = result.loadedArchetypes // Todos los arquetipos cargados
      
      //println "onto flattener: " + params.walk.root.archetypeId.value

      
      archetypes.each { archId, archetype ->
         
         // Hago merge de todas las ontologias en el root, evito procesar el propio root
//         if (archetype != root)
//         {
            flatDefs(archetype.ontology.termDefinitionsList, root.ontology.termDefinitionsList)
            flatDefs(archetype.ontology.constraintDefinitionsList, root.ontology.constraintDefinitionsList)
            
            // bindings
            flatBindings(archetype.ontology.termBindingList, root.ontology.termBindingList)
            flatBindings(archetype.ontology.constraintBindingList, root.ontology.constraintBindingList)
//         }
      }
      
      // Si el arquetipo root tiene alguno de los codigos de slot en su ontologia, se sacan
      // (porque el arquetipo ya viene con su ontologia y el procesamiento previo solo agrega
      // terminos, no saca los que ya estan)
      //def termsToDelete = []
      //result.cache['slotNodeIds'].each { atcode ->
         
         // List<OntologyDefinitions(String language, List<ArchetypeTerm(String code, ...)> definitions)>
         root.ontology.termDefinitionsList.each { ontoDefs ->
            ontoDefs.definitions.removeAll{ result.cache['slotNodeIds'].contains(it.code) }
         }
      //}
   }
   
   private void flatDefs(List<OntologyDefinitions> definitions, List rootList) //Archetype root)
   {
      def result = this.params.result
      def codes = archetype.walkthrough.codes.ArchetypeCodes.instance
      

      definitions.each { defs -> // OntologyDefinitions
         
         // Veo si el idioma ya existe en la ontologia de root
         //def odefSameLang = root.ontology.termDefinitionsList.find { odef -> odef.language == defs.language }
         def odefSameLang = rootList.find { odef -> odef.language == defs.language }
         
         // List<ArchetypeTerm>
         //println "defs: " + odefSameLang.definitions
         
         
         // List<ArchetypeTerm> que va a ir en OntologyDefinitions.definitions
         def archetypeTermList = []
         
         defs.definitions.each{ archTerm ->
            
            // Los nodeIDs de los slots no se incluyen en la ontologia del arquetipo plano
            //println "keycode: " + codes.getKeyCode(params.walk.root.archetypeId.value, archTerm.code)
            if (!result.cache['slotNodeIds'].contains(archTerm.code))
            {
               // test
               //if (odefSameLang)
               //   println odefSameLang.definitions.find{ it.code == archTerm.code }
               
               // Si la ontologia NO tiene ya al ArchetypeTerm con ese idioma y codigo, lo agrega.
               if (odefSameLang && !odefSameLang.definitions.find{ it.code == archTerm.code })
               {
                  //println "Agrega el codigo: "+ archTerm.code
                  
                  /* ["at0016"] = <  -- code ya esta procesado y deberia tener el archId
                   *   text = <"Start time set">
                   *   description = <"The time to start this medication has been set">
                   */
                  archetypeTermList << new ArchetypeTerm(archTerm.code, archTerm.items.text, archTerm.items.description)
               }
               else
               {
                  //println "No agrega el codigo: "+ archTerm.code
               }
            }
            else
            {
               //println "No se incluye el nodeID del slot: " + archTerm.code
            }
         }
         
         
         // Hay que ver si OntologyDefinitions.language ya tiene ArchetypeDefinitions o no.
         if (odefSameLang)
         {
            //println "Ya esta definido el lang "+ defs.language
            //println "X: " + result.cache['slotNodeIds']
            
            odefSameLang.definitions.addAll( archetypeTermList )
         }
         else
         {
            rootList.add(
               new OntologyDefinitions(defs.language, defs.definitions) //archetypeTermList)
            )
         }
         
         
         
         /*
         // Hay que ver si OntologyDefinitions.language ya tiene ArchetypeDefinitions o no.
         if (odefSameLang)
         {
            //println "Ya esta definido el lang "+ defs.language
            //println "X: " + result.cache['slotNodeIds']
            
            defs.definitions.each{ archTerm ->
            
               //println "keycode: " + codes.getKeyCode(params.walk.root.archetypeId.value, archTerm.code)
               if (result.cache['slotNodeIds'].contains(archTerm.code))
               {
                  println "No incluir el nodeID del slot: " + archTerm.code
               }
               
               // List<ArchetypeTerm>
               odefSameLang.definitions.add(
                  
                  //
                  / ["at0016"] = <  -- code ya esta procesado y deberia tener el archId
                  //   text = <"Start time set">
                  //   description = <"The time to start this medication has been set">
                  //
                  new ArchetypeTerm(archTerm.code, archTerm.items.text, archTerm.items.description)
               )
            }
         }
         else
         {
            //println "No esta definido el lang "+ defs.language
         
            // TODO: poner refArchId en los codigos de los terminos ArchTerm.
            
            // La alternativa es crear un ArchetypeOntology nuevo y pasarle en el constructor la lista mergeada de termDefinitions.
            //root.ontology.getTermDefinitionsList().add(
            rootList.add(
               new OntologyDefinitions(defs.language, defs.definitions)
            )
            
            
            def xs = new com.thoughtworks.xstream.XStream()
            println "======================================="
            println xs.toXML(rootList)
            println "======================================="
         }
         */
      }
   }
   
   private void flatBindings(List<OntologyBinding> bindings, List rootBindings)
   {
      //println bindings
      
      /*
       * term_bindings = <
       *     ["LNC205"] = <
       *        items = <
       *           ["/data[at0002]/events[at0003]/data[at0001]/items[at0004]"] = <[LNC205::8310-5]>
       *        >
       *     >
       *  >
       */
      bindings.each { binding -> // OntologyBinding
         
         // TODO: probar si funciona
         rootBindings << binding
      }
   }
}