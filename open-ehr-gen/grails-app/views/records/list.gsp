<%@ page import="org.codehaus.groovy.grails.commons.ApplicationHolder" %>
<%@ page import="hce.core.common.change_control.Version" %>
<%@ page import="java.text.SimpleDateFormat" %>
<html>
  <head>
    <meta name="layout" content="ehr-modal" />
    <title><g:message code="episodio.list.title" /></title>
    <style>
        table #list {
          background-color: #ffffdd;
          width: 100%;
          font-size: 12px;
          border: 1px solid #000;
        }
         #list th {
          background-color: #ccccdd;
        }
        #list td {
          text-align: center;
        }
      </style>
  </head>
  <body>
    <h1><g:message code="episodio.list.title" /></h1>
    
    <ul class="top_actions">
      <li>
        <g:link action="create" class="create"><g:message code="trauma.list.action.crearEpisodio" /></g:link>
      </li>
    </ul>
    
    <table id="list">
      <tr>
        <th><g:message code="trauma.list.label.id" /></th>
        <th><g:message code="trauma.list.label.startTime" /></th>
        <th><g:message code="trauma.list.label.endTime" /></th>
        <th><g:message code="trauma.list.label.observations" /></th>
        <th><g:message code="trauma.list.label.state" /></th>
        <th><g:message code="trauma.list.label.actions" /></th>
      </tr>
      <g:each in="${compositions}" var="composition">
        <tr>
          <td>${composition.id}</td>
          <td><g:format date="${composition.context.startTime?.toDate()}" /></td>
          <td><g:format date="${composition.context.endTime?.toDate()}" /></td>
          <td>
            <%-- OJO: Solo funciona si el otherContext es ItemSingle y el value del Element es DvText --%>
            ${composition.context.otherContext.item.value.value}
          </td>
          <td>
            <g:message code="${g.stateForComposition(episodeId:composition.id)}" />
             </td>
          <td>
            <g:link action="show" id="${composition.id}"><g:message code="trauma.list.action.show" /></g:link>
            <br />
              <g:if test="${(g.stateForComposition(episodeId:composition.id) == Version.STATE_SIGNED)}">
                <g:set var="version" value="${Version.findByData(composition)}"/>
                <g:set var="archivoCDA" value="${new File(ApplicationHolder.application.config.hce.rutaDirCDAs + '\\' + version.nombreArchCDA)}"/>
                <g:if test="${!archivoCDA.exists()}">
                  <g:link controller="cda" action="create" id="${composition.id}">Crear CDA</g:link>
                </g:if>
                <g:else>
                  <g:message code="Documento Clinico Creado" /> <!-- TODO i18n -->
                </g:else>
              </g:if>
          </td>
        </tr>
      </g:each>
    </table>
    
    <%-- creo que esto estaba de test
    <ul>
      <g:each in="${templateNames}" var="template">
        <li><g:link action="generarTemplate" params="[templateId:template]">${template}</g:link></li>
      </g:each>
    </ul>
    --%>
    
  </body>
</html>