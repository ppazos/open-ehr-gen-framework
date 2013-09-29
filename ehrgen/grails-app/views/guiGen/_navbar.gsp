
<%-- Tabs: SUBMENU DE REGISTROS SI HAY MAS DE UN TEMPLATE EN LA STAGE ACTUAL
    Para cumplimiento de ordenes no hay workflow o stage actual.
    --%>
 <g:if test="${stage && stage.recordDefinitions.size()>1}">
   <div id="navbar">
     <ul>
       <g:each in="${stage.recordDefinitions}" var="stageTemplate">
         <li ${((stageTemplate.templateId==template.templateId)?'class="active"':'')}>
          <g:hasContentItemForTemplate episodeId="${session.ehrSession?.episodioId}" templateId="${stageTemplate.templateId}">
            <g:if test="${it.hasItem}">
              <g:link controller="guiGen" action="generarShow" id="${it.itemId}"><g:message code="${stageTemplate.name}" /> (*)</g:link>
            </g:if>
            <g:else>
              <g:hasDomainPermit domain="${domain}" templateId="${stageTemplate.templateId}">
                <g:link controller="guiGen" action="generarTemplate" params="[templateId:stageTemplate.templateId]">
                  <g:message code="${stageTemplate.name}" />
                </g:link>
              </g:hasDomainPermit>
              <g:dontHasDomainPermit>
                <a href="javascript:alert('${message(code:'auth.navbar.dontHavePermission')}');" class="unavailable"><g:message code="${stageTemplate.name}" /></a>
              </g:dontHasDomainPermit>
            </g:else>
          </g:hasContentItemForTemplate>
        </li>
       </g:each>
     </ul>
   </div>
 </g:if>