<%@ page import="domain.Domain" %><?xml version="1.0" encoding="UTF-8" ?>
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
      #clinical ul {
        padding-left: 15px;
      }
      .unavailable {
        color:#aaa;
      }
    </style>
  </head>
  <body>
    <h1><g:message code="trauma.show.title" /></h1>
    <g:if test="${flash.message}">
      <div class="message"><g:message code="${flash.message}" /></div>
    </g:if>
  
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
          
	       <g:each in="${workflow.stages}" var="stage">
	         <ul>
              <li>
                <g:if test="${stage.recordDefinitions.size()==1}">
                
                  <%-- ${stage.recordDefinitions[0].templateId} --%>
                   <g:hasContentItemForTemplate episodeId="${session.ehrSession?.episodioId}" templateId="${stage.recordDefinitions[0].templateId}">
                     <g:if test="${it.hasItem}">
                       <%-- No va a edit, debe ir a show y si quiere en show hace edit. --%>
                       <g:link controller="guiGen" action="generarShow" id="${it.itemId}"><g:message code="${stage.name}" /></g:link>
                       (*)
                     </g:if>
                     <g:else>
                       <%-- Si el regsitro no esta incompleto, no se puede editar --%>
                       <g:isIncompleteRecord episodeId="${session.ehrSession?.episodioId}">
                        <g:if test="${it.answer}">

                          <g:hasDomainPermit domain="${Domain.get(session.ehrSession.domainId)}" templateIds="${stage.recordDefinitions.templateId}">
                            <g:link controller="records" action="registroClinico2" params="[section:stage.name]"><g:message code="${stage.name}" /></g:link>
                          </g:hasDomainPermit>
                          <g:dontHasDomainPermit>
                            <div onclick="javascript:alert('No tiene permisos para ingresar a esta seccion');" class="unavailable"><g:message code="${stage.name}" /></div>
                          </g:dontHasDomainPermit>
                          
                        </g:if>
                        <g:else>
                          <g:message code="${stage.name}" />
                        </g:else>
                       </g:isIncompleteRecord>
                     </g:else>
                   </g:hasContentItemForTemplate>
                
                </g:if>
                <g:else>
                   <g:message code="${stage.name}" />:
                   <ul>
                     <g:each in="${stage.recordDefinitions}" var="template">
                        <li>
                          <g:hasContentItemForTemplate episodeId="${session.ehrSession?.episodioId}" templateId="${template.templateId}">
                            <%--${it}--%>
                            <g:if test="${it.hasItem}">
                              <%-- Si el regsitro no esta incompleto, no se puede editar --%>
                              <g:link controller="guiGen" action="generarShow" id="${it.itemId}"><g:message code="${template.name}" /></g:link>
                              (*)
                            </g:if>
                            <g:else>
                              <%-- Si el regsitro no esta incompleto, no se puede editar --%>
                              <g:isIncompleteRecord episodeId="${session.ehrSession?.episodioId}">
                                <g:if test="${it.answer}">
                                
                                  <%--
                                  <g:link controller="guiGen" action="generarTemplate" params="[templateId: template.templateId]"><g:message code="${template.name}" /></g:link>
                                  --%>
                                                
                                  <g:hasDomainPermit domain="${Domain.get(session.ehrSession.domainId)}" templateId="${template.templateId}">
                                    <g:link controller="guiGen" action="generarTemplate" params="[templateId: template.templateId]"><g:message code="${template.name}" /></g:link>
                                  </g:hasDomainPermit>
                                  <g:dontHasDomainPermit>
                                    <div onclick="javascript:alert('No tiene permisos para ingresar a esta seccion');" class="unavailable"><g:message code="${template.name}" /></div>
                                  </g:dontHasDomainPermit>
                                
                                </g:if>
                                <g:else>
                                  <g:message code="${template.name}" />
                                </g:else>
                              </g:isIncompleteRecord>
                            </g:else>
                          </g:hasContentItemForTemplate>
                        </li>
                     </g:each>
                   </ul>
                </g:else>
	           </li>
            </ul>
	       </g:each>
        </div>
      </g:canFillClinicalRecord>
    </div>
    <br/>
      
    <div class="bottom_actions">
      <%-- Ahora cierre y firma es uno solo: http://code.google.com/p/open-ehr-gen-framework/issues/detail?id=9
      <g:canSignRecord episodeId="${session.ehrSession?.episodioId}">
        <g:link controller="records" action="signRecord" id="${composition.id}"><g:message code="trauma.show.action.sign" /></g:link>
      </g:canSignRecord>
      --%>
      <g:isSignedRecord episodeId="${session.ehrSession?.episodioId}"><g:message code="trauma.sign.registryAlreadySigned" /></g:isSignedRecord>
      <g:reabrirEpisodio episodeId="${session.ehrSession?.episodioId}">
        <g:link controller="records" action="reopenRecord" id="${composition.id}"><g:message code="trauma.show.action.reopenRecord" /></g:link>
      </g:reabrirEpisodio>
      <g:link controller="guiGen" action="showRecord"><g:message code="trauma.list.action.showRecord" /></g:link>
    </div>
  </body>
</html>