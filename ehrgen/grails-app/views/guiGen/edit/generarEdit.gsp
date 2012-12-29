<?xml version="1.0" encoding="UTF-8" ?>
<html>
  <head>
    <meta name="layout" content="ehr" />
    <link rel="stylesheet" href="${createLinkTo(dir:'css', file:'generarTemplate.css')}" />
    <g:javascript library="jquery-1.8.2.min" />
    <g:javascript library="jquery.scrollTo-1.4.2-min" />
    <g:javascript>
    
      // ======================================================
      // MISMO JS QUE EN SHOW!
      var data = ${data};
      var errors = ${errors};
      var errors2 = ${errors2};
      var booleans = {'label.boolean.false':'No', 'label.boolean.true':'Si'}; // TODO: Si y No debe ser i18n
      var templateId = '${template.templateId}';
      //
      // ======================================================
    
      $(document).ready( function() {
        
        // Disable submit button on submit
        // http://code.google.com/p/open-ehr-gen-framework/issues/detail?id=47
        $('.ehrform').submit( function() {
    
          // FIXME: si por algo falla la conexion con el servidor, se deberia poder enviar de nuevo sin tener que ingresar todos los datos!
          // esto se soluciona facil si el envio es por ajax: http://code.google.com/p/open-ehr-gen-framework/issues/detail?id=60
          $('input[type=submit]', this).attr('disabled', 'disabled');
        });
    
    
        // ====================================================
        // MISMO JS QUE EN CREATE!
    
        // Agrego links para clonar nodos multiples
        $('.multiple').each( function(i, e) {
          
          // FIXME: i18n
    
          // Agrega un link en el contenedor (padre) del nodo multiple.
          var link = $('<a href="#" class="cloner">${g.message(code:'guigen.action.addMultiplenode')}</a>');
          
          // Inserta luego del nodo que hay que clonar, para saber que el prev() es el nodo a clonar.
          $(e).after(link);
        });
        
        $('.cloner').click( function (evt) {
          
          var link = $(evt.target);
          var toClone = link.prev();
          //toClone.css("border", "3px solid #f99");
          
          var cloned = toClone.clone(); // Clona el elemento
          
          link.before(cloned); // Agrega el nodo clonado antes del link
          
          $.scrollTo(cloned, {duration: 800}); // Scroll al nuevo elemento, con scroll animado con duracion de 800ms.
          
          evt.preventDefault();
        });
        // ===================================================
        
        // ===================================================
        // MISMO JS QUE EN SHOW!
        
        console.group('Render data');
          
	    for (path in data)
	    {
	        if (path.match("^field")=="field") // if (path.startsWith('field'))
	        {
	          // busca elementos por su class (como es por class devuelve coleccion)
              // es solo uno por como genero el show.
	          //var elems = $("label."+path);
          
              // La entrada puede ser inpunt, select o textarea
              /*
              var elems = $("input[name="+path+"]");
              if (elems.length==0) elems = $("select[name="+path+"]");
              if (elems.length==0) elems = $("textarea[name="+path+"]");
              */
          
              // FIXME: tambien puede ser radio button
              var elems = $(":input[name="+path+"]");
          
              console.info(elems);
    
          
              // TODO: si el tipo es DvQuantity y tenia una sola unidad en el arquetipo, pasa que no se ingresa como dato, entonces no viene en los field-values y no la puedo mostrar junto con la magnitud.
              // TODO: resolver valores booleanos a Si/No o Yes/No, etc, segun el idioma elegido en la config.
              // FIXED: si el valor para path es date.struct, los valores vienen en las keys: path_year, path_month, path_day, path_hour, path_minute.
              
              if ($.isArray( data[path] ))
              {
                console.log('valores multiples');
          
                //alert(path+ ' tiene muchos valores: ' + data[path] );
                // El elem seleccionado deberia tener su contenedor con class multiple
                // En realidad no es su contenedor, sino el primer padre con class "multiple"
                //
    
                // El nodo a clonar siempre es el contenedor multiple del elem
                var nodeToClone = $(elems[0]).closest('.multiple');
                //nodeToClone.css({'border':'2px solid #f00'}); // OK!
    
                // si i es 0, pongo el valor en el nodo, si es mayor que 0 tengo que clonar el nodo para poner el valor en el nodo clonado.
                var lastSibling = nodeToClone; // el ultimo sibling de nodeToClone
    
                //console.info(elems[0]);
                //console.info(elems.get(0)); // igual que el anterior
                //console.info($(elems[0])); // esto me tira un array!, pero funca ok
                //console.info($(elems[0])[0]); // igual que el primero
                
                // El array puede no tener valores
                if ( data[path].length == 0 ) continue;
          
                show($(elems[0]), data[path][0]);


                // pone el valor para el resto de los nodos
                for (i=1; i<data[path].length; i++)
                {
                    // FIXME: si es un tipo estructurado (mas de un valor para el nodo), el nodo puede ya estar clonado, solo tengo que meterle el valor. Para saberlo, tengo que ver si el numero de elems es 1 o no, si es 1, el nodo no esta clonado y lo tengo que clonar.

                    //alert('elems.size: ' + elems.size() + ', data.path.length: ' + data[path].length);
                    
                    // Si la cantidad de labels donde van los datos es menor que la cantidad de datos,
                    // se que faltan nodos para clonar.
                    if (elems.length < data[path].length)
                    {
                      var clonedContainer = nodeToClone.clone(); // Unico caso que clono: cuando los nodos que hay son menos que la cantidad de valores que tengo para mostrar
                      lastSibling.after( clonedContainer ); // Pone en el dom, luego de lastSibling
                      lastSibling = clonedContainer; // Ahora es el nuevo lastSibling
                    
                      // Como agrego un container, tengo un nuevo elem (label).
                      // Es el que tiene class path dentro del clonedContainer.
                      // Forma de seleccionar la label.path dentro del clonedContainer.
                      elems.push( $(':input.[name='+path+']', clonedContainer)[0] ); // dentro del cloned container deberia haber un solo elemento con ese name
                    }
                
                    //console.log('pone valor (%d): %s en el control: ', i, data[path][i]);
                    //console.log(elems[i]);
                
                    show($(elems[i]), data[path][i]);
                }
                continue;
              }

              // elemento y valor simple
              show(elems, data[path]);
	        }
	    }
                
        console.groupEnd();
                
        //
        // ===================================================
        
        console.group('Render errors');
               
        // Mostrar errores de edit
        for (var field in errors)
	    {
           console.log(field); // field_37
           console.log(errors[field]); // object json (ahora es un array, p.e. si el campo es multiple y mas de un campo tiene error)
           
           // field es un identificador (aunque se pone como class), de la div
           // contenedora del field para el que se detecta error.
           // El nodo error lo tengo que colgar como primer hijo del container.
           // Para los nodos multiples viene field_0, field_1, ..., field_n
           
           // Tengo que asegurar que el show de los campos multiples le ponga
           // como class del container el indice correcto al field (0, 1, .. n).
           // Por defecto, el unico contenedor generado tiene class field_0.
           
           /*
           errors[field] es algo asi:
           
           {
		     "errors": [
               {
		         "object":"hce.core.datastructure.itemstructure.representation.Element",
		         "field":"value",
		         "rejected-value":{
		           "class":"data_types.basic.DvBoolean",
		           "className":"DvBoolean",
		           "value":null
		         },
		         "message":"No hay suficientes datos ingresados, el registro de toda la informacion es obligatorio"
		       }
		     ]
		   }
           {
			    "field_634":{
			        "0":{
			            "errors":[{
			                    "object":"DvQuantity",
			                    "field":"magnitude",
			                    "rejected-value":null,
			                    "message":"Debe ingresar magnitud y unidades"
			                }
			            ]
			        },
			        "1":{
			            "errors":[{
			                    "object":"DvQuantity",
			                    "field":"magnitude",
			                    "rejected-value":null,
			                    "message":"Debe ingresar magnitud y unidades"
			                }
			            ]
			        }
			    }
			}
           */
           
           
           for (var errIdx in errors[field])
	       {
               console.log('errIdx %s', errIdx); 
               //console.log(errors[field][errIdx]); // Array de errores para un nodo particular
               
               // TODO: verificar que existe el container con class field (que va a ser field_0, field_1, ...)
               var mensajesError = '';
                
	           //for (var err in errors[field].errors) // Varios errores para el mismo nodo
               for (var err in errors[field][errIdx].errors)
	           {
	             //mensajesError += errors[field].errors[err].message + '<br/>';
	             mensajesError += errors[field][errIdx].errors[err].message + '<br/>';
	           }
	           var nodeError = $('<div class="error">'+ mensajesError +'</div>');
	           
	           
	           // Pido containers por la class (en realidad deberia ser id)
	           var containers = $('.'+field); // Deberia ser uno solo
	           //var container = $(containers[0]);
	           var container = $(containers[parseInt(errIdx)]); // Pone el error en el nodo idx, es el caso de nodo multiple y varios nodos tienen errores.
	           
	           
	           // No puedo obtener el nombre de la tag HTML de container...
	           //console.log(container.type); // undefined
	           //console.log(container.attr('type')); // undefined
	           //console.log(container.attr('class')); // field_37 field_y
	           
	           // Agrego el nodo con los errores de los campos del container
	           container.prepend( nodeError );
           }
        }
        
        console.groupEnd();
        
        
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
                
      }); // onload
      
      
      // ===================================================
      // MISMO JS QUE EN SHOW!
      // el cambio es que en lugar de hacer .text(val) es .val(val)
      //
      var show = function (field, value)
      {
          console.log("show de %s, en:", value);
          console.log(field);
           
          // Para CodedText viene "codeString||Texto"
	      if (value.indexOf("||") > 0)
	      {
            console.log('show piped coded text');
            var partes = value.split("||");
            field.addClass(templateId+"_"+partes[0]); // para customizar el estilo, por ejemplo se usa en el triage.
           
            if (field.attr('type') == 'checkbox')
            {
               // FIXME: este creo que seria analogo al caso del radio
               field.attr('checked', 'true');
            }
            else if (field.attr('type') == 'radio')
            {
               // Si el field es radio, vienen todos los fields que se llaman iguales,
               // tengo que distinguir por el valor, cual seleccionar.
               //console.log('========================================');
               //console.log(field); // array
               
               $.each( field, function(i, radio) {
           
                  //console.log(radio.value);
                  //console.log(value);
                  if (radio.value == value)
                  {
                     radio.checked = 'true';
                     return false; // Sale del loop cuando encuentra el valor
                  }
               });
            }
            else
	          field.val(value);
           
	        return;
	      }

          if (value == "label.boolean.true" || value == "label.boolean.false")
          {
             //console.log('show boolean');
             //console.log(booleans[value]); // Si/No
             
             // TODO: cuando se genera la GUI, el GSP deberia poner los booleanos en el locale seleccionado.
             //field.val(booleans[value]);
             field.val(value); // Se debe usar label.boolean.x, no Si/No
             return;
          }
	
	      if (value == "date.struct")
	      {
            console.log('show date.struct');
           
	        // FIXME: el formato de la fecha depende del locale
	        // TODO: verificar si no tengo tiempo, no mostrar " hora:minuto" (ver que hay un espacio entre la fecha y el tiempo.
	        // Esto muestra: 2011-8-13 21:32 
	        //field.val(data[path+'_year']+"-"+data[path+'_month']+"-"+data[path+'_day']+" "+data[path+'_hour']+":"+data[path+'_minute']);
	        
            // FIXME: cuando regreso al edit, pasa que el campo date biene con
            // la fecha, no con "date.struct", y sobreescribe ese valor
            // (que el controller espera), entonces falla el submit.
            // Deberia evitar la escritura de el field de date/datetime.
            
            // TODO: lo que hay que mostrar es en cada select el valor de cada campo, y eso creo que ya se hace solo cuando encuentra las keys field_year, field_month, etc.
           
            return;
	      }
          
          console.log('show default');
	      field.val(value);
      }
      // ===================================================
      
    </g:javascript>
  </head>
  <body>
    <%-- SUBMENU DE SECCIONES SI EXISTEn --%>
    <g:if test="${stage.recordDefinitions.size()>1}">
      <div id="navbar">
        <ul>
          <g:each in="${stage.recordDefinitions}" var="template">
            <li ${((params.templateId==template.templateId)?'class="active"':'')}>
	          <g:hasContentItemForTemplate episodeId="${session.ehrSession?.episodioId}" templateId="${template.templateId}">
	            <g:if test="${it.hasItem}">
	              <g:link controller="guiGen" action="generarShow" id="${it.itemId}"><g:message code="${template.name}" /> (*)</g:link>
	            </g:if>
	            <g:else>
		          <g:link controller="guiGen" action="generarTemplate" params="[templateId:template.templateId]">
		            <g:message code="${template.name}" />
		          </g:link>
		        </g:else>
	          </g:hasContentItemForTemplate>
	        </li>
          </g:each>
        </ul>
      </div>
    </g:if>
    <%-- Form cacheado --%>
    <g:form url="[controller:'guiGen', action:'save']" class="ehrform" method="post" enctype="multipart/form-data">
      <input type="hidden" name="templateId" value="${template.templateId}" />
      <input type="hidden" name="mode" value="edit" />
      ${form}
      <br/>
      <div class="bottom_actions">
        <g:submitButton name="doit" value="Guardar" />
      </div>
    </g:form>
  </body>
</html>