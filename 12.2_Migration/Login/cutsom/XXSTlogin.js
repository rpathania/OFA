
/*$Header: XXSTlogin.js 001.000.000 2021/04/09 12:25:56 rpathania $*/

function test() {
	alert("test JS call"); // added sample text
}

function xxstCallAuthServlet(params) {
	console.log("Servlet called");
	var xhr = new XMLHttpRequest();
	//var xmlreq = getXMLHttpRequest();
	xhr.onreadystatechange = function() {
		if (xhr.readyState == 4) {
			var data = xhr.responseText;
			alert(data);
			// IE doesn't like pretty-printed Json
			eval("result="
					+ data.replace(/[\n\r\t\s]/g, '').replace(/,}/g, '}'));
			data.http_status = xhr.status;
			action(data);
		}
	}
	xhr.open('POST', 'AuthenticateUser', true);
	xhr.send(params);

}

function handleCancel() {
	var login = document.getElementById('login');
	if (login) { 
		login.usernameField.value = '';
		// login.usernameField.disabled = currentUser != '';
		login.passwordField.value = '';
	}
}


	function submitCredentials() {
		var login = document.getElementById('login');
		if (!login) {
			return;
		}
		var u = login.usernameField;
		if (!u) {
			return;
		}
		/*if (u.value == "") {
			return displayErrorCode('FND_APPL_LOGIN_FAILED');
		}*/
		var p = login.passwordField;
		if (!p) {
			return;
		}
		/*	if (p.value == "") {
			return displayErrorCode('FND_APPL_LOGIN_FAILED');
		}*/
		//lock();

		var params = "username=" + encodeURIComponent(u.value) + "&password="
		+ encodeURIComponent(p.value);

		var ac = "N";
		if (ac) {
			params = params + "&_lAccessibility="
			+ encodeURIComponent(ac);
		}

		// bug 22928920: need to send both display and langCode parameter
		var sl = "US";
		if (sl) {
			params = params + "&displayLangCode="
			+ encodeURIComponent(sl);
		}

		var lc = "US";
		if (lc) {
			params = params + "&langCode=" + encodeURIComponent(lc);
		}

		console.log(params)
		//spinner(true);
		xxstCallAuthServlet(params);
	}