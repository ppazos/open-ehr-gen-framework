<html>
  <head>
    <meta name="layout" content="ehr-modal" />
    <style>
      textarea {
        width: 600px;
        height: 160px;
      }
    </style>
    <g:javascript library="prototype/prototype" />
    
    
    <%-- No se usa YUI calendar, se usa DatePicker
    <!--  YUI CALENDAR -->
    <!--CSS file-->
    <link rel="stylesheet" type="text/css" href="${createLinkTo(dir:'css/yui/calendar',file:'calendar.css')}"></link>
    
    <!-- Dependencies -->
    <g:javascript library="yui/yahoo/yahoo-min" />
    <g:javascript library="yui/dom/dom-min" />
    <g:javascript library="yui/event/event-min" />
    
    <!-- Source file and calendar css -->
    <g:javascript library="yui/calendar/calendar-min" />
    
    <g:javascript>
    <!--
    var calendar1; // UNA SOLA INSTANCIA DEL CALENDARIO SE LLAMA PARA LLENAR DISTINTOS INPUTS
    var showingCalendars = false;
    var currentInputDate = null; // Global que dice, segun en que calendario estoy, cual es el input donde poner la fecha cuando es seleccionada una.
                                 // La cosa es que tengo un solo calendar y varios inputs posibles para poner la fecha.
    
    // Acciones que se hacen onload ...
    Event.observe(window, 'load', function () {

        calendar1 = new YAHOO.widget.Calendar("calendar1", "calendarDiv1", { title:"${message(code:'label.chooseDate')}", close:true} );

        // clic en las imagenes de calendario muestra el calendario
        YAHOO.util.Event.addListener ("showCalendarDiv1", "click", calendar1.show, calendar1, true); // Muestra calendario
        YAHOO.util.Event.addListener ("showCalendarDiv1", "click", showCalendar1EventHandler); // Setea posicion del calendario
        
        YAHOO.util.Event.addListener ("showCalendarDiv2", "click", calendar1.show, calendar1, true); // Muestra calendario
        YAHOO.util.Event.addListener ("showCalendarDiv2", "click", showCalendar1EventHandler); // Setea posicion del calendario
        
        // clic en input muestra el calendario
        // Muestra el calendario pero se confunde al entrar en showCalendar1EventHandler, si hago clic en el input 
        // la condicion del if da false y deberia dar true> if (Event.element(evt).hasClassName('_cal1'))...
        //YAHOO.util.Event.addListener ("date1", "click", calendar1.show, calendar1, true); // Muestra calendario
        //YAHOO.util.Event.addListener ("date1", "click", showCalendar1EventHandler); // Setea posicion del calendario
        
        
        calendar1.selectEvent.subscribe(setDate);
        calendar1.render();
     });
     // ===
     
     function showCalendar1EventHandler(evt)
     {
        //alert(Object.toJSON(evt));
     
        if ( Event.element(evt).hasClassName('_cal1') )
        {
            //alert("cal 1");
            currentInputDate = "startDate";
          }
          /* por si tengo multiples fechas
          else
          {
              //alert("cal 2");
              currentInputDate = "date2";
          }
          */
     
        var mousePosition = getMouseXY(evt);
        var wstr = $('calendarDiv1').style.width;
        var width = wstr.substring(0,wstr.length-2);

        // Aparece a la izquierda del mouse.
        //$('calendarDiv1').style.left = (mousePosition.x - width) + 'px';
        $('calendarDiv1').style.left = (mousePosition.x) + 'px';
        
        // A la derecha del mouse
        //$('calendarDiv1').style.left = mousePosition.x + 'px';
        
        $('calendarDiv1').style.top = mousePosition.y + 'px';
     }
     // ===
     
     function setDate()
     {
        var arrDates = calendar1.getSelectedDates();
        var date = arrDates[0];
        
        setInput(currentInputDate, date.getFullYear(), date.getMonth()+1, date.getDate());
        
        hideCalendars(); // se esconden al hacer click en una fecha ...
     }
     // ===
     
     function setInput(id,year,month,day)
     {
        // Arma la fecha internacionalizada al locale del servidor.
        // Las partes son enviadas con el modelo al view, y aca las agarro para ver como armar la fecha.
        //
        partesFecha = ["yyyy", "MM", "dd"]; // ${partesFecha}.toArray(); // Algo como ["M", "dd", "yy"]
        
        vuelta = 0;
        dateValue = "";
        partesFecha.each(function(n) {

            datePart = n;
        
            if (datePart.startsWith('M'))
            {
               dateValue += ((month<10) ? '0'+month.toString() : month.toString());
               if (vuelta<2) dateValue += '-';
            }
            else if (datePart.startsWith('d'))
            {
               dateValue += ((day<10) ? '0'+day.toString() : day.toString());
               if (vuelta<2) dateValue += '-';
            }
            else if (datePart.startsWith('y'))
            {
               dateValue += year.toString();
               if (vuelta<2) dateValue += '-';
            }
            
            vuelta++;
        });
        
        $(id).value = dateValue; // No funka en IE
     }
     // ===
     
     function hideCalendars()
     {
         currentInputDate = null;
         $('calendarDiv1').style.display = 'none';
     }
     // ===
     
     // Obtener posicion del mouse ...
     // Detect if the browser is IE or not.
      // If it is not IE, we assume that the browser is NS.
      var IE = document.all?true:false
      
      // If NS -- that is, !IE -- then set up for mouse capture
      if (!IE) document.captureEvents(Event.MOUSEMOVE)
      
      // Temporary variables to hold mouse x-y pos.s
      var tempX = 0
      var tempY = 0
      
      // Main function to retrieve mouse x-y pos.s
      
      function getMouseXY(e)
      {
        if (IE) { // grab the x-y pos.s if browser is IE
          tempX = event.clientX + document.body.scrollLeft
          tempY = event.clientY + document.body.scrollTop
        } else {  // grab the x-y pos.s if browser is NS
          tempX = e.pageX
          tempY = e.pageY
        }  
        // catch possible negative values in NS4
        if (tempX < 0){tempX = 0}
        if (tempY < 0){tempY = 0} 
        
        return {x:tempX, y:tempY};
      }
     // ===
    
    -->
    </g:javascript>
    
    // No se usa YUI Calendar, se usa DatePicker
    --%>
  </head>
  <body>
    <h1><g:message code="trauma.create.title" /></h1>
  
    <%--  // No se usa YUI Calendar, se usa DatePicker
    <!-- Calendario para elegir fecha -->
    <div id="calendarDiv1" style="position:absolute; width:205px; display:none; z-index:100;"></div>
    --%>
  
    <g:form action="create">
    
      <%-- se crea el episodio para una persona seleccionada por admision --%>
      <g:if test="${params.root && params.extension}">
        Se crea episodio para la persona con identificador: ${params.root}::${params.extension}<br/>
        <input type="hidden" name="root" value="${params.root}" />
        <input type="hidden" name="extension" value="${params.extension}" />
      </g:if>
      
      <%-- // No se usa YUI Calendar, se usa DatePicker
      <input type="text" name="startDate" id="startDate" />
    
      <span id="showCalendarDiv1" style="cursor:pointer">
        <img src="${createLinkTo(dir:'images',file:'calendar.gif')}" height="30" align="bottom" class="_cal1" />
      </span>
      --%>
    
      <g:message code="trauma.create.label.fechaIngreso" /><br/>   
      <g:datePicker name="startDate" value="${((params.startDate) ? new Date(params.startDate) : null)}" />
      <br/><br/>
   
      <g:message code="trauma.create.label.observaciones" /><br/>
      <textarea name="otherContext">${params.otherContext}</textarea>
      <br/><br/>
      
      <div id="bottom_actions">
        <g:submitButton name="doit" value="${message(code:'trauma.create.action')}" />
      </div>
    </g:form>
  </body>
</html>