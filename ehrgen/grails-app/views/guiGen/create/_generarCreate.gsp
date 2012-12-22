<%@ page import="org.codehaus.groovy.grails.commons.ApplicationHolder" %><%--

Igual a la vista generarTemplate pero solo para el form del registro.

in: template

--%>
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
                  <g:render template="/guiGen/create/${templateName}"
                            model="[(templateName): node,
                                    archetype: archRef.getReferencedArchetype(),
                                    archetypeService: archetypeService,
                                    params: params, lang: lang, locale: locale, template: template]" />
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
                  <g:render template="/guiGen/create/${templateName}"
                            model="[(templateName): node,
                                    archetype: archRef.getReferencedArchetype(),
                                    archetypeService: archetypeService,
                                    params: params, lang:lang, locale: locale, template: template]" />
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
                  <g:render template="/guiGen/create/${templateName}"
                            model="[(templateName): node,
                                    archetype: archRef.getReferencedArchetype(),
                                    archetypeService: archetypeService,
                                    params: params, lang: lang, locale: locale, template: template]" />
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
                <g:render template="/guiGen/create/${templateName}"
                          model="[(templateName): node,
                                  archetype: archRef.getReferencedArchetype(),
                                  archetypeService: archetypeService,
                                  params: params, lang: lang, locale: locale, template: template]" />
              </g:each>
            </g:each>
          </td>
        </tr>
      </table>