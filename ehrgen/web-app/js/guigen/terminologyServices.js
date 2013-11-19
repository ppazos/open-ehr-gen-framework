/*
 * guigen/terminologyServices.js
 * Utilizado en generarCreate y generarEdit para hacer busquedas en servicios
 * terminologicos desde la GUI generada, para controles DV_CODED_TEXT con constraintRefs.
 */
$(function() { // ready

     // Busqueda en servicios terminologicos
   
     /* Mostrar la busqueda anterior no funciona bien si hay 2 campos de busqueda en la misma pantalla>
      * si el click se hace en un input disinto para el que se creo la term_list, se deberian borrar
      * los resultados anteriores porque pueden ser a distintas terminologias.
      *
     // Si hay un texto en el input ya hice una busqueda y pude haber escondido
     // el term_list muestro el term_list si estaba escondido.
     $('input.find_in_terminology').click(function(e) {
     
       $this = $(this);
     
       term_list = $('#term_list')[0];
       term_list_close = $('a.term_list_close')[0];
       if (term_list)
       {
         term_list = $(term_list);
         term_list_close = $(term_list_close);
         
         // Posiciona
         term_list.css({top: $this.position().top + 26, left: $this.position().left + 44});
         term_list_close.css({top: $this.position().top + 26, left: $this.position().left + 22});

         term_list.show();
         term_list_close.show();
       }
     });
     */
   
     // Buscar en el servidor por AJAX, para obtener codigos
     $('input.find_in_terminology').on('input', function() {
     
       console.log(this.value, this.value.length);
       
       input = $(this);
       
       // =================================================================
       // TODO: esperar de hacer el request cuando para de escribir
       //
       if (this.value.length > 3)
       {
          // Cuidado: archetypeid es todo minusculas!
          //{bind: "ac0001", archetypeid: "openEHR-EHR-OBSERVATION.test_servicios_terminologicos.v1"}
          console.log( $(this).data() );
          

          data = $(this).data();
          data.q = this.value;
          
          $.get(
            '/ehrgen-1.4/ajaxApi/findTerm', // WARNING: si cambia la version de EHRGen, esto debe cambiar
            data,
            function(res) {
          
              //console.log(res);
              
              term_list = $('#term_list')[0];
              term_list_close = $('a.term_list_close')[0];
              if (!term_list)
              {
                 term_list = $('<div id="term_list"></div>');
                 term_list_close = $('<a href="#" class="term_list_close">X</a>');
                 
                 $('body').append( term_list );
                 $('body').append( term_list_close );
                 
                 // ===================================================================
                 // Close term list
                 //
                 term_list_close.click(function(e) {
                   //console.log('close');
                   e.preventDefault();
                   term_list.hide();
                   term_list_close.hide();
                 });
                 
                 // ===================================================================
                 // Remover codigo seleccionado
                 $('body').on('click', 'a.removeCode', function(e) {
                 
                   $(this).parent().remove();
                 });
                 
                 // ===================================================================
                 // Select a term
                 //
                 term_list.on('click', 'a.selectCode', function(e) {
                 
                   e.preventDefault();
                   
                   //console.log(this); // anchor
                   container = $(this).parent().parent();
                   
                   //container.toggleClass('active');
                   
                   
                   // El nombre de los campos generados para submitear se sacan de data-name del input generado.
                   text = $('div.text', container).text();
                   code = $('div.code', container).text();
                   
                   console.log(input);
                   
                   // =========================================================================================
                   // Si se esta buscando otro codigo, y ya hay uno seleccionado, y el nodo no es multiple,
                   // no debe dejar seleccionar otro codigo. Pero deja buscar...
                   // =========================================================================================
                   
                   // 1. No es multiple?
                   var container = input.parent().parent(); // DIV que dice si es o no multiple
                   if (!container.hasClass('multiple'))
                   {
                     // 2. Ya hay un codigo seleccionado para el input (puedo tener mas de un input simple y un codigo por input)
                     selectedCodes = $('input[name='+ input.data('name') +']', input.parent());
                     if (selectedCodes.length > 0)
                     {
                       // No deja seleccionar otro hasta que elimine el actual
                       alert('Solo puede seleccionar un codigo y ya hay un codigo seleccionado');
                     }
                     else // Permite seleccionar multiples
                     {
                       input.after('<div><div class="selectedCode">'+ text +' ('+ code +')</div> <input type="hidden" name="'+ input.data('name') +'" value="'+ code +'||'+ text +'" /><a href="#" class="removeCode"> X </a></div>');
                       term_list.css({top: term_list.position().top + 17});
                       term_list_close.css({top: term_list_close.position().top + 17});
                     }
                   }
                   else // Permite seleccionar multiples
                   {
                     input.after('<div><div class="selectedCode">'+ text +' ('+ code +')</div> <input type="hidden" name="'+ input.data('name') +'" value="'+ code +'||'+ text +'" /><a href="#" class="removeCode"> X </a></div>');
                     term_list.css({top: term_list.position().top + 17});
                     term_list_close.css({top: term_list_close.position().top + 17});
                   }
                 });
              }
              else // Muestra term_list y term_list_close que ya estan en el DOM
              {
                 term_list = $(term_list);
                 term_list_close = $(term_list_close);
                 
                 term_list.show();
                 term_list_close.show();
              }
              
              // Quita codigos actuales
              //$('div', term_list).remove();
              term_list.html('');
              
              $(res).each(function(index) {
              
                //console.log( this );
                term_list.append( '<div><div class="text"><a href="#" class="selectCode">'+ this.text +'</a></div><div class="code"><a href="#" class="selectCode">'+ this.terminologyId+'::'+this.code +'</a></div></div>' );
              });
              
              term_list.css({top: input.position().top + 26, left: input.position().left + 44});
              term_list_close.css({top: input.position().top + 26, left: input.position().left + 22});
            },
            'json'
          );
       }
     });
     
     // / Busqueda en servicios terminologicos
     // ===================================================================================
});