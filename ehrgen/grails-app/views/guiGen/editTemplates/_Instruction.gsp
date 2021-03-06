<%@ page import="templates.TemplateManager" %><%@ page import="com.thoughtworks.xstream.XStream" %><%@ page import="archetype.ArchetypeManager" %>
<%--

in: rmNode (Instruction)

--%>
<!-- Instruction -->
<g:if test="${!template}">
  <g:set var="template" value="${TemplateManager.getInstance().getTemplate( rmNode.archetypeDetails.templateId )}" />
</g:if>

<g:set var="archetype" value="${ArchetypeManager.getInstance().getArchetype( rmNode.archetypeDetails.archetypeId )}" />

<%-- http://code.google.com/p/open-ehr-gen-framework/issues/detail?id=19 --%>
<g:set var="generarGUI" value="${fieldPaths.find{rmNode.path.startsWith(it)} != null}" />
<g:if test="${generarGUI}">
  <div class="INSTRUCTION">
    <g:set var="aomNode" value="${archetype.node(rmNode.path)}" />
    <g:set var="archetypeTerm" value="${archetype.ontology.termDefinition(session.locale.language, aomNode.nodeID)}" />
    <span class="label">
      ${archetypeTerm?.text}:
    </span>
    <span class="content">
</g:if>

<g:each in="${rmNode.activities}" var="activity">
  <g:render template="../guiGen/editTemplates/Activity"
            model="[rmNode: activity, archetype: archetype, template: template]" />
</g:each>
<g:each in="${archetype.ontology.getTermBindingList()}" var="ontologyBinding">
  <%-- se sabe que '/' es aomNode.path() --%>
  <g:set var="termBindingItem" value="${ontologyBinding.getBindingList().find{it.code=='/narrative'}}" />
  <g:if test="${termBindingItem}">
    <span class="label">
      <%-- ${termBindingItem.terms[0].replace('::','-')} el origen es '[xxx::eee]' --%>
      <g:message code="${termBindingItem.terms[0].replace('::','-')}" />
    </span>
  </g:if>
</g:each>
<div class="INSTRUCTION_narrative">
  <% println g.editDvText( dataValue: rmNode.narrative,
	                      parent: rmNode,
	                      archetype: archetype,
	                      refPath: '',
	                      //aomNode: aomChildNode,
	                      pathFromOwner: '/narrative',
	                      template: template ) %>
<%--
  <g:render template="../guiGen/editTemplates/DvText"
            model="[dataValue: rmNode.narrative,
                    archetype: archetype,
                    template: template,
                    refPath: '',
                    pathFromOwner: '/narrative']" />
--%>

</div>

<g:if test="${generarGUI}">
    </span>
  </div>
</g:if>