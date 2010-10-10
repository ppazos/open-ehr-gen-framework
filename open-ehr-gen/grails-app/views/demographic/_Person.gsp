<%@ page import="util.DateDifference" %><%@ page import="java.text.SimpleDateFormat" %>

<%-- in: person:Person --%>

<g:render template="../demographic/UIDBasedID" collection="${person.ids}" var="id" />

<%
def nombres = person.identities.find{ it.purpose == 'PersonName' }
%>

${nombres.primerNombre}
${nombres.segundoNombre}
${nombres.primerApellido}
${nombres.segundoApellido}
( ${person.sexo} )
<br/>

<%--
${person.fechaNacimiento.getClass()}
--%>

<%
if (person.fechaNacimiento)
{
  def myFormatter = new SimpleDateFormat( "yyyy-MM-dd" )
  print myFormatter.format(person.fechaNacimiento)
  
  print " ( " + DateDifference.numberOfYears(person.fechaNacimiento, new Date()) + " )"
}
%>
