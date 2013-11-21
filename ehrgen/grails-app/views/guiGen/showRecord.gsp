<%@ page import="archetype.ArchetypeManager" %><%@ page import="templates.TemplateManager" %>
<?xml version="1.0" encoding="ISO-8859-1" ?>
<html>
  <head>
    <meta name="layout" content="ehr" />
    <link rel="stylesheet" href="${createLinkTo(dir:'css', file:'generarTemplate.css')}" />
  </head>
  <body>
    <%--
    <h1>Template: ${rmNode.archetypeDetails.templateId}</h1>
    --%>
    <div class="ehrform">

      <table class="container" cellpadding="0" cellspacing="3">
        <tr>
          <td id="content">
            <g:each in="${composition.content}" var="content">
              
              <%-- ${content.archetypeDetails.archetypeId} --%>
              
              <g:set var="archetype"
                     value="${ArchetypeManager.getInstance().getArchetype( content.archetypeDetails.archetypeId )}" />
            
              <g:set var="template"
                     value="${TemplateManager.getInstance().getTemplate( content.archetypeDetails.templateId )}" />
              
              <g:render template="../guiGen/showTemplates/Locatable"
                        model="[rmNode: content, archetype: archetype, template: template]" />
            </g:each>
          </td>
        </tr>
      </table>
      <br/>
    
      <div class="bottom_actions">
        <g:link controller="records" action="show" id="${composition.id}"><g:message code="trauma.show.action.back" /></g:link>
      </div>

    </div>
  </body>
</html>