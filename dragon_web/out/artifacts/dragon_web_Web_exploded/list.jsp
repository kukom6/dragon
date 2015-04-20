<%@page contentType="text/html;charset=utf-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html>
<html>
<body>

<table border="1">
    <thead>
    <tr>
        <th>meno</th>
        <th>datum narodenia</th>
        <th>rasa</th>
        <th>number of heads</th>
        <th>weight</th>
    </tr>
    </thead>
    <c:forEach items="${dragons}" var="dragon">
        <tr>
            <form method="post action="${pageContext.request.contextPath}/dragons/update">
                <td><input name="name" type="text" value="${dragon.name}" /></td>
                <fmt:formatDate value="${dragon.born}" type="BOTH" var="parsedBornDate" pattern="yyyy-MM-dd HH:mm:ss" />
                <c:set var="string2" value="${fn:replace(parsedBornDate, ' ', 'T')}" />
                <td><input name="born" type="datetime-local" step="1" value="${string2}"/></td>
                <td><input name="race" type="text" value="${dragon.race}"/></td>
                <td><input name="heads" type="text" value="${dragon.numberOfHeads}"/></td>
                <td><input name="weight" type="text" value="${dragon.weight}"/></td>
                <input type="hidden" name="id" value="${dragon.id}"/>
                <td><input type="submit" value="Update"/></td>
            </form>
            <td><form method="post" action="${pageContext.request.contextPath}/dragons/delete?id=${dragon.id}"
                      style="margin-bottom: 0;"><input type="submit" value="Smazat"></form></td>
        </tr>
    </c:forEach>
</table>

<h2>Zadejte draka</h2>
<c:if test="${not empty chyba}">
    <div style="border: solid 1px red; background-color: yellow; padding: 10px">
        <c:out value="${chyba}"/>
    </div>
</c:if>
<form action="${pageContext.request.contextPath}/dragons/add" method="post">
    <table>
        <tr>
            <th>Name:</th>
            <td><input type="text" name="name" value="<c:out value='${param.name}'/>"/></td>
        </tr>
        <tr>
            <th>Born:</th>
            <td><input type="datetime-local" name="born" step="1" value="<c:out value='${param.born}'/>"/></td>
        </tr>
        <tr>
            <th>Race:</th>
            <td><input type="test" name="race" value="<c:out value='${param.race}'/>"/></td>
        </tr>
        <tr>
            <th>Heads:</th>
            <td><input type="text" name="heads" value="<c:out value='${param.heads}'/>"/></td>
        </tr>
        <tr>
            <th>Weight:</th>
            <td><input type="text" name="weight" value="<c:out value='${param.weight}'/>"/></td>
        </tr>
    </table>
    <input type="Submit" value="Zadat" />
</form>
</body>
</html>