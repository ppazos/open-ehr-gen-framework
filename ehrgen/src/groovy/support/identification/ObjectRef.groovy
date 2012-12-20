package support.identification

import support.identification.ObjectID

class ObjectRef {

    // Identificador del servicio que gestiona instancias de clases del tipo 'type'
    String namespace // local, unknown, demographic, ...
    
    /* 
     * Name of the class (concrete or abstract) of object to which this
     * identifier type refers, e.g. “PARTY”, “PERSON”, “GUIDELINE”, “ANY”.
     */
    String type
    
    /*
     * Globally unique id of an object, regardless of where it is stored.
     */
    ObjectID objectId // no puedo ponerle id! por grails.
    
    
    public boolean validate()
    {
       if (!namespace) errors.addError('namespace', 'ObjectRef.namespace es vacio', namespace)
       if (!type) errors.addError('type', 'ObjectRef.type es vacio', type)
       if (!objectId) errors.addError('objectId', 'ObjectRef.objectId es vacio', objectId)
    }
    
    /*
    static constraints = {
        namespace(nullable:false, blank:false)
        type(nullable:false, blank:false)
    }
    
    static mapping = {
        objectId cascade: "save-update"
        //table 'object_ref'
    }
    */
    
    public String toString()
    {
        return this.getClass().getSimpleName()+'-> '+this.namespace+' '+this.type+' '+this.objectId.toString()
    }
}