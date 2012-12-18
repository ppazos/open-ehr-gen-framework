
<h1><g:message code="trauma.create.title" /></h1>
  
<g:form action="create">
    
  <%-- se crea el episodio para una persona seleccionada por admision --%>
  <g:if test="${params.root && params.extension}">
    Se crea episodio para la persona con identificador: ${params.root}::${params.extension}<br/>
    <input type="hidden" name="root" value="${params.root}" />
    <input type="hidden" name="extension" value="${params.extension}" />
  </g:if>

  <g:message code="trauma.create.label.fechaIngreso" /><br/>   
  <g:datePicker name="startDate" value="${((params.startDate) ? new Date(params.startDate) : null)}" />
  <br/><br/>
   
  <g:message code="trauma.create.label.observaciones" /><br/>
  <textarea name="otherContext">${params.otherContext}</textarea>
  <br/><br/>
      
  <div id="bottom_actions">
    <g:submitButton name="doit" value="${message(code:'trauma.create.action')}" />
  </div>
</g:form>

<style>
textarea {
  height: 100px;
}
</style>