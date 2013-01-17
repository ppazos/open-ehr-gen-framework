<%@ page import="demographic.role.Role" %><html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title><g:message code="insttructionRoles.uploadArchetype.title" /></title>
    <style>

    </style>
    <g:javascript library="jquery-1.8.2.min" />
    <g:javascript>
    $(document).ready(function() {
      
      // Al hacer click en close, llama a close_modal() de views/domain/edit.gsp
      $('input[name=close]').click( function(evt) {
      
        window.parent.close_modal();
      });
    });
    </g:javascript>
  </head>
  <body>
    <div class="body">
      <h1><g:message code="insttructionRoles.uploadArchetype.title" /></h1>
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>
      <div class="create">
	  
	     <%-- TODO: i18n --%>
	     <g:form url="[action:'selectInstructionRoles', id:index.id]">
	    
		    Instrucciones para los roles:
          <br/>
          <g:select name="roleId"
            from="${roles}"
            optionKey="id" optionValue="type"
            multiple="multiple" size="${roles.size()}"
            value="${index.instructionRoles.id}" />
        
          Mantenga presionado CTRL para seleccionar mas de un rol.
	       <br/><br/>
		
		    <input type="submit" name="doit" value="Establecer roles" />
	     </g:form>
		
        <input type="button" name="close" value="Cerrar" />
      
      </div>
    </div>
  </body>
</html>