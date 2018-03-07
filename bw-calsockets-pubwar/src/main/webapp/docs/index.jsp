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
    <title>Bedework Account Creation</title>
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
        $('#fname').focus();
        $( "#newAccount" ).on( "submit", function( event ) {
          event.preventDefault();
          if (!captchaOk) {
            $("#captchaError").show();
            return false;
          } else {
            $(".contentTab").hide();
            $("#bwPleaseWait").show();
            $.ajax({
              url: '/selfreg/newid',
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
        <div id="bw-account-box">
          <h1>Create Bedework Account</h1>

          <form id="newAccount" action="/selfreg/newid" method="post">

            <fieldset>
              <div class="fieldCol">
                <label for="fname">
                  First Name<span class="req">*</span>
                </label>
                <input type="text" id="fname" name="fname" required  tabindex="1"/>
              </div>

              <div class="fieldCol">
                <label for="lname">
                  Last Name<span class="req">*</span>
                </label>
                <input type="text" id="lname" name="lname" required  tabindex="2"/>
              </div>

              <label for="email">
                Email Address<span class="req">*</span>
              </label>
              <input type="email" id="email" name="email" required  tabindex="3"/>
              <div id="email-validation-error" class="validation-error" style="display:none;"></div>

              <c:if test="${canSpecifyAccount}">
                <label for="account">
                  Account(optional)<span class="req">*</span>
                </label>
                <input type="text" id="account" name="account" tabindex="4"/>
              </c:if>

              <c:if test="${!pwIsToken}">
                <label for="pw">
                  Password<span class="req">*</span>
                </label>
                <input type="password" id="pw" name="pw" required  tabindex="5"/>
              </c:if>

              <div id="captchaError" class="validation-error" style="display: none;">Please answer the captcha:</div>
              <div id="recaptcha" tabindex="6"></div>
            </fieldset>

            <fieldset>
              <button type="submit" tabindex="7">Create</button>
            </fieldset>

          </form>
        </div>
      </div>
      <div id="bwPleaseWait" class="contentTab" style="display: none;">
        <h3>Registration processing</h3>
        <p>
          Please wait...
          <img src="docs/resources/ajax-loader.gif"/>
        </p>
      </div>
      <div id="bwMessageSent" class="contentTab" style="display: none;">
        <h2>Email Confirmation Required</h2>
        <p>
          Thank you for signing up for a Bedework Guest ID. The final step is to verify your email address.
          Please check your email inbox for a message from Bedework containing a confirmation link.
        </p>
        <p>
          Please click on the link in this email to confirm your Guest ID. Once your ID is confirmed, you can
          then register for Bedework Events.
        </p>
      </div>
      <div id="bwRegError" class="contentTab" style="display: none">
        <h2>New Account Registration: Error</h2>
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