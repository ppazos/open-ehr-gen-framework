package hce.core.composition.content.entry

import hce.core.datastructure.itemstructure.ItemStructure
import data_types.encapsulated.DvParsable
import hce.core.common.archetyped.Locatable
import hce.core.common.archetyped.Pathable

// TEST: para serializar DataValue a string y ahorrar joins
import com.thoughtworks.xstream.XStream
import data_types.basic.DataValue

class Activity extends Locatable {

    ItemStructure description
    DvParsable timing
    String action_archetype_id
    
    String codedTiming
    static transients = ['timing']

    
    // Nuevo para calcular codedValue
    def beforeInsert() {
       // Para generar XML en una sola linea sin pretty print: http://stackoverflow.com/questions/894625/how-to-disable-pretty-printingwhite-space-newline-in-xstream
       // Interesante: http://www.xml.com/pub/a/2001/06/20/databases.html
       XStream xstream = new XStream()
       xstream.omitField(DataValue.class, "errors");
       codedTiming = xstream.toXML(timing)
       codedName = xstream.toXML(name) // atributo de locatable
    }
    def beforeUpdate() {
       XStream xstream = new XStream()
       xstream.omitField(DataValue.class, "errors");
       codedTiming = xstream.toXML(timing)
       codedName = xstream.toXML(name) // atributo de locatable
    }
    // Al reves
    def afterLoad() {
       XStream xstream = new XStream()
       if (codedTiming) timing = xstream.fromXML(codedTiming)
       if (codedName) name = xstream.fromXML(codedName)
    }
    
    static mapping = {
       description cascade: "save-update"
       description column: "activity_description_id"
       //timing cascade: "save-update"
    }

    static constraints = {
       description (nullable: false)
       //timing (nullable: false)
       action_archetype_id (nullable: false)
    }
    
   /*
   // rmParentId definido en Pathable
   // Si no se pone tira except property [rmParent] not found on entity
   static transients = ['padre']
   Pathable getPadre()
   {
      if (!this.rmParentId) return null
      return Pathable.get(this.rmParentId)
   }
   */
   /*
   void setRmParent(Pathable parent)
   {
      if (!parent) throw new Exception("parent no puede ser nulo")
      if (!parent.id) throw new Exception("parent debe tener id (debe guardarse previamente en la base)")
      this.rmParentId = parent.id
   }
   */
}