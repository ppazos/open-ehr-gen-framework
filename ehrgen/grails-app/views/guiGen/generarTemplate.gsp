<?xml version="1.0" encoding="ISO-8859-1" ?>
NO SE USA MAS
<html>
  <head>
    <meta name="layout" content="ehr" />
    <link rel="stylesheet" href="${createLinkTo(dir:'css', file:'generarTemplate.css')}" />
  </head>
  <body>
    <%-- SUBMENU DE SECCIONES SI EXISTEn --%>
    <g:if test="${subsections.size()>1}">
      <div id="navbar">
        <ul>
          <g:each in="${subsections}" var="subsection">
            <li ${((params.templateId==subsection)?'class="active"':'')}>
	          <g:hasContentItemForTemplate episodeId="${session.ehrSession?.episodioId}" templateId="${subsection}">
	            <g:if test="${it.hasItem}">-${subsection}-
	              <g:link controller="guiGen" action="generarShow" id="${it.itemId}"><g:message code="${'section.'+subsection}" /> (*)</g:link>
	            </g:if>
	            <g:else>
	              
	              <g:hasDomainPermit domain="${domain}" templateId="${subsection}">
		             <g:link controller="guiGen" action="generarTemplate" params="[templateId:subsection]">-${subsection}-
		               <g:message code="${'section.'+subsection}" />
		             </g:link>
		           </g:hasDomainPermit>
		           <g:dontHasDomainPermit>
		             <g:message code="${'section.'+subsection}" />
		           </g:dontHasDomainPermit>
		           
		         </g:else>
	          </g:hasContentItemForTemplate>
	        </li>
          </g:each>
        </ul>
      </div>
    </g:if>
    
    <g:form action="save" class="ehrform" method="post" enctype="multipart/form-data">
    
      <input type="hidden" name="templateId" value="${template.templateId}" />
	  <!-- TODO: sacar, es solo para test -->
      <%-- TemplateId: ${template.id}<br/> --%>
      <table class="container" cellpadding="0" cellspacing="3">
        <tr>
          <td colspan="2" id="content">
            <g:each in="${template.getArchetypesByZone('content')}" var="archRef">
              <g:each in="${archRef.getReferencedConstraints()}" var="node">
                <g:if test="${node}">
                  <g:set var="strclass" value='${node.getClass().getSimpleName()}'/>
                  <g:set var="templateName" value="${strclass[0].toLowerCase()+strclass.substring(1)}" />
                  <g:render template="templates2/${templateName}"
                            model="[(templateName): node,
                                    archetype: archRef.getReferencedArchetype(),
                                    archetypeService: archetypeService,
                                    params: params]" />
                </g:if>
                <g:else>
                  Dice que el node es nulo<br/>
                  ArchRef: ${archRef.id}<br/>
                </g:else>
              </g:each>
            </g:each>
          </td>
        </tr>
        <tr>
          <td id="left">
            <g:each in="${template.getArchetypesByZone('left')}" var="archRef">
              <g:each in="${archRef.getReferencedConstraints()}" var="node">
                <g:if test="${node}">
                  <g:set var="strclass" value='${node.getClass().getSimpleName()}'/>
                  <g:set var="templateName" value="${strclass[0].toLowerCase()+strclass.substring(1)}" />
                  <g:render template="templates2/${templateName}"
                            model="[(templateName): node,
                                    archetype: archRef.getReferencedArchetype(),
                                    archetypeService: archetypeService,
                                    params: params]" />
                </g:if>
                <g:else>
                  Dice que el node es nulo<br/>
                  ArchRef: ${archRef.id}<br/>
                </g:else>
              </g:each>
            </g:each>
          </td>
          <td id="right">
            <g:each in="${template.getArchetypesByZone('right')}" var="archRef">
              <g:each in="${archRef.getReferencedConstraints()}" var="node">
                <g:if test="${node}">
                  <g:set var="strclass" value='${node.getClass().getSimpleName()}'/>
                  <g:set var="templateName" value="${strclass[0].toLowerCase()+strclass.substring(1)}" />
                  <g:render template="templates2/${templateName}"
                            model="[(templateName): node,
                                    archetype: archRef.getReferencedArchetype(),
                                    archetypeService: archetypeService,
                                    params: params]" />
                </g:if>
                <g:else>
                  Dice que el node es nulo<br/>
                  ArchRef: ${archRef.id}<br/>
                </g:else>
              </g:each>
            </g:each>
          </td>
        </tr>
        <tr>
          <td colspan="2" id="bottom">
            <g:each in="${template.getArchetypesByZone('bottom')}" var="archRef">
              <g:each in="${archRef.getReferencedConstraints()}" var="node">
                <g:set var="strclass" value='${node.getClass().getSimpleName()}'/>
                <g:set var="templateName" value="${strclass[0].toLowerCase()+strclass.substring(1)}" />
                <g:render template="templates2/${templateName}"
                          model="[(templateName): node,
                                  archetype: archRef.getReferencedArchetype(),
                                  archetypeService: archetypeService,
                                  params: params]" />
              </g:each>
            </g:each>
          </td>
        </tr>
      </table>
      <br/>
      
      <div class="bottom_actions">
        <g:submitButton name="doit" value="Guardar" />
      </div>
    </g:form>
  </body>
</html>