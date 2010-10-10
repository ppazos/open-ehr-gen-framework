<%@ page import="org.codehaus.groovy.grails.commons.*" %><%@ page import="auth.ClientDevice" %><g:if test="${session.device == ClientDevice.DEVICE_MOBILE}"><?xml version="1.0" encoding="UTF-8"?><!DOCTYPE HTML PUBLIC "-//W3C//DTD XHTML Mobile 1.0//EN" "http://www.wapforum.org/DTD/xhtml-mobile10.dtd"></g:if><g:else><?xml version="1.0" ?><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"></g:else><%-- 
=============================================================================
TODO: POR AHORA ES UNA COPIA DE resident-k4.gsp PERO LUEGO VAN A SER DISTINTAS 
==============================================================================
--%>
<html xmlns="http://www.w3.org/1999/xhtml">
<g:set var="startmsec" value="${System.currentTimeMillis()}"/>
  <head>
    <%-- No quiero paginas cacheadas --%>
    <META HTTP-EQUIV="Pragma" CONTENT="no-cache" />
    <META HTTP-EQUIV="Expires" CONTENT="-1" />
    <META HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE" />
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>

    <title><g:layoutTitle/> | Helpi |</title>

    <%-- controller and action variables extracted from URI --%>
    <g:set var="controller" value="${(request.forwardURI.split('/').size()>2)?request.forwardURI.split('/')[2]:''}" />
    <g:set var="action" value="${(request.forwardURI.split('/').size()>3)?request.forwardURI.split('/')[3]:''}" />

    <%-- choose the style skin --%>
    <g:set var="skin" value="${'default'}"/>
    <g:if test="${session.building?.getSetting('resident.skin')}">
      <g:set var="skin" value="${session.building.getSetting('resident.skin')}"/>
    </g:if>
    <g:if test="${session.user?.getSetting('resident.skin')}">
      <g:set var="skin" value="${session.user.getSetting('resident.skin')}"/>
    </g:if>

    <g:set var="random" value=""/>
    <g:if test="${session.debug}">
      <%-- the k4 does not reload css if the url is the same, no matter what --%>
      <g:set var="random" value="?rand=${Math.random()}"/>
    </g:if>
    <link rel="stylesheet" type="text/css" href="${createLinkTo(dir:'style/skins/'+skin+'/resident-k4',file:'skin.css'+random)}"></link>
    <g:if test="${session.building.getSetting('path.style')}">
      <link rel="stylesheet" type="text/css" href="${session.building.getSetting('path.style')}/skins/${skin}/resident-k4/skin.css${random}"></link>
    </g:if>

<!--[if lt IE 7]> 
    <script language="JavaScript">
    var retries = 0;
    var maxretries = 10;
    var retryTimeout = 1000;

    function isImageOk(img) {
        // for IE
        if (!img.complete) {
            return false;
        }
        // for others
        if (typeof img.naturalWidth != "undefined" && img.naturalWidth == 0) {
            return false;
        }
        // No other way of checking: assume it's ok.
        return true;
    }

    function correctPNG() // correctly handle PNG transparency in Win IE 5.5 & 6.
    {
       var arVersion = navigator.appVersion.split("MSIE")
       var version = parseFloat(arVersion[1])
       if ((version >= 5.5) && (document.body.filters)) 
       {
          for(var i=0; i<document.images.length; i++)
          {
             var img = document.images[i];
             if (!isImageOk(img) && retries < maxretries)
             {           
               setTimeout("correctPNG()", retryTimeout);
               return;
             }
             var imgName = img.src.toUpperCase();
             if (imgName.substring(imgName.length-3, imgName.length) == "PNG")
             {
                var imgID = (img.id) ? "id='" + img.id + "' " : ""
                var imgClass = (img.className) ? "class='" + img.className + "' " : ""
                var imgTitle = (img.title) ? "title='" + img.title + "' " : "title='" + img.alt + "' "
                var imgStyle = "display:inline-block;" + img.style.cssText 
                if (img.align == "left") imgStyle = "float:left;" + imgStyle
                if (img.align == "right") imgStyle = "float:right;" + imgStyle
                if (img.parentElement.href) imgStyle = "cursor:hand;" + imgStyle
                var strNewHTML = "<span " + imgID + imgClass + imgTitle
                + " style=\"" + "width:" + img.width + "px; height:" + img.height + "px;" + imgStyle + ";" 
                + "filter:progid:DXImageTransform.Microsoft.AlphaImageLoader"
                + "(src=\'" + img.src + "\', sizingMethod='scale');\"></span>" 
                img.outerHTML = strNewHTML
                i = i-1
             }
          }
       }    
    }
    </script>

    <script defer language="JavaScript">
    // using the defer keyword is much faster than hooking up an on-load event
    correctPNG();
    </script>
<![endif]-->


    <script language="JavaScript">
    function screensaverTimeout()
    {
    <g:if test="${['screensaver'].contains(action)}">
       setTimeout("showScreensaver()",  ${ConfigurationHolder.config.helpi.screensaver.refresh}000);
    </g:if>
    <g:else>
       setTimeout("showScreensaver()",  ${ConfigurationHolder.config.helpi.screensaver.showAfter}000);
    </g:else>
    }

    function showScreensaver()
    {
       window.location.href="${request.contextPath}/resident/screensaver";
    }

    function handleOnLoad()
    {
       screensaverTimeout();
       ${pageProperty(name:'body.onload')};
    }
    </script>

    <g:layoutHead />

  </head>

  <body onload="handleOnLoad();"> 
      
    <div id="content" class="backgroundimage ${pageProperty(name:'body.class')}">

      <g:layoutBody />

      <g:if test="${flash.message}">
        <div id="message" class="error" style="display:none;" >
          <g:message code="${flash.message}" args="${flash.args}" />
        </div>
      </g:if>
    </div>
      
    <!-- main menu -->
    <g:link controller="resident" action="home"><div id="mainmenu_link"><g:message code="resident.layout.main_menu"/></div></g:link>

    <div id="crumbsbar">     
      <g:if test="${['car'].contains(controller)}">
        <!-- car -->
        <h1 class="valet">
          <g:link controller="resident" action="valet"><div><g:message code="resident.layout.valet"/></div></g:link>
        </h1>
      </g:if>
        
      <g:if test="${['timeSlot','amenity'].contains(controller) || ['amenities'].contains(action)}">
        <!-- amenities -->
        <h1 class="amenities">
          <g:link controller="resident" action="amenities"><div><g:message code="resident.layout.amenities"/></div></g:link>
        </h1>
      </g:if>
        
      <g:if test="${['packetInbound','packetOutbound'].contains(controller)}">
        <!-- deliveries -->
        <h1 class="deliveries">
          <g:link controller="resident" action="deliveries"><div><g:message code="resident.layout.deliveries"/></div></g:link>
        </h1>
      </g:if>


      <g:if test="${['aServiceRequest'].contains(controller)}">
        <!-- service -->
        <h1 class="service">
          <g:link controller="resident" action="service"><div><g:message code="resident.layout.service"/></div></g:link>
        </h1>
      </g:if>


      <g:if test="${['automation'].contains(controller)}">
        <!-- automation -->
        <h1 class="automation">
          <g:link controller="resident" action="automation"><div><g:message code="resident.layout.automation"/></div></g:link>
        </h1>
      </g:if>

      <g:if test="${['resident'].contains(controller) && ['settings'].contains(action)}">
        <!-- settings -->
        <h1 class="settings">
          <g:link controller="resident" action="settings"><div><g:message code="resident.layout.settings"/></div></g:link>
        </h1>
      </g:if>

      <g:if test="${['notification'].contains(controller)}">
        <!-- messages -->
        <h1 class="messages">
          <g:link controller="resident" action="messages"><div><g:message code="resident.layout.messages"/></div></g:link>
        </h1>
      </g:if>

      <g:if test="${['subscription'].contains(controller)}">
        <!-- subscriptions / multiple controllers -->
        <!-- must check the subscription_type parameter to know where we are exactly -->
        <g:if test="${params.subscription_type == Subscription.TYPE_VALET}">
          <h1 class="valet">
            <g:link controller="resident" action="valet"><div><g:message code="resident.layout.valet"/></div></g:link>
          </h1>
        </g:if>
        <g:if test="${params.subscription_type == Subscription.TYPE_AMENITY}">
          <h1 class="amenities">
            <g:link controller="resident" action="amenities"><div><g:message code="resident.layout.amenities"/></div></g:link>
          </h1>
        </g:if>
        <g:if test="${params.subscription_type == Subscription.TYPE_NOTIFICATION}">
          <h1 class="messages">
            <g:link controller="resident" action="messages"><div><g:message code="resident.layout.messages"/></div></g:link>
          </h1>
        </g:if>
        <g:if test="${params.subscription_type == Subscription.TYPE_PACKET}">
          <h1 class="deliveries">
            <g:link controller="resident" action="deliveries"><div><g:message code="resident.layout.deliveries"/></div></g:link>
          </h1>
          </g:if>              
      </g:if>
    </div>

  </body>	
<!-- gsp time (msecs): ${System.currentTimeMillis()-startmsec} -->
</html>





    <meta http-equiv="refresh" content="${ConfigurationHolder.config.helpi.screensaver.showAfter}; url=${request.contextPath}/resident/screensaver" />
  
    <%-- No quiero paginas cacheadas --%>
    <META HTTP-EQUIV="Pragma" CONTENT="no-cache" />
    <META HTTP-EQUIV="Expires" CONTENT="-1" />
    <META HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE" />
    
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title><g:layoutTitle/> | Helpi |</title>

    <%-- choose the style skin --%>
    <g:set var="skin" value="${'default'}"/>
    <g:if test="${session.building?.getSetting('resident.skin')}">
      <g:set var="skin" value="${session.building.getSetting('resident.skin')}"/>
    </g:if>
    <g:if test="${session.user?.getSetting('resident.skin')}">
      <g:set var="skin" value="${session.user.getSetting('resident.skin')}"/>
    </g:if>

    <g:if test="${session.device == ClientDevice.DEVICE_MOBILE}">
      <link rel="stylesheet" type="text/css" href="${createLinkTo(dir:'style/skins/'+skin+'/resident-mobile',file:'skin.css')}"></link>
      <g:if test="${session.building.getSetting('path.style')}">
        <link rel="stylesheet" type="text/css" href="${session.building.getSetting('path.style')}/skins/${skin}/resident-mobile/skin.css"></link>
      </g:if>
    </g:if>
    <g:elseif test="${session.device == ClientDevice.DEVICE_IPHONE}">
      <link rel="stylesheet" type="text/css" href="${createLinkTo(dir:'style/skins/'+skin+'/resident-iphone',file:'skin.css')}"></link>
      <g:if test="${session.building.getSetting('path.style')}">
        <link rel="stylesheet" type="text/css" href="${session.building.getSetting('path.style')}/skins/${skin}/resident-iphone/skin.css"></link>
      </g:if>
      <meta name="viewport" content="width=320; initial-scale=1.0; maximum-scale=1.0; user-scalable=0;"/>
    </g:elseif>
    <g:else>
      <g:set var="random" value=""/>
      <g:if test="${session.debug}">
        <%-- the k4 does not reload css if the url is the same, no matter what --%>
        <g:set var="random" value="?rand=${Math.random()}"/>
      </g:if>
      <link rel="stylesheet" type="text/css" href="${createLinkTo(dir:'style/skins/'+skin+'/resident-k4',file:'skin.css'+random)}"></link>
      <g:if test="${session.building.getSetting('path.style')}">
        <link rel="stylesheet" type="text/css" href="${session.building.getSetting('path.style')}/skins/${skin}/resident-k4/skin.css${random}"></link>
      </g:if>
    </g:else>

    <g:layoutHead />
  </head>

  <body onload="${pageProperty(name:'body.onload')}">
  
    <%-- controller and action variables extracted from URI --%>
    <g:set var="controller" value="${(request.forwardURI.split('/').size()>2)?request.forwardURI.split('/')[2]:''}" />
    <g:set var="action" value="${(request.forwardURI.split('/').size()>3)?request.forwardURI.split('/')[3]:''}" />

    <%-- TEST
    <g:if test="${session.user}">
      <g:formatDate2 date="${new Date()}" showdate="true" showtime="true" />
    </g:if>
    --%>
    
    <div id="content" class="backgroundimage ${pageProperty(name:'body.class')}">
      <%-- FOR THE IPHONE --%> 
      <g:if test="${session.device == ClientDevice.DEVICE_IPHONE}">

        <%-- <div id="back" class="back"><a id="back" onClick="history.back();"><g:message code="common.action.back"/></a></div> --%>
        <%-- FIXME: Este link depende de como se llame el proyecto --%>
        <div id="back" class="back"><a id="back" href="/helpi/unit"><g:message code="common.action.back"/></a></div>

        <g:if test="${['aServiceRequest'].contains(controller)}">
          <h1><g:message code="resident.layout.service"/></h1>
        </g:if>
        <g:if test="${['packetInbound','packetOutbound'].contains(controller)}">
          <h1><g:message code="resident.layout.deliveries"/></h1>
        </g:if>
        <g:if test="${['car'].contains(controller)}">
          <h1><g:message code="resident.layout.valet"/></h1>
        </g:if>
        <g:if test="${['notification'].contains(controller)}">
          <h1><g:message code="resident.layout.messages"/></h1>
        </g:if>
        <g:if test="${['automation'].contains(controller)}">
          <h1><g:message code="resident.layout.automation"/></h1>
        </g:if>
        <g:if test="${['subscription'].contains(controller)}">
          <!-- subscriptions / multiple controllers -->
          <!-- must check the subscription_type parameter to know where we are exactly -->
          <g:if test="${params.subscription_type == Subscription.TYPE_VALET}">
            <h1><g:message code="resident.layout.valet"/></h1>
          </g:if>
          <g:if test="${params.subscription_type == Subscription.TYPE_AMENITY}">
            <h1><g:message code="resident.layout.amenities"/></h1>
          </g:if>
          <g:if test="${params.subscription_type == Subscription.TYPE_NOTIFICATION}">
            <h1><g:message code="resident.layout.messages"/></h1>
          </g:if>
          <g:if test="${params.subscription_type == Subscription.TYPE_PACKET}">
            <h1><g:message code="resident.layout.deliveries"/></h1>
          </g:if>              
        </g:if>
      </g:if>

      <g:layoutBody />

      <g:if test="${flash.message}">
        <div id="message" class="error" style="display:none;" >
          <g:message code="${flash.message}" args="${flash.args}" />
        </div>
      </g:if>
    </div>
      
    <%-- NOT FOR THE IPHONE  --%>
    <g:if test="${session.device != ClientDevice.DEVICE_IPHONE }">
      <!-- main menu -->
      <g:link controller="resident" action="home"><div id="mainmenu_link"><g:message code="resident.layout.main_menu"/></div></g:link>

      <div id="crumbsbar">     
        <g:if test="${['car'].contains(controller)}">
          <!-- car -->
          <h1 class="valet">
            <g:link controller="resident" action="valet"><div><g:message code="resident.layout.valet"/></div></g:link>
          </h1>
        </g:if>
        
        <g:if test="${['timeSlot','amenity'].contains(controller) || ['amenities'].contains(action)}">
          <!-- amenities -->
          <h1 class="amenities">
            <g:link controller="resident" action="amenities"><div><g:message code="resident.layout.amenities"/></div></g:link>
          </h1>
        </g:if>
        
        <g:if test="${['packetInbound','packetOutbound'].contains(controller)}">
          <!-- deliveries -->
          <h1 class="deliveries">
            <g:link controller="resident" action="deliveries"><div><g:message code="resident.layout.deliveries"/></div></g:link>
          </h1>
        </g:if>


        <g:if test="${['aServiceRequest'].contains(controller)}">
          <!-- service -->
          <h1 class="service">
            <g:link controller="resident" action="service"><div><g:message code="resident.layout.service"/></div></g:link>
          </h1>
        </g:if>


        <g:if test="${['automation'].contains(controller)}">
          <!-- automation -->
          <h1 class="automation">
            <g:link controller="resident" action="automation"><div><g:message code="resident.layout.automation"/></div></g:link>
          </h1>
        </g:if>

        <g:if test="${['resident'].contains(controller) && ['settings'].contains(action)}">
          <!-- settings -->
          <h1 class="settings">
            <g:link controller="resident" action="settings"><div><g:message code="resident.layout.settings"/></div></g:link>
          </h1>
        </g:if>

        <g:if test="${['notification'].contains(controller)}">
          <!-- messages -->
          <h1 class="messages">
            <g:link controller="resident" action="messages"><div><g:message code="resident.layout.messages"/></div></g:link>
          </h1>
        </g:if>

        <g:if test="${['subscription'].contains(controller)}">
          <!-- subscriptions / multiple controllers -->
          <!-- must check the subscription_type parameter to know where we are exactly -->
          <g:if test="${params.subscription_type == Subscription.TYPE_VALET}">
            <h1 class="valet">
              <g:link controller="resident" action="valet"><div><g:message code="resident.layout.valet"/></div></g:link>
            </h1>
          </g:if>
          <g:if test="${params.subscription_type == Subscription.TYPE_AMENITY}">
            <h1 class="amenities">
              <g:link controller="resident" action="amenities"><div><g:message code="resident.layout.amenities"/></div></g:link>
            </h1>
          </g:if>
          <g:if test="${params.subscription_type == Subscription.TYPE_NOTIFICATION}">
            <h1 class="messages">
              <g:link controller="resident" action="messages"><div><g:message code="resident.layout.messages"/></div></g:link>
            </h1>
          </g:if>
          <g:if test="${params.subscription_type == Subscription.TYPE_PACKET}">
            <h1 class="deliveries">
              <g:link controller="resident" action="deliveries"><div><g:message code="resident.layout.deliveries"/></div></g:link>
            </h1>
          </g:if>              
        </g:if>

      </div>
    </g:if>    
    
  </body>	
<!-- gsp time (msecs): ${System.currentTimeMillis()-startmsec} -->
</html>
