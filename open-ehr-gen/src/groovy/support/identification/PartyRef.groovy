package support.identification

import support.identification.ObjectRef

class PartyRef extends ObjectRef {

    public String toString()
    {
        return this.getClass().getSimpleName()+'-> '+this.namespace+' '+this.type+' '+this.objectId.toString()
    }
}