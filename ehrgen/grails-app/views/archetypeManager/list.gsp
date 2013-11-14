<%=packageName%>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
    <meta name="layout" content="main" />
    <title>Archetype list</title>
    <style>
    #add_archetype {
      display: none;
    }
    iframe {
      border: 0;
      width: 100%;
      height: 100%;
    }
    </style>
    <g:javascript library="jquery"/>
    <r:require module="blockUI" />
    <g:javascript>
    
      var modal;
    
      $(document).ready(function() {
      
        modal = $('#add_archetype');
      
        // TODO: cuando se lanza crear un registro,
        //       abrir create.gsp en la modal de blockUI
        $('a.add_archetype').click( function(evt) {
          
          evt.preventDefault();
          
          modal.children()[0].src = this.href;
          
          // Tamanios del area visible
          // $(window).height() es el alto total de la pagina (no sirve para centrar)
          var viewportHeight = window.innerHeight ? window.innerHeight : $(window).height();
          var viewportWidth = window.innerWidth ? window.innerWidth : $(window).width();
          
          $.blockUI({
             message: modal,
             css: {
               width: '500px',
               height: '220px',
               left: (viewportWidth - 500) /2 + 'px', 
               top:  (viewportHeight - 220) /2 + 'px', 
               padding: '10px',
               textAlign: 'left'
             },
             onOverlayClick: $.unblockUI,
             onUnblock: function () {  modal.children()[0].src = ''; }
           });
        });
      });
    
    </g:javascript>
  </head>
  <body>
    <div class="nav">
      <span class="menuButton"><g:link controller="domain" action="list" class="list">Dominios</g:link></span>
      <span class="menuButton"><g:link action="loadAll">Load all</g:link></span>
      <span class="menuButton"><g:link action="unloadAll">Unload all</g:link></span>
      <span class="menuButton"><g:link action="query">Query</g:link></span>
      <span class="menuButton"><g:link action="uploadArchetype" class="create add_archetype">Add archetype</g:link></span>
    </div>
    
    <!-- modal blockUI -->
    <div id="add_archetype">
      <iframe src=""></iframe>
    </div>
    
    <div class="body">
      <h1>Archetype list</h1>
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>
      <div class="list">
        <table>
          <thead>
          <tr>
            <th>ARCHETYPE</th>
            <th>UTILIZADO</th>
            <th>ACCIONES</th>
          </tr>
          </thead>
          <tbody>
          <g:each in="${archetypeMap.keySet()}" status="i" var="archetypeId">
          <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
            <td>${archetypeId}</td>
            <td>${lastUseList[archetypeId]}</td>
            <td>
            <!-- 
            <g:link class="delete" action="unload" id="${archetypeId}">[bajar]</g:link>
            -->
            </td>
          </tr>
          </g:each>
          </tbody>
        </table>
      </div>
    </div>
  </body>
</html>
