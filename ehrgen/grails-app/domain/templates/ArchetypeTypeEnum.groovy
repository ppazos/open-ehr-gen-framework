package templates

/**
 * @author Pablo Pazos Gutierrez (pablo.pazos@cabolabs.com)
 */
public enum ArchetypeTypeEnum {
    
    COMPOSITION('composition'),
    SECTION('section'),
    OBSERVATION('observation'),
    EVALUATION('evaluation'),
    INSTRUCTION('instruction'),
    ACTION('action'),
    ADMIN_ENTRY('admin_entry'),
    CLUSTER('cluster'),
    ELEMENT('element')

    String name
    
    static belongsTo = [ArchetypeReference]
    
    public ArchetypeTypeEnum(String name)
    {
       this.name = name
    }
    
    static ArchetypeTypeEnum fromValue( String name )
    {
        switch(name)
        {
            case "composition": return COMPOSITION
            case "section": return SECTION
            case "observation": return OBSERVATION
            case "evaluation": return EVALUATION
            case "instruction": return INSTRUCTION
            case "action": return ACTION
            case "admin_entry": return ADMIN_ENTRY
            case "cluster": return CLUSTER
            case "element": return ELEMENT
            default:
               throw new Exception("tipo no soportado: "+ name)
        }
    }
   
    static list() {
     [
      COMPOSITION,
      SECTION,
      OBSERVATION,
      EVALUATION,
      INSTRUCTION,
      ACTION,
      ADMIN_ENTRY,
      CLUSTER,
      ELEMENT
     ]
   }
}