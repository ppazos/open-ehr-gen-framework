package archetype.walkthrough

class WalkthroughResult {

   // archetype id -> Archetype
   Map loadedArchetypes = [:]
   
   // parent archetype id :: path to slot -> List<ref archetype id>
   Map references = [:]
   /*
      <linked-hash-map>
        <entry>
          <string>openEHR-EHR-COMPOSITION.medication_list.v1::/content</string>
          <list>
            <string>openEHR-EHR-ACTION.medication.v1</string>
            <string>openEHR-EHR-INSTRUCTION.medication.v1</string>
          </list>
        </entry>
        <entry>
          <string>openEHR-EHR-ACTION.medication.v1::/description</string>
          <list>
            <string>openEHR-EHR-ITEM_TREE.medication-vaccine.v1</string>
            <string>openEHR-EHR-ITEM_TREE.medication.v1</string>
          </list>
        </entry>
        <entry>
          <string>openEHR-EHR-INSTRUCTION.medication.v1::/activities[at0001]/description</string>
          <list>
            <string>openEHR-EHR-ITEM_TREE.medication-formulation.v1</string>
            <string>openEHR-EHR-ITEM_TREE.medication-vaccine.v1</string>
            <string>openEHR-EHR-ITEM_TREE.medication.v1</string>
          </list>
        </entry>
      </linked-hash-map>
    */
   
   // Otros resultados por nombre, las acciones saben como procesarlos.
   Map cache = [:]
}