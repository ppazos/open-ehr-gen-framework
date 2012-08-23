<?xml version="1.0" encoding="UTF-8" ?>
<html>
  <head>
    <meta name="layout" content="ehr" />
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title><g:message code="trauma.reopenRecord.title" /></title>
    <g:javascript library="prototype/prototype" />
    <g:javascript>
      <!--
        Event.observe(window, 'load', function(event) {

          // Focus en input nombre de usuario
          $('user').focus();

        });
      -->
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
      table #reopen_table {
        width: 290px;
      }
      #form1 input[type=submit] {
        position: relative;
        float: right;
      }
    </style>
  </head>
  <body>
    <h1><g:message code="trauma.reopenRecord.title" /></h1>

    <g:if test="${flash.error}">
      <div class="error"><g:message code="${flash.error}" /></div>
    </g:if>
    <g:if test="${flash.message}">
      <div class="message"><g:message code="${flash.message}" /></div>
    </g:if>

    <g:form url="[action:'reopenRecord', id:params.id]" method="post" id="form1" class="ehrform">

      <table id="reopen_table" align="center">
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

      <input type="submit" name="doit" value="${message(code:'trauma.reopen.action.reopen')}" />

    </g:form>
  </body>
</html>