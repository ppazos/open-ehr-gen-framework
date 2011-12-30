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
<g:if test="${ ! ['ACTIVITY','HISTORY','EVENT','ITEM_TREE','ITEM_TABLE','ITEM_LIST','ITEM_SINGLE','DV_CODED_TEXT','DV_TEXT','DV_COUNT','DV_BOOLEAN','DV_DATE_TIME'].contains( cComplexObject.rmTypeName )}">
  <%-- Si es ELEMENT, quiero el tipo de su value para poder ponerlo en el class de la div, y asi poder ajustar la vista con CSS --%>
  <g:if test="${cComplexObject.rmTypeName == 'ELEMENT'}">
    <g:set var="elementValueRmType" value="ELEMENT_${cComplexObject.attributes[0].children[0].rmTypeName}" />
  </g:if>
  <div class="${cComplexObject.rmTypeName} ${elementValueRmType} ${((cComplexObject.occurrences.isUpperUnbounded())?'multiple':'')}"><%-- FIXME: no quiero mostrar esto para campos simples, solo para sections, clusters y elements --%>
    <g:if test="${cComplexObject.nodeID}">
      <!-- Si es item structure no pone el titulo -->
      <%--
      <g:set var="archetypeTerm" value="${archetype.ontology.termDefinition(lang, cComplexObject.nodeID)}" />
      <g:if test="${!archetypeTerm}">
         < % - - Si es un nodo hoja, siempre cae aca porque no tiene ID - - % >
         El termino con codigo [${cComplexObject.nodeID}] no esta definido en el arquetipo, 
         posiblemente el termino no esta definido para el lenguaje ${lang}.
         <br/><br/>
         Puedo hacer Term Bindings a terminilogias externas, usando path based bindins (adl.pdf pag 100).
      </g:if>
      <g:else>
         <span class="label">
           ${archetypeTerm.text}:
         </span>
      </g:else>
      --%>
        
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
      <%-- TODO?? ver lower y upper 
      es el mismo codigo que abajo...
      --%>
      <g:if test="${cComplexObject.attributes}">
        <g:render template="../guiGen/show/cAttribute"
                  var="cAttribute"
                  collection="${cComplexObject.attributes}"
                  model="[archetype: archetype,
                          archetypeService: archetypeService,
                          refPath: refPath,
                          params: params, lang: lang, locale: locale, template: template]" />
      </g:if>
    </g:if>
    <g:if test="${cComplexObject.rmTypeName.startsWith('DV_MULTIMEDIA')}">
      <%-- TODO: mostrar multimedia
      <input type="file" name="${archetype.archetypeId.value +_refPath+ cComplexObject.path()}" />
      --%>
    </g:if>
    <g:else>
      <%-- Verifico que no sea null porque puede serlo. --%>
      <g:if test="${cComplexObject.attributes}">
          <%-- ${cComplexObject.attributes.size()} --%>
          <g:render template="../guiGen/show/cAttribute"
                    var="cAttribute"
                    collection="${cComplexObject.attributes}"
                    model="[archetype: archetype,
                            archetypeService: archetypeService,
                            refPath: refPath,
                            params: params, lang: lang, locale: locale, template: template]" />
      </g:if>
      <g:else><%-- muestra nodos sin restriccion, solo si no tiene atributos para seguir navegando --%>
      
        <g:set var="control" value="${template.getField( archetype.archetypeId.value, cComplexObject.path() )?.getControlByPath(cComplexObject.path())}" />
        
        <g:if test="${cComplexObject.rmTypeName == 'DV_TEXT'}">
          <g:if test="${control && control.type=='smallText'}">
            <label class="${fields.getField(archetype.archetypeId.value +_refPath+ cComplexObject.path())}"></label>
          </g:if>
          <g:else>
            <label class="${fields.getField(archetype.archetypeId.value +_refPath+ cComplexObject.path())}"></label>
          </g:else>
        </g:if>
        
        <g:if test="${cComplexObject.rmTypeName == 'DV_DATE_TIME'}">
          <%-- Si datetime se muestra desde CComplexObject, no tiene restricciones sobre la forma de la fecha o las fechas posibles. --%>
          <label class="${fields.getField(archetype.archetypeId.value +_refPath+ cComplexObject.path())}"></label>
        </g:if>
        <g:if test="${cComplexObject.rmTypeName == 'DV_DATE'}">
          <%-- Si date se muestra desde CComplexObject, no tiene restricciones sobre la forma de la fecha o las fechas posibles. --%>
          <label class="${fields.getField(archetype.archetypeId.value +_refPath+ cComplexObject.path())}"></label>
        </g:if>
        <%-- TODO: tipo DV_TIME --%>
        <%-- FIXME: nunca muestra DvCount aca, entra a mostrar los atrivutes... --%>
        <g:if test="${cComplexObject.rmTypeName == 'DV_COUNT'}">

          <%-- Si count se muestra desde complexObject es que no tiene restricciones. --%>
          (*..*)
          <label class="${fields.getField(archetype.archetypeId.value +_refPath+ cComplexObject.path())}"></label>
        </g:if>
      </g:else>
    </g:else>

<g:if test="${ ! ['ACTIVITY','HISTORY','EVENT','ITEM_TREE','ITEM_TABLE','ITEM_LIST','ITEM_SINGLE','DV_CODED_TEXT','DV_TEXT','DV_COUNT','DV_BOOLEAN','DV_DATE_TIME'].contains( cComplexObject.rmTypeName )}">

    </span>
  </div>
</g:if>
<% } // si occurrences.upper >1 y no es * repito el nodo %>