<%@ page import="util.UniqueIdIssuer" %><%@ page import="util.FieldNames" %>
<g:set var="fields" value="${FieldNames.getInstance()}" />
<%--
in: cComplexObject (${cComplexObject.rmTypeName})<br/>
in: refPath path del internal ref a este nodo si es que hay.

<b>${cComplexObject.path()}</b>

Complex: ${cComplexObject.rmTypeName}<br/><br/>
--%>
<%
// refPath es nulo si no viene de un arch internal ref

def _refPath = ''
if (refPath) _refPath = refPath

%>
<%-- Por si el elemento tiene varias ocurencias, ojo puede ser * --%>
<%
  // FIXME: poner que sea igual que un nodo multiple, pero que se pueda agregar hasta el upper cantidad de nodos clonados.
  // Si no es null, es eso, si no es 1.
  def max = ((cComplexObject.occurrences.upper) ? cComplexObject.occurrences.upper : 1)
  for (i in 1..max) {
%>
<%-- Agrego DV_CODED_TEXT y DV_COUNT porque genera un contenedor mas dentro del contenedor ELEMENT porque es modelado con CComplexObject y no es necesario ese contenedor extra (jode el estilo) --%>
<g:if test="${!['ACTIVITY','HISTORY','ITEM_TREE','ITEM_TABLE','ITEM_LIST','ITEM_SINGLE','DV_CODED_TEXT','DV_TEXT','DV_COUNT','DV_BOOLEAN','DV_DATE_TIME', 'DV_DURATION'].contains( cComplexObject.rmTypeName )}">

  <%-- Si es ELEMENT, quiero el tipo de su value para poder ponerlo en el class de la div, y asi poder ajustar la vista con CSS --%>
  <g:if test="${cComplexObject.rmTypeName == 'ELEMENT'}">
    <g:set var="class1" value="ELEMENT_${cComplexObject.attributes[0].children[0].rmTypeName}" />
    
    <%-- field con path al element.value, es solo para saber el container para mostrar errores en el edit --%>
    <g:set var="class2" value="${fields.getField(archetype.archetypeId.value +_refPath+ cComplexObject.path()+'/value')}" />
  </g:if>
  
  <%-- Si es un nodo muiltiple, le pone class 'multiple' para que desde JS le asocie la accion de multiplicarlo. --%>
  <%-- necesito la path del cComplexObject para ponerle el error en el edit, pido como si fuera un campo --%>
  <div class="${cComplexObject.rmTypeName} ${class1} ${class2} ${fields.getField(archetype.archetypeId.value +_refPath+ cComplexObject.path())} ${((cComplexObject.occurrences.isUpperUnbounded())?'multiple':'')}"><%-- FIXME: no quiero mostrar esto para campos simples, solo para sections, clusters y elements --%>
    <!-- PATH: ${archetype.archetypeId.value +_refPath+ cComplexObject.path()} -->
    <g:if test="${cComplexObject.nodeID}">
      <!-- Si es item structure no muestra el titulo -->
      <span class="label">
        <g:displayTerm archetype="${archetype}" code="${cComplexObject.nodeID}" locale="${locale}" />:
      </span>
    </g:if>
    <g:else><%-- si no tengo nodeID, busco por path --%>

      <%-- El problema es que deberia caer aqui solo si el atributo simple (son los que no tienen nodeID)
           no esta adentro de un element, ya que para el element ya se muestra la label.
           El tema es que para los primitives hijos de elements, no defino TermBindings en el arq. --%>
      
      <g:each in="${archetype.ontology.getTermBindingList()}" var="ontologyBinding">
        <g:set var="termBindingItem" value="${ontologyBinding.getBindingList().find{ it.code == cComplexObject.path() }}" />
        <g:if test="${termBindingItem}">
          <span class="label">
            <%-- ${termBindingItem.terms[0].replace('::','-')} el origen es '[xxx::eee]' --%>
            <g:message code="${termBindingItem.terms[0].replace('::','-')}" />
          </span>
        </g:if>
      </g:each>
    </g:else>
    <span class="content">
</g:if>
<%--
if ( errors && errors.hasErrorsForPath(archetype.archetypeId.value, cComplexObject.path()) )
{
	println "<h1>"+errors.getErrors(archetype.archetypeId.value, cComplexObject.path())+"</h1>"
}
--%>
    <g:if test="${cComplexObject.rmTypeName.startsWith('DV_INTERVAL')}"><%-- DV_INTERVAL<DV_COUNT> --%>
      <g:if test="${cComplexObject.attributes}">
        <%-- esto hace lo mismo que _cAttribute.gsp --%>
        <g:render template="../guiGen/edit/cAttribute"
                  var="cAttribute"
                  collection="${cComplexObject.attributes}"
                  model="[archetype: archetype,
                          archetypeService: archetypeService,
                          refPath: refPath,
                          params: params, lang: lang, locale: locale, template: template]" />
      </g:if>
    </g:if>
    <g:if test="${cComplexObject.rmTypeName.startsWith('DV_MULTIMEDIA')}">
      <input type="file" name="${fields.getField(archetype.archetypeId.value +_refPath+ cComplexObject.path())}" />
    </g:if>
    <g:else>
      <%-- Verifico que no sea null porque puede serlo. --%>
      <g:if test="${cComplexObject.attributes}">
        <%-- ${cComplexObject.attributes.size()} --%>
        
        <%-- Para las instrucciones, sino esta el narrative en el arquetipo, hay que generarlo igual --%>
        <g:if test="${cComplexObject.rmTypeName == 'INSTRUCTION'}">
          <g:set var="hasNarrative" value="${false}" />
        </g:if>
        <g:each in="${cComplexObject.attributes}" var="cAttribute">
        
          <%--
          ${cAttribute.rmAttributeName}
          --%>
        
          <%-- Las ENTRIES tienen protocolo, y si esta definido en el arquetipo quiero mostrar el titulo --%>
          <g:if test="${['OBSERVATION','EVALUATION','INSTRUCTION','ACTION'].contains(cComplexObject.rmTypeName) && cAttribute.rmAttributeName == 'protocol'}">
            <span class="label"><g:message code="protocol" /></span>
          </g:if>
          <g:if test="${['OBSERVATION'].contains(cComplexObject.rmTypeName) && cAttribute.rmAttributeName == 'state'}">
            <span class="label"><g:message code="state" /></span>
          </g:if>
          <g:if test="${['INTERVAL_EVENT'].contains(cComplexObject.rmTypeName) && cAttribute.rmAttributeName == 'math_function'}">
            <span class="label datavalue"><g:message code="math_function" /></span>
          </g:if>
          
          <%-- Para las instrucciones, sino esta el narrative en el arquetipo, hay que generarlo igual --%>
          <g:if test="${cComplexObject.rmTypeName == 'INSTRUCTION' && cAttribute.rmAttributeName == 'narrative'}">
            <g:set var="hasNarrative" value="${true}" />
          </g:if>
          
          <%-- esto hace lo mismo que _cAttribute.gsp --%>
          <g:render template="../guiGen/edit/cObject"
            var="cObject"
            collection="${cAttribute.children}"
            model="[archetype: archetype,
                    archetypeService: archetypeService,
                    refPath: refPath,
                    params: params, lang: lang, locale: locale, template: template]" />
        </g:each>
        
        <%-- Para las instrucciones, sino esta el narrative en el arquetipo, hay que generarlo igual
        narrative va sin / al inicio porque la path ya lo tiene.
        --%>
        <g:if test="${cComplexObject.rmTypeName == 'INSTRUCTION' && !hasNarrative}">
          <g:message code="instruction.narrative" /><br/>
          <textarea name="${fields.getField(archetype.archetypeId.value+_refPath+cComplexObject.path()+'narrative')}">x</textarea>
        </g:if>
        
      </g:if>
      <g:else><%-- muestra nodos sin restriccion, solo si no tiene atributos para seguir navegando --%>
      
        <g:set var="control" value="${template.getField( archetype.archetypeId.value, cComplexObject.path() )?.getControlByPath(cComplexObject.path())}" />
        
        <!-- TODO: que la generacion de controles para los tipos lo haga la taglib -->
        
        <g:if test="${cComplexObject.rmTypeName == 'DV_TEXT'}">
          <g:if test="${control && control.type=='smallText'}">
            <input type="text" name="${fields.getField(archetype.archetypeId.value +_refPath+ cComplexObject.path())}" />
          </g:if>
          <g:else>
            <%-- Si text se muestra desde CComplexObject, es un texto libre --%>
            <!-- FIXME: me genera <textarea name="field_140"/> y no es HTML valido, esto es porque el el pretty print del XML detecta la tag sin valor y la reduce. -->
            <textarea name="${fields.getField(archetype.archetypeId.value+_refPath+cComplexObject.path())}">x</textarea>
          </g:else>
        </g:if>
        <g:else>
           <g:if test="${cComplexObject.rmTypeName == 'DV_DATE_TIME'}">
             <%-- Si datetime se muestra desde CComplexObject, no tiene restricciones sobre la forma de la fecha o las fechas posibles. --%>
             <g:datePicker name="${fields.getField(archetype.archetypeId.value +_refPath+ cComplexObject.path())}" value="${new Date()}" precision="minute" />
           </g:if>
           <g:else>
             <g:if test="${cComplexObject.rmTypeName == 'DV_DATE'}">
               <%-- Si date se muestra desde CComplexObject, no tiene restricciones sobre la forma de la fecha o las fechas posibles. --%>
               <g:datePicker name="${fields.getField(archetype.archetypeId.value +_refPath+ cComplexObject.path())}" value="${new Date()}" precision="day" />
             </g:if>
             <g:else>
               <%-- TODO: tipo DV_TIME --%>
               <%-- FIXME: nunca muestra DvCount aca, entra a mostrar los atrivutes... --%>
               <g:if test="${cComplexObject.rmTypeName == 'DV_COUNT'}">

                 <%-- Si count se muestra desde complexObject es que no tiene restricciones. --%>
                 (*..*) <input type="text" name="${fields.getField(archetype.archetypeId.value +_refPath+ cComplexObject.path())}" />
               </g:if>
               <g:else>
                 <g:if test="${cComplexObject.rmTypeName == 'DV_DURATION'}">
                   <input type="hidden" name="${fields.getField(archetype.archetypeId.value +_refPath+ cComplexObject.path())}" value="duration.struct" />
                   <g:select name="${fields.getField(archetype.archetypeId.value +_refPath+ cComplexObject.path())+'_years'}" from="${0..10}" /> Y
                   <g:select name="${fields.getField(archetype.archetypeId.value +_refPath+ cComplexObject.path())+'_months'}" from="${0..24}" /> M
                   <g:select name="${fields.getField(archetype.archetypeId.value +_refPath+ cComplexObject.path())+'_days'}" from="${0..100}" /> D
                   <g:select name="${fields.getField(archetype.archetypeId.value +_refPath+ cComplexObject.path())+'_hours'}" from="${0..72}" /> h
                   <g:select name="${fields.getField(archetype.archetypeId.value +_refPath+ cComplexObject.path())+'_minutes'}" from="${0..59}" /> m
                   <g:select name="${fields.getField(archetype.archetypeId.value +_refPath+ cComplexObject.path())+'_seconds'}" from="${0..60}" /> s
                 </g:if>
                 <g:else>
                   Tipo no soportado: ${cComplexObject.rmTypeName}
                 </g:else>
               </g:else>
             </g:else>
           </g:else>
         </g:else>
      </g:else>
    </g:else>

<g:if test="${ ! ['ACTIVITY','HISTORY','ITEM_TREE','ITEM_TABLE','ITEM_LIST','ITEM_SINGLE','DV_CODED_TEXT','DV_TEXT','DV_COUNT','DV_BOOLEAN','DV_DATE_TIME', 'DV_DURATION'].contains( cComplexObject.rmTypeName )}">

    </span>
  </div>
</g:if>
<% } // si occurrences.upper >1 y no es * repito el nodo %>