<%@ page import="demographic.role.Role" %><html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title><g:message code="workflow.create.title" /></title>
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
      <h1><g:message code="workflow.create.title" /></h1>
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>
      <div class="create">
	  
	     <%-- TODO: i18n --%>
	     <g:form url="[action:'createWorkflow', id:domain.id]">
	    
		    Para los roles:
          <br/>
          <g:select name="roleId"
            from="${roles}"
            optionKey="id" optionValue="type"
            multiple="multiple" size="${roles.size()}" />
        
          Mantenga presionado CTRL para seleccionar mas de un rol.
	       <br/><br/>
		
		    <input type="submit" name="doit" value="Crear workflow" />
	     </g:form>
		
        <input type="button" name="close" value="Cerrar" />
      
      </div>
    </div>
  </body>
</html>