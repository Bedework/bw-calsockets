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
  <script src='https://www.google.com/recaptcha/api.js?onload=loadCaptcha&render=explicit'>/* recaptcha */</script>
  <script type="text/javascript">
    var captchaOk = false;
    var captchaContainer = null;
    var loadCaptcha = function() {
      captchaContainer = grecaptcha.render('recaptcha', {
        'sitekey' : '${sitekey}',
        'callback' : function(response) {
          captchaOk = true;
        }
      });
    };

    $(document).ready(function() {
      $('#email').focus();
      $( "#fid" ).on( "submit", function( event ) {
        event.preventDefault();
        if (!captchaOk) {
          $("#captchaError").show();
          return false;
        } else {
          $(".contentTab").hide();
          $("#bwPleaseWait").show();
          $.ajax({
            url: '/selfreg/fid',
            type: 'post',
            dataType: 'json',
            data: $(this).serialize()
          }).done(function (data) {
            if (data.status == 'failed') {
              $("#statusMessage").text(data.msg).show();
              $(".contentTab").hide();
              $("#bwRegError").show();
            } else {
              $(".contentTab").hide();
              $("#bwMessageSent").show();
            }
          }).error(function () {
            $(".contentTab").hide();
            $("#bwRegError").show();
          });
        }
      });
    });
  </script>
</head>
<body>
<header>
  <jsp:include page="theme/customHeader.jsp" />
</header>
<div id="bw-selfreg">
  <div id="bwNewAccount" class="contentTab">
    <h2>Forgotten ID</h2>
    <p>
      To register for events in the Bedework Events Calendar,
      you need to have an account in our system.
    </p>
    <p>
      If you have an account on this system, and you have forgotten your calendar user ID, please submit your
      email in the form below to retrieve it.
    </p>

    <form id="fid" action="#" method="post">
      <h3>Retrieve a Forgotten User ID</h3>
      <ul class="loginBox">
        <li>
          <label for="email">
            Email Address<span class="req">*</span>
          </label>
          <div class="fieldVal">
            <input type="email" id="email" name="email" required="required" tabindex="1"/>
          </div>
        </li>
        <li class="alignCol">
          <div id="captchaError" class="validation-error" style="display: none;">Please answer the captcha:</div>
          <div id="recaptcha" tabindex="2"></div>
        </li>
        <li class="alignCol submitButton">
          <button type="submit" class="ui-button" tabindex="3">Retrieve ID</button>
        </li>
      </ul>
    </form>

  </div>
  <div id="bwPleaseWait" class="contentTab" style="display: none;">
    <h3>Request processing</h3>
    <p>
      Please wait...
      <img src="docs/resources/ajax-loader.gif"/>
    </p>
  </div>
  <div id="bwMessageSent" class="contentTab" style="display: none;">
    <h2>Forgotten ID: Email Sent</h2>
    <p>
      <strong>Please check your email to retrieve your calendar user ID.</strong>
    </p>
  </div>
  <div id="bwRegError" class="contentTab" style="display: none">
    <h2>Forgotten ID: Error</h2>
    <p id="statusMessage" style="display: none;">
    </p>
    <p>
      There was an error submitting your request.<br/>
      For help please contact your administrator.
    </p>
    <p>
      <a href="javascript:location.reload()">Return</a>
    </p>
  </div>
</div>
<footer>
  <jsp:include page="theme/customFooter.jsp" />
</footer>
</body>
</html>