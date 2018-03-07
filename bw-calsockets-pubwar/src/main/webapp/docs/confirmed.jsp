<%-- ********************************************
Licensed to Jasig under one or more contributor license
agreements. See the NOTICE file distributed with this work
for additional information regarding copyright ownership.
Jasig licenses this file to you under the Apache License,
Version 2.0 (the "License"); you may not use this file
except in compliance with the License. You may obtain a
copy of the License at:

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on
an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied. See the License for the
specific language governing permissions and limitations
under the License.
--%><%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%--
--%><%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %><%--
--%><c:set var="req" value="${pageContext.request}" /><%--
--%><c:set var="url">${req.requestURL}</c:set><%--
--%><c:set var="uri" value="${req.requestURI}" /><%--
--%><!DOCTYPE html>
<html>
<head lang="en">
  <meta charset="UTF-8">
  <title>Bedework Account Confirmation</title>
  <script src="docs/resources/jquery-1.11.3.min.js" type="text/javascript">/* jQuery */</script>
  <script src="docs/resources/selfreg.js" type="text/javascript">/* Selfreg */</script>
  <link href="docs/resources/selfreg.css" media="screen,all" type="text/css" rel="stylesheet" />
  <link href="docs/theme/custom.css" media="screen,all" type="text/css" rel="stylesheet" />
  <base href="${fn:substring(url, 0, fn:length(url) - fn:length(uri))}${req.contextPath}/">
  <script type="text/javascript">
    $(document).ready(function() {
      if (window.location.search.indexOf("account") == -1) {
        $(".contentTab").hide();
        $("#standardMessage").show();
      } else {
        $("#useridText").html(getQsParamVal("account"));
      }
    });
  </script>
</head>
<body>
<header>
  <jsp:include page="theme/customHeader.jsp" />
</header>
<div id="bw-selfreg">
  <div id="bwNewAccount" class="contentTab">
    <h2>Email confirmed!</h2>
    <p>
      Your Bedework Events Guest ID has been created.
    </p>
    <p class="showBig">
      Your Bedework Guest ID is:
      <span id="useridText"></span>
      <c:if test='${not empty param["tkn"]}'>
        and the password is <span id="pwText">${param["tkn"]}</span>
      </c:if>
    </p>
    <p>
      You may use this Guest ID and the password you provided to register for events on the
      Bedework Events Calendar.
    </p>
  </div>
  <div id="standardMessage" class="contentTab" style="display: none;">
    <h2>Bedework Events Calendar</h2>
    <p>You have likely arrived at this page in error.</p>
  </div>
</div>
<footer>
  <jsp:include page="theme/customFooter.jsp" />
</footer>
</body>
</html>