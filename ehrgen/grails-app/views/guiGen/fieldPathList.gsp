<html>
<body>
<h1>fieldPathlist</h1>

<ul>
  <g:each in="${mapping}" var="entry">
    <li>${entry.key}: ${entry.value}</li>
  </g:each>
</ul>

</body>
</html>