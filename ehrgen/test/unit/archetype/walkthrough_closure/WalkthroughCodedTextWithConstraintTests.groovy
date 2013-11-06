package archetype.walkthrough_closure

import grails.test.mixin.*
import grails.test.mixin.support.*
import org.junit.*

import archetype.ArchetypeManager

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
class WalkthroughCodedTextWithConstraintTests {

   @Before
   void setUp() {
      // Setup logic here
   }

   @After
   void tearDown() {
      // Tear down logic here
   }

   @Test
   void findConstraintCodes()
   {
      def f = new File(".")
      println "Current folder: "+ f.getCanonicalPath()
      
      // Load archetype
      def PS = System.getProperty("file.separator")
      def loader = ArchetypeManager.getInstance('archetypes'+ PS +'ehr'+ PS)
      def archetype = loader.getArchetype("openEHR-EHR-OBSERVATION.test_servicios_terminologicos.v1")
      
      
      
      def walk = new ArchetypeWalkthrough()
      
      
      // Observer CComplexObject
      // parchetype es el arquetipo padre del node
      //
      /*
      walk.observeCCO { node, parchetype, walkt, parent -> // parent no deberia venir de la recorrida porque lo arregle en el parser... actualizar los jars

         println "CCO: "+ parchetype.archetypeId.value + node.path() +" "+ 
                          node.rmTypeName +" "+ node.nodeID
         
         if (node.rmTypeName == 'ELEMENT')
         {
            println node.attributes
         }
      }
      */
      
      /* Encuentro codigos de restriccion acNNNN en DvCodedText */
      walk.observeCR { node, parchetype, walkt, parent -> // parent no deberia venir de la recorrida porque lo arregle en el parser... actualizar los jars

         println "ConstraintRef: "+ parchetype.archetypeId.value + node.path() +" "+ 
                          node.rmTypeName +" "+ node.nodeID +" "+ node.reference
         // ConstraintRef: 
         //openEHR-EHR-OBSERVATION.test_servicios_terminologicos.v1/data[at0001]/events[at0002]/data[at0003]/items[at0004]/value/defining_code
         //CodePhrase 
         //null 
         //ac0001
         
         // Donde pongo el mapeo entre path del nodo y el codigo de restriccion
         if (!walkt.memory['path_ccode']) walkt.memory['path_ccode'] = [:]
         
         String fullPath = parchetype.archetypeId.value + node.path()
         walkt.memory['path_ccode'][fullPath] = node.reference
      }
      
      // FIXME: esto no distingue entre constraint_binding y term_binding.
      //        y aca solo quiero constraint_binding...
      walk.observeOntBind { bindings, parchetype, walkt, attr ->

         println "bindings class: "+ bindings.class //  List<OntologyBinding>
      
         // Verifica si hay codigos de restricciones en constraint_bindings
         // Si hay, quiero la URL que define el codigo que se usa para identificar
         // el servicio terminologico donde se an a buscar los terminos
         // println attr // term_bindings o constraint_bindings
         if (attr == 'constraint_bindings')
         {
            // Donde pongo los mapeos entre codigo de restriccion y query
            if (!walkt.memory['ccode_query']) walkt.memory['ccode_query'] = [:]
         
            bindings.each{ ontologyBinding ->
            
              //println ontologyBinding.class // org.openehr.am.archetype.ontology.OntologyBinding
              
              println ontologyBinding.terminology // ICD10_1998
              
              // List<OntologyBindingItem>
              ontologyBinding.bindingList.each { queryBindingItem ->
              
                 //println queryBindingItem
                 println queryBindingItem.code      // acNNNN codigo de restriccion vinculado a la consulta
                 //println queryBindingItem.query   // org.openehr.am.archetype.ontology.Query
                 println queryBindingItem.query.url // terminology:ICD10_1998?subset=XIX%20Trauma
                 
                 
                 walkt.memory['ccode_query'][queryBindingItem.code] = queryBindingItem.query.url
              }
            }
            
            /*
            OntBinding: [org.openehr.am.archetype.ontology.OntologyBinding@b56559[
              terminology=ICD10_1998
              bindingList=[org.openehr.am.archetype.ontology.QueryBindingItem@17b6178[
                query=org.openehr.am.archetype.ontology.Query@11b92e6[
                  url=terminology:ICD10_1998?subset=XIX%20Trauma
                ]
                code=ac0002
              ]]
            ]]
            */
         }
      }
      
      
      walk.init(archetype)
      walk.start()
      println walk.memory
      /*
      [path_ccode:
         [openEHR-EHR-OBSERVATION.test_servicios_terminologicos.v1/data[at0001]/events[at0002]/data[at0003]/items[at0004]/value/defining_code:
           ac0001,
          openEHR-EHR-OBSERVATION.test_servicios_terminologicos.v1/data[at0001]/events[at0002]/data[at0003]/items[at0005]/value/defining_code:
           ac0002
         ], 
         ccode_query:
          [ac0002:terminology:ICD10_1998?subset=XIX%20Trauma]
         ]
      */
   }
   
}
