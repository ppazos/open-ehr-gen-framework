<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main" />
    <title><g:message code="domain.list.title" /></title>
    <style>
    .domain {
        width: 120px;
        min-height: 110px;
        margin-right: 20px;
        margin-bottom: 15px;
        text-align: center;
        display: inline-block;
        position: relative;
        vertical-align: top; /* BASELINE CORRECCIÓN*/
        /*border: 1px solid black;*/
    }
    input[type=image], .domain img {
      border: 0px;
    }
    .list {
      padding: 20px;
    }
    .edit_actions {
      display: none;
      margin-top: 5px;
    }
    .edit_actions a {
      background-color: #bbccff;
      padding: 2px;
    }
    #select_role {
      display: none;
    }
    </style>
    
    <g:javascript library="jquery-1.8.2.min" />
    <g:javascript src="jquery.blockUI.js" />
    <g:javascript>
    
    // Formulario del dominio donde hice click y tengo que seleccionar un rol.
    var form;
    
    $(document).ready(function() {
      
       $('#edit_domains').click(function(evt){
        
          evt.preventDefault();
          
          $('.edit_actions').toggle(500);
       });
       
       <%-- Si tiene mas de un rol, debe elegir con que rol desea ingresar al dominio --%>
       <g:if test="${roles.size() > 1}">
         $('.domain_access').submit(function(evt){
           evt.preventDefault();
           
           //console.log(this); // el form
           form = $(this); // Para que select[roleId] pueda saber en que form hice click
           
           $.blockUI({
             message: $('#select_role'),
             css: {
               width: '300px',
               height: '50px',
               //top: 'auto',
               //left:'auto',
               padding: '10px'
             },
             onOverlayClick: $.unblockUI
           });
         });
       </g:if>
       
       /*
        * Abre ventana modal para seleccionar rol si tiene mas de uno.
        */
       $('select[name=roleId]').change(function(evt) {
       
         //console.log(this.value); // id del rol
         hiddenRoleId = $('<input type="hidden" name="roleId" value="'+ this.value +'" />');
         form.append(hiddenRoleId);
         
         $.unblockUI(); // Solo para hacer el efecto grafico de esconder la ventana modal
         
         form.unbind('submit'); // para no lanzar el submit y que abra la modal de nuevo
         form.submit(); // Selecciona el dominio
       });
    });
    </g:javascript>
  </head>
  <body>
    <div class="nav">
      <%-- FIXME: acciones solo para administradores del sistema --%>
      <span class="menuButton">
        <g:link controller="person" action="list" class="list"><g:message code="domain.list.action.personas" /></g:link>
      </span>
      <span class="menuButton">
        <g:link controller="role" action="list" class="list"><g:message code="domain.list.action.roles" /></g:link>
      </span>
      |
      <span class="menuButton">
        <g:link controller="domain" action="create" class="create"><g:message code="domain.list.action.create" /></g:link>
      </span>
      <span class="menuButton">
        <a href="#" class="edit" id="edit_domains">Editar dominios</a>
      </span>
      |
      <span class="menuButton">
        <g:link controller="domain" action="createTemplate" class="create"><g:message code="domain.edit.action.createTemplate" /></g:link>
      </span>
      |
      <span class="menuButton">
        <g:link controller="archetypeManager" action="list" class="list"><g:message code="domain.list.action.archetypes" /></g:link>
      </span>
    </div>
    
    <%--
      Si tiene mas de un rol debe seleccionar uno antes de entrar al dominio.
      El rol determina el wf de templates que le voy a mostrar para el dominio.
    --%>
    <div id="select_role">
      <g:if test="${roles.size() > 1}">
        Elija un rol:
        <g:select from="${roles}" var="role" name="roleId" optionKey="id" optionValue="type" noSelection="['':'']" />
      </g:if>
    </div>
    
    <div class="body">
      <h1><g:message code="domain.list.title" /></h1>
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>
      <div class="list">
        <g:each in="${domains}" status="i" var="domain">
          <g:hasDomainPermit domain="${domain}">
            <div class="domain">
              <g:form url="[action:'selectDomain', id:domain.id]" class="domain_access" action="post">
                <%-- TODO: imagenes distintas configurables por dominio --%>
                <g:actionSubmitImage
                  value="${domain.name}"
                  action="selectDomain"
                  src="${createLinkTo(dir: 'images', file: 'folder.png')}" /><br/>
                ${domain.name}
                
                <%-- Si tiene un solo rol, se autoselecciona --%>
                <g:if test="${roles.size() == 1}">
                  <input type="hidden" name="roleId" value="${roles[0].id}" />
                </g:if>
                
              </g:form>
              
              <%-- solo edita los creados por el admin --%>
              <g:if test="${domain.userDefined}">
                <div class="edit_actions">
                  <g:link action="edit" id="${domain.id}">editar</g:link>
                </div>
              </g:if>
            </div>
          </g:hasDomainPermit>
          <g:dontHasDomainPermit></g:dontHasDomainPermit>
        </g:each>
      </div>
    </div>
  </body>
</html>