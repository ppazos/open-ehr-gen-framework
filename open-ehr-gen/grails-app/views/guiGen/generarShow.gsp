<%@ page import="com.thoughtworks.xstream.XStream" %>
<?xml version="1.0" encoding="ISO-8859-1" ?>
<html>
  <head>
    <meta name="layout" content="ehr" />
    <style>
    #content, #left, #right, #bottom {
      vertical-align: top;
    }
    #left {
      width: 50%;
    }
    #right {
      width: 50%;
    }
    .right {
      float: right;
    }
    
    .OBSERVATION .label, .EVALUATION .label, .INSTRUCTION .label, .ACTION .label {
      font-size: 14px;
      font-weight: bold;
      padding: 3px;
      margin-bottom: 2px;
    }

<%--
    .OBSERVATION, .EVALUATION {
      width: 50%;
      /* border: 1px solid #ffff00; */
      float: left;
      background-color: #ffdddd;
    }
    .INSTRUCTION, .ACTION {
      width: 50%;
      /* border: 1px solid #ffff00; */
      float: right;
      background-color: #ddddff;
    }
--%>

    .INSTRUCTION_narrative{
      padding: 3px;
      display: block;
      font-weight: normal;
      margin-bottom: 3px;
      background-color: #aaff99;
      border: 1px solid #33ff33;
    }
    .CLUSTER {
      margin-bottom: 3px;
      font-weight: bold;
      border: 1px solid #6B90DA;
    }    
    .CLUSTER .label {
      padding: 3px;
      background-color: #BDCDF5;
      font-size: 13px;
      margin: 0px;
    }
    .CLUSTER .content {
      padding: 3px;
      display: block;
      background-color: #EBEFF9;
    }
    .ELEMENT {
      font-weight: normal;
      margin-bottom: 3px;
      background-color: #ffff99;
      border: 1px solid #6B90DA;
    }
    .ELEMENT .label {
      padding: 4px;
      font-size: 12px;
      margin-bottom: 0px;
      background-color: #BDCDF5;
    }
    .ELEMENT .content {
      padding: 3px;
      display: block;
      overflow: auto;
      background-color: transparent;
    }

    /* mejor aprovechamiento del espacio para DvOrdinal poniendo el titulo del ELEMENT a la izquierda del contenido */
    .ELEMENT_DvOrdinal .label, .ELEMENT_DV_CODED_TEXT .label, .ELEMENT_DV_COUNT .label, .ELEMENT_DvQuantity .label, .ELEMENT_DV_BOOLEAN .label {
      padding: 10px;
      padding-left: 6px;
      padding-right: 3px;
      display: inline-block;
      position: relative;
      vertical-align: middle;
      margin-bottom: 0px;
      font-weight: normal;
      width: 140px;
    }
    /* Si pongo .ELEMENT_DvOrdinal .content se ve mal el triage */
    .ELEMENT_DV_CODED_TEXT .content, .ELEMENT_DV_COUNT .content, .ELEMENT_DvQuantity .content, .ELEMENT_DV_BOOLEAN .content {
      display: inline-block;
      position: relative;
      vertical-align: middle;
    }
    .ELEMENT_DvQuantity input { /* que el input donde se pone el numero sea chico */
      width: 60px;
    }
    .ELEMENT .content label {
      display: inline-block;
      margin-right: 3px;
      padding-top: 3px;
      padding-bottom: 3px;
    }
    .ELEMENT img {
      max-width: 385px;
    }
    .label {
      display: block;
    }
    select {
      width: auto;
    }
    
    /*******************************************************/
    /* Para los boolean que el SI NO tenga el mismo largo. */
    label input {
      vertical-align: middle;
    }
    label span {
      /*background-color: #00ff00;*/ /* Para distinguir a que se aplica, lo muestra verde. */
      width: 30px;
      height: 22px;
      text-align: right;
      display: inline-block;
    }
    /*
    label:hover {
      background-color: #ddddff;
    }
    */
    /* / Para los boolean que el SI NO tenga el mismo largo. */
    /*********************************************************/

    .active {
      /*font-weight: bold;*/
    }
    .multiple {
      text-align: right;
      padding: 5px;
    }
    .slot {
      border: 2px solid red;
      padding: 2px;
    }
    .archetype {
      /* background-color: #ddddff; */
      padding: 10px;
      border: 1px solid #dddd80;
    }
    .field {
      padding: 10px;
      border: 1px solid #80dddd;
    }
    
    /* --- NAVBAR ----------------------------- */ 
    #navbar
    {          
      width: 100%;
      /*padding-top: 5px;*/
      line-height: normal;
      font-size: 12px;
      margin-top: 0px;
      clear: both;
      /*background: #e0e0e0;*/
      text-align: left;
    }
    #navbar a {
       padding: 0px;
       text-decoration: none;
       color: #000;
       padding: 4px 10px 2px 10px;
       width: 100%;
    }
    #navbar ul {
       margin: 0;
       padding: 5px 0px 2px 20px;
       list-style: none;
       /*border-bottom: solid 1px #bbb;*/
    }
    #navbar li {
       margin-right: 1px;
       padding: 4px 0px 2px 0px;
       display: inline;
       color: #666;
       border: solid 1px #000000;
       border-bottom: 1px solid #000000;
       background-color: #efefef;
    }
    #navbar li.active {
       border: 1px solid #000000;
       border-bottom: 1px solid #ffffdd;
       /*font-weight: bold;*/
       background-color: #ffffdd;
    }
    </style>
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
            <li ${((template.id==subsection)?'class="active"':'')}>
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
    
      <input type="hidden" name="templateId" value="${template.id}" />
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
                <g:render template="../guiGen/showTemplates/Locatable"
                          model="[rmNode: index[archRef.id], fieldPaths: fieldPaths,
                                  archetype: archRef.getReferencedArchetype()]" />
              </g:if>
              <g:else><%-- No hay estructura del RM, voy por el AOM --%>
                <!-- AOM -->
                <g:each in="${archRef.getReferencedConstraints()}" var="node">
                  <g:render template="../guiGen/templates2/cComplexObject"
                            model="[cComplexObject: node, params: params,
                                   archetype: archRef.getReferencedArchetype()]" />
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
                <g:render template="../guiGen/showTemplates/Locatable"
                          model="[rmNode: index[archRef.id], fieldPaths: fieldPaths,
                                 archetype: archRef.getReferencedArchetype()]" />
              </g:if>
              <g:else><%-- No hay estructura del RM, voy por el AOM --%>
                <!-- AOM -->
                <g:each in="${archRef.getReferencedConstraints()}" var="node">
                   <g:render template="../guiGen/templates2/cComplexObject"
                             model="[cComplexObject: node, params: params,
                                    archetype: archRef.getReferencedArchetype()]" />
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
                <g:render template="../guiGen/showTemplates/Locatable"
                          model="[rmNode: index[archRef.id], fieldPaths: fieldPaths,
                                 archetype: archRef.getReferencedArchetype()]" />
              </g:if>
              <g:else><%-- No hay estructura del RM, voy por el AOM --%>
              <!-- AOM -->
                <g:each in="${archRef.getReferencedConstraints()}" var="node">
                   <g:render template="../guiGen/templates2/cComplexObject"
                             model="[cComplexObject: node, params: params,
                                    archetype: archRef.getReferencedArchetype()]" />
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
                <g:render template="../guiGen/showTemplates/Locatable"
                          model="[rmNode: index[archRef.id], fieldPaths: fieldPaths,
                                 archetype: archRef.getReferencedArchetype()]" />
              </g:if>
              <g:else><%-- No hay estructura del RM, voy por el AOM --%>
              <!-- AOM -->
                <g:each in="${archRef.getReferencedConstraints()}" var="node">
                  <g:render template="../guiGen/templates2/cComplexObject"
                             model="[cComplexObject: node, params: params,
                                    archetype: archRef.getReferencedArchetype()]" />
                </g:each>
              </g:else>
            </g:each>
          </td>
        </tr>
      </table>
      <br/>
      <div class="bottom_actions">
        <g:isNotSignedRecord episodeId="${episodeId}">
        <g:if test="${mode=='edit'}">
          <g:submitButton name="doit" value="Guardar" />
        </g:if>
        <g:else>
          <g:link action="generarShow" id="${rmNode.id}" params="[mode:'edit']"><g:message code="trauma.show.action.edit" /></g:link>
        </g:else>
        |
        </g:isNotSignedRecord>
        <g:link controller="records" action="registroClinico"><g:message code="trauma.show.action.back" /></g:link>
      </div>

    </g:form>
  </body>
</html>