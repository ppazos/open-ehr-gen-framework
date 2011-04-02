<%@ page import="com.thoughtworks.xstream.XStream" %>
<html>
  <head>
    <meta name="layout" content="ehr" />
    <title>
      <g:message code="trauma.show.title" />
    </title>
    <style>
      #show_body {
        overflow: auto;
        width: 100%;
        padding: 0px;
      }
      #demographic {
        width: 46%;
        position: relative;
        /*display: inline;*/
        border: 1px solid #000;
        background-color: #ffffdd;
        float: left;
        padding: 10px;
        font-size: 14px;
      }
      #clinical {
        width: 46%;
        position: relative;
        /* display: inline; */
        border: 1px solid #000;
        background-color: #ffffdd;
        float: right;
        padding: 10px;
        font-size: 14px;
      }
    </style>
  </head>
  <body>
    <h1><g:message code="trauma.show.title" /></h1>
    <g:if test="${flash.message}">
      <div class="message"><g:message code="${flash.message}" /></div>
    </g:if>

  <%-- TEST:
  <textarea>
  <%
    composition.context.participations.each{ it ->
    
      println it.performer
    }
  %>
  </textarea>
  --%>
  
    <div id="show_body">
      <div id="demographic">
        <h2>REGISTRO DEMOGRAFICO</h2>
        <%-- permite asociar un paciente al episodio solo si el mismo no esta ya asignado --%>
        <g:if test="${patient}">
          <g:render template="../demographic/Person" model="[person:patient]" />
        </g:if>
        <g:else>
          <g:message code="trauma.layout.pacienteNoIdentificado.label" />:
          <g:link controller="demographic" action="admisionPaciente">
            <g:message code="trauma.layout.identificarPaciente.action" />
          </g:link>
        </g:else>
      </div>
      <g:canFillClinicalRecord>
        <div id="clinical">
          <h2>REGISTRO CLINICO</h2>
          <g:link action="registroClinico">
            <g:message code="trauma.title.registroClinico" />
          </g:link>
        </div>
      </g:canFillClinicalRecord>
    </div>
    <br/>
      
    <div class="bottom_actions">
      <%-- Ahora cierre y firma es uno solo: http://code.google.com/p/open-ehr-gen-framework/issues/detail?id=9
      <g:canSignRecord episodeId="${episodeId}">
        <g:link controller="records" action="signRecord" id="${composition.id}"><g:message code="trauma.show.action.sign" /></g:link>
      </g:canSignRecord>
      --%>
      <g:isSignedRecord episodeId="${episodeId}"><g:message code="trauma.sign.registryAlreadySigned" /></g:isSignedRecord>
      <g:reabrirEpisodio  episodeId="${episodeId}">
        <g:link controller="records" action="reopenRecord" id="${composition.id}"><g:message code="trauma.show.action.reopenRecord" /></g:link>
      </g:reabrirEpisodio>
      <g:link controller="guiGen" action="showRecord"><g:message code="trauma.list.action.showRecord" /></g:link>

    </div>
    
  </body>
</html>