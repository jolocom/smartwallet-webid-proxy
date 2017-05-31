<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="com.jolocom.webidproxy.config.Config" %>

<html>
<head>

<style>
* { font-family: sans-serif; }
</style>

<script type="text/javascript" src="jquery-2.0.3.min.js"></script>

<script type="text/javascript">

function register() {

	var regtarget = $("#regtarget").val();

	var username = $("#regusername").val();
	var password = $("#regpassword").val();
	var name = $("#regname").val();
	var email = $("#regemail").val();

	$.post(regtarget, {"username":username,"password":password,"name":name,"email":email})
	.done(function() {
		alert("success!");
	})
	.fail(function(err) {
		alert("error: " + JSON.stringify(err));
	});
}

function login() {

	var logintarget = $("#logintarget").val();

	var username = $("#logusername").val();
	var password = $("#logpassword").val();

	$.post(logintarget, {"username":username,"password":password})
	.done(function() {
		alert("success!");
	})
	.fail(function(err) {
		alert("error: " + JSON.stringify(err));
	});
}

function logout() {

	var logouttarget = $("#logouttarget").val();

	$.post(logouttarget)
	.done(function() {
		alert("success!");
	})
	.fail(function(err) {
		alert("error: " + JSON.stringify(err));
	});
}

function forgotpassword() {

	var forgotpasswordtarget = $("#forgotpasswordtarget").val();

	var username = $("#forgotpasswordusername").val();

	$.post(forgotpasswordtarget, {"username":username})
	.done(function() {
		alert("success!");
	})
	.fail(function(err) {
		alert("error: " + JSON.stringify(err));
	});
}

function resetpassword() {

	var resetpasswordtarget = $("#resetpasswordtarget").val();

	var username = $("#resetpasswordusername").val();
	var code = $("#resetpasswordcode").val();
	var password = $("#resetpasswordpassword").val();

	$.post(resetpasswordtarget, {"username":username,"code":code,"password":password})
	.done(function() {
		alert("success!");
	})
	.fail(function(err) {
		alert("error: " + JSON.stringify(err));
	});
}

function verifyemail() {

	var verifyemailtarget = $("#verifyemailtarget").val();

	var username = $("#verifyemailusername").val();
	var code = $("#verifyemailcode").val();

	$.post(verifyemailtarget, {"username":username,"code":code})
	.done(function() {
		alert("success!");
	})
	.fail(function(err) {
		alert("error: " + JSON.stringify(err));
	});
}

function exportkey() {

	var target = $("#exportkeytarget").val();
	
	window.location.href = target;
}

function importkey() {

	var importkeytarget = $("#importkeytarget").val();

	var file = $("#importkeyfile")[0].files[0];

	var formdata = new FormData();
	formdata.append("file", file);
	$.ajax({
		url: importkeytarget,
		type: "POST",
		data: formdata,
		processData: false,
		contentType: false
	})
	.done(function(data) {
		alert("success: " + data);
	})
	.fail(function(err) {
		alert("error: " + JSON.stringify(err));
	});
}

function post() {

	var target = $("#target").val();
	var content = $("#content").val();

	$.ajax({
      url: target,
      type: "POST",
      data: content,
      headers: { 
        "Accept" : "text/turtle; charset=utf-8",
        "Content-Type": "text/turtle; charset=utf-8"
      },
      xhrFields: {
        withCredentials: true
      }
    })
	.done(function(data) {
		alert("success: " + data);
	})
	.fail(function(err) {
		alert("error: " + JSON.stringify(err));
	});
}

function put() {

	var target = $("#target").val();
	var content = $("#content").val();

	$.ajax({
      url: target,
      type: "PUT",
      data: content,
      headers: { 
        "Accept" : "text/turtle; charset=utf-8",
        "Content-Type": "text/turtle; charset=utf-8"
      },
      xhrFields: {
        withCredentials: true
      }
    })  
	.done(function(data) {
		alert("success: " + data);
	})
	.fail(function(err) {
		alert("error: " + JSON.stringify(err));
	});
}

function delet() {

	var target = $("#target").val();

	$.ajax({
      url: target,
      type: "DELETE",
      xhrFields: {
        withCredentials: true
      }
    })
	.done(function(data) {
		alert("success: " + data);
	})
	.fail(function(err) {
		alert("error: " + JSON.stringify(err));
	});
}

function get() {

	var target = $("#target").val();
	var content = $("#content").val();

	$.get(target, content)
	.done(function(data) {
		alert("success: " + data);
	})
	.fail(function(err) {
		alert("error: " + JSON.stringify(err));
	});
}

var ws = null;

function wsStart() {

	if (ws) wsStop();

	var wsurl = $("#wsurl").val();

	ws = new WebSocket(wsurl, null);

	ws.onmessage = function(event) {

		$('#wsmessages').val($('#wsmessages').val() + event.data + "\n");
	};

	ws.onerror = function(event) {

		alert('WebSocket error: ' + event.data);
	};

	ws.onopen = function(event) {

		alert('WebSocket opened.');
		$('#wsmessages').val('');
	};

	ws.onclose = function(event) {

		alert('WebSocket closed: ' + event.code + ' ' + event.reason);
		$('#wsmessages').val('');
	};
}

function wsStop() {

	if (! ws) { alert('No open WebSocket.'); return; }

	ws.close();
	ws = null;
}

function wsMessage() {

	if (! ws) { alert('No open WebSocket.'); return; }

	var wsMessage = $("#wsmessage").val().trim();

	ws.send(wsMessage);
}

</script>

</head>
<body>
<p><button onclick="register();">register:</button>
<input id="regusername" type="text" size="20" value="testuser1">
<input id="regpassword" type="password" size="20" value="secret">
<input id="regname" type="text" size="20" value="My Name">
<input id="regemail" type="text" size="20" value="my@email.com"><br>
<input id="regtarget" type="text" size="40" value="http://localhost:8111/register"></p>
<hr>
<p><button onclick="verifyemail();">verify email:</button>
<input id="verifyemailusername" type="text" size="20" value="testuser1">
<input id="verifyemailcode" type="text" size="20" value="12345">
<input id="verifyemailtarget" type="text" size="40" value="http://localhost:8111/verifyemail"></p>
<hr>
<p><button onclick="login();">login:</button>
<input id="logusername" type="text" size="20" value="testuser1">
<input id="logpassword" type="password" size="20" value="secret"><br>
<input id="logintarget" type="text" size="40" value="http://localhost:8111/login"></p>
<hr>
<p><button onclick="logout();">logout:</button><br>
<input id="logouttarget" type="text" size="40" value="http://localhost:8111/logout"></p>
<hr>
<p><button onclick="forgotpassword();">forgot password:</button>
<input id="forgotpasswordusername" type="text" size="20" value="testuser1">
<input id="forgotpasswordtarget" type="text" size="40" value="http://localhost:8111/forgotpassword"></p>
<hr>
<p><button onclick="resetpassword();">reset password:</button>
<input id="resetpasswordusername" type="text" size="20" value="testuser1">
<input id="resetpasswordcode" type="text" size="20" value="12345">
<input id="resetpasswordpassword" type="password" size="20" value="secret"><br>
<input id="resetpasswordtarget" type="text" size="40" value="http://localhost:8111/resetpassword"></p>
<hr>
<p><button onclick="exportkey();">export key:</button><br>
<input id="exportkeytarget" type="text" size="40" value="http://localhost:8111/exportkey"></p>
<hr>
<p><button onclick="importkey();">import key:</button><br>
<form id="importkeyfileform" name="importkeyfileform" enctype="multipart/form-data"><input id="importkeyfile" type="file"></form>
<input id="importkeytarget" type="text" size="40" value="http://localhost:8111/importkey"></p>
<hr>
<p><button onclick="post();">http post:</button> <button onclick="put();">http put:</button>
<button onclick="get();">http get:</button> <button onclick="delet();">http delete:</button>
<input id="target" type="text" size="80" value="http://localhost:8111/proxy?url=https://testuser1.<%= Config.webidHost() %>/profile/card"></p>
<textarea id="content" cols="140" rows="30">
@prefix rdf: &lt;http://www.w3.org/1999/02/22-rdf-syntax-ns#&gt; .

&lt;&gt;
    &lt;http://purl.org/dc/terms/title&gt; &quot;WebID profile of Lal Laaa&quot; ;
    a &lt;http://xmlns.com/foaf/0.1/PersonalProfileDocument&gt; ;
    &lt;http://xmlns.com/foaf/0.1/maker&gt; &lt;#me&gt; ;
    &lt;http://xmlns.com/foaf/0.1/primaryTopic&gt; &lt;#me&gt; .

&lt;#key&gt;
    a &lt;http://www.w3.org/ns/auth/cert#RSAPublicKey&gt; ;
    &lt;http://www.w3.org/ns/auth/cert#exponent&gt; &quot;65537&quot;^^&lt;http://www.w3.org/2001/XMLSchema#int&gt; ;
    &lt;http://www.w3.org/ns/auth/cert#modulus&gt; &quot;97b8734ca04f16beaa3de34feb8cb8689714fe057102539cc1f48bf117cdab5396b3fbfe5bc460061d2cfaea019bf0245f2748d5db3be7cf7a4832714442fdf7eb3e277f0232939e0333a5ec675416ae929d3edf55a39aa0d77227466ca8d31905c88e4e9c4c629e16841389288d75ef7e15c369cba741e2872abf9bf61adeeee281b17c430128a2fe68812330413872877b5754f66e3c67d7f0fe2d9d326bfab7bc27864c2446d45864200590ce3d7595b503bb492517922337bf613cce3ef25fda48e12ea61ba386eb6b294f6b960701402af5b0597456eb5fbabc869c52900764f84c90d3b6e8c277d0ed452f4380dcfb2be2a679ebdb86ba3aa6cf234919&quot;^^&lt;http://www.w3.org/2001/XMLSchema#hexBinary&gt; .

&lt;#me&gt;
    a &lt;http://xmlns.com/foaf/0.1/Person&gt; ;
    &lt;http://www.w3.org/ns/auth/cert#key&gt; &lt;#key&gt; ;
    &lt;http://xmlns.com/foaf/0.1/mbox&gt; &lt;mailto:lala@gmail.com&gt; ;
    &lt;http://xmlns.com/foaf/0.1/name&gt; &quot;Lal Laaa 233&quot; .

</textarea>

<hr noshade>

<table>
<tr><td>WebSocket URL:</td><td><input type="text" id="wsurl" size="80" value="ws://localhost:8111/websocket/https://<%= Config.webidHost() %>/profile/card"></td></tr>
<tr><td><button onclick="wsStart();">Start WebSocket</button></td><td><button onclick="wsStop();">Stop WebSocket</button></td></tr>
</table>
<textarea id="wsmessages" cols="140" rows="30"></textarea>
<table>
<tr>
<td>WebSocket Message:</td>
<td><input type="text" id="wsmessage" size="40" value="sub https://testuser1.mywebid.com:8443/profile/card2"></td>
<td><button onclick="wsMessage();">Send Message</button></td>
</tr>
</table>

</body>
</html>
