<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
  <head>
    <meta name="layout" content="ehr" />
    <title><g:message code="trauma.sign.title" /></title>
    <g:javascript library="jquery"/>
    <g:javascript>
      $(document).ready(function()
      {
        $("#user").focus();
      });
    </g:javascript>
    <style>
      table {      
        border: 0px;
      }
      th {
        background: none;
        text-align: right;
        vertical-align: middle;
        padding-right: 10px;
        width: 80px;
      }
      .error {
        /* TODO: meter icono de error ! */
        border: 1px solid #f00;
        background-color: #f99;
        padding: 2px;
        margin-bottom: 3px;
      }
      .error ul {
        list-style:none;
        margin:0;
        padding:0;
      }
      .message {
        /* TODO: meter icono de error ! */
        border: 1px solid #0f0;
        background-color: #9f9;
        padding: 2px;
        margin-bottom: 3px;
      }
      .message ul {
        list-style:none;
        margin:0;
        padding:0;
      }
      table #sign_table {
        width: 290px;
      }
      #form1 input[type=submit] {
        position: relative;
        float: right;
      }
    </style>
  </head>
  <body>
    <h1><g:message code="trauma.sign.title" /></h1>

    <g:if test="${flash.error}">
      <div class="error"><g:message code="${flash.error}" /></div>
    </g:if>
    <g:if test="${flash.message}">
      <div class="message"><g:message code="${flash.message}" /></div>
    </g:if>
    <g:if test="${!patient && !flash.error}">
      <div class="message"><g:message code="trauma.sign.noPatientSelected" /></div>
    </g:if>
    <g:isSignedRecord episodeId="${session.ehrSession?.episodioId}">
     <div class="message"><g:message code="trauma.sign.registryAlreadySigned" /></div>
    </g:isSignedRecord>
    
    <g:isNotSignedRecord episodeId="${session.ehrSession?.episodioId}">
    
      <g:form url="[action:'signRecord', id:params.id]" method="post" id="form1" class="ehrform">
         
        <table id="sign_table" align="center">
          <tr>
            <th><g:message code="auth.login.label.userid" /></th>
            <td><input type="text" id="user" name="user" size="24" /></td>
          </tr>
          <tr>
            <th><g:message code="auth.login.label.password" /></th>
            <td><input type="password" name="pass" size="24" /></td>
          </tr>
        </table>
        <br/>
        <input type="submit" name="doit" value="${message(code:'trauma.sign.action.sign')}" />
      
        <%-- TODO: recordar clave
        <div align="center">
          <g:link action="forgotPassword"><g:message code="auth.login.action.forgotPass" /></g:link>
        </div>
        --%>
      
      </g:form>
    </g:isNotSignedRecord>
  </body>
</html>
