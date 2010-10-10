<%@ page import="org.codehaus.groovy.grails.commons.*" %><?xml version="1.0" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<g:set var="startmsec" value="${System.currentTimeMillis()}"/>

    <head>
        <title><g:layoutTitle default="Mirigi Concierge Services" /></title>

        <%-- choose the style skin --%>
        <g:set var="skin" value="${'default'}"/>
        <g:if test="${session.building?.getSetting('staff.skin')}">
          <g:set var="skin" value="${session.building.getSetting('staff.skin')}"/>
        </g:if>
        <g:if test="${session.user?.getSetting('staff.skin')}">
          <g:set var="skin" value="${session.user.getSetting('staff.skin')}"/>
        </g:if>
        <link rel="stylesheet" type="text/css" href="${createLinkTo(dir:'style/skins/'+skin+'/staff',file:'skin.css')}"></link>
        <g:if test="${session.building.getSetting('path.style')}">
          <link rel="stylesheet" type="text/css" href="${session.building.getSetting('path.style')}/skins/${skin}/staff/skin.css"></link>
        </g:if>

        <link rel="shortcut icon" href="${createLinkTo(dir:'images',file:'favicon.ico')}" type="image/x-icon" />
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        
        <%-- No quiero paginas cacheadas --%>
        <META HTTP-EQUIV="Pragma" CONTENT="no-cache" />
        <META HTTP-EQUIV="Expires" CONTENT="-1" />
        <META HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE" />
        
        <g:layoutHead />
        <g:javascript library="application" />				
    </head>
    <body>
    
    <%-- TEST LOCALE es_UY <g:message code="guat.pas.man" /> --%>
    
      <div id="logobar">
       <img src="${session.building.getSetting('path.images')}/logo-h40.png" />
      </div>

      <g:if test="${session.user}">
          <div id="userbar">
            ${session.user.name}
<%--
            (<g:render template="/staffRoles" model="[staffmember:session.user]" />)
--%>
            |
            <g:link controller="staffmember" action="settings" id="${session.user.id}"><g:message code="staffmember.layout.action.settings" /></g:link>
            |
            <g:link controller="staffmember" action="logout"><g:message code="staffmember.layout.action.logout" /></g:link>
          
            <%-- <g:formatDate2 date="${new Date()}" showdate="true" showtime="false" />  --%>
          
          </div>
        </g:if>
        
        <%-- Ok con controller y params.controller funka!!!
        <br/>
        C ${controller=="staffmember"}<br/>
        p.c ${params.controller}<br/>
        
        ${action}<br/>
        ${params.action}<br/>
        ${action=="home"}<br/>
        ${params.action=="home"}<br/>
        --%>                        
        <%-- SESSION.LOCALE: ${session.locale} --%>
        
        <div id="navbar">
          <ul>
            <g:hasPerm controller="staffmember" action="home">
              <li ${(params.controller=="staffmember"&&params.action=="home")?'class="active"':''}>
                <g:link controller="staffmember" action="home">
                  <g:message code="navbar.home"/>
                </g:link>
              </li>
            </g:hasPerm>
            <g:hasPerm controller="unit" action="list">
              <li ${(params.controller=="unit"||params.controller=="pinLoginHistoryItem")?'class="active"':''}>
                <g:link controller="unit" action="list">
                  <g:message code="navbar.units"/>
                </g:link>
              </li>
            </g:hasPerm>
            <g:hasPerm controller="staffmember" action="list">
              <li ${((params.controller=="staffmember"&&params.action!="home") || params.controller=="loginHistoryItem")?'class="active"':''}>
                <g:link controller="staffmember" action="list">
                  <g:message code="navbar.staff"/>
                </g:link>
              </li>
            </g:hasPerm>
            <g:hasPerm controller="carTicket" action="activeList">
              <li ${(["carTicket", "carmake", "carHistoryItem", "carStats"].contains(params.controller))?'class="active"':''}>
                <g:link controller="carTicket" action="activeList">
                  <g:message code="navbar.valet"/>
                </g:link>
              </li>
            </g:hasPerm>
            <g:hasPerm controller="amenity" action="list">
              <li ${(["amenity","timeSlot","amenityHistoryItem"].contains(params.controller))?'class="active"':''}>
                <g:link controller="amenity" action="list">
                  <g:message code="navbar.amenities"/>
                </g:link>
              </li>
            </g:hasPerm>
            <g:hasPerm controller="packetInbound" action="list">
              <li ${(["packetInbound","packetOutbound","packetHistoryItem", "packetSettings"].contains(params.controller))?'class="active"':''}>
                <g:link controller="packetInbound" action="list">
                  <g:message code="navbar.deliveries"/>
                </g:link>
              </li>
            </g:hasPerm>
            <g:hasPerm controller="aServiceRequest" action="listPendingRequests">
              <li ${(["aServiceRequest","requestForm","formGroup"].contains(params.controller))?'class="active"':''}>
                <g:link controller="aServiceRequest" action="listPendingRequests">
                  <g:message code="navbar.requests"/>
                </g:link>
              </li>
            </g:hasPerm>
            <g:hasPerm controller="notification" action="staffGroupList">
              <li ${(["notification"].contains(params.controller))?'class="active"':''}>
                <g:link controller="notification" action="staffGroupList">
                  <g:message code="navbar.notify"/>
                </g:link>
              </li>
            </g:hasPerm>
            <g:hasPerm controller="directory" action="show">
              <li ${(["directory"].contains(params.controller))?'class="active"':''}>
                <g:link controller="directory" action="show">
                  <g:message code="navbar.directory"/>
                </g:link>
              </li>
            </g:hasPerm>
            
            <%--
            <li ${(controller=="staffmemberNotification")?'class="active"':''}><g:link controller="staffmemberNotification"><g:message code="navbar.notify"/></g:link></li>
            <li ${(controller=="requestForm"||controller=="serviceRequest")?'class="active"':''}><g:link controller="serviceRequest"><g:message code="navbar.requests"/></g:link></li>
            <li ${(controller=="packetInbound" || controller=="packetOutbound")?'class="active"':''}><g:link controller="packetInbound"><g:message code="navbar.deliveries"/></g:link></li>
            <li ${(controller=="automation")?'class="active"':''}><g:link controller="automation"><g:message code="navbar.automation"/></g:link></li>
            --%>
            
          </ul>
        </div>
        
        <%-- Dependiendo del modulo, invluye la subnavbar correspondiente --%>
        <div id="subnavbar">
          <g:if test='${params.controller=="unit"}'>
            <g:render template="/subnavbar/unit" />
          </g:if>
          
          <g:if test='${params.controller=="pinLoginHistoryItem"}'>
            <g:render template="/subnavbar/unit" />
          </g:if>
          
          <g:if test='${["staffmember","role"].contains(params.controller)&&params.action!="home"}'>
            <g:render template="/subnavbar/staff" />
          </g:if>
          
          <g:if test='${params.controller=="loginHistoryItem"}'>
            <g:render template="/subnavbar/staff" />
          </g:if>
          
          <g:if test='${["carTicket","carHistoryItem","carmake", "carStats"].contains(params.controller)}'>
            <g:render template="/subnavbar/valet" />
          </g:if>
          
          <g:if test='${["amenity","amenityHistoryItem","timeSlot"].contains(params.controller)}'>
            <g:render template="/subnavbar/amenities" />
          </g:if>
          
          <g:if test='${["packetInbound","packetOutbound","packetHistoryItem","packetSettings"].contains(params.controller)}'>
            <g:render template="/subnavbar/deliveries" />
          </g:if>
          
          <g:if test='${["aServiceRequest","requestForm","formGroup"].contains(params.controller)}'>
            <g:render template="/subnavbar/requests" />
          </g:if>
          
          <g:if test='${["notification"].contains(params.controller)}'>
            <g:render template="/subnavbar/notifications" />
          </g:if>

        </div>
        
        <g:if test="${flash.message}">
          <div class="message">
            <g:message code="${flash.message}" args="${flash.args}" />
          </div>
        </g:if>
        
        <g:layoutBody />		
    </body>	
<!-- gsp time (msecs):         ${System.currentTimeMillis()-startmsec} -->
</html>
