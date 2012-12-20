<%@ page import="com.thoughtworks.xstream.XStream" %>
<?xml version="1.0" encoding="ISO-8859-1" ?>
<html>
  <head>
    <meta name="layout" content="ehr" />
    <link rel="stylesheet" href="${createLinkTo(dir:'css', file:'generarShow.css')}" />
  </head>
  <body>
    <g:if test="${flash.message}">
      <div class="message"><g:message code="${flash.message}" /></div>
    </g:if>
    <%--
    <textarea style="width: 700px; height: 200px;">${new XStream().toXML(rmNode)}</textarea>
    --%>
    <%--
    TODO: el menu deberia ir a show no al registro, a no ser que aun no se
    haya registrado nada...

    <h1>Template: ${rmNode.archetypeDetails.templateId}</h1>
    --%>
    <%-- SUBMENU DE SECCIONES SI EXISTEn --%>
    <g:if test="${subsections.size()>1}">
      <div id="navbar">
        <ul>
          <g:each in="${subsections}" var="subsection">
            <li ${((template.templateId==subsection)?'class="active"':'')}>
              <g:hasContentItemForTemplate episodeId="${episodeId}" templateId="${subsection}">
                <g:if test="${it.hasItem}">
                  <g:link controller="guiGen" action="generarShow" id="${it.itemId}"><g:message code="${'section.'+subsection}" /> (*)</g:link>
                </g:if>
                <g:else>
                  <g:link controller="guiGen" action="generarTemplate" params="[templateId:subsection]">
                    <g:message code="${'section.'+subsection}" />
                  </g:link>
                </g:else>
              </g:hasContentItemForTemplate>
            </li>
          </g:each>
        </ul>
      </div>
    </g:if>
    <g:form action="save" class="ehrform" method="post" enctype="multipart/form-data">
    
      <input type="hidden" name="templateId" value="${template.templateId}" />
      <input type="hidden" name="mode" value="${mode}" />
      
      <table class="container" cellpadding="0" cellspacing="3">
        <tr>
          <td colspan="2" id="content">
            <g:each in="${template.getArchetypesByZone('content')}" var="archRef">
              <g:if test="${index[archRef.id]}">
                <!-- FIXME: habria que arrancar del nodo que diga el template (p.e. esto es correcto si 
                            arranca de la raiz pero no si tiene un field con path distinta a "/"
                            http://code.google.com/p/open-ehr-gen-framework/issues/detail?id=19
                -->
                <%-- Paths de los fields del archRef para los que se debe mostrar GUI
                 El cequeo podria ser una taglib
                 el chequeo para saber si mostrar el nodo se hace con: fieldPaths.find{ rmNode.path.startsWith(it.path)} != null
                --%>
                <g:set var="fieldPaths" value="${['/']}" />
                <g:if test="${archRef.fields?.size()>0}">
                  <g:set var="fieldPaths" value="${archRef.getFieldPaths()}" />
                </g:if>
                <!-- RM -->
                <g:set var="rmNode" value="${index[archRef.id]}" />
                <g:set var="templateName" value="${rmNode.getClassName()}" />
                <g:render template="../guiGen/editTemplates/${templateName}"
                          model="[rmNode:rmNode, fieldPaths:fieldPaths, archetype:archRef.getReferencedArchetype()]" />
              </g:if>
              <g:else><%-- No hay estructura del RM, voy por el AOM --%>
                <!-- AOM -->
                <g:each in="${archRef.getReferencedConstraints()}" var="node">
                  <g:render template="../guiGen/templates2/cComplexObject"
                            model="[cComplexObject: node, params: params, archetype: archRef.getReferencedArchetype()]" />
                </g:each>
              </g:else>
            </g:each>
          </td>
        </tr>
        <tr>
          <td id="left">
            <g:each in="${template.getArchetypesByZone('left')}" var="archRef">
              <g:if test="${index[archRef.id]}">
                <!-- RM -->
                <%-- Paths de los fields del archRef para los que se debe mostrar GUI
                 El cequeo podria ser una taglib
                 el chequeo para saber si mostrar el nodo se hace con: fieldPaths.find{ rmNode.path.startsWith(it.path)} != null
                --%>
                <g:set var="fieldPaths" value="${['/']}" />
                <g:if test="${archRef.fields?.size()>0}">
                  <g:set var="fieldPaths" value="${archRef.getFieldPaths()}" />
                </g:if>
                <g:set var="rmNode" value="${index[archRef.id]}" />
                <g:set var="templateName" value="${rmNode.getClassName()}" />
                <g:render template="../guiGen/editTemplates/${templateName}"
                          model="[rmNode:rmNode, fieldPaths:fieldPaths, archetype:archRef.getReferencedArchetype()]" />
              </g:if>
              <g:else><%-- No hay estructura del RM, voy por el AOM --%>
                <!-- AOM -->
                <g:each in="${archRef.getReferencedConstraints()}" var="node">
                   <g:render template="../guiGen/templates2/cComplexObject"
                             model="[cComplexObject: node, params: params, archetype: archRef.getReferencedArchetype()]" />
                </g:each>
              </g:else>
            </g:each>
          </td>
          <td id="right">
            <g:each in="${template.getArchetypesByZone('right')}" var="archRef">
              <g:if test="${index[archRef.id]}">
                <!-- RM -->
                <%-- Paths de los fields del archRef para los que se debe mostrar GUI
                 El cequeo podria ser una taglib
                 el chequeo para saber si mostrar el nodo se hace con: fieldPaths.find{ rmNode.path.startsWith(it.path)} != null
                --%>
                <g:set var="fieldPaths" value="${['/']}" />
                <g:if test="${archRef.fields?.size()>0}">
                  <g:set var="fieldPaths" value="${archRef.getFieldPaths()}" />
                </g:if>
                <g:set var="rmNode" value="${index[archRef.id]}" />
                <g:set var="templateName" value="${rmNode.getClassName()}" />
                <g:render template="../guiGen/editTemplates/${templateName}"
                          model="[rmNode:rmNode, fieldPaths:fieldPaths, archetype:archRef.getReferencedArchetype()]" />
              </g:if>
              <g:else><%-- No hay estructura del RM, voy por el AOM --%>
              <!-- AOM -->
                <g:each in="${archRef.getReferencedConstraints()}" var="node">
                   <g:render template="../guiGen/templates2/cComplexObject"
                             model="[cComplexObject: node, params: params, archetype: archRef.getReferencedArchetype()]" />
                </g:each>
              </g:else>
            </g:each>
          </td>
        </tr>
        <tr>
          <td colspan="2" id="bottom">
            <g:each in="${template.getArchetypesByZone('bottom')}" var="archRef">
              <g:if test="${index[archRef.id]}">
                <!-- RM -->
                <%-- Paths de los fields del archRef para los que se debe mostrar GUI
                 El cequeo podria ser una taglib
                 el chequeo para saber si mostrar el nodo se hace con: fieldPaths.find{ rmNode.path.startsWith(it.path)} != null
                --%>
                <g:set var="fieldPaths" value="${['/']}" />
                <g:if test="${archRef.fields?.size()>0}">
                  <g:set var="fieldPaths" value="${archRef.getFieldPaths()}" />
                </g:if>
                <g:set var="rmNode" value="${index[archRef.id]}" />
                <g:set var="templateName" value="${rmNode.getClassName()}" />
                <g:render template="../guiGen/editTemplates/${templateName}"
                          model="[rmNode:rmNode, fieldPaths:fieldPaths, archetype:archRef.getReferencedArchetype()]" />
              </g:if>
              <g:else><%-- No hay estructura del RM, voy por el AOM --%>
              <!-- AOM -->
                <g:each in="${archRef.getReferencedConstraints()}" var="node">
                  <g:render template="../guiGen/templates2/cComplexObject"
                             model="[cComplexObject: node, params: params, archetype: archRef.getReferencedArchetype()]" />
                </g:each>
              </g:else>
            </g:each>
          </td>
        </tr>
      </table>
      <br/>
      <div class="bottom_actions">
        <g:isNotSignedRecord episodeId="${episodeId}">
          <%-- edit --%>
          <g:submitButton name="doit" value="Guardar" /> |
        </g:isNotSignedRecord>
        <g:link controller="records" action="registroClinico"><g:message code="trauma.show.action.back" /></g:link>
      </div>
    </g:form>
  </body>
</html>