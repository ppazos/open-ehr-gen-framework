<%@ page import="hce.core.composition.Composition" %><%@ page import="org.codehaus.groovy.grails.commons.ApplicationHolder" %>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
    <meta name="layout" content="main" />
    <title>Archetype based query</title>
    
    <link rel="stylesheet" href="${resource(dir:'css', file:'jquery-ui-1.9.2.datepicker.min.css')}" />
    <style>
    /* ============================>>> Achica imagen depickdate */
    img.ui-datepicker-trigger {
      height: 2em;
      vertical-align: top;
    }
    </style>
    
    <g:javascript library="jquery-1.8.2.min" />
    <g:javascript src="jquery.blockUI.js" />
    <g:javascript src="jquery-ui-1.9.2.datepicker.min.js" />
    
    <script type="text/javascript" src="${createLinkTo(dir:'js', file:'jquery.form.2_43.js')}"></script>
    <g:javascript library="jquery.scrollTo-1.4.2-min" />
    <g:javascript library="jquery.tableFilter-1.0.0" />
    
    <g:javascript>
      $(document).ready(function() {
      
        <g:if test="${archetype}">
        
        /* ===================================================================================== 
         * Calendar para filtro de la query
         */
        $("input[name=fromDate]").datepicker({
            // Icono para mostrar el calendar 
            showOn: "button",
            buttonImage: "${resource(dir:'images', file:'calendar.gif')}",
            buttonImageOnly: true,
            buttonText: 'pick a date',
            // Formato
            dateFormat: '${ApplicationHolder.application.config.app.l10n.date_format.replace("yyyy","yy").replace("MM","mm")}', // poner yy hace salir yyyy y MM hace salir el nombre del mes
            // Menus para cambiar mes y anio 
            changeMonth: true,
            changeYear: true,
            // La fecha maxima es la que esta seleccionada en toDate si la hay
            //onClose: function( selectedDate ) {
            //  $( "input[name=toDate]" ).datepicker( "option", "minDate", selectedDate );
           // }
        });
        
        </g:if>
        
      });
    </g:javascript>
    
    <script type="text/javascript">
       
    $(document).ready(function() {
      
      // Clic en checkbox name="chart_path", de los que hay varios, apaga el resto y prende solo el que se cliquea
      $('input[name=chart_path]').click( function(evt) {
      
        $('input[name=chart_path]').attr('checked', false);
        $(this).attr('checked', true);
      });
    
      // =======================================================================
      // Trae y muestra la agregacion de datos como una tabla de 2 columnas
      //
      // Submit por ajax para agregar datos seleccionados
      // http://jquery.malsup.com/form/#ajaxForm
      //
      $('#aggregate_frm').ajaxForm({
    
          // target element(s) to be updated with server response
          //target:  '#agg_output',
          
          // dataType identifies the expected content type of the server response 
          dataType:  'json', 
          
          beforeSubmit: function(formData, jqForm, options) {
    
            // TODO: verificar que se selecciono una path
            var queryString = $.param(formData); 
 
		    // jqForm is a jQuery object encapsulating the form element.  To access the 
		    // DOM element for the form do this: 
		    // var formElement = jqForm[0]; 
		 
		    //alert('About to submit: \n\n' + queryString); 
		 
		    // here we could return false to prevent the form from being submitted; 
		    // returning anything other than false will allow the form submit to continue 
		    return true; 
          }, 
          success: function(responseText, statusText, xhr, $form) {
    
            //console.log(responseText.names); // code -> nomnbre
            //console.log(responseText.aggregator); // code -> cantidad
    
            var agg_html = $('<table></table>');
    
            $.each( responseText.names, function(i,name) {
                
              //console.log(i+': '+name+'| '+responseText.aggregator[i]);
              count = responseText.aggregator[i]
              agg_html.append('<tr><th>'+name+'</th><td>'+count+'</td></tr>');
            });
            
            $('#agg_output').children().remove(); // Quita el contenido actual
            $('#agg_output').append(agg_html);    // Pone la tabla con cantidades
            
            // Scroll hasta la nueva tabla
            $.scrollTo($('#agg_output'), {duration: 800});
          }
      });
              
      
      // ================================================================================== 
      // Implementa accion de filtrar por el nombre del concepto cuando escribo en el
      // input archetype_filter, mostrando solo las trs que tienen nombres que coinciden
      // con lo que voy escribiendo.
      // 
      // Ojo que input es un evento nuevo de HTML5
      //
      $('input[name=archetype_filter]').tableFilter( $('#concepts'), 1 );
    });
    
    </script>
  </head>
  <body>
    <div class="nav">
      <span class="menuButton"><g:link controller="domain" action="list" class="list">Dominios</g:link></span>
      <span class="menuButton"><g:link action="query">Reiniciar</g:link></span>
    </div>
    <div class="body">
     
      Filtro por concepto: <input type="text" name="archetype_filter" id="archetype_filter" />
    
      <h1>Archetype list</h1>

      <!--
      TODO: poner filtros por fecha desde - hasta.<br/>
        
      TODO: poner filtro por paciente.<br/>
      -->
        
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>
        
      <div class="list">
        <table id="concepts">
          <thead>
            <tr>
              <th>Concepto</th>
              <th>Arquetipo</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            <g:each in="${clinicalConcepts}" status="i" var="entry">
              <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                <td>${entry.value}</td>
                <td>${entry.key}</td>
                <td>
                <g:link action="query" params="[archetypeId: entry.key, conceptName: entry.value]">[seleccionar concepto]</g:link>
                </td>
              </tr>
            </g:each>
          </tbody>
        </table>
          
        <g:if test="${archetype}">
            
          <g:form action="query">
      
            <%--
            <div id="calendarDiv1" style="position:absolute; width:205px; display:none; z-index:100;"></div>
            --%>
            desde: <input type="text" name="fromDate" id="fromDate" value="${params.fromDate}" />
            
            <input type="hidden" name="archetypeId" value="${params.archetypeId}"  />
            <input type="hidden" name="conceptName" value="${params.conceptName}"  />
            
            <table>
              <thead>
                <tr>
                  <th>Nombre</th>
                  <th>Tipo RM</th>
                  <th>Tipo AM</th>
                  <th>Path</th>
                  <th>Seleccionar</th>
                </tr>
              </thead>
              <tbody>
                <g:each in="${archetype.physicalPaths().sort{it}}" status="i" var="path">
                  <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                    <td>
                      <g:if test="${archetype.node(path).nodeID}">
                        ${binding.CtrlTerminologia.getInstance().getTermino(support.identification.TerminologyID.create('local', null), archetype.node(path).nodeID, archetype, session.locale)}
                      </g:if>
                      <g:else>
                        - <!-- Nodo interno CPrimitive no tiene nodeID en el arquetipo, por eso no tiene descripcion en la terminologia local del arquetipo -->
                      </g:else>
                    </td>
                    <td>${archetype.node(path).rmTypeName}</td>
                    <td>
                      
                      <g:set var="constraintClass" value="${archetype.node(path).getClass().getSimpleName()}" />
                      ${constraintClass}
                      
                      <!-- Solo para estas restricciones, si definen alternativas, puedo contar ocurrencias en los datos -->
                      <!-- TODO: cuando selecciono los valores uso la path del ELEMENT, pero para saber la lista de
                           valores contra la que clasifico, es la path mas larga hija de la path del element, porque
                           es al Element.DvCodedText.CodePhrase
                      -->
                      <g:if test="${'CCodePhrase' == constraintClass}">
                        ${archetype.node(path).codeList} <%-- [at0006, at0007] TODO: mostrar el texto que corresponde a cada codigo --%>
                      </g:if>
                      <g:if test="${'CDvOrdinal' == constraintClass}">
                        ${archetype.node(path).list} <%-- [[local] 1|at0003, [local] 2|at0004, [local] 3|at0005, [local] 4|at0006, [local] 5|at0007] TODO: mostrar el texto que corresponde a cada codigo --%>
                      </g:if>
                      
                    </td>
                    <td>${path}</td>
                    <td>
                      <!-- Ahora se selecciona la path con el checkbox y el form.
                      <g:link action="query" params="[path: path, archetypeId: params.archetypeId, conceptName: params.conceptName]">[seleccionar path]</g:link>
                      -->
                      <!-- Si es un nodo interno no puedo seleccionarlo -->
                      <%--<g:if test="${archetype.node(path).nodeID}">--%>
                      <g:if test="${archetype.node(path).rmTypeName == 'ELEMENT'}">
                        <%-- <g:checkBox name="path" value="${path}" checked="${(params.path instanceof String && params.path == path) || (params.path.getClass().isArray() && (params.path as List).contains(path))}" /> --%>
                        <g:checkBox name="paths" value="${path}" checked="${(paths?.contains(path))}" />
                      </g:if>
                    </td>
                  </tr>
                </g:each>
              </tbody>
            </table>
              
            <div class="buttons">
              <span class="button">
                <g:submitButton name="doit" value="Seleccionar paths" />
              </span>
            </div>
            
          </g:form>
            
          <!-- TODO: este deberia ir por ajax y traer los resultados -->
          <g:form url="[action:'aggregate']" id="aggregate_frm">
          
            <input type="hidden" name="archetypeId" value="${params.archetypeId}"  />
            <input type="hidden" name="conceptName" value="${params.conceptName}"  />
          
            <g:if test="${paths}">
                
              <%--
              <g:each in="${data}" var="rmobj">
                   <g:set var="template" value="${templates.TemplateManager.getInstance().getTemplate( rmobj.archetypeDetails.templateId )}" />
                   <g:set var="fieldPaths" value="${['/']}" />
                   <g:set var="templateName" value="${rmobj.getClassName()}" />
                   <g:render template="../guiGen/showTemplates/${templateName}"
                       model="[rmNode:rmobj, fieldPaths:fieldPaths, archetype:archetype, template:template]" />    
              </g:each>
              --%>
                
              <!-- Muestro resultados como tabla, en cada fila los registros de una misma composition -->
              <!--
                TODO: podria seleccionar un pathable y graficar:<br/>
                1. si no tengo filtro de paciente: cuantos valores iguales hay en esta consulta, mostrando tambien los valores que estan en el arquetipo correspondiente (p.e. si es un CodedText con valores definidos en el arquetipo)<br/>
                2. si tengo filtro de paciente: mostrar cada valor en cada fecha (para el paciente interesa la serie temporal, no la cantidad que en gral seria 0..1)<br/><br/>
                
                ** Para poder graficar la path deberia ser a un ELEMENT y tengo que navegar hacia las restricciones de su value, si la path es para
                ELEMENT.value no hay resultado en la busqueda, y si es para un CLUSTER o mayor en la jerarquia, los valores no son lo suficientemente
                granulares para poder graficarlos (entonces le tengo que pedir al usuario que seleccione valores simples para graficar).<br/>
              -->

              <table>
                <tr>
                  <th></th>
                  <th></th>
                  <!-- Pido la entry 0 y el valor de la entry son los rmobjects para la composition que es key de la entry -->
                  <%-- <g:each in="${0..(dataByComposition.entrySet() as List)[0]?.value?.size()-1}" var="i"> --%>
                  <g:each in="${0..paths.size()-1}" var="i"><%-- Mas facil: van a haber tantos elementos como paths --%>
                    <th title="${paths[i]}">
                      
                      <g:if test="${archetype.node(paths[i]).rmTypeName == 'ELEMENT'}">

                        <% // Quiero la path hija de paths[i] que es mas larga (es la ruta del nodo mas interno del arquetipo donde estan los valores que voy a usar para la clasificacion)
                        def path1 = archetype.physicalPaths().findAll{ pt -> pt.startsWith(paths[i]) }.max{ pt -> pt.length() }
                        //print path1
                        %>
						
						${archetype.node(path1).rmTypeName}
                        
                        <g:if test="${archetype.node(path1).rmTypeName == 'DvQuantity'}">
                          <%-- DvOrdinal: valores para clasificacion ${archetype.node(path1).list} --%>
                          <input type="checkbox" name="chart_path" value="${paths[i]+'::'+path1}" />
                        </g:if>
						<g:if test="${archetype.node(path1).rmTypeName == 'Integer'}">
                          <%-- Tipo basico dentro de DvCount --%>
                          <input type="checkbox" name="chart_path" value="${paths[i]+'::'+path1}" />
                        </g:if>
                        <g:if test="${archetype.node(path1).rmTypeName == 'DvOrdinal'}">
                          <%-- DvOrdinal: valores para clasificacion ${archetype.node(path1).list} --%>
                          <input type="checkbox" name="chart_path" value="${paths[i]+'::'+path1}" />
                        </g:if>
                        <g:if test="${archetype.node(path1).rmTypeName == 'CodePhrase'}">
                          <%-- CodePhrase: valores para clasificacion ${archetype.node(path1).codeList} --%>
                          <input type="checkbox" name="chart_path" value="${paths[i]+'::'+path1}" />
                        </g:if>
                        <g:if test="${archetype.node(path1).rmTypeName == 'DvBoolean'}">
                          <%-- DvBoolean: valores para clasificacion true, false o null --%>
                          <input type="checkbox" name="chart_path" value="${paths[i]+'::'+path1}" />
                        </g:if>
                        <g:if test="${archetype.node(path1).rmTypeName == 'DV_TEXT'}">
                          <%-- DvBoolean valores para clasificacion son datdos por el usuario --%>
                          <input type="checkbox" name="chart_path" value="${paths[i]+'::'+path1}" /><br/>
                          Valores separados por coma: <input type="text" name="aggKeys" />
                        </g:if>
                        
                      </g:if>
                      
                    </th>
                  </g:each>
                </tr>
                <g:each in="${dataByComposition}" var="entry">
                  
                  <tr>
                    <th>
                      ${entry.key}
                    </th>
                    <td>
                      <%--
                      <g:format date="${Composition.get(entry.key).context.startTime.toDate()}" />
                      --%>
                      <g:format date="${Composition.get(entry.key).startTime}" />
                    </td>
                    <g:set var="j" value="${0}" />
                    <g:each in="${entry.value}" var="rmobj">
                      
                      <%-- compara ${paths[j]} con ${rmobj.path}<br/> --%>
                      <%-- Si la path del obj no es la de la columna actual, el valor de la
                           composition para la path de la columna es null, salteo las columnas
                           null hasta llegar a la columna donde poner el valor no nulo.
                      --%>
                      <g:while test="${j < paths.size() && paths[j] != rmobj.path}">
                        <td>&nbsp;</td>
                        <g:set var="j" value="${j+1}" /><%-- saltea la columna --%>
                      </g:while>
                      <g:set var="j" value="${j+1}" /><%-- cuenta la columna actual --%>
                      
                    
                      <td title="${rmobj.archetypeDetails.archetypeId+rmobj.path}">
                        <g:set var="template" value="${templates.TemplateManager.getInstance().getTemplate( rmobj.archetypeDetails.templateId )}" />
                        <g:set var="fieldPaths" value="${['/']}" />
                        <g:set var="templateName" value="${rmobj.getClassName()}" />
                        <g:render template="../guiGen/showTemplates/${templateName}"
                                  model="[rmNode:rmobj, fieldPaths:fieldPaths, archetype:archetype, template:template]" />
                        
                        <g:if test="${archetype.node(rmobj.path).rmTypeName != 'ELEMENT'}">
                          ** La path seleccionada corresponde con un objeto complejo que no puede ser graficado,
                          para crear un grafico seleccione la path a un objeto simple "ELEMENT".                
                        </g:if>
                        <g:else>
                          <!--
                          TEST: nodo del arquetipo corresp. para obtener valores para graficar.<br/>
                          -->
                          <!-- Como el nodo archetype.node(rmobj.path) es ELEMENT, siempre tendra un solo atributo "value" -->
                        
                          <!-- FIXME: Lo que no se es si ese atributo va a tener uno o muchos children... si tiene uno puedo
                             graficar sin problemas, si tiene muchos, deberia ver en el template cual alternativa de
                             restriccion aplica a este nodo del RM y tomar esa. Lo que pasa es que ahora el template
                             no dice cual alternativa de restriccion aplica, y deberia.
                             POR AHORA SUPONGO QUE HAY UN SOLO CHILDREN! -->
                        
                          <!--
                          Si la restriccion es CDvOrdinal o CDvCodedText (u otra que tenga una lista de valores), puedo
                          graficar la cantidad de objetos que se encontro en cada path, para cada uno de esos valores
                          de la lista de restriccion.<br/>
                        
                          Ejemplo: evaluacion de triage es CDvOrdinal y tiene una lista de 5 valores posibles, y los objetos
                          del rm que se obtuvieron por la consulta semantica son: 1 para ESTABLE y 1 para INESTABLE, GRAVE,
                          para los demas valores de la restriccion pongo ceros.<br/>
                        
                          <textarea rows="10" cols="100">${archetype.node(rmobj.path).attributes[0].children[0]}</textarea>
                          -->
                          
                          <input type="hidden" name="itemId" value="${rmobj.id}" />
                          
                        </g:else>
                      </td>
                    </g:each>
                  </tr>
                </g:each>
              </table>
            </g:if><%-- if paths --%>
          
            <div class="buttons">
              <span class="button">
                <g:submitButton name="doit" value="Ver agregacion" />
              </span>
            </div>
            
          </g:form><%-- form para agregar informacion --%>
          
          <div id="agg_output">
                    
          </div>
          
        </g:if><%-- if archetype --%>
      </div>
    </div>
  </body>
</html>