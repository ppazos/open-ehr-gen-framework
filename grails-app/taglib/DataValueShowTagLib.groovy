/**
 * Esta taglib es para suplantar todos los templates que generan el show de los tipos basicos
 * que heredan de DataValue.
 * 
 * @author Pablo Pazos Gutierrez <pablo.swp@gmail.com>
 */

import org.openehr.am.archetype.constraintmodel.CObject
import org.openehr.am.openehrprofile.datatypes.text.CCodePhrase
import binding.CtrlTerminologia // Para CodedText

class DataValueShowTagLib {

   def showDvBoolean = { attrs, body ->

      if (attrs.dataValue.value)
      {
         out << g.message(code: "label.boolean.true")
      }
      else
      {
         out << g.message(code: "label.boolean.false")
      }
   }
   
   def showDvCodedText = { attrs, body ->
      
      out << attrs.dataValue.value
   }
   
   def showDvCount = { attrs, body ->
      
      out << attrs.dataValue.magnitude
   }
   
   def showDvDate = { attrs, body ->
      
      out << attrs.dataValue.value
   }
   
   def showDvDateTime = { attrs, body ->
      
      // Igual a DvDate pero con presicion hasta segundos en el datePicker del edit
      out << attrs.dataValue.value
   }
   
   def showDvMultimedia = { attrs, body ->
      
      // TODO: verificar que tipo de multimedia es, y mostrarlo de forma acorde
      //       podria no ser una imagen.
      //
      out << '<img src="'+ request.contextPath +'/records/fetch_mm/'+ dataValue.id +'" />'
      out << '<br/><br/>'
      out << g.link(controller:"records", action:"fetch_mm", id:dataValue.id, target:"_blank", class:"right") { g.message(code:"image.action.fullSize") }
   }
   
   def showDvOrdinal = { attrs, body ->
      
      String pathFromOwner = attrs.pathFromOwner
      String archetypeId = attrs.archetype.archetypeId.value
      String refPath = attrs.refPath
      
      // parent.rmTypeName es ELEMENT
      CObject aomNode = attrs.archetype.node(pathFromOwner)
      
      // No me interesa mostrar el indice seleccionado, sino el codigo para ese indice
      attrs.dataValue = attrs.dataValue.symbol
      out << g.showDvCodedText(attrs)
      /*
      <g:render template="../guiGen/showTemplates/DvCodedText"
      model="[dataValue: dataValue.symbol, archetype: archetype]" />
      */
   }
   
   def showDvQuantity = { attrs, body ->
      
      String pathFromOwner = attrs.pathFromOwner
      String archetypeId = attrs.archetype.archetypeId.value
      
      // parent.rmTypeName es ELEMENT
      CObject aomNode = attrs.archetype.node(pathFromOwner)
      
      out << attrs.dataValue.magnitude
      
      // FIXME: para sobreescribir no es necesario que haya una sola unidad, hay que corregir que se
      //        pueda sobreescribir cualquier cantidad de unidades, una o muchas.
      if (aomNode.list.units.size()==1)
      {
         // Constraint del template, para ver si tiene una transformacion
         Object constraint = attrs.template.getField( archetypeId, aomNode.path() )?.getConstraintByPath(aomNode.path()+'/units')
         if (constraint)
         {
            out << constraint.process( aomNode.list.units[0] ) // (sobre-escrita)
         }
         else
         {
            out << attrs.dataValue.units
         }
      }
      else
      {
         out << attrs.dataValue.units
      }
   }
   
   def showDvText = { attrs, body ->
      
      out << attrs.dataValue.value
   }
   
   def showDvTime = { attrs, body ->
      
      out << attrs.dataValue.value
   }
   
   // TODO: demas tipos soportados
}