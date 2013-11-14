<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main" />
    <title><g:message code="domain.edit.title" /></title>
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
    .domain img {
      border: 0px;
    }
    .list {
      padding: 20px;
    }
    #create_wf {
      display: none;
    }
    iframe {
      border: 0;
      width: 100%;
      height: 100%;
    }
    li.stage_template {
      list-style: none;
      padding: 5px;
    }
    img {
      vertical-align: middle;
    }
    a.button {
      border: 1px solid #ddd;
      padding: 3px 7px;
      display: inline-block;
      margin: 5px 0px;
    }
    </style>
    <g:javascript library="jquery"/>
    <r:require module="blockUI" />
    <g:javascript>
    
    var modal;
    
    $(document).ready(function() {
    
      modal = $('#create_wf');
    
      /*
       * Abre modal para crear workflow
       */
      $('input[name=add_wf]').click( function(evt) {
      
        modal.children()[0].src = '${createLink(action:'createWorkflow', id:domain.id)}';
      
        // Tamanios del area visible
        // $(window).height() es el alto total de la pagina (no sirve para centrar)
        var viewportHeight = window.innerHeight ? window.innerHeight : $(window).height();
        var viewportWidth = window.innerWidth ? window.innerWidth : $(window).width();
      
        $.blockUI({
          message: modal,
          css: {
            width: '500px',
            height: '320px',
            left: (viewportWidth - 500) /2 + 'px', 
            top:  (viewportHeight - 300) /2 + 'px', 
            padding: '10px'
          },
          onOverlayClick: $.unblockUI,
          onUnblock: function() { window.location.reload(true); }
        });
      });
      
      /*
       * Abre modal para crear una etapa en un workflow
       */
      $('a.add_stage').click( function(evt) {
      
        evt.preventDefault();
      
        console.log(this.href);
        
        modal.children()[0].src = this.href;
      
        $.blockUI({
          message: modal,
          css: {
            width: '500px',
            height: '450px',
            top:  ($(window).height() - 400) /2 + 'px', 
            left: ($(window).width() - 500) /2 + 'px', 
            padding: '10px'
          },
          onOverlayClick: $.unblockUI,
          onUnblock: function() { window.location.reload(true); }
        });
      });
      
    });
    
    var close_modal = function() {
      //console.log('close_modal :)');
      $.unblockUI();
    };
    
    </g:javascript>
  </head>
  <body>
    <div id="create_wf">
      <iframe src=""></iframe>
    </div>
  
    <div class="nav">
	  <span class="menuButton">
        <g:link controller="domain" action="list" class="list"><g:message code="domain.list.title" /></g:link>
      </span>
    </div>
    
    <div class="body">
      <h1>
        <img src="${createLinkTo(dir: 'images', file: 'domain_icon_64.png')}" width="48" height="48"  />
        <g:message code="domain.edit.title" />
      </h1>
      
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>
      <div class="edit">
	  
	    <%-- TODO: i18n --%>
	    <g:form url="[action:'edit', id:domain.id]">
		
		  Nombre del dominio <input type="text" name="value" value="${domain.name}" />
		  <%--
		  <br/>
		  C&oacute;digo identificador del dominio <input type="text" name="codeString" value="${domain.name.definingCode.codeString}" />
		  --%>
		  <br/><br/>
        
        <h2>
          <img src="${createLinkTo(dir: 'images', file: 'workflow_icon_64.png')}" width="32" height="32" />
          Flujos de trabajo
        </h2>
        
        <input type="button" name="add_wf" value="Agregar flujo de trabajo" />
        <br/><br/>
        
        <%-- Es wf por conjunto de roles --%>
        <%-- TODO: cambiar el orden de las etapas con drag and drop --%>
        <g:if test="${domain.workflows.size() > 0}">
          <g:each in="${domain.workflows}" var="wf">
          
            <%-- TODO: i18n --%>
            <%-- forRoles son instancias, pueden haber 2 roles con el mismo tipo, por eso el unique --%>
            <h3>Flujo para los roles ${wf.forRoles.type.unique()}</h3>
            
            <!--
            <a href="javascript:alert('No implementado');">Editar flujo</a>
            -->
            <g:link action="createStage" params="[workflowId:wf.id]" class="add_stage button">Agregar etapa</g:link>
            <g:link action="removeWorkflow" params="[id:wf.id]" class="button">Remover flujo de trabajo</g:link>
            
            <table>
              <tr>
                <th>Nombre</th>
                <th>Definici&oacute;n de registros</th>
                <th>Acciones</th>
              </tr>
              <g:if test="${wf.stages.size() > 0}">
                <g:each in="${wf.stages}" var="stage">
                  <tr>
                    <td>
                      <img src="${createLinkTo(dir: 'images', file: 'stage_icon_64.png')}" width="16" height="16" />
                      ${stage.name}
                    </td>
                    <td>
                      <ol>
                        <g:each in="${stage.recordDefinitions}" var="template">
                          <li class="stage_template">
                            <img src="${createLinkTo(dir: 'images', file: 'template_icon_64.png')}" width="16" height="16" />
                            ${template.name}
                          </li>
                        </g:each>
                      </ol>
                    </td>
                    <td>
                      <g:link action="removeStage" params="[id:stage.id]" class="button">Remover etapa</g:link>
                    </td>
                  </tr>
                </g:each>
              </g:if>
              <g:else>
                <td colspan="3">
                  No se han definido las etapas del flujo de trabajo
                </td>
              </g:else>
            </table>
            <br/><br/>
            
          </g:each>
        </g:if>
        <g:else>
          No se han definido flujos de trabajo para el dominio.
        </g:else>
		  <input type="submit" name="doit" value="Guardar cambios" />
	    </g:form>
		
      </div>
    </div>
  </body>
</html>