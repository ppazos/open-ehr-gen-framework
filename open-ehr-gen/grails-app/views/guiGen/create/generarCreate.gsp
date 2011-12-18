<?xml version="1.0" encoding="UTF-8" ?>
<html>
  <head>
    <meta name="layout" content="ehr" />
    <link rel="stylesheet" href="${createLinkTo(dir:'css', file:'generarTemplate.css')}" />
    <g:javascript library="jquery-1.6.2.min" />
    <g:javascript library="jquery.scrollTo-1.4.2-min" />
    <g:javascript>
      $(document).ready( function() {
        
        // Agrego links para clonar nodos multiples
        $('.multiple').each( function(i, e) {
          
          //$(e).css('border', '3px solid violet');
    
          // FIXME: i18n
    
          // Agrega un link en el contenedor (padre) del nodo multiple.
          var link = $('<a href="#" class="cloner">agregar otro</a>');
          
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
      });
    </g:javascript>
  </head>
  <body>
    <%-- SUBMENU DE SECCIONES SI EXISTEn --%>
    <g:if test="${subsections.size()>1}">
      <div id="navbar">
        <ul>
          <g:each in="${subsections}" var="subsection">
            <li ${((params.templateId==subsection)?'class="active"':'')}>
	          <g:hasContentItemForTemplate episodeId="${episodeId}" templateId="${subsection}">
	            <g:if test="${it.hasItem}">
	              <g:link controller="guiGen" action="generarShow" id="${it.itemId}"><g:message code="${'section.'+subsection}" /> (*)</g:link>
	            </g:if>
	            <g:else>
		          <g:link controller="guiGen" action="generarTemplate" params="[templateId:subsection]">
		            <g:message code="${'section.'+subsection}" />
		          </g:link>
		        </g:else>
	          </g:hasContentItemForTemplate>
	        </li>
          </g:each>
        </ul>
      </div>
    </g:if>
    <g:if test="${flash.message}">
      <div class="message"><g:message code="${flash.message}" /></div>   
    </g:if>
    <%-- Form cacheado --%>
   <g:form url="[controller:'guiGen', action:'save']" class="ehrform" method="post" enctype="multipart/form-data">
     <input type="hidden" name="templateId" value="${params.templateId}" />
     ${form}
     <br/>
     <div class="bottom_actions">
       <g:submitButton name="doit" value="Guardar" />
     </div>
   </g:form>
  </body>
</html>