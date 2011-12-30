/**
 * Esta taglib es para suplantar todos los templates que generan el show de los tipos basicos
 * que heredan de DataValue.
 * 
 * @author Pablo Pazos Gutierrez <pablo.swp@gmail.com>
 */
import org.openehr.am.archetype.constraintmodel.CObject
import org.openehr.am.openehrprofile.datatypes.text.CCodePhrase
import binding.CtrlTerminologia // Para CodedText

class DataValueEditTagLib {

   def editDvBoolean = { attrs, body ->

      String pathFromOwner = attrs.pathFromOwner
      String archetypeId = attrs.archetype.archetypeId.value
      String refPath = attrs.refPath
      
      CObject aomNode = attrs.archetype.node(pathFromOwner)
      Boolean selectedValue = attrs.dataValue.value
      
      // Verificacion de errores
      if ( attrs.dataValue.errors?.hasErrors() )
      {
         // Los errores los muestra el template de ELEMENT
        
         selectedValue = attrs.dataValue.errors.getFieldError('magnitude').rejectedValue
      }
      // FIXME: esto sale como un string...
      out << '<select name="'+ archetypeId +refPath+ aomNode.path() +'">'
      out <<  '<option value="" '+ ((selectedValue==null)?'selected="true"':'') +'></option>'
      out <<  '<option value="true" '+ ((selectedValue==true)?'selected="true"':'') +'>'
      out <<   g.message( code: "label.boolean.true" )
      out <<  '</option>'
      out <<  '<option value="false" '+ ((selectedValue==false)?'selected="true"':'') +'>'
      out <<   g.message( code: "label.boolean.false" )
      out <<  '</option>'
      out << '</select>'
   }
   
   def editDvCodedText = { attrs, body ->
      
      String pathFromOwner = attrs.pathFromOwner
      String archetypeId = attrs.archetype.archetypeId.value
      String refPath = attrs.refPath
      String selectedValue = attrs.dataValue?.definingCode?.codeString
      
      CObject aomNode = attrs.archetype.node(pathFromOwner)
      
      // Mismo codigo de _cCodePhrase
      List values = []
      List codes = []
      CCodePhrase cphrase = aomNode?.attributes[0].children.find{it instanceof CCodePhrase}
      if (cphrase.codeList?.size()==1 && cphrase.codeList[0].startsWith('ac'))
      {
         def ctrm = CtrlTerminologia.getInstance()
         values = ctrm.getNombreTerminos( cphrase.terminologyId.name )
         codes = ctrm.getCodigoTerminos( cphrase.terminologyId.name )
      }
      else
      {
         codes = cphrase.codeList
         codes.each{ code ->
            
            /*
            // FIXME: deberia escalar en el locale como ArchetypeTagLib.findTerm, o mismo usar esa funcion.
            def archetypeTerm = attrs.archetype.ontology.termDefinition(session.locale.language, code)
            if (archetypeTerm) values << archetypeTerm.items.text
            else
            {
               // TODO:
               // El termino con codigo [${code}] no esta definido en el arquetipo, posiblemente el
               // termino no esta definido para el lenguaje seleccionado.<br/>
            }
            */
            
            // Cuidado el locale tiene formato: es_AR
            // Pero el arquetipo tiene formato: es-ar
            
            // pido para todo el locale
            def archetypeTerm = attrs.archetype.ontology.termDefinition(session.locale.toString().toLowerCase().replaceAll("_", "-"), code)
            
            // pido para el idioma y pais
            if (!archetypeTerm) archetypeTerm = attrs.archetype.ontology.termDefinition(session.locale.language+'-'+session.locale.country.toLowerCase(), code)
            
            // pido para el idioma
            if (!archetypeTerm) archetypeTerm = attrs.archetype.ontology.termDefinition(session.locale.language, code)
            
            if (archetypeTerm) values << archetypeTerm.items.text
            else
            {
               // TODO:
               // El termino con codigo [${code}] no esta definido en el arquetipo, posiblemente el
               // termino no esta definido para el lenguaje seleccionado.<br/>
            }
         }
      }

      // Control de template
      def control = attrs.template.getField( archetypeId, aomNode.path() )?.getControlByPath(aomNode.path())
      
      if (control && control.type=='radioGroup')
      {
         int i = 0
         values.each { value ->
            
            out << '<label class="id_'+ value +'">' // <!-- necesita id por el CSS -->
            out << '<input type="radio" value="'+ codes[i] +'" name="'+ archetypeId +refPath+ cphrase.path() +'" />'
            out << ${value}
            out << '</label>'
            i++
         }
         out << '<label class="id_nr">' // <!-- necesita id por el CSS -->
         out << '<input type="radio" checked="true" value="" name="'+ archetypeId +refPath+ cphrase.path() +'" />'
         out << 'NR' // TODO: i18n
         out << '</label>'
      }
      else
      {
         out << g.select( from: values, keys: codes,
                          name: archetypeId + refPath + cphrase.path(),
                          noSelection: ['':''],
                          value: selectedValue)
      }
   }
   
   def editDvCount = { attrs, body ->
      
      String pathFromOwner = attrs.pathFromOwner
      String archetypeId = attrs.archetype.archetypeId.value
      String refPath = attrs.refPath
      
      CObject aomNode = attrs.archetype.node(pathFromOwner)
      Integer selectedValue = attrs.dataValue.magnitude
      
      // Verificacion de errores
      if ( attrs.dataValue.errors.hasErrors() )
      {
         // Los errores los muestra el template de ELEMENT
        
         selectedValue = attrs.dataValue.errors.getFieldError('magnitude').rejectedValue
      }
      
      // Es un interval de constraints para mostrar el rango de validez
      // interval puede ser null
      def cattr = aomNode.attributes?.find { it.rmAttributeName=='magnitude' }
      def interval = cattr?.children[0].item.interval
      
      // Muestra el rango viendo si tiene lower y upper
      out << ((interval?.lower)? interval.lower : '*')
      out << '..'
      out << ((interval?.upper)? interval.upper : '*')
      
      out << '<input type="text" name="'+ archetypeId +refPath+ aomNode.path() +'" value="'+ selectedValue +'" />'
   }
   
   def editDvDate = { attrs, body ->
      
      /* Lo hace el ELEMENT
      // Verificacion de errores
      if ( attrs.dataValue.errors.hasErrors() )
      {
        out << '<div class="error">'
        out << g.renderErrors( bean: attrs.dataValue, as: "list")
        out << '</div>'
        
        // Me fijo si tiene error para saber que valor mostrar, siempre sera el valor ingresado
        // El valor viene de un select, asi que no va a tener errores
        //<g:set var="selectedValue" value="${dataValue.errors.getFieldError('value').rejectedValue}" />
      }
      */
      
      out << g.datePicker(name:archetypeId+refPath+aomNode.path(), value:attrs.dataValue.toDate(), precision: "day")
   }
   
   def editDvDateTime = { attrs, body ->
      
      // Igual a DvDate pero con presicion hasta segundos en el datePicker del edit

      String pathFromOwner = attrs.pathFromOwner
      String archetypeId = attrs.archetype.archetypeId.value
      String refPath = attrs.refPath
      
      CObject aomNode = attrs.archetype.node(pathFromOwner)
      
      /* Lo hace el ELEMENT
      // Verificacion de errores
      if ( attrs.dataValue.errors.hasErrors() )
      {
        // Los errores los muestra el template de ELEMENT
        
        // Me fijo si tiene error para saber que valor mostrar, siempre sera el valor ingresado
        // El valor viene de un select, asi que no va a tener errores
        //<g:set var="selectedValue" value="${dataValue.errors.getFieldError('value').rejectedValue}" />
      }
      */

      out << g.datePicker(name: archetypeId +refPath+ aomNode.path(),
                          value: attrs.dataValue.toDate(),
                          precision: "minute")
   }
   
   def editDvMultimedia = { attrs, body ->
      
      // TODO:
      
   }
   
   def editDvOrdinal = { attrs, body ->
      
      String pathFromOwner = attrs.pathFromOwner
      String archetypeId = attrs.archetype.archetypeId.value
      String refPath = attrs.refPath
      
      // parent.rmTypeName es ELEMENT
      CObject aomNode = attrs.archetype.node(pathFromOwner)
      
      String selectedValue = attrs.dataValue?.symbol?.definingCode?.codeString
      
      List labels = []
      List sortedAOMValues = aomNode.list.sort{ it.value } 
      sortedAOMValues.each { ordinal ->
      
         def archetypeTerm = attrs.archetype.ontology.termDefinition(session.locale.language, ordinal.symbol.codeString)
         if (archetypeTerm)
         {
            labels << archetypeTerm.items.text
         }
         else
         {
            // FIXME: definir un texto por defecto que diga que cierto codigo no tiene traduccion
            //        para el idioma seleccionado xxx, mostrando el idioma.
            //El termino con codigo [${ordinal.symbol.codeString}] no esta definido en el arquetipo,
            //posiblemente el termino no esta definido para el lenguaje seleccionado.<br/>
         }
      }
      
      List values = sortedAOMValues.symbol.codeString
   
      // Control de template
      def control = attrs.template.getField( archetypeId, aomNode.path() )?.getControlByPath(aomNode.path())
      
      if (control && control.type=='radioGroup')
      {
         itn i = 0
         values.each { value ->
           out << '<label id="id_'+ value +'">'
           out << '<input type="radio" value="'+ value +'" name="'+ archetypeId +refPath+ aomNode.path() +'" '+ ((value==selectedValue)?'checked="true"':'') +' />'
           out << labels[i]
           out << '</label>'
           i++
         }
      }
      else
      {
         out << g.select( from: labels, keys: values,
                          name: archetypeId +refPath+ aomNode.path(),
                          noSelection: ['':''],
                          value: selectedValue)
      }
   }
   
   def editDvQuantity = { attrs, body ->
      
      String pathFromOwner = attrs.pathFromOwner
      String archetypeId = attrs.archetype.archetypeId.value
      
      // parent.rmTypeName es ELEMENT
      CObject aomNode = attrs.archetype.node(pathFromOwner)
      
      String refPath = attrs.refPath
      
      // Valores a mostrar
      String selectedValueMagnitude = attrs.dataValue.magnitude
      String selectedValueUnits = attrs.dataValue.units
      
      
      // Verificacion de errores
      if ( attrs.dataValue.errors.hasErrors() )
      {
         /* Lo hace el ELEMENT
         out << '<div class="error">'
         out << g.renderErrors( bean: attrs.dataValue, as: "list" )
         out << '</div>'
         */
         
         // Me fijo si tiene error para saber que valor mostrar, siempre sera el valor ingresado
         if (attrs.dataValue.errors.hasFieldErrors('magnitude'))
         {
            selectedValueMagnitude = attrs.dataValue.errors.getFieldError('magnitude')?.rejectedValue
         }
         if (attrs.dataValue.errors.hasFieldErrors('units'))
         {
           selectedValueUnits = attrs.dataValue.errors.getFieldError('units')?.rejectedValue
         }
      }
      
      // Es un interval de constraints para mostrar el rango de validez
      // interval puede ser null
      def interval = aomNode.list?.find { it.magnitude != null }
      
      // Muestra el rango viendo si tiene lower y upper
      out << ((interval?.lower)? interval.lower : '*')
      out << '..'
      out << ((interval?.upper)? interval.upper : '*')
      
      // Edit de magnitude
      out << '<input type="text" name="'+ attrs.archetype.archetypeId.value +refPath+ aomNode.path() +'/magnitude" value="'+ selectedValueMagnitude +'" />'
      
      // Veo las unidades
      if (aomNode.list.units.size()==1)
      {
         // Constraint del template, para ver si tiene una transformacion
         Object constraint = attrs.template.getField( archetypeId, aomNode.path() )?.getConstraintByPath(aomNode.path()+'/units')
         if (constraint)
         {
            // Mustro la unica unidad que tiene, sobreescrita
            out << constraint.process( aomNode.list.units[0] )
         }
         else
         {
            out << aomNode.list.units[0] // Muestro la unica unidad que tiene
         }
      }
      else // Hay mas de una unidad posible
      {
         out << g.select( from: aomNode.list.units,
                          name: archetypeId + refPath + aomNode.path() + '/units',
                          noSelection: ['':''],
                          value: selectedValueUnits )
      }
   }
   
   def editDvText = { attrs, body ->

         String refPath = attrs.refPath
         String pathFromOwner = attrs.pathFromOwner
         
         // Si es null me muestra 'null', y quiero que muestre vacio
         String showValue = ((attrs.dataValue.value) ? attrs.dataValue.value : '')
         
         out << '<textarea name="'+ attrs.archetype.archetypeId.value +refPath+ pathFromOwner +'">'+ showValue +'</textarea>'
   }
   
   def editDvTime = { attrs, body ->
      
      // TODO
      //out << attrs.dataValue.value
   }
   
   // TODO: demas tipos soportados
}