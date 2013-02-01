package hce.core.composition

import hce.core.common.archetyped.Locatable
import data_types.text.DvCodedText
import data_types.quantity.date_time.DvDateTime
import hce.core.datastructure.itemstructure.ItemStructure
import hce.core.common.generic.Participation

// TEST: para serializar DataValue a string y ahorrar joins
import com.thoughtworks.xstream.XStream
import data_types.basic.DataValue

class EventContext extends Locatable {

   //DvDateTime startTime
   //DvDateTime endTime
   String location
   DvCodedText setting
   //PartyIdentified healthCareFacility;
   //String codedStartTime
   //String codedEndTime
   String codedSetting
   static transients = ['setting'] //['startTime', 'endTime', 'setting']
   
   
   // Nuevo para calcular codedValue
   def beforeInsert() {
      // Para generar XML en una sola linea sin pretty print: http://stackoverflow.com/questions/894625/how-to-disable-pretty-printingwhite-space-newline-in-xstream
      // Interesante: http://www.xml.com/pub/a/2001/06/20/databases.html
      XStream xstream = new XStream()
      xstream.omitField(DataValue.class, "errors");
      //codedStartTime = xstream.toXML(startTime)
      //codedEndTime = xstream.toXML(endTime)
      codedSetting = xstream.toXML(setting)
      codedName = xstream.toXML(name) // atributo de locatable
   }
   def beforeUpdate() {
      XStream xstream = new XStream()
      xstream.omitField(DataValue.class, "errors");
      //codedStartTime = xstream.toXML(startTime)
      //codedEndTime = xstream.toXML(endTime)
      codedSetting = xstream.toXML(setting)
      codedName = xstream.toXML(name) // atributo de locatable
   }
   // Al reves
   def afterLoad() {
      XStream xstream = new XStream()
      //if (codedStartTime) startTime = xstream.fromXML(codedStartTime)
      //if (codedEndTime) endTime = xstream.fromXML(codedEndTime)
      if (codedSetting) setting = xstream.fromXML(codedSetting)
      if (codedName) name = xstream.fromXML(codedName)
   }
   
   // TODO: participation, todavia no lo usamos para poner a los medicos! necesitamos el login antes!
   List participations // Para que guarden en orden
   static hasMany = [participations: Participation]
   
   ItemStructure otherContext

   static mapping = {
      //startTime cascade: "save-update"
      //endTime cascade: "save-update"
      //setting cascade: "save-update"
      otherContext cascade: "save-update"
      location column:'event_context_location'
   }

   static constraints = {
      //startTime (nullable: false)
      //setting (nullable: false)
      //codedStartTime(nullable: true, maxSize:4096) // nullable para que valide
      //codedEndTime(nullable: true, maxSize:4096) // nullable para que valide
      codedSetting(nullable: true, maxSize:4096) // nullable para que valide
   }
}