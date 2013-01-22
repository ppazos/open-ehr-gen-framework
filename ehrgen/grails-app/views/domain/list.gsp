<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main" />
    <title><g:message code="domain.list.title" /></title>
    <style>
    .domain {
      width: 120px;
      min-height: 110px;
      margin: 0px 0px 15px 25px;
      
      text-align: center;
      display: inline-block;
      position: relative;
      vertical-align: top; /* BASELINE CORRECCIÓN*/
      /*border: 1px solid black;*/
    }
    input[type=image], .domain img {
      border: 0px;
    }
    /* Luego pongo opacity en 1 para mostrar el dominio seleccionado! */
    input[type=image] {
      opacity: 0.6;
    }
    
    .edit_actions {
      display: none;
      margin-top: 5px;
    }
    .edit_actions a {
      background-color: #bbccff;
      padding: 2px;
    }
    #select_role, #create_admission {
      display: none;
    }
    
    h1 {
      margin-left: 20px;
    }
    
    div.body {
      display: table;
      position:relative;
      padding: 0px;
      height: 100%;
      width: 100%;
    }
    div.list {
      display: table-cell;
      padding: 20px;
    }
    /* muestra domains y admissions como 2 columnas que
       ocupan toda la pantalla para dar la idea de app */
    div.domains_wrapper {
      position: absolute;
      left: 0px;
      top: 0px;
      padding: 0px;
      width: 60%;
      height: 100%;
    }
    div.admissions_wrapper {
      border-left: 1px solid #ddd;
      position: absolute;
      top: 0px;
      right: 0px;
      padding: 0px;
      width: 40%;
      height: 100%;
      background-color: #fcfcfc;
    }
    div.admissions {
    }
    div.patient_data {
      background-color: #efefef;
      border-bottom: 1px solid #ddd;
      border-top: 1px solid #f9f9f9;
      padding: 0px;
    }
    div.patient_data a {
      padding: 10px;
      display: block;
    }
    div.patient_data a:hover {
      background-color: #f9f9f9;
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
       
       
       // --------------------------------------------------- RESALTO EL DOMINIO
       // Muestra resaltado el dominio al pasar el mouse
       // por arriba de un paciente en admision
       $('a.admission').mouseover( function(evt) {
       
          var domain_class = 
            $.grep( $(this).attr('class').split(' '), function(clss, i) { 
              return (clss.indexOf('domain_') == 0);
            } );
          
          var form = $('form.'+domain_class);
          
          // Resalto
          $('input[type=image]', form).css({opacity:'1.0'});
       });
       // Cuando saco el mouse, vuelve a opacity 0.6
       $('a.admission').mouseout( function(evt) {
       
          var domain_class = 
            $.grep( $(this).attr('class').split(' '), function(clss, i) { 
              return (clss.indexOf('domain_') == 0);
            } );
          
          var form = $('form.'+domain_class);
          
          // Apago
          $('input[type=image]', form).css({opacity:'0.6'});
       });
       
       // Idem al pasar el mouse por los dominios
       $('form.domain_access').mouseover( function(evt) {
       
          // Resalto
          $('input[type=image]', this).css({opacity:'1.0'});
       });
       // Cuando saco el mouse, vuelve a opacity 0.6
       $('form.domain_access').mouseout( function(evt) {

          // Apago
          $('input[type=image]', this).css({opacity:'0.6'});
       });
       // --------------------------------------------------- /RESALTO EL DOMINIO
       
       <%-- Si tiene mas de un rol, debe elegir con que rol desea ingresar al dominio --%>
       <g:if test="${roles.size() > 1}">
         $('.domain_access').submit(function(evt){
           evt.preventDefault();
           
           //console.log(this); // el form
           form = $(this); // Para que select[roleType] pueda saber en que form hice click
           
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
       $('select[name=roleType]').change(function(evt) {
       
         //console.log(this.value); // id del rol
         hiddenRoleType = $('<input type="hidden" name="roleType" value="'+ this.value +'" />');
         form.append(hiddenRoleType);
         
         $.unblockUI(); // Solo para hacer el efecto grafico de esconder la ventana modal
         
         form.unbind('submit'); // para no lanzar el submit y que abra la modal de nuevo
         form.submit(); // Selecciona el dominio
       });
       
       
       $('a.create_admission').click( function(evt) {
       
         evt.preventDefault();
       
         $.blockUI({
           message: $('#create_admission'),
           css: {
             width: '300px',
             height: '260px',
             //top: 'auto',
             //left:'auto',
             padding: '10px'
           },
           onOverlayClick: $.unblockUI
         });
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
      <span class="menuButton">
        <g:link action="admissionCreate" class="create create_admission"><g:message code="domain.list.action.admission" /></g:link>
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
        <g:select from="${roles}" var="role" name="roleType" optionKey="type" optionValue="type" noSelection="['':'']" />
      </g:if>
    </div>
    
    <%--
      Crea una admision para un paciente en un dominio y para un medico.
    --%>
    <div id="create_admission">
      <g:include action="admissionCreate" />
    </div>
    <g:if test="${flash.message}">
      <div class="message">${flash.message}</div>
    </g:if>
    <div class="body">
      <div class="list">
      
        <div class="domains_wrapper">
      
        <h1><g:message code="domain.list.title" /></h1>
        <g:each in="${domains}" status="i" var="domain">
          <g:hasDomainPermit domain="${domain}">
            <div class="domain">
              <g:form url="[action:'selectDomain', id:domain.id]" class="domain_access domain_${domain.id}" action="post">
                <%-- TODO: imagenes distintas configurables por dominio --%>
                <g:actionSubmitImage
                  value="${domain.name}"
                  action="selectDomain"
                  src="${createLinkTo(dir: 'images', file: 'folder.png')}" /><br/>
                ${domain.name}
                
                <%-- Si tiene un solo rol, se autoselecciona, si hay mas de un rol, se selecciona por javascript --%>
                <g:if test="${roles.size() == 1}">
                  <input type="hidden" name="roleType" value="${roles[0].type}" />
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
      
      <div class="list admissions_wrapper">
        <h1><g:message code="patients.admissions.title" /></h1>
        
        <g:include action="admissionList" />
      </div>
    </div>
  </body>
</html>