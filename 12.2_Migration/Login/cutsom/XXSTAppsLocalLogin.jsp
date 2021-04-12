<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>OFA Login Page</title>
<script type="text/javascript" src="XXSTlogin.js"></script>
<link type="text/css" rel="stylesheet" href="XXSTlogin.css" />
</head>

<body style="background-color: #214b6c;">
	<%
		response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	%>
	<div id=logo class="logo">
		<img src="oracle_white_logo.png" alt="Oracle Logo" title="Oracle Logo">
	</div>

	<div class="login_panel">
		<form action="ofa" method=post class="img-container" id=login>
			<label color=#e8e8e8; font-weight=bold;>User Name</label><br> <input
				type="text" name="usrname" class="inp" id="usernameField"><br>
			<label color=#e8e8e8; font-weight=bold;>Password</label><br> <input
				type="password" name="pass" class="inp" id="passwordField"><br>
			<!--	<input type="submit" value="login"> -->
		</form>



		<div id=ButtonBox class="control_box center">

			<button tabindex=0 class="OraButton left" message=FND_SSO_LOGIN
				onclick="submitCredentials()">Login</button>
			<button tabindex=0 class="OraButton right" message=FND_SSO_CANCEL
				onclick="handleCancel()">Cancel</button>
		</div>
	</div>


</body>
</html>