<%@ 
page contentType="text/html;charset=UTF-8" language="java"%><%@ 
taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %><%
response.setHeader("Cache-Control", "no-cache"); //HTTP 1.1
response.setHeader("Pragma", "no-cache"); //HTTP 1.0
response.setDateHeader("Expires", 0); //prevents caching at the proxy server
%><!DOCTYPE html>
<html>
  <head>
    <title>Blog</title>
    <link rel="stylesheet" href="/style.css">
  </head>
<body>
  Welcome to a simple blog!
  <c:if test="${postId != null}">
    <a href="/">Back to home</a>
  </c:if>
  <br/>
  <c:forEach items="${entities}" var="entity">
    <div class="blogEntry">
        <a href="/?postId=<c:out value="${entity.id}" />">On <c:out value="${entity.postdate}" /> <c:out value="${entity.name}" /> wrote:</a>
        <div class="message">
          <c:out value="${entity.message}" />
        </div>
        <div class="following">
          <c:choose>
            <c:when test="${entity.following == 'true' }">
              Following this user (<a href="/follow?action=unfollow&name=<c:out value="${entity.name}" /><c:out value="${queryString}" />">un-follow</a>)
            </c:when>
            <c:otherwise>
              Not following (<a href="/follow?action=follow&name=<c:out value="${entity.name}" /><c:out value="${queryString}" />">follow</a>)
            </c:otherwise>
          </c:choose>
        </div>
        <c:if test="${comments != null}">
        Comments:
        <div class="comments">
          <c:forEach items="${comments}" var="comment">
            <div class="comment">
              <c:out value="${comment.name}" /> - <c:out value="${comment.message}" />
            </div>
          </c:forEach>
        </div>
        </c:if>
    </div>
  </c:forEach>
  
      
  <br/><br/>
  
  <c:if test="${postId == null}">
    Post a message:
    <form method="post" action="/">
      <input type="hidden" name="action" value="post" /> 
      Message: <textarea name="message"> </textarea> <br />
      <input type="submit" value="Submit" />
    </form>
  </c:if>
  
  <c:if test="${postId != null}">
    Post a comment:
    <form method="post" action="/">
      <input type="hidden" name="action" value="comment" />
      <input type="hidden" name="postId" value="<c:out value="${postId}" />" /> 
      Message: <textarea name="message"> </textarea> <br />
      <input type="submit" value="Submit" />
    </form>
  </c:if>
  
</body>
</html>