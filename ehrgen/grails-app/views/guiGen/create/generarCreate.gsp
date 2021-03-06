<%@ page import="domain.Domain" %><?xml version="1.0" encoding="UTF-8" ?>
<html>
  <head>
    <meta name="layout" content="ehr" />
    <link rel="stylesheet" href="${createLinkTo(dir:'css', file:'generarTemplate.css')}" />
    <g:javascript library="jquery"/>
    <r:require module="scrollTo" />
    <r:require module="terminologyServices" />
    <g:javascript>
      $(document).ready( function() {
        
        // Disable submit button on submit
        // http://code.google.com/p/open-ehr-gen-framework/issues/detail?id=47
        $('.ehrform').submit( function() {

          // FIXME: si por algo falla la conexion con el servidor, se deberia poder enviar de nuevo sin tener que ingresar todos los datos!
          // esto se soluciona facil si el envio es por ajax: http://code.google.com/p/open-ehr-gen-framework/issues/detail?id=60
          $('input[type=submit]', this).attr('disabled', 'disabled');
        });
    
    
        // Agrego links para clonar nodos multiples
        $('.multiple').each( function(i, e) {
    
          // Para controles de find_in_terminology, si es multiple, no debe agregar el link de clonar.
          if ($('.find_in_terminology', this).length > 0) return;
    
          // Agrega un link en el contenedor (padre) del nodo multiple.
          var link = $('<a href="#" class="cloner">${g.message(code:'guigen.action.addMultiplenode')}</a>');
          
          // Inserta luego del nodo que hay que clonar, para saber que el prev() es el nodo a clonar.
          $(e).after(link);
        });
        
        $('.cloner').click( function (evt) {
          
          var link = $(evt.target);
          var toClone = link.prev();
          
          //toClone.css("border", "3px solid #f99");
          //alert(toClone.name);
          //var container = link.parent();
          
          var cloned = toClone.clone(); // Clona el elemento
          
          link.before(cloned); // Agrega el nodo clonado antes del link
          
          $.scrollTo(cloned, {duration: 800}); // Scroll al nuevo elemento, con scroll animado con duracion de 800ms.
          
          evt.preventDefault();
        });
        
        // ===================================================================
        // Nuevo: soporte para varios eventos dentro de OBSERVATION.HISTORY
        //  http://code.google.com/p/open-ehr-gen-framework/issues/detail?id=53
        //  http://code.google.com/p/open-ehr-gen-framework/issues/detail?id=74
        //
        var events = [];
        $('div.EVENT').each( function(i, e) { events.push(e) } );
        $('div.POINT_EVENT').each( function(i, e) { events.push(e) } );
        $('div.INTERVAL_EVENT').each( function(i, e) { events.push(e) } );
        
        console.log( 'events', events );
        
        // reverse porque agrega los elementos al reves en el prepend
        $(events.reverse()).each( function(i, e) {
        
          // Etiqueta de cada evento
          //console.log( $(e).children('span.label') );
          
          
          label = $(e).children('span.label').text();
          event_selector = $('<a href="#" class="event active">'+ label +'</a>');
          
          // Los events que no son el primero se van a mostrar usando el event selector
          // La condicion deja visible solo el ultimo porque events esta al reves
          if (i < events.length-1)
          {
            $(e).hide();
            event_selector.removeClass('active'); // solo el primero activo
          }
          
          // Asocia handler del click para seleccionar el registro del event
          event_selector.click( function (evt) {
            
            evt.preventDefault();

            $(events).hide();
            
            /*
             * FIX: hay un problema si el evento es multiple, el boton
             *      de clone (agregar otro) esta por fuera de la div
             *      del evento y queda visible al ocultar el evento.
             */
            $(events).next('a.cloner').hide();
            
            
            $(e).show(500); // Muestra el evento seleccionado
            $(e).next('a.cloner').show(); // Muestra el boton de clone del evento si hay
            
            
            // Solo activo el link a event donde se hizo click
            $('a.event').removeClass('active');
            $(this).addClass('active');
          });
          
          // Agrega un boton arriba del form de registro
          if (i > 0) $('.ehrform').prepend( '<span> | </span>' ); // Separador de links
          
          $('.ehrform').prepend( event_selector );
          
        }); // events
        
      });
    </g:javascript>
  </head>
  <body>
    <g:render template="navbar" model="[domain:domain, stage:stage, template:template]" />
    
    <g:if test="${flash.message}">
      <div class="message"><g:message code="${flash.message}" /></div>   
    </g:if>
    
    <%-- Form cacheado --%>
    <g:form url="[controller:'guiGen', action:'save']" class="ehrform" method="post" enctype="multipart/form-data">
      <input type="hidden" name="templateId" value="${template.templateId}" />
     
      <%-- Si esta presente, el registro es de cumplimiento de una orden --%>
      <input type="hidden" name="instructionExecId" value="${params.instructionExecId}" />
     
      ${form}<br/>
      <div class="bottom_actions">
        <g:submitButton name="doit" value="Guardar" />
      </div>
    </g:form>
  </body>
</html>