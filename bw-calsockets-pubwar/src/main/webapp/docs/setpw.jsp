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
      $('#pw').focus();
      if (window.location.search.indexOf("confid") == -1) {
        $(".contentTab").hide();
        $("#standardMessage").show();
      }
      var setpwUrl = "/selfreg/setpw" + window.location.search;
      $( "#fpw" ).on( "submit", function( event ) {
        event.preventDefault();
        if (!captchaOk) {
          $("#captchaError").show();
          return false;
        } else {
          $(".contentTab").hide();
          $("#bwPleaseWait").show();
          $.ajax({
            url: setpwUrl,
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
              $("#bwConfirmed").show();
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
    <h2>Forgotten Password</h2>
    <p>
      Please enter your new password in the form below.
    </p>

    <form id="fpw" action="#" method="post">
      <h3>Step 2 of 2: Set Password</h3>
      <ul class="loginBox">
        <li>
          <label for="pw">
            Password<span class="req">*</span>
          </label>
          <div class="fieldVal">
            <input type="password" id="pw" name="pw" required="required" tabindex="1"/>
          </div>
        </li>
        <li class="alignCol">
          <div id="captchaError" class="validation-error" style="display: none;">Please answer the captcha:</div>
          <div id="recaptcha" tabindex="2"></div>
        </li>
        <li class="alignCol submitButton">
          <button type="submit" class="ui-button" tabindex="3">Set Password</button>
        </li>
      </ul>
    </form>

  </div>
  <div id="standardMessage" class="contentTab" style="display: none;">
    <h2>Bedework Events Calendar</h2>
    <p>You have likely arrived at this page in error.</p>
  </div>
  <div id="bwPleaseWait" class="contentTab" style="display: none;">
    <h3>Request processing</h3>
    <p>
      Please wait...
      <img src="docs/resources/ajax-loader.gif"/>
    </p>
  </div>
  <div id="bwConfirmed" class="contentTab" style="display: none;">
    <h2>Password Reset Confirmed</h2>
    <p>
      <strong>Your password has been reset.</strong>
    </p>
  </div>
  <div id="bwRegError" class="contentTab" style="display: none">
    <h2>Reset Password: Error</h2>
    <p id="statusMessage" style="display: none;">
    </p>
    <p>
      There was an error submitting your request.<br/>
      For help please contact your administrator.
    </p>
  </div>
</div>
<footer>
  <jsp:include page="theme/customFooter.jsp" />
</footer>
</body>
</html>