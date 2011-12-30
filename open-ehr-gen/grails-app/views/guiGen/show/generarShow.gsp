<?xml version="1.0" encoding="UTF-8" ?>
<html>
  <head>
    <meta name="layout" content="ehr" />
    <link rel="stylesheet" href="${createLinkTo(dir:'css', file:'generarTemplate.css')}" />
    <script type="text/javascript" src="${createLinkTo(dir:'js', file:'jquery-1.6.2.min.js')}"></script>
    <script type="text/javascript">
      var data = ${data};
      var booleans = {'label.boolean.false':'No', 'label.boolean.true':'Si'}; // TODO: Si y No debe ser i18n
      var templateId = '${template.id}';
    
      $(document).ready(function() {
    
    //return;
    
        for (path in data)
        {
          if (path.match("^field")=="field") // path.startsWith('field')
          {
            //alert(path + ": " + data[path]);
            
            // busca elementos por su class (como es por class devuelve coleccion)
            // es solo uno por como genero el show.
            var elems = $("label."+path);
    
    console.group("ELEMS");
            console.log('elems para path: label.'+path);
            console.log(elems);
    console.groupEnd();

            // Cuando la path es a un campo que es parte de una date, elems es siempre vacio
            // y para mostrar la date tengo que esperar al campo que contiene todas las partes.
            if (elems.size()==0) continue;
    
    
            
            
    
            //alert("Hay elems: " + elems.size());
    
            // TODO: si el tipo es DvQuantity y tenia una sola unidad en el arquetipo, pasa que no se ingresa como dato, entonces no viene en los field-values y no la puedo mostrar junto con la magnitud.
            // TODO: resolver valores booleanos a Si/No o Yes/No, etc, segun el idioma elegido en la config.
            // FIXED: si el valor para path es date.struct, los valores vienen en las keys: path_year, path_month, path_day, path_hour, path_minute.
          
            /*
             Puede ser array de un solo elemento, por ejemplo si se sumitea esto:
              - [a,'','']
             En el paramsCache (que es el 'data') se guarda:
              - [a]
             */
            if ($.isArray( data[path] ))
            {
              console.log( path+ ' tiene muchos valores: ' + data[path] );
              // El elem seleccionado deberia tener su contenedor con class multiple
              // En realidad no es su contenedor, sino el primer padre con class "multiple"

              // El nodo a clonar siempre es el contenedor multiple del elem
              var nodeToClone = $(elems[0]).closest('.multiple');
              //nodeToClone.css({'border':'2px solid #f00'}); // OK!


              // si i es 0, pongo el valor en el nodo, si es mayor que 0 tengo que clonar
              // el nodo para poner el valor en el nodo clonado.

              // el ultimo sibling de nodeToClone, los nuevos nodos clonados van despues de este.
              var lastSibling = nodeToClone;

              // El array puede no tener valores
              if ( data[path].length == 0 ) continue;
    
              // pone el primer valor en el primer nodo
              show($(elems[0]), data[path][0]);

              // pone el valor para el resto de los nodos
              for (i=1; i<data[path].length; i++)
              {
                // FIXME: si es un tipo estructurado (mas de un valor para el nodo), el nodo puede
                // ya estar clonado, solo tengo que meterle el valor. Para saberlo, tengo que ver
                // si el numero de elems es 1 o no, si es 1, el nodo no esta clonado y lo tengo que clonar.

                //alert('elems.size: ' + elems.size() + ', data.path.length: ' + data[path].length);
                
                // Si la cantidad de labels donde van los datos es menor que la cantidad de datos,
                // se que faltan nodos para clonar.
                if (elems.length < data[path].length)
                {
                  var clonedContainer = nodeToClone.clone(); // Unico caso que clono: cuando los nodos que hay son menos que la cantidad de valores que tengo para mostrar
            
                  // ========================
                  // FIXME: deberia borrar los valores de las labels del nodo clonado para que se muestren
                  // solo los valores insertados en ese nodo, no los del nodo original.
                  // ========================
            
                  lastSibling.after( clonedContainer ); // Pone en el dom, luego de lastSibling
                  lastSibling = clonedContainer; // Ahora es el nuevo lastSibling
                
                  // Como agrego un container, tengo un nuevo elem (label).
                  // Es el que tiene class path dentro del clonedContainer.
                  // Forma de seleccionar la label.path dentro del clonedContainer.
                  elems.push( $('label.'+path, clonedContainer) );
                }
                
                // Ahora tengo que pedir el elem dentro de clonedContainer

                //console.log('pongo valor='+ data[path][i]+ ' en elem='+ i);
                //$(elems[i]).text(data[path][i]); // FIXME: podria ser cod||texto o un date con varias partes.
                
                console.log('muestra valor %s de array (%d)', data[path][i], i);
                show($(elems[i]), data[path][i]);
              }
                
              //console.log('elems2');
              //console.log(elems);
              //console.log('---------------------------------');
            
              /* Ejemplo del nodo multiple
             <div class="CLUSTER multiple"> << este es el nodo que deberia clonar
               <span class="label">VVP:</span>
               <span class="content">
                 <div class="ELEMENT ELEMENT_DV_CODED_TEXT">
                   <span class="label">Topografía:</span>
                   <span class="content">
                     <div class="DV_CODED_TEXT ">
                       <span class="content">
                         <label class="field_134"></label> << estos son los elems por los que itero aca
                       </span>
                     </div>
                   </span>
                 </div>
                 <div class="ELEMENT ELEMENT_DvOrdinal">
                   <span class="label">Calibre:</span>
                   <span class="content">
                     <label class="field_135"></label> << estos son los elems por los que itero aca
                   </span>
                 </div>
               </span>
             </div>
              */
        
              continue;
             
            } // if ($.isArray( data[path] ))

            // elemento y valor simple
            console.log('muestra valor simple %s', data[path]);
            show(elems, data[path]);
            
          } // path.startsWith('field')
        } // for path in data
    
        // Otra cosa que puedo hacer es usar como key el atributo id en lugar del class,
        // porque cada item aparece una sola vez en la pagina cuando carga, luego desde
        // js puedo clonar nodos multiples... ahi cambios los ids.
         
      }); // document ready

      var show = function (field, value)
      {
        //console.log('show en de ' + value + ' en field: ');
        //console.log(field);
            
        // Para CodedText viene "codeString||Texto"
        if (value.indexOf("||") > 0)
        {
          var partes = value.split("||");
          field.addClass(templateId+"_"+partes[0]); // para customizar el estilo, por ejemplo se usa en el triage.
          field.text(partes[1] + ' ('+ partes[0] +')'); // muestro el nombre y el codigo entre parentesis
          return;
        }

        if (value == "label.boolean.true" || value == "label.boolean.false")
        {
          // TODO: cuando se genera la GUI, el GSP deberia poner los booleanos en el locale seleccionado.
          field.text(booleans[value]);
          return;
        }
  
        if (value == "date.struct")
        {
          // FIXME: el formato de la fecha depende del locale
          // TODO: verificar si no tengo tiempo, no mostrar " hora:minuto" (ver que hay un espacio entre la fecha y el tiempo.
          // Esto muestra: 2011-8-13 21:32 
          field.text(data[path+'_year']+"-"+data[path+'_month']+"-"+data[path+'_day']+" "+data[path+'_hour']+":"+data[path+'_minute']);
          return;
        }
        
        // Por defecto
        field.text(value);
      }
    </script>
  </head>
  <body>
    <%-- SUBMENU DE SECCIONES SI EXISTEn --%>
    <g:if test="${subsections.size()>1}">
      <div id="navbar">
        <ul>
          <g:each in="${subsections}" var="subsection">
            <li ${((templateId==subsection)?'class="active"':'')}>
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
    <div class="ehrform">
      <%-- Contenido cacheado --%>
      ${content}
    </div>
  </body>
</html>