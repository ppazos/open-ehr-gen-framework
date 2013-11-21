<%@ page import="demographic.role.Role" %><html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title><g:message code="satge.create.title" /></title>
    <style>

    </style>
    <g:javascript library="jquery"/>
    <g:javascript>
    $(document).ready(function() {
      
      // Al hacer click en close, llama a close_modal() de views/domain/edit.gsp
      $('input[name=close]').click( function(evt) {
      
        window.parent.close_modal();
      });
    });
    </g:javascript>
    <r:layoutResources/>
  </head>
  <body>
    <div class="body">
      <h1><g:message code="stage.create.title" /></h1>
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>
      <div class="create">
	  
	     <%-- TODO: i18n --%>
	     <g:form url="[action:'createStage', params:[workflowId: workflow.id]]">
	    
          Nombre de la etapa:
          <br/>
          <input type="text" name="name" />
          <br/><br/>
       
		    Definiciones del registro:
          <br/>
          <g:select name="templateId"
            from="${templates}"
            optionKey="id" optionValue="templateId"
            multiple="multiple" size="10" />
        
          Mantenga presionado CTRL para seleccionar mas de un template.
	       <br/><br/>
		
		    <input type="submit" name="doit" value="Crear stage" />
	     </g:form>
		
        <input type="button" name="close" value="Cerrar" />
      
      </div>
    </div>
  </body>
</html>